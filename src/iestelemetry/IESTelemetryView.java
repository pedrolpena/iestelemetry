/*@author Pedro Pena
 * The IESTelemetry class houses the gui that the user will interact with
 * Deckbox configuration and IES telemetry options can be configred and
 * viewed here.
 * 01.19.2012 Finally implemented the notes section. the user can now type notes
 * during a telemetry session that is logged to a file.
 * IESTelemetryView.java
 * 01.19.2012 noticed that the IES type was not making it's way to the ConvertIncommingFreq2Data
 * resulting in that all file names began with the default value of "CPIES"
 * The problem was corrected
 * 1.26.2012 removed 5 second sleep in ReadSerialPort.java when closing the port. it seems thats replacing
 * the rxtxserial.dll with the arduino fixed solved the original problem of freezing.
 * 1.26.2012 Automatically logs timestamp when a command is sent
 * 1.26.2012 the default deck unit is the udb-9000
 * 2.01.2011 modified the serial port close mehtod to add a mock event listener and later remove it, also added
 * a while loop to empty out any remaiing bytes left in the receive buffer. these mods seem to stop the program from hanging when closing the serial port(windows).
 * Ocassionally the JVM will crash and close the app. According to the logs, there seems to be an issue with the rxtxserial.dll
 * 2.07.2012 replaced "currentTime = Calendar.getInstance().getTimeInMillis();" by "currentTime = new Date().getTime();" in ConvertIncommingFreq2Data.java because it fails on a mac for some reason. bug?????
 * 1.11.2013   Program was writing time in 12 hour format when creating uri file. changed  line 258 in ConvertIncommingFrequency.java to
 * endOfDay.get(endOfDay.HOUR_OF_DAY)+ " " +  //endOfDay.get(endOfDay.HOUR)+ " " +
 * 1.11.2013 removed buffered reader in main loop of ReadSerialPort.java and no longer using readline to read data coming in. readline does not have a timeout feature.
 * Now data is being read one character at a time using InputStreamReader's read and appending it to a string. The line is considered read when a '\r' is detected.
 * at which time a the epoch time is added to the raw data file also fixing the duplicated timestamp problem.
 * 1.11.2013 added methods in ConvertImcommingFreq2Data.java to set and get epoch time from when the marker is received. the time stamps are now syned to the arrival epoch time of the marker signal
 * 2.16.2013 changed startup location to the user home directory so that user will always have pemission to write.
 *02/21/2013 changed continuous transponde command to ats15=6 so that 
 * box does not start in continous transponde mode
 */

package iestelemetry;

import java.awt.event.KeyEvent;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.text.*;
import gnu.io.*;
import java.awt.Color;
import java.util.*;
import java.io.*;
import java.io.BufferedWriter;
import java.io.BufferedOutputStream;
import javax.swing.*;
import java.awt.Toolkit;
import java.util.TimerTask;
import java.util.Date;
import javax.swing.UIManager.*;
/**
 * The application's main frame.
 */
public class IESTelemetryView extends FrameView {




