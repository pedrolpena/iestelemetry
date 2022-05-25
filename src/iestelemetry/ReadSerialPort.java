


package iestelemetry;
import java.io.*;
import gnu.io.*;

import java.awt.Color;
import javax.swing.*;
import java.util.*;
import java.awt.Toolkit;
import java.util.TimerTask;

/**
 * This class runs in the background reading data from the serial port,
 * updating the raw data display and creating a raw data file
 * @author Pedro Pena
 * 1.26.2012  removed 5 second sleep when stopping thread.
 * 1.11.2013 removed buffered reader in main loop of ReadSerialPort.java and no longer using readline to read data coming in. readline does not have a timeout feature.
 * Now data is being read one character at a time using InputStreamReader's read and appending it to a string. The line is considered read when a '\r' is detected.
 * at which time a the epoch time is added to the raw data file also fixing the duplicated timestamp problem.
 * 1.11.2013 epoch time is now sent to ConvertIncommingFreq2Data so that the timestamps are the same throughout
 *
 */


public class ReadSerialPort extends Thread{


    private SerialPort port = null;
    private javax.swing.JTextArea rawDataDisplay = null;
   static private JTextArea convertedDataDisplay = null;
    private boolean stopped = false;
    private javax.swing.JTextField savePath = null;
    private String temp = "",someChar="";
    private String serialNumber=null;
    private String IESType = null;
    private String IESModel = null;
    private String IESFirmware = null;
    private double deckBoxCounter = 0.00;

    static private ConvertIncommingFreq2Data cnvData;
    static String []order;// = {"10.5","11.5","12.0","12.5","13.0"};
    String []line = null;
    double currentFreq = 0.0;
    String freqNow = "", deckBoxNow = "";
    private JLabel markerLabel=null;
    static private double cnvTimeOut = 0.0;
    private static String deckBoxType = null;
    static private String path = "";
    private int MSBCount=0;
    private JLabel MSBCounterLabel = null;
    private JLabel countDown = null;
    private double timerLength = 0.0;
    private static long epochTime = 0;
    Toolkit toolkit;
    java.util.Timer timer;
    DecrementMSB decMSBThread;
   
    JProgressBar theProgressBar = null;
//    moveProgressBar mpb=null;
    //InputStreamReader ist = null;

/**
 *Constructor
 * @param sp Serial port object that is opened
 * @param ta An array of JTextAreas that are updated with the data
 * @param tf An array of JTextFields that are updated by the class
 * @param cb An Array of JComboFields updated by the class
 */

   public ReadSerialPort(SerialPort sp, javax.swing.JTextArea []ta ,JTextField tf[], JComboBox cb[]){
       port = sp;
       rawDataDisplay = ta[0];
       convertedDataDisplay=ta[1];
       savePath = tf[0];
       serialNumber=tf[1].getText();
       temp = "";
       IESType = cb[0].getSelectedItem().toString();
       IESModel = cb[1].getSelectedItem().toString();
       IESFirmware = cb[2].getSelectedItem().toString();
       order = new String[]{cb[3].getSelectedItem()+"",cb[4].getSelectedItem()+"",cb[5].getSelectedItem()+"",cb[6].getSelectedItem()+"",cb[7].getSelectedItem()+""};
       cnvTimeOut = 1000*(new Double(cb[8].getSelectedItem()+"").doubleValue());

    }//end constructor
/**
 * stops the thread
 */
   public void stopThread(){
       if(decMSBThread!=null && decMSBThread.isAlive()){
       this.decMSBThread.stop();
       }//end if
/*
       if(mpb!=null && mpb.isAlive){
           mpb.stopMoveProgressBar();

       }// end if
*/
       stopped = true;
   }// end stopThread

 /**
  * closes the serial port in use
  */
   public void closePort(){
       try{
          //Thread.sleep(5000);
           //ist.close();
           stopped=true;
           port.getOutputStream().close();
           port.getInputStream().close();
           port.close();

       }
       catch(Exception e){
           e.printStackTrace();
       }



   }//end close port

/**
 * sets the label that will be updated LSB or MSB whenever a marker is detected
 * @param j Jlabel that holds the label that show the marker.
 */
   public void setMarkerLabel(JLabel j){
       markerLabel = j;


   }

   /**
    * sets the frequencies
    * @param j JComboBox[] that holds the frequencies that are mapped to measurements
    */
   public void setFreqsAndPeriod(JComboBox[] j){
       order = new String[]{j[0].getSelectedItem()+"",j[1].getSelectedItem()+"",j[2].getSelectedItem()+"",j[3].getSelectedItem()+"",j[4].getSelectedItem()+""};
       cnvTimeOut = 1000*(new Double (j[5].getSelectedItem()+"").doubleValue());

   }

/**
 *
 * @param d String sets the Deckbox in use
 */
   public static void setDeckBoxType(String d){
        deckBoxType = d;

   }
/**
 *
 * @param j JLabel sets the JLabel that will get incremented whenever
 * and MSB is detected
 */

