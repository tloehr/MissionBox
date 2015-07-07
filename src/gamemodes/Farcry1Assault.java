package gamemodes;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;
import interfaces.LEDBar;
import interfaces.MessageListener;
import interfaces.Relay;
import interfaces.RelaySiren;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import main.MissionBox;
import misc.Config;
import misc.Tools;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by tloehr on 31.05.15.
 */
public class Farcry1Assault implements GameModes {

    public static final String SND_TYPE_SIREN = "siren";
    public static final String SND_TYPE_ROCKET = "rocket";
    public static final String SND_TYPE_WINNING = "winning";
    public static final String SND_TYPE_WELCOME = "welcome";
    public static final String SND_TYPE_VICTORY = "victory";
    public static final String SND_TYPE_DEFEAT = "defeat";


    private final Logger logger = Logger.getLogger(getClass());
//    private final Config config;
    private int LCD_ROWS = 2;
    private int LCD_COLUMNS = 16;
    private int LCD_BITS = 4;
    private int TIME2RESPAWN = 20, MAXCYLCES = 500, SECONDS2CAPTURE = 60 * 11;
//    private final ArrayList<GpioPinDigitalOutput> myLEDs = new ArrayList<>();
//    private final ArrayList<GpioPinDigitalOutput> mySirens = new ArrayList<>();
//    private  LEDBar ledBar;
//    private final RelaySiren relaySiren;
//    private final Relay relayStrobe, relayRocket;
//    private final int lcdHandle;

    // a CYCLE takes 50 millis


    private Farcry1AssaultThread farcryAssaultThread;




