/*This class extends thread. It converts the raw data in the background
 * ,updates the converted data display and creates two files.
 * 10.26.2011---- The deckbox is configured to send a silent ping every 99 seconds and since
 * any ping real or silent resets the counter, the counter rollover is essentially 99 seconds
 * instead of 104.857 seconds. The constant has been changed and all the calculations are based on a 99 second
 * rollover now.
 * 11.01.2011  The deck boxes now inhereit fom decckbox
 *  2.07.2012 replaced "currentTime = Calendar.getInstance().getTimeInMillis();" by "currentTime = new Date().getTime();" in ConvertIncommingFreq2Data.java because it fails on a mac for some reason. bug?????
 * 2.24.2012 added string array to store values that is later used to generate a data file in the URI format.
 * 2.242.2012 created method log(string,string) to replace log(string)
 * 1.11.2013   Program was writing time in 12 hour format when creating uri file. changed  line 258 to
 *  endOfDay.get(endOfDay.HOUR_OF_DAY)+ " " +  //endOfDay.get(endOfDay.HOUR)+ " " +
 * 1.11.2013 added methods to set and get epoch time from when the marker is received. the time stamps are now syned to the arrival epoch time of the marker signal
 * 1.16.2013 changed pressure precision from #.#### to #
 *
 * @author Pedro Pena
 */
package iestelemetry;

//import com.sun.org.apache.bcel.internal.generic.NOP;
import java.awt.Color;
import java.util.Date;
import java.io.*;
import javax.swing.*;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author pedro
 */
class ConvertIncommingFreq2Data extends Thread {

    /*
    Order of data sent by PIES/C-PIES
     * Marker      PIES & CPIES
     * Pressure    PIES & CPIES
     * Tau         PIES & CPIES
     * Year Day    PIES & CPIES
     *
     * Speed       CPIES
     * Heading     /CPIES
     */
    //Constants
    double deckBoxCounter = 104.857;
    //double deckBoxCounter      = 99.000; //using this value because the repeat interval is set to 99 seconds
    Double timeConstants[] = {-.25, -14.5, -20.75, -23.0, -26.25};
    Double MSBFactors[] = {500000.0, 1.5, 200.0, 83.333, 133.33};
    Double LSBFactors[] = {142.857, 0.08333, 200.0, 13.333, 133.33};
    String rowOfDataInURIFormat[] = {"-99", "-99.0000", "-99", "-99.0000", "-99.0000"};// this will be used to hold and display the data in URI format for comaptibility with previous processing scripts
    int currentFreq = 0;
    int numOfFreqs = 0;         // holds the number of frequencies to listen for
    boolean stopped = false;
    boolean LSBMarker = false;
    boolean MSBMarker = false;
    
    boolean startOfDaymarkerHasElapsed = false ;         // holds wheter the LSB/MSB marker time has elapsed
    boolean pressureMarkerHasElapsed = false;           // holds whether the pressure marker has elapsed
    boolean tauMarkerHasElapsed = false;                // holds whether tau marker time has elapsed
    boolean yearDayMarkerHasElapsed = false;            // holds whether the yearday market time has elapsed
    boolean speedMarkerHasElapsed = false;              // holds whether the speed marker time has elapsed
    boolean headingMarkerHasElapsed = false;          // holds whether the direction marker time has elapsed
   
    long timerLength = 0;        // holds the amount of time that the thread must stay alive
    long startTime = 0;           //holds the start time
    long currentTime = 0;          // holds the current time from timer
    long markerEpochTime = 0;   //hold the epoch time of the marker
    double deckBoxTimeElapsed = 0;
    double currentDeckBoxCounterValue = 0;
    double markerArrivalTime = 0.0;
    double transmittedValue = 0.0;
    double currentDeckBoxCount = 0.0;
    String savePath = ""; // path used to save file
    String[] freqOrder = null;      // holds the frequencies to listen for in the order they must be heard.
    Integer[] freqOrderInt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    Double[] factors = null;
    Date timer;                   // used to get the current time
    Calendar endOfDay;
    JTextArea displayArea = null;
    DecimalFormat pressureFormat = null;
    DecimalFormat tauFormat = null;
    DecimalFormat yearDayFormat = null;
    DecimalFormat speedFormat = null;
    DecimalFormat headingFormat = null;
    String deckBoxInUse = "DS-7000";

