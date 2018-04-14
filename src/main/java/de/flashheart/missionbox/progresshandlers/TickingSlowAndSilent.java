package de.flashheart.missionbox.progresshandlers;

import de.flashheart.missionbox.Main;
import de.flashheart.missionbox.interfaces.HasLogger;
import de.flashheart.missionbox.interfaces.PercentageInterface;

import org.apache.log4j.Logger;
import org.joda.time.Interval;
import org.joda.time.Seconds;

import java.math.BigDecimal;

/**
 * Diese Klasse unterteilt den Zeitraum zwischen 1% und 100% in 4 Viertel. Zu Beginn jeden Viertels ertönt einmalig das Einleitungssignal
 * gefolgt von 1 - 4 kurzen Signalen, je nachdem welches Viertel jetzt gerade läuft.
 */
public class TickingSlowAndSilent extends PercentageInterface implements HasLogger {
    private int previousQuarter = -1;
//    private final Logger logger = Logger.getLogger(getClass());
    private final String key;
    private final StringBuilder tickingScheme;
    private final long repeats;

    /**
     * @param siren - der Schlüssel der Sirene, die benutzt werden soll.
     */
    public TickingSlowAndSilent(String siren) {
        super("Ticking Slow and Silent");

        // maximale anzahl von 10 sekunden intervalle bei einer max. spielzeit von 60 Minuten. Müsste was elegantersein.
        // wenn ich das berechnen soll, muss ich ein weg finden, dass neu zu setzen, wenn jemand das während der laufzeit ändet
        // idee: die drei handlers neu erstellen, wenn jemand die parameter ändert.
        // todo: später...
        repeats = 90;

        tickingScheme = new StringBuilder(6 * 25);
        this.key = siren;

    }


    public void setValue(BigDecimal percent) {
        if (percent.compareTo(BigDecimal.ZERO) < 0 || percent.compareTo(new BigDecimal(100)) >= 0) {
            previousQuarter = -1;
            Main.getPinHandler().off(key);
            return;
        }

        // sind wir in den letzten 10 sekunden ?
        // dann brauchen wir keinen progress mehr.
        Interval remaining = new Interval(now, end);
        if (Seconds.secondsIn(remaining).getSeconds() <= 10) return;

        int quarter = -1;
        if (percent.compareTo(new BigDecimal(25)) <= 0) {
            quarter = 1;
        } else if (percent.compareTo(new BigDecimal(25)) > 0 && (percent.compareTo(new BigDecimal(50)) <= 0)) {
            quarter = 2;
        } else if (percent.compareTo(new BigDecimal(50)) > 0 && (percent.compareTo(new BigDecimal(75)) <= 0)) {
            quarter = 3;
        } else {
            quarter = 4;
        }


        if (quarter == previousQuarter) return;
        previousQuarter = quarter;


        tickingScheme.setLength(0);

        // intro signal zu beginn jedes drittels
        //tickingScheme.append("1;70,25,70,25,70,25,800,75,");  // beloved shuttle bay doors
        tickingScheme.append("1:");  

        // Achtung. Bei längeren Spielzeit (über 120 Minuten) muss der Source-Code geändert werden.
        // dann müssen die schleifen auf 500 statt 250 erhöht werden.
        if (quarter == 1) {
            tickingScheme.append("on,2000;off,10000;");
            for (int t = 0; t < repeats; t++) {
                tickingScheme.append("on,100;off,10000;");
            }
        } else if (quarter == 2) {
            for (int t = 0; t < repeats; t++) {
                tickingScheme.append("on,100;off,100;on,100;off,10000;");
            }
        } else if (quarter == 3) {
            for (int t = 0; t < repeats; t++) {
                tickingScheme.append("on,100;off,100;on,100;off,100;on,100;off,10000;");
            }
        } else {
            for (int t = 0; t < repeats; t++) {
                tickingScheme.append("on,100;off,100;on,100;off,100;on,100;off,100;on,100;off,10000;");
            }
        }

        Main.getPinHandler().setScheme(key, tickingScheme.toString());


    }
}
