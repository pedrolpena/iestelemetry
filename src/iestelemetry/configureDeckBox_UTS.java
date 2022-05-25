/**
 *This class writes the deck box configuration info to the serial port
 * and configures it. This class runs in the background as a thread.
 * 02/21/2013 changed continuous transponde command to ats15=6 so that 
 * box does not start in continous transponde mode
 * 05/20/2017 added echo on for new deck box
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
public class configureDeckBox_UTS extends DeckBox{


/**
 * This method is invoked when the class is instantiated and it starts
 * the class
 */
public void run(){
    freqFormat = new DecimalFormat("##.##");

    
if(port!=null){

    sendEscapeSequence();
    pause(delayTime);
    echoOn();
    pause(delayTime);

    for(double f = 7.00 ; f <= 16.00 ; f+=.25){


        if(f == 10.00 || f == 10.50  ||f == 11.00 || f == 11.50 || f == 12.00 || f == 12.50 || f ==13.00 || f ==13.50 ){

            adjustChannelReceiveThreshold(f,0);
            pause(delayTime);
        }// end if
        else
        {
            adjustChannelReceiveThreshold(f,-1.00);
            pause(delayTime);


        }// end else


    }// end for

    setGlobalReceiveThreshold(receiveThreshold);
    pause(delayTime);
    setRXPulseWidth(receivePulseWidth);
    pause(delayTime);
    setTemperatureCompensatedOscillator(2);         // setting because URI says so
    pause(delayTime);
    setListenTimeout(30);   //sets listening timeout to 25  secs 
    pause(delayTime);    
    startContinuousTranspondMode();
    pause(delayTime);
    }// end if

   
     this.setJComponentsEnable(true);


clearPort();


}// end run

public void sendEscapeSequence(){
    try{
     os = new PrintStream(port.getOutputStream());
     os.print("+++");
     os.print("\r");


     if(os!=null){
         os.flush();
         os.close();
     }

}
    catch(Exception e){
    e.printStackTrace();
    }// end catch


}// end sendEscapeSequnce


public void startContinuousTranspondMode(){
    try{
     os = new PrintStream(port.getOutputStream());
     os.print("\rats15=6\r");


     if(os!=null){
         os.flush();
         os.close();
     }

}
    catch(Exception e){
    e.printStackTrace();
    }// end catch


}//end continuousTranspondMode

public void adjustChannelReceiveThreshold(double ch, double val){
    try{
     os = new PrintStream(port.getOutputStream());
     os.print("rxadj " + ch + " " + val + "\r");
     
     if(os!=null){
         os.flush();
         os.close();
     }

}
    catch(Exception e){
    e.printStackTrace();
    }// end catch

}// end adjustChannelReceiveThreshold

public void setGlobalReceiveThreshold(int rst){

    try{
     os = new PrintStream(port.getOutputStream());
     os.print("@RxThresh=" + rst + "\r");

     if(os!=null){
         os.flush();
         os.close();
     }

}
    catch(Exception e){
    e.printStackTrace();
    }// end catch

}// end setGlobalReceiveThreshold

void setRecievePulseWidth(int rpw){
receivePulseWidth = rpw;
}// end setRecievePulseWidth

void setRXPulseWidth(int rpw){

    try{
     os = new PrintStream(port.getOutputStream());
     os.print("@RxToneDur=" + rpw + "\r");

     if(os!=null){
         os.flush();
         os.close();
     }

}
    catch(Exception e){
    e.printStackTrace();
    }// end catch    
}//end set Ping Length

void setTemperatureCompensatedOscillator(int tco){

    try{
     os = new PrintStream(port.getOutputStream());
     os.print("@SyncPPS=" + tco + "\r");

     if(os!=null){
         os.flush();
         os.close();
     }

}
    catch(Exception e){
    e.printStackTrace();
    }// end catch  
}//set TCO


public void clearDataBuffer(){
    
    try{
     os = new PrintStream(port.getOutputStream());
     os.print("atbc" + "\r");

     if(os!=null){
         os.flush();
         os.close();
     }

}
    catch(Exception e){
    e.printStackTrace();
    }// end catch     
    
}// end clear data buffer

public void setListenTimeout(int to)
{
    try{
     os = new PrintStream(port.getOutputStream());
     os.print("ats7=" + to + "\r");

     if(os!=null){
         os.flush();
         os.close();
     }

}
    catch(Exception e){
    e.printStackTrace();
    }// end catch       
    
}//end setListenTimeout


public void echoOn(){
    
    try{
     os = new PrintStream(port.getOutputStream());
     os.print("echo on" + "\r");

     if(os!=null){
         os.flush();
         os.close();
     }

}
    catch(Exception e){
    e.printStackTrace();
    }// end catch     
    
}// end echo on


}// end class