    public void run() {
        //this.setPriority(1);
        //System.out.println("cif Priority is "+this.getPriority());

        //System.out.println("I'm in the converter");
        timer = new Date();
        startTime = timer.getTime();
        currentTime = startTime;
        int freqSequenceCounter = 0;
        //pressureFormat = new DecimalFormat("#.####");
        pressureFormat = new DecimalFormat("#");
        tauFormat = new DecimalFormat("#.####");
        yearDayFormat = new DecimalFormat("#");
        speedFormat = new DecimalFormat("#.####");
        headingFormat = new DecimalFormat("###.####");
        currentDeckBoxCounterValue = getDeckBoxElapsedTime();
        
        while (!stopped && (currentTime - startTime) <= timerLength) 
        {
            try{Thread.sleep(20);}catch(Exception e){e.printStackTrace();}
           
            currentTime = Calendar.getInstance().getTimeInMillis(); //added this because "currentTime = new Date().getTime();" fails on a mac for some reason. bug?????
            //deckBoxTimeElapsed = currentTime - startTime;
            deckBoxTimeElapsed = getDeckBoxElapsedTime() - currentDeckBoxCounterValue;
            
            if((deckBoxTimeElapsed < 0 || deckBoxTimeElapsed > 30 )&& freqSequenceCounter == 0)
            {
                deckBoxTimeElapsed = 0.0;   
            }//end if
  
            
            switch(freqSequenceCounter){

                case 0:
                    
                    if(!pressureMarkerHasElapsed && deckBoxTimeElapsed >=.25 && deckBoxTimeElapsed >= 14.25){
                        appendData2Display((MSBMarker ? "\nMSB" : "\nLSB"));
                        appendData2Display("Pressure = -99.9999");
                        log("%Pressure\n" + (MSBMarker ? "1 " : "0 ") + freqSequenceCounter + "-99.9999 " + getEpochTimeOfReceivedMarker(),"converted_download_log.txt");
                        freqSequenceCounter++;
                        pressureMarkerHasElapsed = true;
                    }//endif                    
                    break;
                case 1:
                    if(!tauMarkerHasElapsed && deckBoxTimeElapsed >= 14.5 &&  deckBoxTimeElapsed >= 20.5){
                        appendData2Display("Tau = -99.9999");
                        log("%Tau\n" + (MSBMarker ? "1 " : "0 ") + freqSequenceCounter + "-99.9999 " + getEpochTimeOfReceivedMarker(),"converted_download_log.txt");
                        freqSequenceCounter++;
                        tauMarkerHasElapsed = true;
                    }//endif                       
                    break;
                case 2:
                    if(!yearDayMarkerHasElapsed && deckBoxTimeElapsed >= 20.75 && deckBoxTimeElapsed >= 22.75){
                        appendData2Display("Year Day = -99.9999");
                        log("%Year-Day\n" + (MSBMarker ? "1 " : "0 ") + freqSequenceCounter + "-99.9999 " + getEpochTimeOfReceivedMarker(),"converted_download_log.txt");
                        freqSequenceCounter++;
                        yearDayMarkerHasElapsed = true;
                        if(timerLength ==23500){
                        stopped = true;
                       
                        }
                    }//endif                     
                    break;
                case 3:
                    if(!speedMarkerHasElapsed && deckBoxTimeElapsed >= 23 && deckBoxTimeElapsed >= 26){
                        appendData2Display("Speed = -99.9999");
                        log("%Speed\n" + (MSBMarker ? "1 " : "0 ") + freqSequenceCounter + "-99.9999" + getEpochTimeOfReceivedMarker(),"converted_download_log.txt");
                         freqSequenceCounter++;
                        speedMarkerHasElapsed = true;
                    }//endif                     
                    break;  
                case 4:
                    if(!headingMarkerHasElapsed && deckBoxTimeElapsed >= 26.25 && deckBoxTimeElapsed >= 29.25){
                        appendData2Display("Heading = -99.9999");
                        log("%Heading\n" + (MSBMarker ? "1 " : "0 ") + freqSequenceCounter + "-99.9999" + getEpochTimeOfReceivedMarker(),"converted_download_log.txt");
                        headingMarkerHasElapsed = true;
                         //stopped = true;
                        
                    }//endif                     
                    break; 
                    
                /*
                case 99:
                    if(!startOfDaymarkerHasElapsed && deckBoxTimeElapsed >= 14){
                        freqSequenceCounter++;
                        startOfDaymarkerHasElapsed = true;
                    }//endif
                    break;
                    */                    
                    
            }// end switch
            
            
            
            
            if (currentFreq == freqOrderInt[freqSequenceCounter].intValue()) { // test to see if the incomming freq is the expected one

                if (MSBMarker) {
                    transmittedValue = MSBFactors[freqSequenceCounter] * (this.getDeckBoxElapsedTime() + timeConstants[freqSequenceCounter]);
                    if (freqSequenceCounter == 0) {
                         
                        log("%MSB","converted_download_log.txt");
                    }
                }
                if (LSBMarker) {
                    transmittedValue = LSBFactors[freqSequenceCounter] * (this.getDeckBoxElapsedTime() + timeConstants[freqSequenceCounter]);
                    if (freqSequenceCounter == 0) {
                        
                        log("%LSB","converted_download_log.txt");
                    }
                }
                
                
                switch (freqSequenceCounter) {
                    case 0:
                        //System.out.println("Pressure = "    + transmittedValue);
                        appendData2Display((MSBMarker ? "\nMSB" : "\nLSB"));
                        appendData2Display("Pressure = " + pressureFormat.format(transmittedValue));
                        log("%Pressure\n" + (MSBMarker ? "1 " : "0 ") + freqSequenceCounter + " " + pressureFormat.format(transmittedValue) + " " + getEpochTimeOfReceivedMarker(),"converted_download_log.txt");
                        rowOfDataInURIFormat[2] = pressureFormat.format(transmittedValue);
                        pressureMarkerHasElapsed = true;
                        break;
                    case 1:
                        //System.out.println("Tau ="          + transmittedValue);
                        appendData2Display("Tau = " + tauFormat.format(transmittedValue));
                        log("%Tau\n" + (MSBMarker ? "1 " : "0 ") + freqSequenceCounter + " " + tauFormat.format(transmittedValue) + " " + getEpochTimeOfReceivedMarker(),"converted_download_log.txt");
                        rowOfDataInURIFormat[1] = tauFormat.format(transmittedValue);
                        tauMarkerHasElapsed = true;
                        break;
                    case 2:
                        //System.out.println("Year Day ="     + transmittedValue);
                        appendData2Display("Year Day = " + yearDayFormat.format(transmittedValue));
                        log("%Year-Day\n" + (MSBMarker ? "1 " : "0 ") + freqSequenceCounter + " " + yearDayFormat.format(transmittedValue) + " " + getEpochTimeOfReceivedMarker(),"converted_download_log.txt");
                        rowOfDataInURIFormat[0] = yearDayFormat.format(transmittedValue);
                        yearDayMarkerHasElapsed = true;
                        if (timerLength == 23500) {
                            this.stopThread();
                        }
                        break;
                    case 3:
                        //System.out.println("Speed ="        + transmittedValue);
                        appendData2Display("Speed = " + speedFormat.format(transmittedValue));
                        log("%Speed\n" + (MSBMarker ? "1 " : "0 ") + freqSequenceCounter + " " + speedFormat.format(transmittedValue) + " " + getEpochTimeOfReceivedMarker(),"converted_download_log.txt");
                        rowOfDataInURIFormat[3] = speedFormat.format(transmittedValue);
                        speedMarkerHasElapsed = true;
                        break;
                    case 4:
                        //System.out.println("Heading ="      + transmittedValue);
                        appendData2Display("Heading =  " + headingFormat.format(transmittedValue));
                        log("%Heading\n" + (MSBMarker ? "1 " : "0 ") + freqSequenceCounter + " " + headingFormat.format(transmittedValue) + " " + getEpochTimeOfReceivedMarker(),"converted_download_log.txt");
                        rowOfDataInURIFormat[4] = headingFormat.format(transmittedValue);
                        headingMarkerHasElapsed = true;
                        stopped = true;
                        if (timerLength == 29500) 
                        {
                            this.stopThread();
                        }
                        break;
                        
                }//end switch


                freqSequenceCounter++;

            }//end if that checks current frequency


            //}// en if marker

        }// end while
        //endOfDay = new Date();

        endOfDay = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        //System.out.println(getEpochTimeOfReceivedMarker());
        endOfDay.setTimeInMillis(getEpochTimeOfReceivedMarker());
        String tempX = rowOfDataInURIFormat[0] + " " + 
                            rowOfDataInURIFormat[1] + " " +
                            rowOfDataInURIFormat[2] + " " +
                            rowOfDataInURIFormat[3] + " " +
                            rowOfDataInURIFormat[4] + " " + 
                            endOfDay.get(endOfDay.YEAR) + " "+ 
                            (endOfDay.get(endOfDay.MONTH )+ 1 ) + " " + 
                            endOfDay.get(endOfDay.DAY_OF_MONTH)+ " " + 
                            endOfDay.get(endOfDay.HOUR_OF_DAY)+ " " +  //endOfDay.get(endOfDay.HOUR)+ " " +
                            endOfDay.get(endOfDay.MINUTE)+ " " +
                            endOfDay.get(endOfDay.SECOND)+ " ";
        
        log(tempX,"converted_download_log_URI_Format.txt");
        
        rowOfDataInURIFormat[0] = "-99";
        rowOfDataInURIFormat[1] = "-99.0000";
        rowOfDataInURIFormat[2] = "-99";
        rowOfDataInURIFormat[3] = "-99.0000";
        rowOfDataInURIFormat[4] = "-99.0000";
        
        //System.out.println("Time Elapsed= " + (currentTime - startTime) + " milli Seconds");
        //log("\nTime Elapsed= " + (currentTime-startTime) + " secs\n");;
    }// end run

