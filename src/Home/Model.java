package Home;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.ArrayList;
import rmiinterface.RMIInterface;

public class Model extends Observable{
    
    protected ArrayList<ArrayList<RMIInterface>> activeList;
    protected RMIInterface newEmail = null;
    protected int news; 
    private  int switchSR = 0;
        
    private Parent root;
    private final Stage stage;

    public Model() {
        news = 0;
        stage = new Stage();
        activeList = new ArrayList<>();
        activeList.add(new ArrayList<>());
        activeList.add(new ArrayList<>());
    }
   
    
    public int getNews(){
        System.out.println("news: " + news);
        if(news != 0){
            setChanged();
            notifyObservers("notification," + news);
           
        }
        return news;
    }

    
    public void setNews(int news){  
        this.news = news;
    }
    
        
    public void changeSwitchSR() {
         setChanged();
         if(switchSR == 1){
            switchSR = 0;
            notifyObservers("switch, Inviate");
        }else{
            switchSR = 1;
            notifyObservers("switch, Ricevute");
        }
       
    }
    
    public String writePage(int page){
        if(page<10){
            return "00"+page; 
        }else if(page >= 10 && page < 100){
            return "0" + page;
        }else
            return ""+page;
    }
    
    public void writePage(){
        int maxPage = activeList.get(switchSR).size();
        if(maxPage % 11 == 0)
            maxPage = (maxPage/11)-1;
        else
            maxPage = maxPage/11;
        
        setChanged();
        if(maxPage >= 99)
            notifyObservers("ErrorSpace, out of space. Space for: "
                        + "" + ((11 * 100) - activeList.get(switchSR).size()) + ""
                        + " Email,"+writePage(maxPage));
        else
             notifyObservers("ErrorSpace, ,"+writePage(maxPage));
        
    
    }
    
    public void getPreview(int index) { 
        ArrayList<String> arrString = new ArrayList<>();
        
        if(index >= activeList.get(switchSR).size()){
            arrString.add(""+index);
            arrString.add("");
            arrString.add("");
            arrString.add("");
            arrString.add("true");   
        }else{
            try {
                
 /* index  */   arrString.add(""+index);
/*Mittente */   arrString.add(suppPreviewMittente(index));
/*Argomento*/   arrString.add(suppPreview(activeList.get(switchSR).get(index).getArgomento()));
/*  Data   */   arrString.add(activeList.get(switchSR).get(index).getData());
/*disattivo*/   arrString.add("false");
            } catch (RemoteException ex) {
                setChanged();
                notifyObservers("error, connection broken");
            }
        }
     
        setChanged();
        notifyObservers(arrString);
    }
    
    
    private String suppPreviewMittente(int index) throws RemoteException{
        String tmp;
        if(switchSR == 0)
            tmp = activeList.get(switchSR).get(index).getMittente();
        else
            tmp = activeList.get(switchSR).get(index).getDestinatario();
       
        return  String.join(" ", tmp.split(","));
    }
    
    private String suppPreview(String tmp) throws RemoteException{
        return tmp;
    }
    
    
    public void openNewWindow(String path, String title, Object controller){
        if(!GetIsOpen()){
            notifyObservers("error, ");
            NewWindow(path, title, controller);
        }else
            notifyObservers("error,first close the windows");
        
        setChanged();
    }
    
    private void NewWindow(String path, String title, Object controller){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(path));
            fxmlLoader.setController(controller);
            root = (Parent) fxmlLoader.load();
            stage.setTitle(title);
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.show();
         
        } catch (IOException ex) {
            System.out.println(ex.getCause());
            System.out.println(ex.getMessage());
        }// aggiungere errore server
    }

    
    public int getSwitchSR() {
        return switchSR;
    }
    
    public boolean GetIsOpen() {
        return stage.isShowing();
    }    

    public Stage getStage() {
        return stage;
    }
    
   


    public int getId(int index){
        try {
            return activeList.get(switchSR).get(index).getId();
        } catch (RemoteException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        notifyObservers("error, no connection");
        setChanged();
        return -1;
        
    }
    
    public RMIInterface getEmail(int index){
        if(index <= activeList.get(switchSR).size())
            return activeList.get(switchSR).get(index-1);
        else
            return null;
    }
    
    public RMIInterface findEmail(int index, int switchSRManual){
        for(int i = 0; i != activeList.get(switchSRManual).size(); i++){
            try {
                if(activeList.get(switchSRManual).get(i).getId() == index)
                    return activeList.get(switchSRManual).get(i);
            } catch (RemoteException ex) {
                Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public void setNewEmail(RMIInterface newEmail) {
        this.newEmail = newEmail;
    }
    
    
    
    public RMIInterface getNewEmail() {
        return newEmail;
    }
    
    
}