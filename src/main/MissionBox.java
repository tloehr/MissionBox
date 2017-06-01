package main;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.*;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import gamemodes.Farcry1Assault;
import gamemodes.Farcry1GameEvent;
import gamemodes.GameMode;
import interfaces.MyAbstractButton;
import interfaces.PercentageInterface;
import interfaces.Relay;
import misc.SortedProperties;
import misc.Tools;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import progresshandlers.RelayProgressRGB;
import progresshandlers.RelayProgressRedYellowGreen;
import threads.PinHandler;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Properties;

import static com.sun.org.apache.xalan.internal.utils.SecuritySupport.getResourceAsStream;


/**
 * Created by tloehr on 22.04.15.
 */
public class MissionBox {


    private static int startup_progress = 0;
    private static Logger logger;
    private static Level logLevel = Level.DEBUG;
    private static GpioController GPIO;
    private static FrmTest frmTest;
    //    private static FrmSimulator frmSimulator;
    private static Properties config;


    public static PinHandler getPinHandler() {
        return pinHandler;
    }

    private static GameMode gameMode;

    private static GpioPinDigitalInput ioRed, ioGreen, ioGameStartStop, ioPAUSE;
    private static MyAbstractButton btnRed, btnGreen, btnGameStartStop, btnPAUSE;
    private static JButton btnQuit;

    private static final HashMap<String, GpioPinDigitalOutput> outputMap = new HashMap<>();
    private static final HashMap<String, GpioPinDigitalInput> inputMap = new HashMap<>();

    private static PercentageInterface relaisSirens, relaisLEDs, relaisFlagpole;

    public static SortedProperties appinfo = new SortedProperties();

    /**
     * Diese Zuordnung bezieht sich auf die Schlüssel aus der missionbox.cfg Datei.
     */
    public static final String FCY_TIME2CAPTURE = "fcy.time2capture";
    public static final String FCY_GAMETIME = "fcy.gametime";
    //    public static final String FCY_SIREN = "fcy.siren";
//    public static final String MBX_SIREN_TIME = "mbx.siren.time";
    public static final String MBX_RESUME_TIME = "mbx.resume.time"; // in ms
    public static final String MBX_SIRENHANDLER = "mbx.sirenhandler";
    public static final String MBX_LOGLEVEL = "mbx.loglevel";
    public static final String FCY_RESPAWN_INTERVAL = "fcy.respawn.interval";
    public static final String MBX_RESPAWN_SIRENTIME = "mbx.respawn.sirentime";
    public static final String MBX_STARTGAME_SIRENTIME = "mbx.startgame.sirentime";
    public static final String FCY_WINNING_SIREN_SCHEME = "fcy.winning.siren.scheme";

    public static final String MBX_SIREN1 = "mbx.siren1";
    public static final String MBX_SIREN2 = "mbx.siren2";
    //    public static final String MBX_SIREN3 = "mbx.siren3";
    public static final String MBX_AIRSIREN = "mbx.airsiren";
    public static final String MBX_SHUTDOWN_SIREN = "mbx.shutdown.siren";


    //    public static final String MBX_TIME_SIREN = "mbx.time.siren";
    public static final String MBX_LED_GREEN = "mbx.led.green";
    public static final String MBX_LED_RED = "mbx.led.red";
    public static final String MBX_LED_PB_GREEN = "mbx.led.progress.green";
    public static final String MBX_LED_PB_YELLOW = "mbx.led.progress.yellow";
    public static final String MBX_LED_PB_RED = "mbx.led.progress.red";
    public static final String MBX_LED_RGB_RED = "mbx.led.rgb.red";
    public static final String MBX_LED_RGB_GREEN = "mbx.led.rgb.green";
    public static final String MBX_LED_RGB_BLUE = "mbx.led.rgb.blue";
    public static final String MBX_I2C_1 = "mbx.i2c.1";
    public static final String MBX_I2C_2 = "mbx.i2c.2";
    public static final String MBX_BTN_GREEN = "mbx.button.green";
    public static final String MBX_BTN_RED = "mbx.button.red";
    public static final String MBX_BTN_START_STOP = "mbx.button.startstop";
    public static final String MBX_BTN_PAUSE = "mbx.button.pause";

    private static HashMap<String, Relay> relayMap = new HashMap<>();

    private static PinHandler pinHandler = null;


    public static Properties getAppinfo() {
        return appinfo;
    }

