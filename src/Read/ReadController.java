package Read;

import Server.Constants;
import Write.ScriviController;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import rmiinterface.RMIInterface;



public class ReadController extends Observable implements Observer, Initializable{
    @FXML Label mittente;
    @FXML Label destinatario;
    
    @FXML Label oggetto;
    @FXML TextArea idTextArea;
    @FXML Label data;
    @FXML Label SR;
    @FXML Button replayAll;
    
    
    private final RMIInterface email;
    private final RMIInterface newEmail;
    private final Stage stage;
    private final int id;
    private final String miaEmail;
    
    private Model model;
    
    
    public void SuppConnect(){
        try{
            String nomeLocale = InetAddress.getLocalHost().getHostName();
            System.out.println("Finestra del clien:\n\tporta Server:" + nomeLocale);
            Socket s = new Socket(nomeLocale, 8189);
            DataOutputStream dOut;
            //DataInputStream dIn;

            dOut = new DataOutputStream(s.getOutputStream());
            //dIn = new DataInputStream(s.getInputStream());

            System.out.println("Ho aperto il socket verso il server per richiedere aggiornamento emails.\n");

            dOut.writeByte(Constants.START_UPDATE());
            dOut.flush();
            dOut.writeByte(id);
            dOut.flush();

        }catch (java.net.ConnectException e){
            
            
        }catch(java.rmi.UnknownHostException e){
           
            
        }catch(IOException e){
       
        }
    }

    public ReadController(RMIInterface email, Stage stage, int id, RMIInterface newEmail, String miaEmail) {
        this.id = id;
        this.email = email;
        this.stage = stage;
        this.newEmail = newEmail; 
        this.miaEmail = miaEmail;        
    }
    
    public void replay(){
        try {
            ScriviController controller =  new ScriviController(id, 
                    email.getDestinatario() , mittente.getText(), 
                    oggetto.getText(), "", "replay", newEmail, stage, 
                    email.getTesto());
                    
                    
            model.openNewWindow("Write/Scrivi.fxml", "Write Email", controller);
        } catch (RemoteException ex) {
            Logger.getLogger(ReadController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void replayAll(){    
        String tmp = destinatario.getText() +"," + mittente.getText();
        String[] newDest = tmp.split(",");
        String[] altri = new String[newDest.length-1];
        int i = 0;
        for(String dest :newDest){
            if(!dest.equals(miaEmail))
                altri[i++] = dest;
        }
        
        tmp = String.join(",", altri);
        try{
        ScriviController controller =  new ScriviController(id, miaEmail, tmp, 
                oggetto.getText(), "", "replay", newEmail, stage,  email.getTesto());
        
        model.openNewWindow("Write/Scrivi.fxml", "Write Email", controller);
        } catch (RemoteException ex) {
            Logger.getLogger(ReadController.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }
    
    public void delete(){
        try{
            email.impostaEliminaEmail(id);
            SuppConnect();
            stage.hide();
        }catch(ExceptionInInitializerError e){
            System.err.println(e);
        } catch (RemoteException ex) {
            Logger.getLogger(ReadController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

   
    public void forwars(){
        ScriviController controller;
        try {
            controller = new ScriviController(id, email.getDestinatario(), 
                    mittente.getText(), oggetto.getText(), idTextArea.getText(),
                    "forwars",  newEmail, stage, email.getTesto());
             model.openNewWindow("Write/Scrivi.fxml", "Write Email", controller);
        } catch (RemoteException ex) {
            Logger.getLogger(ReadController.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }
    
    
        @Override
    public void update(Observable o, Object arg) {
        try{
            mittente.setText(email.getMittente());
            destinatario.setText(email.getDestinatario());           
            oggetto.setText(email.getArgomento());
            idTextArea.setText(email.getTesto());
            data.setText(email.getData());
        }catch(RemoteException e){
            
        }
    }
     
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        model = new Model(stage);
        try{
            String[] altri= email.getDestinatario().split(",");
            replayAll.setVisible(!(altri.length == 1));
        
            mittente.setText(email.getMittente());
            destinatario.setText(email.getDestinatario());
                      
            
            oggetto.setText(email.getArgomento());
            idTextArea.setText(email.getTesto());
            data.setText(email.getData());
        }catch(RemoteException e){
            stage.hide();
        }
    }
    
    
}