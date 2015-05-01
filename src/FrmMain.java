import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import javax.swing.*;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import interfaces.DisplayTarget;
import interfaces.ProgressTarget;
import misc.AEPlayWave;
import threads.DisplayThread;
import threads.RespawnThread;
/*
 * Created by JFormDesigner on Thu Apr 23 10:23:23 PDT 2015
 */


/**
 * @author Torsten Löhr
 */
public class FrmMain extends JFrame {
    boolean flag = false;
    RespawnThread respawnThread;
    DisplayThread displayThread;

    public ResourceBundle lang;
    private int TIME2RESPAWN = 20, MAXCYLCES = 5;

    public FrmMain() {
        initComponents();
        initFrame();
    }

    private void initFrame() {
        progressBar1.setMinimum(0);
        progressBar1.setMinimum(MAXCYLCES);
        respawnThread = new RespawnThread(new DisplayTarget(lblRespawn), TIME2RESPAWN);
        displayThread = new DisplayThread(new DisplayTarget(lblMessage), new ProgressTarget(progressBar1), MAXCYLCES);
        lang = ResourceBundle.getBundle("Messages");
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            respawnThread.start();
            displayThread.start();
        } else {

        }

    }

    private void btnMainActionPerformed(ActionEvent e) {
        displayThread.toggleFlag();


    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        lblRespawn = new JLabel();
        lblMessage = new JLabel();
        panel1 = new JPanel();
        btnMain = new JButton();
        progressBar1 = new JProgressBar();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //---- lblRespawn ----
        lblRespawn.setText("--");
        lblRespawn.setFont(new Font("sansserif", Font.PLAIN, 18));
        lblRespawn.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(lblRespawn, BorderLayout.NORTH);

        //---- lblMessage ----
        lblMessage.setText("--");
        lblMessage.setFont(new Font("sansserif", Font.PLAIN, 18));
        lblMessage.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(lblMessage, BorderLayout.SOUTH);

        //======== panel1 ========
        {
            panel1.setLayout(new FormLayout(
                "default:grow",
                "fill:default:grow, default"));

            //---- btnMain ----
            btnMain.setText("Dr\u00fcck mich");
            btnMain.setForeground(Color.black);
            btnMain.addActionListener(e -> btnMainActionPerformed(e));
            panel1.add(btnMain, CC.xy(1, 1, CC.CENTER, CC.CENTER));
            panel1.add(progressBar1, CC.xy(1, 2));
        }
        contentPane.add(panel1, BorderLayout.CENTER);
        setSize(400, 300);
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel lblRespawn;
    private JLabel lblMessage;
    private JPanel panel1;
    private JButton btnMain;
    private JProgressBar progressBar1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
