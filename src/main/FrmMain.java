/*
 * Created by JFormDesigner on Sat Jun 27 17:19:53 CEST 2015
 */

package main;

import java.awt.*;
import javax.swing.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

/**
 * @author Torsten Löhr
 */
public class FrmMain extends JFrame {
    public FrmMain() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        button1 = new JButton();
        button2 = new JButton();
        button3 = new JButton();
        label1 = new JLabel();
        button4 = new JButton();
        button5 = new JButton();
        button6 = new JButton();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(new FormLayout(
            "2*(default:grow, $lcgap), default:grow",
            "fill:default:grow, $lgap, default:grow, $lgap, fill:default:grow"));

        //---- button1 ----
        button1.setText("text");
        button1.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 20));
        contentPane.add(button1, CC.xy(1, 1, CC.FILL, CC.FILL));

        //---- button2 ----
        button2.setText("text");
        button2.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 20));
        contentPane.add(button2, CC.xy(3, 1, CC.FILL, CC.FILL));

        //---- button3 ----
        button3.setText("text");
        button3.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 20));
        contentPane.add(button3, CC.xy(5, 1, CC.FILL, CC.FILL));

        //---- label1 ----
        label1.setText("text");
        label1.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 20));
        contentPane.add(label1, CC.xywh(1, 3, 5, 1, CC.CENTER, CC.FILL));

        //---- button4 ----
        button4.setText("text");
        button4.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 20));
        contentPane.add(button4, CC.xy(1, 5, CC.FILL, CC.FILL));

        //---- button5 ----
        button5.setText("text");
        button5.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 20));
        contentPane.add(button5, CC.xy(3, 5, CC.FILL, CC.FILL));

        //---- button6 ----
        button6.setText("text");
        button6.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 20));
        contentPane.add(button6, CC.xy(5, 5, CC.FILL, CC.FILL));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JLabel label1;
    private JButton button4;
    private JButton button5;
    private JButton button6;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