    public ConvertIncommingFreq2Data(double t) {
        markerArrivalTime = t;

    }// end constructor

    public void stopThread() {
        stopped = true;

    }//end stopThread               
 

    public void setNumberOfFreq(int num) {
        numOfFreqs = num;

    }// end setNumberOfFreq

    private double getDeckBoxElapsedTime() {
        double time = 0.0;
        time = currentDeckBoxCount - markerArrivalTime;
        if (time < 0) {
            time += deckBoxCounter;
        }

        return time;

    }// end getDeckBoxElapsedTime

    public void setFreqOrder(String[] order) {
        freqOrder = order;
        for (int i = 0; (i < numOfFreqs); i++) {
            freqOrderInt[i] = new Integer((int) (10 * (new Double(freqOrder[i]).doubleValue())) + "");
            //System.out.println(freqOrder[i]);

        }// end for


    }// end setFreqOrder

    public void setTimer(int t) {
        timerLength = t;

    }// end setTimer

    public void sendFrequency(double freq, double time) {
        this.currentFreq = (int) (10 * freq);
        this.currentDeckBoxCount = time;

    }//end sendFrequency

    public void setMSB() {
        MSBMarker = true;
        LSBMarker = false;
    }// end setMSB()

    public void setLSB() {
        LSBMarker = true;
        MSBMarker = false;
    }

