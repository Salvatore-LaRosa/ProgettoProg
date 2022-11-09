/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Write;



import java.rmi.RemoteException;
import java.util.Observable;
import rmiinterface.RMIInterface;



public class ScriviModel extends Observable{
    
    private final int userId;
    private final String mittente;
    private  RMIInterface newEmail;
    
    public  ScriviModel(int userId, String mittente, RMIInterface newEmail){
        this.userId = userId;
        System.out.println("id, scrivi: "+ userId);
        this.mittente = mittente;
        this.newEmail = newEmail;
    }

    
    public boolean SetEmail(String destinatario, String argomento, String testo) throws RemoteException{
        /*controllo l'email*/
        if(destinatario.length() <= "@gmail.it".length()){
            setChanged();
            notifyObservers("error, email non esistente");
            return false;
        }else{
            newEmail.setDestinatario(destinatario);
            if(!newEmail.checkDestinatari()){
                setChanged();
                notifyObservers("error, email non esistente");
                return false;
            }
        }
        
        /*controllo argomento**/
        if(argomento.length() == 0){
            setChanged();
            notifyObservers("error, Argomento nullo");
            return false;
        }else {
            newEmail.setArgomento(argomento);
            newEmail.setMittente(mittente); 
            newEmail.setTesto(testo);    
          
        }
        setChanged();
        notifyObservers("ok, ");
        return true;
    }
}
    
    

