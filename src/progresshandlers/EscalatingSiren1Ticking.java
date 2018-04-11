package progresshandlers;

import interfaces.PercentageInterface;
import main.Main;
import org.apache.log4j.Logger;

import java.math.BigDecimal;

/**
 * This class progresses through a group of pins (defined by their keys within the outputMap. According to the number of defined pins
 * and the current percentage set by setValue(), the corresponding pin is chosen to blink in the frequency pulsetimeinmillis.
 */
public class EscalatingSiren1Ticking extends PercentageInterface {
    //    long pulsetimeinmillis = 1000; // Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.MBX_SIREN_TIME));
    private int previousTenth = -1;
    protected final Logger logger = Logger.getLogger(getClass());
    protected final String key;

//    private String FOREVER = Integer.toString(Integer.MAX_VALUE);
//    private long MAXTIMEFORSIGNAL = 5000;


    /**
     * @param siren
     */
    public EscalatingSiren1Ticking(String siren) {
        super("Escalating Siren1 ticking");
        this.key = siren;
        logger.setLevel(Main.getLogLevel());
    }


    public void setValue(BigDecimal percent) {
//       logger.debug("PERCENT: " + percent);

        if (percent.compareTo(BigDecimal.ZERO) < 0 || percent.compareTo(new BigDecimal(100)) >= 0) {
            Main.off(key);
            previousTenth = -1;
            return;
        }


        int tenth = new BigDecimal(percent.intValue() / 10).multiply(BigDecimal.TEN).intValue();

//        int tenth = percent.setScale(-1, RoundingMode.HALF_UP).intValue(); // Scale to the power of tenth

        if (tenth == previousTenth) return;
        previousTenth = tenth;

        // je weiter richtung Sieg, desto kürzer die Abstände
        int tickPause = 100 * (100-tenth) / 10;
        String tickingSound = "";
        for (int t = 0; t < 9; t++) {
            tickingSound += String.format("50,%d,", tickPause);
        }
        // der letzte ohne das nachfolgende Komma
        // deswegen läuft die vorige schleife auch nur bis 9
        tickingSound += String.format("50,%d", tickPause);


        // hundertmal sollten oft genug sein
        Main.setScheme(key, "100;70,25,70,25,70,25,800,75," + tickingSound);

    }
}
