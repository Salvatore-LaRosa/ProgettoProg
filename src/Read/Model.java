
package Read;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Model {
    Stage stage;
    
    
    public Model(Stage stage){
        this.stage = stage;
    }
    
        
    public boolean GetIsOpen() {
        return stage.isShowing();
    }    

    
    public void openNewWindow(String path, String title, Object controller){
        NewWindow(path, title, controller); 
    }
    
    private void NewWindow(String path, String title, Object controller){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(path));
            fxmlLoader.setController(controller);
            stage.setTitle(title);
            stage.setResizable(false);
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.show();
         
        } catch (IOException ex) {
            System.out.println(ex.getCause());
            System.out.println(ex.getMessage());
        }
    }
}
