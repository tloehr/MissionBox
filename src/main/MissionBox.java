package main;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.*;
import com.pi4j.io.i2c.I2CBus;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import gamemodes.Farcry1Assault;
import interfaces.Relay;
import interfaces.RelaySiren;
import org.apache.log4j.*;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;


/**
 * Created by tloehr on 22.04.15.
 */
public class MissionBox {

    private static final Logger logger = Logger.getRootLogger();
    public static Level logLevel = Level.DEBUG;
    private static GpioController GPIO;
    private static FrmTest frmTest;
    private static Properties config;

    private static final HashMap<String, GpioPinDigitalOutput> mapGPIO = new HashMap<>();
    private static final ArrayList<Relay> relayBoard = new ArrayList<>();
    private static final ArrayList<Relay> progressLEDs = new ArrayList<>();

    private static GpioPinDigitalInput ioRed = null;
    private static GpioPinDigitalInput ioGreen = null;
    private static GpioPinDigitalInput ioGameStartStop = null;
    private static GpioPinDigitalInput ioMisc = null;

    private static GpioPinDigitalOutput ioLedGreen = null;
    private static GpioPinDigitalOutput ioLedRed = null;
    private static GpioPinDigitalOutput ioLedBarGreen = null;
    private static GpioPinDigitalOutput ioLedBarYellow = null;
    private static GpioPinDigitalOutput ioLedBarRed = null;

    private static Relay ledGreen = null;
    private static Relay ledRed = null;
    private static Relay ledBarGreen = null;
    private static Relay ledBarYellow = null;
    private static Relay ledBarRed = null;

    private static RelaySiren relaisSirens, relaisLEDs;

    public static final String FCY_TIME2CAPTURE = "fcy.time2capture";
    public static final String FCY_GAMETIME = "fcy.gametime";
    public static final String FCY_SOUND = "fcy.sound";
    public static final String FCY_SIREN = "fcy.siren";


    public static final void main(String[] args) throws Exception {
        logLevel = Level.toLevel("DEBUG", Level.DEBUG);

//        try {
//            GPIO = GpioFactory.getInstance();
//        } catch (Exception e) {
//            GPIO = null;
//        }
        loadLocalProperties();
        GPIO = null;

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }

//        String text = "10 minutes";
//
//


//        voiceManager = VoiceManager.getInstance();

//        voice.speak(text);


        PatternLayout layout = new PatternLayout("%d{ISO8601} %-5p [%t] %c: %m%n");
        logger.addAppender(new ConsoleAppender(layout));
        logger.addAppender(new FileAppender(layout, System.getenv("user.home") + File.separator + "missionbox" + File.separator + "missionbox.log"));

        hwinit();

        frmTest = new FrmTest();
        frmTest.pack();
        frmTest.setVisible(true);

