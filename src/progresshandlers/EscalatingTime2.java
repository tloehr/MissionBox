package progresshandlers;

import interfaces.PercentageInterface;
import main.MissionBox;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * This class progresses through a group of pins (defined by their keys within the outputMap. According to the number of defined pins
 * and the current percentage set by setValue(), the corresponding pin is chosen to blink in the frequency pulsetimeinmillis.
 */
public class EscalatingTime2 extends PercentageInterface {
    long pulsetimeinmillis = 1000; // Integer.parseInt(MissionBox.getConfig().getProperty(MissionBox.MBX_SIREN_TIME));
    private int previousTenth = -1;
    protected final Logger logger = Logger.getLogger(getClass());
    protected final String key;
    private String FOREVER = Integer.toString(Integer.MAX_VALUE);
    private long MAXTIMEFORSIGNAL = 10000, SIGNALTIME = 200;

    /**
     * @param key
     */
    public EscalatingTime2(String key) {
        super("Escalating over Time. Short Signals.");
        this.key = key;
    }


    public void setValue(BigDecimal percent) {

        if (percent.compareTo(BigDecimal.ZERO) < 0) {
            MissionBox.off(key);
            previousTenth = -1;
            return;
        }

        // das ist die Zehnerstelle in dem sich der Prozent Fortschritt gerade befindet.
        // Also 40 für 41,42,43... oder 50 für 55,56,57 usw.
        int tenth = percent.setScale(-1, RoundingMode.HALF_UP).intValue(); // Scale to the power of tenth

        if (tenth == previousTenth) return;
        previousTenth = tenth;


        // Das Signalmuster erfolgt in 10 Sekunden Intervallen.
        // Dabei gibt es den signal und pausen zeitraum
        // Die Sirene bleibt immer nur 200ms sekunden an. gefolgt von einer gleich langen pause
        // bei 40% soll das Signal wie folgt aussehen
        //
        // FOREVER;200;200;200;200;200;200;200;8600
        //
        // also:4x 200ms on und off, bis auf das letzte mal pause (die soll ja bis zum Ende des Intervalls dauern.
        // also 7x200ms. Danach 10000 - (7x200ms)

        // todo: signalfolgen mal manuell ausprobieren und dann einfach hardcoden
        String signalPattern = "FOREVER;";
        if (tenth < 10) {
            signalPattern += "200,500,50,50,200,500,0,8500";
        } else {
            signalPattern += "200,500,50,50,200,500,0,8500";
        }


        long onTime = -1, offTime = -1;
        if (tenth < 10) {
            onTime = 200;
        } else {
            onTime = MAXTIMEFORSIGNAL / 100 * tenth;
        }

        offTime = MAXTIMEFORSIGNAL - onTime;


//        logger.debug("onTime " + onTime);
//        logger.debug("offTime " + offTime);

        MissionBox.setScheme(key, FOREVER + ";" + onTime + "," + offTime);
    }
}
