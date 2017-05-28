/*
 * Created by JFormDesigner on Tue Mar 15 10:21:26 CET 2016
 */

package main;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import gamemodes.Farcry1GameEvent;
import gamemodes.GameEventListener;
import interfaces.PercentageInterface;
import misc.Tools;
import org.apache.log4j.Logger;
import progresshandlers.EscalatingSiren1Only;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;


/**
 * @author Torsten Löhr
 */
public class FrmTest extends JFrame implements GameEventListener {
    PercentageInterface[] progressHandlers = new PercentageInterface[]{
            new EscalatingSiren1Only(MissionBox.MBX_SIREN1)
//            new EscalatingSirens(MissionBox.MBX_SIREN1, MissionBox.MBX_SIREN2, MissionBox.MBX_SIREN3),
//            new EscalatingSirensTime(MissionBox.MBX_SIREN1, MissionBox.MBX_SIREN2, MissionBox.MBX_SIREN3)
    };
    Logger logger = Logger.getLogger(getClass());

    ArrayList<Farcry1GameEvent> eventModel = new ArrayList<>();

    public FrmTest() {
        initComponents();
        initPanel();
    }

    @Override
    public void eventSent(Farcry1GameEvent event) {
        setRevertEvent(event);
    }

    public void addGameEvent(Farcry1GameEvent event, long remaining) {
        event.setGameEventListener(this);
        if (!eventModel.isEmpty()) {
            getLastEvent().finalizeEvent(event.getMessageEvent().getGametimer(), remaining);
        }
        eventModel.add(event);
        listEvents.add(event);

        // scrolle die Liste immer ganz nach unten
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = panel7.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    public void clearEvents() {
        eventModel.clear();
        listEvents.removeAll();
    }

    public Farcry1GameEvent getLastEvent() {
        return eventModel.get(eventModel.size() - 1);
    }

    // setzt einen Revert Event fest, zu dem zurückgesprungen werden soll.
    public void setRevertEvent(Farcry1GameEvent revertEvent) {
        lblRevertEvent.setText(revertEvent == null ? "--" : revertEvent.toHTML());
        lblRevertEvent.setIcon(revertEvent == null ? null : revertEvent.getIcon());
        MissionBox.setRevertEvent(revertEvent);
    }

//    public void revert() {
//        int pos = eventModel.indexOf(revertEvent);
//        listEvents.removeAll();
//
//        ArrayList<Farcry1GameEvent> tmpList = new ArrayList<>();
//
//
//        for (int p = 0; p < pos; p++) {
//            tmpList.add(eventModel.get(p));
//        }
//        eventModel.clear();
//        eventModel.addAll(tmpList);
//        tmpList.clear();
//
//    }

    private void initPanel() {
        logger.setLevel(MissionBox.getLogLevel());
//        tbDebug.setSelected(MissionBox.getConfig(MissionBox.MBX_DEBUG).equals("true"));
        tbDebug.addItemListener(i -> {
//            MissionBox.setConfig(MissionBox.MBX_DEBUG, i.getStateChange() == ItemEvent.SELECTED ? "true" : "false");

            SwingUtilities.invokeLater(() -> {
                if (i.getStateChange() == ItemEvent.SELECTED) {
                    ((FormLayout) contentPanel.getLayout()).setColumnSpec(3, ColumnSpec.decode("default"));
                } else {
                    ((FormLayout) contentPanel.getLayout()).setColumnSpec(3, ColumnSpec.decode("0dlu"));
                }
                contentPanel.revalidate();
                contentPanel.repaint();
            });
        });


        SwingUtilities.invokeLater(() -> {
            if (tbDebug.isSelected()) {
                ((FormLayout) contentPanel.getLayout()).setColumnSpec(3, ColumnSpec.decode("default"));
            } else {
                ((FormLayout) contentPanel.getLayout()).setColumnSpec(3, ColumnSpec.decode("0dlu"));
            }
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        setTitle(MissionBox.getAppinfo().getProperty("program.BUILDDATE") + " [" + MissionBox.getAppinfo().getProperty("program.BUILDNUM") + "]");

        pb1.setVisible(true);

        cmbSirenHandler.setModel(new DefaultComboBoxModel<>(progressHandlers));
        int sirenhandler = Integer.parseInt(MissionBox.getConfig(MissionBox.MBX_SIRENHANDLER, "0"));
        cmbSirenHandler.setSelectedIndex(sirenhandler);
        MissionBox.setRelaisSirens(progressHandlers[sirenhandler]);

        cmbSirenHandler.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    MissionBox.setRelaisSirens((PercentageInterface) e.getItem());
                    MissionBox.setConfig(MissionBox.MBX_SIRENHANDLER, Integer.toString(cmbSirenHandler.getSelectedIndex()));
                }
            }
        });


        // Events for the Hardware Test
        btnRelayTest1.addActionListener(e -> relayAction(e));
        btnRelayTest2.addActionListener(e -> relayAction(e));
        btnRelayTest3.addActionListener(e -> relayAction(e));
        btnRelayTest4.addActionListener(e -> relayAction(e));
        btnRelayTest5.addActionListener(e -> relayAction(e));
        btnRelayTest6.addActionListener(e -> relayAction(e));
        btnRelayTest7.addActionListener(e -> relayAction(e));
        btnRelayTest8.addActionListener(e -> relayAction(e));

        btnRedLED.addActionListener(e -> relayAction(e));
        btnGreenLED.addActionListener(e -> relayAction(e));
        btnRedProgress.addActionListener(e -> relayAction(e));
        btnYellowProgress.addActionListener(e -> relayAction(e));
        btnGreenProgress.addActionListener(e -> relayAction(e));

        btnRGBred.addActionListener(e -> relayAction(e));
        btnRGBgreen.addActionListener(e -> relayAction(e));
        btnRGBblue.addActionListener(e -> relayAction(e));

