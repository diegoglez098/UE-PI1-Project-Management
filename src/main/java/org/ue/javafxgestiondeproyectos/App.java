package org.ue.javafxgestiondeproyectos;

import org.json.JSONObject;
import org.json.JSONArray;

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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;

import java.io.StringReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import java.io.StringReader;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

interface LoginHandler {
    void handleLogin(String username, String password);
}

interface SendDataHandler {
    void handleSendData(String client, String ingeniero, String date, String estimatedAmount, String title,
                        String comercial);
}

interface GetProjectsHandler {
    void handlerGetProjects(String ingeniero);
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

class LoginWindow extends org.ue.javafxgestiondeproyectos.MainWindow {
    private org.ue.javafxgestiondeproyectos.LoginHandler loginHandler;

    public LoginWindow(org.ue.javafxgestiondeproyectos.LoginHandler loginHandler) {
        this.loginHandler = loginHandler;
    }

    @Override
    public GridPane getPane() {
        GridPane gridPane = org.ue.javafxgestiondeproyectos.UIUtils.createCommonGridPane();

        StackPane logoPane = org.ue.javafxgestiondeproyectos.UIUtils.createLogoPane();
        gridPane.add(logoPane, 0, 0, 2, 1);

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button loginButton = org.ue.javafxgestiondeproyectos.UIUtils.createStyledButton("Iniciar Sesión");

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

class ComercialWindow extends org.ue.javafxgestiondeproyectos.MainWindow {
    private org.ue.javafxgestiondeproyectos.GetProjectsHandler getProjectsHandler;
    private org.ue.javafxgestiondeproyectos.SendDataHandler sendDataHandler;
    private String comercial;
    private String response;

    public ComercialWindow(org.ue.javafxgestiondeproyectos.SendDataHandler sendDataHandler, String comercial) {
        this.sendDataHandler = sendDataHandler;
        this.comercial = comercial;
        this.response = "";

    }

    @Override
    public GridPane getPane() {

        GridPane gridPane = org.ue.javafxgestiondeproyectos.UIUtils.createCommonGridPane();

        StackPane logoPane = org.ue.javafxgestiondeproyectos.UIUtils.createLogoPane();
        gridPane.add(logoPane, 0, 0, 2, 1);

        ListView<String> projectsList = new ListView<>();
        loadProjectsFromBackend(projectsList);

        gridPane.add(new Label("Proyectos:"), 0, 1);
        gridPane.add(projectsList, 0, 2, 2, 4);

        TextField clientNameField = new TextField();
        TextField ingenieroField = new TextField();
        TextField titleField = new TextField();
        TextField deliveryDateField = new TextField();
        TextField estimatedAmountField = new TextField();
        Button sendDataButton = org.ue.javafxgestiondeproyectos.UIUtils.createStyledButton("Enviar Datos");
        DatePicker fechaEntregaPicker = new DatePicker();
        fechaEntregaPicker.setPromptText("Seleccionar Fecha de Entrega");

        gridPane.add(new Label("Nuevo proyecto"), 2, 1);
        gridPane.add(new Label("Nombre del Proyecto:"), 2, 2);
        gridPane.add(titleField, 3, 2);
        gridPane.add(new Label("Cliente final:"), 2, 3);
        gridPane.add(clientNameField, 3, 3);
        gridPane.add(new Label("Ingeniero:"), 2, 4);
        gridPane.add(ingenieroField, 3, 4);
        gridPane.add(new Label("Fecha de Entrega:"), 2, 5);
        gridPane.add(fechaEntregaPicker, 3, 5);
        gridPane.add(new Label("Importe estimado:"), 2, 6);
        gridPane.add(estimatedAmountField, 3, 6);
        gridPane.add(sendDataButton, 3, 7);

        sendDataButton.setOnAction(e -> {
            String clientName = clientNameField.getText();
            String title = titleField.getText();
            String ingeniero = ingenieroField.getText();
            String deliveryDate = fechaEntregaPicker.getValue().toString();
            String estimatedAmount = estimatedAmountField.getText();
            sendDataHandler.handleSendData(clientName, ingeniero, deliveryDate, estimatedAmount, title, comercial);
            clientNameField.clear();
            ingenieroField.clear();
            deliveryDateField.clear();
            estimatedAmountField.clear();
            titleField.clear();
        });
        projectsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                String selectedProject = projectsList.getSelectionModel().getSelectedItem();
                displayProjectInfo(selectedProject);
            }
        });

