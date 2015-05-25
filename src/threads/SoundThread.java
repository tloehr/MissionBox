package threads;

import interfaces.SoundClip;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tloehr on 25.04.15.
 */
public class SoundThread extends Thread {
    private final ResourceBundle lang;
    private SoundClip clip;
    //    private TextLabelDisplay displayTarget;
    private boolean interrupted = false;
    public final Logger LOGGER = Logger.getLogger(getName());

    int state = 0;

    final int STATE_IDLE = 0;
    final int STATE_START = 1;
    final int STATE_STOP = 2;

    public boolean isInterrupted() {
        return interrupted;
    }

    public SoundThread(SoundClip clip) {
        super();
        this.clip = clip;
        state = STATE_IDLE;

        lang = ResourceBundle.getBundle("Messages");

        setName("threads.FarcryAssaultThread");
        interrupted = false;

    }

    public void playSound(){
        state = STATE_START;
    }

    public void stopSound(){
        state = STATE_STOP;
    }



    public void run() {
        while (!interrupted) {
            try {

                if (state == STATE_START) {
                    clip.play();
                    state = STATE_IDLE;
                }

                if (state == STATE_STOP) {
                    clip.stop();
                    state = STATE_IDLE;
                }



                Thread.sleep(50); // Milliseconds

            } catch (InterruptedException ie) {
                interrupted = true;
                LOGGER.log(Level.FINE, "PrintProcessor interrupted!");
            }
        }
    }
}
