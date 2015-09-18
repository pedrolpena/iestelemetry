/**
 *This class writes the deck box configuration info to the serial port
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
public class configureDeckBox_UDB9000_DS7000_Mode extends DeckBox{
    int delayTime = 350;


/**
 * This method is invoked when the class is instantiated and it starts
 * the class
 */
public void run(){
    freqFormat = new DecimalFormat("##.##");

    
if(port!=null){

    setFrequencyFilterNone();
    pause(delayTime);
    //setFilterStatus(true);
    //pause(delayTime);

    // set the freqs
        String temp="";
    for(double f = 7.00 ; f <= 16.00 ; f+=.25){


        if(f == 10.00 || f == 10.50  ||f == 11.00 || f == 11.50 || f == 12.00 || f == 12.50 || f ==13.00 || f ==13.50 ){
            // do nothig
            //pause(delayTime);
        }// end if
        else
        {
            
            temp+=f+" ";

        }// end else


    }// end for

        temp = temp.trim();
        this.setFrequencyFilter(temp); 
    
    setTXFreq("15.00");// this is so the silent ping pings at this freq that way it doesn't corrupt the data
    pause(delayTime);
    setReceiveSensitivity();
    pause(delayTime);
    //this.setEnableSilentPing(true);
    //pause(delayTime);
    //setRepeatInterval(99);
    //pause(delayTime);
    //this.ping();
    //pause(delayTime);
    //this.setEnabledRepeatInterval(true);
    pause(delayTime);
    


    }// end if

this.setJComponentsEnable(true);
    





}// end run



private void setFrequencyFilter(double ff){

            try{
     os = new PrintStream(port.getOutputStream());
     os.print("\rff=" + ff + "\r\n");

     if(os!=null){
         os.flush();
         os.close();
     }

}
    catch(Exception e){
    e.printStackTrace();
    }// end catch

}// end setFrequencyFilter


private void setFrequencyFilter(String ff){

            try{
     os = new PrintStream(port.getOutputStream());
     os.print("\rff=" + ff + "\r\n");

     if(os!=null){
         os.flush();
         os.close();
     }

}
    catch(Exception e){
    e.printStackTrace();
    }// end catch

}// end setFrequencyFilter


private void setFrequencyFilterNone(){

            try{
     os = new PrintStream(port.getOutputStream());
     os.print("\rff=none\n\r");

     if(os!=null){
         os.flush();
         os.close();
     }

}
    catch(Exception e){
    e.printStackTrace();
    }// end catch

}// end setFrequencyFilter

private int  getReceiveSensitivity(){
    //return 246*gain/10 +10;
return receiveThreshold;
}

private void setReceiveSensitivity(){

            try{
     os = new PrintStream(port.getOutputStream());
     os.print("\rrs="+getReceiveSensitivity()+"\n\r");

     if(os!=null){
         os.flush();
         os.close();
     }

}
    catch(Exception e){
    e.printStackTrace();
    }// end catch

}

 void setFilterStatus(Boolean fs){
    try{
     os = new PrintStream(port.getOutputStream());
     if(fs){

         os.print("\rfs=e\n\r");
     }

     else
         os.print("\rfs=d\n\r");

     if(os!=null){
         os.flush();
         os.close();
     }


    }catch(Exception e){
    e.printStackTrace();
    }

}// end

 void setEnabledRepeatInterval(Boolean rp){

            try{
     os = new PrintStream(port.getOutputStream());
     if(rp){

         os.print("\rrpe\r\n");
     }

     else
         os.print("\rrpd\r\n");

     if(os!=null){
         os.flush();
         os.close();
     }


    }catch(Exception e){
    e.printStackTrace();
    }






}// end setEnabledRepeatInterval

 private void setTXFreq(String ff){

            try{
     os = new PrintStream(port.getOutputStream());
     os.print("\rtx=" + ff + "\r\n");

     if(os!=null){
         os.flush();
         os.close();
     }

}
    catch(Exception e){
    e.printStackTrace();
    }// end catch

}// end setTXFreq


}// end class
