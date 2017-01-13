package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.*;

// for fancy features
import javafx.scene.paint.Paint;
import javafx.geometry.Pos;

import java.net.URISyntaxException;
import java.util.regex.*;

import javafx.application.Platform;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.*;

import io.socket.client.*;
import io.socket.emitter.Emitter;
//import com.github.nkzawa.socketio.client.*;
//import com.github.nkzawa.emitter.Emitter;
import org.json.*;

class Serializer {

    public static byte[] serialize(Object obj) throws IOException {
        try(ByteArrayOutputStream b = new ByteArrayOutputStream()){
            try(ObjectOutputStream o = new ObjectOutputStream(b)){
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try(ByteArrayInputStream b = new ByteArrayInputStream(bytes)){
            try(ObjectInputStream o = new ObjectInputStream(b)){
                return o.readObject();
            }
        }
    }

}

public class Main extends Application {

    /* Layout Components */
    private static Stage window;
    private static Scene scene1, scene0, scene2;

    /* Connection Variables */
    private Socket mSocket;
    private boolean loginSuccess = false;
    private int registerSuccess = 0;
    private boolean messageSuccess = false;
    private boolean logTransmit = false;

    /* Personal Data */
    private User user;
    private String nowTalking = "";
    private String nowMessage = "";

    private String logUser = "";
    /* Message Display Enable*/
    private BooleanProperty displayOnScreen = new SimpleBooleanProperty(false);
    private BooleanProperty logNotice = new SimpleBooleanProperty(false);

    private Queue<String> logMessageSender = new LinkedList<String>();
    private Queue<String> logMessage = new LinkedList<String>();
    private Queue<String> fileDownloadPlace = new LinkedList<String >();
    private Queue<String> fileToDownload = new LinkedList<String >();

    public class UserConfig {
        public String id;
        public String pwd;

        public UserConfig(String line){
            StringTokenizer st = new StringTokenizer(line);
            id = st.nextToken(":");
            pwd = st.nextToken();
        }
    }

    private boolean repeated(String gotstring){
        /* TODO: check repeated username info */
        return false;
    }

    private boolean login(String id, String pwd)  throws Exception {

        try {
            FileInputStream in = new FileInputStream(Constants.USERCONFIG);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line = reader.readLine();
            while(line != null){
                UserConfig user = new UserConfig(line);
                if(user.id.equals(id) && user.pwd.equals(pwd)){
                    return true;
                } else {
                    line = reader.readLine();
                }
            }
            in.close();
            return false;

        } catch (Exception e){
            e.printStackTrace();
        }

        return false; // nothing
    }

    private void register(String id, String pwd) throws Exception {

        try {
            FileOutputStream out = new FileOutputStream(Constants.USERCONFIG, true);
            String content = id+":"+pwd+"\n";

            byte[] contentInBytes = content.getBytes();

            out.write(contentInBytes);
            out.flush();
            out.close();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private ArrayList<String> jsArrayParse(String jsArray){
        StringTokenizer st = new StringTokenizer(jsArray);
        String tmp;
        ArrayList<String> target = new ArrayList<String>();
        for (int i = 0; st.hasMoreTokens(); i++) {
            tmp = st.nextToken("\"");
            if(!st.hasMoreTokens())
                break;
            target.add(st.nextToken("\""));
            System.out.println(target.get(i));
        }
        return target;
    }

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if(args[0].toString().equals("success")) {
                loginSuccess = true;
                System.out.println(args[1]);
                user = new User(jsArrayParse(args[1].toString()));
            }
        }
    };

    private Emitter.Listener onRegister = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if(args[0].toString().equals("success")) {
                registerSuccess = 1;
            } else if(args[0].toString().equals("fail")){
                registerSuccess = -1;
            }
        }
    };

    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if(args[0].toString().equals("success")) {
                messageSuccess = true;
            }
        }
    };

    private Emitter.Listener onMessageReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println(nowTalking);
            if(args[0].toString().equals(nowTalking)) {
                nowMessage = args[1].toString();
              	System.out.println(nowMessage);
                displayOnScreen.set(!displayOnScreen.get());
            }
        }
    };