    public static final void main(String[] args) throws Exception {

        System.setProperty("logs", Tools.getMissionboxDirectory());
        logger = Logger.getRootLogger();

        try {
            // Lade Build Informationen
            InputStream in2 = null;
            //Class clazz = getClass();
            in2 = getResourceAsStream("appinfo.properties");
            appinfo.load(in2);
            in2.close();
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                saveLocalProps();
                pinHandler.off();

            }
        }));

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            sw.toString(); // stack trace as a string
            logger.fatal(e);
            logger.fatal(sw);
        });

        Tools.printProgBar(startup_progress);

        loadLocalProperties();

        frmTest = new FrmTest();
        frmTest.setVisible(true);

        hwinit();
        initPinHandler();
        initProgressSystem();


        if (GPIO != null) frmTest.setExtendedState(JFrame.MAXIMIZED_BOTH);

        startup_progress = startup_progress + 10;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);

        startup_progress = startup_progress + 10;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);

        /***
         *      ____        _   _
         *     | __ ) _   _| |_| |_ ___  _ __  ___
         *     |  _ \| | | | __| __/ _ \| '_ \/ __|
         *     | |_) | |_| | |_| || (_) | | | \__ \
         *     |____/ \__,_|\__|\__\___/|_| |_|___/
         *
         */
        btnRed = new MyAbstractButton(ioRed, getGUIBtnRed());
        btnGreen = new MyAbstractButton(ioGreen, getGUIBtnGreen());
        btnGameStartStop = new MyAbstractButton(ioGameStartStop, getGUIBtn1());
        btnQuit = frmTest.getBtn2();
        btnPAUSE = new MyAbstractButton(ioPAUSE, getGUIBtnPause());

        btnQuit.addActionListener(e -> {
            MissionBox.shutdownEverything();
            System.exit(0);
        });


        startup_progress = 100;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);

        gameMode = new Farcry1Assault();

