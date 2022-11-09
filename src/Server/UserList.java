
package Server;

public class UserList {
    private String username;
    private String password;
    private int id;
    
    /*
     * Modella l'oggetto user
     */
    public UserList(String username, String password, String id) {
        this.username = username;
        this.password = password;
        this.id = Integer.parseInt(id);
    }
    
    public int getId(){
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
    
    
    /*
        Controlla se password ed email coincidono, 
            se coincidono ritorna l'id
            -1 altrimenti
    */

    public int IsUser(String username, String password){
        if(this.username.equals(username) && this.password.equals(password))
            return getId();
        else
            return -1;
    }
}
