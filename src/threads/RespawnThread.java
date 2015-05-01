package threads;

import interfaces.DisplayTarget;
import misc.AEPlayWave;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tloehr on 25.04.15.
 */
public class RespawnThread extends Thread {
    private final ResourceBundle lang;
    private DisplayTarget displayTarget;
    private int secondsSinceLastRespawn, time2respawn;
    public final Logger LOGGER = Logger.getLogger(getName());


    public void restart() {
        secondsSinceLastRespawn = 0;
    }

    public void setTimer(int time2respawn) {
        this.time2respawn = time2respawn;
    }

    public void pause(){

    }

    public RespawnThread(DisplayTarget displayTarget, int time2respawn) {
        super();

        this.displayTarget = displayTarget;
        this.time2respawn = time2respawn;
        lang = ResourceBundle.getBundle("Messages");

        setName("threads.RespawnThread");

    }

    public void run() {
        while (!isInterrupted()) {
            try {
                displayTarget.setText(lang.getString("assault.seconds.for.reinforcements") + ": " + (time2respawn - secondsSinceLastRespawn));
                secondsSinceLastRespawn++;
                if (secondsSinceLastRespawn > time2respawn) {
                    secondsSinceLastRespawn = 0;
                    AEPlayWave playWave = new AEPlayWave("/local/codepad.wav");
                    playWave.start();
                }
                Thread.sleep(1000); // Milliseconds
            } catch (InterruptedException ie) {
                LOGGER.log(Level.FINE, "RespawnThread interrupted!");
            }
        }
    }
}
