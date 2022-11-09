/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Login;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class Controller implements Initializable {
    
    @FXML
    private Label Error;
    @FXML
    private TextField email;
    @FXML
    private TextField password;
    
    private Model model;
    

    
    @FXML
    private void loginRequest(ActionEvent event) {
        Error.setText("");        
        model.connect(password.getText(), email.getText());
        
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (this.model != null) 
            throw new IllegalStateException("Model can only be initialized once");
        
        this.model = new Model();
        ModelObserver observer = new ModelObserver(Error);
        model.addObserver(observer);

        
      
      
    }
     
}
