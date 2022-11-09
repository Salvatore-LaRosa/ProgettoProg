package Server;


public class message {
    /*
     *  il flag è l'azione che si vuole fare se è uguale a :
     *          LOG_ID: sta ritornando l'id dell'user;
     *          LOG_ERROR: si è generato un errore;
                LOG_Login: si vuole effettuare il login
     */
   private int flag;
   private String message;
   private int id;

    public message(int flag, String message, int id) {
        this.flag = flag;
        this.message = message;
        this.id = id;
    }

    public int getFlag() {
        return flag;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
    
   public void set(int flag, String message, int id){
       this.flag = flag;
       this.message = message;
       this.id = id;
   }
}