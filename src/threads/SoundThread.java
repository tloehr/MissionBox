package threads;

import threads.AEPlayWave;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ResourceBundle;


/**
 * Created by tloehr on 25.04.15.
 */
public class SoundThread implements Runnable {
    private final ResourceBundle lang;

    //    private TextLabelDisplay displayTarget;
    private boolean interrupted = false;
    public final Logger LOGGER = Logger.getLogger("threads.SoundThread");

    HashMap<String, AEPlayWave> clips;

    private Thread thread;

    public boolean isInterrupted() {
        return interrupted;
    }



    public SoundThread() {
        super();

        clips = new HashMap<>();
        thread = new Thread(this);


        lang = ResourceBundle.getBundle("Messages");

        interrupted = false;
    }

    public void addClip(String key, AEPlayWave clip){
        clips.put(key, clip);
    }

    public void play(String key, int repeat){

    }

    @Override
    public void run() {
        while (!interrupted) {
            try {





                Thread.sleep(50); // Milliseconds
            } catch (InterruptedException ie) {
                interrupted = true;
                LOGGER.debug("PrintProcessor interrupted!");
            }
        }
    }
}
