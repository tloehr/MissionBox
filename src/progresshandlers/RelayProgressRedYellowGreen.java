package progresshandlers;

import interfaces.PercentageInterface;
import main.MissionBox;
import org.apache.log4j.Logger;

import java.awt.*;
import java.math.BigDecimal;

/**
 * Created by tloehr on 07.06.15.
 */
public class RelayProgressRedYellowGreen extends PercentageInterface {

    protected final Logger logger = Logger.getLogger(getClass());
    private final String pinRed;
    private final String pinGreen;
    private final String pinYellow;
    protected int previousRelay = -1;

    final String on = "1;" + Long.MAX_VALUE + ",0";
    final String slow = Integer.toString(Integer.MAX_VALUE) + ";500,500";
    final String fast = Integer.toString(Integer.MAX_VALUE) + ";100,100";

    String[] colors;
    final String[] schemes = {fast, slow, on, fast, slow, on, fast, slow, on};

    public RelayProgressRedYellowGreen(String pinRed, String pinYellow, String pinGreen) {
        super("");
        this.pinRed = pinRed;
        this.pinGreen = pinGreen;
        this.pinYellow = pinYellow;
        colors = new String[]{pinRed, pinRed, pinRed, pinYellow, pinYellow, pinYellow, pinGreen, pinGreen, pinGreen};
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public void setValue(BigDecimal percent) {

        logger.debug("PERCENT: " + percent);

        if (percent.compareTo(BigDecimal.ZERO) < 0) {
            MissionBox.off(pinRed);
            MissionBox.off(pinGreen);
            MissionBox.off(pinYellow);
            return;
        }

        BigDecimal myPercent = new BigDecimal(100).subtract(percent);
        int colornum = new BigDecimal(colors.length).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).multiply(myPercent).intValue();

        // shortcut
        if (previousRelay == colornum) return;
        previousRelay = colornum;

        MissionBox.off(pinRed);
        MissionBox.off(pinGreen);
        MissionBox.off(pinYellow);

        if (colornum < colors.length) {
            MissionBox.setScheme(colors[colornum], schemes[colornum]);
        }

    }

}
