/**
 *This class writes the deck box configuration info to the serial port
 * and configures it. This class runs in the background as a thread.
 * 10-26-2011---- A silent ping is necessary periodically if not the
 * deck unit stops listening. therefore silent ping is enabled with a
 * 99 second period. This essentially makes the timer rollover value 99 seconds.
 * this must be taken into account when the values are calculated.
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
public class configureDeckBox extends Thread{
    SerialPort port;
    String deckBox = "";
    boolean stopped = false;
    PrintStream os = null;
    private int numberOfFrequencies = 0;
    private double freqTemp = 10.0;
    DecimalFormat freqFormat = null;
    String temp = "";
    int gain = 0;
    long delayTime = 350;
    JButton busyButton;

/**
 * This method is invoked when the class is instantiated and it starts
 * the class
 */
public void run(){
    freqFormat = new DecimalFormat("##.##");

    
if(port!=null){
    this.setEnableDeckBoxReplies(false);
    pause(delayTime);
    this.setEnabledRepeatInterval(false);
    pause(delayTime);

// configure the channels
        for(int i = 1 ; i <= 8 ; i++){
            
            temp = freqFormat.format(freqTemp);
            if(i <= numberOfFrequencies+1)
                this.setChannnel("CH"+i, "RX"+temp, "TX15", "GA"+ (i<7?gain:gain+1));

             if(i > numberOfFrequencies+1)
                this.setChannnel("CH"+i, "RX15", "TX15", "GA0");

            freqTemp+=.5;
          
        }// end for
    
    this.setEnableDeckBoxReplies(true);
    pause(delayTime);
    this.setEnableSilentPing(true);
    pause(delayTime);
    setRepeatInterval(99);
    pause(delayTime);
    this.ping();
    pause(delayTime);
    this.setEnabledRepeatInterval(true);
    pause(delayTime);
    


    }// end if

    busyButton.setEnabled(true);
    





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


/**
 * sends the ping command to the deck box
 */
private void ping(){

        try{
     os = new PrintStream(port.getOutputStream());


         os.print("\rPI\n\r");



     if(os!=null){
         os.flush();
         os.close();
     }


    }catch(Exception e){
    e.printStackTrace();
    }

}// end silentPing
/**
 * Enables silent pinging on the deck box
 * @param sp Boolean- true for pinging and false for no pingin
 */
private void setEnableSilentPing(Boolean sp){
    try{
     os = new PrintStream(port.getOutputStream());
     if(sp){ 
     
         os.print("\rSPE\n\r");
     }
     
     else
         os.print("\rSPD\n\r");
     
     if(os!=null){
         os.flush();
         os.close();
     }
        
    
    }catch(Exception e){
    e.printStackTrace();
    }

}// end setEnableSilentPing
/**
 * Enabled deck box replies on the deck box.
 * @param on boolean - true for enable and false for disable
 */
private void setEnableDeckBoxReplies(boolean on){

        try{
     os = new PrintStream(port.getOutputStream());
     if(on){

         os.print("\rONE\n\r");
     }

     else
         os.print("\rOND\n\r");

     if(os!=null){
         os.flush();
         os.close();
     }


    }catch(Exception e){
    e.printStackTrace();
    }
} // end enable DeckBox Replies

/**
 * Used to disable the button that starts this process, so that another thread 
 * cannot be started
 * @param jb
 */
public void setButton(JButton jb){
    this.busyButton = jb;
}// end setButton

/**
 * Sets the gain for the deck box
 * @param g - int holds a value between 0 and 10
 */
public void setGain(int g){
    gain = g;

}// end setGain()


/**
 * Enable the repeat interval option on the deck box
 * @param rp  Boolean true for enable and false for disable
 */
private void setEnabledRepeatInterval(Boolean rp){

            try{
     os = new PrintStream(port.getOutputStream());
     if(rp){

         os.print("\rRPE\n\r");
     }

     else
         os.print("\rRPD\n\r");

     if(os!=null){
         os.flush();
         os.close();
     }


    }catch(Exception e){
    e.printStackTrace();
    }






}// end setEnabledRepeatInterval


/**
 * Set the repeat interval  on the deck box
 * @param ri  A value between 0 and 99
 */
private void setRepeatInterval(int ri){

            try{
     os = new PrintStream(port.getOutputStream());
     if(ri<0 || ri > 99){

         //os.print("\rRPE\n\r");
     }

     else
         os.print("\rRI" + ri + "\n\r");

     if(os!=null){
         os.flush();
         os.close();
     }


    }catch(Exception e){
    e.printStackTrace();
    }


}// end setEnabledRepeatInterval

/**
 * Set s the deck unit's channel to the specified frequencies and the gain for that channel
 *
 * @param ch the channel to modify
 * @param rxFreq the frequency to modify
 * @param txFreq the frequency to modify
 * @param gain the gain for that channel
 */
private void setChannnel(String ch,String rxFreq,String txFreq, String gain){
    
 try{
     os = new PrintStream(port.getOutputStream());
     os.print("\r"+ch+"\n\r");
     pause(delayTime);
     os.print("\r"+rxFreq+"\n\r");
     pause(delayTime);
     os.print("\r"+txFreq+"\n\r");
     pause(delayTime);
     os.print("\r"+gain+"\n\r");
     pause(delayTime);

     if(os!=null){
         os.flush();
         os.close();
     }


 }

 catch(Exception e){
    e.printStackTrace();
    }



}//end setCjannel
/**
 * sets the number of frequencies to configure.
 * @param f int- the number of frequencies
 */
public void setNumberOfFrequencies(int f){
    numberOfFrequencies = f;

}

/**
 * pause the thread t milliseconds
 * @param t long - the number of milliseconds to pause the thread.
 */
private void pause(long t){
    try{
        this.sleep(t);
    }

    catch(Exception e){
        e.printStackTrace();

    }


}


}// end class