        btnRelayTest1.setToolTipText("mcp23017-01-B7");
        btnRelayTest2.setToolTipText("mcp23017-01-B6");
        btnRelayTest3.setToolTipText("mcp23017-01-B5");
        btnRelayTest4.setToolTipText("mcp23017-01-B4");
        btnRelayTest5.setToolTipText("mcp23017-01-B3");
        btnRelayTest6.setToolTipText("mcp23017-01-B2");
        btnRelayTest7.setToolTipText("mcp23017-01-B1");
        btnRelayTest8.setToolTipText("mcp23017-01-B0");


    }

    public void setButtonTestLabel(String name, boolean on) {
        if (tabbedPane1.getSelectedIndex() != 2) return; // only react when in debug gameState

        if (name.equalsIgnoreCase("red")) {
            lblButtonRed.setEnabled(on);
        } else if (name.equalsIgnoreCase("green")) {
            lblButtonGreen.setEnabled(on);
        } else if (name.equalsIgnoreCase("pause")) {
            lblButtonPAUSE.setEnabled(on);
        } else if (name.equalsIgnoreCase("start")) {
            lblButtonStartStop.setEnabled(on);
        } else if (name.equalsIgnoreCase("quit")) {
            lblButtonQuit.setEnabled(on);
        }
    }

    public JToggleButton getTbDebug() {
        return tbDebug;
    }

    /**
     * Einheitliche Action Methode für alle Relais Testbuttons
     *
     * @param e
     */
    private void relayAction(ActionEvent e) {
        String text = ((JButton) e.getSource()).getText();

        logger.debug(text);

        if (text.equalsIgnoreCase("relay1")) {
            MissionBox.setScheme("mcp23017-01-B7", txtHandlerPattern.getText().trim());
        } else if (text.equalsIgnoreCase("relay2")) {
            MissionBox.setScheme("mcp23017-01-B6", txtHandlerPattern.getText().trim());
        } else if (text.equalsIgnoreCase("relay3")) {
            MissionBox.setScheme("mcp23017-01-B5", txtHandlerPattern.getText().trim());
        } else if (text.equalsIgnoreCase("relay4")) {
            MissionBox.setScheme("mcp23017-01-B4", txtHandlerPattern.getText().trim());
        } else if (text.equalsIgnoreCase("relay5")) {
            MissionBox.setScheme("mcp23017-01-B3", txtHandlerPattern.getText().trim());
        } else if (text.equalsIgnoreCase("relay6")) {
            MissionBox.setScheme("mcp23017-01-B2", txtHandlerPattern.getText().trim());
        } else if (text.equalsIgnoreCase("relay7")) {
            MissionBox.setScheme("mcp23017-01-B1", txtHandlerPattern.getText().trim());
        } else if (text.equalsIgnoreCase("relay8")) {
            // airsiren
            MissionBox.setScheme("mcp23017-01-B0", txtHandlerPattern.getText().trim());
        } else if (text.equalsIgnoreCase("LEDred")) {
            MissionBox.setScheme("mcp23017-01-A7", txtHandlerPattern.getText().trim());
        } else if (text.equalsIgnoreCase("LEDgreen")) {
            MissionBox.setScheme("mcp23017-01-A6", txtHandlerPattern.getText().trim());
        } else if (text.equalsIgnoreCase("PBred")) {
            MissionBox.setScheme("mcp23017-01-A5", txtHandlerPattern.getText().trim());
        } else if (text.equalsIgnoreCase("PByellow")) {
            MissionBox.setScheme("mcp23017-01-A4", txtHandlerPattern.getText().trim());
        } else if (text.equalsIgnoreCase("PBgreen")) {
            MissionBox.setScheme("mcp23017-01-A3", txtHandlerPattern.getText().trim());
        } else if (text.equalsIgnoreCase("RGBred")) {
            MissionBox.setScheme("mcp23017-02-A7", txtHandlerPattern.getText().trim());
        } else if (text.equalsIgnoreCase("RGBgreen")) {
            MissionBox.setScheme("mcp23017-02-A6", txtHandlerPattern.getText().trim());
        } else if (text.equalsIgnoreCase("RGBblue")) {
            MissionBox.setScheme("mcp23017-02-A5", txtHandlerPattern.getText().trim());
        }


    }


    private void btnFCYcapPlusActionPerformed(ActionEvent e) {
        fcyCapChange(1);
    }

    void fcyCapChange(int seconds) {
        int time2capture = Integer.parseInt(MissionBox.getConfig(MissionBox.FCY_TIME2CAPTURE));

        if (time2capture + seconds < 1) time2capture = 1;
        else time2capture += seconds;

        final String text = Integer.toString(time2capture);
        MissionBox.setConfig(MissionBox.FCY_TIME2CAPTURE, text);
        SwingUtilities.invokeLater(() -> {
            lblFCYCapture.setText(text);
            revalidate();
            repaint();
        });
    }

    void fcyGameTimeChange(int seconds) {
        int gametime = Integer.parseInt(MissionBox.getConfig(MissionBox.FCY_GAMETIME));

        if (gametime + seconds < 1) gametime = 1;
        else gametime += seconds;

        final String text = Integer.toString(gametime);
        MissionBox.setConfig(MissionBox.FCY_GAMETIME, text);

        SwingUtilities.invokeLater(() -> {
            lblFCYGametime.setText(text);
            revalidate();
            repaint();
        });
    }

    void fcyRespawnChange(int seconds) {
        long respawn = Long.parseLong(MissionBox.getConfig(MissionBox.FCY_RESPAWN_INTERVAL));

        if (respawn + seconds < 0) respawn = 0;
        else respawn += seconds;

        final String text = Long.toString(respawn);
        MissionBox.setConfig(MissionBox.FCY_RESPAWN_INTERVAL, text);
        SwingUtilities.invokeLater(() -> {
            lblFCYRespawn.setText(text);
            revalidate();
            repaint();
        });
    }

    private void btnFCYcapMinusActionPerformed(ActionEvent e) {
        fcyCapChange(-1);
    }


    public JPanel getDebugPanel4Pins() {
        return debugPanel4Pins;
    }

