package Login;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;

import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


public class Login extends Application {
    Scene scene;
    private static Stage GUIStage;
    
    public static Stage getStage(){
         return GUIStage;
    }
    
    @Override
    public void start(Stage stage){
        GUIStage = stage;
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
            scene = new Scene(root);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (IOException | NullPointerException e) {
            Label error = new Label("Errore nel caricamento della pagina");
            StackPane root = new StackPane();
            root.getChildren().add(error);
            
            scene = new Scene(root,300,150);
            stage.setTitle("Error");
            stage.setScene(scene);

            stage.show();
        }
       

    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
