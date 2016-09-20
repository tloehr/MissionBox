/*
 * Created by JFormDesigner on Tue Mar 15 10:21:26 CET 2016
 */

package main;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import interfaces.PercentageInterface;
import misc.Tools;
import org.apache.log4j.Logger;
import progresshandlers.RelaySiren;
import progresshandlers.RelaySirenPulsating;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.util.Date;


/**
 * @author Torsten Löhr
 */
public class FrmTest extends JFrame {
    PercentageInterface[] progressHandlers = new PercentageInterface[]{new RelaySirenPulsating(MissionBox.MBX_SIREN1), new RelaySiren(MissionBox.MBX_SIREN1, MissionBox.MBX_SIREN2, MissionBox.MBX_SIREN3)};
    Logger logger = Logger.getLogger(getClass());


    public FrmTest() {
        initComponents();
        initPanel();
//        setUndecorated(false);
    }

    private void initPanel() {

        btnSiren.addActionListener(e -> MissionBox.getConfig().setProperty(MissionBox.FCY_SIREN, btnSiren.isSelected() ? "true" : "false"));
        btnSound.addActionListener(e -> MissionBox.getConfig().setProperty(MissionBox.FCY_SOUND, btnSound.isSelected() ? "true" : "false"));
        btnMusic.addActionListener(e -> MissionBox.getConfig().setProperty(MissionBox.FCY_MUSIC, btnMusic.isSelected() ? "true" : "false"));
        btnRespawnSignal.addActionListener(e -> MissionBox.getConfig().setProperty(MissionBox.FCY_RESPAWN_SIGNAL, btnRespawnSignal.isSelected() ? "true" : "false"));

        setTitle(MissionBox.getAppinfo().getProperty("program.BUILDDATE") + " [" + MissionBox.getAppinfo().getProperty("program.BUILDNUM") + "]");

        pb1.setVisible(true);

        cmbSirenHandler.setModel(new DefaultComboBoxModel<>(progressHandlers));
        int sirenhandler = MissionBox.getConfig().containsKey(MissionBox.MBX_SIRENHANDLER) ? Integer.parseInt(MissionBox.getConfig().get(MissionBox.MBX_SIRENHANDLER).toString()) : 0;
        cmbSirenHandler.setSelectedIndex(sirenhandler);
        MissionBox.setRelaisSirens(progressHandlers[sirenhandler]);

        cmbSirenHandler.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    MissionBox.setRelaisSirens((PercentageInterface) e.getItem());
                    MissionBox.getConfig().setProperty(MissionBox.MBX_SIRENHANDLER, Integer.toString(cmbSirenHandler.getSelectedIndex()));
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


        btnRelayTest1.setToolTipText("mcp23017-01-B0");
        btnRelayTest2.setToolTipText("mcp23017-01-B1");
        btnRelayTest3.setToolTipText("mcp23017-01-B2");
        btnRelayTest4.setToolTipText("mcp23017-01-B3");
        btnRelayTest5.setToolTipText("mcp23017-01-B4");
        btnRelayTest6.setToolTipText("mcp23017-01-B5");
        btnRelayTest7.setToolTipText("mcp23017-01-B6");
        btnRelayTest8.setToolTipText("mcp23017-01-B7");


    }