        return gridPane;
    }

    public void loadProjectsFromBackend(ListView<String> projectsList) {
        response = handlerGetProjects(comercial);

        JSONObject jsonObject = new JSONObject(response);

        List<String> projects = new ArrayList<>();

        for (int i = 1; i < 99; i++) {
            try {
                JSONObject value = new JSONObject();
                value = jsonObject.getJSONObject(String.valueOf(i));
                var id = value.getInt("id");
                projects.add(String.valueOf(id) + " : " + value.getString("nombre") + " - "
                        + value.getString("cliente"));

            } catch (Exception e) {
            }
        }
        for (String elemento : projects) {
            projectsList.getItems().add(elemento);
        }
    }

    public String handlerGetProjects(String comercial) {

        try {
            URL url = new URL("https://ue-pi1-project-management.onrender.com/api/v1/getProjectsComercial");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonInputString = "{\"comercial\": \"" + comercial + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();

            }
        } catch (Exception e) {

            e.printStackTrace();

        }
        return "";
    }

    private void displayProjectInfo(String selectedProject) {
        Alert projectInfoAlert = new Alert(Alert.AlertType.INFORMATION);
        JSONObject jsonObject = new JSONObject(response);
        JSONObject selected = new JSONObject();
        JSONObject actual = new JSONObject();

        int end = selectedProject.indexOf(':');

        String id = selectedProject.substring(0, end - 1);

        for (int w = 0; w < 999999; w++) {
            try {
                actual = jsonObject.getJSONObject(String.valueOf(w));
                Integer actualid = actual.getInt("id");
                if (String.valueOf(actualid).equalsIgnoreCase(id)) {
                    selected = actual;
                    break;
                }
            } catch (Exception e) {

            }

        }
        projectInfoAlert.setTitle(selectedProject);
        projectInfoAlert.setHeaderText(null);
        projectInfoAlert.setContentText(
                "Nombre del Proyecto: " + selected.getString("nombre") + "\nCliente Final: "
                        + selected.getString("cliente") + "\nIngeniero: " + selected.getString("ingeniero")
                        + "\nFecha de Entrega: " + selected.getString("fechaEntrega") + "\nImporte: "
                        + selected.getString("importe") + "\nEstado: " + selected.getString("estado"));

        projectInfoAlert.showAndWait();
    }
}

class IngenieroWindow extends org.ue.javafxgestiondeproyectos.MainWindow {
    private org.ue.javafxgestiondeproyectos.SendDataHandler sendDataHandler;
    private String ingeniero;

    public IngenieroWindow(org.ue.javafxgestiondeproyectos.SendDataHandler sendDataHandler, String ingeniero) {
        this.sendDataHandler = sendDataHandler;
        this.ingeniero = ingeniero; // Asigna el valor del ingeniero

    }

