
package Server;
    
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;


public class controllerServer implements Initializable, Observer{

    @FXML TextArea textServer;
    String [] arr;
    int index;
    int count = 0;
    final int MAX_VALUE = 10;
    
    public controllerServer() {
        arr = new String[MAX_VALUE]; //crea un array
        for(int ind= 0; ind != MAX_VALUE; ind++)
            arr[ind] = " ";//riempe tutto l'array con spazi vuoti
        index = 0;
    }

    //aggiunge un elemento nell'array
    private void addElement(String text){
        index = (index + 1)% MAX_VALUE;//scorrre ciclicamente l'array fino all'ulatima posizione per poi riniziare da capo
        arr[index] =  count +") "+ text + ";\n"; ;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ServerObservable os = new ServerObservable();
        os.addObserver(this);
        Server sv = new Server(os);
        sv.start();
    }

    @Override
    public void update(Observable o, Object arg) {
        String tmp ="";
        count++;
        addElement((String)arg);
        for(int ind = (index+1)%MAX_VALUE; ind != index;  ind = (ind + 1)% MAX_VALUE)
            if(!" ".equals(arr[ind]))
                tmp += arr[ind];
        
        textServer.screenToLocal(0,500);
        textServer.setText(tmp);
    }

    
    
    
}
