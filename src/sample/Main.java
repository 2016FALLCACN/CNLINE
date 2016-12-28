package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.github.nkzawa.socketio.client.*;
import com.github.nkzawa.emitter.Emitter;

public class Main extends Application {

    private static Stage window;
    private static Scene scene1, scene0, scene2;

    private Socket mSocket;

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
            FileInputStream in = new FileInputStream(Constant.USERCONFIG);
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
            FileOutputStream out = new FileOutputStream(Constant.USERCONFIG, true);
            String content = id+":"+pwd+"\n";

            byte[] contentInBytes = content.getBytes();

            out.write(contentInBytes);
            out.flush();
            out.close();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        /* Setting: window setting */
        // Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        window = primaryStage;
        window.setTitle("CNLINE");

        /*try {
            mSocket = IO.socket(Constant.SERVERURL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/

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
                        Boolean loginSuccess = false;
                        try{
                            loginSuccess = login(gotUsername, gotPassword);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                        if(loginSuccess){
                        /* TODO: load user's data */
                            window.setScene(scene2);
                        } else {
                            AlertBox.display("Attention", "The user info is wrong!");
                            usernameTF.setText("");
                            passwordTF.setText("");
                        }
                    }
                }
            });
            login.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event){
                    String gotUsername = usernameTF.getCharacters().toString();
                    String gotPassword = passwordTF.getCharacters().toString();
                    Boolean loginSuccess = false;
                    try{
                        loginSuccess = login(gotUsername, gotPassword);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    if(loginSuccess){
                        /* TODO: load user's data */
                        window.setScene(scene2);
                    } else {
                        AlertBox.display("Attention", "The user info is wrong!");
                        usernameTF.setText("");
                        passwordTF.setText("");
                    }
                }
            });
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
            typePassword.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    /* TODO */
                }
            });
            Button submit = new Button("Submit");
            submit.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    String gotUsername = typeUsername.getCharacters().toString();
                    String gotPassword = typePassword.getCharacters().toString();

                    if (gotUsername.equals("")) {
                        AlertBox.display("Attention", "You haven't enter your name!");
                    } else if (repeated(gotUsername)) {
                        AlertBox.display("Attention", "This name has been registered!");
                    } else if (gotPassword.length() < Constant.PWDMINLEN) {
                        AlertBox.display("Attention", "This password is too short!");
                    } else if (gotPassword.length() > Constant.PWDMAXLEN) {
                        AlertBox.display("Attention", "This password is too long!");
                    } else {
                        try{
                            register(gotUsername, gotPassword);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        AlertBox.display("Information", "Success!");
                        window.setScene(scene1);
                    }
                }
            });
            register.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    String gotUsername = typeUsername.getCharacters().toString();
                    String gotPassword = typePassword.getCharacters().toString();

                    if (gotUsername.equals("")) {
                        AlertBox.display("Attention", "You haven't enter your name!");
                    } else if (repeated(gotUsername)) {
                        AlertBox.display("Attention", "This name has been registered!");
                    } else if (gotPassword.length() < Constant.PWDMINLEN) {
                        AlertBox.display("Attention", "This password is too short!");
                    } else if (gotPassword.length() > Constant.PWDMAXLEN) {
                        AlertBox.display("Attention", "This password is too long!");
                    } else {
                        AlertBox.display("Information", "Welcome! Let's start!");
                        /* TODO */
                        window.setScene(scene1);
                    }
                }
            });

        /* layout setting */

        registerUsername.getChildren().addAll(hintUsername, typeUsername);
        registerPassword.getChildren().addAll(hintPassword, typePassword);
        registerPage.add(registerUsername, 0, 0);
        registerPage.add(registerPassword,0,1);
        registerPage.add(submit, 0, 2);

        scene0 = new Scene(registerPage, 720, 540);

        /* Scene2: Main */
        GridPane mainPage = new GridPane();
        mainPage.setAlignment(Pos.CENTER);
        mainPage.setHgap(10);
        mainPage.setVgap(10);
        mainPage.setPadding(new Insets(30));

            /* Components */
            ListView<Label> contact = new ListView<Label>();
            ObservableList names = FXCollections.observableArrayList();
            ArrayList<Label> friend = new ArrayList<Label>();
            friend.add(new Label("Trump"));
            friend.add(new Label("Obama"));
            friend.add(new Label("Page"));
            friend.add(new Label("Dijkstra"));
            friend.add(new Label("Turing"));
            names.addAll(friend);
            contact.setItems(names);

            final VBox messageArea = new VBox(20); /* TODO: maximize the size */
            HBox messageInput = new HBox(10);
            final TextArea messageLog = new TextArea();
            messageLog.setEditable(false);
            final TextField typeMessage = new TextField();
            // Button sendFile = new Button("file"); /* TODO: window showup */
            Button sendMessage = new Button("send");
            /* TODO: auto-newline */
            sendMessage.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if(!typeMessage.getCharacters().toString().equals("")) {
                        messageLog.appendText(typeMessage.getCharacters().toString() + "\n"); /* auto scroll to bottom */
                        typeMessage.setText("");
                    }
                }
            });
            typeMessage.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event){
                    if(event.getCode() == KeyCode.ENTER) {
                        if(!typeMessage.getCharacters().toString().equals("")) {
                            messageLog.appendText(typeMessage.getCharacters().toString() + "\n"); /* auto scroll to bottom */
                            typeMessage.setText("");
                        }
                    }
                }
            });

        /* layout setting */

        typeMessage.setAlignment(Pos.CENTER_LEFT);
        sendMessage.setAlignment(Pos.CENTER_RIGHT);
        messageInput.getChildren().addAll(typeMessage, sendMessage);
        messageInput.setAlignment(Pos.BOTTOM_LEFT);
        messageArea.getChildren().addAll(messageLog, messageInput);


        mainPage.add(contact, 0, 0);
        mainPage.add(messageArea, 1, 0);

        scene2 = new Scene(mainPage, 720, 540);

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