    @Override
    public GridPane getPane() {
        GridPane gridPane = org.ue.javafxgestiondeproyectos.UIUtils.createCommonGridPane();

        StackPane logoPane = org.ue.javafxgestiondeproyectos.UIUtils.createLogoPane();
        gridPane.add(logoPane, 0, 0, 2, 1);

        ListView<String> requestsList = new ListView<>();
        loadRequestsFromBackend(requestsList);

        gridPane.add(new Label("Peticiones Recibidas:"), 0, 1);
        gridPane.add(requestsList, 0, 2, 1, 4);

        TextField clientNameField = new TextField();
        TextField titleNameField = new TextField();
        TextField ingenieroField = new TextField();
        TextField comerciaField = new TextField();
        DatePicker fechaEntregaPicker = new DatePicker();
        fechaEntregaPicker.setPromptText("Seleccionar Fecha de Entrega");
        TextField estimatedAmountField = new TextField();
        Button sendDataButton = org.ue.javafxgestiondeproyectos.UIUtils.createStyledButton("Enviar Datos");

        gridPane.add(new Label("Nuevo proyecto"), 1, 1);
        gridPane.add(new Label("Nombre:"), 1, 2);
        gridPane.add(titleNameField, 2, 2);
        gridPane.add(new Label("Cliente final:"), 1, 3);
        gridPane.add(clientNameField, 2, 3);
        gridPane.add(new Label("Ingeniero:"), 1, 4);
        gridPane.add(ingenieroField, 2, 4);
        gridPane.add(new Label("Fecha de Entrega:"), 1, 5);
        gridPane.add(fechaEntregaPicker, 2, 5);
        gridPane.add(new Label("Importe estimado:"), 1, 6);
        gridPane.add(estimatedAmountField, 2, 6);
        gridPane.add(new Label("Comercial:"), 1, 7);
        gridPane.add(comerciaField, 2, 7);
        gridPane.add(sendDataButton, 2, 8);

        sendDataButton.setOnAction(e -> {
            String clientName = clientNameField.getText();
            String ingeniero = ingenieroField.getText();
            String deliveryDate = fechaEntregaPicker.getValue().toString();
            String estimatedAmount = estimatedAmountField.getText();
            String nombre = titleNameField.getText();
            String comercial = comerciaField.getText();

            sendDataHandler.handleSendData(clientName, ingeniero, deliveryDate, estimatedAmount, nombre, comercial);
            clientNameField.clear();
            ingenieroField.clear();
            estimatedAmountField.clear();
        });
        requestsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedRequest = requestsList.getSelectionModel().getSelectedItem();
                openEditRequestWindow(selectedRequest);
            }
        });

        return gridPane;
    }

    private void openEditRequestWindow(String selectedRequest) {

        String id = getId(selectedRequest);

        Stage editRequestStage = new Stage();
        editRequestStage.initModality(Modality.APPLICATION_MODAL);
        editRequestStage.setTitle(selectedRequest);

        GridPane editRequestPane = org.ue.javafxgestiondeproyectos.UIUtils.createCommonGridPane();
        editRequestPane.setHgap(10);
        editRequestPane.setVgap(10);

        TextField editClientNameField = new TextField();
        TextField editTitleField = new TextField();
        TextField editIngenieroField = new TextField();
        DatePicker editDeliveryDateField = new DatePicker();
        editDeliveryDateField.setPromptText("Seleccionar Fecha de Entrega");
        TextField editEstimatedAmountField = new TextField();
        ComboBox<String> estadoProyectoComboBox = new ComboBox<>();
        estadoProyectoComboBox.getItems().addAll("Pendiente", "Entregado", "Desestimado");
        estadoProyectoComboBox.setPromptText("Seleccionar Estado");

        Button saveButton = org.ue.javafxgestiondeproyectos.UIUtils.createStyledButton("Guardar Cambios");

        editRequestPane.add(new Label("Nombre del proyecto:"), 0, 1);
        editRequestPane.add(editTitleField, 1, 1);
        editRequestPane.add(new Label("Cliente final:"), 0, 2);
        editRequestPane.add(editClientNameField, 1, 2);
        editRequestPane.add(new Label("Ingeniero:"), 0, 3);
        editRequestPane.add(editIngenieroField, 1, 3);
        editRequestPane.add(new Label("Fecha de entrega:"), 0, 4);
        editRequestPane.add(editDeliveryDateField, 1, 4);
        editRequestPane.add(new Label("Importe final:"), 0, 5);
        editRequestPane.add(editEstimatedAmountField, 1, 5);
        editRequestPane.add(new Label("Estado del Proyecto:"), 0, 6);
        editRequestPane.add(estadoProyectoComboBox, 1, 6);

        editRequestPane.add(saveButton, 1, 7);
        if (editDeliveryDateField.getValue() == null){
            editDeliveryDateField.setValue(LocalDate.now());
        }
        saveButton.setOnAction(e -> {
            updateData(editClientNameField.getText(), editIngenieroField.getText(), editDeliveryDateField.getValue().toString(),
                    editEstimatedAmountField.getText(), editTitleField.getText(), estadoProyectoComboBox.getValue(),
                    id);
            editRequestStage.close();
        });

        Scene editRequestScene = new Scene(editRequestPane, 500, 300);
        editRequestStage.setScene(editRequestScene);
        editRequestStage.show();
    }

    private void loadRequestsFromBackend(ListView<String> requestsList) {

        List<String> response = handlerGetProjects(ingeniero);

        for (String elemento : response) {
            requestsList.getItems().add(elemento);
        }
    }

    public String getId(String selected) {

        int end = selected.indexOf(':');

        String id = selected.substring(0, end - 1);

        return id;

    }

    private void updateData(String client, String ingeniero, String date, String estimatedAmount, String title,
                            String estado, String id) {
        String jsonInputString = "{\"cliente\": \"" + client + "\", \"ingeniero\": \"" + ingeniero
                + "\",\"fechaEntrega\": \"" + date + "\",\"nombre\": \"" + title + "\",\"id\": \""
                + Integer.parseInt(id) + "\",\"estado\": \"" + estado + "\",\"importe\": \""
                + estimatedAmount + "\"}";
        String responseString = "";

        try {
            URL url = new URL("https://ue-pi1-project-management.onrender.com/api/v1/updateProject");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println("Respuesta del servidor: " + response.toString());
                responseString = response.toString();

            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        JSONObject jsonResponse = new JSONObject(responseString.toString());
        String status = jsonResponse.getString("status");
        if (status.equals("OK")) {
            //showAlert("Datos enviados", "Datos enviados correctamente.");

        } else {
            // showAlert("Error al enviar", "Compruebe su conexión a internet y vuelva a
            // intentarlo");

        }

    }

    public List<String> handlerGetProjects(String ingeniero) {
        List<String> projects = new ArrayList<>();

        try {
            URL url = new URL("https://ue-pi1-project-management.onrender.com/api/v1/getProjects");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonInputString = "{\"ingeniero\": \"" + ingeniero + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                ArrayList projectsToIng = new ArrayList<>();
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                JSONObject jsonObject = new JSONObject(response.toString());

                Object proyectosIng = jsonObject.get("projects");
                JSONArray jsonArray = new JSONArray(proyectosIng.toString());

                // Imprimir los 12 primeros proyectos
                for (int i = 0; i < 11; i++) {
                    if (jsonArray.isNull(i)){
                        break;
                    }
                    JSONObject value = jsonArray.getJSONObject(i);

                    System.out.println(value.toString());
                    projects.add(value.getInt("proyId") + " : " + value.getString("proyName") + " - "
                            + value.getString("proyClient"));

                }
                return projects;

            }
        } catch (Exception e) {

            e.printStackTrace();

        }
        return projects;
    }

}

