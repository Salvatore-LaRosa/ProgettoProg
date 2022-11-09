
package Server;


 public class PortUser {
     private int id;
     private int port;
     private int isNewEmail;

     public PortUser(int user, int port){
         this.id = user;
         this.port = port;
         this.isNewEmail = 0;
     }

     public int[] getPortUser(){
         int array[] = new int[2];
         array[0] = this.id;
         array[1] = this.port;
         return array;
     }

     public void setIsNewEmail(int emailId){
         this.isNewEmail = emailId;
     }

     public int getIsNewEmail(){
         return this.isNewEmail;
     }
 }
