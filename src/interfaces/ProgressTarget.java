package interfaces;

import javax.swing.*;

/**
 * Created by tloehr on 26.04.15.
 */
public class ProgressTarget implements ProgressInterface {


    private final JProgressBar jProgressBar;

    public ProgressTarget(JProgressBar jProgressBar) {
        this.jProgressBar = jProgressBar;
    }

    @Override
    public void setValue(int value) {
        SwingUtilities.invokeLater(() -> jProgressBar.setValue(value));
    }


}
