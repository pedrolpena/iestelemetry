/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package iestelemetry;

import gnu.io.SerialPort;
import java.io.PrintStream;

/**
 *
 * @author pedro
 */
public class SetTXPowerLevel extends Thread {

    /**
     * This class sets the TXPower level of the UDB and UTS deck units. This
     * class runs in the background as a thread.
     *
     * @author Pedro Pena
     */
    SerialPort port;
    String deckBox = "";
    boolean stopped = false;
    PrintStream os = null;
    int tXPowerLevel = 8;
    String tXPowerLevelCommand = "ats6=";

    //JComponent jComponents[];
    /**
     * This method is invoked when the class is instantiated and it starts the
     * class
     */
    public void run() {

        try {
            os = new PrintStream(port.getOutputStream());
            os.print("+++");
            os.print("\r");
            Thread.sleep(500);
            os.print(tXPowerLevelCommand + tXPowerLevel + "\r");
            Thread.sleep(500);
            os.print("ats15=6\r");

            if (os != null) {
                os.flush();
                os.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }// end catch

// busyButton.setEnabled(true);
    }// end run

    /**
     * Sets the serial port to be opened.
     *
     * @param sp
     */
    public void setPort(SerialPort sp) {
        port = sp;

    }// end setPort

    /**
     * sets the deck box to be used
     *
     * @param db String- name of the deck box to be used.
     */
    public void setDeckBox(String db) {
        deckBox = db;
    }// end setDeckBox

    public void stopThread() {
        stopped = true;

    }// end stopThread

    public void setTXPowerLevel(int rt) {

        tXPowerLevel = rt;
    }// end    
}
