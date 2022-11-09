
package Server;


//classe contenente le definizioni di costanti usati nei messaggi

public class Constants {
    static private final int LOG_Login = 1;
    static private final int LOG_Id = 2;
    static private final int LOG_ERROR = 3;
    static private final int LOG_UPDATE = 4;
    static private final int START_UPDATE = 5;
    
    
    public static int LOG_Id() {
        return LOG_Id;
    }

    public static int LOG_Login() {
        return LOG_Login;
    }

    public static int LOG_ERROR() {
        return LOG_ERROR;
    }

    public static int LOG_UPDATE() {
        return LOG_UPDATE;
    }

    public static int START_UPDATE() {
        return START_UPDATE;
    }
    
    
}