//        logger.debug(gameMode);

        gameMode.runGame();


    }

    private static void initProgressSystem() {
        relaisLEDs = new RelayProgressRedYellowGreen(MBX_LED_PB_RED, MBX_LED_PB_YELLOW, MBX_LED_PB_GREEN);
        relaisFlagpole = new RelayProgressRGB(MBX_LED_RGB_RED, MBX_LED_RGB_GREEN, MBX_LED_RGB_BLUE);
    }

    public static void setRelaisSirens(PercentageInterface relaisSirens) {
        MissionBox.relaisSirens = relaisSirens;
    }

    public static HashMap<String, GpioPinDigitalOutput> getOutputMap() {
        return outputMap;
    }

    private static void initPinHandler() {

        pinHandler = new PinHandler();

        JPanel debugPanel4Pins = frmTest.getDebugPanel4Pins();

        // these relays belong to one cd. They are all connected to the same siren circuit.

        // three sirens now.
        // Siren 1
        pinHandler.add(new Relay(MBX_SIREN1, Color.ORANGE, debugPanel4Pins, 70, 60)); // Original Siren Button 3
        pinHandler.add(new Relay(MBX_SIREN2, Color.ORANGE, debugPanel4Pins, 70, 80)); // Original Siren Button 3
//        pinHandler.add(1, new Relay(MBX_SIREN3, Color.ORANGE, debugPanel4Pins, 20, 60)); // Original Siren Button 5
        pinHandler.add(new Relay(MBX_SHUTDOWN_SIREN, Color.MAGENTA, debugPanel4Pins, 20, 40));

        // Siren 2
//        pinHandler.add(2, new Relay(MBX_TIME_SIREN, Color.BLUE, debugPanel4Pins, 20, 60)); // Original Siren Button 2

        // Siren 3
//        pinHandler.add(3, new Relay(MBX_RESPAWN_SIREN, Color.BLUE, debugPanel4Pins, true)); // Original Siren Button 6

        // The Airsiren
        pinHandler.add(new Relay(MBX_AIRSIREN, Color.ORANGE, debugPanel4Pins, 50, 90)); // Motor Siren

        pinHandler.add(new Relay(MBX_LED_GREEN, Color.GREEN, debugPanel4Pins));
        pinHandler.add(new Relay(MBX_LED_RED, Color.RED, debugPanel4Pins));
        pinHandler.add(new Relay(MBX_LED_PB_GREEN, Color.GREEN, debugPanel4Pins));
        pinHandler.add(new Relay(MBX_LED_PB_YELLOW, Color.YELLOW, debugPanel4Pins));
        pinHandler.add(new Relay(MBX_LED_PB_RED, Color.RED, debugPanel4Pins));

        pinHandler.add(new Relay(MBX_LED_RGB_BLUE, Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(MBX_LED_RGB_RED, Color.RED, debugPanel4Pins));
        pinHandler.add(new Relay(MBX_LED_RGB_GREEN, Color.GREEN, debugPanel4Pins));

        // for hardware testing only
        pinHandler.add(new Relay(outputMap.get("mcp23017-01-B0"), "mcp23017-01-B0", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-01-B1"), "mcp23017-01-B1", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-01-B2"), "mcp23017-01-B2", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-01-B3"), "mcp23017-01-B3", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-01-B4"), "mcp23017-01-B4", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-01-B5"), "mcp23017-01-B5", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-01-B6"), "mcp23017-01-B6", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-01-B7"), "mcp23017-01-B7", Color.BLUE, debugPanel4Pins));

        pinHandler.add(new Relay(outputMap.get("mcp23017-01-A0"), "mcp23017-01-A0", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-01-A1"), "mcp23017-01-A1", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-01-A2"), "mcp23017-01-A2", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-01-A3"), "mcp23017-01-A3", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-01-A4"), "mcp23017-01-A4", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-01-A5"), "mcp23017-01-A5", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-01-A6"), "mcp23017-01-A6", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-01-A7"), "mcp23017-01-A7", Color.BLUE, debugPanel4Pins));

        pinHandler.add(new Relay(outputMap.get("mcp23017-02-A0"), "mcp23017-02-A0", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-02-A1"), "mcp23017-02-A1", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-02-A2"), "mcp23017-02-A2", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-02-A3"), "mcp23017-02-A3", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-02-A4"), "mcp23017-02-A4", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-02-A5"), "mcp23017-02-A5", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-02-A6"), "mcp23017-02-A6", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("mcp23017-02-A7"), "mcp23017-02-A7", Color.BLUE, debugPanel4Pins));

    }

    public static void setScheme(String name, String scheme, Object... objects) {
        pinHandler.setScheme(name, String.format(scheme, objects));
    }

    public static void setScheme(String name, String scheme) {
        pinHandler.setScheme(name, scheme);
    }

    public static void off(String name) {
        pinHandler.off(name);
    }

    public static void on(String name) {
        pinHandler.on(name);
    }

    public static MyAbstractButton getBtnPAUSE() {
        return btnPAUSE;
    }

    public static MyAbstractButton getBtnRed() {
        return btnRed;
    }

    public static boolean isGameStartable() {
        return frmTest.isGameStartable();
    }

    public static MyAbstractButton getBtnGreen() {
        return btnGreen;
    }

    public static MyAbstractButton getBtnGameStartStop() {
        return btnGameStartStop;
    }

    public static JButton getGUIBtnRed() {
        return frmTest.getBtnRed();
    }

    public static JButton getGUIBtnPause() {
        return frmTest.getBtnPause();
    }

    public static Level getLogLevel() {
        return logLevel;
    }

    public static JButton getGUIBtnGreen() {
        return frmTest.getBtnGreen();
    }

    public static JButton getGUIBtn1() {
        return frmTest.getBtn1();
    }


    public static FrmTest getFrmTest() {
        return frmTest;
    }

    public static void enableSettings(boolean enable) {
        frmTest.enableSettings(enable);
    }


    public static int random_int(int Min, int Max) {
        return (int) (Math.random() * (Max - Min)) + Min;
    }


    public static GpioController getGPIO() {
        return GPIO;
    }


    private static void loadLocalProperties() throws IOException {
        config = new SortedProperties();

        // Hier stehen die Standardwerte, falls keine missionbox.cfg existiert.
        config.put(FCY_TIME2CAPTURE, "180");
        config.put(FCY_GAMETIME, "6");
        config.put(FCY_RESPAWN_INTERVAL, "60");
        config.put(MBX_RESUME_TIME, "10000"); // Zeit in ms, bevor es nach eine Pause weiter geht

        config.put(MBX_RESPAWN_SIRENTIME, "2000");
        config.put(MBX_STARTGAME_SIRENTIME, "10000");
        config.put(FCY_WINNING_SIREN_SCHEME, "1;1000,300,1000,300,1000,300,10000,0");

//        config.put(MBX_SIREN_TIME, "750");

        config.put(MBX_LOGLEVEL, "debug");
        config.put(MBX_I2C_1, "0x20");
        config.put(MBX_I2C_2, "0x24");

        // hier werden die üblichen Zuordnungen der einzelnen GPIOs
        // zu den jeweiligen Signalleitungen vorgenommen.
        // Falls man mal was umstecken muss, kann man das einfach
        // später in der missionbox.cfg ändern.
        // diese Werte werden hier so gesetzt, wie es in der
        // Bauanleitung der Box steht.

        // die hier brauchen wir immer
        config.put(MBX_SIREN1, "mcp23017-01-B1"); // die große
        config.put(MBX_SIREN2, "mcp23017-01-B3"); // die kleine

        config.put(MBX_AIRSIREN, "mcp23017-01-B0");
        config.put(MBX_SHUTDOWN_SIREN, "mcp23017-01-B2");

        config.put(MBX_LED_GREEN, "mcp23017-01-A7");
        config.put(MBX_LED_RED, "mcp23017-01-A6");
        config.put(MBX_LED_PB_GREEN, "mcp23017-01-A3");
        config.put(MBX_LED_PB_YELLOW, "mcp23017-01-A4");
        config.put(MBX_LED_PB_RED, "mcp23017-01-A5");
        config.put(MBX_LED_RGB_BLUE, "mcp23017-02-A5");
        config.put(MBX_LED_RGB_RED, "mcp23017-02-A7");
        config.put(MBX_LED_RGB_GREEN, "mcp23017-02-A6");

        config.put(MBX_BTN_RED, "mcp23017-02-B0");
        config.put(MBX_BTN_GREEN, "mcp23017-02-B1");
        config.put(MBX_BTN_START_STOP, "mcp23017-02-B2");
        config.put(MBX_BTN_PAUSE, "mcp23017-02-B3");

        File configFile = new File(Tools.getMissionboxDirectory() + File.separator + "config.txt");

        configFile.getParentFile().mkdirs();

        configFile.createNewFile();

        FileInputStream in = new FileInputStream(configFile);
        Properties p = new SortedProperties();
        p.load(in);
        config.putAll(p);
        p.clear();
        in.close();

        logLevel = Level.toLevel(config.getProperty(MissionBox.MBX_LOGLEVEL), Level.DEBUG);

    }

    public static void saveLocalProps() {
        logger.debug("saving local props");
        try {
            File configFile = new File(Tools.getMissionboxDirectory() + File.separator + "config.txt");
            FileOutputStream out = new FileOutputStream(configFile);
            config.store(out, "Settings MissionBox");
            out.close();
        } catch (Exception ex) {
            logger.fatal(ex);
            System.exit(1);
        }
    }


