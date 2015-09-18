/**
 * This class sends a single ping
 *  
 **/

package iestelemetry;
import gnu.io.*;
import java.io.*;
import java.text.DecimalFormat;
import javax.swing.*;



/**
 * This class writes the deck box configuration info to the serial port
 * and configures it. This class runs in the background as a thread.
 * @author Pedro Pena
 */
public class SendSinglePing extends Thread{
    SerialPort port;
    String deckBox = "";
    boolean stopped = false;
    PrintStream os = null;

    JComponent jComponents[];

/**
 * This method is invoked when the class is instantiated and it starts
 * the class
 */
public void run(){


            try{


     os = new PrintStream(port.getOutputStream());
  
     os.print("\n\r");
     Thread.sleep(300);
     os.print("pi\n\r");
     if(os!=null){
         os.flush();
         os.close();
     }

}
    catch(Exception e){
    e.printStackTrace();
    }// end catch

      


 this.setJComponentsEnable(true);


// busyButton.setEnabled(true);

}// end run

/**
 * Sets the serial port to be opened.
 * @param sp
 */
public void setPort(SerialPort sp){
    port = sp;

}// end setPort
/**
 * sets the deck box to be used
 * @param db String- name of the deck box to be used.
 */
public void setDeckBox(String db){
    deckBox = db;
}// end setDeckBox

public void stopThread(){
    stopped=true;

}// end stopThread




public void setJComponents(JComponent j[]){
    jComponents = j;

}// end

 public void setJComponentsEnable(boolean e)   {
    for (int i = 0 ; i < jComponents.length ; i++){
        jComponents[i].setEnabled(e);

    }// end for}

}
}
