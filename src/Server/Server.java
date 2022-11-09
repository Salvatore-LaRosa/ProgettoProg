package Server;

import java.io.*;
import java.net.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import static Server.Server.listUser;
import static Server.Server.portUser;
import java.rmi.*;
import java.util.Observable;
import java.util.Random;

/*
 *	Classe di supporto.
 *	una per ogni client connesso
 */

//classe che estende la classe thread, essa può essere eseguita contemporaneamente ad altre attività
class NewClient extends Thread {
    private Socket incoming;
    ArrayList<UserList> list;
    ArrayList<Email> emailListRicevute;//contiene la lista di tutte le email dell'utente(viene inizializato quando un socket/utente si collega/login)
    ArrayList<Email> emailListInviate;

    private ServerObservable cs;
    /*
     * inizializzazione del Thread, incoming identifica il socket aperto con il client
     */

    public NewClient(Socket incoming, ArrayList<UserList> list, ArrayList<PortUser> portUser, ServerObservable cs) {
        super();
        this.cs = cs;
        this.incoming = incoming;
        this.list = list;
        //System.out.println(list);
    }


    public int login(String attributi){
        //System.out.println(attributi);
        String[] split = attributi.split(",");//separa la stringa ogni volta che trova una virgola

        for(int index = 0; index != list.size(); index++){
            int id = list.get(index).IsUser(split[0], split[1]); // <--- controlla se esiste una coppia {email, password} nella list uguale alla coppia {user, password} presente nella stringa "attributi"
            //System.out.println(list.get(index).getUsername() + ","+list.get(index).getPassword());
            if( id > 0){
                System.out.println("\tlogin id: " + id);
                return id;
                //in caso di successo ritorna id , altrimenti esce dal for e c'è return di -1 che ritorna un errore
            }
        }
        System.out.println("\tlogin failed");
        return -1;
    }

    //System.lineSeparator();//returns the right new line character for linux and windows
    public void getUserEmails(int userId, String directory){//crea per ogni email dell'utente un oggetto Email e lo inserisce in emailList(arrayList)
        if(directory.equals("ricevute")){
            emailListRicevute = new ArrayList<Email>();
        }else{
            emailListInviate = new ArrayList<Email>();
        }
        BufferedReader reader;
        Path f = Paths.get(userId+ File.separator + directory);//f punta alla cartella con nome = all'id dell'utente
        System.out.println(userId+ File.separator + directory);
        String [] lista = new File(f.toString()).list();//per ottenere ciò che c'è all'interno di f
	try {
            for(String i :lista){
                String path = f.toString() + File.separator + i;//es. path = "3/3-1.txt" o anche 3/3-5.txt
                reader = new BufferedReader(new FileReader(path));
                String line = reader.readLine();
                while (line != null) {
                    String [] tmp = line.split("\\|");
                    System.out.println(line);//per controllare quale mail sta elaborando
                    if(directory.equals("ricevute")){
                        emailListRicevute.add(new Email(Integer.parseInt(tmp[0]), tmp[1], tmp[2], tmp[3], tmp[4], tmp[5], cs));
                    }else{
                        emailListInviate.add(new Email(Integer.parseInt(tmp[0]), tmp[1], tmp[2], tmp[3], tmp[4], tmp[5], cs));
                    }
                    line = reader.readLine();
                }
                reader.close();
            }
	} catch (IOException e) {
            e.printStackTrace();
	}
    }
    /*
        cerca nell'array contenente gli users l'id relativo all'indirizzo email passatogli
    */
    public static int findUserId(String emailAddress){
        for(int i=0; i< listUser.size(); i++){
            UserList temp = listUser.get(i);
            if(temp.getUsername().equals(emailAddress)){
                return temp.getId();
            }
        }
        return -1;//indirizzo Email inesistente
    }

    /*
        cerca nell'array contenente gli users l'indirizzo email relativo all'Id passatogli
    */
    public static String findUserEmailAddress(int userId){
        for(int i=0; i< listUser.size(); i++){
            UserList temp = listUser.get(i);
            if(temp.getId() == userId){
                return temp.getUsername();
            }
        }
        return "FALSE";//Id inesistente
    }

    public static void setFlagNewEmail(int userId, int emailId){
        for(PortUser obj: portUser){//recupero la porta collegata con l'id
            if(obj.getPortUser()[0] == userId){
                obj.setIsNewEmail(emailId);
            }
        }
    }

