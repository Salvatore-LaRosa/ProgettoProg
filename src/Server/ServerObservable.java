
package Server;

import java.util.Observable;


public class ServerObservable extends Observable{

    public ServerObservable() {
        
    }
    
    public synchronized void change(String text){
        setChanged();
        notifyObservers(text);
    }
    
}