//    public static Properties getConfig() {
//        return config;
//    }


    public static void setConfig(String key, String value) {
        config.setProperty(key, value);

        if (key.equalsIgnoreCase(FCY_TIME2CAPTURE)) {
            ((Farcry1Assault) gameMode).setCapturetime(Long.parseLong(value));
        }
        if (key.equalsIgnoreCase(FCY_GAMETIME)) {
            ((Farcry1Assault) gameMode).setMaxgametime(Long.parseLong(value));
        }
        if (key.equalsIgnoreCase(FCY_RESPAWN_INTERVAL)) {
            ((Farcry1Assault) gameMode).setRespawninterval(Long.parseLong(value));
        }

        saveLocalProps();
    }


    public static String getConfig(String key) {
        return getConfig(key, "");
    }

    /**
     * @param key
     * @param neutral - wenn es den mainSiren nicht gibt, wir dieser Neutralwert zurück gegeben
     * @return
     */
    public static String getConfig(String key, String neutral) {
        return config.containsKey(key) ? config.getProperty(key) : neutral;
    }

    public static int getIntConfig(String key) {
        return getIntConfig(key, 0);
    }

    public static int getIntConfig(String key, int neutral) {
        String cfg = getConfig(key);
        if (cfg.isEmpty()) return neutral;
        return Integer.parseInt(getConfig(key));
    }

    public static void setMessage(String message) {
        frmTest.setMessage(message);
    }

//    public static int getGamemode() {
//        return gamemode;
//    }
//
//    public static void setGamemode(int gamemode) {
//        MissionBox.gamemode = gamemode;
//    }

    public static void setProgress(BigDecimal percent) {
        if (relaisSirens != null) relaisSirens.setValue(percent);
        frmTest.setProgress(percent.intValue());
        if (relaisLEDs != null) relaisLEDs.setValue(percent);
        if (relaisFlagpole != null) relaisFlagpole.setValue(percent);
    }
