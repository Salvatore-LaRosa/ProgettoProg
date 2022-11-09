package Login;

import Home.*;
import Server.Constants;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Observable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class Model extends Observable{
    private int id = 0;
    private int port = 0;
    private Socket s;
    
    public Model() {
        this.id = 0;
    }
    
    public void connect(String CampPassword, String CampEmail){
 
        if(!"".equals(CampPassword) && !"".equals(CampEmail)){
            //richiesta login al server  
            SuppConnect(CampEmail +","+ CampPassword);
            
            if(id > 0){
                try {
                    InterfacciaController controller = new InterfacciaController();
                    controller.setId(id);
                    controller.setMit(CampEmail);
                    controller.setPort(port);
                    FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Home/Facciata.fxml"));
                    loader.setController(controller);
                    
                    Parent root = loader.load();
                    Scene scene = new Scene(root);
                    Login.getStage().setResizable(false);
                    Login.getStage().setScene(scene);
        
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }else{
                if(id > -3){
                    setChanged();
                    notifyObservers("Errore 4:\n\tEmail o password errati, riprovare");
                }
            }
                
        }else{
            setChanged();
            if("".equals(CampEmail)) 
                notifyObservers("Inserisci l'email");
            else if("".equals(CampPassword))
                notifyObservers("Inserisci la password");
            else
               notifyObservers("inserisci l'email e la password");
        }
    }
    
    public void SuppConnect(String data){
        try{
            String nomeLocale = InetAddress.getLocalHost().getHostName();
            System.out.println("Finestra del clien:\n\tporta Server:" + nomeLocale);
            s = new Socket(nomeLocale, 8189);
            DataOutputStream dOut;
            DataInputStream dIn;

            dOut = new DataOutputStream(s.getOutputStream());
            dIn = new DataInputStream(s.getInputStream());

            System.out.println("Ho aperto il socket verso il server.\n");

            dOut.writeByte(Constants.LOG_Login());
            dOut.writeUTF(data);
            dOut.flush();
            
            this.id = (byte)dIn.read();
            setChanged();
            notifyObservers("okay");
            
            byte temp[]= new byte[4];
            int length =(byte)dIn.read(temp, 0, 4);
            this.port =(int) (long)byteToInt(temp, length);//salvo in port la porta a cui connettersi per RMI registry
            
        }catch (java.net.ConnectException e){
            this.id = -1;
            setChanged();
            notifyObservers("Errore 1:\n\tServer offline");
            
        }catch(java.rmi.UnknownHostException e){
            this.id = -2;
            setChanged();
            notifyObservers("Errore 2:\n\tProblemi con la connessione");
            
        }catch(IOException e){
            this.id = -3;
            setChanged();
            notifyObservers("Errore 3:\n\tProblemi con la connessione");
        }
    }

    public Socket getS() {
        return s;
    }
    
    public long byteToInt(byte[] bytes, int length) {//converte un array di byte in un int
        int val = 0;
        if(length>4) throw new RuntimeException("Too big to fit in int");
        for (int i = 0; i < length; i++) {
            val=val<<8;
            val=val|(bytes[i] & 0xFF);
        }
        return val;
    }

}
