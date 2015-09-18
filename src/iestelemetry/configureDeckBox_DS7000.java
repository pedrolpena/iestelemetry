/**
 *This class writes the deck box configuration for the DS-7000 to the serial port
 * and configures it. This class runs in the background as a thread.

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
public class configureDeckBox_DS7000 extends DeckBox{


/**
 * This method is invoked when the class is instantiated and it starts
 * the class
 */
public void run(){
    freqFormat = new DecimalFormat("##.##");

    
if(port!=null){
    setEnableDeckBoxReplies(false);
    pause(delayTime);
    setEnabledRepeatInterval(false);
    pause(delayTime);

// configure the channels
        for(int i = 1 ; i <= 8 ; i++){
            
            temp = freqFormat.format(freqTemp);
            if(i <= numberOfFrequencies+1)
                setChannnel("CH"+i, "RX"+temp, "TX15", "GA"+ (i<7?gain:gain+1));

             if(i > numberOfFrequencies+1)
                setChannnel("CH"+i, "RX15", "TX15", "GA0");

            freqTemp+=.5;
          
        }// end for
    
    setEnableDeckBoxReplies(true);
    pause(delayTime);
    setEnableSilentPing(false);
    pause(delayTime);
    //setRepeatInterval(99);
    //pause(delayTime);
    ping();
    pause(delayTime);
    //setEnabledRepeatInterval(true);
    //pause(delayTime);
    


    }// end if

  this.setJComponentsEnable(true);
    





}// end run

}// end class
