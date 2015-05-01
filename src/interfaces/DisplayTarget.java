package interfaces;

import misc.Tools;

import javax.swing.*;
import java.util.ResourceBundle;

/**
 * Created by tloehr on 26.04.15.
 */
public class DisplayTarget {


    private final JLabel jLabel;


    public DisplayTarget(JLabel jLabel) {

        this.jLabel = jLabel;
    }


    public void setText(String text){
        if (jLabel != null){
            jLabel.setText( Tools.xx(text));
        }
    }
}
