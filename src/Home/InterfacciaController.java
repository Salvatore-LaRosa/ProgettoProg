package Home;
import Read.ReadController;
import Server.Constants;
import Write.ScriviController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import java.net.URL;
import java.rmi.*;
import rmiinterface.RMIInterface;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class InterfacciaController implements Observer, Initializable{

    class reflash extends Thread {

    @Override
        public void run() {
            String[] str;
            Socket s;
            try {
                while(true){
                    String nomeLocale = InetAddress.getLocalHost().getHostName();

                    //System.out.println("Finestra del client:\n\tporta Server:" + nomeLocale);
                    s = new Socket(nomeLocale, 8189);
                    DataOutputStream dOut;
                    DataInputStream dIn;

                    dOut = new DataOutputStream(s.getOutputStream());
                    dIn = new DataInputStream(s.getInputStream());

                    //System.out.println("Ho aperto il socket verso il server.\n");

                    dOut.writeByte(Constants.START_UPDATE());
                    dOut.writeInt(id);
                    dOut.flush();

                    if((byte)dIn.read() == 0){
                        
                        //System.out.println("INIZIO ESTRAZIONE REGISTRO RMI DI: "+ id);
                        model.activeList.get(0).clear();
                        model.activeList.get(1).clear();
                        str = Naming.list("//127.0.0.1:"+ port +"/");
                        for (String str1 : str) {
                            if (str1.contains("ricevute" + String.valueOf(id)) && Naming.lookup(str1) instanceof RMIInterface){
                                model.activeList.get(0).add((RMIInterface) Naming.lookup(str1));
                            }else if (str1.contains("inviate" + String.valueOf(id)) && Naming.lookup(str1) instanceof RMIInterface){
                                model.activeList.get(1).add((RMIInterface) Naming.lookup(str1));
                            }else if (str1.contains("newEmail" + String.valueOf(id)) && Naming.lookup(str1) instanceof RMIInterface){
                                model.setNewEmail((RMIInterface)Naming.lookup(str1));
                            }
                        }
                        arrayPane = new ArrayList<>();
                        for(int index = 0; index != 11; index++)
                            arrayPane.add((Pane) listPane.getChildren().get(index));

                        model.setNews((byte)dIn.read());
                        reflash();
                        sleep(3000);
                    }
                }
            }catch(MalformedURLException | NotBoundException | RemoteException e){
                System.err.println("Client exception: " + e.toString());
            } catch (InterruptedException | IOException ex) {
                Logger.getLogger(InterfacciaController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private void reflash(){
        Platform.runLater(() -> {
            //System.out.println("Ricarico pagina");
            model.getNews();
        
            model.writePage();
          
            next();
            previous();
           changePage();
        });
    }


    private  Model model = new Model();
    private ArrayList<Pane>arrayPane;
    private int id = -1;
    private int port;
    private int page = 0;
    private String mittente;

    @FXML private Label email;
    @FXML private AnchorPane listPane;
    @FXML private Label currentPage;
    @FXML private Label totPage;
    @FXML private Button send;
    @FXML private Button btnNext;
    @FXML private Button btnPrevious;
    @FXML private Label error;
    @FXML private Label ErrorSpace;



    public void switchSR(){
        page = 0;
        model.changeSwitchSR();
        model.writePage();
        changePage();
        btnNext.setDisable(false);
        btnPrevious.setDisable(false);

        if(model.getSwitchSR() == 0)
            send.setText("ricevute");
        else
            send.setText("inviate");


        next();
        previous();
        
          
      
        
    }

  
    public void openEmail(Event e){
        String label = e.getSource().toString().substring(11, 13);
        int index = (Integer.parseInt(label)+(page *11));

        if(index > 0 ){
            if(model.getEmail(index) != null){
            ReadController controller =  new ReadController(model.getEmail(index), model.getStage(), id, model.getNewEmail(), email.getText());
                model.openNewWindow("Read/ricevute.fxml", "Read Email", controller);
            }
        }
    }
    
    public void ScriviAzione(){
        ScriviController controller =  new ScriviController(id, mittente, model.getNewEmail(), model.getStage());
        model.openNewWindow("Write/Scrivi.fxml", "Write Email", controller);
    }

    public void previous(){
        if(page == 1)
            btnPrevious.setDisable(true);

        if(page == Integer.parseInt(totPage.getText()))
            btnNext.setDisable(false);
        page --;
        
          currentPage.setText(model.writePage(page));
        changePage();
    }

    public void next(){
        if(page == 0)
            btnPrevious.setDisable(false);
        
        page ++;
        if(page >= Integer.parseInt(totPage.getText()))
            btnNext.setDisable(true);
        

        
          currentPage.setText(model.writePage(page));
        changePage();
    }


    public void changePage(){
        for(int index = 0; index != arrayPane.size(); index++)
            model.getPreview(index + (page * 11));
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof String) {
            System.out.println("update: " + (String) arg );
            String[] all = ((String) arg).split(",");
            switch (all[0]) {
                case "error":
                    error.setText(all[1]);
                    break;
                case "switch":
                    send.setText(all[1]);
                    break;
                case "ErrorSpace":
                    ErrorSpace.setText(all[1]);
                    totPage.setText(all[2]);
                    break;
                case "notification":
                    int tmp_Index = Integer.parseInt(all[1]);
                    RMIInterface tmp_Email = model.findEmail(tmp_Index, 0);
                    
                   //JOptionPane.showMessageDialog(null,"Hai ricevuto una nuova email");
                    
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Nuova Email");
          
                    try {
                        alert.setHeaderText("Mittente: " + tmp_Email.getMittente() + ""
                                + "\nOggetto: " + tmp_Email.getArgomento());
                   

                    ButtonType buttonTypeOne = new ButtonType("Mostra");
                    ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                    alert.getButtonTypes().setAll(buttonTypeOne,buttonTypeCancel);
                    Optional<ButtonType> result = alert.showAndWait();
                    
                    if (result.get() == buttonTypeOne){
                        ReadController controller =  new ReadController(
                                tmp_Email, model.getStage(), 
                                id, model.getNewEmail(), email.getText());
                        model.openNewWindow("Read/ricevute.fxml", "Read Email", controller);
                    } 
                    
                    } catch (RemoteException ex) {
                        Logger.getLogger(InterfacciaController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                default:
                    break;
                }
       
        }else if(arg instanceof ArrayList){
            
            ArrayList<String> tmp = (ArrayList<String>) arg;
            
            int index =(tmp.get(0) != null)?(Integer.parseInt(tmp.get(0))%11): 0;
            Pane tmpPane= arrayPane.get(index);
            ((Label)tmpPane.getChildren().get(0)).setText((tmp.get(1)));
            ((Label)tmpPane.getChildren().get(1)).setText((tmp.get(2)));
            ((Label)tmpPane.getChildren().get(2)).setText((tmp.get(3)));
            tmpPane.setDisable(Boolean.parseBoolean(tmp.get(4)));
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb){
        System.out.println("mittente :" + mittente);
        email.setText(mittente);
        System.out.println("id: " + id);
        model.addObserver(this);
        reflash contr = new reflash();
        contr.setDaemon(true);
        contr.start();
    }
    
    
    
    public void setMit(String mittente){
          this.mittente = mittente;
      }

      public void setId(int id) {
          this.id =(this.id == -1)? id : this.id;
      }

      public void setPort(int port) {
          this.port =port;
      }

}