   public void setMSBCounterLabel(JLabel j){
       this.MSBCounterLabel = j;



   }// end MSBCOunter

/**
 * Increments the MSB counter label
 */
   private void incrementMSBCounterLabel(){
       
        String m =this.MSBCounterLabel.getText();
        this.MSBCount = new Integer(m);
        MSBCount++;
        MSBCounterLabel.setText(MSBCount+"");       
   
   
   }

   /**
    * sets the MSB countdown to 15
    */
public void setNextMSBLabel(JLabel j){
    countDown = j;


}// end setNextMSBLabel




/**
 * gets call when the class is instantiated. The bulk of the work is done here
 */
   public void run(){
        //this.setPriority(1);
       BufferedReader is = null;
       FileWriter fw = null;
       BufferedWriter bw = null;
       InputStreamReader ist = null;

 double time1 = 0.0 , time2=0.0;
       String temp2 = "",tmp="",someLine="";
       //mpb = new moveProgressBar();
       //mpb.setProgressBar(theProgressBar);
       //mpb.start();
       try{



           ist = new InputStreamReader(port.getInputStream());
           //is = new BufferedReader (ist);
           path = savePath.getText()+File.separator+IESType+"_"+serialNumber+"_"+IESModel+"_"+deckBoxType+"_";
           

            fw = new FileWriter(path+"rawdata.txt",true);
           //java.io.BufferedWriter = new BufferedWriter(fw);
            bw = new BufferedWriter(fw);
           //System.out.println(savePath.getText()+File.separator+"Rawdata.txt");
           cnvData = new ConvertIncommingFreq2Data(0.0);
           cnvData.setTimer(0);
           cnvData.stopThread();
           
           while(!stopped && port!=null)
           {
               Date time = new Date();
               Thread.sleep(3);
                    
               
               if(ist!=null /*&& is!=null*/ && bw!=null && ist.ready() /*&& is.ready()*/)
               {                   
                   try{     //here I will try to read in charcters one by one until end of line is detected                  
                        
                       someChar = (char)ist.read()+"";
                       someLine+=someChar;
                       someLine = someLine.replaceAll("\n", "\r").replaceAll("\r\r", "\r");
                       bw.append(someChar.replaceAll("\r", "").replaceAll("\n", ""));
                       bw.flush();
                        //rawDataDisplay.append(someChar.replace("\r", "").replace("\n",""));
                       rawDataDisplay.setCaretPosition(rawDataDisplay.getDocument().getLength());
                       new Date().getTime();

                       if(someLine.endsWith("\r") && someLine.length() > 1) // detect end of line
                       {  
                           if(someLine.contains("12.00 "))
                            {                              
                                rawDataDisplay.append("!PING! ");
                               
                            }//end if
 
                           tmp = (new Date()).getTime() + "";                                   
                           bw.append(" " + tmp + "\n");  
                           bw.flush();  
                           rawDataDisplay.append(someLine);
                           rawDataDisplay.append(" " + tmp + "\n");  
                           rawDataDisplay.setCaretPosition(rawDataDisplay.getDocument().getLength());   
                           temp = someLine;  
                           epochTime = new Long(tmp).longValue();   
                           someChar = "";  
                           someLine="";
                           tmp = "";
                            //rawDataDisplay.setForeground(Color.black);                      
                       }//end if

                   }// end try
                    
                   catch(Exception e)
                   {

                   }

                    
                   temp = temp.replaceAll("\\p{Cntrl}", "").trim();
                    
                   if(!temp.trim().equals(""))
                   {
                       line = temp.split(" "); // tokenize the data
                       temp2 = temp;
                       temp = temp +" "+time.getTime()+"\n";

                        
                       if(deckBoxType.equals("DS-7000") && line.length ==4 && (temp2.length() == 21 || temp2.length() == 20))
                       { 
                           freqNow = line[1];
                           deckBoxNow = line[3];
                           currentFreq=new Double(freqNow).doubleValue();
                           deckBoxCounter = new Double(deckBoxNow).doubleValue(); 
                       }// end if

                       if((deckBoxType.equals("UDB-9000") || deckBoxType.equals("UTS") ) && line.length ==9)                        
                       {   
                           freqNow = line[0];
                           deckBoxNow = line[3];
                           currentFreq=new Double(freqNow).doubleValue();
                           deckBoxCounter = (new Double(deckBoxNow).doubleValue())/1000;  
                       }// end if



                        
                       if(deckBoxType.equals("SIM-7000") && line.length ==3 /*&& temp2.length() == 21 */&& line[0].contains("@RT"))
                        
                       {
                           freqNow = line[1];
                           deckBoxNow = line[2];
                           currentFreq=new Double(freqNow).doubleValue();
                           deckBoxCounter = new Double(deckBoxNow).doubleValue();
                       }// end if

//********************do stuff
                            
                                                       
                        
                       if(currentFreq==10.0)                                                   
                       {                                  
                           markerLabel.setBackground(Color.red);
                           markerLabel.setText("MSB");                      
                       }
                            
                        
                       if(currentFreq == 10.0 &&!cnvData.isAlive())                        
                       {                                                            
                           incrementMSBCounterLabel();                                
                           markerLabel.setOpaque(true);                                
                           markerLabel.setBackground(Color.red);                                
                           startDecrementMSB(60);                                
                           countDown.setText("14");                                
                           startConverter(deckBoxCounter, true);                                                    
                       }// end MSB if

                           
                        
                       if(currentFreq == 11.00 &&!cnvData.isAlive())                        
                       {                                                           
                           markerLabel.setOpaque(true);                               
                           markerLabel.setBackground(Color.green);                                
                           markerLabel.setText("LSB");                                
                           startConverter(deckBoxCounter, false);                         
                       }// end LSB if
 
                        
                       if(cnvData != null &&cnvData.isAlive()){                               
                           cnvData.sendFrequency(currentFreq, deckBoxCounter);  
                       }//end send data                            
                             
//********************
                      
                       temp="";
                       
                   }// end innner if               
               }// end if
           }//end while
           

           
           if(port!=null)  port.close();
           if (is!=null) is.close();
           if (fw!=null) fw.close();
           if (bw!=null) bw.close();
           if (ist!= null) ist.close();

       }// end try

       catch(Exception e)
       {
           e.printStackTrace();
       }//end catch
       
       finally
       {          
           try{           
               if (is!=null) is.close();           
               if (fw!=null) fw.close();           
               if (bw!=null) bw.close();           
               if(ist!=null) ist.close();           
           }          
          catch(Exception e)
          {}
       }

    }// end run





/**
 * This method starts the converter that decodes the incoming raw data
 * @param m holds the arrival time provided by the deck box
 * @param d indicates whether it's an MSB or an LSB. true for MSB , false for LSB
 */
    public  static void startConverter(double m, boolean d){
        cnvData= new ConvertIncommingFreq2Data(m);
        cnvData.setNumberOfFreq(5);
        cnvData.setFreqOrder(order);
        cnvData.setTimer((int)cnvTimeOut);
        cnvData.setDeckBoxType(deckBoxType);

        if(d) cnvData.setMSB();
        if(!d)  cnvData.setLSB();
        cnvData.setDisplayArea(convertedDataDisplay);
        cnvData.setSavePath(path);
        cnvData.setEpochTimeOfReceivedMarker(epochTime);

        cnvData.start();


    }

