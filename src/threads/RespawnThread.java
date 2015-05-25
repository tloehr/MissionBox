package threads;

import interfaces.TextLabelDisplay;
import misc.AEPlayWave;
import misc.Tools;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tloehr on 25.04.15.
 */
public class RespawnThread extends Thread {
    private final ResourceBundle lang;
    private TextLabelDisplay textLabelDisplay;
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

    public RespawnThread(TextLabelDisplay textLabelDisplay, int time2respawn) {
        super();

        this.textLabelDisplay = textLabelDisplay;
        this.time2respawn = time2respawn;
        lang = ResourceBundle.getBundle("Messages");

        setName("threads.RespawnThread");

    }

    public void run() {
        while (!isInterrupted()) {
            try {
                textLabelDisplay.setText(lang.getString("assault.seconds.for.reinforcements") + ": " + (time2respawn - secondsSinceLastRespawn));
                secondsSinceLastRespawn++;
                if (secondsSinceLastRespawn > time2respawn) {
                    secondsSinceLastRespawn = 0;
                    AEPlayWave playWave = new AEPlayWave(Tools.SND_MINIONS_SPAWNED);
                    playWave.start();
                }
                Thread.sleep(1000); // Milliseconds
            } catch (InterruptedException ie) {
                LOGGER.log(Level.FINE, "RespawnThread interrupted!");
            }
        }
    }
}
