package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.io.OutputStream;
import java.net.HttpURLConnection;

import java.net.URL;
import java.nio.charset.StandardCharsets;

interface LoginHandler {
    void handleLogin(String username, String password);
}

interface SendDataHandler {
    void handleSendData(String userRole);
}

abstract class MainWindow {
    abstract GridPane getPane();
}

class UIUtils {
    public static GridPane createCommonGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setAlignment(Pos.CENTER); 
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        return gridPane;
    }

    public static Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #ff3228; -fx-text-fill: white;");
        return button;
    }

    public static StackPane createLogoPane() {
        StackPane stackPane = new StackPane();
        Image image = new Image(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/UE_Madrid_Logo_Positive_RGB.png/800px-UE_Madrid_Logo_Positive_RGB.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(300); 
        imageView.setPreserveRatio(true); 
        stackPane.getChildren().add(imageView);
        return stackPane;
    }
}

class LoginWindow extends MainWindow {
    private LoginHandler loginHandler;

    public LoginWindow(LoginHandler loginHandler) {
        this.loginHandler = loginHandler;
    }

    @Override
    public GridPane getPane() {
        GridPane gridPane = UIUtils.createCommonGridPane();

        StackPane logoPane = UIUtils.createLogoPane();
        gridPane.add(logoPane, 0, 0, 2, 1); 

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button loginButton = UIUtils.createStyledButton("Iniciar Sesión");

        gridPane.add(new Label("Usuario:"), 0, 1);
        gridPane.add(usernameField, 1, 1);
        gridPane.add(new Label("Contraseña:"), 0, 2);
        gridPane.add(passwordField, 1, 2);
        gridPane.add(loginButton, 1, 3);

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            loginHandler.handleLogin(username, password);
        });

        return gridPane;
    }
}

class ComercialWindow extends MainWindow {
    private SendDataHandler sendDataHandler;

    public ComercialWindow(SendDataHandler sendDataHandler) {
        this.sendDataHandler = sendDataHandler;
    }

    @Override
    public GridPane getPane() {
        GridPane gridPane = UIUtils.createCommonGridPane();

        // Agregar logo
        StackPane logoPane = UIUtils.createLogoPane();
        gridPane.add(logoPane, 0, 0, 2, 1); 

        TextField clientNameField = new TextField();
        TextField ingenieroField = new TextField();
        TextField deliveryDateField = new TextField();
        TextField estimatedAmountField = new TextField();
        Button sendDataButton = UIUtils.createStyledButton("Enviar Datos");

        gridPane.add(new Label("Cliente final:"), 0, 1);
        gridPane.add(clientNameField, 1, 1);
        gridPane.add(new Label("Ingeniero:"), 0, 2);
        gridPane.add(ingenieroField, 1, 2);
        gridPane.add(new Label("Fecha de entrega:"), 0, 3);
        gridPane.add(deliveryDateField, 1, 3);
        gridPane.add(new Label("Importe estimado:"), 0, 4);
        gridPane.add(estimatedAmountField, 1, 4);
        gridPane.add(sendDataButton, 1, 5);

        sendDataButton.setOnAction(e -> sendDataHandler.handleSendData("Comercial"));

        return gridPane;
    }
}

class IngenieroWindow extends MainWindow {
    private SendDataHandler sendDataHandler;

    public IngenieroWindow(SendDataHandler sendDataHandler) {
        this.sendDataHandler = sendDataHandler;
    }

    @Override
    public GridPane getPane() {
        GridPane gridPane = UIUtils.createCommonGridPane();

        StackPane logoPane = UIUtils.createLogoPane();
        gridPane.add(logoPane, 0, 0, 2, 1); // Span dos columnas para ocupar todo el ancho

        ListView<String> requestsList = new ListView<>();
        loadRequestsFromBackend(requestsList);

        gridPane.add(new Label("Peticiones Recibidas:"), 0, 1);
        gridPane.add(requestsList, 0, 2, 1, 4);

        TextField clientNameField = new TextField();
        TextField ingenieroField = new TextField();
        TextField deliveryDateField = new TextField();
        TextField estimatedAmountField = new TextField();
        Button sendDataButton = UIUtils.createStyledButton("Enviar Datos");

        gridPane.add(new Label("Nuevo proyecto"), 1, 1);
        gridPane.add(new Label("Cliente final:"), 1, 2);
        gridPane.add(clientNameField, 2, 2);
        gridPane.add(new Label("Ingeniero:"), 1, 3);
        gridPane.add(ingenieroField, 2, 3);
        gridPane.add(new Label("Fecha de entrega:"), 1, 4);
        gridPane.add(deliveryDateField, 2, 4);
        gridPane.add(new Label("Importe estimado:"), 1, 5);
        gridPane.add(estimatedAmountField, 2, 5);
        gridPane.add(sendDataButton, 2, 6);

        sendDataButton.setOnAction(e -> sendDataHandler.handleSendData("Ingeniero"));

        requestsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedRequest = requestsList.getSelectionModel().getSelectedItem();
                openEditRequestWindow(selectedRequest);
            }
        });

        return gridPane;
    }

    private void openEditRequestWindow(String selectedRequest) {
        Stage editRequestStage = new Stage();
        editRequestStage.initModality(Modality.APPLICATION_MODAL);
        editRequestStage.setTitle("Editar Petición");

        GridPane editRequestPane = UIUtils.createCommonGridPane();
        editRequestPane.setHgap(10);
        editRequestPane.setVgap(10);

        // Agregar campos para editar la petición
        TextField editClientNameField = new TextField();
        TextField editIngenieroField = new TextField();
        TextField editDeliveryDateField = new TextField();
        TextField editEstimatedAmountField = new TextField();
        Button saveButton = UIUtils.createStyledButton("Guardar Cambios");

        editRequestPane.add(new Label("Cliente final:"), 0, 1);
        editRequestPane.add(editClientNameField, 1, 1);
        editRequestPane.add(new Label("Ingeniero:"), 0, 2);
        editRequestPane.add(editIngenieroField, 1, 2);
        editRequestPane.add(new Label("Fecha de entrega:"), 0, 3);
        editRequestPane.add(editDeliveryDateField, 1, 3);
        editRequestPane.add(new Label("Importe final:"), 0, 4);
        editRequestPane.add(editEstimatedAmountField, 1, 4);

        editRequestPane.add(saveButton, 1, 5);


        saveButton.setOnAction(e -> {
            
            editRequestStage.close();
        });

        Scene editRequestScene = new Scene(editRequestPane, 300, 200);
        editRequestStage.setScene(editRequestScene);
        editRequestStage.show();
    }

    private void loadRequestsFromBackend(ListView<String> requestsList) {
        
        requestsList.getItems().addAll("Salas de reuniones Telefónica - Entregado", "Instalaciones Prosegur - Pendiente", "Hyflex Universidad Europea - Ganado");
    }
}

