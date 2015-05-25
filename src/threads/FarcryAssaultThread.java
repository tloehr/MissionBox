package threads;

import interfaces.TextLabelDisplay;
import interfaces.PercentageInterface;
import misc.AEPlayWave;
import misc.Tools;

import javax.sound.sampled.LineEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tloehr on 25.04.15.
 */
public class FarcryAssaultThread extends Thread {

    //    private TextLabelDisplay displayTarget;
    public final Logger LOGGER = Logger.getLogger(getName());
    private int cycle = 0;
    private final PercentageInterface progressTarget;
    private final int seconds2capture;
    private int MAXCYCLES;
    private int gameState;
    private AEPlayWave playWave, playFlagHot, playFlagCold, playAfterGame;
    private long starttime = 0, endtime = 0;
    private boolean playedCold = true; // so that the shutdown is not constantly ringing. only once.


    public static final int GAME_PRE_GAME = 0;
    public static final int GAME_FLAG_ACTIVE = 1;
    public static final int GAME_FLAG_COLD = 2;
    public static final int GAME_FLAG_HOT = 3;
    public static final int GAME_ROCKET_LAUNCHED = 5;
    public static final int GAME_OUTCOME_FLAG_TAKEN = 6;
    public static final int GAME_OUTCOME_FLAG_DEFENDED = 7;

    DateFormat formatter = new SimpleDateFormat("mm:ss");

    private final TextLabelDisplay messageTarget, gameTimer;//, dp2, dp3, dp4;


    public FarcryAssaultThread(TextLabelDisplay messageTarget, TextLabelDisplay gameTimer, PercentageInterface progressTarget, int maxcycles, int seconds2capture) {
        super();
        this.gameTimer = gameTimer;
        this.progressTarget = progressTarget;
        this.seconds2capture = seconds2capture;

        gameState = GAME_PRE_GAME;

        MAXCYCLES = maxcycles;


        this.messageTarget = messageTarget;

        LOGGER.setLevel(Level.FINEST);

        setName("threads.FarcryAssaultThread");
    }

    public void setGameState(int state) {
        this.gameState = state;

    }

    public void toggleFlag() {
        if (gameState == GAME_PRE_GAME || gameState == GAME_OUTCOME_FLAG_TAKEN || gameState == GAME_OUTCOME_FLAG_DEFENDED)
            return;

        if (gameState == GAME_FLAG_ACTIVE) gameState = GAME_FLAG_COLD;

        if (gameState == GAME_FLAG_COLD) gameState = GAME_FLAG_HOT;
        else gameState = GAME_FLAG_COLD;

    }

