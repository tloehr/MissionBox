package main;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import events.MessageEvent;
import events.MessageListener;
import gamemodes.Farcry1Assault;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.lf5.LogLevel;


/**
 * Created by tloehr on 22.04.15.
 */
public class MissionBox {


    public static final int INFO_TIME = 2;
    public static int TIME2RESPAWN = 20, MAXCYLCES = 2, SECONDS2CAPTURE = 60;
    protected static Logger logger;
    public static Level logLevel = Level.DEBUG;
    public final static GpioController GPIO = GpioFactory.getInstance();

    public static final void main(String[] args) throws Exception {

//        FrmMain frmMain = new FrmMain();
        logger = Logger.getRootLogger();
        PatternLayout layout = new PatternLayout("%d{ISO8601} %-5p [%t] %c: %m%n");
        ConsoleAppender consoleAppender = new ConsoleAppender(layout);
        logger.addAppender(consoleAppender);


        final GpioPinDigitalInput btnFlagTrigger = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_02, "FlagTrigger", PinPullResistance.PULL_DOWN);
        final GpioPinDigitalInput btnGameStart = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_03, "GameStart", PinPullResistance.PULL_DOWN);
        final GpioPinDigitalOutput ledRed = GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_06, "LedRED", PinState.HIGH);
        ledRed.setShutdownOptions(true, PinState.LOW);
        final GpioPinDigitalOutput ledYellow = GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_05, "LedYELLOW", PinState.HIGH);
        ledYellow.setShutdownOptions(true, PinState.LOW);
        final GpioPinDigitalOutput ledGreen = GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_04, "LedGREEN", PinState.HIGH);
        ledGreen.setShutdownOptions(true, PinState.LOW);

        MessageListener textListener = new MessageListener() {
            @Override
            public void messageReceived(MessageEvent messageEvent) {
                logger.debug("TextMessage received");
            }
        };

        MessageListener gameTimeListener = new MessageListener() {
            @Override
            public void messageReceived(MessageEvent messageEvent) {
                logger.debug("GameTime received");
            }
        };

        MessageListener percentageListener = new MessageListener() {
            @Override
            public void messageReceived(MessageEvent messageEvent) {
                logger.debug("% received");
            }
        };

        MessageListener gameModeListener = new MessageListener() {
            @Override
            public void messageReceived(MessageEvent messageEvent) {
                logger.debug("gameMode changed: " + Farcry1Assault.GAME_MODES[messageEvent.getMode()]);
                if (messageEvent.getMode() == Farcry1Assault.GAME_FLAG_COLD){
                    ledRed.low();
                    ledYellow.high();
                    ledGreen.low();
                } else if (messageEvent.getMode() == Farcry1Assault.GAME_FLAG_HOT){
                    ledRed.low();
                    ledYellow.low();
                    ledGreen.high();
                } else if (messageEvent.getMode() == Farcry1Assault.GAME_FLAG_ACTIVE){
                    ledRed.low();
                    ledYellow.low();
                    ledGreen.high();
                } else {
                    ledRed.low();
                    ledYellow.low();
                    ledGreen.low();
                }
            }
        };




        Farcry1Assault farcryAssaultThread = new Farcry1Assault(textListener, gameTimeListener, percentageListener, gameModeListener, MAXCYLCES, SECONDS2CAPTURE);


        MessageListener actionListener = messageEvent -> {
            logger.debug("Button selected: " + messageEvent.isOn());
            farcryAssaultThread.toggleFlag();
        };

        btnFlagTrigger.addListener((GpioPinListenerDigital) event -> {
            logger.debug("btnFlagTrigger");
            farcryAssaultThread.toggleFlag();
        });

        btnGameStart.addListener((GpioPinListenerDigital) event -> {
            logger.debug("btnGameStart");
            farcryAssaultThread.startGame();
        });


        farcryAssaultThread.run();
//        for (;;) {
//            Thread.sleep(500);
//        }


//        FrmMain frmMain = new FrmMain(actionListener);
//        frmMain.setVisible(true);


    }

}
