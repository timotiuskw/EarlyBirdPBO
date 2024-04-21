package com.mycompany.earlybirdpbo;

import LibraryStuff.WebServer;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.prefs.Preferences;


public class App extends Application {

    public WebServer webServer;
    public boolean running = false;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Web Server Configuration");

        Preferences prefs = Preferences.userNodeForPackage(App.class);

        int defaultPort = prefs.getInt("port", 8080);
        String defaultWebDir = prefs.get("webdir", "D:/Web/");
        String defaultLogDir = prefs.get("logdir", "D:/Web/Logs");
        
        Button startButton = new Button("Start Server");
        Button stopButton = new Button("Stop Server");

        Label portLabel = new Label("Port: ");
        TextField portTextField = new TextField(Integer.toString(defaultPort));
        portTextField.setPromptText("Masukkan nomor port");

        Label webDirLabel = new Label("Web Directory: ");
        TextField webDirTextField = new TextField(defaultWebDir);
        webDirTextField.setPromptText("Masukkan Web Directory");

        Label logDirLabel = new Label("Log Directory: ");
        TextField logDirTextField = new TextField(defaultLogDir);
        logDirTextField.setPromptText("Masukkan Log Directory");

        TextArea logArea = new TextArea();
        logArea.setEditable(false);

        startButton.setOnAction(e -> {
            if (running == false){
                int port = Integer.parseInt(portTextField.getText());
                String webdir = webDirTextField.getText();
                String logdir = logDirTextField.getText();

                prefs.putInt("port", port);
                prefs.put("webdir", webdir); // Menyimpan Port, WebDir, LogDir ke Preferences untuk digunakan saat aplikasi dibuka lagi
                prefs.put("logdir", logdir);

                webServer = new WebServer(port, webdir, logdir);

                running = true;

                try {
                        webServer.start();
                } catch (IOException ex) {
                        ex.printStackTrace();
                }

                logArea.appendText("Server started on port " + port + "\n");
            }
        });

        stopButton.setOnAction(e -> {
            if (webServer != null && webServer.isRunning() == true) {
                webServer.stop();
                running = false;
                logArea.appendText("Server stopped\n");
            }
        });

        Button viewLogsButton = new Button("View Logs");
        viewLogsButton.setOnAction(e -> {
            try {
                // Mendapatkan direktori logs dari Preferences atau lokasi yang telah ditentukan
                String logsDirectoryPath = "D:/Web/Logs"; // Ubah dengan lokasi direktori logs Anda
                
                // Membuat objek File yang merepresentasikan direktori logs
                File logsDirectory = new File(logsDirectoryPath);
                
                // Membuka direktori logs menggunakan desktop
                Desktop.getDesktop().open(logsDirectory);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        VBox mainLayout = new VBox();
        HBox buttonLayout = new HBox();
        buttonLayout.getChildren().addAll(startButton, stopButton, viewLogsButton);
        mainLayout.getChildren().addAll(buttonLayout, portLabel, portTextField, webDirLabel, webDirTextField, logDirLabel, logDirTextField, logArea);

        primaryStage.setScene(new Scene(mainLayout, 500, 300));
        primaryStage.show();
        
    }

}