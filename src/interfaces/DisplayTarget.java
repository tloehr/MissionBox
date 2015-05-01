package interfaces;

import javax.swing.*;
import java.util.ResourceBundle;

/**
 * Created by tloehr on 26.04.15.
 */
public class DisplayTarget {


    private final JLabel jLabel;
    private final ResourceBundle lang;

    public DisplayTarget(JLabel jLabel) {
        lang = ResourceBundle.getBundle("Messages");
        this.jLabel = jLabel;
    }


    public void setText(String text){
        if (jLabel != null){
            jLabel.setText( lang.getString(text));
        }
    }
}
