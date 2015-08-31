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
import misc.Tools;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by tloehr on 31.05.15.
 */
public class Farcry1Assault implements GameModes {
    private final Logger logger = Logger.getLogger(getClass());
    private int LCD_ROWS = 2;
    private int LCD_COLUMNS = 16;
    private int LCD_BITS = 4;
    private int TIME2RESPAWN = 20, MAXCYLCES = 200, SECONDS2CAPTURE = 60 * 10, someint = 24;
    private final ArrayList<GpioPinDigitalOutput> myLEDs = new ArrayList<>();
    private final ArrayList<GpioPinDigitalOutput> mySirens = new ArrayList<>();
//    private final LEDBar ledBar;
//    private final RelaySiren relaisSirens;
//    private final Relay relayStrobe, relayRocket;
//    private final int lcdHandle;

    // a CYCLE takes 50 millis


    private Farcry1AssaultThread farcryAssaultThread;

    private Music playSiren, playWinningSon;
    private Sound playWelcome, playRocket;


    public Farcry1Assault(GpioController GPIO) throws IOException {
        logger.setLevel(MissionBox.logLevel);

        logger.debug("test");

        final GpioPinDigitalInput btnRed = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_00, "RedTrigger", PinPullResistance.PULL_DOWN);
        final GpioPinDigitalInput btnGreen = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_03, "GreenTrigger", PinPullResistance.PULL_DOWN);
        final GpioPinDigitalInput btnGameStartStop = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_02, "GameStartStop", PinPullResistance.PULL_DOWN);
        final GpioPinDigitalInput btnMisc = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_21, "MISC", PinPullResistance.PULL_DOWN);

        final MCP23017GpioProvider gpioProvider0 = new MCP23017GpioProvider(I2CBus.BUS_1, 0x20);
        final MCP23017GpioProvider gpioProvider1 = new MCP23017GpioProvider(I2CBus.BUS_1, 0x21);
        final MCP23017GpioProvider gpioProvider2 = new MCP23017GpioProvider(I2CBus.BUS_1, 0x22);
        final MCP23017GpioProvider gpioProvider3 = new MCP23017GpioProvider(I2CBus.BUS_1, 0x23);

        int NUMLED4PROGRESS = 40;

        GpioPinDigitalOutput myOutputs[] = {
                GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A0, "mcp23017-01-A0", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A1, "mcp23017-01-A1", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A2, "mcp23017-01-A2", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A3, "mcp23017-01-A3", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A4, "mcp23017-01-A4", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A5, "mcp23017-01-A5", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A6, "mcp23017-01-A6", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A7, "mcp23017-01-A7", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B0, "mcp23017-01-B0", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B1, "mcp23017-01-B1", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B2, "mcp23017-01-B2", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B3, "mcp23017-01-B3", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B4, "mcp23017-01-B4", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B5, "mcp23017-01-B5", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B6, "mcp23017-01-B6", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B7, "mcp23017-01-B7", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_A0, "mcp23017-02-A0", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_A1, "mcp23017-02-A1", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_A2, "mcp23017-02-A2", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_A3, "mcp23017-02-A3", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_A4, "mcp23017-02-A4", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_A5, "mcp23017-02-A5", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_A6, "mcp23017-02-A6", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_A7, "mcp23017-02-A7", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_B0, "mcp23017-02-B0", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_B1, "mcp23017-02-B1", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_B2, "mcp23017-02-B2", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_B3, "mcp23017-02-B3", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_B4, "mcp23017-02-B4", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_B5, "mcp23017-02-B5", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_B6, "mcp23017-02-B6", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_B7, "mcp23017-02-B7", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_A0, "mcp23017-03-A0", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_A1, "mcp23017-03-A1", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_A2, "mcp23017-03-A2", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_A3, "mcp23017-03-A3", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_A4, "mcp23017-03-A4", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_A5, "mcp23017-03-A5", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_A6, "mcp23017-03-A6", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_A7, "mcp23017-03-A7", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_B0, "mcp23017-03-B0", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_B1, "mcp23017-03-B1", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_B2, "mcp23017-03-B2", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_B3, "mcp23017-03-B3", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_B4, "mcp23017-03-B4", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_B5, "mcp23017-03-B5", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_B6, "mcp23017-03-B6", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider2, MCP23017Pin.GPIO_B7, "mcp23017-03-B7", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_A0, "mcp23017-02-A0", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_A1, "mcp23017-02-A1", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_A2, "mcp23017-02-A2", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_A3, "mcp23017-02-A3", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_A4, "mcp23017-02-A4", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_A5, "mcp23017-02-A5", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_A6, "mcp23017-02-A6", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_A7, "mcp23017-02-A7", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_B0, "mcp23017-02-B0", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_B1, "mcp23017-02-B1", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_B2, "mcp23017-02-B2", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_B3, "mcp23017-02-B3", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_B4, "mcp23017-02-B4", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_B5, "mcp23017-02-B5", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_B6, "mcp23017-02-B6", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider3, MCP23017Pin.GPIO_B7, "mcp23017-02-B7", PinState.LOW)
        };


        for (int ledON = 0; ledON < NUMLED4PROGRESS; ledON++) {
            myLEDs.add(myOutputs[ledON]);
        }

        mySirens.add(myOutputs[40]);
        mySirens.add(myOutputs[41]);
        mySirens.add(myOutputs[42]);

