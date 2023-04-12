package pcd.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GuiEntry extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(this.getClass().getClassLoader().getResource("view/GuiLayout.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("File analysis tool");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        Controller controller = loader.getController();
        primaryStage.setOnCloseRequest(e -> controller.onExit());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
