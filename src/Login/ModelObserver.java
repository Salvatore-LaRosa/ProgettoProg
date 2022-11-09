package Login;

import java.util.Observable;
import java.util.Observer;
import javafx.scene.control.Label;


class ModelObserver implements Observer{
    Label error;
    
    public ModelObserver(Label error) {
        this.error = error;
    }
    
    @Override
    public void update(Observable o, Object arg) {
        if(arg instanceof String){
            String tmp = (String) arg;
            error.setText(tmp);
        }
    }
    
}
