package main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*
 * Created by JFormDesigner on Thu Jul 14 15:10:17 CEST 2016
 */


/**
 * @author Torsten Löhr
 */
public class FrmSimulator extends JFrame {

    public FrmSimulator() {
        initComponents();

    }



    private void okButtonActionPerformed(ActionEvent e) {
        System.exit(0);
    }



    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        dialogPane = new JPanel();
        panel1 = new JScrollPane();
        contentPanel = new JPanel();
        buttonBar = new JPanel();
        okButton = new JButton();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== panel1 ========
            {

                //======== contentPanel ========
                {
                    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
                }
                panel1.setViewportView(contentPanel);
            }
            dialogPane.add(panel1, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new BoxLayout(buttonBar, BoxLayout.X_AXIS));

                //---- okButton ----
                okButton.setText("OK");
                okButton.addActionListener(e -> okButtonActionPerformed(e));
                buttonBar.add(okButton);
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        setSize(615, 315);
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    private JScrollPane panel1;
    private JPanel contentPanel;
    private JPanel buttonBar;
    private JButton okButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
