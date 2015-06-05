package gamemodes;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.util.StringUtil;
import com.pi4j.wiringpi.Lcd;
import interfaces.MessageListener;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import main.MissionBox;
import threads.AEPlayWave;
import misc.Tools;
import org.apache.log4j.Logger;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by tloehr on 31.05.15.
 */
public class Farcry1Assault implements GameModes {
    private final Logger logger = Logger.getLogger(getClass());
    private int LCD_ROWS = 2;
    private int LCD_COLUMNS = 16;
    private int LCD_BITS = 4;
    private int TIME2RESPAWN = 20, MAXCYLCES = 200, SECONDS2CAPTURE = 60 * 10;
    // a CYCLE takes 50 millis

    private Farcry1AssaultThread farcryAssaultThread;
    private Music playSiren, playRocket;


    public Farcry1Assault(GpioController GPIO) throws IOException {
        logger.setLevel(MissionBox.logLevel);

        final GpioPinDigitalInput btnFlagTrigger = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_03, "FlagTrigger", PinPullResistance.PULL_DOWN);
        final GpioPinDigitalInput btnGameStartStop = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_02, "GameStartStop", PinPullResistance.PULL_DOWN);
        final MCP23017GpioProvider gpioProvider = new MCP23017GpioProvider(I2CBus.BUS_1, 0x20);

        GpioPinDigitalOutput myOutputs[] = {
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_A0, "MyOutput-A0", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_A1, "MyOutput-A1", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_A2, "MyOutput-A2", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_A3, "MyOutput-A3", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_A4, "MyOutput-A4", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_A5, "MyOutput-A5", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_A6, "MyOutput-A6", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_A7, "MyOutput-A7", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_B0, "MyOutput-B0", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_B1, "MyOutput-B1", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_B2, "MyOutput-B2", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_B3, "MyOutput-B3", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_B4, "MyOutput-B4", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_B5, "MyOutput-B5", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_B6, "MyOutput-B6", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_B7, "MyOutput-B7", PinState.LOW)
        };

        // initialize LCD
        // the wiring is according to the examples from https://kofler.info/buecher/raspberry-pi/
        int lcdHandle = Lcd.lcdInit(LCD_ROWS,     // number of row supported by LCD
                LCD_COLUMNS,  // number of columns supported by LCD
                LCD_BITS,     // number of bits used to communicate to LCD
                11,           // LCD RS pin
                10,           // LCD strobe pin
                6,            // LCD data bit 1
                5,            // LCD data bit 2
                4,            // LCD data bit 3
                1,            // LCD data bit 4
                0,            // LCD data bit 5 (set to 0 if using 4 bit communication)
                0,            // LCD data bit 6 (set to 0 if using 4 bit communication)
                0,            // LCD data bit 7 (set to 0 if using 4 bit communication)
                0);           // LCD data bit 8 (set to 0 if using 4 bit communication)


        // verify initialization
        if (lcdHandle == -1) {
            logger.fatal(" ==>> LCD INIT FAILED");
            return;
        }

        TinySound.init();

        playSiren = TinySound.loadMusic(new File(Tools.SND_SIREN));
        playRocket = TinySound.loadMusic(new File(Tools.SND_FLARE));

        Lcd.lcdClear(lcdHandle);

        MessageListener textListener = messageEvent -> logger.debug(messageEvent.getMessage().toString());

        MessageListener gameTimeListener = messageEvent -> {
            Lcd.lcdPosition(lcdHandle, 0, 1);
            Lcd.lcdPuts(lcdHandle, StringUtil.padCenter(messageEvent.getMessage().toString(), LCD_COLUMNS));
        };

        MessageListener percentageListener = messageEvent -> {
            int barrier = new BigDecimal(16).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).multiply(messageEvent.getPercentage()).intValue();

            for (int ledON = 0; ledON < barrier; ledON++) {
                GPIO.setState(true, myOutputs[ledON]);
            }
            for (int ledOFF = barrier; ledOFF < 16; ledOFF++) {
                GPIO.setState(false, myOutputs[ledOFF]);
            }
        };

        MessageListener gameModeListener = messageEvent -> {
            logger.debug("gameMode changed: " + Farcry1AssaultThread.GAME_MODES[messageEvent.getMode()]);
            Lcd.lcdHome(lcdHandle);
            Lcd.lcdPosition(lcdHandle, 0, 0);
            Lcd.lcdPuts(lcdHandle, StringUtil.padCenter(Farcry1AssaultThread.GAME_MODES[messageEvent.getMode()], LCD_COLUMNS));

            if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_FLAG_HOT)) {
                playSiren.play(true);
            } else if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_FLAG_COLD)) {
                playSiren.stop();
            } else if (messageEvent.getMode().equals(Farcry1AssaultThread.GAME_ROCKET_LAUNCHED)) {
                playSiren.stop();
                playRocket.play(false);
            }
        };

        farcryAssaultThread = new Farcry1AssaultThread(textListener, gameTimeListener, percentageListener, gameModeListener, MAXCYLCES, SECONDS2CAPTURE, 50);

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

        farcryAssaultThread.run();
        System.out.println("<--Pi4J--> Wiring Pi LCD test program");
    }

    @Override
    public void quitGame() {
        farcryAssaultThread.quitGame();
        TinySound.shutdown();
    }

}