        Farcry1Assault fc = new Farcry1Assault(frmTest);
    }

    public static Voice getVoice() {
        Voice voice = VoiceManager.getInstance().getVoice("kevin"); // kevin, kevin16, alan
        voice.allocate();
        return voice;
    }


    private static void loadLocalProperties() throws IOException {

        config = new Properties();
        // some defaults


        config.put(FCY_TIME2CAPTURE, "20");
        config.put(FCY_GAMETIME, "5");
        config.put(FCY_SOUND, "true");
        config.put(FCY_SIREN, "false");

        String path = System.getProperty("user.home") + File.separator + "missionbox";

        File configFile = new File(path + File.separator + "config.txt");

        configFile.getParentFile().mkdirs();

        configFile.createNewFile();

        FileInputStream in = new FileInputStream(configFile);
        Properties p = new Properties();
        p.load(in);
        config.putAll(p);
        p.clear();
        in.close();

    }

    public static void saveLocalProps() {
        String path = System.getProperty("user.home") + File.separator + "missionbox";

        try {
            File configFile = new File(path + File.separator + "config.txt");
            FileOutputStream out = new FileOutputStream(configFile);
            config.store(out, "Settings MissionBox");
            out.close();
        } catch (Exception ex) {
            System.exit(1);
        }
    }


    public static Properties getConfig() {
        return config;
    }

    private static void hwinit() throws IOException {

        if (GPIO == null) return;

        MCP23017GpioProvider gpioProvider0 = new MCP23017GpioProvider(I2CBus.BUS_1, 0x20);
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

        for (int ioPin = 0; ioPin < myOutputs.length; ioPin++) {
            mapGPIO.put(myOutputs[ioPin].getName(), myOutputs[ioPin]);
        }

        relayBoard.add(new Relay(MissionBox.getMapGPIO().containsKey("mcp23017-01-B0") ? MissionBox.getMapGPIO().get("mcp23017-01-B0") : null));
        relayBoard.add(new Relay(MissionBox.getMapGPIO().containsKey("mcp23017-01-B1") ? MissionBox.getMapGPIO().get("mcp23017-01-B1") : null));
        relayBoard.add(new Relay(MissionBox.getMapGPIO().containsKey("mcp23017-01-B2") ? MissionBox.getMapGPIO().get("mcp23017-01-B2") : null));
        relayBoard.add(new Relay(MissionBox.getMapGPIO().containsKey("mcp23017-01-B3") ? MissionBox.getMapGPIO().get("mcp23017-01-B3") : null));
        relayBoard.add(new Relay(MissionBox.getMapGPIO().containsKey("mcp23017-01-B4") ? MissionBox.getMapGPIO().get("mcp23017-01-B4") : null));
        relayBoard.add(new Relay(MissionBox.getMapGPIO().containsKey("mcp23017-01-B5") ? MissionBox.getMapGPIO().get("mcp23017-01-B5") : null));
        relayBoard.add(new Relay(MissionBox.getMapGPIO().containsKey("mcp23017-01-B6") ? MissionBox.getMapGPIO().get("mcp23017-01-B6") : null));
        relayBoard.add(new Relay(MissionBox.getMapGPIO().containsKey("mcp23017-01-B7") ? MissionBox.getMapGPIO().get("mcp23017-01-B7") : null));

        ioRed = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_00, "RedTrigger", PinPullResistance.PULL_DOWN);
        ioGreen = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_02, "GreenTrigger", PinPullResistance.PULL_DOWN);
        ioGameStartStop = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_03, "GameStartStop", PinPullResistance.PULL_DOWN);
        ioMisc = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_21, "MISC", PinPullResistance.PULL_DOWN);


        ioLedGreen = MissionBox.getMapGPIO().get("mcp23017-01-A0");
        ioLedRed = MissionBox.getMapGPIO().get("mcp23017-01-A1");
        ioLedBarGreen = MissionBox.getMapGPIO().get("mcp23017-01-A2");
        ioLedBarYellow = MissionBox.getMapGPIO().get("mcp23017-01-A3");
        ioLedBarRed = MissionBox.getMapGPIO().get("mcp23017-01-A4");

        Relay ledGreen = new Relay(ioLedGreen);
        Relay ledRed = new Relay(ioLedRed);
        Relay ledBarGreen = new Relay(ioLedBarGreen);
        Relay ledBarYellow = new Relay(ioLedBarYellow);
        Relay ledBarRed = new Relay(ioLedBarRed);

        progressLEDs.add(ledBarGreen);
        progressLEDs.add(ledBarYellow);
        progressLEDs.add(ledBarRed);

        relaisLEDs = new RelaySiren(MissionBox.getProgressLEDs());
        relaisSirens = new RelaySiren(MissionBox.getRelayBoard());
    }

    public static HashMap<String, GpioPinDigitalOutput> getMapGPIO() {
        return mapGPIO;
    }

    public static GpioController getGPIO() {
        return GPIO;
    }

    public static ArrayList<Relay> getRelayBoard() {
        return relayBoard;
    }

    public static ArrayList<Relay> getProgressLEDs() {
        return progressLEDs;
    }

    public static Relay getLedGreen() {
        return ledGreen;
    }

    public static Relay getLedRed() {
        return ledRed;
    }

    public static Relay getLedBarGreen() {
        return ledBarGreen;
    }

    public static Relay getLedBarYellow() {
        return ledBarYellow;
    }

    public static Relay getLedBarRed() {
        return ledBarRed;
    }

    public static RelaySiren getRelaisSirens() {
        return relaisSirens;
    }
}
