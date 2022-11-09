package Server;

import static Server.NewClient.findUserEmailAddress;
import static Server.NewClient.findUserId;
import static Server.NewClient.setFlagNewEmail;
import java.rmi.Naming;
import java.io.*;
import java.rmi.RemoteException;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class Email extends java.rmi.server.UnicastRemoteObject implements rmiinterface.RMIInterface{

    private int id;
    private String data;
    private String mittente;
    private String destinatario;
    private String argomento;
    private String testo;
    
    private ServerObservable cs = null;

    public Email(int id, String data, String mittente, String destinatario,
            String argomento, String testo, ServerObservable cs) throws RemoteException{
        this.id=id;
        this.data=data;
        this.mittente=mittente;
        this.destinatario=destinatario;
        this.argomento=argomento;
        this.testo=testo;
        this.cs = cs;
    }

    public Email(ServerObservable cs) throws RemoteException{
    
        this.cs = cs;
    }//per creare una istanza di email vuota da caricare nel registro RMI

    public void impostaInvioEmail(int userId){    
        try{
            String [] tmp = this.getDestinatario().split(",");
            int listaDestinatari[];
            listaDestinatari = new int[tmp.length];
            for(int i=0; i<tmp.length; i++){
                listaDestinatari[i] = findUserId(tmp[i]);
            }
            synchronized(this){//esegue il blocco in mutua esclusione
                    this.inviaEmail(userId, listaDestinatari);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void impostaEliminaEmail(int userId){
        try{
            String [] tmp = this.getMittente().split(",");
            this.eliminaEmail(userId, findUserId(tmp[0]));
        }catch(RemoteException e){
            System.err.println(e);
        }
    }

    public void impostaGiraEmail(int userId, String destinatari){
        try{
            String [] tmp = this.getMittente().split(",");
            System.out.println(this.getMittente());
            System.out.println(this.getDestinatario());
            for(int i=0; i<tmp.length; i++){
                System.out.println("contenuto di tmp:" + tmp[i] +"");
            }
            String userEmailAddress = findUserEmailAddress(userId);

            destinatari = destinatari.replaceAll(" ","");
            String [] tmp2 = destinatari.split(",");

            int [] array = new int [tmp2.length];

            for(int index = 0; index < tmp2.length; index++ ){
                array[index] = findUserId(tmp2[index]);
            }
            if(userId == findUserId(tmp[0])){
                this.salvaEmail(userId, array, "inviate", userEmailAddress);
            }else{
                this.salvaEmail(userId, array, "ricevute", userEmailAddress);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean checkDestinatari(){//ritorna false se uno degli indirizzi email inseriti è inesistente
        try{
            String [] tmp = this.getDestinatario().split(",");
            for(int i=0; i<tmp.length; i++){
                if(findUserId(tmp[i]) == -1) {return false;}
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public void salvaEmail(int userId, int[] destId, String str, String userAddress){
        try{
            cs.change("Salvo email di :" + mittente);
            File temp = new File(userId + File.separator + str);
            File f = new File(userId + File.separator + temp.getName(), String.valueOf(this.getId()) + ".txt");
            File dir;
            File f2;
            InputStream is;
            OutputStream os;
            for(int i : destId){
                dir = new File(i + File.separator +"ricevute");
                f2 = new File(i + File.separator + dir.getName(), f.getName());
                //fà una copia del file nella cartella ricevute del destinatario
                is = new FileInputStream(f);
                os = new FileOutputStream(f2);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    if (userAddress != null){//se dobbiamo guarere la mail aggiungo l'indidirizzo email di chi la sta girando
                        String ex = new String(buffer);
                        String [] tmp = ex.split("\\|");
                        String [] tmp2 = tmp[2].split(",");
                        tmp[2] = tmp2[0] + "," + userAddress;
                        ex = String.join("|", tmp);
                        System.out.println("BUFFER :" + ex);
                        buffer = ex.getBytes();
                    }
                    os.write(buffer, 0, length);
                }
                is.close();
                os.close();
                cs.change("INVIO email n. "+this.id+"  da: "+this.mittente+"  verso: "+this.destinatario+"");
                
            }
        }catch(IOException e){
            System.err.println("Errore n°9:'\n\tEmail non registrata nel sistema");
            e.printStackTrace();
        }
    }

    public void eliminaEmail(int userId, int mittente){//bisogna passare l'id dell'utente e quello del mittente
        File f;
        try{
            if(userId == mittente){//se l'ho inviata io, la elimino nelle Email inviate altrimento l'ho ricevuta e la elimino tra quelle ricevute
                f = new File(userId + File.separator +"inviate"+ File.separator + String.valueOf(this.getId()) + ".txt");
            }else{
                f = new File(userId + File.separator +"ricevute"+ File.separator + String.valueOf(this.getId()) + ".txt");
            }
            if(f.exists()){
                f.delete();
            }
        }catch(RemoteException e){
            System.err.println(e);
        }
    }

    public boolean inviaEmail(int userId, int[] destId){//bisogna passare l'id dell'utente che invia e del/dei destinatari
        try{
            /*
            la lettura e l'aggiornamento del file ID.txt devono essere fatti in mutua esclusione per evitare email con lo stesso id
            */

            //inizio mutua esclusione file ID.txt
            BufferedWriter writer;
            int ID;
            synchronized(cs){
                cs.change("leggo ID");
                BufferedReader reader = new BufferedReader(new FileReader("ID.txt"));
                String line = reader.readLine();
                reader.close();
                //fine fase di lettura di ID.txt
                ID = Integer.valueOf(line) +1;
                //inizio fase di aggiornamento
                writer = new BufferedWriter(new FileWriter("ID.txt"));
                writer.write(String.valueOf(ID));
                writer.close();
                this.setId(ID);//inserisco l'id nella Email
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                this.setData(dateFormat.format(date).replace(" ", ", "));//insrisco la data di invio della mail
            }
            //fine fase di aggiornamento e fine mutua esclusione
            /*
            creo il file .txt contenente tutti i dati della Email
            */
            File dir = new File(userId + File.separator + "inviate");
            File f = new File(userId + File.separator + dir.getName(), String.valueOf(ID) + ".txt");//crea un path astratto a cui punta f, ad es 3.txt
            if(!f.createNewFile()){
                System.out.println("file .txt non creato");
            }
            writer = null;
            try{//inserisco il contenuto della Email nel file permanente
                writer = new BufferedWriter(new FileWriter(userId + File.separator +"inviate"+ File.separator + f.getName()));
                String contenuto = String.valueOf(this.getId() +"|"+this.getData()+"|"+this.getMittente()+"|"+this.getDestinatario()+"|"+this.getArgomento()+"|"+this.getTesto());
                writer.write(contenuto);
            } finally {
                writer.close();
            }
            for(int i=0; i<destId.length;i++){
                setFlagNewEmail(destId[i], ID);
            }
            salvaEmail(userId, destId, "inviate", null);

        }catch(IOException e){
            e.printStackTrace();
        }
        return true;//se la mail è stata salvata correttamente in un file txt nella cartella delle email dell'utente
    }

    public int getId()throws RemoteException{
        return this.id;
    }
    public String getData()throws RemoteException{
        return this.data;
    }
    public String getMittente()throws RemoteException{
        return this.mittente;
    }
    public String getDestinatario() throws RemoteException{
        return this.destinatario;
    }
    public String getArgomento()throws RemoteException{
        return this.argomento;
    }
    public String getTesto()throws RemoteException{
        return this.testo;
    }

    public void setId(int newId){
        this.id = newId;
    }
    public void setData(String newData){
        this.data = newData;
    }
    public void setMittente(String mittente)throws RemoteException{
        this.mittente = mittente;
    }
    public void setDestinatario(String destinatario)throws RemoteException{
        this.destinatario = destinatario;
    }
    public void setArgomento(String argomento)throws RemoteException{
        this.argomento = argomento;
    }
    public void setTesto(String testo)throws RemoteException{
        this.testo = testo;
    }
}
