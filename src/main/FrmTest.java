/*
 * Created by JFormDesigner on Tue Mar 15 10:21:26 CET 2016
 */

package main;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;


/**
 * @author Torsten Löhr
 */
public class FrmTest extends JFrame {

    public FrmTest() {
        initComponents();
        initPanel();
    }

    private void initPanel() {
        lblFCYCapture.setText(MissionBox.getConfig().getProperty(MissionBox.FCY_TIME2CAPTURE));
        lblFCYGametime.setText(MissionBox.getConfig().getProperty(MissionBox.FCY_GAMETIME));
        lblFCYRespawn.setText(MissionBox.getConfig().getProperty(MissionBox.FCY_RESPAWN));
        btnSiren.setSelected(MissionBox.getConfig().getProperty(MissionBox.FCY_SIREN).equals("true"));
        btnSound.setSelected(MissionBox.getConfig().getProperty(MissionBox.FCY_SOUND).equals("true"));

        btnSiren.addActionListener(e -> MissionBox.getConfig().setProperty(MissionBox.FCY_SIREN, btnSiren.isSelected() ? "true" : "false"));
        btnSound.addActionListener(e -> MissionBox.getConfig().setProperty(MissionBox.FCY_SOUND, btnSound.isSelected() ? "true" : "false"));

        pb1.setVisible(true);

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

    private void tabbedPane1StateChanged(ChangeEvent e) {

        if (tabbedPane1.getSelectedIndex() == 0) {
            MissionBox.saveLocalProps();
        } else {

        }
    }

    public void setProgress(int progress) {
        pb1.setIndeterminate(progress == 0);
        if (progress > 0) {
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
        // TODO add your code here
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


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        tabbedPane1 = new JTabbedPane();
        contentPanel = new JPanel();
        btn1 = new JButton();
        btn2 = new JButton();
        btnRed = new JButton();
        btnGreen = new JButton();
        pb1 = new JProgressBar();
        lblMessage = new JLabel();
        lblRespawn = new JLabel();
        lblTimer = new JLabel();
        settingsPanel = new JPanel();
        label1 = new JLabel();
        btnFCYcapPlus = new JButton();
        lblFCYCapture = new JLabel();
        btnFCYcapMinus = new JButton();
        label2 = new JLabel();
        btnFCYgametimePlus = new JButton();
        lblFCYGametime = new JLabel();
        btnFCYgametimeMinus = new JButton();
        label3 = new JLabel();
        btnFCYrespawnPlus = new JButton();
        lblFCYRespawn = new JLabel();
        btnFCYrespawnMinus = new JButton();
        btnSound = new JToggleButton();
        btnSiren = new JToggleButton();
        panel1 = new JPanel();
        lblBtnRed = new JLabel();
        btnRedLed = new JButton();
        lblBtnGreen = new JLabel();
        btnRedLed2 = new JButton();
        lblBtnStartStop = new JLabel();
        btnRedLedBar = new JButton();
        lblBtnQuit = new JLabel();
        btnYellowLedBar = new JButton();
        btnGreenLedBar = new JButton();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));

        //======== tabbedPane1 ========
        {
            tabbedPane1.setFont(new Font("Dialog", Font.PLAIN, 16));
            tabbedPane1.addChangeListener(e -> tabbedPane1StateChanged(e));

            //======== contentPanel ========
            {
                contentPanel.setLayout(new FormLayout(
                    "min:grow, $lcgap, default, $rgap, min:grow",
                    "fill:default:grow, $lgap, fill:pref:grow, $lgap, 10dlu, $lgap, default"));

                //---- btn1 ----
                btn1.setText(null);
                btn1.setIcon(new ImageIcon(getClass().getResource("/artwork/farcry-logo.png")));
                contentPanel.add(btn1, CC.xywh(1, 1, 1, 3));

                //---- btn2 ----
                btn2.setText(null);
                btn2.setIcon(new ImageIcon(getClass().getResource("/artwork/exit.png")));
                contentPanel.add(btn2, CC.xywh(3, 1, 1, 3));

                //---- btnRed ----
                btnRed.setText(null);
                btnRed.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkred32.png")));
                contentPanel.add(btnRed, CC.xy(5, 1, CC.FILL, CC.FILL));

                //---- btnGreen ----
                btnGreen.setText(null);
                btnGreen.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkgreen32.png")));
                contentPanel.add(btnGreen, CC.xy(5, 3, CC.FILL, CC.FILL));
                contentPanel.add(pb1, CC.xywh(1, 5, 5, 1));

                //---- lblMessage ----
                lblMessage.setText("text");
                lblMessage.setFont(new Font("Dialog", Font.PLAIN, 16));
                contentPanel.add(lblMessage, CC.xy(1, 7));

                //---- lblRespawn ----
                lblRespawn.setText("--");
                lblRespawn.setFont(new Font("Dialog", Font.PLAIN, 16));
                lblRespawn.setForeground(Color.red);
                contentPanel.add(lblRespawn, CC.xy(3, 7, CC.CENTER, CC.DEFAULT));

                //---- lblTimer ----
                lblTimer.setText("--");
                lblTimer.setFont(new Font("Dialog", Font.PLAIN, 16));
                contentPanel.add(lblTimer, CC.xy(5, 7, CC.CENTER, CC.DEFAULT));
            }
            tabbedPane1.addTab("Game", contentPanel);

            //======== settingsPanel ========
            {
                settingsPanel.setLayout(new FormLayout(
                    "default:grow, $ugap, default, $lcgap, default:grow, $lcgap, default",
                    "3*(default, $lgap), default"));

                //---- label1 ----
                label1.setText("capture (sec)");
                label1.setFont(new Font("Dialog", Font.PLAIN, 16));
                settingsPanel.add(label1, CC.xy(1, 1));

                //---- btnFCYcapPlus ----
                btnFCYcapPlus.setText(null);
                btnFCYcapPlus.setIcon(new ImageIcon(getClass().getResource("/artwork/edit_add.png")));
                btnFCYcapPlus.addActionListener(e -> btnFCYcapPlusActionPerformed(e));
                settingsPanel.add(btnFCYcapPlus, CC.xy(3, 1));

                //---- lblFCYCapture ----
                lblFCYCapture.setFont(new Font("Dialog", Font.BOLD, 20));
                lblFCYCapture.setText("1");
                settingsPanel.add(lblFCYCapture, CC.xy(5, 1, CC.CENTER, CC.DEFAULT));

                //---- btnFCYcapMinus ----
                btnFCYcapMinus.setText(null);
                btnFCYcapMinus.setIcon(new ImageIcon(getClass().getResource("/artwork/edit_remove.png")));
                btnFCYcapMinus.addActionListener(e -> btnFCYcapMinusActionPerformed(e));
                settingsPanel.add(btnFCYcapMinus, CC.xy(7, 1));

                //---- label2 ----
                label2.setText("gametime (min)");
                label2.setFont(new Font("Dialog", Font.PLAIN, 16));
                settingsPanel.add(label2, CC.xy(1, 3));

                //---- btnFCYgametimePlus ----
                btnFCYgametimePlus.setText(null);
                btnFCYgametimePlus.setIcon(new ImageIcon(getClass().getResource("/artwork/edit_add.png")));
                btnFCYgametimePlus.addActionListener(e -> btnFCYgametimePlusActionPerformed(e));
                settingsPanel.add(btnFCYgametimePlus, CC.xy(3, 3));

                //---- lblFCYGametime ----
                lblFCYGametime.setFont(new Font("Dialog", Font.BOLD, 20));
                lblFCYGametime.setText("1");
                settingsPanel.add(lblFCYGametime, CC.xy(5, 3, CC.CENTER, CC.DEFAULT));

                //---- btnFCYgametimeMinus ----
                btnFCYgametimeMinus.setText(null);
                btnFCYgametimeMinus.setIcon(new ImageIcon(getClass().getResource("/artwork/edit_remove.png")));
                btnFCYgametimeMinus.addActionListener(e -> btnFCYgametimeMinusActionPerformed(e));
                settingsPanel.add(btnFCYgametimeMinus, CC.xy(7, 3));

                //---- label3 ----
                label3.setText("respawn (sec)");
                label3.setFont(new Font("Dialog", Font.PLAIN, 16));
                settingsPanel.add(label3, CC.xy(1, 5));

                //---- btnFCYrespawnPlus ----
                btnFCYrespawnPlus.setText(null);
                btnFCYrespawnPlus.setIcon(new ImageIcon(getClass().getResource("/artwork/edit_add.png")));
                btnFCYrespawnPlus.addActionListener(e -> btnFCYrespawnPlusActionPerformed(e));
                settingsPanel.add(btnFCYrespawnPlus, CC.xy(3, 5));

                //---- lblFCYRespawn ----
                lblFCYRespawn.setFont(new Font("Dialog", Font.BOLD, 20));
                lblFCYRespawn.setText("1");
                settingsPanel.add(lblFCYRespawn, CC.xy(5, 5, CC.CENTER, CC.DEFAULT));

                //---- btnFCYrespawnMinus ----
                btnFCYrespawnMinus.setText(null);
                btnFCYrespawnMinus.setIcon(new ImageIcon(getClass().getResource("/artwork/edit_remove.png")));
                btnFCYrespawnMinus.addActionListener(e -> btnFCYrespawnMinusActionPerformed(e));
                settingsPanel.add(btnFCYrespawnMinus, CC.xy(7, 5));

                //---- btnSound ----
                btnSound.setText(null);
                btnSound.setIcon(new ImageIcon(getClass().getResource("/artwork/soundoff.png")));
                btnSound.setSelectedIcon(new ImageIcon(getClass().getResource("/artwork/sound.png")));
                settingsPanel.add(btnSound, CC.xy(1, 7));

                //---- btnSiren ----
                btnSiren.setText(null);
                btnSiren.setIcon(new ImageIcon(getClass().getResource("/artwork/speaker_mute.png")));
                btnSiren.setSelectedIcon(new ImageIcon(getClass().getResource("/artwork/speaker.png")));
                settingsPanel.add(btnSiren, CC.xywh(3, 7, 5, 1));
            }
            tabbedPane1.addTab("Settings", settingsPanel);

            //======== panel1 ========
            {
                panel1.setLayout(new FormLayout(
                    "default, $lcgap, default, 6dlu, default",
                    "4*(default, $lgap), default"));

                //---- lblBtnRed ----
                lblBtnRed.setText("Red Button");
                lblBtnRed.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkred32.png")));
                panel1.add(lblBtnRed, CC.xy(1, 1));

                //---- btnRedLed ----
                btnRedLed.setText("LED ON");
                panel1.add(btnRedLed, CC.xy(3, 1));

                //---- lblBtnGreen ----
                lblBtnGreen.setText("Green Button");
                lblBtnGreen.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkgreen32.png")));
                panel1.add(lblBtnGreen, CC.xy(1, 3));

                //---- btnRedLed2 ----
                btnRedLed2.setText("LED ON");
                panel1.add(btnRedLed2, CC.xy(3, 3));

                //---- lblBtnStartStop ----
                lblBtnStartStop.setText("Start/Stop");
                lblBtnStartStop.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkblue32.png")));
                panel1.add(lblBtnStartStop, CC.xy(1, 5));

                //---- btnRedLedBar ----
                btnRedLedBar.setText("Red LED Bar");
                btnRedLedBar.addActionListener(e -> btnRedLedBarActionPerformed(e));
                panel1.add(btnRedLedBar, CC.xy(3, 5));

                //---- lblBtnQuit ----
                lblBtnQuit.setText("Quit");
                lblBtnQuit.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkyellow32.png")));
                panel1.add(lblBtnQuit, CC.xy(1, 7));

                //---- btnYellowLedBar ----
                btnYellowLedBar.setText("Yellow LED Bar");
                panel1.add(btnYellowLedBar, CC.xy(3, 7));

                //---- btnGreenLedBar ----
                btnGreenLedBar.setText("Green LED Bar");
                panel1.add(btnGreenLedBar, CC.xy(3, 9));
            }
            tabbedPane1.addTab("HW-Test", panel1);
        }
        contentPane.add(tabbedPane1);
        setSize(305, 240);
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
    private JButton btn2;
    private JButton btnRed;
    private JButton btnGreen;
    private JProgressBar pb1;
    private JLabel lblMessage;
    private JLabel lblRespawn;
    private JLabel lblTimer;
    private JPanel settingsPanel;
    private JLabel label1;
    private JButton btnFCYcapPlus;
    private JLabel lblFCYCapture;
    private JButton btnFCYcapMinus;
    private JLabel label2;
    private JButton btnFCYgametimePlus;
    private JLabel lblFCYGametime;
    private JButton btnFCYgametimeMinus;
    private JLabel label3;
    private JButton btnFCYrespawnPlus;
    private JLabel lblFCYRespawn;
    private JButton btnFCYrespawnMinus;
    private JToggleButton btnSound;
    private JToggleButton btnSiren;
    private JPanel panel1;
    private JLabel lblBtnRed;
    private JButton btnRedLed;
    private JLabel lblBtnGreen;
    private JButton btnRedLed2;
    private JLabel lblBtnStartStop;
    private JButton btnRedLedBar;
    private JLabel lblBtnQuit;
    private JButton btnYellowLedBar;
    private JButton btnGreenLedBar;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
