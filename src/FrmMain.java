import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import javax.swing.*;

import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import interfaces.DisplayTarget;
import interfaces.ProgressTarget;
import threads.FarcryAssaultThread;
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
    FarcryAssaultThread farcryAssaultThread;

    public ResourceBundle lang;
    private int TIME2RESPAWN = 20, MAXCYLCES = 5, SECONDS2CAPTURE = 60;

    public FrmMain() {
        initComponents();
        initFrame();
    }

    private void initFrame() {
        progressBar1.setMinimum(0);
        progressBar1.setMinimum(MAXCYLCES);
        respawnThread = new RespawnThread(new DisplayTarget(lblRespawn), TIME2RESPAWN);
        farcryAssaultThread = new FarcryAssaultThread(new DisplayTarget(lblMessage), new DisplayTarget(lblGametimer), new ProgressTarget(progressBar1), MAXCYLCES, SECONDS2CAPTURE);
        lang = ResourceBundle.getBundle("Messages");
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            respawnThread.start();
            farcryAssaultThread.start();
        } else {

        }

    }

    private void btnMainActionPerformed(ActionEvent e) {
        farcryAssaultThread.toggleFlag();


    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        lblGametimer = new JLabel();
        lblRespawn = new JLabel();
        btnMain = new JButton();
        progressBar1 = new JProgressBar();
        lblMessage = new JLabel();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
            "default, $lcgap, default:grow, $lcgap, default",
            "default, $ugap, fill:default, $rgap, fill:default:grow, $lgap, default, $lgap, fill:default, $lgap, default"));

        //---- lblGametimer ----
        lblGametimer.setText("--");
        lblGametimer.setFont(new Font("sansserif", Font.PLAIN, 18));
        lblGametimer.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(lblGametimer, CC.xy(3, 1));

        //---- lblRespawn ----
        lblRespawn.setText("--");
        lblRespawn.setFont(new Font("sansserif", Font.PLAIN, 18));
        lblRespawn.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(lblRespawn, CC.xy(3, 3));

        //---- btnMain ----
        btnMain.setText("Dr\u00fcck mich");
        btnMain.setForeground(Color.black);
        btnMain.addActionListener(e -> btnMainActionPerformed(e));
        contentPane.add(btnMain, CC.xy(3, 5, CC.FILL, CC.FILL));
        contentPane.add(progressBar1, CC.xy(3, 7));

        //---- lblMessage ----
        lblMessage.setText("--");
        lblMessage.setFont(new Font("sansserif", Font.PLAIN, 18));
        lblMessage.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(lblMessage, CC.xy(3, 9));
        setSize(400, 300);
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel lblGametimer;
    private JLabel lblRespawn;
    private JButton btnMain;
    private JProgressBar progressBar1;
    private JLabel lblMessage;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