// FORMAT: ("MSG"/"FIN") (NUM) (USER) (CONTENTS)
    private Emitter.Listener onLogReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("[Log received]: " + args[0].toString() + "; " + args[2].toString() + ": " + args[3].toString());
            if (args[0].toString().equals("MSG")) {
                logTransmit = true;
                logMessageSender.offer(args[2].toString());
                logMessage.offer(args[3].toString());
                logNotice.set(!logNotice.get());
                

            }
            else if (args[0].toString().equals("FIN")) {
                logTransmit = false;
            }
            else {
                System.out.println("[onLogReceived]: Unknown status tag: " + args[0].toString());
            }
        }
    };

    private Emitter.Listener onFileUploadStatus = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if(args[0].toString().equals("success")){
                System.out.println("[FILE] Upload Success");
            } else if (args[0].toString().equals("fail")){
                System.out.println("[FILE] Upload Fail");
            } else
                System.out.println("[FILE] Unknown Status");
        }
    };

    private Emitter.Listener onFileListReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("[FILELIST] something should happen!");
            if(args[0] != null) {
                System.out.println(args[0].toString());
                for (Friend friend: user.friends) {
                    if(friend.getName().equals(nowTalking) ) {
                        friend.setAllFile(jsArrayParse(args[0].toString()));
                        break;
                    }
                }
            }
        }
    };

    private Emitter.Listener onFileReceived = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("[FILE] listener called!");
            if(!args[0].toString().equals("") && !args[1].toString().equals("")){
                System.out.println("[FILE] recv: "+args[0]);
                try {
                    FileOutputStream out = new FileOutputStream(fileDownloadPlace.poll()+"/"+args[0].toString());

                    /* try another */
                    /*ObjectOutputStream oos = new ObjectOutputStream(out);
                    oos.writeObject(args[1]);
                    oos.flush();
                    oos.close();*/

                    /* original method */
                    byte[] binFile = null;
                    try {
                        binFile = Serializer.serialize(args[1]);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    if (args[1] != null) {
                        out.write(binFile);
                        out.flush();
                        out.close();
                        System.out.print("[FILE] recv success");
                    } else {
                        System.out.print("[FILE] null file");
                    }

                } catch (Exception e){
                    e.printStackTrace();
                }
            } else {
                System.out.println("[FILE] recv fail");
            }
        }
    };

    @Override
    public void start(Stage primaryStage) throws Exception {

        /* Setting: window setting */
        // Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        window = primaryStage;
        window.setTitle("CNLINE");

        /* Setting: connection */
        ChatApplication app = new ChatApplication();
        mSocket = app.getSocket();
        mSocket.on("loginAck", onLogin);
        mSocket.on("registerAck", onRegister);
        mSocket.on("messageAck", onMessage);
        mSocket.on("messageFromOther", onMessageReceived);
        mSocket.on("logData" ,onLogReceived);
        mSocket.on("uploadStatus", onFileUploadStatus);
        mSocket.on("listDownloadOptions", onFileListReceived);
        mSocket.on("fileDownloadAck", onFileReceived);
        mSocket.connect();

        /* Scene0: Register */
        GridPane registerPage = new GridPane();
        registerPage.setAlignment(Pos.CENTER);
        registerPage.setHgap(10);
        registerPage.setVgap(10);
        registerPage.setPadding(new Insets(50));
            /* Components */
        HBox registerUsername = new HBox();
        HBox registerPassword = new HBox();
        Text hintUsername = new Text("Username: ");
        Text hintPassword = new Text("Password: ");
        final TextField typeUsername = new TextField();
        final PasswordField typePassword = new PasswordField();
        Button submit = new Button("Submit");
        Button cancel = new Button("Cancel");

        final Label strengthIndicator = new Label("Password Strength Meter"); // fancy feature 1: Password Strength Indicator


        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                typeUsername.setText("");
                typePassword.setText("");

                strengthIndicator.setText("Password Strength Meter");
                strengthIndicator.setBackground(new Background(new BackgroundFill(null, null, null))); // fancy feature 1: Password Strength Indicator

                window.setScene(scene1);
            }
        });
        typePassword.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.ENTER) {
                    String gotUsername = typeUsername.getCharacters().toString();
                    String gotPassword = typePassword.getCharacters().toString();

                    if (gotUsername.equals("")) {
                        AlertBox.display("Attention", "You haven't enter your name!");
                    } /*else if (repeated(gotUsername)) {
                        AlertBox.display("Attention", "This name has been registered!");
                    }*/ else if (gotPassword.length() < Constants.PWDMINLEN) {
                        AlertBox.display("Attention", "This password is too short!");
                    } else if (gotPassword.length() > Constants.PWDMAXLEN) {
                        AlertBox.display("Attention", "This password is too long!");
                    } else {
                        try{
                            /* online version */
                            mSocket.emit("register", gotUsername, gotPassword);
                            TimeUnit.SECONDS.sleep(1);
                            /* offline version */
                            // register(gotUsername, gotPassword);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        switch(registerSuccess){
                            case 1:
                                AlertBox.display("Information", "Success!");
                                window.setScene(scene1);
                                registerSuccess = 0;
                                break;
                            case 0:
                                AlertBox.display("Attention", "The connection is unavailable now");
                                break;
                            case -1:
                                AlertBox.display("Attention", "This name has been registered!");
                                registerSuccess = 0;
                                break;
                        }
                    }
                    typeUsername.setText("");
                    typePassword.setText("");

                    strengthIndicator.setText("Password Strength Meter");
                    strengthIndicator.setBackground(new Background(new BackgroundFill(null, null, null))); // fancy feature 1: Password Strength Indicator
                }
            }
        });

        // fancy feature 1: Password Strength Indicator

        strengthIndicator.setMinWidth(250);
        strengthIndicator.setMinHeight(20);
        strengthIndicator.setAlignment(Pos.CENTER);
        

        typePassword.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                String gotPassword = typePassword.getCharacters().toString();
                int strengthScore = strengthGrader(gotPassword);
                //System.out.printf("You typed something: %s\n", gotPassword); 
                //System.out.printf("Your score: %d\n", strengthScore); 
                meterOutput(strengthScore);
            }
            private void meterOutput(int score) {
                int rate = 0;
                if (score < 0)
                    rate = score;
                else if (0 <= score && score < 25)
                    rate = 0;
                else if (score < 50)
                    rate = 1;
                else if (score < 75)
                    rate = 2;
                else if (score < 100)
                    rate = 3;
                else
                    rate = 4;

                switch (rate) {
                    case -1:
                        strengthIndicator.setText("Password Strength Meter");
                        strengthIndicator.setBackground(new Background(new BackgroundFill(null, null, null)));
                        break;
                    case -2:
                        strengthIndicator.setText("Password length too short!");
                        strengthIndicator.setBackground(new Background(new BackgroundFill(Paint.valueOf("#b3b3b3"), null, null)));
                        break;
                    case -3:
                        strengthIndicator.setText("Password length too long!");
                        strengthIndicator.setBackground(new Background(new BackgroundFill(Paint.valueOf("#b3b3b3"), null, null)));
                        break;
                    case 0:
                        strengthIndicator.setText("Trash!");
                        strengthIndicator.setBackground(new Background(new BackgroundFill(Paint.valueOf("#ff0000"), null, null)));
                        break;
                    case 1:
                        strengthIndicator.setText("Weak!");
                        strengthIndicator.setBackground(new Background(new BackgroundFill(Paint.valueOf("#ff7b00"), null, null)));
                        break;
                    case 2:
                        strengthIndicator.setText("Average!");
                        strengthIndicator.setBackground(new Background(new BackgroundFill(Paint.valueOf("#ffff00"), null, null)));
                        break;
                    case 3:
                        strengthIndicator.setText("Strong!");
                        strengthIndicator.setBackground(new Background(new BackgroundFill(Paint.valueOf("#007fff"), null, null)));
                        break;
                    case 4:
                        strengthIndicator.setText("Secure!");
                        strengthIndicator.setBackground(new Background(new BackgroundFill(Paint.valueOf("#00ff00"), null, null)));
                        break;
                    default:
                        break;
                }
            }
            // derived from https://code.tutsplus.com/tutorials/build-a-simple-password-strength-checker--net-7565
            private int strengthGrader(String gotPassword) {
                int score, baseScore;
                if (gotPassword.length() >= Constants.PWDMINLEN && gotPassword.length() <= Constants.PWDMAXLEN) {
                    baseScore = 40;
                    int upperCount = 0, numberCount = 0, symbolCount = 0, bonus = 0;
                    boolean flatLower = false, flatNumber = false;
                    int excess = gotPassword.length() - Constants.PWDMINLEN;

                    Pattern p = Pattern.compile("[A-Z]");
                    for (int i = 0; i < gotPassword.length(); i++) {
                        Matcher m = p.matcher(gotPassword.substring(i,i+1));
                        if (m.matches()) 
                            upperCount++;
                    }
                    p = Pattern.compile("[0-9]");
                    for (int i = 0; i < gotPassword.length(); i++) {
                        Matcher m = p.matcher(gotPassword.substring(i,i+1));
                        if (m.matches()) 
                            numberCount++;
                    }
                    p = Pattern.compile("[!@#$%^&*?_~]");
                    for (int i = 0; i < gotPassword.length(); i++) {
                        Matcher m = p.matcher(gotPassword.substring(i,i+1));
                        if (m.matches()) 
                            symbolCount++;
                    }

                    if (upperCount > 0 && numberCount > 0 && symbolCount > 0)
                        bonus = 25;
                    else if ( (upperCount > 0 && numberCount > 0) || (upperCount > 0 && symbolCount > 0) || (numberCount > 0 && symbolCount > 0) )
                        bonus = 15;

                    p = Pattern.compile("^[a-z]+$");
                    Matcher m = p.matcher(gotPassword);
                    if (m.matches())
                        flatLower = true;
                    p = Pattern.compile("^[0-9]+$");
                    m = p.matcher(gotPassword);
                    if (m.matches())
                        flatNumber = true;

                    score = baseScore + (excess*3) + (upperCount*5) + (numberCount*4) + (symbolCount*5) + bonus + ( (flatLower)? -20 : 0 ) + ( (flatNumber)? -40 : 0 );

                }
                else if (gotPassword.length() == 0) {
                    score = -1;
                }
                else if (gotPassword.length() < Constants.PWDMINLEN) {
                    score = -2;
                }
                else {
                    score = -3;
                }
                return score;
            }
        });

        /*submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String gotUsername = typeUsername.getCharacters().toString();
                String gotPassword = typePassword.getCharacters().toString();

                if (gotUsername.equals("")) {
                    AlertBox.display("Attention", "You haven't enter your name!");
                } else if (repeated(gotUsername)) {
                    AlertBox.display("Attention", "This name has been registered!");
                } else if (gotPassword.length() < Constants.PWDMINLEN) {
                    AlertBox.display("Attention", "This password is too short!");
                } else if (gotPassword.length() > Constants.PWDMAXLEN) {
                    AlertBox.display("Attention", "This password is too long!");
                } else {
                    try{
                            *//* online version *//*
                        mSocket.emit("register", gotUsername, gotPassword);
                        TimeUnit.SECONDS.sleep(1);
                            *//* offline version *//*
                        // register(gotUsername, gotPassword);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    if(registerSuccess) {
                        AlertBox.display("Information", "Success!");
                        window.setScene(scene1);
                    } else {
                        *//* TODO: disconnect or repeated name *//*
                        AlertBox.display("Attention", "This name has been registered!");
                    }
                }
                typeUsername.setText("");
                typePassword.setText("");

                strengthIndicator.setText("Password Strength Meter");
                strengthIndicator.setBackground(new Background(new BackgroundFill(null, null, null))); // fancy feature 1: Password Strength Indicator
            }
        });*/


        /* layout setting */

        registerUsername.getChildren().addAll(hintUsername, typeUsername);
        registerPassword.getChildren().addAll(hintPassword, typePassword);
        registerPage.add(registerUsername, 0, 0);
        registerPage.add(registerPassword,0,1);
        registerPage.add(strengthIndicator, 0, 2); // fancy feature 1
        registerPage.add(submit, 0, 3);
        registerPage.add(cancel, 0, 4);

        scene0 = new Scene(registerPage, 720, 540);

        /* Scene2: Main */
        GridPane mainPage = new GridPane();
        mainPage.setAlignment(Pos.CENTER);
        mainPage.setHgap(10);
        mainPage.setVgap(10);
        mainPage.setPadding(new Insets(30));

            /* Components */
            /* [!] Scene2's Components */

            /* =================== */

        final VBox messageArea = new VBox(20); /* TODO: maximize the size */
        HBox messageInput = new HBox(10);
        HBox fileTransfer = new HBox(10);
        final TextArea messageLog = new TextArea();
        messageLog.setEditable(false);
	
        final ListView<String> contact = new ListView<String>();
        final ObservableList names = FXCollections.observableArrayList();
        final ArrayList<String> friend = new ArrayList<String>();
        names.addAll(friend);
        contact.setItems(names);
	    contact.getSelectionModel().selectedItemProperty().addListener( new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> ov,
                String old_val, String new_val) {
                System.out.println(new_val);
                if(!nowTalking.equals(new_val)) {
                    nowTalking = new_val;
                    messageLog.setText("");
                    // [OFFMSG] naive method: no local caches

                    try {
                        mSocket.emit("getLog", nowTalking);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    // [OFFMSG] better method: with local caches

                }
            }
        });

        logNotice.addListener( new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                Boolean old_val, Boolean new_val) {
                System.out.println("nowTalking = "+ nowTalking);
                System.out.println("logMessageSender.peek() = "+ logMessageSender.peek());
                Platform.runLater(() -> {
                    if (logMessageSender.peek() != null && logMessage.peek() != null && nowTalking.equals(logMessageSender.peek()) && !nowTalking.equals(user.username)) {
                        messageLog.appendText(logMessageSender.poll()+": "+logMessage.poll()+"\n");
                    }
                    else {
                        logMessageSender.poll();
                        messageLog.appendText("You: "+logMessage.poll()+"\n");
                    }
                });
            }
        });

        displayOnScreen.addListener( new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                Boolean old_val, Boolean new_val) {
                /* auto scroll to bottom */
                messageLog.appendText(nowTalking+": "+nowMessage+"\n");
                //displayOnScreen.set(!displayOnScreen.get());
            }
        });

        final TextField typeMessage = new TextField();

        /* File Transfer */
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        Button uploadFile = new Button("Upload");
        uploadFile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) { /* TODO: handle not selecting */
                List<File> list = fileChooser.showOpenMultipleDialog(window);
                fileChooser.setTitle("Select files");
                fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
                for (File file: list) {
                    if(file != null) {
                        System.out.println(file.getName());
                        byte[] fileData = new byte[(int) file.length()];
                        try {
                            FileInputStream in = new FileInputStream(file);
                            in.read(fileData);
                            in.close();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        try {
                            mSocket.emit("fileUpload", nowTalking, file.getName(), fileData);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Download Place");
        Button downloadFile = new Button("Download");
        downloadFile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                /* request for list of downloadable files */
                try {
                    mSocket.emit("listDownloadFiles", user.username, nowTalking);
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e){
                    e.printStackTrace();
                }

                PopupWindow fileList = new PopupWindow() {
                    @Override
                    public void show(Window owner) {
                        // super.show(owner);
                        Stage pop = new Stage();
                        pop.setTitle("File Select");
                        pop.setMinWidth(250);
                        Scene popScene;
                        Pane pane = new GridPane();

                        ListView<String> filesList = new ListView<String>();
                        ObservableList fileNames = FXCollections.observableArrayList();
                        fileNames.addAll(user.getFriend(nowTalking).getFiles());
                        filesList.setItems(fileNames);
                        filesList.getSelectionModel().selectedItemProperty().addListener( new ChangeListener<String>() {
                            public void changed(ObservableValue<? extends String> ov, String old_val, String new_val) {
                                System.out.println("[FILELIST] select: "+new_val);
                                fileToDownload.offer(new_val); // push to queue of files
                                pop.close();
                            }
                        });

                        pane.getChildren().add(filesList);
                        popScene = new Scene(pane);
                        pop.setScene(popScene);
                        pop.showAndWait();
                    }
                };
                fileList.show(null);

                try {TimeUnit.SECONDS.sleep(1); } catch (Exception e) { e.printStackTrace(); }

                fileDownloadPlace.offer(System.getProperty("user.dir"));

                try {
                    mSocket.emit("fileDownload", user.username, nowTalking, fileToDownload.poll());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                /*DirectoryChooser directoryChooser = new DirectoryChooser();
                File selectedDirectory = directoryChooser.showDialog(window);

                if(selectedDirectory == null){
                    AlertBox.display("Attention", "You haven't choose a dir yet!");
                }else{
                    System.out.println(selectedDirectory.getName());
                    fileDownloadPlace.offer(selectedDirectory.getAbsolutePath());
                    try {
                        mSocket.emit("fileDownload", user.username, nowTalking, fileToDownload.poll());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }*/
            }
        }); /* TODO: window showup */

        Button sendMessage = new Button("send");
            /* TODO: feature: auto-newline */
        typeMessage.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event){
                if(event.getCode() == KeyCode.ENTER) {
                    if(!typeMessage.getCharacters().toString().equals("")) {
                        /* offline version */
                        messageLog.appendText("You: "+typeMessage.getCharacters().toString() + "\n"); /* auto scroll to bottom */
                        /* TODO: online version */
                        try{
                            mSocket.emit("message", nowTalking, typeMessage.getCharacters().toString());
        			        typeMessage.setText("");
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        /*sendMessage.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(!typeMessage.getCharacters().toString().equals("")) {
                    *//* TODO: send to server *//*
                    *//**//*
                    messageLog.appendText(typeMessage.getCharacters().toString() + "\n"); *//* auto scroll to bottom *//*
                    typeMessage.setText("");
                }
            }
        });*/

        /* layout setting */

        typeMessage.setAlignment(Pos.CENTER_LEFT);
        sendMessage.setAlignment(Pos.CENTER_RIGHT);
        messageInput.getChildren().addAll(typeMessage, sendMessage);
        fileTransfer.getChildren().addAll(uploadFile, downloadFile);
        messageInput.setAlignment(Pos.BOTTOM_LEFT);
        messageArea.getChildren().addAll(messageLog, fileTransfer, messageInput);


        mainPage.add(contact, 0, 0);
        mainPage.add(messageArea, 1, 0);

        scene2 = new Scene(mainPage, 720, 540);

        /* Scene1: Login */
        GridPane loginPage = new GridPane();
        loginPage.setAlignment(Pos.CENTER);
        loginPage.setHgap(10);
        loginPage.setVgap(10);
        loginPage.setPadding(new Insets(50));

            /* Welcome Text */
            Text welcome = new Text("Welcome to the CNLINE");
            welcome.setFont(Font.font ("Verdana", 20));

            loginPage.add(welcome, 0, 3);

            /* Login input */
            final TextField usernameTF = new TextField();
            final PasswordField passwordTF = new PasswordField();
            HBox loginHBox = new HBox();
            final Button login = new Button("Login");
            passwordTF.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    if(event.getCode() == KeyCode.ENTER){
                        String gotUsername = usernameTF.getCharacters().toString();
                        String gotPassword = passwordTF.getCharacters().toString();
                        /* Login to Server */
                        try{
                            /* online version */
                            mSocket.emit("login", gotUsername, gotPassword);
                            TimeUnit.SECONDS.sleep(1);
                            /* offline version */
                            // loginSuccess = login(gotUsername, gotPassword);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                        if (user != null)
                            user.username = gotUsername;

                        /* Change the scene or alert */
                        if (loginSuccess) {
                            /* TODO: load user's data */
                            for(int i = 0 ; i < user.friends.size(); i++){
                                //final Button tmp = new Button(user.friends.get(i).getName());
                                final String tmp = user.friends.get(i).getName();
                               // tmp.setOnAction(new EventHandler<ActionEvent>() {
                               //     @Override
                               //     public void handle(ActionEvent event) {
                                        /* TODO: load offline messages */
                               //         if(!nowTalking.equals(tmp)) {
                               //             messageLog.setText("");
                               //             nowTalking = tmp;
                               //         }
                                        /*try {
                                            TimeUnit.SECONDS.sleep(1);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        messageLog.appendText(nowMessage);*/
                               //     }
                               // });
                                friend.add(tmp);
                            }
                            names.addAll(friend);
                            contact.setItems(names);
                            window.setScene(scene2);
                        } else {
                            AlertBox.display("Attention", "The user info is wrong!");
                            usernameTF.setText("");
                            passwordTF.setText("");
                        }
                    }
                }
            });
            /*login.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event){
                    String gotUsername = usernameTF.getCharacters().toString();
                    String gotPassword = passwordTF.getCharacters().toString();
                    *//* Login to Server *//*
                    try{
                        *//* online version *//*
                        mSocket.emit("login", gotUsername, gotPassword);
                        TimeUnit.SECONDS.sleep(1);
                        *//* offline version *//*
                        // loginSuccess = login(gotUsername, gotPassword);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    *//* Change the scene or alert *//*
                    if (loginSuccess) {
                        *//* TODO: load user's data *//*
                        window.setScene(scene2);
                    } else {
                        AlertBox.display("Attention", "The user info is wrong!");
                        usernameTF.setText("");
                        passwordTF.setText("");
                    }
                }
            });*/
            HBox registerHBox = new HBox();
            Text registerHint = new Text("have no account yet?  ");
            registerHint.setFont(Font.font ("Verdana", 12));
            final Button register = new Button("Register");
            register.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    window.setScene(scene0);
                }
            });

            loginHBox.getChildren().addAll(login);
            loginHBox.setAlignment(Pos.CENTER_RIGHT);
            registerHBox.getChildren().addAll(registerHint, register);
            registerHBox.setAlignment(Pos.CENTER_RIGHT);

            loginPage.add(usernameTF, 0, 5);
            loginPage.add(passwordTF, 0, 6);
            loginPage.add(loginHBox, 0, 7);
            loginPage.add(registerHBox, 0, 8);

        /* Actually Start */
        scene1 = new Scene(loginPage, 720, 540);
        scene1.getStylesheets().addAll(this.getClass().getResource("style.css").toExternalForm());

        window.setScene(scene1);
        window.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
