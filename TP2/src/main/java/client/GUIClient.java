package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GUIClient extends Application {

    /**
     * La fonction de JavaFX qui est nécessaire pour commencer l'application
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("src/main/resources/main_view.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Le main, pour démarrer l'application
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}