    public void setDisplayArea(JTextArea j) {
        displayArea = j;


    } // end setDisplayArea

    public void setDeckBoxType(String d) {
        deckBoxInUse = d;
        if (d.equals("UDB-9000")) {
            deckBoxCounter = 65536;// I don't know the real value but this should work
        }
        if (d.equals("SIM-7000")) {
            deckBoxCounter = 65536;// I don't know the real value but this should work
        }
        if (d.equals("DS-7000")) {
            deckBoxCounter = 104.857;

        }

        //System.out.println(deckBoxInUse + " rollover counter value of " + deckBoxCounter + " will be used");
    }// end setDeckBox

    public void appendData2Display(String s) {
        //System.out.println("appending to display");
        displayArea.append(s + "\n");
        displayArea.setCaretPosition(displayArea.getDocument().getLength());
        try {
            File f = new File(savePath + "screendump.txt");
            FileWriter fw = new FileWriter(f, true);
            BufferedWriter bw = new BufferedWriter(fw);
            if (fw != null && bw != null) {
                //displayArea.append(s+"\n");
                bw.append(s + "\n");
                bw.flush();
                bw.close();
                fw.close();
            }

        }// end try
        catch (Exception e) {
            e.printStackTrace();
        }// end catch

    }

    public void setSavePath(String path) {
        savePath = path;

    }// end setSavePath

    public void setCurrentTime(long ct){
        this.currentTime = ct;
    
    }
    
    public long getCurrentTime(){
    return currentTime;
    }

    public void setEpochTimeOfReceivedMarker(long et)
    {
        markerEpochTime = et;

    }// end

        private long getEpochTimeOfReceivedMarker()
    {
        return markerEpochTime;

    }
    
   /* 
    
    private void log(String s) {
        try {
            System.out.println("Logging to file");
            File f = new File(savePath + "converted_download_log.txt");
            FileWriter fw = new FileWriter(f, true);
            BufferedWriter bw = new BufferedWriter(fw);
            if (fw != null && bw != null) {
                //displayArea.append(s+"\n");
                bw.append(s + "\n");
                bw.flush();
                bw.close();
                fw.close();
            }

        }// end try
        catch (Exception e) {
            e.printStackTrace();
        }// end catch


    }// end log
 */   
    
    
    private void log(String s,String fileName) {
        try {
            //System.out.println("Logging to file");
            File f = new File(savePath + fileName);
            FileWriter fw = new FileWriter(f, true);
            BufferedWriter bw = new BufferedWriter(fw);
            if (fw != null && bw != null) {
                //displayArea.append(s+"\n");
                bw.append(s + "\n");
                bw.flush();
                bw.close();
                fw.close();
            }

        }// end try
        catch (Exception e) {
            e.printStackTrace();
        }// end catch


    }// end log    
    
}