	@Override
    synchronized public void run() {
        DataInputStream dIn;
        DataOutputStream dOut;
        int id = -1;  
        try {
            dIn = new DataInputStream(incoming.getInputStream());
            dOut = new DataOutputStream(incoming.getOutputStream());
            Byte input = dIn.readByte();
            int port =-1;   
            
            if(input == Constants.LOG_Login()){
                id = login(dIn.readUTF());
                cs.change("Nuova connessione azione richiesta: LOGIN");
                if(id != -1){
                    emailListRicevute = new ArrayList<>();
                    emailListInviate = new ArrayList<>();
                    port = creaRegistroRMI(id);//crea registro e inserisce la porta e l'id utente a cui si riferisce
                    portUser.add(new PortUser(id, port));
                    getUserEmails(id, "ricevute");
                    getUserEmails(id, "inviate");
                    caricaEmail(id, port);//devo passare anche la porta
                    Naming.rebind("//127.0.0.1:"+ port +"/newEmail" + String.valueOf(id), new Email(cs));
                }
                dOut.write(id);//invio l'id dell'utente
                dOut.flush();
                dOut.writeInt(port);//invio la porta su cui ascoltare
                dOut.flush();
            }else if(input == Constants.START_UPDATE()){
                cs.change("Nuova connessione azione richiesta: Update");
                //id = findUserId(dIn.readUTF());
                id= dIn.readInt();
                System.out.println("AGGIORNO REGISTRO DELL'UTENTE: "+ id);
                int flag = 0;
                boolean bool = false;
                for(PortUser obj: portUser){//recupero la porta collegata con l'id
                    if(obj.getPortUser()[0] == id){
                        port = (obj.getPortUser()[1]);
                        flag = obj.getIsNewEmail();
                        obj.setIsNewEmail(0);
                        bool = true;
                    }
                }
                if(bool){
                    eliminaEmailRegistro(port);
                    getUserEmails(id, "ricevute");
                    getUserEmails(id, "inviate");
                    Naming.rebind("//127.0.0.1:"+ port +"/newEmail" + String.valueOf(id), new Email(cs));
                    caricaEmail(id, port);//devo passare anche la porta
                    //Naming.rebind("//127.0.0.1:"+ port +"/newEmail" + String.valueOf(id), new Email());
                }
                dOut.writeByte(0/*se non bisogna aggiornare inserire 1*/);//invio la porta su cui ascoltare
                dOut.writeByte(flag);
                dOut.flush();
            }


            } catch (IOException e) {
                System.out.println("errore: " + e.getMessage());
        }
    }


    public void eliminaEmailRegistro(int port){//sostituisce tutti riferimenti del registro con null
        try{
             String str[] = Naming.list("//127.0.0.1:"+ port +"/");
             for(int i =0; i< str.length; i++){
                 Naming.rebind(str[i], new EmailChild());
             }
         }catch(Exception e){
              e.printStackTrace();
         }
    }

    public void caricaEmail(int id, int port){//carica nel RMI le email ricevute
        try{
            for(int i =0; i< emailListRicevute.size(); i++){
                Naming.rebind("//127.0.0.1:"+ port +"/ricevute" + String.valueOf(id)+ "-" + String.valueOf(i), emailListRicevute.get(i));//registro le Email nel regostro RMI
            }
            for(int i =0; i< emailListInviate.size(); i++){
                Naming.rebind("//127.0.0.1:"+ port +"/inviate" + String.valueOf(id)+ "-" + String.valueOf(i), emailListInviate.get(i));//registro le Email nel regostro RMI
            }
        }catch(Exception e){
             e.printStackTrace();
        }
    }

    public Socket getIncoming() {
        return incoming;
    }

    public int getPort(){
        Random r = new Random();
        return r.nextInt((65535 - 49152) + 1) + 49152;
    }

    public static boolean lanciaRMIRegistry(int port){//crea il registro sulla porta passatagli e ritorna true se è stato creato o false altrimenti(ovvero la porta è gia utilizzata da un altro registro)
        try{
            java.rmi.registry.LocateRegistry.createRegistry(port);
            System.out.println("java rmi registry created ta port: " + port);
            return true;
        }catch(RemoteException e){
            System.out.println("rmi registry at port: "+port +" already exists");
            return false;
        }finally{
        }
    }

    public int creaRegistroRMI(int id){
    int port = getPort();
    while(!lanciaRMIRegistry(port)){
        port = getPort();
    }
    return port;
    }

}

public class Server extends Thread{
    static ArrayList<UserList> listUser;
    static ArrayList<PortUser> portUser;
    private ServerSocket s;
    private ServerObservable cs;
    
    
    public Server(ServerObservable cs) { 
        this.cs = cs;
        portUser = new ArrayList<>();
        listUser = new ArrayList<>();
        readUser();
    }

    @Override
    public void run() {
        cs.change("AVVIO SERVER");
        try {
            s = new ServerSocket(8189);
            while(true) {
                Socket incoming = s.accept( );
                //cs.change("start connessione");
                NewClient client = new NewClient(incoming, listUser, portUser, cs);
                //cs.change("fine connessione");
                client.start();
            }
	} catch (IOException e) {
            e.printStackTrace();
        }
    }
        
     
    public static void readUser(){
        BufferedReader reader;
	try {
            //legge da file users.txt
            reader = new BufferedReader(new FileReader("." + File.separator +"users.txt"));
            String line = reader.readLine();
            while (line != null) {
                String [] tmp = line.split(",");
                listUser.add(new UserList(tmp[0], tmp[1], tmp[2]));
		    line = reader.readLine();

            }
            reader.close();
	} catch (IOException e) {
            e.printStackTrace();
	}

    }
}