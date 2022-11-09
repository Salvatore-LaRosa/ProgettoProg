package Server;

import java.io.IOException;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

//application è una classe astratta di javafx
public class Start extends Application {

    Scene scene;//crea una scena da utilizzare scene builder come strumento grafico per il design dell'interfaccia utente
    private static Stage GUIStage;//lo stage rappresenta una finestra simile al jframe di swing


    public static Stage getStage(){
         return GUIStage;
    }//getter per ottenere stage

    //il metodo start è l'entrypoint di un'applicazione javafx
    //questo metodo prende uno stage(finestra dell'applicazione) come parametro e viene invocato dal launcher dell'applicazione
    @Override
    public void start(Stage stage){
        GUIStage = stage;//lo stage rappresenta una finestra simile al jframe di swing
        try {
            controllerServer cs = new controllerServer();       
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("scene.fxml"));//carica il documento fxml per generare la GUI
            fxmlLoader.setController(cs);//imposta il controller dell'fxml
            stage.setTitle("Server");//imposta il titolo dello stage
            stage.setResizable(false);//non permette il ridimensionamento della finestra
            stage.setScene(new Scene(fxmlLoader.load()));//imposta la scena nello stage
            stage.show();//visualizza lo stage
            
        } catch (NullPointerException | IOException e) {
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