public class App extends Application {
    private boolean isLoggedIn = false;
    private String userRole = "";
    private Stage primaryStage;
    public String projectsData;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Gestión de Proyectos");

        org.ue.javafxgestiondeproyectos.LoginWindow loginWindow = new org.ue.javafxgestiondeproyectos.LoginWindow(this::handleLogin);

        Scene scene = new Scene(loginWindow.getPane(), 400, 300);
        primaryStage.setScene(scene);

        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void handleLogin(String username, String password) {
        JSONObject responseJson = sendPostToBackend(username, password);
        String status = responseJson.getString("status");

        if (status.equals("ERROR")) {
            showAlert("Inicio de sesión fallido", "Verifica tus credenciales.");

        } else {
            isLoggedIn = true;
            userRole = responseJson.getString("rol");

            // Agregar logs para ver el rol recibido
            System.out.println("Rol recibido del servidor: " + userRole);

            if ("comercial".equals(userRole)) {
                openComercialWindow(username);

            } else if ("ingeniero".equals(userRole)) {
                openIngenieroWindow(username);

            }
            else {
                showAlert("Error", "Rol no válido");
            }
        }
    }

    private JSONObject sendPostToBackend(String username, String password) {
        String responseString;
        try {
            URL url = new URL("https://ue-pi1-project-management.onrender.com/api/v1/login");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonInputString = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        finally {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println("Respuesta del servidor: " + response.toString());
                    responseString = response.toString();
                    JSONObject jsonResponse = new JSONObject(responseString.toString());
                    return jsonResponse;
            }
        } catch (Exception e) {

            e.printStackTrace();

        }
       return (JSONObject) new JSONObject();
    }

