package Ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import Service.UserService;

public class AuthView extends Application {
    private UserService userService = new UserService();
    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showLoginScene();
    }

    private void showLoginScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");

        Label title = new Label("Personal Expense Tracker");
        title.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        Label subtitle = new Label("Login");
        subtitle.setStyle("-fx-font-size: 16;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(250);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12;");

        Button loginBtn = new Button("Login");
        loginBtn.setPrefWidth(100);
        loginBtn.setStyle("-fx-font-size: 12;");
        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Username and password cannot be empty");
                return;
            }

            if (userService.login(username, password)) {
                showMainApp();
            } else {
                errorLabel.setText("Invalid username or password");
                passwordField.clear();
            }
        });

        Button signupBtn = new Button("Sign Up");
        signupBtn.setPrefWidth(100);
        signupBtn.setStyle("-fx-font-size: 12;");
        signupBtn.setOnAction(e -> showSignupScene());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(loginBtn, signupBtn);

        root.getChildren().addAll(title, subtitle, usernameField, passwordField, errorLabel, buttonBox);

        Scene scene = new Scene(root, 400, 350);
        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showSignupScene() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");

        Label title = new Label("Create New Account");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(250);

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm Password");
        confirmField.setPrefWidth(250);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12;");

        Label infoLabel = new Label();
        infoLabel.setStyle("-fx-text-fill: blue; -fx-font-size: 11;");
        infoLabel.setText("• Username and password cannot be the same\n• Password must match");

        Button signupBtn = new Button("Sign Up");
        signupBtn.setPrefWidth(100);
        signupBtn.setStyle("-fx-font-size: 12;");
        signupBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirm = confirmField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Fields cannot be empty");
                return;
            }

            if (!password.equals(confirm)) {
                errorLabel.setText("Passwords do not match");
                return;
            }

            if (username.equals(password)) {
                errorLabel.setText("Username and password cannot be the same");
                return;
            }

            if (userService.userExists(username)) {
                showTakenAlert(username);
                return;
            }

            if (userService.signup(username, password)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText("Account Created!");
                alert.setContentText("Account for '" + username + "' created successfully. Please login.");
                alert.showAndWait();
                showLoginScene();
            } else {
                errorLabel.setText("Signup failed. Try again.");
            }
        });

        Button backBtn = new Button("Back");
        backBtn.setPrefWidth(100);
        backBtn.setStyle("-fx-font-size: 12;");
        backBtn.setOnAction(e -> showLoginScene());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(signupBtn, backBtn);

        root.getChildren().addAll(title, usernameField, passwordField, confirmField, 
                                  infoLabel, errorLabel, buttonBox);

        Scene scene = new Scene(root, 400, 450);
        primaryStage.setTitle("Sign Up");
        primaryStage.setScene(scene);
    }

    private void showTakenAlert(String username) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Username Taken");
        alert.setHeaderText("Registration Failed");
        alert.setContentText("Username '" + username + "' is already taken. Please choose another.");
        alert.showAndWait();
    }

    private void showMainApp() {
        String currentUser = userService.getCurrentUser();
        MainView.setCurrentUser(currentUser);
        MainView mainView = new MainView();
        Stage mainStage = new Stage();
        try {
            mainView.start(mainStage);
            primaryStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}