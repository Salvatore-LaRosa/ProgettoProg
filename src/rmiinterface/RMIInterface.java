
package rmiinterface;
import java.rmi.*;

public interface RMIInterface extends Remote
{
    
    int getId()throws RemoteException;
    String getData()throws RemoteException;
    String getMittente()throws RemoteException;
    String getDestinatario() throws RemoteException;
    String getArgomento()throws RemoteException;
    String getTesto()throws RemoteException;
    
    void setMittente(String mittente)throws RemoteException;
    void setDestinatario(String destinatario)throws RemoteException;
    void setArgomento(String argomento)throws RemoteException;
    void setTesto(String testo)throws RemoteException;
    
    
    public void impostaEliminaEmail(int userId)throws RemoteException;
    public void impostaInvioEmail(int userId) throws RemoteException;
    public void impostaGiraEmail(int userId, String destinatari)throws RemoteException;
    public boolean checkDestinatari()throws RemoteException;
    
}
