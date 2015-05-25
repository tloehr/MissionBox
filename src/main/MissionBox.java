package main;

import events.MessageEvent;
import events.MessageListener;
import gamemodes.Farcry1Assault;
import threads.FarcryAssaultThread;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tloehr on 22.04.15.
 */
public class MissionBox {

    public static final Logger LOGGER = Logger.getLogger(MissionBox.class.getName());
    public static final Level LOGLEVEL = Level.FINEST;
    public static final int INFO_TIME = 2;
    public static  int TIME2RESPAWN = 20, MAXCYLCES = 2, SECONDS2CAPTURE = 60;

    public static final void main(String[] args) {

//        FrmMain frmMain = new FrmMain();
//        frmMain.setVisible(true);


        LOGGER.setLevel(LOGLEVEL);

        MessageListener textListener = new MessageListener() {
            @Override
            public void messageReceived(MessageEvent messageEvent) {
                LOGGER.fine("TextMessage received");
            }
        };

        MessageListener gameTimeListener = new MessageListener() {
            @Override
            public void messageReceived(MessageEvent messageEvent) {
                LOGGER.fine("GameTime received");
            }
        };

        MessageListener percentageListener = new MessageListener() {
            @Override
            public void messageReceived(MessageEvent messageEvent) {
                LOGGER.fine("% received");
            }
        };

        MessageListener gameModeListener = new MessageListener() {
            @Override
            public void messageReceived(MessageEvent messageEvent) {
                LOGGER.fine("gameMode changed: " + Farcry1Assault.GAME_MODES[messageEvent.getMode()]);
            }
        };





        Farcry1Assault farcryAssaultThread = new Farcry1Assault(textListener, gameTimeListener, percentageListener, gameModeListener, MAXCYLCES, SECONDS2CAPTURE);

        MessageListener actionListener = new MessageListener() {
            @Override
            public void messageReceived(MessageEvent messageEvent) {
                LOGGER.fine("Button selected: " + messageEvent.isOn());
                farcryAssaultThread.toggleFlag();
            }
        };


        FrmMain frmMain = new FrmMain(actionListener);
        frmMain.setVisible(true);


    }
}