//        relayRocket = new Relay(GPIO, myOutputs[43]);
//        relayStrobe = new Relay(GPIO, myOutputs[47]);


//        this.ledBar = new LEDBar(GPIO, myLEDs);
//        this.relaisSirens = new RelaySiren(GPIO, mySirens);


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

        TinySound.init();

        playSiren = TinySound.loadMusic(new File(Tools.SND_SIREN));
        playWinningSon = TinySound.loadMusic(new File(Tools.SND_MIB));
        playWelcome = TinySound.loadSound(new File(Tools.SND_WELCOME));
        playRocket = TinySound.loadSound(new File(Tools.SND_FLARE));

//        Lcd.lcdClear(lcdHandle);

        MessageListener textListener = messageEvent -> logger.debug(messageEvent.getMessage().toString());

//        MessageListener gameTimeListener = messageEvent -> {
//            Lcd.lcdPosition(lcdHandle, 0, 1);
//            Lcd.lcdPuts(lcdHandle, StringUtil.padCenter(messageEvent.getMessage().toString(), LCD_COLUMNS));
//        };

        MessageListener gameTimeListener = messageEvent -> {
            logger.info("GameTime: " + messageEvent.getMessage());
        };


        MessageListener percentageListener = messageEvent -> {
            logger.debug(messageEvent.getPercentage());
//            ledBar.setValue(messageEvent.getPercentage());
//            relaisSirens.setValue(messageEvent.getPercentage());

        };

        MessageListener gameModeListener = messageEvent -> {
            logger.debug("gameMode changed: " + Farcry1AssaultThread.GAME_MODES[messageEvent.getMode()]);
//            Lcd.lcdHome(lcdHandle);
//            Lcd.lcdPosition(lcdHandle, 0, 0);
//            Lcd.lcdPuts(lcdHandle, StringUtil.padCenter(Farcry1AssaultThread.GAME_MODES[messageEvent.getMode()], LCD_COLUMNS));

            if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_FLAG_HOT)) {
                playSiren.play(true);
//                relayStrobe.setOn();
            } else if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_FLAG_COLD)) {
                playSiren.stop();
//                relayStrobe.setOff();
//                relaisSirens.setValue(BigDecimal.ZERO);
            } else if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_ROCKET_LAUNCHED)) {
                playSiren.stop();
//                ledBar.setSimple();
//                relaisSirens.setValue(BigDecimal.ZERO);
                playRocket.play();
//                relayRocket.setOn();
            } else if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_PRE_GAME)) {
//                relayStrobe.setOff();
//                relayRocket.setOff();
                playSiren.stop();
                playRocket.stop();
                playWinningSon.stop();
                playWelcome.play();
//                ledBar.setOff();
            } else if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_OVER)) {
                playSiren.stop();
                playRocket.stop();
                fadeout(playWinningSon);
            } else if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_OUTCOME_FLAG_TAKEN)) {
                playSiren.stop();
                playRocket.stop();
                playWinningSon.play(false);
//                ledBar.setCylon();
            }
        };

        farcryAssaultThread = new Farcry1AssaultThread(textListener, gameTimeListener, percentageListener, gameModeListener, MAXCYLCES, SECONDS2CAPTURE, 50);

        btnRed.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                logger.debug("RedButton pressed");
                farcryAssaultThread.toggleFlag();
            }
        });

        btnGreen.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                logger.debug("GreenButton pressed");
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
//                GPIO.setState(!GPIO.getState(myOutputs[someint]).isHigh(), myOutputs[someint]);
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

    void fadeout(Music music) {
        SwingWorker worker = new SwingWorker() {
            double volume;

            @Override
            protected Object doInBackground() throws Exception {
                volume = music.getVolume();

                for (double vol = volume; vol >= 0d; vol = vol - 0.01d) {
                    logger.debug(vol);
                    music.setVolume(vol);
                    Thread.sleep(50);
                }

                return null;
            }

            @Override
            protected void done() {
                super.done();
                music.stop();
                music.setVolume(volume);
            }
        };
        worker.run();
    }

    @Override
    public void quitGame() {

        farcryAssaultThread.quitGame();

//        Lcd.lcdClear(lcdHandle);
        farcryAssaultThread.quitGame();
//        relayRocket.setOff();
//        relayStrobe.setOff();
//        ledBar.setOff();
        playSiren.unload();
        playRocket.unload();
        playWinningSon.unload();
        TinySound.shutdown();
        System.exit(0);

    }

}
