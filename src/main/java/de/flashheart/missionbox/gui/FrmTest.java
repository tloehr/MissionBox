/*
 * Created by JFormDesigner on Tue Mar 15 10:21:26 CET 2016
 */

package de.flashheart.missionbox.gui;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import de.flashheart.missionbox.Main;
import de.flashheart.missionbox.gamemodes.FC1SavePoint;
import de.flashheart.missionbox.events.GameEventListener;

import de.flashheart.missionbox.misc.Configs;
import de.flashheart.missionbox.misc.Tools;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.math.BigDecimal;
import java.util.ArrayList;


/**
 * @author Torsten Löhr
 */
public class FrmTest extends JFrame implements GameEventListener {

    Logger logger = Logger.getLogger(getClass());

    ArrayList<FC1SavePoint> eventModel = new ArrayList<>();

    public FrmTest() {
        initComponents();
        initPanel();
    }

    @Override
    public void eventSent(FC1SavePoint event) {
        setRevertEvent(event);
    }

    public void addGameEvent(FC1SavePoint event, long remaining) {
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

        // wenn der letzte Event ein "GameOver" bedeutet, macht es keinen Sinn, ein Revert auf den vorherigen
        // Event zu erlauben. Wohin sollte das führen. Ein GameOver tritt durch die Zeit ein und nicht
        // durch eine unfaire Spieler-Aktion (wie z.B. drücken obwohl getroffen).
    }

    public void clearEvents() {
        eventModel.clear();
        listEvents.removeAll();
    }

    public FC1SavePoint getLastEvent() {
        return eventModel.get(eventModel.size() - 1);
    }

    // setzt einen Revert Event fest, zu dem zurückgesprungen werden soll.
    public void setRevertEvent(FC1SavePoint revertEvent) {
        lblRevertEvent.setText(revertEvent == null ? "--" : revertEvent.toHTML());
        lblRevertEvent.setIcon(revertEvent == null ? null : revertEvent.getIcon());
        Main.setRevertEvent(revertEvent);
    }


