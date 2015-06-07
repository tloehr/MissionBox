package interfaces;

import javax.swing.*;
import java.math.BigDecimal;

/**
 * Created by tloehr on 26.04.15.
 */
public class ProgressBarDisplay implements PercentageInterface {


    private final JProgressBar jProgressBar;

    public ProgressBarDisplay(JProgressBar jProgressBar) {
        this.jProgressBar = jProgressBar;
    }

    @Override
    public void setValue(BigDecimal value) {
        SwingUtilities.invokeLater(() -> {
            jProgressBar.setValue(value.intValue());
            jProgressBar.revalidate();
            jProgressBar.repaint();
        });
    }


}