    private void relayAction(ActionEvent e) {
        String text = ((JButton) e.getSource()).getText();
        if (text.equalsIgnoreCase("relay1")) {
            MissionBox.setScheme("mcp23017-01-B0", "1;1000,1000");
        } else if (text.equalsIgnoreCase("relay2")) {
            MissionBox.setScheme("mcp23017-01-B1", "1;1000,1000");
        } else if (text.equalsIgnoreCase("relay3")) {
            MissionBox.setScheme("mcp23017-01-B2", "1;1000,1000");
        } else if (text.equalsIgnoreCase("relay4")) {
            MissionBox.setScheme("mcp23017-01-B3", "1;1000,1000");
        } else if (text.equalsIgnoreCase("relay5")) {
            MissionBox.setScheme("mcp23017-01-B4", "1;1000,1000");
        } else if (text.equalsIgnoreCase("relay6")) {
            MissionBox.setScheme("mcp23017-01-B5", "1;1000,1000");
        } else if (text.equalsIgnoreCase("relay7")) {
            MissionBox.setScheme("mcp23017-01-B6", "1;1000,1000");
        } else if (text.equalsIgnoreCase("relay8")) {
            MissionBox.setScheme("mcp23017-01-B7", "1;1000,1000");
        } else if (text.equalsIgnoreCase("LEDred")) {
            MissionBox.setScheme("mcp23017-01-A7", "1;1000,1000");
        } else if (text.equalsIgnoreCase("LEDgreen")) {
            MissionBox.setScheme("mcp23017-01-A6", "1;1000,1000");
        } else if (text.equalsIgnoreCase("PBred")) {
            MissionBox.setScheme("mcp23017-01-A5", "1;1000,1000");
        } else if (text.equalsIgnoreCase("PByellow")) {
            MissionBox.setScheme("mcp23017-01-A4", "1;1000,1000");
        } else if (text.equalsIgnoreCase("PBgreen")) {
            MissionBox.setScheme("mcp23017-01-A3", "1;1000,1000");
        } else if (text.equalsIgnoreCase("RGBred")) {
            MissionBox.setScheme("mcp23017-02-A7", "1;1000,1000");
        } else if (text.equalsIgnoreCase("RGBgreen")) {
            MissionBox.setScheme("mcp23017-02-A6", "1;1000,1000");
        } else if (text.equalsIgnoreCase("RGBblue")) {
            MissionBox.setScheme("mcp23017-02-A5", "1;1000,1000");
        }


    }

    public JButton getBtnUndo() {
        return btnUndo;
    }

    private void btnFCYcapPlusActionPerformed(ActionEvent e) {
        fcyCapChange(1);
    }

