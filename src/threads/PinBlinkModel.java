package threads;

import interfaces.Relay;
import misc.SoundUtils;
import org.apache.log4j.Logger;

import javax.sound.sampled.LineUnavailableException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;


/**
 * Created by tloehr on 14.07.16.
 */
public class PinBlinkModel implements Callable<String> {

    private final int msecs;
    private final int hz;

    Relay pin;
    private ArrayList<Long> onOffScheme;
    int repeat;
    boolean currentlyOn;
    int positionInScheme;
    private final Logger logger = Logger.getLogger(getClass().getName());
    String infinity = "\u221E";


    @Override
    public String call() throws Exception {

        if (repeat == 0) {
            restart();
            pin.setOn(false);
        } else {
            for (int turn = 0; turn < repeat; turn++) {
                restart();

                while (hasNext()) {
                    long time = 0;

                    if (Thread.currentThread().isInterrupted()) {
                        pin.setOn(false);
                        return null;
                    }


                    time = next();
                    pin.setOn(currentlyOn);


//                    // debug sound output
//                    if (hz > 0) {
//                        try {
//                            SoundUtils.tone(hz, msecs);
//                        } catch (LineUnavailableException e) {
//                            // dont care
//                        }
//                    }


                    try {
                        if (time > 0) Thread.sleep(time);
                    } catch (InterruptedException exc) {
                        pin.setOn(false);
                        return null;
                    }

                }
            }
        }
        pin.setText("");
        return null;
    }

    public void clear() {
        onOffScheme.clear();
        restart();
    }

    public PinBlinkModel(Relay pin) {
        this(pin, 0, 0);
    }

    public PinBlinkModel(Relay pin, int hz, int msecs) {
        this.hz = hz;
        this.msecs = msecs;

        this.onOffScheme = new ArrayList<>();
        this.positionInScheme = -1;
        this.pin = pin;
        this.currentlyOn = false;
        this.repeat = Integer.MAX_VALUE;
    }

    /**
     * accepts a blinking scheme as a String formed like this: "repeat;ontimeINms;offtimeINms".
     * if repeat is 0 then a previous blinking process is stopped and the pin is set to OFF.
     * There is no "BLINK FOREVER" really. But You could always put Integer.MAX_VALUE as REPEAT instead into the String.
     *
     * @param scheme
     */
    public void setScheme(String scheme) {
        onOffScheme.clear();

//        logger.debug("new scheme for pin: " + pin.getName() + " : " + scheme);

        String[] splitScheme = scheme.trim().split(";");

        String textScheme = "";
        this.repeat = Integer.parseInt(splitScheme[0]);
        textScheme = (this.repeat == Integer.MAX_VALUE ? infinity : Integer.toString(this.repeat));

        if (repeat > 0) {
            StringTokenizer st = new StringTokenizer(splitScheme[1], ",");
            textScheme += ";";
            while (st.hasMoreElements()) {
                long myLong = Long.parseLong(st.nextToken());
                textScheme += (myLong == Long.MAX_VALUE ? infinity : myLong) + (st.hasMoreElements() ? "," : "");
                this.onOffScheme.add(myLong);
            }
        }

        pin.setText(textScheme);
    }


    private void restart() {
        currentlyOn = false;
        positionInScheme = 0;
    }

    private boolean hasNext() {
        return positionInScheme + 1 <= onOffScheme.size();
    }

    private long next() {
        long next = onOffScheme.get(positionInScheme);
        currentlyOn = !currentlyOn;
        positionInScheme++;
        return next;
    }


}
