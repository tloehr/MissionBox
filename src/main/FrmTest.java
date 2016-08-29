/*
 * Created by JFormDesigner on Tue Mar 15 10:21:26 CET 2016
 */

package main;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import misc.Tools;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DateFormat;
import java.util.Date;


/**
 * @author Torsten Löhr
 */
public class FrmTest extends JFrame {

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

    }

    public JButton getBtnUndo() {
        return btnUndo;
    }

    private void btnFCYcapPlusActionPerformed(ActionEvent e) {
        int time2capture = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_TIME2CAPTURE));
        time2capture++;
        final String text = Integer.toString(time2capture);
        MissionBox.getConfig().setProperty(MissionBox.FCY_TIME2CAPTURE, text);
        SwingUtilities.invokeLater(() -> {
            lblFCYCapture.setText(text);
            revalidate();
            repaint();
        });
    }

    private void btnFCYcapMinusActionPerformed(ActionEvent e) {
        int time2capture = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_TIME2CAPTURE));
        if (time2capture == 1) return;
        time2capture--;
        final String text = Integer.toString(time2capture);
        MissionBox.getConfig().setProperty(MissionBox.FCY_TIME2CAPTURE, text);
        SwingUtilities.invokeLater(() -> {
            lblFCYCapture.setText(text);
            revalidate();
            repaint();
        });
    }

    private void btnFCYgametimePlusActionPerformed(ActionEvent e) {
        int gametime = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_GAMETIME));
        gametime++;
        final String text = Integer.toString(gametime);
        MissionBox.getConfig().setProperty(MissionBox.FCY_GAMETIME, text);
        SwingUtilities.invokeLater(() -> {
            lblFCYGametime.setText(text);
            revalidate();
            repaint();
        });
    }

    private void btnFCYgametimeMinusActionPerformed(ActionEvent e) {
        int gametime = Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.FCY_GAMETIME));
        if (gametime == 1) return;
        gametime--;
        final String text = Integer.toString(gametime);
        MissionBox.getConfig().setProperty(MissionBox.FCY_GAMETIME, text);
        SwingUtilities.invokeLater(() -> {
            lblFCYGametime.setText(text);
            revalidate();
            repaint();
        });
    }

    public JPanel getDebugPanel4Pins() {
        return debugPanel4Pins;
    }

    public void log(String text) {
        log(0, "", text);
    }

    public void log(long someID, String someText, String text) {
        DateFormat df = DateFormat.getDateTimeInstance();
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
        label2 = new JLabel();
        lblFCYGametime = new JTextField();
        label3 = new JLabel();
        lblFCYRespawn = new JTextField();
        btnSound = new JToggleButton();
        btnSiren = new JToggleButton();
        btnMusic = new JToggleButton();
        btnRespawnSignal = new JToggleButton();
        panel1 = new JPanel();
        btnRespawn = new JButton();
        btnTestWinner = new JButton();
        btnTimeSignal = new JButton();
        btnTestLooser = new JButton();
        btnRedLedBar = new JButton();
        btnStopAll = new JButton();
        btnRelayTest = new JButton();

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
                btn1.setIcon(new ImageIcon(getClass().getResource("/artwork/farcry-logo.png")));
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
                    btnRed.setIcon(new ImageIcon(getClass().getResource("/artwork/ledred128.png")));
                    panel2.add(btnRed, CC.xy(1, 1, CC.FILL, CC.FILL));

                    //---- btnGreen ----
                    btnGreen.setText(null);
                    btnGreen.setIcon(new ImageIcon(getClass().getResource("/artwork/ledgreen128.png")));
                    panel2.add(btnGreen, CC.xy(1, 3, CC.FILL, CC.FILL));
                }
                contentPanel.add(panel2, CC.xywh(7, 1, 1, 5));

                //---- btnUndo ----
                btnUndo.setText("Undo");
                btnUndo.setIcon(new ImageIcon(getClass().getResource("/artwork/ledblue128.png")));
                btnUndo.setFont(new Font("Dialog", Font.BOLD, 20));
                btnUndo.setVerticalTextPosition(SwingConstants.BOTTOM);
                btnUndo.setHorizontalTextPosition(SwingConstants.CENTER);
                contentPanel.add(btnUndo, CC.xy(1, 3));

                //---- btn2 ----
                btn2.setText(null);
                btn2.setIcon(new ImageIcon(getClass().getResource("/artwork/exit128.png")));
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
                    "default:grow, $rgap, default:grow, $ugap",
                    "4*(default, $lgap), default"));

                //---- label1 ----
                label1.setText("capture (sec)");
                label1.setFont(new Font("Dialog", Font.PLAIN, 16));
                settingsPanel.add(label1, CC.xy(1, 1));

                //---- lblFCYCapture ----
                lblFCYCapture.setFont(new Font("Dialog", Font.BOLD, 20));
                lblFCYCapture.setText("1");
                lblFCYCapture.setHorizontalAlignment(SwingConstants.RIGHT);
                lblFCYCapture.addActionListener(e -> lblFCYCaptureActionPerformed(e));
                lblFCYCapture.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        lblFCYCaptureFocusLost(e);
                    }
                });
                settingsPanel.add(lblFCYCapture, CC.xy(3, 1, CC.FILL, CC.DEFAULT));

                //---- label2 ----
                label2.setText("gametime (min)");
                label2.setFont(new Font("Dialog", Font.PLAIN, 16));
                settingsPanel.add(label2, CC.xy(1, 3));

                //---- lblFCYGametime ----
                lblFCYGametime.setFont(new Font("Dialog", Font.BOLD, 20));
                lblFCYGametime.setText("1");
                lblFCYGametime.setHorizontalAlignment(SwingConstants.RIGHT);
                lblFCYGametime.addActionListener(e -> lblFCYGametimeActionPerformed(e));
                lblFCYGametime.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        lblFCYGametimeFocusLost(e);
                    }
                });
                settingsPanel.add(lblFCYGametime, CC.xy(3, 3, CC.FILL, CC.DEFAULT));

                //---- label3 ----
                label3.setText("respawn (sec)");
                label3.setFont(new Font("Dialog", Font.PLAIN, 16));
                settingsPanel.add(label3, CC.xy(1, 5));

                //---- lblFCYRespawn ----
                lblFCYRespawn.setFont(new Font("Dialog", Font.BOLD, 20));
                lblFCYRespawn.setText("1");
                lblFCYRespawn.setHorizontalAlignment(SwingConstants.RIGHT);
                lblFCYRespawn.addActionListener(e -> lblFCYRespawnActionPerformed(e));
                lblFCYRespawn.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        lblFCYRespawnFocusLost(e);
                    }
                });
                settingsPanel.add(lblFCYRespawn, CC.xy(3, 5, CC.FILL, CC.DEFAULT));

                //---- btnSound ----
                btnSound.setText("Sound");
                btnSound.setIcon(null);
                btnSound.setSelectedIcon(new ImageIcon(getClass().getResource("/artwork/sound.png")));
                settingsPanel.add(btnSound, CC.xy(1, 7));

                //---- btnSiren ----
                btnSiren.setText("Sirens");
                btnSiren.setIcon(null);
                btnSiren.setSelectedIcon(null);
                settingsPanel.add(btnSiren, CC.xy(3, 7));

                //---- btnMusic ----
                btnMusic.setText("Music");
                btnMusic.setIcon(null);
                btnMusic.setSelectedIcon(new ImageIcon(getClass().getResource("/artwork/sound.png")));
                settingsPanel.add(btnMusic, CC.xy(1, 9));

                //---- btnRespawnSignal ----
                btnRespawnSignal.setText("Respawn Signal");
                btnRespawnSignal.setIcon(null);
                btnRespawnSignal.setSelectedIcon(null);
                settingsPanel.add(btnRespawnSignal, CC.xy(3, 9));
            }
            tabbedPane1.addTab("Settings", settingsPanel);

            //======== panel1 ========
            {
                panel1.setLayout(new FormLayout(
                    "default, $lcgap, default, 6dlu, default",
                    "4*(default, $lgap), default"));

                //---- btnRespawn ----
                btnRespawn.setText("Respawn Signal");
                btnRespawn.addActionListener(e -> btnRespawnActionPerformed(e));
                panel1.add(btnRespawn, CC.xy(3, 1));

                //---- btnTestWinner ----
                btnTestWinner.setText("Winner Songs");
                btnTestWinner.addActionListener(e -> btnTestWinnerActionPerformed(e));
                panel1.add(btnTestWinner, CC.xy(5, 1));

                //---- btnTimeSignal ----
                btnTimeSignal.setText("4 Minutes");
                btnTimeSignal.addActionListener(e -> btnTimeSignalActionPerformed(e));
                panel1.add(btnTimeSignal, CC.xy(3, 3));

                //---- btnTestLooser ----
                btnTestLooser.setText("Loser Songs");
                btnTestLooser.addActionListener(e -> btnTestLooserActionPerformed(e));
                panel1.add(btnTestLooser, CC.xy(5, 3));

                //---- btnRedLedBar ----
                btnRedLedBar.setText("30 Seconds");
                btnRedLedBar.addActionListener(e -> btnRedLedBarActionPerformed(e));
                panel1.add(btnRedLedBar, CC.xy(3, 5));

                //---- btnStopAll ----
                btnStopAll.setText("Stop All Music");
                btnStopAll.addActionListener(e -> btnStopAllActionPerformed(e));
                panel1.add(btnStopAll, CC.xy(5, 5));

                //---- btnRelayTest ----
                btnRelayTest.setText("Relay Test");
                btnRelayTest.setEnabled(false);
                btnRelayTest.addActionListener(e -> btnRelayTestActionPerformed(e));
                panel1.add(btnRelayTest, CC.xy(3, 7));
            }
            tabbedPane1.addTab("HW-Test", panel1);
        }
        contentPane.add(tabbedPane1);
        setSize(800, 590);
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
    private JLabel label2;
    private JTextField lblFCYGametime;
    private JLabel label3;
    private JTextField lblFCYRespawn;
    private JToggleButton btnSound;
    private JToggleButton btnSiren;
    private JToggleButton btnMusic;
    private JToggleButton btnRespawnSignal;
    private JPanel panel1;
    private JButton btnRespawn;
    private JButton btnTestWinner;
    private JButton btnTimeSignal;
    private JButton btnTestLooser;
    private JButton btnRedLedBar;
    private JButton btnStopAll;
    private JButton btnRelayTest;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
