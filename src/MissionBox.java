import threads.RespawnThread;

import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tloehr on 22.04.15.
 */
public class MissionBox {

    public final Logger LOGGER = Logger.getLogger(MissionBox.class.getName());
    public static final Level LOGLEVEL = Level.FINEST;


    public static void main(String[] args) {

        FrmMain frmMain = new FrmMain();

        frmMain.setVisible(true);

    }
}
