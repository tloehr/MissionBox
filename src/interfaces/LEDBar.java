package interfaces;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by tloehr on 07.06.15.
 */
public class LEDBar implements PercentageInterface, Runnable {

    public final int PERCENTAGE = 0;
    public final int SIMPLE = 1;
    public final int CYLON = 2;

    private final GpioController GPIO;
    private final ArrayList<GpioPinDigitalOutput> myLEDs;
    private final Thread thread;
    private int barrier = 0, prevBarrier = 0, cyloneposition = 0;
    private int flashmode = 0;
    private long SLEEPTIME = 25;
    private long simple_frequency = 8;
    private long cycle = 0;
    private boolean odd = true;
    private Logger logger = Logger.getLogger(this.getClass());


    public LEDBar(GpioController GPIO, ArrayList<GpioPinDigitalOutput> myLEDs) {
        this.GPIO = GPIO;
        this.myLEDs = myLEDs;
        this.thread = new Thread(this);
//        for (int ledOFF = 0; ledOFF < myLEDs.size(); ledOFF++) {
//            GPIO.setState(false, myLEDs.get(ledOFF));
//        }
//
        flashmode = PERCENTAGE;
        barrier = 0;
        prevBarrier = 0;

        thread.start();
    }

    @Override
    public void run() {

        while (!thread.isInterrupted()) {

            cycle++;

            try {

                if (flashmode == PERCENTAGE) {

                    if (prevBarrier != barrier) {
                        prevBarrier = barrier;

                        logger.debug("Barrier: " + barrier);

                        for (int ledON = 0; ledON < barrier; ledON++) {
                            GPIO.setState(true, myLEDs.get(ledON));
                        }
                        for (int ledOFF = barrier; ledOFF < myLEDs.size(); ledOFF++) {
                            GPIO.setState(false, myLEDs.get(ledOFF));
                        }
                    }

                } else if (flashmode == SIMPLE) {
                    if (cycle % simple_frequency == 0) {
                        for (int ledON = 0; ledON < myLEDs.size(); ledON++) {
                            GPIO.setState(odd, myLEDs.get(ledON));
                        }
                        odd = !odd;
                    }
                } else if (flashmode == CYLON) {
//                    if (cycle % simple_frequency == 0) {

                        if (odd) {
                            cyloneposition++;
                        } else {
                            cyloneposition--;
                        }

                        if (cyloneposition >= myLEDs.size()) {
                            odd = false;
                            cyloneposition = myLEDs.size() - 1;
                        } else if (cyloneposition < 0) {
                            odd = true;
                            cyloneposition = 0;
                        }


                        for (int ledOFF = 0; ledOFF < myLEDs.size(); ledOFF++) {
                            GPIO.setState(false, myLEDs.get(ledOFF));
                        }
                        GPIO.setState(true, myLEDs.get(cyloneposition));

//                    }
                }

                Thread.sleep(SLEEPTIME);
            } catch (InterruptedException ie) {
                logger.debug(this + " interrupted!");
            }
        }
    }

    @Override
    public void setValue(BigDecimal percent) {
        flashmode = PERCENTAGE;
        barrier = new BigDecimal(myLEDs.size()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).multiply(percent).intValue();
    }

    public void cleanup() {

        thread.interrupt();

    }

    public void setSimple() {
        flashmode = SIMPLE;
        simple_frequency = 16;
        odd = true;
    }

    public void setCylon() {
        flashmode = CYLON;
        simple_frequency = 8;
        odd = true;
    }

    public void setOff() {
        setValue(BigDecimal.ZERO);
    }

}