public class App extends Application {
    private boolean isLoggedIn = false;
    private String userRole = "";
    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Gestión de Proyectos");

       
        // Ventana de inicio de sesión
        LoginWindow loginWindow = new LoginWindow(this::handleLogin);

        Scene scene = new Scene(loginWindow.getPane(), 400, 300);
        primaryStage.setScene(scene);

        primaryStage.setResizable(false); 
        primaryStage.show();
    }

    private void handleLogin(String username, String password) {
        // Simula la autenticación con el backend
        if ("diego".equals(username) && "admin".equals(password)) {
            isLoggedIn = true;
            userRole = "ingeniero"; 

            // Envía un POST al backend con la información de usuario y contraseña
            sendPostToBackend(username, password);

            // Determina la ventana a abrir según el rol
            if ("comercial".equals(userRole)) {
                openComercialWindow(username);

            } else if ("ingeniero".equals(userRole)) {
                openIngenieroWindow(username);

            }
            primaryStage.close();
        } else if ("javi".equals(username) && "admin".equals(password)) {
            isLoggedIn = true;
            userRole = "comercial"; 

            // Envía un POST al backend con la información de usuario y contraseña
            sendPostToBackend(username, password);

            // Determina la ventana a abrir según el rol
            if ("comercial".equals(userRole)) {
                openComercialWindow(username);

            } else if ("ingeniero".equals(userRole)) {
                openIngenieroWindow(username);

            }
            primaryStage.close();
        } else if ("david".equals(username) && "admin".equals(password)) {
            isLoggedIn = true;
            userRole = "ingeniero"; 

            // Envía un POST al backend con la información de usuario y contraseña
            sendPostToBackend(username, password);

            // Determina la ventana a abrir según el rol
            if ("comercial".equals(userRole)) {
                openComercialWindow(username);

            } else if ("ingeniero".equals(userRole)) {
                openIngenieroWindow(username);

            }
            primaryStage.close();
        } else {
            showAlert("Inicio de sesión fallido", "Verifica tus credenciales.");
        }
    }

    private void sendPostToBackend(String username, String password) {
        try {
            URL url = new URL("http://localhost:8080/login");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            String jsonInputString = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("POST request sent successfully");
            } else {
                System.out.println("POST request failed, response code: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openComercialWindow(String username) {
        ComercialWindow comercialWindow = new ComercialWindow(this::handleSendData);
        openWindow(userRole, "Bienvenido " + username, comercialWindow);
    }

    private void openIngenieroWindow(String username) {
        IngenieroWindow ingenieroWindow = new IngenieroWindow(this::handleSendData);
        openWindow(userRole, "Bienvenido " + username, ingenieroWindow);
    }

    private void openWindow(String rol, String title, MainWindow window) {
        Stage stage = new Stage();
        stage.setTitle(title);
        GridPane gridPane = window.getPane();
        gridPane.setAlignment(Pos.CENTER); 
        stage.setScene(new Scene(gridPane, 600, 400));
        System.out.println(rol);
        if ("ingeniero".equals(rol) || "comercial".equals(rol)) {
            Button logoutButton = UIUtils.createStyledButton("Cerrar Sesión");

            logoutButton.setOnAction(e -> handleLogout(stage));
            gridPane.add(logoutButton, 2, 0);
        }
        stage.setResizable(false); 
        stage.show();
    }

    private void handleLogout(Stage window) {
        isLoggedIn = false;
        window.close();
        userRole = "";
        primaryStage.show();
    }

    private void handleSendData(String userRole) {
        if (isLoggedIn) {
            showAlert("Datos enviados", "Datos enviados correctamente.");
        } else {
            showAlert("Acceso denegado", "Debes iniciar sesión antes de enviar datos.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