    public IESTelemetryView(SingleFrameApplication app) {



        super(app);

   try {
    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
            UIManager.setLookAndFeel(info.getClassName());
            break;
        }
    }
} catch (Exception e) {
    // If Nimbus is not available, you can set the GUI to another look and feel.
}

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // conneting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    
    init();
    }// end constructor

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = IESTelemetryApp.getApplication().getMainFrame();
            aboutBox = new IESTelemetryAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        IESTelemetryApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        rawDataTabbedPane = new javax.swing.JTabbedPane();
        DatajPanelTab = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        convertedDatajTextArea = new javax.swing.JTextArea();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        NotesjTextArea = new javax.swing.JTextArea();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        RawDatajTextArea = new javax.swing.JTextArea();
        jPanel19 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        singlePingjButton = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        transmitCodejButton = new javax.swing.JButton();
        transmitCodejComboBox = new javax.swing.JComboBox();
        selectedURICommandjLabel = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        markerJLabel = new javax.swing.JLabel();
        configurationjPanelTab = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        FileLocationjButton = new javax.swing.JButton();
        fileLocationjTextField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        DeckUnitjComboBox = new javax.swing.JComboBox();
        jPanel5 = new javax.swing.JPanel();
        portComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        portSpeedjComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        parityjComboBox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        bitsjComboBox = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        stopBitsjComboBox = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        FlowControljComboBox = new javax.swing.JComboBox();
        connectjButton = new javax.swing.JButton();
        disconnectjButton = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        Frequency6jComboBox = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        Frequency5jComboBox = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        Frequency1jComboBox = new javax.swing.JComboBox();
        Frequency4jComboBox = new javax.swing.JComboBox();
        jLabel14 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        Frequency2jComboBox = new javax.swing.JComboBox();
        Frequency3jComboBox = new javax.swing.JComboBox();
        jLabel15 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        deckBoxConfigurejButton = new javax.swing.JButton();
        ReceiveThresholdjComboBox = new javax.swing.JComboBox();
        setGainjComboBox = new javax.swing.JComboBox();
        jLabel22 = new javax.swing.JLabel();
        setGainJButton = new javax.swing.JButton();
        receivePulseWidthjComboBox = new javax.swing.JComboBox();
        jLabel23 = new javax.swing.JLabel();
        clearDataLoggerjButton = new javax.swing.JButton();
        set100kSensitivityjComboBox = new javax.swing.JComboBox();
        set105kSensitivityjComboBox = new javax.swing.JComboBox();
        set110kSensitivityjComboBox = new javax.swing.JComboBox();
        set115kSensitivityjComboBox = new javax.swing.JComboBox();
        set120kSensitivityjComboBox = new javax.swing.JComboBox();
        set125kSensitivityjComboBox = new javax.swing.JComboBox();
        set100kSensitivityjButton = new javax.swing.JButton();
        set105kSensitivityjButton = new javax.swing.JButton();
        set110kSensitivityjButton = new javax.swing.JButton();
        set115kSensitivityjButton = new javax.swing.JButton();
        set120kSensitivityjButton = new javax.swing.JButton();
        set130kSensitivityjComboBox = new javax.swing.JComboBox();
        set125kSensitivityjButton = new javax.swing.JButton();
        set130kSensitivityjButton = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        IESTypejComboBox = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        IESModeljComboBox = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        FirmwarejComboBox = new javax.swing.JComboBox();
        jLabel9 = new javax.swing.JLabel();
        SerialNumberjTextField = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        PlotjPanelTab = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar();
        jLabel20 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        MSBTimerjLabel = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        MSBCountjLabel = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        statusPanel = new javax.swing.JPanel();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem1 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItem5 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItem6 = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jMenuItem4 = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jMenuItem3 = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        jTabbedPane1 = new javax.swing.JTabbedPane();

        mainPanel.setMaximumSize(new java.awt.Dimension(735, 614));
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new java.awt.Dimension(760, 614));
        mainPanel.setRequestFocusEnabled(false);

        rawDataTabbedPane.setMinimumSize(new java.awt.Dimension(0, 0));
        rawDataTabbedPane.setName("rawDataTabbedPane"); // NOI18N
        rawDataTabbedPane.setPreferredSize(new java.awt.Dimension(750, 550));
        rawDataTabbedPane.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                rawDataTabbedPaneKeyPressed(evt);
            }
        });

        DatajPanelTab.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        DatajPanelTab.setName("DatajPanelTab"); // NOI18N

        jPanel14.setName("jPanel14"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(iestelemetry.IESTelemetryApp.class).getContext().getResourceMap(IESTelemetryView.class);
        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel7.border.title"))); // NOI18N
        jPanel7.setName("jPanel7"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        convertedDatajTextArea.setColumns(20);
        convertedDatajTextArea.setEditable(false);
        convertedDatajTextArea.setRows(5);
        convertedDatajTextArea.setName("convertedDatajTextArea"); // NOI18N
        jScrollPane2.setViewportView(convertedDatajTextArea);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel9.border.title"))); // NOI18N
        jPanel9.setName("jPanel9"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        NotesjTextArea.setColumns(20);
        NotesjTextArea.setRows(5);
        NotesjTextArea.setName("NotesjTextArea"); // NOI18N
        NotesjTextArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NotesjTextAreaKeyPressed(evt);
            }
        });
        jScrollPane3.setViewportView(NotesjTextArea);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel6.border.title"))); // NOI18N
        jPanel6.setName("jPanel6"); // NOI18N

        jScrollPane4.setAutoscrolls(true);
        jScrollPane4.setName("jScrollPane4"); // NOI18N

        RawDatajTextArea.setColumns(20);
        RawDatajTextArea.setEditable(false);
        RawDatajTextArea.setRows(5);
        RawDatajTextArea.setName("RawDatajTextArea"); // NOI18N
        jScrollPane4.setViewportView(RawDatajTextArea);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 435, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel14Layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel19.setName("jPanel19"); // NOI18N

        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel16.border.title"))); // NOI18N
        jPanel16.setName("jPanel16"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(iestelemetry.IESTelemetryApp.class).getContext().getActionMap(IESTelemetryView.class, this);
        singlePingjButton.setAction(actionMap.get("sendSinglePing")); // NOI18N
        singlePingjButton.setText(resourceMap.getString("singlePingjButton.text")); // NOI18N
        singlePingjButton.setName("singlePingjButton"); // NOI18N

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(singlePingjButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addComponent(singlePingjButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel10.border.title"))); // NOI18N
        jPanel10.setName("jPanel10"); // NOI18N

        transmitCodejButton.setAction(actionMap.get("sendURICode")); // NOI18N
        transmitCodejButton.setText(resourceMap.getString("transmitCodejButton.text")); // NOI18N
        transmitCodejButton.setName("transmitCodejButton"); // NOI18N

        transmitCodejComboBox.setAction(actionMap.get("selectURICommand")); // NOI18N
        transmitCodejComboBox.setName("transmitCodejComboBox"); // NOI18N
        transmitCodejComboBox.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                transmitCodejComboBoxKeyPressed(evt);
            }
        });

        selectedURICommandjLabel.setFont(resourceMap.getFont("selectedURICommandjLabel.font")); // NOI18N
        selectedURICommandjLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        selectedURICommandjLabel.setText(resourceMap.getString("selectedURICommandjLabel.text")); // NOI18N
        selectedURICommandjLabel.setName("selectedURICommandjLabel"); // NOI18N

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(transmitCodejButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(transmitCodejComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(selectedURICommandjLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(transmitCodejButton)
                .addComponent(transmitCodejComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(selectedURICommandjLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel18.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel18.border.title"))); // NOI18N
        jPanel18.setName("jPanel18"); // NOI18N

        markerJLabel.setFont(resourceMap.getFont("markerJLabel.font")); // NOI18N
        markerJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        markerJLabel.setText(resourceMap.getString("markerJLabel.text")); // NOI18N
        markerJLabel.setName("markerJLabel"); // NOI18N

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(markerJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addComponent(markerJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel19Layout.createSequentialGroup()
                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(28, 28, 28)
                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(60, 60, 60)
                .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel18, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel16, javax.swing.GroupLayout.Alignment.LEADING, 0, 58, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout DatajPanelTabLayout = new javax.swing.GroupLayout(DatajPanelTab);
        DatajPanelTab.setLayout(DatajPanelTabLayout);
        DatajPanelTabLayout.setHorizontalGroup(
            DatajPanelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, DatajPanelTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(DatajPanelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel19, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel14, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        DatajPanelTabLayout.setVerticalGroup(
            DatajPanelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DatajPanelTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        rawDataTabbedPane.addTab(resourceMap.getString("DatajPanelTab.TabConstraints.tabTitle"), DatajPanelTab); // NOI18N

        configurationjPanelTab.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        configurationjPanelTab.setName("configurationjPanelTab"); // NOI18N
        configurationjPanelTab.setPreferredSize(new java.awt.Dimension(770, 530));

        jPanel17.setName("jPanel17"); // NOI18N

        jPanel11.setName("jPanel11"); // NOI18N

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel4.border.title"))); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N

        FileLocationjButton.setAction(actionMap.get("openFileChooser")); // NOI18N
        FileLocationjButton.setText(resourceMap.getString("FileLocationjButton.text")); // NOI18N
        FileLocationjButton.setName("FileLocationjButton"); // NOI18N

        fileLocationjTextField.setEditable(false);
        fileLocationjTextField.setText(resourceMap.getString("fileLocationjTextField.text")); // NOI18N
        fileLocationjTextField.setName("fileLocationjTextField"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(FileLocationjButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(fileLocationjTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(FileLocationjButton)
                    .addComponent(fileLocationjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        DeckUnitjComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "DS-7000", "UDB-9000", "SIM-7000" }));
        DeckUnitjComboBox.setSelectedIndex(1);
        DeckUnitjComboBox.setAction(actionMap.get("setDeckBoxType")); // NOI18N
        DeckUnitjComboBox.setName("DeckUnitjComboBox"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(DeckUnitjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(67, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(DeckUnitjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel5.border.title"))); // NOI18N
        jPanel5.setName("jPanel5"); // NOI18N

        portComboBox.setName("portComboBox"); // NOI18N

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        portSpeedjComboBox.setMaximumRowCount(10);
        portSpeedjComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "300", "600", "1200", "2400", "4800", "9600", "19200", "38400", "57600", "115200" }));
        portSpeedjComboBox.setName("portSpeedjComboBox"); // NOI18N

        jLabel3.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        parityjComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "none", "even", "odd" }));
        parityjComboBox.setName("parityjComboBox"); // NOI18N

        jLabel4.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        bitsjComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "8", "7", "6", "5" }));
        bitsjComboBox.setName("bitsjComboBox"); // NOI18N

        jLabel5.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        stopBitsjComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2" }));
        stopBitsjComboBox.setName("stopBitsjComboBox"); // NOI18N

        jLabel6.setFont(resourceMap.getFont("jLabel2.font")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        FlowControljComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "none", "RTS/CTS", "Xon/Xoff" }));
        FlowControljComboBox.setName("FlowControljComboBox"); // NOI18N

        connectjButton.setAction(actionMap.get("openPort")); // NOI18N
        connectjButton.setText(resourceMap.getString("connectjButton.text")); // NOI18N
        connectjButton.setName("connectjButton"); // NOI18N
        connectjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectjButtonActionPerformed(evt);
            }
        });

        disconnectjButton.setAction(actionMap.get("closePort")); // NOI18N
        disconnectjButton.setText(resourceMap.getString("disconnectjButton.text")); // NOI18N
        disconnectjButton.setName("disconnectjButton"); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(stopBitsjComboBox, 0, 142, Short.MAX_VALUE)
                            .addComponent(portSpeedjComboBox, 0, 142, Short.MAX_VALUE)
                            .addComponent(parityjComboBox, 0, 142, Short.MAX_VALUE)
                            .addComponent(bitsjComboBox, 0, 142, Short.MAX_VALUE)
                            .addComponent(FlowControljComboBox, 0, 142, Short.MAX_VALUE)
                            .addComponent(portComboBox, 0, 142, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel6)
                            .addComponent(jLabel5)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(connectjButton, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(disconnectjButton)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(portComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portSpeedjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(parityjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bitsjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(stopBitsjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(FlowControljComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(connectjButton)
                    .addComponent(disconnectjButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel12.setName("jPanel12"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel1.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel1.border.titleFont"))); // NOI18N
        jPanel1.setName("jPanel1"); // NOI18N

        jPanel13.setName("jPanel13"); // NOI18N

        Frequency6jComboBox.setFont(resourceMap.getFont("Frequency1jComboBox.font")); // NOI18N
        Frequency6jComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "23.5", "29.5", "22.5", "22.0", "21.5", "21.0", "20.5", "20.0", "19.5", "19.0" }));
        Frequency6jComboBox.setSelectedIndex(1);
        Frequency6jComboBox.setAction(actionMap.get("setFreqsAndPeriod")); // NOI18N
        Frequency6jComboBox.setMaximumSize(new java.awt.Dimension(70, 25));
        Frequency6jComboBox.setMinimumSize(new java.awt.Dimension(70, 25));
        Frequency6jComboBox.setName("Frequency6jComboBox"); // NOI18N
        Frequency6jComboBox.setPreferredSize(new java.awt.Dimension(70, 25));

        jLabel16.setFont(resourceMap.getFont("jLabel12.font")); // NOI18N
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel16.setText(resourceMap.getString("jLabel16.text")); // NOI18N
        jLabel16.setName("jLabel16"); // NOI18N

        Frequency5jComboBox.setFont(resourceMap.getFont("Frequency1jComboBox.font")); // NOI18N
        Frequency5jComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10.5", "11.5", "12.0", "12.5", "13.0" }));
        Frequency5jComboBox.setSelectedIndex(4);
        Frequency5jComboBox.setAction(actionMap.get("setFreqsAndPeriod")); // NOI18N
        Frequency5jComboBox.setMaximumSize(new java.awt.Dimension(70, 25));
        Frequency5jComboBox.setMinimumSize(new java.awt.Dimension(70, 25));
        Frequency5jComboBox.setName("Frequency5jComboBox"); // NOI18N
        Frequency5jComboBox.setPreferredSize(new java.awt.Dimension(70, 25));

        jLabel12.setFont(resourceMap.getFont("jLabel12.font")); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        Frequency1jComboBox.setFont(resourceMap.getFont("Frequency1jComboBox.font")); // NOI18N
        Frequency1jComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10.5", "11.5", "12.0", "12.5", "13.0" }));
        Frequency1jComboBox.setAction(actionMap.get("setFreqsAndPeriod")); // NOI18N
        Frequency1jComboBox.setMaximumSize(new java.awt.Dimension(70, 25));
        Frequency1jComboBox.setMinimumSize(new java.awt.Dimension(70, 25));
        Frequency1jComboBox.setName("Frequency1jComboBox"); // NOI18N
        Frequency1jComboBox.setPreferredSize(new java.awt.Dimension(70, 25));

        Frequency4jComboBox.setFont(resourceMap.getFont("Frequency1jComboBox.font")); // NOI18N
        Frequency4jComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10.5", "11.5", "12.0", "12.5", "13.0" }));
        Frequency4jComboBox.setSelectedIndex(3);
        Frequency4jComboBox.setAction(actionMap.get("setFreqsAndPeriod")); // NOI18N
        Frequency4jComboBox.setMaximumSize(new java.awt.Dimension(70, 25));
        Frequency4jComboBox.setMinimumSize(new java.awt.Dimension(70, 25));
        Frequency4jComboBox.setName("Frequency4jComboBox"); // NOI18N
        Frequency4jComboBox.setPreferredSize(new java.awt.Dimension(70, 25));

        jLabel14.setFont(resourceMap.getFont("jLabel12.font")); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setText(resourceMap.getString("jLabel14.text")); // NOI18N
        jLabel14.setName("jLabel14"); // NOI18N

        jLabel17.setFont(resourceMap.getFont("jLabel12.font")); // NOI18N
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel17.setText(resourceMap.getString("jLabel17.text")); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N

        Frequency2jComboBox.setFont(resourceMap.getFont("Frequency1jComboBox.font")); // NOI18N
        Frequency2jComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10.5", "11.5", "12.0", "12.5", "13.0" }));
        Frequency2jComboBox.setSelectedIndex(1);
        Frequency2jComboBox.setAction(actionMap.get("setFreqsAndPeriod")); // NOI18N
        Frequency2jComboBox.setMaximumSize(new java.awt.Dimension(70, 25));
        Frequency2jComboBox.setMinimumSize(new java.awt.Dimension(70, 25));
        Frequency2jComboBox.setName("Frequency2jComboBox"); // NOI18N
        Frequency2jComboBox.setPreferredSize(new java.awt.Dimension(70, 25));

        Frequency3jComboBox.setFont(resourceMap.getFont("Frequency1jComboBox.font")); // NOI18N
        Frequency3jComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "10.5", "11.5", "12.0", "12.5", "13.0" }));
        Frequency3jComboBox.setSelectedIndex(2);
        Frequency3jComboBox.setAction(actionMap.get("setFreqsAndPeriod")); // NOI18N
        Frequency3jComboBox.setMaximumSize(new java.awt.Dimension(70, 25));
        Frequency3jComboBox.setMinimumSize(new java.awt.Dimension(70, 25));
        Frequency3jComboBox.setName("Frequency3jComboBox"); // NOI18N
        Frequency3jComboBox.setPreferredSize(new java.awt.Dimension(70, 25));

        jLabel15.setFont(resourceMap.getFont("jLabel12.font")); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText(resourceMap.getString("jLabel15.text")); // NOI18N
        jLabel15.setName("jLabel15"); // NOI18N

        jLabel13.setFont(resourceMap.getFont("jLabel12.font")); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel13.setText(resourceMap.getString("jLabel13.text")); // NOI18N
        jLabel13.setName("jLabel13"); // NOI18N

        jButton1.setAction(actionMap.get("setFreqsAndPeriod")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setMaximumSize(new java.awt.Dimension(70, 25));
        jButton1.setMinimumSize(new java.awt.Dimension(70, 25));
        jButton1.setName("jButton1"); // NOI18N
        jButton1.setPreferredSize(new java.awt.Dimension(70, 25));

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel14)
                    .addComponent(jLabel13)
                    .addComponent(jLabel12))
                .addGap(4, 4, 4)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(Frequency3jComboBox, 0, 1, Short.MAX_VALUE)
                            .addComponent(Frequency2jComboBox, 0, 1, Short.MAX_VALUE)
                            .addComponent(Frequency1jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel15)
                            .addComponent(jLabel16)
                            .addComponent(jLabel17))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Frequency6jComboBox, 0, 1, Short.MAX_VALUE)
                            .addComponent(Frequency5jComboBox, 0, 0, Short.MAX_VALUE)
                            .addComponent(Frequency4jComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Frequency1jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12))
                        .addGap(3, 3, 3)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Frequency2jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Frequency3jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14)))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Frequency4jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15))
                        .addGap(3, 3, 3)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Frequency5jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Frequency6jComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(65, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, resourceMap.getString("jPanel3.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, resourceMap.getFont("jPanel3.border.titleFont"))); // NOI18N
        jPanel3.setName("jPanel3"); // NOI18N

        deckBoxConfigurejButton.setAction(actionMap.get("configureTheDeckBox")); // NOI18N
        deckBoxConfigurejButton.setText(resourceMap.getString("deckBoxConfigurejButton.text")); // NOI18N
        deckBoxConfigurejButton.setName("deckBoxConfigurejButton"); // NOI18N

        ReceiveThresholdjComboBox.setFont(resourceMap.getFont("setGainjComboBox.font")); // NOI18N
        ReceiveThresholdjComboBox.setName("ReceiveThresholdjComboBox"); // NOI18N

        setGainjComboBox.setFont(resourceMap.getFont("setGainjComboBox.font")); // NOI18N
        setGainjComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
        setGainjComboBox.setName("setGainjComboBox"); // NOI18N

        jLabel22.setFont(resourceMap.getFont("jLabel22.font")); // NOI18N
        jLabel22.setText(resourceMap.getString("jLabel22.text")); // NOI18N
        jLabel22.setName("jLabel22"); // NOI18N

        setGainJButton.setAction(actionMap.get("setRXThreshold")); // NOI18N
        setGainJButton.setFont(resourceMap.getFont("setGainJButton.font")); // NOI18N
        setGainJButton.setText(resourceMap.getString("setGainJButton.text")); // NOI18N
        setGainJButton.setName("setGainJButton"); // NOI18N

        receivePulseWidthjComboBox.setFont(resourceMap.getFont("setGainjComboBox.font")); // NOI18N
        receivePulseWidthjComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }));
        receivePulseWidthjComboBox.setSelectedIndex(7);
        receivePulseWidthjComboBox.setName("receivePulseWidthjComboBox"); // NOI18N

        jLabel23.setFont(resourceMap.getFont("jLabel22.font")); // NOI18N
        jLabel23.setText(resourceMap.getString("jLabel23.text")); // NOI18N
        jLabel23.setName("jLabel23"); // NOI18N

        clearDataLoggerjButton.setAction(actionMap.get("clearDataLogger")); // NOI18N
        clearDataLoggerjButton.setFont(resourceMap.getFont("clearDataLoggerjButton.font")); // NOI18N
        clearDataLoggerjButton.setText(resourceMap.getString("clearDataLoggerjButton.text")); // NOI18N
        clearDataLoggerjButton.setName("clearDataLoggerjButton"); // NOI18N

        set100kSensitivityjComboBox.setFont(resourceMap.getFont("setGainjComboBox.font")); // NOI18N
        set100kSensitivityjComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-1.0", "-0.9", "-0.8", "-0.7", "-0.6", "-0.5", "-0.4", "-0.3", "-0.2", "-0.1", "0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0" }));
        set100kSensitivityjComboBox.setSelectedIndex(10);
        set100kSensitivityjComboBox.setName("set100kSensitivityjComboBox"); // NOI18N

        set105kSensitivityjComboBox.setFont(resourceMap.getFont("setGainjComboBox.font")); // NOI18N
        set105kSensitivityjComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-1.0", "-0.9", "-0.8", "-0.7", "-0.6", "-0.5", "-0.4", "-0.3", "-0.2", "-0.1", "0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0" }));
        set105kSensitivityjComboBox.setSelectedIndex(10);
        set105kSensitivityjComboBox.setName("set105kSensitivityjComboBox"); // NOI18N

        set110kSensitivityjComboBox.setFont(resourceMap.getFont("setGainjComboBox.font")); // NOI18N
        set110kSensitivityjComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-1.0", "-0.9", "-0.8", "-0.7", "-0.6", "-0.5", "-0.4", "-0.3", "-0.2", "-0.1", "0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0" }));
        set110kSensitivityjComboBox.setSelectedIndex(10);
        set110kSensitivityjComboBox.setName("set110kSensitivityjComboBox"); // NOI18N

        set115kSensitivityjComboBox.setFont(resourceMap.getFont("setGainjComboBox.font")); // NOI18N
        set115kSensitivityjComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-1.0", "-0.9", "-0.8", "-0.7", "-0.6", "-0.5", "-0.4", "-0.3", "-0.2", "-0.1", "0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0" }));
        set115kSensitivityjComboBox.setSelectedIndex(10);
        set115kSensitivityjComboBox.setName("set115kSensitivityjComboBox"); // NOI18N

        set120kSensitivityjComboBox.setFont(resourceMap.getFont("setGainjComboBox.font")); // NOI18N
        set120kSensitivityjComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-1.0", "-0.9", "-0.8", "-0.7", "-0.6", "-0.5", "-0.4", "-0.3", "-0.2", "-0.1", "0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0" }));
        set120kSensitivityjComboBox.setSelectedIndex(10);
        set120kSensitivityjComboBox.setName("set120kSensitivityjComboBox"); // NOI18N

        set125kSensitivityjComboBox.setFont(resourceMap.getFont("setGainjComboBox.font")); // NOI18N
        set125kSensitivityjComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-1.0", "-0.9", "-0.8", "-0.7", "-0.6", "-0.5", "-0.4", "-0.3", "-0.2", "-0.1", "0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0" }));
        set125kSensitivityjComboBox.setSelectedIndex(10);
        set125kSensitivityjComboBox.setName("set125kSensitivityjComboBox"); // NOI18N

        set100kSensitivityjButton.setAction(actionMap.get("set100kSensitivity")); // NOI18N
        set100kSensitivityjButton.setFont(resourceMap.getFont("set100kSensitivityjButton.font")); // NOI18N
        set100kSensitivityjButton.setText(resourceMap.getString("set100kSensitivityjButton.text")); // NOI18N
        set100kSensitivityjButton.setName("set100kSensitivityjButton"); // NOI18N

        set105kSensitivityjButton.setAction(actionMap.get("set105kSensitivity")); // NOI18N
        set105kSensitivityjButton.setFont(resourceMap.getFont("set105kSensitivityjButton.font")); // NOI18N
        set105kSensitivityjButton.setText(resourceMap.getString("set105kSensitivityjButton.text")); // NOI18N
        set105kSensitivityjButton.setName("set105kSensitivityjButton"); // NOI18N

        set110kSensitivityjButton.setAction(actionMap.get("set110kSensitivity")); // NOI18N
        set110kSensitivityjButton.setFont(resourceMap.getFont("set110kSensitivityjButton.font")); // NOI18N
        set110kSensitivityjButton.setText(resourceMap.getString("set110kSensitivityjButton.text")); // NOI18N
        set110kSensitivityjButton.setName("set110kSensitivityjButton"); // NOI18N

        set115kSensitivityjButton.setAction(actionMap.get("set115kSensitivity")); // NOI18N
        set115kSensitivityjButton.setFont(resourceMap.getFont("set115kSensitivityjButton.font")); // NOI18N
        set115kSensitivityjButton.setText(resourceMap.getString("set115kSensitivityjButton.text")); // NOI18N
        set115kSensitivityjButton.setName("set115kSensitivityjButton"); // NOI18N

        set120kSensitivityjButton.setAction(actionMap.get("set120kSensitivity")); // NOI18N
        set120kSensitivityjButton.setFont(resourceMap.getFont("set120kSensitivityjButton.font")); // NOI18N
        set120kSensitivityjButton.setText(resourceMap.getString("set120kSensitivityjButton.text")); // NOI18N
        set120kSensitivityjButton.setName("set120kSensitivityjButton"); // NOI18N

        set130kSensitivityjComboBox.setFont(resourceMap.getFont("setGainjComboBox.font")); // NOI18N
        set130kSensitivityjComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "-1.0", "-0.9", "-0.8", "-0.7", "-0.6", "-0.5", "-0.4", "-0.3", "-0.2", "-0.1", "0.0", "0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0" }));
        set130kSensitivityjComboBox.setSelectedIndex(10);
        set130kSensitivityjComboBox.setName("set130kSensitivityjComboBox"); // NOI18N

        set125kSensitivityjButton.setAction(actionMap.get("set125kSensitivity")); // NOI18N
        set125kSensitivityjButton.setFont(resourceMap.getFont("set125kSensitivityjButton.font")); // NOI18N
        set125kSensitivityjButton.setText(resourceMap.getString("set125kSensitivityjButton.text")); // NOI18N
        set125kSensitivityjButton.setName("set125kSensitivityjButton"); // NOI18N

        set130kSensitivityjButton.setAction(actionMap.get("set130kSensitivity")); // NOI18N
        set130kSensitivityjButton.setFont(resourceMap.getFont("set130kSensitivityjButton.font")); // NOI18N
        set130kSensitivityjButton.setText(resourceMap.getString("set130kSensitivityjButton.text")); // NOI18N
        set130kSensitivityjButton.setName("set130kSensitivityjButton"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(clearDataLoggerjButton, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                    .addComponent(deckBoxConfigurejButton, javax.swing.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ReceiveThresholdjComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, 57, Short.MAX_VALUE)
                            .addComponent(receivePulseWidthjComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, 57, Short.MAX_VALUE)
                            .addComponent(setGainjComboBox, 0, 57, Short.MAX_VALUE)
                            .addComponent(set130kSensitivityjComboBox, 0, 1, Short.MAX_VALUE)
                            .addComponent(set120kSensitivityjComboBox, 0, 57, Short.MAX_VALUE)
                            .addComponent(set115kSensitivityjComboBox, 0, 57, Short.MAX_VALUE)
                            .addComponent(set110kSensitivityjComboBox, 0, 57, Short.MAX_VALUE)
                            .addComponent(set105kSensitivityjComboBox, 0, 57, Short.MAX_VALUE)
                            .addComponent(set125kSensitivityjComboBox, 0, 57, Short.MAX_VALUE)
                            .addComponent(set100kSensitivityjComboBox, 0, 57, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(set130kSensitivityjButton, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                            .addComponent(setGainJButton, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                            .addComponent(set125kSensitivityjButton, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                            .addComponent(set120kSensitivityjButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                            .addComponent(set115kSensitivityjButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                            .addComponent(jLabel23)
                            .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(set110kSensitivityjButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                            .addComponent(set105kSensitivityjButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                            .addComponent(set100kSensitivityjButton, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE))))
                .addGap(12, 12, 12))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(setGainjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(receivePulseWidthjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ReceiveThresholdjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(setGainJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(set100kSensitivityjButton, 0, 0, Short.MAX_VALUE)
                        .addGap(7, 7, 7))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(set100kSensitivityjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(set105kSensitivityjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(set105kSensitivityjButton, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(set110kSensitivityjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(set110kSensitivityjButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(set115kSensitivityjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(set115kSensitivityjButton, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(set120kSensitivityjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(set120kSensitivityjButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(set125kSensitivityjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(set125kSensitivityjButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(set130kSensitivityjComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(set130kSensitivityjButton, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clearDataLoggerjButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deckBoxConfigurejButton, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel8.border.title"))); // NOI18N
        jPanel8.setName("jPanel8"); // NOI18N

        IESTypejComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "PIES", "C-PIES" }));
        IESTypejComboBox.setSelectedIndex(1);
        IESTypejComboBox.setAction(actionMap.get("setGuiPeriod")); // NOI18N
        IESTypejComboBox.setName("IESTypejComboBox"); // NOI18N

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        IESModeljComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "6.1C", "6.1E", "6.2", "6.2A", "6.2B" }));
        IESModeljComboBox.setSelectedIndex(2);
        IESModeljComboBox.setAction(actionMap.get("setGuiModelNumber")); // NOI18N
        IESModeljComboBox.setName("IESModeljComboBox"); // NOI18N

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        FirmwarejComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "A", "B", "C" }));
        FirmwarejComboBox.setSelectedIndex(1);
        FirmwarejComboBox.setAction(actionMap.get("setGuiFreqs")); // NOI18N
        FirmwarejComboBox.setName("FirmwarejComboBox"); // NOI18N

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        SerialNumberjTextField.setText(resourceMap.getString("SerialNumberjTextField.text")); // NOI18N
        SerialNumberjTextField.setName("SerialNumberjTextField"); // NOI18N

        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(IESModeljComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(FirmwarejComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(SerialNumberjTextField)
                    .addComponent(IESTypejComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel8)
                    .addComponent(jLabel7)
                    .addComponent(jLabel10))
                .addGap(54, 54, 54))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(IESTypejComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(12, 12, 12)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(IESModeljComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(FirmwarejComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addGap(7, 7, 7)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SerialNumberjTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel11, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(68, 68, 68))
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        javax.swing.GroupLayout configurationjPanelTabLayout = new javax.swing.GroupLayout(configurationjPanelTab);
        configurationjPanelTab.setLayout(configurationjPanelTabLayout);
        configurationjPanelTabLayout.setHorizontalGroup(
            configurationjPanelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configurationjPanelTabLayout.createSequentialGroup()
                .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        configurationjPanelTabLayout.setVerticalGroup(
            configurationjPanelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configurationjPanelTabLayout.createSequentialGroup()
                .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        rawDataTabbedPane.addTab(resourceMap.getString("configurationjPanelTab.TabConstraints.tabTitle"), configurationjPanelTab); // NOI18N

        PlotjPanelTab.setName("PlotjPanelTab"); // NOI18N

        javax.swing.GroupLayout PlotjPanelTabLayout = new javax.swing.GroupLayout(PlotjPanelTab);
        PlotjPanelTab.setLayout(PlotjPanelTabLayout);
        PlotjPanelTabLayout.setHorizontalGroup(
            PlotjPanelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 855, Short.MAX_VALUE)
        );
        PlotjPanelTabLayout.setVerticalGroup(
            PlotjPanelTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 515, Short.MAX_VALUE)
        );

        rawDataTabbedPane.addTab(resourceMap.getString("PlotjPanelTab.TabConstraints.tabTitle"), PlotjPanelTab); // NOI18N

        jPanel15.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel15.setMaximumSize(new java.awt.Dimension(698, 662));
        jPanel15.setName("jPanel15"); // NOI18N
        jPanel15.setPreferredSize(new java.awt.Dimension(698, 662));

        progressBar.setName("progressBar"); // NOI18N

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel20.setText(resourceMap.getString("jLabel20.text")); // NOI18N
        jLabel20.setName("jLabel20"); // NOI18N

        jLabel19.setText(resourceMap.getString("jLabel19.text")); // NOI18N
        jLabel19.setName("jLabel19"); // NOI18N

        MSBTimerjLabel.setText(resourceMap.getString("MSBTimerjLabel.text")); // NOI18N
        MSBTimerjLabel.setName("MSBTimerjLabel"); // NOI18N

        jLabel18.setText(resourceMap.getString("jLabel18.text")); // NOI18N
        jLabel18.setName("jLabel18"); // NOI18N

        MSBCountjLabel.setText(resourceMap.getString("MSBCountjLabel.text")); // NOI18N
        MSBCountjLabel.setName("MSBCountjLabel"); // NOI18N

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(MSBCountjLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(MSBTimerjLabel)
                .addGap(47, 47, 47)
                .addComponent(jLabel19)
                .addGap(76, 76, 76))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel18)
                        .addComponent(MSBCountjLabel)
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel20)
                            .addComponent(jLabel19)
                            .addComponent(MSBTimerjLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(5, 5, 5)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        statusPanel.setName("statusPanel"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(statusMessageLabel)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 867, Short.MAX_VALUE)
                        .addComponent(statusAnimationLabel)
                        .addContainerGap())))
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(statusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, 867, Short.MAX_VALUE)
                    .addComponent(rawDataTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 867, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rawDataTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 559, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(117, 117, 117)
                .addComponent(statusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        jMenuItem2.setAction(actionMap.get("telemetryHelp")); // NOI18N
        jMenuItem2.setText(resourceMap.getString("jMenuItem2.text")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        helpMenu.add(jMenuItem2);

        jSeparator1.setName("jSeparator1"); // NOI18N
        helpMenu.add(jSeparator1);

        jMenuItem1.setAction(actionMap.get("launchIESManual")); // NOI18N
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        helpMenu.add(jMenuItem1);

        jSeparator2.setName("jSeparator2"); // NOI18N
        helpMenu.add(jSeparator2);

        jMenuItem5.setAction(actionMap.get("launchUDBManual")); // NOI18N
        jMenuItem5.setText(resourceMap.getString("jMenuItem5.text")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        helpMenu.add(jMenuItem5);

        jSeparator3.setName("jSeparator3"); // NOI18N
        helpMenu.add(jSeparator3);

        jMenuItem6.setAction(actionMap.get("launchDS7000Manual")); // NOI18N
        jMenuItem6.setText(resourceMap.getString("jMenuItem6.text")); // NOI18N
        jMenuItem6.setName("jMenuItem6"); // NOI18N
        helpMenu.add(jMenuItem6);

        jSeparator4.setName("jSeparator4"); // NOI18N
        helpMenu.add(jSeparator4);

        jMenuItem4.setAction(actionMap.get("launchATM900SeriesManual")); // NOI18N
        jMenuItem4.setText(resourceMap.getString("jMenuItem4.text")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        helpMenu.add(jMenuItem4);

        jSeparator5.setName("jSeparator5"); // NOI18N
        helpMenu.add(jSeparator5);

        jMenuItem3.setAction(actionMap.get("launchATM90xAddendum")); // NOI18N
        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        helpMenu.add(jMenuItem3);

        jSeparator6.setName("jSeparator6"); // NOI18N
        helpMenu.add(jSeparator6);

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void rawDataTabbedPaneKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_rawDataTabbedPaneKeyPressed

        switch(evt.getKeyCode()){
            case KeyEvent.VK_G:
            if(evt.isControlDown()){
                NotesjTextArea.setText("g has been pressed");
                logNotes("Guarded command sent");
            }
            break;
        }// end switch
    }//GEN-LAST:event_rawDataTabbedPaneKeyPressed

    private void connectjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectjButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_connectjButtonActionPerformed

    private void transmitCodejComboBoxKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_transmitCodejComboBoxKeyPressed
        switch(evt.getKeyCode()){
            case KeyEvent.VK_G:
            if(evt.isControlDown()){
                NotesjTextArea.setText("Sending guarded commands has not yet been implemented");

            }
            break;
        }// end switch
    }//GEN-LAST:event_transmitCodejComboBoxKeyPressed

    private void NotesjTextAreaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NotesjTextAreaKeyPressed
        // TODO add your handling code here:
        String notes = "",lastLine="",temp="";
        String[] lines;
        switch(evt.getKeyCode()){

            case KeyEvent.VK_ENTER:
            NotesjTextArea.append("\r");
            notes = NotesjTextArea.getText();
            lines = notes.split("\n");

            if(lines.length > 0){
                lastLine = lines[lines.length-1];

                logNotes(lastLine+"\n\r");
            }// end if
            break;

            case KeyEvent.VK_D:
            if(evt.isControlDown()){
                NotesjTextArea.append(new Date().toGMTString()+" ");
            }

            break;
            case KeyEvent.VK_1:
            if(evt.isControlDown()){
                temp =new Date().toGMTString()+" First data received.";
                NotesjTextArea.append(temp);
                //NotesjTextArea.append("\n\r");
                //logNotes(temp+"\n\r");
            }

            break;

            case KeyEvent.VK_2:
            if(evt.isControlDown()){
                temp = new Date().toGMTString()+" Two ping response heard.";
                NotesjTextArea.append(temp);
                //NotesjTextArea.append("\n\r");
                //logNotes(temp+"\n\r");
            }

            break;
            case KeyEvent.VK_T:
            if(evt.isControlDown()){
                temp = new Date().toGMTString()+" Telemetry command sent.";
                NotesjTextArea.append(temp);
                //NotesjTextArea.append("\n\r");
                //logNotes(temp+"\n\r");
            }

            break;

            case KeyEvent.VK_DELETE:
            if(evt.isControlDown()){
                NotesjTextArea.setText("");
                logNotes("");
            }

            break;

        }// end switch
        NotesjTextArea.setCaretPosition(NotesjTextArea.getDocument().getLength());
    }//GEN-LAST:event_NotesjTextAreaKeyPressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel DatajPanelTab;
    private javax.swing.JComboBox DeckUnitjComboBox;
    private javax.swing.JButton FileLocationjButton;
    private javax.swing.JComboBox FirmwarejComboBox;
    private javax.swing.JComboBox FlowControljComboBox;
    private javax.swing.JComboBox Frequency1jComboBox;
    private javax.swing.JComboBox Frequency2jComboBox;
    private javax.swing.JComboBox Frequency3jComboBox;
    private javax.swing.JComboBox Frequency4jComboBox;
    private javax.swing.JComboBox Frequency5jComboBox;
    private javax.swing.JComboBox Frequency6jComboBox;
    private javax.swing.JComboBox IESModeljComboBox;
    private javax.swing.JComboBox IESTypejComboBox;
    private javax.swing.JLabel MSBCountjLabel;
    private javax.swing.JLabel MSBTimerjLabel;
    private javax.swing.JTextArea NotesjTextArea;
    private javax.swing.JPanel PlotjPanelTab;
    private javax.swing.JTextArea RawDatajTextArea;
    private javax.swing.JComboBox ReceiveThresholdjComboBox;
    private javax.swing.JTextField SerialNumberjTextField;
    private javax.swing.JComboBox bitsjComboBox;
    private javax.swing.JButton clearDataLoggerjButton;
    private javax.swing.JPanel configurationjPanelTab;
    private javax.swing.JButton connectjButton;
    private javax.swing.JTextArea convertedDatajTextArea;
    private javax.swing.JButton deckBoxConfigurejButton;
    private javax.swing.JButton disconnectjButton;
    private javax.swing.JTextField fileLocationjTextField;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel markerJLabel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JComboBox parityjComboBox;
    private javax.swing.JComboBox portComboBox;
    private javax.swing.JComboBox portSpeedjComboBox;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JTabbedPane rawDataTabbedPane;
    private javax.swing.JComboBox receivePulseWidthjComboBox;
    private javax.swing.JLabel selectedURICommandjLabel;
    private javax.swing.JButton set100kSensitivityjButton;
    private javax.swing.JComboBox set100kSensitivityjComboBox;
    private javax.swing.JButton set105kSensitivityjButton;
    private javax.swing.JComboBox set105kSensitivityjComboBox;
    private javax.swing.JButton set110kSensitivityjButton;
    private javax.swing.JComboBox set110kSensitivityjComboBox;
    private javax.swing.JButton set115kSensitivityjButton;
    private javax.swing.JComboBox set115kSensitivityjComboBox;
    private javax.swing.JButton set120kSensitivityjButton;
    private javax.swing.JComboBox set120kSensitivityjComboBox;
    private javax.swing.JButton set125kSensitivityjButton;
    private javax.swing.JComboBox set125kSensitivityjComboBox;
    private javax.swing.JButton set130kSensitivityjButton;
    private javax.swing.JComboBox set130kSensitivityjComboBox;
    private javax.swing.JButton setGainJButton;
    private javax.swing.JComboBox setGainjComboBox;
    private javax.swing.JButton singlePingjButton;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JComboBox stopBitsjComboBox;
    private javax.swing.JButton transmitCodejButton;
    private javax.swing.JComboBox transmitCodejComboBox;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;

    private CommPortIdentifier portId = null;
    private SerialPort port = null;
    private ReadSerialPort rxRawData = null;
    private JComboBox []IESParameters = new JComboBox[10];
    private JTextField []IESFields = new JTextField[2];
    private ConvertIncommingFreq2Data converter = null;
    private JTextArea []displays = new JTextArea[2];
    private String deckBoxInUse = "DS-7000";
    private DeckBox deckBoxCnv;// = new configureDeckBox();
    Toolkit toolkit;
    java.util.Timer timer;
    DecrementMSB decMSBThread;
    private int selectedURICommand = 76;
   
/*
 returns the port to open

 */
/**
 * Searches for available ports
 * @param port String the name of the port that you want to return
 * @return CommPortIdentifier
 */
   private CommPortIdentifier getPort(String port){
    // Javacomm fields
        Enumeration portIdentifiers = CommPortIdentifier.getPortIdentifiers();
        CommPortIdentifier pid = null;
        while(portIdentifiers.hasMoreElements()){
            pid = (CommPortIdentifier) portIdentifiers.nextElement();
           // this.portComboBox.addItem(pid.getName());
            if(pid.getName().equals(port))
                break;
        }

        return pid;
    }// end getPort


   /**
    * populates the ComboBox with the available ports on the system
    */
   private void populatePortComboBox(){
        Enumeration portIdentifiers = CommPortIdentifier.getPortIdentifiers();
        CommPortIdentifier pid = null;

        while(portIdentifiers.hasMoreElements()){
        pid = (CommPortIdentifier) portIdentifiers.nextElement();
        this.portComboBox.addItem(pid.getName());
        }

    }// end populatePortComboBox



/**
 * things that need to be initialized go here
 */
private void init(){
    this.getFrame().setResizable(false);
    populatePortComboBox();

    //DefaultCaret dc = (DefaultCaret)this.RawDatajTextArea.getCaret();
    //DefaultCaret dc1 =(DefaultCaret)this.convertedDatajTextArea.getCaret();
    //dc.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
   // dc1.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

    
   

    File pWD;
    
    try
    {
        pWD = new File(System.getProperty("user.home") + File.separatorChar + "data"+File.separatorChar+ "TelemetryFiles");
        
        
        //if(pWD.isDirectory())
        //{
            pWD.mkdirs();
        //}//end if
        this.fileLocationjTextField.setText(pWD.getAbsolutePath());
       
       
    }//end try
    catch(Exception e)
    {
        pWD = new File("");
        this.fileLocationjTextField.setText(pWD.getAbsolutePath());
        e.printStackTrace();
    }//end catch
   
    //this.fileLocationjTextField.setText(pWD.getAbsolutePath());
    IESParameters[0]=this.IESTypejComboBox;
    IESParameters[1]=this.IESModeljComboBox;
    IESParameters[2]=this.FirmwarejComboBox;
    IESParameters[3]=this.Frequency1jComboBox;
    IESParameters[4]=this.Frequency2jComboBox;
    IESParameters[5]=this.Frequency3jComboBox;
    IESParameters[6]=this.Frequency4jComboBox;
    IESParameters[7]=this.Frequency5jComboBox;
    IESParameters[8]=this.Frequency6jComboBox;
    IESParameters[9]=this.DeckUnitjComboBox;


    this.setGainjComboBox.setSelectedIndex(3);
// sets receive threshold values
    for(int rxt = 10 ; rxt <= 256 ; rxt++ ){
        ReceiveThresholdjComboBox.addItem(rxt);
    }// end for


// sets receive threshold values
    for(int URICodes = 0 ; URICodes <= 76 ; URICodes++ ){
        this.transmitCodejComboBox.addItem(URICodes);
    }// end for
    this.transmitCodejComboBox.setSelectedIndex(76);
    selectURICommand();

    this.transmitCodejComboBox.setEnabled(false);
    this.transmitCodejButton.setEnabled(false);
    //this.singlePingjButton.setEnabled(false);

    IESFields[0] = this.fileLocationjTextField;
    IESFields[1] = this.SerialNumberjTextField;
    displays[0] = this.RawDatajTextArea;
    displays[1] = this.convertedDatajTextArea;
    rxRawData = new ReadSerialPort(port,displays,this.IESFields,this.IESParameters);
    rxRawData.stopThread();
    deckBoxCnv = new DeckBox();

    //deckBoxCnv.start();
    //deckBoxCnv.setButton(this.deckBoxConfigurejButton);
   // deckBoxCnv.stopThread();

    this.setPortSettingsEnable(true);

    this.deckBoxConfigurejButton.setEnabled(false);
    //startDecrementMSB(60);

   

//    ConvertIncommingFreq2Data tester = new ConvertIncommingFreq2Data();
  //  tester.start();

   
setDeckBoxType();
//progressBar.setOpaque(true);
//this.progressBar.setForeground(Color.red);
progressBar.setMinimum(0);
progressBar.setMaximum(25);
//progressBar.setValue(50);
progressBar.setVisible(true);

this.NotesjTextArea.setEditable(false);

this.setGainJButton.setEnabled(false);



            this.set100kSensitivityjComboBox.setEnabled(false);
            this.set100kSensitivityjButton.setEnabled(false);
            
            this.set105kSensitivityjComboBox.setEnabled(false);
            this.set105kSensitivityjButton.setEnabled(false);

            this.set110kSensitivityjComboBox.setEnabled(false);
            this.set110kSensitivityjButton.setEnabled(false);
            
            this.set115kSensitivityjComboBox.setEnabled(false);
            this.set115kSensitivityjButton.setEnabled(false);
            
            this.set120kSensitivityjComboBox.setEnabled(false);
            this.set120kSensitivityjButton.setEnabled(false);
            
            this.set125kSensitivityjComboBox.setEnabled(false);
            this.set125kSensitivityjButton.setEnabled(false);
            
            this.set130kSensitivityjComboBox.setEnabled(false);
            this.set130kSensitivityjButton.setEnabled(false); 
            
            this.clearDataLoggerjButton.setEnabled(false);

            this.ReceiveThresholdjComboBox.setEnabled(false);
            this.setGainjComboBox.setEnabled(false);
            this.receivePulseWidthjComboBox.setEnabled(false); 


}// end init()
/**
 * sends messages to standard out
 * @param message String-  the message to be printed
 */
public void log(String message){
    //System.out.println(message);
}//end log





    private void logNotes(String s){
        try{
            File f = new File(fileLocationjTextField.getText()+File.separator+IESTypejComboBox.getSelectedItem()+"_"+SerialNumberjTextField.getText()+"_"+IESModeljComboBox.getSelectedItem()+"_"+DeckUnitjComboBox.getSelectedItem()+"_notes.txt");
            FileWriter fw = new FileWriter(f,true);
            BufferedWriter bw = new BufferedWriter(fw);
            if(fw!=null && bw!=null){
                bw.append(s+"\n");
                bw.flush();
                bw.close();
                fw.close();
            }

        }// end try
        catch(Exception e){
            e.printStackTrace();
        }// end catch


    }// end log

/**
 * opens the selected port and initializes the SerialPortReader
 * @return boolean - true if open false if closed
 */
    @Action
    public boolean openPort() {


        /*If this works then I'll place this in a method
         */
            
    IESParameters[0]=this.IESTypejComboBox;
    IESParameters[1]=this.IESModeljComboBox;
    IESParameters[2]=this.FirmwarejComboBox;
    IESParameters[3]=this.Frequency1jComboBox;
    IESParameters[4]=this.Frequency2jComboBox;
    IESParameters[5]=this.Frequency3jComboBox;
    IESParameters[6]=this.Frequency4jComboBox;
    IESParameters[7]=this.Frequency5jComboBox;
    IESParameters[8]=this.Frequency6jComboBox;
    IESParameters[9]=this.DeckUnitjComboBox;

    /*thats the end of this block*/



        //SerialPort port = null;
        portId = getPort(portComboBox.getSelectedItem().toString());
        try{
            port = (SerialPort)portId.open("IESTelemetry",10000);
            port.setSerialPortParams(getPortSpeed(),getPortDatabits(),getStopBits(),getPortParity());

            rxRawData = new ReadSerialPort(port,displays,this.IESFields,this.IESParameters);
            rxRawData.setDeckBoxType(deckBoxInUse);
            rxRawData.setMarkerLabel(markerJLabel);
            rxRawData.setMSBCounterLabel(MSBCountjLabel);
            rxRawData.setNextMSBLabel(MSBTimerjLabel);
            rxRawData.setProgressBar(progressBar);
            //rxRawData.setTimerLength(new Double(Frequency6jComboBox.getSelectedItem()+"").doubleValue());
            rxRawData.start();

        }// end try
        catch(Exception e){
           e.printStackTrace();
            log(e.getMessage());
            //port = null;
            log(port.getName()+" did not open");
            return false;

        }//end catch

       
        log(port.getName()+" opened");
        //this.disconnectjButton.setEnabled(true);
        //this.connectjButton.setEnabled(false);
        this.setPortSettingsEnable(false);
        //log(getPortSpeed()+"");
    return true;
    }
/**
 * gets the index of the selected port speed
 * @return int - index of the selected port speed
 */
    int getPortSpeed(){
       int selectedIndex = this.portSpeedjComboBox.getSelectedIndex();
       int selection = -1;

       switch(selectedIndex){
           case -1:
               selection = 0;
           break;

           case 0:
               selection = 300;
           break;
           case 1:
               selection = 600;
           break;
           case 2:
               selection = 1200;
           break;
           case 3:
               selection = 2400;
           break;
           case 4:
               selection = 4800;
           break;
           case 5:
               selection = 9600;
           break;
           case 6:
               selection = 19200;
           break;
           case 7:
               selection = 38400;
           break;
           case 8:
               selection = 57600;
           break;
           case 9:
               selection = 115200;
           break;
               }// end switch
       return selection;
    };

/**
 * returns the parity that is selected
 * @return in the index of the selected port
 */
int getPortParity(){
       int selectedIndex = this.parityjComboBox.getSelectedIndex();
       int selection = -1;

       switch(selectedIndex){
           case -1:
               selection = 0;
           break;

           case 0:
               selection = SerialPort.PARITY_NONE;
           break;
           case 1:
               selection = SerialPort.PARITY_EVEN;
           break;
           case 2:
               selection = SerialPort.PARITY_ODD;
           break;

               }// end switch
       return selection;
    };

    /**
     * returns the selected data bits
     * @return nt - returns the index of the selected databits.
     */

int getPortDatabits(){
       int selectedIndex = this.bitsjComboBox.getSelectedIndex();
       int selection = -1;

       switch(selectedIndex){
           case -1:
               selection = 0;
           break;

           case 0:
               selection = SerialPort.DATABITS_8;
           break;
           case 1:
               selection = SerialPort.DATABITS_7;
           break;
           case 2:
               selection = SerialPort.DATABITS_6;
           break;
           case 3:
               selection = SerialPort.DATABITS_5;
           break;
          
               }// end switch
       return selection;
    };

    /**
     * returns the selected stop bits
     * @return int - returns the index of the selected stop bits
     */
int getStopBits(){
       int selectedIndex = this.stopBitsjComboBox.getSelectedIndex();
       int selection = -1;

       switch(selectedIndex){
           case -1:
               selection = 0;
           break;

           case 0:
               selection = SerialPort.STOPBITS_1;
           break;
           case 1:
               selection = SerialPort.STOPBITS_2;
           break;

               }// end switch
       return selection;
    }
/**
 * returns the flow control
 * @return int returns the index of the selected flow control
 */
int getFlowControl(){
       int selectedIndex = this.stopBitsjComboBox.getSelectedIndex();
       int selection = -1;

       switch(selectedIndex){
           case -1:
               selection = 0;
           break;

           case 0:
               selection = SerialPort.FLOWCONTROL_RTSCTS_IN;
           break;
           case 1:
               selection = SerialPort.FLOWCONTROL_XONXOFF_OUT;
           break;

               }// end switch
       return selection;
    }
/**
 * closes the selected serial port if it's open.
 */
    @Action
    public void closePort() {
        
        //rxRawData.closePort();
        //rxRawData.stopThread();
        if(port!= null){


       try{
          
           if((DeckUnitjComboBox.getSelectedItem()+"").equals("UDB-9000")){
               PrintStream ose = null;   
               ose = new PrintStream(port.getOutputStream());
               ose.print("+++");
               ose.print("\r");
               //System.out.println("+++");

           
               if(ose!=null){

                   ose.flush();
                   ose.close();
               }//end
           }//end if
           

            port.removeEventListener();
            MockEventListener mev = new MockEventListener();
            port.addEventListener(mev);
            port.removeEventListener();
            OutputStream os = port.getOutputStream();
            InputStream is = port.getInputStream();
            

            while(is.available() > 0){  // will empty out any remainig bytes int he receive buffer
                is.read();
            
            }// end while

            os.flush();
            is.close();
            os.close();
            
           
           
           port.close();

       }
       catch(Exception e){
           e.printStackTrace();
       }


           if(rxRawData != null && rxRawData.isAlive()  )
           {


               rxRawData.stopThread();

           }
            //this.converter.stopped=true;
        }
        
        //this.connectjButton.setEnabled(true);
        //this.disconnectjButton.setEnabled(false);
        this.setPortSettingsEnable(true);
    }
/**
 * This method enable and disables certain combo boxes and buttons when the
 * port is opened and closed
 * @param set boolean - true for enable and false for disable
 */
    private void setPortSettingsEnable(Boolean set){

        this.portComboBox.setEnabled(set);
        this.portSpeedjComboBox.setEnabled(set);
        this.parityjComboBox.setEnabled(set);
        this.bitsjComboBox.setEnabled(set);
        this.stopBitsjComboBox.setEnabled(set);
        this.FlowControljComboBox.setEnabled(set);
        this.disconnectjButton.setEnabled(!set);
        this.connectjButton.setEnabled(set);
        this.FileLocationjButton.setEnabled(set);
        this.fileLocationjTextField.setEnabled(set);
        this.deckBoxConfigurejButton.setEnabled(!set);
        this.DeckUnitjComboBox.setEnabled(set);
        this.NotesjTextArea.setEditable(!set);
        
       
        

        if(this.deckBoxInUse.equals("UDB-9000")){
            this.transmitCodejButton.setEnabled(!set);
            this.transmitCodejComboBox.setEnabled(!set);
            this.setGainJButton.setEnabled(!set);
            
            this.ReceiveThresholdjComboBox.setEnabled(!set);
            this.receivePulseWidthjComboBox.setEnabled(!set);               
            
            this.set100kSensitivityjComboBox.setEnabled(!set);
            this.set100kSensitivityjButton.setEnabled(!set);
            
            this.set105kSensitivityjComboBox.setEnabled(!set);
            this.set105kSensitivityjButton.setEnabled(!set);

            this.set110kSensitivityjComboBox.setEnabled(!set);
            this.set110kSensitivityjButton.setEnabled(!set);
            
            this.set115kSensitivityjComboBox.setEnabled(!set);
            this.set115kSensitivityjButton.setEnabled(!set);
            
            this.set120kSensitivityjComboBox.setEnabled(!set);
            this.set120kSensitivityjButton.setEnabled(!set);
            
            this.set125kSensitivityjComboBox.setEnabled(!set);
            this.set125kSensitivityjButton.setEnabled(!set);
            
            this.set130kSensitivityjComboBox.setEnabled(!set);
            this.set130kSensitivityjButton.setEnabled(!set); 
            
            this.clearDataLoggerjButton.setEnabled(!set);            
            
            
            
        }
      if(this.deckBoxInUse.equals("DS-7000")){
        
        this.singlePingjButton.setEnabled(!set);
        //this.ReceiveThresholdjComboBox.setEnabled(set);
        this.setGainjComboBox.setEnabled(!set);
        //this.receivePulseWidthjComboBox.setEnabled(set);        

      }

        //this.IESModeljComboBox.setEnabled(set);
        //this.IESTypejComboBox.setEnabled(set);
        //this.FirmwarejComboBox.setEnabled(set);
        //this.SerialNumberjTextField.setEnabled(set);
    
            if(deckBoxInUse.equals("SIM-7000")){
           
            this.ReceiveThresholdjComboBox.setEnabled(!set);
            //this.setGainjComboBox.setEnabled(set);
            //this.receivePulseWidthjComboBox.setEnabled(set);    
        }
    
    
    }// end setPortSettingsEnable
    
 
/**
 * Opens the file chooser used to select which folder to save the data files to.
 */
    @Action
    public void openFileChooser() {
       FileChooserJFrame fileChooser = new FileChooserJFrame(fileLocationjTextField.getText());
       fileChooser.setJTextField(fileLocationjTextField);
       fileChooser.setVisible(true);
       

    }//end open File chooser
/**
 * sets the frequencies and period that will ultimately be used by the ConvertIncomming data object
 */
    @Action
    public void setFreqsAndPeriod() {
        JComboBox[] x = new JComboBox[]{Frequency1jComboBox,Frequency2jComboBox,Frequency3jComboBox,Frequency4jComboBox,Frequency5jComboBox,Frequency6jComboBox};

        this.rxRawData.setFreqsAndPeriod(x);
        this.rxRawData.setDeckBoxType(deckBoxInUse);
       //rxRawData.setTimerLength(new Double(Frequency6jComboBox.getSelectedItem()+"").doubleValue());
        
    }
/**
 * sets the combo boxes to the frequencies based on firmware selection
 */
    @Action
    public void setGuiFreqs() {
        String firmWare  = this.FirmwarejComboBox.getSelectedItem()+"";
        
        if (firmWare.equals("A")){
            Frequency1jComboBox.setSelectedIndex(1);
            Frequency2jComboBox.setSelectedIndex(2);
            Frequency3jComboBox.setSelectedIndex(3);
            Frequency4jComboBox.setSelectedIndex(0);
            Frequency5jComboBox.setSelectedIndex(4);
            //Frequency6jComboBox.setSelectedIndex(0);

        }// end B
        if (firmWare.equals("B")){
            Frequency1jComboBox.setSelectedIndex(0);
            Frequency2jComboBox.setSelectedIndex(1);
            Frequency3jComboBox.setSelectedIndex(2);
            Frequency4jComboBox.setSelectedIndex(3);
            Frequency5jComboBox.setSelectedIndex(4);
            //Frequency6jComboBox.setSelectedIndex(1);
        }// end B

         setFreqsAndPeriod();
    }
/**
 * Sets the period for the data converter based on the type of instrument
 */
    @Action
    public void setGuiPeriod() {
        String IESType = this.IESTypejComboBox.getSelectedItem()+"";

        if(IESType.equals("PIES")){
            Frequency6jComboBox.setSelectedIndex(0);
            Frequency5jComboBox.setEnabled(false);
            Frequency4jComboBox.setEnabled(false);

            



        }// end PIES if

                if(IESType.equals("C-PIES")){
            Frequency6jComboBox.setSelectedIndex(1);
            Frequency5jComboBox.setEnabled(true);
            Frequency4jComboBox.setEnabled(true);

            
            

        }// end PIES if

         setFreqsAndPeriod();
    }

    @Action
    public void setGuiModelNumber() {
         setFreqsAndPeriod();
    }

    @Action
    public void setDeckBoxType() {
        deckBoxInUse = this.DeckUnitjComboBox.getSelectedItem()+"";
        //this.rxRawData.setDeckBoxType(deckBoxInUse);

        //System.out.println(deckBoxInUse);
        if(deckBoxInUse.equals("DS-7000")){
            portSpeedjComboBox.setSelectedIndex(5);
            
            
      /*     
            this.ReceiveThresholdjComboBox.setEnabled(false);
            this.setGainjComboBox.setEnabled(true);
            this.receivePulseWidthjComboBox.setEnabled(false);
            
            this.set100kSensitivityjComboBox.setEnabled(false);
            this.set100kSensitivityjButton.setEnabled(false);
            
            this.set105kSensitivityjComboBox.setEnabled(false);
            this.set105kSensitivityjButton.setEnabled(false);

            this.set110kSensitivityjComboBox.setEnabled(false);
            this.set110kSensitivityjButton.setEnabled(false);
            
            this.set115kSensitivityjComboBox.setEnabled(false);
            this.set115kSensitivityjButton.setEnabled(false);
            
            this.set120kSensitivityjComboBox.setEnabled(false);
            this.set120kSensitivityjButton.setEnabled(false);
            
            this.set125kSensitivityjComboBox.setEnabled(false);
            this.set125kSensitivityjButton.setEnabled(false);
            
            this.set130kSensitivityjComboBox.setEnabled(false);
            this.set130kSensitivityjButton.setEnabled(false); 
            
            this.clearDataLoggerjButton.setEnabled(false);
       */     
        }// end if

        if(deckBoxInUse.equals("SIM-7000")){
            portSpeedjComboBox.setSelectedIndex(9);
            
            
            /*
            this.ReceiveThresholdjComboBox.setEnabled(true);
            this.setGainjComboBox.setEnabled(false);
            this.receivePulseWidthjComboBox.setEnabled(false);
            
            this.set100kSensitivityjComboBox.setEnabled(false);
            this.set100kSensitivityjButton.setEnabled(false);
            
            this.set105kSensitivityjComboBox.setEnabled(false);
            this.set105kSensitivityjButton.setEnabled(false);

            this.set110kSensitivityjComboBox.setEnabled(false);
            this.set110kSensitivityjButton.setEnabled(false);
            
            this.set115kSensitivityjComboBox.setEnabled(false);
            this.set115kSensitivityjButton.setEnabled(false);
            
            this.set120kSensitivityjComboBox.setEnabled(false);
            this.set120kSensitivityjButton.setEnabled(false);
            
            this.set125kSensitivityjComboBox.setEnabled(false);
            this.set125kSensitivityjButton.setEnabled(false);
            
            this.set130kSensitivityjComboBox.setEnabled(false);
            this.set130kSensitivityjButton.setEnabled(false); 
            
            this.clearDataLoggerjButton.setEnabled(false);            
           */ 
        }// end if


        if(deckBoxInUse.equals("UDB-9000")){
            portSpeedjComboBox.setSelectedIndex(5);
            
          /*   
            this.ReceiveThresholdjComboBox.setEnabled(true);
            this.setGainjComboBox.setEnabled(false);
            this.receivePulseWidthjComboBox.setEnabled(true);
           
            this.set100kSensitivityjComboBox.setEnabled(true);
            this.set100kSensitivityjButton.setEnabled(true);
            
            this.set105kSensitivityjComboBox.setEnabled(true);
            this.set105kSensitivityjButton.setEnabled(true);

            this.set110kSensitivityjComboBox.setEnabled(true);
            this.set110kSensitivityjButton.setEnabled(true);
            
            this.set115kSensitivityjComboBox.setEnabled(true);
            this.set115kSensitivityjButton.setEnabled(true);
            
            this.set120kSensitivityjComboBox.setEnabled(true);
            this.set120kSensitivityjButton.setEnabled(true);
            
            this.set125kSensitivityjComboBox.setEnabled(true);
            this.set125kSensitivityjButton.setEnabled(true);
            
            this.set130kSensitivityjComboBox.setEnabled(true);
            this.set130kSensitivityjButton.setEnabled(true); 
            
            this.clearDataLoggerjButton.setEnabled(true);            
           */ 
        }// end if
        //System.out.println("set to "+deckBoxInUse);
    }

    @Action
    public void configureTheDeckBox() {



        if(deckBoxCnv!= null && !deckBoxCnv.isAlive()){

           JComponent jc[] = new JComponent[24];
           int item =  this.DeckUnitjComboBox.getSelectedIndex();

           //System.out.println("Selected item = "+item);

           switch(item){
               
               case 0:
                   deckBoxCnv = new configureDeckBox_DS7000();
                   break;
               case 1:
                   deckBoxCnv = new configureDeckBox_UDB9000();
                   set100kSensitivityjComboBox.setSelectedIndex(10);
                   set105kSensitivityjComboBox.setSelectedIndex(10);
                   set110kSensitivityjComboBox.setSelectedIndex(10);
                   set115kSensitivityjComboBox.setSelectedIndex(10);
                   set120kSensitivityjComboBox.setSelectedIndex(10);
                   set125kSensitivityjComboBox.setSelectedIndex(10);
                   set130kSensitivityjComboBox.setSelectedIndex(10);
                   break;
               case 2:
                   deckBoxCnv = new configureDeckBox_UDB9000_DS7000_Mode();
                   break;
            }// end switch

                
            
            jc[0] = this.deckBoxConfigurejButton;
            jc[1] = this.transmitCodejButton;
            jc[2] = this.transmitCodejComboBox;
            jc[3] = this.disconnectjButton;
            jc[4] = this.singlePingjButton;
            jc[5] = set100kSensitivityjComboBox;
            jc[6] = set105kSensitivityjComboBox;
            jc[7] = set110kSensitivityjComboBox;
            jc[8] = set115kSensitivityjComboBox;
            jc[9] = set120kSensitivityjComboBox;
            jc[10] = set125kSensitivityjComboBox;
            jc[11] = set130kSensitivityjComboBox;  
            
            jc[12] = set100kSensitivityjButton;
            jc[13] = set105kSensitivityjButton;
            jc[14] = set110kSensitivityjButton;
            jc[15] = set115kSensitivityjButton;
            jc[16] = set120kSensitivityjButton;
            jc[17] = set125kSensitivityjButton;
            jc[18] = set130kSensitivityjButton;
            jc[19] = ReceiveThresholdjComboBox;
            jc[20] = setGainJButton;
            jc[21] = clearDataLoggerjButton;
            jc[22] = receivePulseWidthjComboBox;
            jc[23] = setGainjComboBox;
            
            
            
            deckBoxCnv.setPort(port);
            deckBoxCnv.setDeckBox(deckBoxInUse);
            deckBoxCnv.setGain(this.setGainjComboBox.getSelectedIndex());
            deckBoxCnv.setReceiveThreshold(new Integer(ReceiveThresholdjComboBox.getSelectedItem()+"").intValue());
            deckBoxCnv.setNumberOfFrequencies(7);
            deckBoxCnv.setRecievePulseWidth(new Integer(this.receivePulseWidthjComboBox.getSelectedItem()+"").intValue());
            //System.out.println(new Integer(this.receivePulseWidthjComboBox.getSelectedItem()+"").intValue());
            //deckBoxCnv.setButton(this.deckBoxConfigurejButton);
            int enabled = 0;
            int j = 0;
             for (int i = 0 ; i < jc.length ; i++){
                if(jc[i].isEnabled()) enabled++;

            }// end for}

            JComponent jc1[] = new JComponent[enabled];
            for (int i = 0 ; i < jc.length ; i++){
                if(jc[i].isEnabled())
                {
                    jc1[j] = jc[i];
                    jc1[j].setEnabled(false);
                    j++;

                }
            }// end for

            deckBoxCnv.setJComponents(jc1);
            deckBoxCnv.start();


            

            //deckBoxConfigurejButton.setEnabled(false);

    }
    }

    /**
     * starts the timer that decrements the MSB timer every minute if it
     * is greater tha 0
     * @param seconds int the time in between decrements in seconds
     */

  public void startDecrementMSB(int seconds) {
    toolkit = Toolkit.getDefaultToolkit();
    timer = new java.util.Timer();
    decMSBThread = new DecrementMSB(this.MSBTimerjLabel);
    //timer.schedule(new DecrementMSB(), seconds * 1000);

    timer.scheduleAtFixedRate(decMSBThread, 0, seconds*1000);


  }




/**
 * this class  decrements tye MSB timer label
 */

    class DecrementMSB extends TimerTask {
JLabel labelThatHoldsTimeLeft;
        public DecrementMSB(JLabel j){
            labelThatHoldsTimeLeft = j;

       }// end consturctor
    public void run() {

            int timeLeft = new Integer(labelThatHoldsTimeLeft.getText()).intValue();

            if (timeLeft > 0){
                timeLeft--;
                labelThatHoldsTimeLeft.setText(timeLeft+"");

            }


            if(timeLeft < 0 )
                labelThatHoldsTimeLeft.setText("0");
            
            if(timeLeft == 0)
                this.cancel();
      //System.out.println("One Minute has passed");
     //timer.cancel();


    }
  }



    @Action
    public void sendURICode() {
        if(selectedURICommand >= 0 && selectedURICommand<=63){}// end if

        else{
            JComponent jc[] = new JComponent[24];
            jc[0]=this.transmitCodejComboBox;
            jc[1]=this.transmitCodejButton;
            jc[2]=this.disconnectjButton;
            jc[3]=this.deckBoxConfigurejButton;
            jc[4]=this.setGainJButton;
            
            jc[5] = set100kSensitivityjComboBox;
            jc[6] = set105kSensitivityjComboBox;
            jc[7] = set110kSensitivityjComboBox;
            jc[8] = set115kSensitivityjComboBox;
            jc[9] = set120kSensitivityjComboBox;
            jc[10] = set125kSensitivityjComboBox;
            jc[11] = set130kSensitivityjComboBox;  
            
            jc[12] = set100kSensitivityjButton;
            jc[13] = set105kSensitivityjButton;
            jc[14] = set110kSensitivityjButton;
            jc[15] = set115kSensitivityjButton;
            jc[16] = set120kSensitivityjButton;
            jc[17] = set125kSensitivityjButton;
            jc[18] = set130kSensitivityjButton;
            jc[19] = ReceiveThresholdjComboBox;
            jc[20] = setGainJButton;
            jc[21] = clearDataLoggerjButton;
            jc[22] = receivePulseWidthjComboBox;
            jc[23] = setGainjComboBox;
            
            SendURICommand suc = new SendURICommand();
            suc.setDeckBox(deckBoxInUse);
            suc.setJComponents(jc);
            suc.setPort(port);

            for (int i = 0 ; i < jc.length ; i++){
                jc[i].setEnabled(false);

            }// end for
            suc.start();
            String date = "";
            date = new Date().toGMTString();
            if(selectedURICommand >=64  && selectedURICommand <= 67){
                logNotes(date + " Telem command " + selectedURICommand + " sent.\r");
                this.NotesjTextArea.append(date + " TELEM command " + selectedURICommand + " sent.\n");

            }

            if(selectedURICommand >= 68 && selectedURICommand <= 71){
                logNotes(date + " XPND command " + selectedURICommand + " sent.\r");
                this.NotesjTextArea.append(date + " XPND command " + selectedURICommand + " sent.\n");

            }// end else

            if(selectedURICommand >= 72 && selectedURICommand <= 75){
                logNotes(date + " BEACON command " + selectedURICommand + " sent.\r");
                this.NotesjTextArea.append(date + " BEACON command " + selectedURICommand + " sent.\n");

            }// end else


            if(selectedURICommand== 76){
                logNotes(date + " Clear command " + selectedURICommand + " sent.\r");
                this.NotesjTextArea.append(date + " CLEAR command " + selectedURICommand + " sent.\n");

            }// end else
        }// end else
       

        

    }

    @Action
    public void sendSinglePing() {

        JComponent jc[] = new JComponent[3];

        jc[0]=this.disconnectjButton;
        jc[1]=this.deckBoxConfigurejButton;
        jc[2]=this.singlePingjButton;
        SendSinglePing ssp = new SendSinglePing();
        ssp.setDeckBox(deckBoxInUse);
        ssp.setJComponents(jc);
        ssp.setPort(port);

    for (int i = 0 ; i < jc.length ; i++){
        jc[i].setEnabled(false);

    }// end for}

        ssp.start();
    }// end sendSinglePing

    @Action
    public void selectURICommand() {
        this.selectedURICommand=this.transmitCodejComboBox.getSelectedIndex();

        if(selectedURICommand >= 0 && selectedURICommand <= 63)
        {
            selectedURICommandjLabel.setText("RELEASE");
            selectedURICommandjLabel.setOpaque(true);
            selectedURICommandjLabel.setBackground(Color.red);
            
        }//end if

        if(selectedURICommand >=64  && selectedURICommand <= 67)
        {
            selectedURICommandjLabel.setText("TELEM");
            selectedURICommandjLabel.setOpaque(true);
            selectedURICommandjLabel.setBackground(Color.green);
        }//end if
        if(selectedURICommand >= 68 && selectedURICommand <= 71)
        {
            selectedURICommandjLabel.setText("XPND");
            selectedURICommandjLabel.setOpaque(true);
            selectedURICommandjLabel.setBackground(Color.orange);
        }//end if
        if(selectedURICommand >= 72 && selectedURICommand <= 75)
        {
            selectedURICommandjLabel.setText("BEACON");
            selectedURICommandjLabel.setOpaque(true);
            selectedURICommandjLabel.setBackground(Color.yellow);
        }//end if
        if(selectedURICommand== 76)
        {
            selectedURICommandjLabel.setText("CLEAR");
            selectedURICommandjLabel.setOpaque(true);
            selectedURICommandjLabel.setBackground(Color.green);
        }//end if
    }

    @Action
    public void launchIESManual() {
      String ps = File.separator;
      String pwd = new File("").getAbsolutePath();
      File document = null;

      try{
         document = new File(pwd + ps + "documents"+ ps + "CPIESUserManual.pdf");
         new LaunchDesktopDocument(document);
  
      }// end try

      catch(Exception e){
      //e.printStackTrace();
      if (e.getMessage().contains("doesn't exist")){

          try{
           pwd = new File("").getAbsoluteFile().getParentFile().getAbsolutePath();
          document = new File(pwd + ps + "documents"+ ps + "CPIESUserManual.pdf");
         //System.out.println(pwd);
           new LaunchDesktopDocument(document);
          }
          catch(Exception e1){
          e1.printStackTrace();}



      }// end if
      }// end catch

       
    } // end launchIESManual

    @Action
    public void telemetryHelp() {

      String ps = File.separator;
      String pwd = new File("").getAbsolutePath();
      File document = null;

      try{
         document = new File(pwd + ps + "documents"+ ps + "help.html");
         new LaunchDesktopDocument(document);

      }// end try

      catch(Exception e){
      //e.printStackTrace();
      if (e.getMessage().contains("doesn't exist")){

          try{
           pwd = new File("").getAbsoluteFile().getParentFile().getAbsolutePath();
          document = new File(pwd + ps + "documents"+ ps + "help.html");
         //System.out.println(pwd);
           new LaunchDesktopDocument(document);
          }
          catch(Exception e1){
          e1.printStackTrace();}



      }// end if
      }// end catch



    }//end telemetry help

    @Action
    public void launchATM90xAddendum() {

      String ps = File.separator;
      String pwd = new File("").getAbsolutePath();
      File document = null;

      try{
         document = new File(pwd + ps + "documents"+ ps + "URI_PIES_Addendum.pdf");
         new LaunchDesktopDocument(document);

      }// end try

      catch(Exception e){
      //e.printStackTrace();
      if (e.getMessage().contains("doesn't exist")){

          try{
           pwd = new File("").getAbsoluteFile().getParentFile().getAbsolutePath();
          document = new File(pwd + ps + "documents"+ ps + "URI_PIES_Addendum.pdf");
         //System.out.println(pwd);
           new LaunchDesktopDocument(document);
          }
          catch(Exception e1){
          e1.printStackTrace();}



      }// end if
      }// end catch
    }

    @Action
    public void launchATM900SeriesManual() {

      String ps = File.separator;
      String pwd = new File("").getAbsolutePath();
      File document = null;

      try{
         document = new File(pwd + ps + "documents"+ ps + "ATM-900_Series_Acoustic_Telemetry_Modem_User's_Manual_Rev_A.pdf");
         new LaunchDesktopDocument(document);

      }// end try

      catch(Exception e){
      //e.printStackTrace();
      if (e.getMessage().contains("doesn't exist")){

          try{
           pwd = new File("").getAbsoluteFile().getParentFile().getAbsolutePath();
          document = new File(pwd + ps + "documents"+ ps + "ATM-900_Series_Acoustic_Telemetry_Modem_User's_Manual_Rev_A.pdf");
         //System.out.println(pwd);
           new LaunchDesktopDocument(document);
          }
          catch(Exception e1){
          e1.printStackTrace();}



      }// end if
      }// end catch
    }

    @Action
    public void launchUDBManual() {

      String ps = File.separator;
      String pwd = new File("").getAbsolutePath();
      File document = null;

      try{
         document = new File(pwd + ps + "documents"+ ps + "UDB-9000-M_and_MR_ Rev_C.pdf");
         new LaunchDesktopDocument(document);

      }// end try

      catch(Exception e){
      //e.printStackTrace();
      if (e.getMessage().contains("doesn't exist")){

          try{
           pwd = new File("").getAbsoluteFile().getParentFile().getAbsolutePath();
          document = new File(pwd + ps + "documents"+ ps + "UDB-9000-M_and_MR_ Rev_C.pdf");
         //System.out.println(pwd);
           new LaunchDesktopDocument(document);
          }
          catch(Exception e1){
          e1.printStackTrace();}



      }// end if
      }// end catch



    }

    @Action
    public void launchDS7000Manual() {

      String ps = File.separator;
      String pwd = new File("").getAbsolutePath();
      File document = null;

      try{
         document = new File(pwd + ps + "documents"+ ps + "DS7000MANUAL.PDF");
         new LaunchDesktopDocument(document);

      }// end try

      catch(Exception e){
      //e.printStackTrace();
      if (e.getMessage().contains("doesn't exist")){

          try{
           pwd = new File("").getAbsoluteFile().getParentFile().getAbsolutePath();
          document = new File(pwd + ps + "documents"+ ps + "DS7000MANUAL.PDF");
         //System.out.println(pwd);
           new LaunchDesktopDocument(document);
          }
          catch(Exception e1){
          e1.printStackTrace();}



      }// end if
      }// end catch
    }


public class MockEventListener implements SerialPortEventListener{

    public void serialEvent(SerialPortEvent ev){}

}// end mockEventListener

    @Action
    public void setRXThreshold() {
        SetRXThreshold rxt = new SetRXThreshold();
        rxt.setPort(port);
        rxt.setGlobalReceiveThreshold(new Integer(this.ReceiveThresholdjComboBox.getSelectedItem()+"").intValue());
        rxt.start();
        
    }

    @Action
    public void set100kSensitivity() {
        
        SetChannelReceiveSensitivity crs = new SetChannelReceiveSensitivity();
        crs.setPort(port);
        crs.setChannelandRxSensitivity("10.0",set100kSensitivityjComboBox.getSelectedItem()+"");
        crs.start();
    }

    @Action
    public void set105kSensitivity() {
        
        SetChannelReceiveSensitivity crs = new SetChannelReceiveSensitivity();
        crs.setPort(port);
        crs.setChannelandRxSensitivity("10.5",set105kSensitivityjComboBox.getSelectedItem()+"");
        crs.start();        
        
    }

    @Action
    public void set110kSensitivity() {
  
        SetChannelReceiveSensitivity crs = new SetChannelReceiveSensitivity();
        crs.setPort(port);
        crs.setChannelandRxSensitivity("11.0",set110kSensitivityjComboBox.getSelectedItem()+"");
        crs.start();         
        
    }

    @Action
    public void set115kSensitivity() {
        SetChannelReceiveSensitivity crs = new SetChannelReceiveSensitivity();
        crs.setPort(port);
        crs.setChannelandRxSensitivity("11.5",set115kSensitivityjComboBox.getSelectedItem()+"");
        crs.start();         
        
    }

    @Action
    public void set120kSensitivity() {
        
        SetChannelReceiveSensitivity crs = new SetChannelReceiveSensitivity();
        crs.setPort(port);
        crs.setChannelandRxSensitivity("12.0",set120kSensitivityjComboBox.getSelectedItem()+"");
        crs.start();         
    }

    @Action
    public void set125kSensitivity() {

        SetChannelReceiveSensitivity crs = new SetChannelReceiveSensitivity();
        crs.setPort(port);
        crs.setChannelandRxSensitivity("12.5",set125kSensitivityjComboBox.getSelectedItem()+"");
        crs.start();        
        
    }

    @Action
    public void set130kSensitivity() {
        
        SetChannelReceiveSensitivity crs = new SetChannelReceiveSensitivity();
        crs.setPort(port);
        crs.setChannelandRxSensitivity("13.0",set130kSensitivityjComboBox.getSelectedItem()+"");
        crs.start();          
    }

    @Action
    public void clearDataLogger() {
        JComponent jc[] = new JComponent[24];
        ClearUDB9000DataLogger cdl = new ClearUDB9000DataLogger();
        cdl.setPort(port);
        

            jc[0]=this.transmitCodejComboBox;
            jc[1]=this.transmitCodejButton;
            jc[2]=this.disconnectjButton;
            jc[3]=this.deckBoxConfigurejButton;
            jc[4]=this.setGainJButton;
            
            jc[5] = set100kSensitivityjComboBox;
            jc[6] = set105kSensitivityjComboBox;
            jc[7] = set110kSensitivityjComboBox;
            jc[8] = set115kSensitivityjComboBox;
            jc[9] = set120kSensitivityjComboBox;
            jc[10] = set125kSensitivityjComboBox;
            jc[11] = set130kSensitivityjComboBox;  
            
            jc[12] = set100kSensitivityjButton;
            jc[13] = set105kSensitivityjButton;
            jc[14] = set110kSensitivityjButton;
            jc[15] = set115kSensitivityjButton;
            jc[16] = set120kSensitivityjButton;
            jc[17] = set125kSensitivityjButton;
            jc[18] = set130kSensitivityjButton;
            jc[19] = ReceiveThresholdjComboBox;
            jc[20] = setGainJButton;
            jc[21] = clearDataLoggerjButton;
            jc[22] = receivePulseWidthjComboBox;
            jc[23] = setGainjComboBox;
            
            
            
            int enabled = 0;
            int j = 0;
             for (int i = 0 ; i < jc.length ; i++){
                if(jc[i].isEnabled()) enabled++;

            }// end for}

            JComponent jc1[] = new JComponent[enabled];
            for (int i = 0 ; i < jc.length ; i++){
                if(jc[i].isEnabled())
                {
                    jc1[j] = jc[i];
                    jc1[j].setEnabled(false);
                    j++;

                }
            }// end for
            
            cdl.setJComponents(jc1);
           
            cdl.start();
        
        
        
        
    }

}// end class




