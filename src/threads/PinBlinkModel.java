package threads;

import interfaces.Relay;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;


/**
 * Created by tloehr on 14.07.16.
 */
public class PinBlinkModel implements Callable<String> {

    Relay pin;
    ArrayList<Long> onOffScheme;
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
                    if (Thread.currentThread().isInterrupted()) {
                        pin.setOn(false);
//                        logger.debug(pin.getName() + ": interrupted");
                        pin.setText("");
                        return null;
                    }

                    long time = next();
                    pin.setOn(currentlyOn);

                    try {
//                        logger.debug(time + "ms");
                        if (time > 0) Thread.sleep(time);
                    } catch (InterruptedException exc) {
                        pin.setOn(false);
//                        logger.debug(pin.getName() + ": interrupted");
                        pin.setText("");
                        return null;
                    }
                }
            }
        }
        pin.setText("");
        return null;
    }

    public PinBlinkModel(Relay pin) {
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
    public void setScheme(String scheme) throws Exception {
        onOffScheme.clear();

        logger.debug("new scheme for pin: " + pin.getName() + " : " + scheme);

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
