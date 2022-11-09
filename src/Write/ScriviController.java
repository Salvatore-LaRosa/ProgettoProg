/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Write;


import Server.Constants;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import javafx.scene.control.TextField;
import java.rmi.RemoteException;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import rmiinterface.RMIInterface;

public class ScriviController extends Observable implements Observer, Initializable{

    

    @FXML private TextField Destinatario;
    @FXML private TextField Oggetto;
    @FXML private TextArea CorpoMail;
    @FXML private TextArea Risposta;
    @FXML private Label error;
    
    private int id;
    ScriviModel model;
    
    
    private RMIInterface newEmail = null;
    /*campi caso base*/
    private String mittente;
    
    /*campi per inoltro e rispondi*/
    private String str_detinatario;
    private String str_oggetto;
    private String text;
    private String flag;
    private Stage stage;
    private String oldText;

    public void SuppConnect(String str){
        try{
            String nomeLocale = InetAddress.getLocalHost().getHostName();
            System.out.println("Finestra del clien:\n\tporta Server:" + nomeLocale);
            Socket s = new Socket(nomeLocale, 8189);
            DataOutputStream dOut = new DataOutputStream(s.getOutputStream());

            System.out.println("Ho aperto il socket verso il server per richiedere aggiornamento emails.\n");

            dOut.writeByte(Constants.START_UPDATE());
            dOut.flush();
            dOut.writeUTF(str);
            dOut.flush();

        }catch (java.net.ConnectException e){
            error.setText("connessione al server interrotta");
        }catch(java.rmi.UnknownHostException e){
            error.setText("connessione al server interrotta");
        }catch(IOException e){
            error.setText("connessione al server interrotta");
        }
    }
    
    
    public ScriviController(int id, String mittente, RMIInterface newEmail, Stage stage) {
        this.newEmail = newEmail;
        this.id = id;
        this.mittente = mittente;
        this.stage = stage;
    }
    
    public ScriviController(int id, String mittente, String str_detinatario, 
            String str_oggetto, String text, String flag, RMIInterface newEmail, 
            Stage stage, String oldText) {
        this.mittente = mittente;
        this.newEmail = newEmail;
        this.id = id;
        this.str_detinatario = str_detinatario;
        this.str_oggetto = str_oggetto;
        this.flag = flag;
        this.text = text;
        this.stage = stage;
        this.oldText = oldText;

    }
    
    public void InviaMail(){
        String campoDestinatario;
        String campoOggetto;
        String campoCorpo;

        campoDestinatario = Destinatario.getText();
        campoOggetto = Oggetto.getText();
        campoCorpo = CorpoMail.getText();
        
        
        campoDestinatario = supp(campoDestinatario);
        try {  
            if("replay".equals(flag))
                campoCorpo = "[risposta a: " + oldText + " ] " + campoCorpo;
            
            model.SetEmail(campoDestinatario, campoOggetto, campoCorpo);
        }catch (RemoteException ex){
            Logger.getLogger(ScriviController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try{
            newEmail.impostaInvioEmail(id);
            
         }catch (RemoteException ex) {
             Logger.getLogger(ScriviController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    @Override
     public void initialize(URL location, ResourceBundle resources) {
        model = new ScriviModel(id, mittente, newEmail);
        model.addObserver(this);
        Oggetto.setDisable(true);
        
        if("replay".equals(flag)){
            System.out.println("sono in: " + flag);
            Risposta.setVisible(true);
            Risposta.setText(oldText);
        }else{
            Risposta.setVisible(false);
            Risposta.setText(" ");
        }

        
        if(null == flag){
            CorpoMail.setDisable(false);
            Oggetto.setDisable(false);
            Destinatario.setDisable(false);
        }else switch (flag) {
            case "replay":
                Destinatario.setText(str_detinatario);
                if(!str_oggetto.subSequence(0, 4).equals("[RE]"))
                    Oggetto.setText("[RE] " + str_oggetto); 
                else
                    Oggetto.setText(str_oggetto); 
                Destinatario.setDisable(true);
                break;
            case "forwars":
                if(!str_oggetto.subSequence(0, 4).equals("[RE]"))
                    Oggetto.setText("[IN] " + str_oggetto); 
                CorpoMail.setText("[Inoltrata da "+ str_detinatario + "]  " + text);
                CorpoMail.setDisable(true);
                break;
            default:
                CorpoMail.setDisable(false);
                Oggetto.setDisable(false);
                Destinatario.setDisable(false);
                break;
        }
            
    }
    
    private String supp(String campoDestinatario) {
        return campoDestinatario.replaceAll("\\s+","");
    }

@Override
    public void update(Observable o, Object arg) {
        if (arg instanceof String) {
            String[] all = ((String) arg).split(",");
            if(null != all[0])
                switch (all[0]) {
                    case "error":
                        error.setText(all[1]);
                        break;
                    case "ok":
                        SuppConnect(mittente);
                        String str[]=Destinatario.getText().split(",");
                        for(String dest: str){
                            SuppConnect(dest);
                        }
                        stage.hide();
                    default:
                        break;
                }
        }
    }
    
    
}


