package threads;

import interfaces.DisplayTarget;
import interfaces.ProgressInterface;
import interfaces.SoundClip;
import misc.AEPlayWave;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tloehr on 25.04.15.
 */
public class DisplayThread extends Thread {
    private final ResourceBundle lang;
    //    private DisplayTarget displayTarget;
    public final Logger LOGGER = Logger.getLogger(getName());
    private int cycle = 0;
    private final ProgressInterface progressTarget;
    private int MAXCYCLES = 0;
    private int gameState;
    private AEPlayWave playWave;


    public static final int GAME_PRE_GAME = 0;
    public static final int GAME_FLAG_ACTIVE = 1;
    public static final int GAME_FLAG_COLD = 2;
    public static final int GAME_FLAG_HOT = 3;
    public static final int GAME_FLAG_TAKEN = 4;
    public static final int GAME_AFTER_GAME = 5;


    private final DisplayTarget dp1;//, dp2, dp3, dp4;


    public DisplayThread(DisplayTarget displayTarget, ProgressInterface progressTarget, int maxcycles) {
        super();
        this.progressTarget = progressTarget;
        gameState = GAME_PRE_GAME;

        MAXCYCLES = maxcycles;
        this.dp1 = displayTarget;


        LOGGER.setLevel(Level.FINEST);

        lang = ResourceBundle.getBundle("Messages");

        setName("threads.DisplayThread");


    }

    public void setGameState(int state) {
        this.gameState = state;

    }

    public void toggleFlag() {
        if (gameState == GAME_PRE_GAME || gameState == GAME_AFTER_GAME) return;

        if (gameState == GAME_FLAG_COLD) gameState = GAME_FLAG_HOT;
        else gameState = GAME_FLAG_COLD;

    }

    public void run() {
        while (!isInterrupted()) {
            try {


                switch (gameState) {
                    case GAME_PRE_GAME: {
                        dp1.setText("assault.gamestate.pre.game");
                        break;
                    }
                    case GAME_FLAG_ACTIVE: {
                        dp1.setText("assault.gamestate.flag.is.active");
                        break;
                    }
                    case GAME_FLAG_HOT: {
                        dp1.setText("assault.gamestate.flag.is.hot");
                        break;
                    }
                    case GAME_FLAG_COLD: {
                        dp1.setText("assault.gamestate.flag.is.cold");
                        break;
                    }
                    case GAME_FLAG_TAKEN: {
                        dp1.setText("assault.gamestate.flag.is.taken");
                        break;
                    }
                    case GAME_AFTER_GAME: {
                        dp1.setText("assault.gamestate.after.game");
                        break;
                    }
                    default: {
                        
                    }
                }

                if (gameState == GAME_PRE_GAME)
                    dp1.setText("assault.flag.is.on");

                dp1.setText(activated ? lang.getString("assault.flag.is.on") : lang.getString("assault.flag.is.off"));

                if (activated) {

                    if (playWave == null) {
                        playWave = new AEPlayWave("/local/capture_siren.wav", event -> {
                            if (event.getType() == LineEvent.Type.STOP) {
                                cycle++;
                                progressTarget.setValue(cycle);
                            } else if (event.getType() == LineEvent.Type.CLOSE) {
                                playWave = null;
                            }
                        });
                    }

                    if (cycle == MAXCYCLES) {
                        cycle++;
                        playWave = new AEPlayWave("/local/MP_flare.wav", event -> {
                            if (event.getType() == LineEvent.Type.STOP) {
                                interrupted = true;
                            }
                        });
                    }
                    if (!playWave.isAlive()) {
                        playWave.start();
                    }
                } else {
                    cycle = 0;
                    progressTarget.setValue(cycle);
                    if (playWave != null) {
                        playWave.stopSound();
                    }
                }

                Thread.sleep(50); // Milliseconds

            } catch (InterruptedException ie) {
                interrupted = true;
                LOGGER.log(Level.FINE, "DisplayThread interrupted!");
            }
        }
    }
}
