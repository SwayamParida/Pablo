package pablo.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GUI extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Board.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            primaryStage.setTitle("Pablo!");
            primaryStage.setScene(scene);
            primaryStage.show();

            new Thread(new ClientThread(fxmlLoader.getController()), "ClientNetworkingThread").start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