//
//
//    public static boolean isRESPAWN() {
//        return RESPAWN;
//    }


    public static void setTimerMessage(String message) {
        frmTest.setTimer(message);
//        logger.debug(message);
    }

//    public static void minuteSignal(int minutes) {
//        setScheme(MBX_TIME_SIREN, minutes + ";1000,1000");
//    }


    public static void setRespawnTimer(String message) {
        frmTest.setRespawnTimer(message);
    }

    private static void hwinit() throws IOException, I2CFactory.UnsupportedBusNumberException {

        GPIO = null;
        if (Tools.isArm()) {
            try {
                GPIO = GpioFactory.getInstance();

            } catch (Exception e) {
                logger.fatal(e);
                System.exit(0);
            }


            // this map provides an easier access to the gpioProvider0
            MCP23017GpioProvider gpioProvider0 = new MCP23017GpioProvider(I2CBus.BUS_1, Integer.decode(config.getProperty(MissionBox.MBX_I2C_1)));
            MCP23017GpioProvider gpioProvider1 = new MCP23017GpioProvider(I2CBus.BUS_1, Integer.decode(config.getProperty(MissionBox.MBX_I2C_2)));

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
                    GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_A7, "mcp23017-02-A7", PinState.LOW)
            };
            for (int ioPin = 0; ioPin < myOutputs.length; ioPin++) {
                outputMap.put(myOutputs[ioPin].getName(), myOutputs[ioPin]);
            }

            GpioPinDigitalInput myInputs[] = {
                    GPIO.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B0, "mcp23017-02-B0", PinPullResistance.PULL_UP),
                    GPIO.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B1, "mcp23017-02-B1", PinPullResistance.PULL_UP),
                    GPIO.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B2, "mcp23017-02-B2", PinPullResistance.PULL_UP),
                    GPIO.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B3, "mcp23017-02-B3", PinPullResistance.PULL_UP),
                    GPIO.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B4, "mcp23017-02-B4", PinPullResistance.PULL_UP),
                    GPIO.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B5, "mcp23017-02-B5", PinPullResistance.PULL_UP),
                    GPIO.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B6, "mcp23017-02-B6", PinPullResistance.PULL_UP),
                    GPIO.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B7, "mcp23017-02-B7", PinPullResistance.PULL_UP)
            };
            for (int ioPin = 0; ioPin < myInputs.length; ioPin++) {
                inputMap.put(myInputs[ioPin].getName(), myInputs[ioPin]);
            }


//            ioRed = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_00, "RedTrigger", PinPullResistance.PULL_DOWN); // Board 11
//            ioGreen = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_02, "GreenTrigger", PinPullResistance.PULL_DOWN); // Board 13
//            ioGameStartStop = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_03, "GameStartStop", PinPullResistance.PULL_DOWN); // Board 15
//            ioMisc = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_21, "MISC", PinPullResistance.PULL_DOWN); // Board 29


            /***
             *      ____        _   _
             *     | __ ) _   _| |_| |_ ___  _ __  ___
             *     |  _ \| | | | __| __/ _ \| '_ \/ __|
             *     | |_) | |_| | |_| || (_) | | | \__ \
             *     |____/ \__,_|\__|\__\___/|_| |_|___/
             *
             */
            ioRed = inputMap.get(config.getProperty(MBX_BTN_RED));
            ioGreen = inputMap.get(config.getProperty(MBX_BTN_GREEN));
            ioGameStartStop = inputMap.get((config.getProperty(MBX_BTN_START_STOP)));
//            ioMisc = inputMap.get((config.getProperty(MBX_BTN_QUIT)));
            ioPAUSE = inputMap.get((config.getProperty(MBX_BTN_PAUSE)));
        }

    }


    public static void shutdownEverything() {
        pinHandler.off();


//        synchronized (mapWorker) {
//            Set<String> keys = mapWorker.keySet();
//            for (String mainSiren : keys) {
//                logger.debug("cleaning mainSiren: "+mainSiren);
//                MissionBox.blink(mainSiren, 0);
//            }
//        }

    }


    public static void setRevertEvent(Farcry1GameEvent revert2Event) {
        ((Farcry1Assault) MissionBox.gameMode).setRevertEvent(revert2Event);
    }
}