    private void openComercialWindow(String username) {
        org.ue.javafxgestiondeproyectos.ComercialWindow comercialWindow = new org.ue.javafxgestiondeproyectos.ComercialWindow(this::handleSendData, username);
        openWindow(userRole, "Bienvenido " + username, comercialWindow, username);
    }

    private void openIngenieroWindow(String username) {
        org.ue.javafxgestiondeproyectos.IngenieroWindow ingenieroWindow = new org.ue.javafxgestiondeproyectos.IngenieroWindow(this::handleSendData, username);
        openWindow(userRole, "Bienvenido " + username, ingenieroWindow, username);

    }

    private void openWindow(String rol, String title, org.ue.javafxgestiondeproyectos.MainWindow window, String name) {
        Stage stage = new Stage();
        stage.setTitle(title);
        GridPane gridPane = window.getPane();
        gridPane.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(gridPane, 600, 400));
        if ("ingeniero".equals(rol) || "comercial".equals(rol)) {
            Button logoutButton = org.ue.javafxgestiondeproyectos.UIUtils.createStyledButton("Cerrar Sesión");

            logoutButton.setOnAction(e -> handleLogout(stage));
            if ("ingeniero".equals(rol)) {
                gridPane.add(logoutButton, 2, 0);

            } else {
                gridPane.add(logoutButton, 3, 0);
            }
        }
        stage.setResizable(false);
        stage.show();
    }

    private void handleLogout(Stage window) {
        isLoggedIn = false;
        userRole = "";

        window.close();
        primaryStage.show();
    }

    private void handleSendData(String client, String ingeniero, String date, String estimatedAmount, String title,
                                String comercial) {
        String jsonInputString = "{\"cliente\": \"" + client + "\", \"ingeniero\": \"" + ingeniero
                + "\",\"fechaEntrega\": \"" + date + "\",\"nombre\": \"" + title + "\",\"comercial\": \"" + comercial
                + "\",\"importe\": \""
                + estimatedAmount + "\"}";
        String responseString = "";

        if (isLoggedIn) {      
            try {
                URL url = new URL("https://ue-pi1-project-management.onrender.com/api/v1/addProject");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println("Respuesta del servidor: " + response.toString());
                    responseString = response.toString();

                }
            } catch (Exception e) {
                e.printStackTrace();

            }
            JSONObject jsonResponse = new JSONObject(responseString.toString());
            String status = jsonResponse.getString("status");
            if (status.equals("OK")) {
                showAlert("Datos enviados", "Datos enviados correctamente.");

            } else {
                showAlert("Error al enviar", "Compruebe su conexión a internet y vuelva a intentarlo");

            }

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