//    public void log(String text) {
//
//        if (text == null) {
//            txtLog.setText("");
//        } else {
//            log(0, "", text);
//        }
//    }
//
//    public void log(long someID, String someText, String text) {
//        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
//        String newTxt = txtLog.getText() + "\n(" + df.format(new Date()) + ") ";
//        newTxt += someID > 0 ? " [" + someID + "] " : "";
//        newTxt += !someText.isEmpty() ? " \"" + someText + "\" " : "";
//        newTxt += text;
//        txtLog.setText(newTxt);
//    }


    private void tabbedPane1StateChanged(ChangeEvent e) {
        if (tabbedPane1.getSelectedIndex() == 0) {
//            MissionBox.saveLocalProps();
        } else if (tabbedPane1.getSelectedIndex() == 1) {
            lblFCYCapture.setText(MissionBox.getConfig(MissionBox.FCY_TIME2CAPTURE));
            lblFCYGametime.setText(MissionBox.getConfig(MissionBox.FCY_GAMETIME));
            lblFCYRespawn.setText(MissionBox.getConfig(MissionBox.FCY_RESPAWN_INTERVAL));

        } else {
            MissionBox.getPinHandler().off();
        }
    }

    public void setProgress(int progress) {
        pb1.setIndeterminate(progress < 0);
        if (progress >= 0) {
            pb1.setValue(progress);
        }
    }

    public JButton getBtnClearEvent() {
        return btnClearEvent;
    }

    public void enableSettings(boolean yes) {
        tabbedPane1.setEnabledAt(1, yes);
        tabbedPane1.setEnabledAt(2, yes);
    }

    public void setToPauseMode(boolean yes) {
        for (Component comp : listEvents.getComponents()) {
            comp.setEnabled(yes);
        }
        listEvents.setEnabled(yes);
        btnClearEvent.setEnabled(yes);
    }


    public boolean isGameStartable() {
        return tabbedPane1.getSelectedIndex() == 0;
    }

    private void btnRedLedBarActionPerformed(ActionEvent e) {
//        MissionBox.setScheme(MissionBox.MBX_TIME_SIREN, "3;500,500");
    }

    private void lblFCYCaptureActionPerformed(ActionEvent e) {
        JTextField txt = ((JTextField) e.getSource());
        int capture = Integer.parseInt(MissionBox.getConfig(MissionBox.FCY_TIME2CAPTURE));
        int value = Tools.parseInt(txt.getText(), 0, Integer.MAX_VALUE, capture);
        MissionBox.setConfig(MissionBox.FCY_TIME2CAPTURE, Integer.toString(value));
        txt.setText(Integer.toString(value));
    }

    private void lblFCYCaptureFocusLost(FocusEvent e) {
        lblFCYCaptureActionPerformed(new ActionEvent(e.getSource(), 0, ""));
    }

    private void lblFCYGametimeActionPerformed(ActionEvent e) {
        JTextField txt = ((JTextField) e.getSource());
        int capture = Integer.parseInt(MissionBox.getConfig(MissionBox.FCY_GAMETIME));
        int value = Tools.parseInt(txt.getText(), 1, Integer.MAX_VALUE, capture);
        MissionBox.setConfig(MissionBox.FCY_GAMETIME, Integer.toString(value));
        txt.setText(Integer.toString(value));
    }

    private void lblFCYGametimeFocusLost(FocusEvent e) {
        lblFCYGametimeActionPerformed(new ActionEvent(e.getSource(), 0, ""));
    }

    private void lblFCYRespawnActionPerformed(ActionEvent e) {
        JTextField txt = ((JTextField) e.getSource());
        int respawn = Integer.parseInt(MissionBox.getConfig(MissionBox.FCY_RESPAWN_INTERVAL));
        int value = Tools.parseInt(txt.getText(), 0, Integer.MAX_VALUE, respawn);
        MissionBox.setConfig(MissionBox.FCY_RESPAWN_INTERVAL, Integer.toString(value));
        txt.setText(Integer.toString(value));
    }

    private void lblFCYRespawnFocusLost(FocusEvent e) {
        lblFCYRespawnActionPerformed(new ActionEvent(e.getSource(), 0, ""));
    }

    private void btnRespawnActionPerformed(ActionEvent e) {
        MissionBox.setScheme(MissionBox.MBX_AIRSIREN, "1;1000,1000");
    }

    private void btnTimeSignalActionPerformed(ActionEvent e) {
//        MissionBox.minuteSignal(2);
    }

    private void btnFcyMinus60ActionPerformed(ActionEvent e) {
        fcyCapChange(-60);
    }

    private void btnFcyMinus10ActionPerformed(ActionEvent e) {
        fcyCapChange(-10);
    }

    private void btnFcyPlus10ActionPerformed(ActionEvent e) {
        fcyCapChange(10);
    }

    private void btnFcyPlus60ActionPerformed(ActionEvent e) {
        fcyCapChange(60);
    }

    private void btnFcyGTPlus10ActionPerformed(ActionEvent e) {
        fcyGameTimeChange(10);
    }

    private void btnFcyGTPlus1ActionPerformed(ActionEvent e) {
        fcyGameTimeChange(1);
    }

    private void btnFcyGTMinus1ActionPerformed(ActionEvent e) {
        fcyGameTimeChange(-1);
    }

    private void btnFcyGTMinus10ActionPerformed(ActionEvent e) {
        fcyGameTimeChange(-10);
    }

    private void btnFcyRpwnMinus60ActionPerformed(ActionEvent e) {
        fcyRespawnChange(-60);
    }

    private void btnFcyRpwnMinus10ActionPerformed(ActionEvent e) {
        fcyRespawnChange(-10);
    }

    private void btnFcyRpwnMinus1ActionPerformed(ActionEvent e) {
        fcyRespawnChange(-1);
    }

    private void btnFcyRpwnPlus1ActionPerformed(ActionEvent e) {
        fcyRespawnChange(1);
    }

    private void btnFcyRpwnPlus10ActionPerformed(ActionEvent e) {
        fcyRespawnChange(10);
    }

    private void btnFcyRpwnPlus60ActionPerformed(ActionEvent e) {
        fcyRespawnChange(60);
    }


    private void btnSiren1ActionPerformed(ActionEvent e) {
        MissionBox.setScheme(MissionBox.MBX_SIREN1, txtHandlerPattern.getText().trim());
    }

    private void btnSiren2ActionPerformed(ActionEvent e) {
        MissionBox.setScheme(MissionBox.MBX_SIREN2, txtHandlerPattern.getText().trim());
    }

    private void btnSiren3ActionPerformed(ActionEvent e) {
        //MissionBox.setScheme(MissionBox.MBX_SIREN3, "1;1000,1000");
    }

    private void btnAirSirenActionPerformed(ActionEvent e) {
        MissionBox.setScheme(MissionBox.MBX_AIRSIREN, txtHandlerPattern.getText().trim());
    }

    private void btnClearEventActionPerformed(ActionEvent e) {
        setRevertEvent(null);
    }

    private void btnNoRespawnActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void txtHandlerPatternActionPerformed(ActionEvent e) {

        //MissionBox.getPinHandler().setScheme(MissionBox.MBX_SIREN1, txtHandlerPattern.getText().trim());

    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        tabbedPane1 = new JTabbedPane();
        contentPanel = new JPanel();
        btn1 = new JButton();
        scrollPane2 = new JScrollPane();
        debugPanel4Pins = new JPanel();
        panel8 = new JPanel();
        panel7 = new JScrollPane();
        listEvents = new JPanel();
        panel9 = new JPanel();
        btnClearEvent = new JButton();
        lblRevertEvent = new JLabel();
        panel2 = new JPanel();
        btnRed = new JButton();
        btnGreen = new JButton();
        btnPause = new JButton();
        btn2 = new JButton();
        lblTimer = new JLabel();
        tbDebug = new JToggleButton();
        lblMessage = new JLabel();
        pb1 = new JProgressBar();
        lblRespawn = new JLabel();
        settingsPanel = new JPanel();
        label1 = new JLabel();
        lblFCYCapture = new JTextField();
        panel3 = new JPanel();
        btnFcyPlus60 = new JButton();
        btnFcyPlus10 = new JButton();
        btnFCYcapPlus = new JButton();
        btnFCYcapMinus = new JButton();
        btnFcyMinus10 = new JButton();
        btnFcyMinus60 = new JButton();
        label2 = new JLabel();
        lblFCYGametime = new JTextField();
        panel4 = new JPanel();
        btnFcyGTPlus10 = new JButton();
        btnFcyGTPlus1 = new JButton();
        btnFcyGTMinus1 = new JButton();
        btnFcyGTMinus10 = new JButton();
        label3 = new JLabel();
        lblFCYRespawn = new JTextField();
        panel6 = new JPanel();
        btnFcyRpwnPlus60 = new JButton();
        btnFcyRpwnPlus10 = new JButton();
        btnFcyRpwnPlus1 = new JButton();
        btnFcyRpwnMinus1 = new JButton();
        btnFcyRpwnMinus10 = new JButton();
        btnFcyRpwnMinus60 = new JButton();
        panel5 = new JPanel();
        cmbSirenHandler = new JComboBox();
        panel1 = new JPanel();
        lblButtonGreen = new JLabel();
        btnRelayTest1 = new JButton();
        btnRedLED = new JButton();
        btnSiren1 = new JButton();
        lblButtonRed = new JLabel();
        btnRelayTest2 = new JButton();
        btnGreenLED = new JButton();
        btnSiren2 = new JButton();
        lblButtonPAUSE = new JLabel();
        btnRelayTest3 = new JButton();
        btnRedProgress = new JButton();
        btnSiren3 = new JButton();
        lblButtonStartStop = new JLabel();
        btnRelayTest4 = new JButton();
        btnYellowProgress = new JButton();
        btnAirSiren = new JButton();
        lblButtonQuit = new JLabel();
        btnRelayTest5 = new JButton();
        btnGreenProgress = new JButton();
        btnRespawn = new JButton();
        panel10 = new JPanel();
        label4 = new JLabel();
        txtHandlerPattern = new JTextField();
        btnRelayTest6 = new JButton();
        btnRGBred = new JButton();
        btnTimeSignal = new JButton();
        btnRelayTest7 = new JButton();
        btnRGBgreen = new JButton();
        btnRedLedBar = new JButton();
        btnRelayTest8 = new JButton();
        btnRGBblue = new JButton();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));

        //======== tabbedPane1 ========
        {
            tabbedPane1.setFont(new Font("Dialog", Font.PLAIN, 16));
            tabbedPane1.addChangeListener(e -> tabbedPane1StateChanged(e));

            //======== contentPanel ========
            {
                contentPanel.setLayout(new FormLayout(
                        "pref, $rgap, default, $lcgap, min:grow, $lcgap, pref",
                        "2*(fill:default:grow, $lgap), fill:pref:grow, $lgap, fill:default:grow, 10dlu, $lgap, default"));

                //---- btn1 ----
                btn1.setText("Start / Stop");
                btn1.setIcon(new ImageIcon(getClass().getResource("/artwork/farcry-logo-64.png")));
                btn1.setHorizontalTextPosition(SwingConstants.CENTER);
                btn1.setVerticalTextPosition(SwingConstants.BOTTOM);
                btn1.setFont(new Font("Dialog", Font.BOLD, 18));
                contentPanel.add(btn1, CC.xy(1, 1));

                //======== scrollPane2 ========
                {

                    //======== debugPanel4Pins ========
                    {
                        debugPanel4Pins.setLayout(new BoxLayout(debugPanel4Pins, BoxLayout.PAGE_AXIS));
                    }
                    scrollPane2.setViewportView(debugPanel4Pins);
                }
                contentPanel.add(scrollPane2, CC.xywh(3, 1, 1, 3));

                //======== panel8 ========
                {
                    panel8.setLayout(new BorderLayout());

                    //======== panel7 ========
                    {

                        //======== listEvents ========
                        {
                            listEvents.setLayout(new BoxLayout(listEvents, BoxLayout.PAGE_AXIS));
                        }
                        panel7.setViewportView(listEvents);
                    }
                    panel8.add(panel7, BorderLayout.CENTER);

                    //======== panel9 ========
                    {
                        panel9.setAlignmentX(0.0F);
                        panel9.setLayout(new BorderLayout());

                        //---- btnClearEvent ----
                        btnClearEvent.setText(null);
                        btnClearEvent.setIcon(new ImageIcon(getClass().getResource("/artwork/editdelete.png")));
                        btnClearEvent.addActionListener(e -> btnClearEventActionPerformed(e));
                        panel9.add(btnClearEvent, BorderLayout.LINE_START);

                        //---- lblRevertEvent ----
                        lblRevertEvent.setText("--");
                        lblRevertEvent.setAlignmentX(0.5F);
                        lblRevertEvent.setFont(new Font("Dialog", Font.BOLD, 16));
                        lblRevertEvent.setHorizontalAlignment(SwingConstants.CENTER);
                        panel9.add(lblRevertEvent, BorderLayout.CENTER);
                    }
                    panel8.add(panel9, BorderLayout.SOUTH);
                }
                contentPanel.add(panel8, CC.xywh(5, 1, 1, 3));

                //======== panel2 ========
                {
                    panel2.setLayout(new FormLayout(
                            "default:grow",
                            "fill:default:grow, $lgap, fill:default:grow"));

                    //---- btnRed ----
                    btnRed.setText(null);
                    btnRed.setIcon(new ImageIcon(getClass().getResource("/artwork/ledred64.png")));
                    panel2.add(btnRed, CC.xy(1, 1, CC.FILL, CC.FILL));

                    //---- btnGreen ----
                    btnGreen.setText(null);
                    btnGreen.setIcon(new ImageIcon(getClass().getResource("/artwork/ledgreen64.png")));
                    panel2.add(btnGreen, CC.xy(1, 3, CC.FILL, CC.FILL));
                }
                contentPanel.add(panel2, CC.xywh(7, 1, 1, 7));

                //---- btnPause ----
                btnPause.setText("PAUSE");
                btnPause.setIcon(new ImageIcon(getClass().getResource("/artwork/ledblue64.png")));
                btnPause.setFont(new Font("Dialog", Font.BOLD, 18));
                btnPause.setVerticalTextPosition(SwingConstants.BOTTOM);
                btnPause.setHorizontalTextPosition(SwingConstants.CENTER);
                contentPanel.add(btnPause, CC.xy(1, 3));

                //---- btn2 ----
                btn2.setText(null);
                btn2.setIcon(new ImageIcon(getClass().getResource("/artwork/exit64.png")));
                contentPanel.add(btn2, CC.xy(1, 5));

                //---- lblTimer ----
                lblTimer.setText("--");
                lblTimer.setFont(new Font("Dialog", Font.PLAIN, 12));
                lblTimer.setHorizontalAlignment(SwingConstants.CENTER);
                contentPanel.add(lblTimer, CC.xywh(3, 5, 3, 1, CC.FILL, CC.DEFAULT));

                //---- tbDebug ----
                tbDebug.setText("Debug");
                tbDebug.setFont(new Font("Dialog", Font.BOLD, 18));
                tbDebug.setIcon(new ImageIcon(getClass().getResource("/artwork/circle_grey_32.png")));
                tbDebug.setSelectedIcon(new ImageIcon(getClass().getResource("/artwork/circle_yellow_32.png")));
                contentPanel.add(tbDebug, CC.xy(1, 7, CC.FILL, CC.DEFAULT));

                //---- lblMessage ----
                lblMessage.setText("Initializing...");
                lblMessage.setFont(new Font("Dialog", Font.BOLD, 28));
                lblMessage.setHorizontalAlignment(SwingConstants.CENTER);
                contentPanel.add(lblMessage, CC.xywh(3, 7, 3, 1));
                contentPanel.add(pb1, CC.xywh(1, 8, 7, 1));

                //---- lblRespawn ----
                lblRespawn.setText("--");
                lblRespawn.setFont(new Font("Dialog", Font.PLAIN, 16));
                lblRespawn.setForeground(Color.red);
                contentPanel.add(lblRespawn, CC.xy(7, 10, CC.CENTER, CC.DEFAULT));
            }
            tabbedPane1.addTab("Game", contentPanel);

            //======== settingsPanel ========
            {
                settingsPanel.setLayout(new FormLayout(
                        "2*(pref:grow, $rgap), pref",
                        "3*(default, $lgap), fill:default:grow"));

                //---- label1 ----
                label1.setText("Flaggenzeit (sec)");
                label1.setFont(new Font("Dialog", Font.PLAIN, 16));
                settingsPanel.add(label1, CC.xy(1, 1));

                //---- lblFCYCapture ----
                lblFCYCapture.setFont(new Font("Dialog", Font.BOLD, 20));
                lblFCYCapture.setText("1");
                lblFCYCapture.setHorizontalAlignment(SwingConstants.RIGHT);
                lblFCYCapture.setBackground(Color.orange);
                lblFCYCapture.addActionListener(e -> lblFCYCaptureActionPerformed(e));
                lblFCYCapture.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        lblFCYCaptureFocusLost(e);
                    }
                });
                settingsPanel.add(lblFCYCapture, CC.xy(3, 1, CC.FILL, CC.DEFAULT));

                //======== panel3 ========
                {
                    panel3.setLayout(new BoxLayout(panel3, BoxLayout.LINE_AXIS));

                    //---- btnFcyPlus60 ----
                    btnFcyPlus60.setText("+60");
                    btnFcyPlus60.setFont(new Font("Dialog", Font.BOLD, 16));
                    btnFcyPlus60.addActionListener(e -> btnFcyPlus60ActionPerformed(e));
                    panel3.add(btnFcyPlus60);

                    //---- btnFcyPlus10 ----
                    btnFcyPlus10.setText("+10");
                    btnFcyPlus10.setFont(new Font("Dialog", Font.BOLD, 16));
                    btnFcyPlus10.addActionListener(e -> btnFcyPlus10ActionPerformed(e));
                    panel3.add(btnFcyPlus10);

                    //---- btnFCYcapPlus ----
                    btnFCYcapPlus.setText("+1");
                    btnFCYcapPlus.setFont(new Font("Dialog", Font.BOLD, 16));
                    btnFCYcapPlus.setActionCommand("+");
                    btnFCYcapPlus.addActionListener(e -> btnFCYcapPlusActionPerformed(e));
                    panel3.add(btnFCYcapPlus);

                    //---- btnFCYcapMinus ----
                    btnFCYcapMinus.setText("-1");
                    btnFCYcapMinus.setFont(new Font("Dialog", Font.BOLD, 16));
                    btnFCYcapMinus.addActionListener(e -> btnFCYcapMinusActionPerformed(e));
                    panel3.add(btnFCYcapMinus);

                    //---- btnFcyMinus10 ----
                    btnFcyMinus10.setText("-10");
                    btnFcyMinus10.setFont(new Font("Dialog", Font.BOLD, 16));
                    btnFcyMinus10.addActionListener(e -> btnFcyMinus10ActionPerformed(e));
                    panel3.add(btnFcyMinus10);

                    //---- btnFcyMinus60 ----
                    btnFcyMinus60.setText("-60");
                    btnFcyMinus60.setFont(new Font("Dialog", Font.BOLD, 16));
                    btnFcyMinus60.addActionListener(e -> btnFcyMinus60ActionPerformed(e));
                    panel3.add(btnFcyMinus60);
                }
                settingsPanel.add(panel3, CC.xy(5, 1, CC.CENTER, CC.DEFAULT));

                //---- label2 ----
                label2.setText("Spieldauer (min)");
                label2.setFont(new Font("Dialog", Font.PLAIN, 16));
                settingsPanel.add(label2, CC.xy(1, 3));

                //---- lblFCYGametime ----
                lblFCYGametime.setFont(new Font("Dialog", Font.BOLD, 20));
                lblFCYGametime.setText("1");
                lblFCYGametime.setHorizontalAlignment(SwingConstants.RIGHT);
                lblFCYGametime.setBackground(Color.orange);
                lblFCYGametime.addActionListener(e -> lblFCYGametimeActionPerformed(e));
                lblFCYGametime.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        lblFCYGametimeFocusLost(e);
                    }
                });
                settingsPanel.add(lblFCYGametime, CC.xy(3, 3, CC.FILL, CC.DEFAULT));

                //======== panel4 ========
                {
                    panel4.setLayout(new BoxLayout(panel4, BoxLayout.LINE_AXIS));

                    //---- btnFcyGTPlus10 ----
                    btnFcyGTPlus10.setText("+10");
                    btnFcyGTPlus10.setFont(new Font("Dialog", Font.BOLD, 16));
                    btnFcyGTPlus10.addActionListener(e -> btnFcyGTPlus10ActionPerformed(e));
                    panel4.add(btnFcyGTPlus10);

                    //---- btnFcyGTPlus1 ----
                    btnFcyGTPlus1.setText("+1");
                    btnFcyGTPlus1.setFont(new Font("Dialog", Font.BOLD, 16));
                    btnFcyGTPlus1.setActionCommand("+");
                    btnFcyGTPlus1.addActionListener(e -> btnFcyGTPlus1ActionPerformed(e));
                    panel4.add(btnFcyGTPlus1);

                    //---- btnFcyGTMinus1 ----
                    btnFcyGTMinus1.setText("-1");
                    btnFcyGTMinus1.setFont(new Font("Dialog", Font.BOLD, 16));
                    btnFcyGTMinus1.addActionListener(e -> btnFcyGTMinus1ActionPerformed(e));
                    panel4.add(btnFcyGTMinus1);

                    //---- btnFcyGTMinus10 ----
                    btnFcyGTMinus10.setText("-10");
                    btnFcyGTMinus10.setFont(new Font("Dialog", Font.BOLD, 16));
                    btnFcyGTMinus10.addActionListener(e -> btnFcyGTMinus10ActionPerformed(e));
                    panel4.add(btnFcyGTMinus10);
                }
                settingsPanel.add(panel4, CC.xy(5, 3, CC.CENTER, CC.DEFAULT));

                //---- label3 ----
                label3.setText("respawn (sec)");
                label3.setFont(new Font("Dialog", Font.PLAIN, 16));
                settingsPanel.add(label3, CC.xy(1, 5));

                //---- lblFCYRespawn ----
                lblFCYRespawn.setFont(new Font("Dialog", Font.BOLD, 20));
                lblFCYRespawn.setText("1");
                lblFCYRespawn.setHorizontalAlignment(SwingConstants.RIGHT);
                lblFCYRespawn.setBackground(Color.orange);
                lblFCYRespawn.addActionListener(e -> lblFCYRespawnActionPerformed(e));
                lblFCYRespawn.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        lblFCYRespawnFocusLost(e);
                    }
                });
                settingsPanel.add(lblFCYRespawn, CC.xy(3, 5, CC.FILL, CC.DEFAULT));

                //======== panel6 ========
                {
                    panel6.setLayout(new BoxLayout(panel6, BoxLayout.LINE_AXIS));

                    //---- btnFcyRpwnPlus60 ----
                    btnFcyRpwnPlus60.setText("+60");
                    btnFcyRpwnPlus60.setFont(new Font("Dialog", Font.BOLD, 16));
                    btnFcyRpwnPlus60.addActionListener(e -> btnFcyRpwnPlus60ActionPerformed(e));
                    panel6.add(btnFcyRpwnPlus60);

                    //---- btnFcyRpwnPlus10 ----
                    btnFcyRpwnPlus10.setText("+10");
                    btnFcyRpwnPlus10.setFont(new Font("Dialog", Font.BOLD, 16));
                    btnFcyRpwnPlus10.addActionListener(e -> btnFcyRpwnPlus10ActionPerformed(e));
                    panel6.add(btnFcyRpwnPlus10);

                    //---- btnFcyRpwnPlus1 ----
                    btnFcyRpwnPlus1.setText("+1");
                    btnFcyRpwnPlus1.setFont(new Font("Dialog", Font.BOLD, 16));
                    btnFcyRpwnPlus1.setActionCommand("+");
                    btnFcyRpwnPlus1.addActionListener(e -> btnFcyRpwnPlus1ActionPerformed(e));
                    panel6.add(btnFcyRpwnPlus1);

                    //---- btnFcyRpwnMinus1 ----
                    btnFcyRpwnMinus1.setText("-1");
                    btnFcyRpwnMinus1.setFont(new Font("Dialog", Font.BOLD, 16));
                    btnFcyRpwnMinus1.addActionListener(e -> btnFcyRpwnMinus1ActionPerformed(e));
                    panel6.add(btnFcyRpwnMinus1);

                    //---- btnFcyRpwnMinus10 ----
                    btnFcyRpwnMinus10.setText("-10");
                    btnFcyRpwnMinus10.setFont(new Font("Dialog", Font.BOLD, 16));
                    btnFcyRpwnMinus10.addActionListener(e -> btnFcyRpwnMinus10ActionPerformed(e));
                    panel6.add(btnFcyRpwnMinus10);

                    //---- btnFcyRpwnMinus60 ----
                    btnFcyRpwnMinus60.setText("-60");
                    btnFcyRpwnMinus60.setFont(new Font("Dialog", Font.BOLD, 16));
                    btnFcyRpwnMinus60.addActionListener(e -> btnFcyRpwnMinus60ActionPerformed(e));
                    panel6.add(btnFcyRpwnMinus60);
                }
                settingsPanel.add(panel6, CC.xy(5, 5));

                //======== panel5 ========
                {
                    panel5.setLayout(new GridLayout(3, 2));

                    //---- cmbSirenHandler ----
                    cmbSirenHandler.setFont(new Font("Dialog", Font.BOLD, 16));
                    panel5.add(cmbSirenHandler);
                }
                settingsPanel.add(panel5, CC.xywh(1, 7, 5, 1));
            }
            tabbedPane1.addTab("Settings", settingsPanel);

            //======== panel1 ========
            {
                panel1.setLayout(new FormLayout(
                        "left:82dlu, 3*($ugap, default:grow), $lcgap, default:grow",
                        "8*(default:grow, $lgap), default"));

                //---- lblButtonGreen ----
                lblButtonGreen.setText("Button Green");
                lblButtonGreen.setIcon(new ImageIcon(getClass().getResource("/artwork/ledblue32.png")));
                lblButtonGreen.setDisabledIcon(new ImageIcon(getClass().getResource("/artwork/leddarkblue32.png")));
                lblButtonGreen.setEnabled(false);
                panel1.add(lblButtonGreen, CC.xy(1, 1));

                //---- btnRelayTest1 ----
                btnRelayTest1.setText("Relay1");
                panel1.add(btnRelayTest1, CC.xy(3, 1, CC.FILL, CC.FILL));

                //---- btnRedLED ----
                btnRedLED.setText("LEDred");
                panel1.add(btnRedLED, CC.xy(5, 1, CC.FILL, CC.FILL));

                //---- btnSiren1 ----
                btnSiren1.setText("Siren 1");
                btnSiren1.addActionListener(e -> btnSiren1ActionPerformed(e));
                panel1.add(btnSiren1, CC.xy(7, 1, CC.FILL, CC.FILL));

                //---- lblButtonRed ----
                lblButtonRed.setText("Button RED");
                lblButtonRed.setIcon(new ImageIcon(getClass().getResource("/artwork/ledblue32.png")));
                lblButtonRed.setEnabled(false);
                lblButtonRed.setDisabledIcon(new ImageIcon(getClass().getResource("/artwork/leddarkblue32.png")));
                panel1.add(lblButtonRed, CC.xy(1, 3));

                //---- btnRelayTest2 ----
                btnRelayTest2.setText("Relay2");
                panel1.add(btnRelayTest2, CC.xy(3, 3, CC.FILL, CC.FILL));

                //---- btnGreenLED ----
                btnGreenLED.setText("LEDgreen");
                panel1.add(btnGreenLED, CC.xy(5, 3, CC.FILL, CC.FILL));

                //---- btnSiren2 ----
                btnSiren2.setText("Siren 2");
                btnSiren2.addActionListener(e -> btnSiren2ActionPerformed(e));
                panel1.add(btnSiren2, CC.xy(7, 3, CC.FILL, CC.FILL));

                //---- lblButtonPAUSE ----
                lblButtonPAUSE.setText("Button PAUSE");
                lblButtonPAUSE.setIcon(new ImageIcon(getClass().getResource("/artwork/ledblue32.png")));
                lblButtonPAUSE.setEnabled(false);
                lblButtonPAUSE.setDisabledIcon(new ImageIcon(getClass().getResource("/artwork/leddarkblue32.png")));
                panel1.add(lblButtonPAUSE, CC.xy(1, 5));

                //---- btnRelayTest3 ----
                btnRelayTest3.setText("Relay3");
                panel1.add(btnRelayTest3, CC.xy(3, 5, CC.FILL, CC.FILL));

                //---- btnRedProgress ----
                btnRedProgress.setText("PBred");
                panel1.add(btnRedProgress, CC.xy(5, 5, CC.FILL, CC.FILL));

                //---- btnSiren3 ----
                btnSiren3.setText("Siren 3");
                btnSiren3.addActionListener(e -> btnSiren3ActionPerformed(e));
                panel1.add(btnSiren3, CC.xy(7, 5, CC.FILL, CC.FILL));

                //---- lblButtonStartStop ----
                lblButtonStartStop.setText("Button Start/Stop");
                lblButtonStartStop.setIcon(new ImageIcon(getClass().getResource("/artwork/ledblue32.png")));
                lblButtonStartStop.setEnabled(false);
                lblButtonStartStop.setDisabledIcon(new ImageIcon(getClass().getResource("/artwork/leddarkblue32.png")));
                panel1.add(lblButtonStartStop, CC.xy(1, 7));

                //---- btnRelayTest4 ----
                btnRelayTest4.setText("Relay4");
                panel1.add(btnRelayTest4, CC.xy(3, 7, CC.FILL, CC.FILL));

                //---- btnYellowProgress ----
                btnYellowProgress.setText("PByellow");
                panel1.add(btnYellowProgress, CC.xy(5, 7, CC.FILL, CC.FILL));

                //---- btnAirSiren ----
                btnAirSiren.setText("AirSiren");
                btnAirSiren.addActionListener(e -> btnAirSirenActionPerformed(e));
                panel1.add(btnAirSiren, CC.xy(7, 7, CC.FILL, CC.FILL));

                //---- lblButtonQuit ----
                lblButtonQuit.setText("Button Quit");
                lblButtonQuit.setIcon(new ImageIcon(getClass().getResource("/artwork/ledblue32.png")));
                lblButtonQuit.setEnabled(false);
                lblButtonQuit.setDisabledIcon(new ImageIcon(getClass().getResource("/artwork/leddarkblue32.png")));
                panel1.add(lblButtonQuit, CC.xy(1, 9));

                //---- btnRelayTest5 ----
                btnRelayTest5.setText("Relay5");
                panel1.add(btnRelayTest5, CC.xy(3, 9, CC.FILL, CC.FILL));

                //---- btnGreenProgress ----
                btnGreenProgress.setText("PBgreen");
                btnGreenProgress.setActionCommand("Progress yellow");
                panel1.add(btnGreenProgress, CC.xy(5, 9, CC.FILL, CC.FILL));

                //---- btnRespawn ----
                btnRespawn.setText("Respawn Signal");
                btnRespawn.addActionListener(e -> btnRespawnActionPerformed(e));
                panel1.add(btnRespawn, CC.xy(7, 9, CC.FILL, CC.FILL));

                //======== panel10 ========
                {
                    panel10.setLayout(new BoxLayout(panel10, BoxLayout.PAGE_AXIS));

                    //---- label4 ----
                    label4.setText("Relay Test Scheme");
                    panel10.add(label4);

                    //---- txtHandlerPattern ----
                    txtHandlerPattern.setText("1;1000,1000");
                    txtHandlerPattern.addActionListener(e -> txtHandlerPatternActionPerformed(e));
                    panel10.add(txtHandlerPattern);
                }
                panel1.add(panel10, CC.xy(1, 11));

                //---- btnRelayTest6 ----
                btnRelayTest6.setText("Relay6");
                panel1.add(btnRelayTest6, CC.xy(3, 11, CC.FILL, CC.FILL));

                //---- btnRGBred ----
                btnRGBred.setText("RGBred");
                panel1.add(btnRGBred, CC.xy(5, 11, CC.FILL, CC.FILL));

                //---- btnTimeSignal ----
                btnTimeSignal.setText("2 Minutes");
                btnTimeSignal.addActionListener(e -> btnTimeSignalActionPerformed(e));
                panel1.add(btnTimeSignal, CC.xy(7, 11, CC.FILL, CC.FILL));

                //---- btnRelayTest7 ----
                btnRelayTest7.setText("Relay7");
                panel1.add(btnRelayTest7, CC.xy(3, 13, CC.FILL, CC.FILL));

                //---- btnRGBgreen ----
                btnRGBgreen.setText("RGBgreen");
                panel1.add(btnRGBgreen, CC.xy(5, 13, CC.FILL, CC.FILL));

                //---- btnRedLedBar ----
                btnRedLedBar.setText("30 Seconds");
                btnRedLedBar.addActionListener(e -> btnRedLedBarActionPerformed(e));
                panel1.add(btnRedLedBar, CC.xy(7, 13, CC.FILL, CC.FILL));

                //---- btnRelayTest8 ----
                btnRelayTest8.setText("Relay8");
                panel1.add(btnRelayTest8, CC.xy(3, 15, CC.FILL, CC.FILL));

                //---- btnRGBblue ----
                btnRGBblue.setText("RGBblue");
                panel1.add(btnRGBblue, CC.xy(5, 15, CC.FILL, CC.FILL));
            }
            tabbedPane1.addTab("HW-Test", panel1);
        }
        contentPane.add(tabbedPane1);
        setSize(675, 470);
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    public JButton getBtnRed() {
        return btnRed;
    }

    public JButton getBtnPause() {
        return btnPause;
    }

    public JButton getBtnGreen() {
        return btnGreen;
    }

    public JButton getBtn1() {
        return btn1;
    }

    public JButton getBtn2() {
        return btn2;
    }

    public void setTimer(String time) {
        SwingUtilities.invokeLater(() -> {
            lblTimer.setText("<html>" + time + "</html>");
            revalidate();
            repaint();
        });
    }

    public void setRespawnTimer(String time) {
        SwingUtilities.invokeLater(() -> {
            lblRespawn.setText(time);
            revalidate();
            repaint();
        });
    }


    public void setMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            lblMessage.setText(message);
            revalidate();
            repaint();
        });
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JTabbedPane tabbedPane1;
    private JPanel contentPanel;
    private JButton btn1;
    private JScrollPane scrollPane2;
    private JPanel debugPanel4Pins;
    private JPanel panel8;
    private JScrollPane panel7;
    private JPanel listEvents;
    private JPanel panel9;
    private JButton btnClearEvent;
    private JLabel lblRevertEvent;
    private JPanel panel2;
    private JButton btnRed;
    private JButton btnGreen;
    private JButton btnPause;
    private JButton btn2;
    private JLabel lblTimer;
    private JToggleButton tbDebug;
    private JLabel lblMessage;
    private JProgressBar pb1;
    private JLabel lblRespawn;
    private JPanel settingsPanel;
    private JLabel label1;
    private JTextField lblFCYCapture;
    private JPanel panel3;
    private JButton btnFcyPlus60;
    private JButton btnFcyPlus10;
    private JButton btnFCYcapPlus;
    private JButton btnFCYcapMinus;
    private JButton btnFcyMinus10;
    private JButton btnFcyMinus60;
    private JLabel label2;
    private JTextField lblFCYGametime;
    private JPanel panel4;
    private JButton btnFcyGTPlus10;
    private JButton btnFcyGTPlus1;
    private JButton btnFcyGTMinus1;
    private JButton btnFcyGTMinus10;
    private JLabel label3;
    private JTextField lblFCYRespawn;
    private JPanel panel6;
    private JButton btnFcyRpwnPlus60;
    private JButton btnFcyRpwnPlus10;
    private JButton btnFcyRpwnPlus1;
    private JButton btnFcyRpwnMinus1;
    private JButton btnFcyRpwnMinus10;
    private JButton btnFcyRpwnMinus60;
    private JPanel panel5;
    private JComboBox cmbSirenHandler;
    private JPanel panel1;
    private JLabel lblButtonGreen;
    private JButton btnRelayTest1;
    private JButton btnRedLED;
    private JButton btnSiren1;
    private JLabel lblButtonRed;
    private JButton btnRelayTest2;
    private JButton btnGreenLED;
    private JButton btnSiren2;
    private JLabel lblButtonPAUSE;
    private JButton btnRelayTest3;
    private JButton btnRedProgress;
    private JButton btnSiren3;
    private JLabel lblButtonStartStop;
    private JButton btnRelayTest4;
    private JButton btnYellowProgress;
    private JButton btnAirSiren;
    private JLabel lblButtonQuit;
    private JButton btnRelayTest5;
    private JButton btnGreenProgress;
    private JButton btnRespawn;
    private JPanel panel10;
    private JLabel label4;
    private JTextField txtHandlerPattern;
    private JButton btnRelayTest6;
    private JButton btnRGBred;
    private JButton btnTimeSignal;
    private JButton btnRelayTest7;
    private JButton btnRGBgreen;
    private JButton btnRedLedBar;
    private JButton btnRelayTest8;
    private JButton btnRGBblue;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
