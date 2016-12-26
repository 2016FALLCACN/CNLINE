package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {

    static Stage window;
    static Scene scene1, scene0;

    public boolean repeated(String gotstring){
        return false;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));

        window = primaryStage;
        window.setTitle("CNLINE");

        /* Scene1: Login */
        GridPane loginPage = new GridPane();
        loginPage.setAlignment(Pos.CENTER);
        loginPage.setHgap(10);
        loginPage.setVgap(10);
        loginPage.setPadding(new Insets(50));
        //loginPage.setId("loginPage");

            /* Welcome Text */
            Text welcome = new Text("Welcome to the CNLINE");
            welcome.setFont(Font.font ("Verdana", 20));

            loginPage.add(welcome, 0, 3);

            /* Login input */
            TextField usernameTF = new TextField();
            PasswordField passwordTF = new PasswordField();
            HBox loginHBox = new HBox();
            Button login = new Button("Login");
            HBox registerHBox = new HBox();
            Text registerHint = new Text("have no account yet?  ");
            registerHint.setFont(Font.font ("Verdana", 12));
            Button register = new Button("Register");
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
                        AlertBox.display("Information", "Welcome! Let's start!");
                        /* TODO */
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
        mainPage.setPadding(new Insets(50));

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