    void fcyCapChange(int seconds) {
        int time2capture = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_TIME2CAPTURE));

        if (time2capture + seconds < 1) time2capture = 1;
        else time2capture += seconds;

        final String text = Integer.toString(time2capture);
        MissionBox.getConfig().setProperty(MissionBox.FCY_TIME2CAPTURE, text);
        SwingUtilities.invokeLater(() -> {
            lblFCYCapture.setText(text);
            revalidate();
            repaint();
        });
    }

    void fcyGameTimeChange(int seconds) {
        int gametime = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_GAMETIME));

        if (gametime + seconds < 1) gametime = 1;
        else gametime += seconds;

        final String text = Integer.toString(gametime);
        MissionBox.getConfig().setProperty(MissionBox.FCY_GAMETIME, text);
        SwingUtilities.invokeLater(() -> {
            lblFCYGametime.setText(text);
            revalidate();
            repaint();
        });
    }

    void fcyRespawnChange(int seconds) {
        int respawn = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_RESPAWN));

        if (respawn + seconds < 1) respawn = 1;
        else respawn += seconds;

        final String text = Integer.toString(respawn);
        MissionBox.getConfig().setProperty(MissionBox.FCY_RESPAWN, text);
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

    public void log(String text) {

        if (text == null) {
            txtLog.setText("");
        } else {
            log(0, "", text);
        }
    }

    public void log(long someID, String someText, String text) {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        String newTxt = txtLog.getText() + "\n(" + df.format(new Date()) + ") ";
        newTxt += someID > 0 ? " [" + someID + "] " : "";
        newTxt += !someText.isEmpty() ? " \"" + someText + "\" " : "";
        newTxt += text;
        txtLog.setText(newTxt);
    }


    private void tabbedPane1StateChanged(ChangeEvent e) {
        if (tabbedPane1.getSelectedIndex() == 0) {
            MissionBox.saveLocalProps();
        } else if (tabbedPane1.getSelectedIndex() == 1) {
            lblFCYCapture.setText(MissionBox.getConfig().getProperty(MissionBox.FCY_TIME2CAPTURE));
            lblFCYGametime.setText(MissionBox.getConfig().getProperty(MissionBox.FCY_GAMETIME));
            lblFCYRespawn.setText(MissionBox.getConfig().getProperty(MissionBox.FCY_RESPAWN));
            btnSiren.setSelected(MissionBox.getConfig().getProperty(MissionBox.FCY_SIREN).equals("true"));
            btnSound.setSelected(MissionBox.getConfig().getProperty(MissionBox.FCY_SOUND).equals("true"));
            btnRespawnSignal.setSelected(MissionBox.getConfig().getProperty(MissionBox.FCY_RESPAWN_SIGNAL).equals("true"));
            btnMusic.setSelected(MissionBox.getConfig().getProperty(MissionBox.FCY_MUSIC).equals("true"));
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

    public void enableSettings(boolean yes) {
        tabbedPane1.setEnabledAt(1, yes);
        tabbedPane1.setEnabledAt(2, yes);
    }


    public boolean isGameStartable() {
        return tabbedPane1.getSelectedIndex() == 0;
    }

    private void btnRedLedBarActionPerformed(ActionEvent e) {
        MissionBox.secondsSignal(3);
    }

    private void btnFCYrespawnPlusActionPerformed(ActionEvent e) {
        int respawn = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_RESPAWN));
        respawn++;
        final String text = Integer.toString(respawn);
        MissionBox.getConfig().setProperty(MissionBox.FCY_RESPAWN, text);
        SwingUtilities.invokeLater(() -> {
            lblFCYRespawn.setText(text);
            revalidate();
            repaint();
        });
    }

    private void btnFCYrespawnMinusActionPerformed(ActionEvent e) {
        int respawn = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_RESPAWN));
        respawn--;
        if (respawn == 1) return;
        final String text = Integer.toString(respawn);
        MissionBox.getConfig().setProperty(MissionBox.FCY_RESPAWN, text);
        SwingUtilities.invokeLater(() -> {
            lblFCYRespawn.setText(text);
            revalidate();
            repaint();
        });
    }

    private void lblFCYCaptureActionPerformed(ActionEvent e) {
        JTextField txt = ((JTextField) e.getSource());
        int capture = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_TIME2CAPTURE));
        int value = Tools.parseInt(txt.getText(), 1, Integer.MAX_VALUE, capture);
        MissionBox.getConfig().setProperty(MissionBox.FCY_TIME2CAPTURE, Integer.toString(value));
        txt.setText(Integer.toString(value));
    }

    private void lblFCYCaptureFocusLost(FocusEvent e) {
        lblFCYCaptureActionPerformed(new ActionEvent(e.getSource(), 0, ""));
    }

    private void lblFCYGametimeActionPerformed(ActionEvent e) {
        JTextField txt = ((JTextField) e.getSource());
        int capture = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_GAMETIME));
        int value = Tools.parseInt(txt.getText(), 1, Integer.MAX_VALUE, capture);
        MissionBox.getConfig().setProperty(MissionBox.FCY_GAMETIME, Integer.toString(value));
        txt.setText(Integer.toString(value));
    }

    private void lblFCYGametimeFocusLost(FocusEvent e) {
        lblFCYGametimeActionPerformed(new ActionEvent(e.getSource(), 0, ""));
    }

    private void lblFCYRespawnActionPerformed(ActionEvent e) {
        JTextField txt = ((JTextField) e.getSource());
        int capture = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_RESPAWN));
        int value = Tools.parseInt(txt.getText(), 1, Integer.MAX_VALUE, capture);
        MissionBox.getConfig().setProperty(MissionBox.FCY_RESPAWN, Integer.toString(value));
        txt.setText(Integer.toString(value));
    }

    private void lblFCYRespawnFocusLost(FocusEvent e) {
        lblFCYRespawnActionPerformed(new ActionEvent(e.getSource(), 0, ""));
    }

    private void btnRespawnActionPerformed(ActionEvent e) {
        MissionBox.setScheme("respawnSiren", "1;1000,1000");
    }

    private void btnTimeSignalActionPerformed(ActionEvent e) {
        MissionBox.minuteSignal(4);
    }

    private void btnTestWinnerActionPerformed(ActionEvent e) {
        MissionBox.playWinner();
    }

    private void btnTestLooserActionPerformed(ActionEvent e) {
        MissionBox.playLooser();
    }

    private void btnStopAllActionPerformed(ActionEvent e) {
        MissionBox.stopAllSongs();
    }

    private void btnRelayTestActionPerformed(ActionEvent e) {
//        MissionBox.blink("relay0", 750, 1);
//        MissionBox.blink("relay1", 750, 1);
//        MissionBox.blink("relay2", 750, 1);
//        MissionBox.blink("relay3", 750, 1);
//        MissionBox.blink("relay4", 750, 1);
//        MissionBox.blink("relay5", 750, 1);
//        MissionBox.blink("relay6", 750, 1);
//        MissionBox.blink("relay7", 750, 1);
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


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        tabbedPane1 = new JTabbedPane();
        contentPanel = new JPanel();
        btn1 = new JButton();
        scrollPane2 = new JScrollPane();
        debugPanel4Pins = new JPanel();
        scrollPane1 = new JScrollPane();
        txtLog = new JTextPane();
        panel2 = new JPanel();
        btnRed = new JButton();
        btnGreen = new JButton();
        btnUndo = new JButton();
        btn2 = new JButton();
        pb1 = new JProgressBar();
        lblMessage = new JLabel();
        lblTimer = new JLabel();
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
        btnSiren = new JToggleButton();
        btnSound = new JToggleButton();
        btnRespawnSignal = new JToggleButton();
        btnMusic = new JToggleButton();
        cmbSirenHandler = new JComboBox();
        panel1 = new JPanel();
        lblButtonGreen = new JLabel();
        btnRelayTest1 = new JButton();
        btnRedLED = new JButton();
        btnTestWinner = new JButton();
        lblButtonRed = new JLabel();
        btnRelayTest2 = new JButton();
        btnGreenLED = new JButton();
        btnTestLooser = new JButton();
        lblButtonUNDO = new JLabel();
        btnRelayTest3 = new JButton();
        btnRedProgress = new JButton();
        btnStopAll = new JButton();
        lblButtonStartStop = new JLabel();
        btnRelayTest4 = new JButton();
        btnYellowProgress = new JButton();
        btnRespawn = new JButton();
        lblButtonQuit = new JLabel();
        btnRelayTest5 = new JButton();
        btnGreenProgress = new JButton();
        btnTimeSignal = new JButton();
        btnRelayTest6 = new JButton();
        btnRGBred = new JButton();
        btnRedLedBar = new JButton();
        btnRelayTest7 = new JButton();
        btnRGBgreen = new JButton();
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
                    "2*(fill:default:grow, $lgap), fill:pref:grow, $lgap, 10dlu, $lgap, default"));

                //---- btn1 ----
                btn1.setText("Start / Stop");
                btn1.setIcon(new ImageIcon(getClass().getResource("/artwork/farcry-logo-64.png")));
                btn1.setHorizontalTextPosition(SwingConstants.CENTER);
                btn1.setVerticalTextPosition(SwingConstants.BOTTOM);
                btn1.setFont(new Font("Dialog", Font.BOLD, 20));
                contentPanel.add(btn1, CC.xy(1, 1));

                //======== scrollPane2 ========
                {

                    //======== debugPanel4Pins ========
                    {
                        debugPanel4Pins.setLayout(new BoxLayout(debugPanel4Pins, BoxLayout.PAGE_AXIS));
                    }
                    scrollPane2.setViewportView(debugPanel4Pins);
                }
                contentPanel.add(scrollPane2, CC.xywh(3, 1, 1, 5));

                //======== scrollPane1 ========
                {
                    scrollPane1.setViewportView(txtLog);
                }
                contentPanel.add(scrollPane1, CC.xywh(5, 1, 1, 5));

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
                contentPanel.add(panel2, CC.xywh(7, 1, 1, 5));

                //---- btnUndo ----
                btnUndo.setText("Undo");
                btnUndo.setIcon(new ImageIcon(getClass().getResource("/artwork/ledblue64.png")));
                btnUndo.setFont(new Font("Dialog", Font.BOLD, 20));
                btnUndo.setVerticalTextPosition(SwingConstants.BOTTOM);
                btnUndo.setHorizontalTextPosition(SwingConstants.CENTER);
                contentPanel.add(btnUndo, CC.xy(1, 3));

                //---- btn2 ----
                btn2.setText(null);
                btn2.setIcon(new ImageIcon(getClass().getResource("/artwork/exit64.png")));
                contentPanel.add(btn2, CC.xy(1, 5));
                contentPanel.add(pb1, CC.xywh(1, 7, 7, 1));

                //---- lblMessage ----
                lblMessage.setText("text");
                lblMessage.setFont(new Font("Dialog", Font.PLAIN, 16));
                contentPanel.add(lblMessage, CC.xy(1, 9));

                //---- lblTimer ----
                lblTimer.setText("--");
                lblTimer.setFont(new Font("Dialog", Font.PLAIN, 16));
                contentPanel.add(lblTimer, CC.xy(5, 9, CC.CENTER, CC.DEFAULT));

                //---- lblRespawn ----
                lblRespawn.setText("--");
                lblRespawn.setFont(new Font("Dialog", Font.PLAIN, 16));
                lblRespawn.setForeground(Color.red);
                contentPanel.add(lblRespawn, CC.xy(7, 9, CC.CENTER, CC.DEFAULT));
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

                    //---- btnSiren ----
                    btnSiren.setText("Sirens");
                    btnSiren.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkgreen32.png")));
                    btnSiren.setSelectedIcon(new ImageIcon(getClass().getResource("/artwork/ledgreen32.png")));
                    btnSiren.setFont(new Font("Dialog", Font.BOLD, 16));
                    panel5.add(btnSiren);

                    //---- btnSound ----
                    btnSound.setText("Sound");
                    btnSound.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkgreen32.png")));
                    btnSound.setSelectedIcon(new ImageIcon(getClass().getResource("/artwork/ledgreen32.png")));
                    btnSound.setFont(new Font("Dialog", Font.BOLD, 16));
                    panel5.add(btnSound);

                    //---- btnRespawnSignal ----
                    btnRespawnSignal.setText("Respawn Signal");
                    btnRespawnSignal.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkgreen32.png")));
                    btnRespawnSignal.setSelectedIcon(new ImageIcon(getClass().getResource("/artwork/ledgreen32.png")));
                    btnRespawnSignal.setFont(new Font("Dialog", Font.BOLD, 16));
                    panel5.add(btnRespawnSignal);

                    //---- btnMusic ----
                    btnMusic.setText("Music");
                    btnMusic.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkgreen32.png")));
                    btnMusic.setSelectedIcon(new ImageIcon(getClass().getResource("/artwork/ledgreen32.png")));
                    btnMusic.setFont(new Font("Dialog", Font.BOLD, 16));
                    panel5.add(btnMusic);

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
                    "default, $lcgap, 3*(default, $ugap), default",
                    "8*(default, $lgap), default"));

                //---- lblButtonGreen ----
                lblButtonGreen.setText("Button Green");
                lblButtonGreen.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkblue32.png")));
                panel1.add(lblButtonGreen, CC.xy(3, 1));

                //---- btnRelayTest1 ----
                btnRelayTest1.setText("Relay1");
                panel1.add(btnRelayTest1, CC.xy(5, 1));

                //---- btnRedLED ----
                btnRedLED.setText("LEDred");
                panel1.add(btnRedLED, CC.xy(7, 1));

                //---- btnTestWinner ----
                btnTestWinner.setText("Winner Songs");
                btnTestWinner.addActionListener(e -> btnTestWinnerActionPerformed(e));
                panel1.add(btnTestWinner, CC.xy(9, 1));

                //---- lblButtonRed ----
                lblButtonRed.setText("Button RED");
                lblButtonRed.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkblue32.png")));
                panel1.add(lblButtonRed, CC.xy(3, 3));

                //---- btnRelayTest2 ----
                btnRelayTest2.setText("Relay2");
                panel1.add(btnRelayTest2, CC.xy(5, 3));

                //---- btnGreenLED ----
                btnGreenLED.setText("LEDgreen");
                panel1.add(btnGreenLED, CC.xy(7, 3));

                //---- btnTestLooser ----
                btnTestLooser.setText("Loser Songs");
                btnTestLooser.addActionListener(e -> btnTestLooserActionPerformed(e));
                panel1.add(btnTestLooser, CC.xy(9, 3));

                //---- lblButtonUNDO ----
                lblButtonUNDO.setText("Button UNDO");
                lblButtonUNDO.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkblue32.png")));
                panel1.add(lblButtonUNDO, CC.xy(3, 5));

                //---- btnRelayTest3 ----
                btnRelayTest3.setText("Relay3");
                panel1.add(btnRelayTest3, CC.xy(5, 5));

                //---- btnRedProgress ----
                btnRedProgress.setText("PBred");
                panel1.add(btnRedProgress, CC.xy(7, 5));

                //---- btnStopAll ----
                btnStopAll.setText("Stop All Music");
                btnStopAll.addActionListener(e -> btnStopAllActionPerformed(e));
                panel1.add(btnStopAll, CC.xy(9, 5));

                //---- lblButtonStartStop ----
                lblButtonStartStop.setText("Button Start/Stop");
                lblButtonStartStop.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkblue32.png")));
                panel1.add(lblButtonStartStop, CC.xy(3, 7));

                //---- btnRelayTest4 ----
                btnRelayTest4.setText("Relay4");
                panel1.add(btnRelayTest4, CC.xy(5, 7));

                //---- btnYellowProgress ----
                btnYellowProgress.setText("PByellow");
                panel1.add(btnYellowProgress, CC.xy(7, 7));

                //---- btnRespawn ----
                btnRespawn.setText("Respawn Signal");
                btnRespawn.addActionListener(e -> btnRespawnActionPerformed(e));
                panel1.add(btnRespawn, CC.xy(9, 7));

                //---- lblButtonQuit ----
                lblButtonQuit.setText("Button Quit");
                lblButtonQuit.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkblue32.png")));
                panel1.add(lblButtonQuit, CC.xy(3, 9));

                //---- btnRelayTest5 ----
                btnRelayTest5.setText("Relay5");
                panel1.add(btnRelayTest5, CC.xy(5, 9));

                //---- btnGreenProgress ----
                btnGreenProgress.setText("PBgreen");
                btnGreenProgress.setActionCommand("Progress yellow");
                panel1.add(btnGreenProgress, CC.xy(7, 9));

                //---- btnTimeSignal ----
                btnTimeSignal.setText("4 Minutes");
                btnTimeSignal.addActionListener(e -> btnTimeSignalActionPerformed(e));
                panel1.add(btnTimeSignal, CC.xy(9, 9));

                //---- btnRelayTest6 ----
                btnRelayTest6.setText("Relay6");
                panel1.add(btnRelayTest6, CC.xy(5, 11));

                //---- btnRGBred ----
                btnRGBred.setText("RGBred");
                panel1.add(btnRGBred, CC.xy(7, 11));

                //---- btnRedLedBar ----
                btnRedLedBar.setText("30 Seconds");
                btnRedLedBar.addActionListener(e -> btnRedLedBarActionPerformed(e));
                panel1.add(btnRedLedBar, CC.xy(9, 11));

                //---- btnRelayTest7 ----
                btnRelayTest7.setText("Relay7");
                panel1.add(btnRelayTest7, CC.xy(5, 13));

                //---- btnRGBgreen ----
                btnRGBgreen.setText("RGBgreen");
                panel1.add(btnRGBgreen, CC.xy(7, 13));

                //---- btnRelayTest8 ----
                btnRelayTest8.setText("Relay8");
                panel1.add(btnRelayTest8, CC.xy(5, 15));

                //---- btnRGBblue ----
                btnRGBblue.setText("RGBblue");
                panel1.add(btnRGBblue, CC.xy(7, 15));
            }
            tabbedPane1.addTab("HW-Test", panel1);
        }
        contentPane.add(tabbedPane1);
        setSize(675, 395);
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    public JButton getBtnRed() {
        return btnRed;
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
            lblTimer.setText(time);
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
    private JScrollPane scrollPane1;
    private JTextPane txtLog;
    private JPanel panel2;
    private JButton btnRed;
    private JButton btnGreen;
    private JButton btnUndo;
    private JButton btn2;
    private JProgressBar pb1;
    private JLabel lblMessage;
    private JLabel lblTimer;
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
    private JToggleButton btnSiren;
    private JToggleButton btnSound;
    private JToggleButton btnRespawnSignal;
    private JToggleButton btnMusic;
    private JComboBox cmbSirenHandler;
    private JPanel panel1;
    private JLabel lblButtonGreen;
    private JButton btnRelayTest1;
    private JButton btnRedLED;
    private JButton btnTestWinner;
    private JLabel lblButtonRed;
    private JButton btnRelayTest2;
    private JButton btnGreenLED;
    private JButton btnTestLooser;
    private JLabel lblButtonUNDO;
    private JButton btnRelayTest3;
    private JButton btnRedProgress;
    private JButton btnStopAll;
    private JLabel lblButtonStartStop;
    private JButton btnRelayTest4;
    private JButton btnYellowProgress;
    private JButton btnRespawn;
    private JLabel lblButtonQuit;
    private JButton btnRelayTest5;
    private JButton btnGreenProgress;
    private JButton btnTimeSignal;
    private JButton btnRelayTest6;
    private JButton btnRGBred;
    private JButton btnRedLedBar;
    private JButton btnRelayTest7;
    private JButton btnRGBgreen;
    private JButton btnRelayTest8;
    private JButton btnRGBblue;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
