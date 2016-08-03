package interfaces;

import main.MissionBox;
import org.apache.log4j.Logger;

import java.awt.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tloehr on 07.06.15.
 */
public class RelayProgressRGB implements PercentageInterface {


    protected final Logger logger = Logger.getLogger(getClass());
    private final String pinRed;
    private final String pinGreen;
    private final String pinBlue;
    protected int previousRelay = -1;

    final Color yellow = new Color(255,255,0);
    final Color purple = new Color(255,0,255);
    final Color cyan = new Color(0,255,255);

    final Color green = new Color(0,255,0);
    final Color blue = new Color(0,0,255);
    final Color red = new Color(255,0,0);

    final Color[] colors = {red, purple, blue, yellow, cyan, green};

    public RelayProgressRGB(String pinRed, String pinGreen, String pinBlue) {
        this.pinRed = pinRed;
        this.pinGreen = pinGreen;
        this.pinBlue = pinBlue;
    }


    @Override
    public void setValue(BigDecimal percent) {

        logger.debug("PERCENT: " + percent);

        if (percent.compareTo(BigDecimal.ZERO) < 0) {
            MissionBox.off(pinRed);
            MissionBox.off(pinGreen);
            MissionBox.off(pinBlue);
            return;
        }

        BigDecimal myPercent = new BigDecimal(100).subtract(percent);
        int colnum = new BigDecimal(colors.length).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).multiply(myPercent).intValue();

        // shortcut
        if (previousRelay == colnum) return;
        previousRelay = colnum;

        if (colnum >= colors.length) {
            MissionBox.off(pinRed);
            MissionBox.off(pinGreen);
            MissionBox.off(pinBlue);
        } else {
            setColor(colors[colnum]);
        }

    }

    void setColor(Color color){
        if (color.getRed() > 0){
            MissionBox.on(pinRed);
        } else {
            MissionBox.off(pinRed);
        }
        if (color.getBlue() > 0){
            MissionBox.on(pinBlue);
        } else {
            MissionBox.off(pinBlue);
        }
        if (color.getGreen() > 0){
            MissionBox.on(pinGreen);
        } else {
            MissionBox.off(pinGreen);
        }
    }
}