    public void run() {
        while (!isInterrupted()) {

            if (gameState == GAME_FLAG_COLD && System.currentTimeMillis() > endtime) {
                gameState = GAME_OUTCOME_FLAG_DEFENDED;
            }


            String dateFormatted = "00:00:00";
            if (endtime > System.currentTimeMillis()) {
                Date date = new Date(endtime - System.currentTimeMillis());
                System.out.println(endtime - System.currentTimeMillis());
                dateFormatted = formatter.format(date);
            }


            gameTimer.setText(dateFormatted);

            try {
                switch (gameState) {
                    case GAME_PRE_GAME: {
                        messageTarget.setText("assault.gamestate.pre.game");
                        if (playWave == null || !playWave.isAlive()) {
                            playWave = new AEPlayWave(Tools.SND_WELCOME, event -> {
                                if (event.getType() == LineEvent.Type.CLOSE) {
                                    gameState = GAME_FLAG_ACTIVE;
                                }
                            });

                            playWave.start();
                        }
                        break;
                    }
                    case GAME_FLAG_ACTIVE: {
                        messageTarget.setText("assault.gamestate.flag.is.active");
                        if (playWave == null || !playWave.isAlive()) {
                            starttime = System.currentTimeMillis();
                            endtime = starttime + (seconds2capture * 1000);
                            playWave = new AEPlayWave(Tools.SND_SIREN, event -> {
                                if (event.getType() == LineEvent.Type.CLOSE) {
                                    gameState = GAME_FLAG_COLD;
                                    starttime = System.currentTimeMillis();
                                    endtime = starttime + (seconds2capture * 1000);
                                }
                            });
                            playWave.start();
                        }
                        break;
                    }
                    case GAME_FLAG_HOT: {
                        messageTarget.setText("assault.gamestate.flag.is.hot");

                        playedCold = false;

                        if (playFlagCold != null) {
                            if (playFlagCold.isAlive()) {
                                playFlagCold.stopSound();
                            }
                            playFlagCold = null;
                        }

                        if (playFlagHot == null && cycle < MAXCYCLES) {
                            playFlagHot = new AEPlayWave(Tools.SND_SIREN, event -> {
                                if (event.getType() == LineEvent.Type.STOP) {
                                    cycle++;
                                    progressTarget.setValue(cycle);
                                } else if (event.getType() == LineEvent.Type.CLOSE) {
                                    playFlagHot = null;
                                }
                            });
                            playFlagHot.start();
                        }

                        if (cycle >= MAXCYCLES) {
//                            cycle++;
                            gameState = GAME_ROCKET_LAUNCHED;
                        }
//
//                        if (playFlagHot != null && !playFlagHot.isAlive()) {
//                            playFlagHot.start();
//                        }
                        break;
                    }
                    case GAME_ROCKET_LAUNCHED: {
                        if (playWave == null || !playWave.isAlive()) {

                            if (playFlagHot!= null && playFlagHot.isAlive()){
                                playFlagHot.stopSound();
                            }

                            playWave = new AEPlayWave(Tools.SND_FLARE, event -> {
                                if (event.getType() == LineEvent.Type.STOP) {
                                    gameState = GAME_OUTCOME_FLAG_TAKEN;
                                }
                            });
                            playWave.start();
                        }
                        break;
                    }
                    case GAME_FLAG_COLD: {
                        messageTarget.setText("assault.gamestate.flag.is.cold");
                        cycle = 0;
                        progressTarget.setValue(cycle);

                        if (!playedCold) {
                            if (playFlagHot != null) {
                                if (playFlagHot.isAlive()) {
                                    playFlagHot.stopSound();
                                }
                                playFlagHot = null;
                            }


                            if (playFlagCold == null) {
                                playFlagCold = new AEPlayWave(Tools.SND_SHUTDOWN, event -> {
                                    if (event.getType() == LineEvent.Type.CLOSE) {
                                        playFlagCold = null;
                                    }
                                });
                                playFlagCold.start();
                            }
                        }
                        playedCold = true;

                        break;
                    }
                    case GAME_OUTCOME_FLAG_TAKEN: {
                        messageTarget.setText("assault.gamestate.outcome.flag.taken");
                        if (playWave != null && playWave.isAlive()) {
                            playWave.stopSound();
                        }

                        if (playAfterGame == null) {
                            playAfterGame = new AEPlayWave(Tools.SND_VICTORY, event -> {
                                if (event.getType() == LineEvent.Type.CLOSE) {

                                }
                            });
                            playAfterGame.start();
                        }
                        break;
                    }
                    case GAME_OUTCOME_FLAG_DEFENDED: {
                        messageTarget.setText("assault.gamestate.outcome.flag.defended");

                        if (playWave != null && playWave.isAlive()) {
                            playWave.stopSound();
                        }

                        if (playAfterGame == null) {
                            playAfterGame = new AEPlayWave(Tools.SND_DEFEAT, event -> {
                                if (event.getType() == LineEvent.Type.CLOSE) {

                                }
                            });
                            playAfterGame.start();
                        }

                        break;
                    }

                    default: {
                        messageTarget.setText("msg.error");
                    }
                }


                Thread.sleep(50); // Milliseconds

            } catch (InterruptedException ie) {

                LOGGER.log(Level.FINE, "FarcryAssaultThread interrupted!");
            }
        }
    }
}