    public Farcry1Assault() throws IOException {
//        this.config = config;




//        final GpioPinDigitalInput btnFlagTrigger = MissionBox.getConfig().getGPIO().provisionDigitalInputPin(RaspiPin.GPIO_03, "FlagTrigger", PinPullResistance.PULL_DOWN);
//        final GpioPinDigitalInput btnGameStartStop = MissionBox.getConfig().getGPIO().provisionDigitalInputPin(RaspiPin.GPIO_02, "GameStartStop", PinPullResistance.PULL_DOWN);
//        final GpioPinDigitalInput btnMisc = MissionBox.getConfig().getGPIO().provisionDigitalInputPin(RaspiPin.GPIO_00, "MISC", PinPullResistance.PULL_DOWN);
//



//        for (int ledON = 0; ledON < NUMLED4PROGRESS; ledON++) {
//            myLEDs.add(myOutputs[ledON]);
//        }
//
//        mySirens.add(myOutputs[40]);
//        mySirens.add(myOutputs[41]);
//        mySirens.add(myOutputs[42]);
//
//        relayRocket = new Relay(MissionBox.getConfig().getGPIO(), myOutputs[43]);
//        relayStrobe = new Relay(MissionBox.getConfig().getGPIO(), myOutputs[47]);
//
//
//        this.ledBar = new LEDBar(MissionBox.getConfig().getGPIO(), myLEDs);
//        this.relaySiren = new RelaySiren(MissionBox.getConfig().getGPIO(), mySirens);


        // initialize LCD
        // the wiring is according to the examples from https://kofler.info/buecher/raspberry-pi/
        // LCD data bit 8 (set to 0 if using 4 bit communication)
//        lcdHandle = Lcd.lcdInit(LCD_ROWS,     // number of row supported by LCD
//                LCD_COLUMNS,  // number of columns supported by LCD
//                LCD_BITS,     // number of bits used to communicate to LCD
//                11,           // LCD RS pin
//                10,           // LCD strobe pin
//                6,            // LCD data bit 1
//                5,            // LCD data bit 2
//                4,            // LCD data bit 3
//                1,            // LCD data bit 4
//                0,            // LCD data bit 5 (set to 0 if using 4 bit communication)
//                0,            // LCD data bit 6 (set to 0 if using 4 bit communication)
//                0,            // LCD data bit 7 (set to 0 if using 4 bit communication)
//                0);


        // verify initialization
//        if (lcdHandle == -1) {
//            logger.fatal(" ==>> LCD INIT FAILED");
//            return;
//        }

//        TinySound.init();


//        Lcd.lcdClear(lcdHandle);

        MessageListener textListener = messageEvent -> logger.debug(messageEvent.getMessage().toString());

        MessageListener gameTimeListener = messageEvent -> {
//            Lcd.lcdPosition(lcdHandle, 0, 1);
//            Lcd.lcdPuts(lcdHandle, StringUtil.padCenter(messageEvent.getMessage().toString(), LCD_COLUMNS));

            logger.debug(messageEvent.getMessage().toString());

        };

        MessageListener percentageListener = messageEvent -> {

            ledBar.setValue(messageEvent.getPercentage());
            relaySiren.setValue(messageEvent.getPercentage());

        };
//
//        MessageListener gameModeListener = messageEvent -> {
//            logger.debug("gameMode changed: " + Farcry1AssaultThread.GAME_MODES[messageEvent.getMode()]);
////            Lcd.lcdHome(lcdHandle);
////            Lcd.lcdPosition(lcdHandle, 0, 0);
////            Lcd.lcdPuts(lcdHandle, StringUtil.padCenter(Farcry1AssaultThread.GAME_MODES[messageEvent.getMode()], LCD_COLUMNS));
//
//            if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_FLAG_HOT)) {
//                MissionBox.getConfig().getConfigFC1().getPlaySiren().play(true);
//                relayStrobe.setOn();
//            } else if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_FLAG_COLD)) {
//                MissionBox.getConfig().getConfigFC1().getPlaySiren().stop();
//                relayStrobe.setOff();
//                relaySiren.setValue(BigDecimal.ZERO);
//            } else if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_ROCKET_LAUNCHED)) {
//                MissionBox.getConfig().getConfigFC1().getPlaySiren().stop();
//                ledBar.setSimple();
//                relaySiren.setValue(BigDecimal.ZERO);
//                MissionBox.getConfig().getConfigFC1().getPlayRocket().play();
//                relayRocket.setOn();
//            } else if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_PRE_GAME)) {
//                relayStrobe.setOff();
//                relayRocket.setOff();
//                MissionBox.getConfig().getConfigFC1().getPlaySiren().stop();
//                MissionBox.getConfig().getConfigFC1().getPlayRocket().stop();
//                MissionBox.getConfig().getConfigFC1().getPlayWinningSon().stop();
//                MissionBox.getConfig().getConfigFC1().getPlayWelcome().play();
//                ledBar.setOff();
//            } else if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_OVER)) {
//                MissionBox.getConfig().getConfigFC1().getPlaySiren().stop();
//                MissionBox.getConfig().getConfigFC1().getPlayRocket().stop();
//                Tools.fadeout(config.getConfigFC1().getPlayWinningSon());
//            } else if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_OUTCOME_FLAG_TAKEN)) {
//                MissionBox.getConfig().getConfigFC1().getPlaySiren().stop();
//                MissionBox.getConfig().getConfigFC1().getPlayRocket().stop();
//                MissionBox.getConfig().getConfigFC1().getPlayWinningSon().play(false);
//                ledBar.setCylon();
//            }
//        };

//        farcryAssaultThread = new Farcry1AssaultThread(textListener, gameTimeListener, percentageListener, gameModeListener, MAXCYLCES, SECONDS2CAPTURE, 50);





        btnFlagTrigger.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                logger.debug("btnFlagTrigger");
                farcryAssaultThread.toggleFlag();
            }
        });

        btnGameStartStop.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                logger.debug("btnGameStartStop");
                if (farcryAssaultThread.getGameState() == Farcry1AssaultThread.GAME_PRE_GAME) {
                    farcryAssaultThread.startGame();
                } else {
                    farcryAssaultThread.restartGame();
                }
            }
        });

        btnMisc.addListener((GpioPinListenerDigital) event -> {


//            fadeout(playWinningSon);


            quitGame();


//            if (event.getState() == PinState.HIGH) {
//                logger.debug("btnMisc");
//
//               MissionBox.getConfig().getGPIO().setState(!GPIO.getState(myOutputs[someint]).isHigh(), myOutputs[someint]);
//                someint++;
//                if (someint >= myOutputs.length) someint = 24;
//
//                ledBar.setSimple();
//
//            }
        });

        farcryAssaultThread.run();
        System.out.println("<--Pi4J--> Wiring Pi LCD test program");
    }


    @Override
    public void quitGame() {

        farcryAssaultThread.quitGame();

//        Lcd.lcdClear(lcdHandle);
        farcryAssaultThread.quitGame();
//        relayRocket.setOff();
//        relayStrobe.setOff();
        ledBar.setOff();

        TinySound.shutdown();
        System.exit(0);

    }

}
