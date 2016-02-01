package com.btcc.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by zhenning on 15/8/29.
 */
public class FxApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("FIX client for BTCC Pro exchange");

        Parent root = FXMLLoader.load(this.getClass().getResource("profixclient.fxml"));

        Scene scene = new Scene(root, 600, 1050);
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