    private void initPanel() {
        logger.setLevel(Main.getLogLevel());
//        tbDebug.setSelected(MissionBox.getConfig(MissionBox.MBX_DEBUG).equals("true"));
        tbDebug.addItemListener(i -> {
//            MissionBoxgetConfigs().put(MissionBox.MBX_DEBUG, i.getStateChange() == ItemEvent.SELECTED ? "true" : "false");

            SwingUtilities.invokeLater(() -> {
                if (i.getStateChange() == ItemEvent.SELECTED) {
                    ((FormLayout) contentPanel.getLayout()).setColumnSpec(3, ColumnSpec.decode("300dlu"));
                } else {
                    ((FormLayout) contentPanel.getLayout()).setColumnSpec(3, ColumnSpec.decode("0dlu"));
                }
                contentPanel.revalidate();
                contentPanel.repaint();
            });
        });


        SwingUtilities.invokeLater(() -> {
            if (tbDebug.isSelected()) {
                ((FormLayout) contentPanel.getLayout()).setColumnSpec(3, ColumnSpec.decode("300dlu"));
            } else {
                ((FormLayout) contentPanel.getLayout()).setColumnSpec(3, ColumnSpec.decode("0dlu"));
            }
            contentPanel.revalidate();
            contentPanel.repaint();
        });

        setTitle(Main.getConfigs().getApplicationInfo("program.BUILDDATE") + " [" + Main.getConfigs().getApplicationInfo("program.BUILDNUM") + "]");

        pb1.setVisible(true);

        // Events for the Hardware Test
        btnRelayTest1.addActionListener(e -> pinActionHandler(e));
        btnRelayTest2.addActionListener(e -> pinActionHandler(e));
        btnRelayTest3.addActionListener(e -> pinActionHandler(e));
        btnRelayTest4.addActionListener(e -> pinActionHandler(e));
        btnRelayTest5.addActionListener(e -> pinActionHandler(e));
        btnRelayTest6.addActionListener(e -> pinActionHandler(e));
        btnRelayTest7.addActionListener(e -> pinActionHandler(e));
        btnRelayTest8.addActionListener(e -> pinActionHandler(e));

        btnRedLED1.addActionListener(e -> pinActionHandler(e));
        btnGreenLED1.addActionListener(e -> pinActionHandler(e));

        btnRedLED2.addActionListener(e -> pinActionHandler(e));
        btnGreenLED2.addActionListener(e -> pinActionHandler(e));

        btnRedProgress1.addActionListener(e -> pinActionHandler(e));
        btnYellowProgress1.addActionListener(e -> pinActionHandler(e));
        btnGreenProgress1.addActionListener(e -> pinActionHandler(e));

        btnRedProgress2.addActionListener(e -> pinActionHandler(e));
        btnYellowProgress2.addActionListener(e -> pinActionHandler(e));
        btnGreenProgress2.addActionListener(e -> pinActionHandler(e));

        btnSiren1.addActionListener(e -> pinActionHandler(e));
        btnSiren2.addActionListener(e -> pinActionHandler(e));
        btnSiren3.addActionListener(e -> pinActionHandler(e));
        btnSiren4.addActionListener(e -> pinActionHandler(e));

        debugPanel4Pins.add(new JLabel("yipiieee"));
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
    private void pinActionHandler(ActionEvent e) {
        String text = ((JButton) e.getSource()).getText();
        logger.debug(text);
        Main.getPinHandler().setScheme(text, txtHandlerPattern.getText().trim());
    }


    private void btnFCYcapPlusActionPerformed(ActionEvent e) {
        fcyCapChange(1);
    }

    void fcyCapChange(int seconds) {
        int time2capture = Integer.parseInt(Main.getConfigs().get(Configs.FCY_TIME2CAPTURE));

        if (time2capture + seconds < 1) time2capture = 1;
        else time2capture += seconds;

        final String text = Integer.toString(time2capture);
        Main.getConfigs().put(Configs.FCY_TIME2CAPTURE, text);
        SwingUtilities.invokeLater(() -> {
            lblFCYCapture.setText(text);
            revalidate();
            repaint();
        });
    }

    void fcyGameTimeChange(int seconds) {
        int gametime = Integer.parseInt(Main.getConfigs().get(Configs.FCY_GAMETIME));

        if (gametime + seconds < 1) gametime = 1;
        else gametime += seconds;

        final String text = Integer.toString(gametime);
        Main.getConfigs().put(Configs.FCY_GAMETIME, text);

        SwingUtilities.invokeLater(() -> {
            lblFCYGametime.setText(text);
            revalidate();
            repaint();
        });
    }

    void fcyRespawnChange(int seconds) {
        long respawn = Long.parseLong(Main.getConfigs().get(Configs.FCY_RESPAWN_INTERVAL));

        if (respawn + seconds < 0) respawn = 0;
        else respawn += seconds;

        final String text = Long.toString(respawn);
        Main.getConfigs().put(Configs.FCY_RESPAWN_INTERVAL, text);
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


    private void tabbedPane1StateChanged(ChangeEvent e) {
        if (tabbedPane1.getSelectedIndex() == 0) {
            Main.prepareGame(); // zurück zum Pregame Mode
        } else if (tabbedPane1.getSelectedIndex() == 1) {
            Main.getPinHandler().off();
            lblFCYCapture.setText(Main.getConfigs().get(Configs.FCY_TIME2CAPTURE));
            lblFCYGametime.setText(Main.getConfigs().get(Configs.FCY_GAMETIME));
            lblFCYRespawn.setText(Main.getConfigs().get(Configs.FCY_RESPAWN_INTERVAL));
            lblRspwnSiren.setText(Main.getConfigs().get(Configs.MBX_RESPAWN_SIRENTIME));
            lblStartsiren.setText(Main.getConfigs().get(Configs.MBX_STARTGAME_SIRENTIME));
        } else {
            Main.getPinHandler().off();
        }
    }

    public void setProgress(long start, long now, long end) {

        BigDecimal progress = new BigDecimal(now - start).divide(new BigDecimal(end - start), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
        setProgress(progress.intValue());
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
        int capture = Integer.parseInt(Main.getConfigs().get(Configs.FCY_TIME2CAPTURE));
        int value = Tools.parseInt(txt.getText(), 0, Integer.MAX_VALUE, capture);
        Main.getConfigs().put(Configs.FCY_TIME2CAPTURE, Integer.toString(value));
        txt.setText(Integer.toString(value));
    }

    private void lblFCYCaptureFocusLost(FocusEvent e) {
        lblFCYCaptureActionPerformed(new ActionEvent(e.getSource(), 0, ""));
    }

    private void lblFCYGametimeActionPerformed(ActionEvent e) {
        JTextField txt = ((JTextField) e.getSource());
        int capture = Integer.parseInt(Main.getConfigs().get(Configs.FCY_GAMETIME));
        int value = Tools.parseInt(txt.getText(), 1, Integer.MAX_VALUE, capture);
        Main.getConfigs().put(Configs.FCY_GAMETIME, Integer.toString(value));
        txt.setText(Integer.toString(value));
    }

    private void lblFCYGametimeFocusLost(FocusEvent e) {
        lblFCYGametimeActionPerformed(new ActionEvent(e.getSource(), 0, ""));
    }

    private void lblFCYRespawnActionPerformed(ActionEvent e) {
        JTextField txt = ((JTextField) e.getSource());
        int respawn = Integer.parseInt(Main.getConfigs().get(Configs.FCY_RESPAWN_INTERVAL));
        int value = Tools.parseInt(txt.getText(), 0, Integer.MAX_VALUE, respawn);
        Main.getConfigs().put(Configs.FCY_RESPAWN_INTERVAL, Integer.toString(value));
        txt.setText(Integer.toString(value));
    }

    private void lblFCYRespawnFocusLost(FocusEvent e) {
        lblFCYRespawnActionPerformed(new ActionEvent(e.getSource(), 0, ""));
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

    private void btnClearEventActionPerformed(ActionEvent e) {
        setRevertEvent(null);
    }


    private void btnRelaySirensActionPerformed(ActionEvent e) {
        BigDecimal bd = new BigDecimal(txtPercentage.getText().trim());
        logger.debug("intvalue: " + bd.intValue() / 10);
        Main.getRelaisSirens().setValue(bd);
    }


    private void lblRspwnSirenFocusLost(FocusEvent e) {
        JTextField txt = ((JTextField) e.getSource());

        long respawnsiren = Long.parseLong(Main.getConfigs().get(Configs.MBX_RESPAWN_SIRENTIME));
        long value = Tools.parseLong(txt.getText(), 0, Long.MAX_VALUE, respawnsiren);

        Main.getConfigs().put(Configs.MBX_RESPAWN_SIRENTIME, Long.toString(value)); // speichern als millis
        txt.setText(Long.toString(value));
    }


    private void lblStartsirenFocusLost(FocusEvent e) {
        JTextField txt = ((JTextField) e.getSource());

        long startsiren = Long.parseLong(Main.getConfigs().get(Configs.MBX_STARTGAME_SIRENTIME));
        long value = Tools.parseLong(txt.getText(), 0, Long.MAX_VALUE, startsiren);

        Main.getConfigs().put(Configs.MBX_STARTGAME_SIRENTIME, Long.toString(value)); // speichern als millis
        txt.setText(Long.toString(value));
    }


    private void btnRedProgressActionPerformed(ActionEvent e) {
        Main.getPinHandler().setScheme(Main.NAME_LED1_PROGRESS_RED, txtHandlerPattern.getText().trim());
    }

    private void btnYellowProgressActionPerformed(ActionEvent e) {
        Main.getPinHandler().setScheme(Main.NAME_LED1_PROGRESS_YELLOW, txtHandlerPattern.getText().trim());
    }

    private void btnGreenProgressActionPerformed(ActionEvent e) {
        Main.getPinHandler().setScheme(Main.NAME_LED1_PROGRESS_GREEN, txtHandlerPattern.getText().trim());
    }

    private void tbUsePinHandlerItemStateChanged(ItemEvent e) {
        Main.getPinHandler().off();
    }

    private void btnProgessActionPerformed(ActionEvent e) {
        BigDecimal bd = new BigDecimal(txtPercentage.getText().trim());
        logger.debug("intvalue: " + bd.intValue() / 10);
        Main.setPBLeds(bd);
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
        panel13 = new JPanel();
        lblTimer = new JLabel();
        lblMessage = new JLabel();
        tbDebug = new JToggleButton();
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
        label7 = new JLabel();
        lblRspwnSiren = new JTextField();
        label6 = new JLabel();
        lblStartsiren = new JTextField();
        panel5 = new JPanel();
        panel1 = new JPanel();
        lblButtonGreen = new JLabel();
        btnRelayTest1 = new JButton();
        btnRedLED1 = new JButton();
        btnRedLED2 = new JButton();
        lblButtonRed = new JLabel();
        btnRelayTest2 = new JButton();
        btnGreenLED1 = new JButton();
        btnGreenLED2 = new JButton();
        lblButtonPAUSE = new JLabel();
        btnRelayTest3 = new JButton();
        btnRedProgress1 = new JButton();
        btnSiren1 = new JButton();
        lblButtonStartStop = new JLabel();
        btnRelayTest4 = new JButton();
        btnYellowProgress1 = new JButton();
        btnSiren2 = new JButton();
        btnRelayTest5 = new JButton();
        btnGreenProgress1 = new JButton();
        btnSiren3 = new JButton();
        panel10 = new JPanel();
        label4 = new JLabel();
        txtHandlerPattern = new JTextField();
        btnRelayTest6 = new JButton();
        btnRedProgress2 = new JButton();
        btnSiren4 = new JButton();
        tbUsePinHandler = new JToggleButton();
        btnRelayTest7 = new JButton();
        btnYellowProgress2 = new JButton();
        panel11 = new JPanel();
        label5 = new JLabel();
        txtPercentage = new JTextField();
        btnRelayTest8 = new JButton();
        btnGreenProgress2 = new JButton();
        panel12 = new JPanel();
        btnRelaySirens = new JButton();
        btnProgess = new JButton();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));

        //======== tabbedPane1 ========
        {
            tabbedPane1.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
            tabbedPane1.addChangeListener(e -> tabbedPane1StateChanged(e));

            //======== contentPanel ========
            {
                contentPanel.setLayout(new FormLayout(
                    "pref, $rgap, pref, $lcgap, min:grow, $lcgap, pref",
                    "2*(fill:default:grow, $lgap), fill:pref:grow, $lgap, fill:default:grow, 10dlu, $lgap, default"));

                //---- btn1 ----
                btn1.setText("Start / Stop");
                btn1.setIcon(new ImageIcon(getClass().getResource("/artwork/farcry-logo-64.png")));
                btn1.setHorizontalTextPosition(SwingConstants.CENTER);
                btn1.setVerticalTextPosition(SwingConstants.BOTTOM);
                btn1.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
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
                        lblRevertEvent.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
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
                btnPause.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
                btnPause.setVerticalTextPosition(SwingConstants.BOTTOM);
                btnPause.setHorizontalTextPosition(SwingConstants.CENTER);
                contentPanel.add(btnPause, CC.xy(1, 3));

                //---- btn2 ----
                btn2.setText(null);
                btn2.setIcon(new ImageIcon(getClass().getResource("/artwork/exit64.png")));
                contentPanel.add(btn2, CC.xy(1, 5));

                //======== panel13 ========
                {
                    panel13.setLayout(new BoxLayout(panel13, BoxLayout.PAGE_AXIS));

                    //---- lblTimer ----
                    lblTimer.setText("--");
                    lblTimer.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
                    lblTimer.setHorizontalAlignment(SwingConstants.CENTER);
                    panel13.add(lblTimer);

                    //---- lblMessage ----
                    lblMessage.setText("<html><h1>Initializing...</h1></html>");
                    lblMessage.setHorizontalAlignment(SwingConstants.CENTER);
                    panel13.add(lblMessage);
                }
                contentPanel.add(panel13, CC.xywh(3, 5, 3, 3, CC.FILL, CC.DEFAULT));

                //---- tbDebug ----
                tbDebug.setText("Debug");
                tbDebug.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
                tbDebug.setIcon(new ImageIcon(getClass().getResource("/artwork/circle_grey_32.png")));
                tbDebug.setSelectedIcon(new ImageIcon(getClass().getResource("/artwork/circle_yellow_32.png")));
                contentPanel.add(tbDebug, CC.xy(1, 7, CC.FILL, CC.DEFAULT));
                contentPanel.add(pb1, CC.xywh(1, 8, 7, 1));

                //---- lblRespawn ----
                lblRespawn.setText("--");
                lblRespawn.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
                lblRespawn.setForeground(Color.red);
                contentPanel.add(lblRespawn, CC.xy(7, 10, CC.CENTER, CC.DEFAULT));
            }
            tabbedPane1.addTab("Game", contentPanel);

            //======== settingsPanel ========
            {
                settingsPanel.setLayout(new FormLayout(
                    "left:70dlu:grow, $rgap, pref:grow, $rgap, pref",
                    "6*(default, $lgap), fill:default:grow, $lgap, default"));

                //---- label1 ----
                label1.setText("Eroberungszeit (Sek.)");
                label1.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
                settingsPanel.add(label1, CC.xy(1, 1));

                //---- lblFCYCapture ----
                lblFCYCapture.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
                lblFCYCapture.setText("1");
                lblFCYCapture.setHorizontalAlignment(SwingConstants.LEFT);
                lblFCYCapture.setBackground(Color.orange);
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
                    btnFcyPlus60.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
                    btnFcyPlus60.addActionListener(e -> btnFcyPlus60ActionPerformed(e));
                    panel3.add(btnFcyPlus60);

                    //---- btnFcyPlus10 ----
                    btnFcyPlus10.setText("+10");
                    btnFcyPlus10.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
                    btnFcyPlus10.addActionListener(e -> btnFcyPlus10ActionPerformed(e));
                    panel3.add(btnFcyPlus10);

                    //---- btnFCYcapPlus ----
                    btnFCYcapPlus.setText("+1");
                    btnFCYcapPlus.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
                    btnFCYcapPlus.setActionCommand("+");
                    btnFCYcapPlus.addActionListener(e -> btnFCYcapPlusActionPerformed(e));
                    panel3.add(btnFCYcapPlus);

                    //---- btnFCYcapMinus ----
                    btnFCYcapMinus.setText("-1");
                    btnFCYcapMinus.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
                    btnFCYcapMinus.addActionListener(e -> btnFCYcapMinusActionPerformed(e));
                    panel3.add(btnFCYcapMinus);

                    //---- btnFcyMinus10 ----
                    btnFcyMinus10.setText("-10");
                    btnFcyMinus10.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
                    btnFcyMinus10.addActionListener(e -> btnFcyMinus10ActionPerformed(e));
                    panel3.add(btnFcyMinus10);

                    //---- btnFcyMinus60 ----
                    btnFcyMinus60.setText("-60");
                    btnFcyMinus60.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
                    btnFcyMinus60.addActionListener(e -> btnFcyMinus60ActionPerformed(e));
                    panel3.add(btnFcyMinus60);
                }
                settingsPanel.add(panel3, CC.xy(5, 1, CC.CENTER, CC.DEFAULT));

                //---- label2 ----
                label2.setText("Spielzeit (Minuten)");
                label2.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
                settingsPanel.add(label2, CC.xy(1, 3));

                //---- lblFCYGametime ----
                lblFCYGametime.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
                lblFCYGametime.setText("1");
                lblFCYGametime.setHorizontalAlignment(SwingConstants.LEFT);
                lblFCYGametime.setBackground(Color.orange);
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
                    btnFcyGTPlus10.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
                    btnFcyGTPlus10.addActionListener(e -> btnFcyGTPlus10ActionPerformed(e));
                    panel4.add(btnFcyGTPlus10);

                    //---- btnFcyGTPlus1 ----
                    btnFcyGTPlus1.setText("+1");
                    btnFcyGTPlus1.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
                    btnFcyGTPlus1.setActionCommand("+");
                    btnFcyGTPlus1.addActionListener(e -> btnFcyGTPlus1ActionPerformed(e));
                    panel4.add(btnFcyGTPlus1);

                    //---- btnFcyGTMinus1 ----
                    btnFcyGTMinus1.setText("-1");
                    btnFcyGTMinus1.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
                    btnFcyGTMinus1.addActionListener(e -> btnFcyGTMinus1ActionPerformed(e));
                    panel4.add(btnFcyGTMinus1);

                    //---- btnFcyGTMinus10 ----
                    btnFcyGTMinus10.setText("-10");
                    btnFcyGTMinus10.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
                    btnFcyGTMinus10.addActionListener(e -> btnFcyGTMinus10ActionPerformed(e));
                    panel4.add(btnFcyGTMinus10);
                }
                settingsPanel.add(panel4, CC.xy(5, 3, CC.CENTER, CC.DEFAULT));

                //---- label3 ----
                label3.setText("Respawn (Sekunden)");
                label3.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
                settingsPanel.add(label3, CC.xy(1, 5));

                //---- lblFCYRespawn ----
                lblFCYRespawn.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
                lblFCYRespawn.setText("1");
                lblFCYRespawn.setHorizontalAlignment(SwingConstants.LEFT);
                lblFCYRespawn.setBackground(Color.orange);
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
                    btnFcyRpwnPlus60.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
                    btnFcyRpwnPlus60.addActionListener(e -> btnFcyRpwnPlus60ActionPerformed(e));
                    panel6.add(btnFcyRpwnPlus60);

                    //---- btnFcyRpwnPlus10 ----
                    btnFcyRpwnPlus10.setText("+10");
                    btnFcyRpwnPlus10.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
                    btnFcyRpwnPlus10.addActionListener(e -> btnFcyRpwnPlus10ActionPerformed(e));
                    panel6.add(btnFcyRpwnPlus10);

                    //---- btnFcyRpwnPlus1 ----
                    btnFcyRpwnPlus1.setText("+1");
                    btnFcyRpwnPlus1.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
                    btnFcyRpwnPlus1.setActionCommand("+");
                    btnFcyRpwnPlus1.addActionListener(e -> btnFcyRpwnPlus1ActionPerformed(e));
                    panel6.add(btnFcyRpwnPlus1);

                    //---- btnFcyRpwnMinus1 ----
                    btnFcyRpwnMinus1.setText("-1");
                    btnFcyRpwnMinus1.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
                    btnFcyRpwnMinus1.addActionListener(e -> btnFcyRpwnMinus1ActionPerformed(e));
                    panel6.add(btnFcyRpwnMinus1);

                    //---- btnFcyRpwnMinus10 ----
                    btnFcyRpwnMinus10.setText("-10");
                    btnFcyRpwnMinus10.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
                    btnFcyRpwnMinus10.addActionListener(e -> btnFcyRpwnMinus10ActionPerformed(e));
                    panel6.add(btnFcyRpwnMinus10);

                    //---- btnFcyRpwnMinus60 ----
                    btnFcyRpwnMinus60.setText("-60");
                    btnFcyRpwnMinus60.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
                    btnFcyRpwnMinus60.addActionListener(e -> btnFcyRpwnMinus60ActionPerformed(e));
                    panel6.add(btnFcyRpwnMinus60);
                }
                settingsPanel.add(panel6, CC.xy(5, 5));

                //---- label7 ----
                label7.setText("Respawnsirene (millis)");
                label7.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
                settingsPanel.add(label7, CC.xy(1, 7));

                //---- lblRspwnSiren ----
                lblRspwnSiren.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
                lblRspwnSiren.setText("1");
                lblRspwnSiren.setHorizontalAlignment(SwingConstants.LEFT);
                lblRspwnSiren.setBackground(Color.orange);
                lblRspwnSiren.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        lblRspwnSirenFocusLost(e);
                    }
                });
                settingsPanel.add(lblRspwnSiren, CC.xywh(3, 7, 3, 1, CC.FILL, CC.DEFAULT));

                //---- label6 ----
                label6.setText("Startsirene (millis)");
                label6.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
                settingsPanel.add(label6, CC.xy(1, 9));

                //---- lblStartsiren ----
                lblStartsiren.setFont(new Font(Font.DIALOG, Font.BOLD, 20));
                lblStartsiren.setText("1");
                lblStartsiren.setHorizontalAlignment(SwingConstants.LEFT);
                lblStartsiren.setBackground(Color.orange);
                lblStartsiren.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        lblStartsirenFocusLost(e);
                    }
                });
                settingsPanel.add(lblStartsiren, CC.xywh(3, 9, 3, 1, CC.FILL, CC.DEFAULT));

                //======== panel5 ========
                {
                    panel5.setLayout(new GridLayout(3, 2));
                }
                settingsPanel.add(panel5, CC.xywh(1, 13, 5, 1));
            }
            tabbedPane1.addTab("Settings", settingsPanel);

            //======== panel1 ========
            {
                panel1.setLayout(new FormLayout(
                    "left:82dlu, 2*($ugap, default:grow), $ugap, 69dlu:grow, $lcgap, default:grow",
                    "8*(default:grow, $lgap), default"));

                //---- lblButtonGreen ----
                lblButtonGreen.setText("Button Green");
                lblButtonGreen.setIcon(new ImageIcon(getClass().getResource("/artwork/ledblue32.png")));
                lblButtonGreen.setDisabledIcon(new ImageIcon(getClass().getResource("/artwork/leddarkblue32.png")));
                lblButtonGreen.setEnabled(false);
                panel1.add(lblButtonGreen, CC.xy(1, 1));

                //---- btnRelayTest1 ----
                btnRelayTest1.setText("relay1");
                panel1.add(btnRelayTest1, CC.xy(3, 1, CC.FILL, CC.FILL));

                //---- btnRedLED1 ----
                btnRedLED1.setText("btnRedLED1");
                panel1.add(btnRedLED1, CC.xy(5, 1, CC.FILL, CC.FILL));

                //---- btnRedLED2 ----
                btnRedLED2.setText("btnRedLED2");
                panel1.add(btnRedLED2, CC.xy(7, 1, CC.FILL, CC.FILL));

                //---- lblButtonRed ----
                lblButtonRed.setText("Button RED");
                lblButtonRed.setIcon(new ImageIcon(getClass().getResource("/artwork/ledblue32.png")));
                lblButtonRed.setEnabled(false);
                lblButtonRed.setDisabledIcon(new ImageIcon(getClass().getResource("/artwork/leddarkblue32.png")));
                panel1.add(lblButtonRed, CC.xy(1, 3));

                //---- btnRelayTest2 ----
                btnRelayTest2.setText("relay2");
                panel1.add(btnRelayTest2, CC.xy(3, 3, CC.FILL, CC.FILL));

                //---- btnGreenLED1 ----
                btnGreenLED1.setText("btnGreenLED1");
                panel1.add(btnGreenLED1, CC.xy(5, 3, CC.FILL, CC.FILL));

                //---- btnGreenLED2 ----
                btnGreenLED2.setText("btnGreenLED2");
                panel1.add(btnGreenLED2, CC.xy(7, 3, CC.FILL, CC.FILL));

                //---- lblButtonPAUSE ----
                lblButtonPAUSE.setText("Button PAUSE");
                lblButtonPAUSE.setIcon(new ImageIcon(getClass().getResource("/artwork/ledblue32.png")));
                lblButtonPAUSE.setEnabled(false);
                lblButtonPAUSE.setDisabledIcon(new ImageIcon(getClass().getResource("/artwork/leddarkblue32.png")));
                panel1.add(lblButtonPAUSE, CC.xy(1, 5));

                //---- btnRelayTest3 ----
                btnRelayTest3.setText("relay3");
                panel1.add(btnRelayTest3, CC.xy(3, 5, CC.FILL, CC.FILL));

                //---- btnRedProgress1 ----
                btnRedProgress1.setText("btnGreenLED1");
                btnRedProgress1.addActionListener(e -> btnRedProgressActionPerformed(e));
                panel1.add(btnRedProgress1, CC.xy(5, 5, CC.FILL, CC.FILL));

                //---- btnSiren1 ----
                btnSiren1.setText("btnSiren1");
                panel1.add(btnSiren1, CC.xy(7, 5, CC.FILL, CC.FILL));

                //---- lblButtonStartStop ----
                lblButtonStartStop.setText("Button Start/Stop");
                lblButtonStartStop.setIcon(new ImageIcon(getClass().getResource("/artwork/ledblue32.png")));
                lblButtonStartStop.setEnabled(false);
                lblButtonStartStop.setDisabledIcon(new ImageIcon(getClass().getResource("/artwork/leddarkblue32.png")));
                panel1.add(lblButtonStartStop, CC.xy(1, 7));

                //---- btnRelayTest4 ----
                btnRelayTest4.setText("relay4");
                panel1.add(btnRelayTest4, CC.xy(3, 7, CC.FILL, CC.FILL));

                //---- btnYellowProgress1 ----
                btnYellowProgress1.setText("btnYellowProgress1");
                btnYellowProgress1.addActionListener(e -> btnYellowProgressActionPerformed(e));
                panel1.add(btnYellowProgress1, CC.xy(5, 7, CC.FILL, CC.FILL));

                //---- btnSiren2 ----
                btnSiren2.setText("btnSiren2");
                panel1.add(btnSiren2, CC.xy(7, 7, CC.FILL, CC.FILL));

                //---- btnRelayTest5 ----
                btnRelayTest5.setText("relay5");
                panel1.add(btnRelayTest5, CC.xy(3, 9, CC.FILL, CC.FILL));

                //---- btnGreenProgress1 ----
                btnGreenProgress1.setText("btnGreenProgress1");
                btnGreenProgress1.setActionCommand("Progress yellow");
                btnGreenProgress1.addActionListener(e -> btnGreenProgressActionPerformed(e));
                panel1.add(btnGreenProgress1, CC.xy(5, 9, CC.FILL, CC.FILL));

                //---- btnSiren3 ----
                btnSiren3.setText("btnSiren3");
                panel1.add(btnSiren3, CC.xy(7, 9, CC.FILL, CC.FILL));

                //======== panel10 ========
                {
                    panel10.setLayout(new BoxLayout(panel10, BoxLayout.PAGE_AXIS));

                    //---- label4 ----
                    label4.setText("Relay Test Scheme");
                    panel10.add(label4);

                    //---- txtHandlerPattern ----
                    txtHandlerPattern.setText("1;1000,1000");
                    panel10.add(txtHandlerPattern);
                }
                panel1.add(panel10, CC.xy(1, 11));

                //---- btnRelayTest6 ----
                btnRelayTest6.setText("relay6");
                panel1.add(btnRelayTest6, CC.xy(3, 11, CC.FILL, CC.FILL));

                //---- btnRedProgress2 ----
                btnRedProgress2.setText("btnRedProgress2");
                panel1.add(btnRedProgress2, CC.xy(5, 11, CC.FILL, CC.FILL));

                //---- btnSiren4 ----
                btnSiren4.setText("btnSiren4");
                panel1.add(btnSiren4, CC.xy(7, 11, CC.FILL, CC.FILL));

                //---- tbUsePinHandler ----
                tbUsePinHandler.setText("Use Pinhandler");
                tbUsePinHandler.setSelected(true);
                tbUsePinHandler.setEnabled(false);
                tbUsePinHandler.addItemListener(e -> tbUsePinHandlerItemStateChanged(e));
                panel1.add(tbUsePinHandler, CC.xy(1, 13, CC.FILL, CC.FILL));

                //---- btnRelayTest7 ----
                btnRelayTest7.setText("relay7");
                panel1.add(btnRelayTest7, CC.xy(3, 13, CC.FILL, CC.FILL));

                //---- btnYellowProgress2 ----
                btnYellowProgress2.setText("btnYellowProgress2");
                panel1.add(btnYellowProgress2, CC.xy(5, 13, CC.FILL, CC.FILL));

                //======== panel11 ========
                {
                    panel11.setLayout(new BoxLayout(panel11, BoxLayout.PAGE_AXIS));

                    //---- label5 ----
                    label5.setText("Percentage");
                    panel11.add(label5);

                    //---- txtPercentage ----
                    txtPercentage.setText("50");
                    panel11.add(txtPercentage);
                }
                panel1.add(panel11, CC.xy(7, 13));

                //---- btnRelayTest8 ----
                btnRelayTest8.setText("relay8");
                panel1.add(btnRelayTest8, CC.xy(3, 15, CC.FILL, CC.FILL));

                //---- btnGreenProgress2 ----
                btnGreenProgress2.setText("btnGreenProgress2");
                panel1.add(btnGreenProgress2, CC.xy(5, 15, CC.FILL, CC.FILL));

                //======== panel12 ========
                {
                    panel12.setLayout(new BoxLayout(panel12, BoxLayout.PAGE_AXIS));

                    //---- btnRelaySirens ----
                    btnRelaySirens.setText("btnRelaySirens");
                    btnRelaySirens.addActionListener(e -> btnRelaySirensActionPerformed(e));
                    panel12.add(btnRelaySirens);

                    //---- btnProgess ----
                    btnProgess.setText("btnProgess");
                    btnProgess.addActionListener(e -> btnProgessActionPerformed(e));
                    panel12.add(btnProgess);
                }
                panel1.add(panel12, CC.xy(7, 15));
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
        if (tbDebug.isSelected()) {
            SwingUtilities.invokeLater(() -> {
                lblTimer.setText("<html>" + time + "</html>");
                revalidate();
                repaint();
            });
        } else {
            lblTimer.setText(null);
        }
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
//            logger.debug("<html>" + message + "</html>");
            lblMessage.setText("<html>" + message + "</html>");
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
    private JPanel panel13;
    private JLabel lblTimer;
    private JLabel lblMessage;
    private JToggleButton tbDebug;
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
    private JLabel label7;
    private JTextField lblRspwnSiren;
    private JLabel label6;
    private JTextField lblStartsiren;
    private JPanel panel5;
    private JPanel panel1;
    private JLabel lblButtonGreen;
    private JButton btnRelayTest1;
    private JButton btnRedLED1;
    private JButton btnRedLED2;
    private JLabel lblButtonRed;
    private JButton btnRelayTest2;
    private JButton btnGreenLED1;
    private JButton btnGreenLED2;
    private JLabel lblButtonPAUSE;
    private JButton btnRelayTest3;
    private JButton btnRedProgress1;
    private JButton btnSiren1;
    private JLabel lblButtonStartStop;
    private JButton btnRelayTest4;
    private JButton btnYellowProgress1;
    private JButton btnSiren2;
    private JButton btnRelayTest5;
    private JButton btnGreenProgress1;
    private JButton btnSiren3;
    private JPanel panel10;
    private JLabel label4;
    private JTextField txtHandlerPattern;
    private JButton btnRelayTest6;
    private JButton btnRedProgress2;
    private JButton btnSiren4;
    private JToggleButton tbUsePinHandler;
    private JButton btnRelayTest7;
    private JButton btnYellowProgress2;
    private JPanel panel11;
    private JLabel label5;
    private JTextField txtPercentage;
    private JButton btnRelayTest8;
    private JButton btnGreenProgress2;
    private JPanel panel12;
    private JButton btnRelaySirens;
    private JButton btnProgess;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
