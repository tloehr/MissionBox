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
        btnSiren.setSelected(MissionBox.getConfig().getProperty(MissionBox.FCY_SIREN).equals("1"));
        btnSound.setSelected(MissionBox.getConfig().getProperty(MissionBox.FCY_SOUND).equals("1"));

        btnSiren.addActionListener(e -> MissionBox.getConfig().setProperty(MissionBox.FCY_SIREN, btnSiren.isSelected() ? "1" : "0"));
        btnSound.addActionListener(e -> MissionBox.getConfig().setProperty(MissionBox.FCY_SOUND, btnSound.isSelected() ? "1" : "0"));

        pb1.setMaximum(100);
        pb1.setMaximum(0);

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

        } else {

        }
    }

    public void setProgress(int value) {
        SwingUtilities.invokeLater(() -> {
            pb1.setValue(value);
            pb1.revalidate();
            pb1.repaint();
        });

    }

    public void enableSettings(boolean yes) {
        tabbedPane1.setEnabledAt(1, yes);
    }

    public boolean isGameStartable() {
        return tabbedPane1.getSelectedIndex() == 0;
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        tabbedPane1 = new JTabbedPane();
        contentPanel = new JPanel();
        btnRed = new JButton();
        btnGreen = new JButton();
        pb1 = new JProgressBar();
        lblMessage = new JLabel();
        lblTimer = new JLabel();
        btn1 = new JButton();
        btn2 = new JButton();
        panel2 = new JPanel();
        label1 = new JLabel();
        btnFCYcapPlus = new JButton();
        lblFCYCapture = new JLabel();
        btnFCYcapMinus = new JButton();
        label2 = new JLabel();
        btnFCYgametimePlus = new JButton();
        lblFCYGametime = new JLabel();
        btnFCYgametimeMinus = new JButton();
        btnSound = new JToggleButton();
        btnSiren = new JToggleButton();

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
                        "pref:grow, $ugap, default:grow",
                        "fill:pref:grow, 3*($lgap, default)"));

                //---- btnRed ----
                btnRed.setText(null);
                btnRed.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkred32.png")));
                contentPanel.add(btnRed, CC.xy(1, 1, CC.FILL, CC.FILL));

                //---- btnGreen ----
                btnGreen.setText(null);
                btnGreen.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkgreen32.png")));
                contentPanel.add(btnGreen, CC.xy(3, 1, CC.FILL, CC.FILL));
                contentPanel.add(pb1, CC.xywh(1, 3, 3, 1));

                //---- lblMessage ----
                lblMessage.setText("text");
                lblMessage.setFont(new Font("Dialog", Font.PLAIN, 16));
                contentPanel.add(lblMessage, CC.xy(1, 5));

                //---- lblTimer ----
                lblTimer.setText("text");
                lblTimer.setFont(new Font("Dialog", Font.PLAIN, 16));
                contentPanel.add(lblTimer, CC.xy(3, 5));

                //---- btn1 ----
                btn1.setText("Start/Stop");
                contentPanel.add(btn1, CC.xy(1, 7));

                //---- btn2 ----
                btn2.setText("QUIT");
                contentPanel.add(btn2, CC.xy(3, 7));
            }
            tabbedPane1.addTab("Game", contentPanel);

            //======== panel2 ========
            {
                panel2.setLayout(new FormLayout(
                        "default:grow, $ugap, default, $lcgap, default:grow, $lcgap, default",
                        "2*(default, $lgap), default"));

                //---- label1 ----
                label1.setText("capture");
                label1.setFont(new Font("Dialog", Font.PLAIN, 16));
                panel2.add(label1, CC.xy(1, 1));

                //---- btnFCYcapPlus ----
                btnFCYcapPlus.setText(null);
                btnFCYcapPlus.setIcon(new ImageIcon(getClass().getResource("/artwork/edit_add.png")));
                btnFCYcapPlus.addActionListener(e -> btnFCYcapPlusActionPerformed(e));
                panel2.add(btnFCYcapPlus, CC.xy(3, 1));

                //---- lblFCYCapture ----
                lblFCYCapture.setFont(new Font("Dialog", Font.BOLD, 20));
                lblFCYCapture.setText("1");
                panel2.add(lblFCYCapture, CC.xy(5, 1, CC.CENTER, CC.DEFAULT));

                //---- btnFCYcapMinus ----
                btnFCYcapMinus.setText(null);
                btnFCYcapMinus.setIcon(new ImageIcon(getClass().getResource("/artwork/edit_remove.png")));
                btnFCYcapMinus.addActionListener(e -> btnFCYcapMinusActionPerformed(e));
                panel2.add(btnFCYcapMinus, CC.xy(7, 1));

                //---- label2 ----
                label2.setText("gametime");
                label2.setFont(new Font("Dialog", Font.PLAIN, 16));
                panel2.add(label2, CC.xy(1, 3));

                //---- btnFCYgametimePlus ----
                btnFCYgametimePlus.setText(null);
                btnFCYgametimePlus.setIcon(new ImageIcon(getClass().getResource("/artwork/edit_add.png")));
                btnFCYgametimePlus.addActionListener(e -> btnFCYgametimePlusActionPerformed(e));
                panel2.add(btnFCYgametimePlus, CC.xy(3, 3));

                //---- lblFCYGametime ----
                lblFCYGametime.setFont(new Font("Dialog", Font.BOLD, 20));
                lblFCYGametime.setText("1");
                panel2.add(lblFCYGametime, CC.xy(5, 3, CC.CENTER, CC.DEFAULT));

                //---- btnFCYgametimeMinus ----
                btnFCYgametimeMinus.setText(null);
                btnFCYgametimeMinus.setIcon(new ImageIcon(getClass().getResource("/artwork/edit_remove.png")));
                btnFCYgametimeMinus.addActionListener(e -> btnFCYgametimeMinusActionPerformed(e));
                panel2.add(btnFCYgametimeMinus, CC.xy(7, 3));

                //---- btnSound ----
                btnSound.setText(null);
                btnSound.setIcon(new ImageIcon(getClass().getResource("/artwork/soundoff.png")));
                btnSound.setSelectedIcon(new ImageIcon(getClass().getResource("/artwork/sound.png")));
                panel2.add(btnSound, CC.xy(1, 5));

                //---- btnSiren ----
                btnSiren.setText(null);
                btnSiren.setIcon(new ImageIcon(getClass().getResource("/artwork/speaker_mute.png")));
                btnSiren.setSelectedIcon(new ImageIcon(getClass().getResource("/artwork/speaker.png")));
                panel2.add(btnSiren, CC.xywh(3, 5, 5, 1));
            }
            tabbedPane1.addTab("Settings", panel2);
        }
        contentPane.add(tabbedPane1);
        setSize(320, 240);
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
    private JButton btnRed;
    private JButton btnGreen;
    private JProgressBar pb1;
    private JLabel lblMessage;
    private JLabel lblTimer;
    private JButton btn1;
    private JButton btn2;
    private JPanel panel2;
    private JLabel label1;
    private JButton btnFCYcapPlus;
    private JLabel lblFCYCapture;
    private JButton btnFCYcapMinus;
    private JLabel label2;
    private JButton btnFCYgametimePlus;
    private JLabel lblFCYGametime;
    private JButton btnFCYgametimeMinus;
    private JToggleButton btnSound;
    private JToggleButton btnSiren;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
