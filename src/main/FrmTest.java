/*
 * Created by JFormDesigner on Tue Mar 15 10:21:26 CET 2016
 */

package main;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Torsten Löhr
 */
public class FrmTest extends JFrame {
    public FrmTest() {
        initComponents();


    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        dialogPane = new JPanel();
        buttonBar = new JPanel();
        btn1 = new JButton();
        btn2 = new JButton();
        tabbedPane1 = new JTabbedPane();
        contentPanel = new JPanel();
        btnRed = new JButton();
        panel1 = new JPanel();
        lbl1 = new JLabel();
        lbl2 = new JLabel();
        lbl3 = new JLabel();
        lbl4 = new JLabel();
        lbl5 = new JLabel();
        lbl6 = new JLabel();
        lbl7 = new JLabel();
        lbl8 = new JLabel();
        lbl9 = new JLabel();
        lbl10 = new JLabel();
        lbl11 = new JLabel();
        lbl12 = new JLabel();
        lbl13 = new JLabel();
        lbl14 = new JLabel();
        lbl15 = new JLabel();
        btnGreen = new JButton();
        lblMessage = new JLabel();
        lblTimer = new JLabel();
        panel2 = new JPanel();
        label1 = new JLabel();
        txtTime2Capture = new JTextField();
        label2 = new JLabel();
        txtGametime = new JTextField();
        btnSound = new JToggleButton();
        btnSirens = new JToggleButton();
        label3 = new JLabel();
        label4 = new JLabel();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

                //---- btn1 ----
                btn1.setText("Start/Stop");
                buttonBar.add(btn1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- btn2 ----
                btn2.setText("QUIT");
                buttonBar.add(btn2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);

            //======== tabbedPane1 ========
            {
                tabbedPane1.setFont(new Font("Dialog", Font.BOLD, 24));

                //======== contentPanel ========
                {
                    contentPanel.setLayout(new FormLayout(
                        "pref:grow, $lcgap, default, $lcgap, center:default:grow",
                        "fill:default:grow, $lgap, default"));

                    //---- btnRed ----
                    btnRed.setText(null);
                    btnRed.setIcon(new ImageIcon(getClass().getResource("/artwork/red-led-off.png")));
                    contentPanel.add(btnRed, CC.xy(1, 1));

                    //======== panel1 ========
                    {
                        panel1.setLayout(new VerticalLayout(1));

                        //---- lbl1 ----
                        lbl1.setText(null);
                        lbl1.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkgreen.png")));
                        panel1.add(lbl1);

                        //---- lbl2 ----
                        lbl2.setText(null);
                        lbl2.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkgreen.png")));
                        panel1.add(lbl2);

                        //---- lbl3 ----
                        lbl3.setText(null);
                        lbl3.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkgreen.png")));
                        panel1.add(lbl3);

                        //---- lbl4 ----
                        lbl4.setText(null);
                        lbl4.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkgreen.png")));
                        panel1.add(lbl4);

                        //---- lbl5 ----
                        lbl5.setText(null);
                        lbl5.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkgreen.png")));
                        panel1.add(lbl5);

                        //---- lbl6 ----
                        lbl6.setText(null);
                        lbl6.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkorange.png")));
                        panel1.add(lbl6);

                        //---- lbl7 ----
                        lbl7.setText(null);
                        lbl7.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkorange.png")));
                        panel1.add(lbl7);

                        //---- lbl8 ----
                        lbl8.setText(null);
                        lbl8.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkorange.png")));
                        panel1.add(lbl8);

                        //---- lbl9 ----
                        lbl9.setText(null);
                        lbl9.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkorange.png")));
                        panel1.add(lbl9);

                        //---- lbl10 ----
                        lbl10.setText(null);
                        lbl10.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkorange.png")));
                        panel1.add(lbl10);

                        //---- lbl11 ----
                        lbl11.setText(null);
                        lbl11.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkred.png")));
                        panel1.add(lbl11);

                        //---- lbl12 ----
                        lbl12.setText(null);
                        lbl12.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkred.png")));
                        panel1.add(lbl12);

                        //---- lbl13 ----
                        lbl13.setText(null);
                        lbl13.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkred.png")));
                        panel1.add(lbl13);

                        //---- lbl14 ----
                        lbl14.setText(null);
                        lbl14.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkred.png")));
                        panel1.add(lbl14);

                        //---- lbl15 ----
                        lbl15.setText(null);
                        lbl15.setIcon(new ImageIcon(getClass().getResource("/artwork/leddarkred.png")));
                        panel1.add(lbl15);
                    }
                    contentPanel.add(panel1, CC.xy(3, 1));

                    //---- btnGreen ----
                    btnGreen.setText(null);
                    btnGreen.setIcon(new ImageIcon(getClass().getResource("/artwork/green-led-off.png")));
                    contentPanel.add(btnGreen, CC.xy(5, 1));

                    //---- lblMessage ----
                    lblMessage.setText("text");
                    lblMessage.setFont(new Font("Dialog", Font.PLAIN, 18));
                    contentPanel.add(lblMessage, CC.xywh(1, 3, 3, 1));

                    //---- lblTimer ----
                    lblTimer.setText("text");
                    lblTimer.setFont(new Font("Dialog", Font.BOLD, 18));
                    contentPanel.add(lblTimer, CC.xy(5, 3));
                }
                tabbedPane1.addTab("Game", contentPanel);

                //======== panel2 ========
                {
                    panel2.setLayout(new FormLayout(
                        "default, 9dlu, default:grow",
                        "5*(default, $lgap), default"));

                    //---- label1 ----
                    label1.setText("Time to capture the flag (seconds)");
                    label1.setFont(new Font("Dialog", Font.PLAIN, 26));
                    panel2.add(label1, CC.xy(1, 1));

                    //---- txtTime2Capture ----
                    txtTime2Capture.setFont(new Font("Dialog", Font.PLAIN, 26));
                    panel2.add(txtTime2Capture, CC.xy(3, 1));

                    //---- label2 ----
                    label2.setText("Maximum gametime (minutes)");
                    label2.setFont(new Font("Dialog", Font.PLAIN, 26));
                    panel2.add(label2, CC.xy(1, 3));

                    //---- txtGametime ----
                    txtGametime.setFont(new Font("Dialog", Font.PLAIN, 26));
                    panel2.add(txtGametime, CC.xy(3, 3));

                    //---- btnSound ----
                    btnSound.setText(null);
                    btnSound.setIcon(new ImageIcon(getClass().getResource("/artwork/soundoff.png")));
                    btnSound.setSelectedIcon(new ImageIcon(getClass().getResource("/artwork/sound.png")));
                    panel2.add(btnSound, CC.xy(1, 7));

                    //---- btnSirens ----
                    btnSirens.setText(null);
                    btnSirens.setIcon(new ImageIcon(getClass().getResource("/artwork/speaker_mute.png")));
                    btnSirens.setSelectedIcon(new ImageIcon(getClass().getResource("/artwork/speaker.png")));
                    panel2.add(btnSirens, CC.xy(3, 7));

                    //---- label3 ----
                    label3.setText("Sound-System");
                    label3.setFont(new Font("Dialog", Font.PLAIN, 26));
                    panel2.add(label3, CC.xy(1, 9, CC.CENTER, CC.DEFAULT));

                    //---- label4 ----
                    label4.setText("Siren");
                    label4.setFont(new Font("Dialog", Font.PLAIN, 26));
                    panel2.add(label4, CC.xy(3, 9, CC.CENTER, CC.DEFAULT));
                }
                tabbedPane1.addTab("Settings", panel2);
            }
            dialogPane.add(tabbedPane1, BorderLayout.NORTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
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
    private JPanel dialogPane;
    private JPanel buttonBar;
    private JButton btn1;
    private JButton btn2;
    private JTabbedPane tabbedPane1;
    private JPanel contentPanel;
    private JButton btnRed;
    private JPanel panel1;
    private JLabel lbl1;
    private JLabel lbl2;
    private JLabel lbl3;
    private JLabel lbl4;
    private JLabel lbl5;
    private JLabel lbl6;
    private JLabel lbl7;
    private JLabel lbl8;
    private JLabel lbl9;
    private JLabel lbl10;
    private JLabel lbl11;
    private JLabel lbl12;
    private JLabel lbl13;
    private JLabel lbl14;
    private JLabel lbl15;
    private JButton btnGreen;
    private JLabel lblMessage;
    private JLabel lblTimer;
    private JPanel panel2;
    private JLabel label1;
    private JTextField txtTime2Capture;
    private JLabel label2;
    private JTextField txtGametime;
    private JToggleButton btnSound;
    private JToggleButton btnSirens;
    private JLabel label3;
    private JLabel label4;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
