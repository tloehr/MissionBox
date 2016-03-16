package gamemodes;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;
import interfaces.MessageListener;
import interfaces.MyAbstractButton;
import interfaces.Relay;
import interfaces.RelaySiren;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import main.FrmTest;
import main.MissionBox;
import misc.Tools;
import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tloehr on 31.05.15.
 */
public class Farcry1Assault implements GameModes {
    private final Logger logger = Logger.getLogger(getClass());

    // the game is organized in cycles. In a cycle the game state is checked and it is decided if the game was won or not.
    private final int MILLISPERCYCLE = 50;


    private final int MAXCYLCES = 200;

    private int SECONDS2CAPTURE = 60 * 10;

    private final ArrayList<Relay> relayBoard = new ArrayList<>();
    private final ArrayList<Relay> relaidLEDs = new ArrayList<>();
    private final HashMap<String, GpioPinDigitalOutput> mapGPIO = new HashMap<>();

    private final RelaySiren relaisSirens, relaisLEDs;


    private Farcry1AssaultThread farcryAssaultThread;

    private Music playSiren, playWinningSon;
    private Sound playWelcome, playRocket;

    private void hwinit(GpioController GPIO) throws IOException {

        if (GPIO == null) return;

        MCP23017GpioProvider gpioProvider0 = GPIO == null ? null : new MCP23017GpioProvider(I2CBus.BUS_1, 0x20);
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
                GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B7, "mcp23017-01-B7", PinState.LOW)
        };

        for (int ledON = 0; ledON < myOutputs.length; ledON++) {
            mapGPIO.put(myOutputs[ledON].getName(), myOutputs[ledON]);
        }
    }

    public Farcry1Assault(GpioController GPIO, FrmTest frmTest) throws IOException {

        hwinit(GPIO);

        logger.setLevel(MissionBox.logLevel);

        final GpioPinDigitalInput ioRed = GPIO == null ? null : GPIO.provisionDigitalInputPin(RaspiPin.GPIO_00, "RedTrigger", PinPullResistance.PULL_DOWN);
        MyAbstractButton btnRed = new MyAbstractButton(ioRed, frmTest.getBtnRed());

        final GpioPinDigitalInput ioGreen = GPIO == null ? null : GPIO.provisionDigitalInputPin(RaspiPin.GPIO_02, "GreenTrigger", PinPullResistance.PULL_DOWN);
        MyAbstractButton btnGreen = new MyAbstractButton(ioGreen, frmTest.getBtnGreen());

        final GpioPinDigitalInput ioGameStartStop = GPIO == null ? null : GPIO.provisionDigitalInputPin(RaspiPin.GPIO_03, "GameStartStop", PinPullResistance.PULL_DOWN);
        MyAbstractButton btnGameStartStop = new MyAbstractButton(ioGameStartStop, frmTest.getBtn1());

        final GpioPinDigitalInput ioMisc = GPIO == null ? null : GPIO.provisionDigitalInputPin(RaspiPin.GPIO_21, "MISC", PinPullResistance.PULL_DOWN);
        MyAbstractButton btnMisc = new MyAbstractButton(ioMisc, frmTest.getBtn2());


//        final MCP23017GpioProvider gpioProvider1 = new MCP23017GpioProvider(I2CBus.BUS_1, 0x21);
//        final MCP23017GpioProvider gpioProvider2 = new MCP23017GpioProvider(I2CBus.BUS_1, 0x22);
//        final MCP23017GpioProvider gpioProvider3 = new MCP23017GpioProvider(I2CBus.BUS_1, 0x23);


        final GpioPinDigitalOutput ioLedGreen = mapGPIO.get("mcp23017-01-A0");
        final GpioPinDigitalOutput ioLedRed = mapGPIO.get("mcp23017-01-A1");
        final GpioPinDigitalOutput ioLedBarGreen = mapGPIO.get("mcp23017-01-A2");
        final GpioPinDigitalOutput ioLedBarYellow = mapGPIO.get("mcp23017-01-A3");
        final GpioPinDigitalOutput ioLedBarRed = mapGPIO.get("mcp23017-01-A4");

        Relay ledGreen = new Relay(ioLedGreen);
        Relay ledRed = new Relay(ioLedRed);
        Relay ledBarGreen = new Relay(ioLedBarGreen);
        Relay ledBarYellow = new Relay(ioLedBarYellow);
        Relay ledBarRed = new Relay(ioLedBarRed);

        relayBoard.add(new Relay(mapGPIO.containsKey("mcp23017-01-B0") ? mapGPIO.get("mcp23017-01-B0") : null));
        relayBoard.add(new Relay(mapGPIO.containsKey("mcp23017-01-B1") ? mapGPIO.get("mcp23017-01-B1") : null));
        relayBoard.add(new Relay(mapGPIO.containsKey("mcp23017-01-B2") ? mapGPIO.get("mcp23017-01-B2") : null));
        relayBoard.add(new Relay(mapGPIO.containsKey("mcp23017-01-B3") ? mapGPIO.get("mcp23017-01-B3") : null));
        relayBoard.add(new Relay(mapGPIO.containsKey("mcp23017-01-B4") ? mapGPIO.get("mcp23017-01-B4") : null));
        relayBoard.add(new Relay(mapGPIO.containsKey("mcp23017-01-B5") ? mapGPIO.get("mcp23017-01-B5") : null));
        relayBoard.add(new Relay(mapGPIO.containsKey("mcp23017-01-B6") ? mapGPIO.get("mcp23017-01-B6") : null));
        relayBoard.add(new Relay(mapGPIO.containsKey("mcp23017-01-B7") ? mapGPIO.get("mcp23017-01-B7") : null));

        relaidLEDs.add(ledBarGreen);
        relaidLEDs.add(ledBarYellow);
        relaidLEDs.add(ledBarRed);

        this.relaisLEDs = new RelaySiren(relaidLEDs);
        this.relaisSirens = new RelaySiren(relayBoard);

        TinySound.init();

        playSiren = TinySound.loadMusic(new File(Tools.SND_SIREN));
        playWinningSon = TinySound.loadMusic(new File(Tools.SND_MIB));
        playWelcome = TinySound.loadSound(new File(Tools.SND_WELCOME));
        playRocket = TinySound.loadSound(new File(Tools.SND_FLARE));

        MessageListener textListener = messageEvent -> logger.debug(messageEvent.getMessage().toString());

        MessageListener gameTimeListener = messageEvent -> {
            logger.debug("GameTime: " + messageEvent.getMessage());
            frmTest.setTimer( messageEvent.getMessage().toString());
        };

        MessageListener percentageListener = messageEvent -> {
            logger.debug(messageEvent.getPercentage());
            relaisSirens.setValue(messageEvent.getPercentage());
        };

        MessageListener gameModeListener = messageEvent -> {
            logger.debug("gameMode changed: " + Farcry1AssaultThread.GAME_MODES[messageEvent.getMode()]);
            frmTest.setMessage(Farcry1AssaultThread.GAME_MODES[messageEvent.getMode()]);

            if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_FLAG_HOT)) {
                playSiren.play(true);
                ledRed.blink(100, PinState.HIGH);
                ledGreen.blink(100, PinState.LOW);
            } else if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_FLAG_COLD)) {
                playSiren.stop();
                ledRed.blink(1000, PinState.HIGH);
                ledGreen.blink(1000, PinState.LOW);
                ledBarGreen.blink(0);
                ledBarYellow.blink(0);
                ledBarRed.blink(0);
                relaisSirens.setValue(BigDecimal.ZERO);
            } else if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_ROCKET_LAUNCHED)) {
                playSiren.stop();
                relaisSirens.setValue(BigDecimal.ZERO);
                playRocket.play();
                ledRed.blink(50, PinState.HIGH);
                ledGreen.blink(50, PinState.LOW);
                ledBarGreen.blink(50);
                ledBarYellow.blink(50);
                ledBarRed.blink(50);
            } else if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_PRE_GAME)) {
                ledRed.blink(500, PinState.HIGH);
                ledGreen.blink(500, PinState.LOW);
                ledBarGreen.blink(1000);
                ledBarYellow.blink(1000);
                ledBarRed.blink(1000);

                playSiren.stop();
                playRocket.stop();
                playWinningSon.stop();
                playWelcome.play();
//                ledBar.setOff();
            } else if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_OVER)) {
                playSiren.stop();
                playRocket.stop();
                ledGreen.blink(0);
//                fadeout(playWinningSon);
            } else if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_OUTCOME_FLAG_TAKEN)) {
                ledRed.blink(250, PinState.HIGH);
                ledGreen.blink(250, PinState.HIGH);
                ledBarGreen.blink(500);
                ledBarYellow.blink(500);
                ledBarRed.blink(500);
                playSiren.stop();
                playRocket.stop();
                playWinningSon.play(false);
            }
        };

        farcryAssaultThread = new Farcry1AssaultThread(textListener, gameTimeListener, percentageListener, gameModeListener, MAXCYLCES, SECONDS2CAPTURE);

        btnRed.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                logger.debug("RedButton pressed");
                farcryAssaultThread.setFlag(true);
            }
        });

        btnRed.addListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.debug("RedButton pressed");
                farcryAssaultThread.setFlag(true);
            }
        });

        btnGreen.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                // If both buttons are pressed, the red one wins.
                if (btnRed.isHigh()) return;
                logger.debug("GreenButton pressed");
                farcryAssaultThread.setFlag(false);
            }
        });

        btnGreen.addListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.debug("GreenButton pressed");
                farcryAssaultThread.setFlag(false);
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

        btnGameStartStop.addListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
        });

        btnMisc.addListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quitGame();
            }
        });

        farcryAssaultThread.run();

    }

//    void fadeout(Music music) {
//        SwingWorker worker = new SwingWorker() {
//            double volume;
//
//            @Override
//            protected Object doInBackground() throws Exception {
//                volume = music.getVolume();
//
//                for (double vol = volume; vol >= 0d; vol = vol - 0.01d) {
//                    logger.debug(vol);
//                    music.setVolume(vol);
//                    Thread.sleep(50);
//                }
//
//                return null;
//            }
//
//            @Override
//            protected void done() {
//                super.done();
//                music.stop();
//                music.setVolume(volume);
//            }
//        };
//        worker.run();
//    }

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