    /**
     * starts the timer that decrements the MSB timer every minute if it
     * is greater tha 0
     * @param seconds int the time in between decrements in seconds
     */

  public void startDecrementMSB(int seconds) {
    toolkit = Toolkit.getDefaultToolkit();

    if(timer!=null){
        timer.cancel();
        timer = null;
    }

    timer = new java.util.Timer();
/*
    if(decMSBThread == null){
        //decMSBThread = new DecrementMSB(countDown);
    }
*/
    if(decMSBThread!= null ){
        decMSBThread.stop();
        decMSBThread = null;
        //decMSBThread = new DecrementMSB(countDown);
      }
    //timer.schedule(new DecrementMSB(), seconds * 1000);

try{
    decMSBThread = new DecrementMSB(countDown);
    timer.scheduleAtFixedRate(decMSBThread, 60000, seconds*1000);
      }
catch(IllegalStateException e){
    e.printStackTrace();
}




  }// end start decrement

  public void setProgressBar(JProgressBar jpb){
      theProgressBar = jpb;
      

  }// end setProgressBar






    class DecrementMSB extends TimerTask {
JLabel labelThatHoldsTimeLeft;
        boolean alive = true;
        public DecrementMSB(JLabel j){
            labelThatHoldsTimeLeft = j;

       }// end consturctor
public void stop(){
this.cancel();
}

public boolean isAlive(){
    return alive;
}
    public void run() {

            int timeLeft = new Integer(labelThatHoldsTimeLeft.getText()).intValue();

            if (timeLeft > 0){
                timeLeft--;
                labelThatHoldsTimeLeft.setText(timeLeft+"");

            }
            if(timeLeft == 0){
                alive = false;
                this.cancel();
                
        }

      //System.out.println("One Minute has passed");
     //timer.cancel();


    }
  }



/*

    public class moveProgressBar extends Thread{

        boolean replyReceived = false;
        boolean isAlive = true;
        JProgressBar theProgressBar = null;

        public void run(){
        
            

                  

            movePB();

        }// end run

        public void setReplyReceived(boolean r){
            replyReceived = r;


        }

        public void movePB(){
            try{
                    for(int i = 0 ; i<=25 || !isAlive ; i++){
                    theProgressBar.setValue(i);
                    Thread.sleep(1);

                    }

                    for( int i = 25 ; i>=0 || !isAlive; i--){
                    theProgressBar.setValue(i);
                    Thread.sleep(1);
                        }

        replyReceived =false;
    }//end try
            
            
catch(Exception e){
    e.printStackTrace();

}


        }// end movePB

        public void setProgressBar(JProgressBar jp){
            this.theProgressBar = jp;

        }// end setProgressBar

        public void stopMoveProgressBar(){

            isAlive = false;
           

        }


    }// end class
*/
    
        public void setTimerLength(double tl){
            timerLength = tl;
        
        
        }//end setTimerLength()    
    
}// end class


