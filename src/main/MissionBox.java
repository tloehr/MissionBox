package main;

import com.pi4j.io.gpio.*;
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

    private static PercentageInterface relaisSirens, relaidPBLeds1, relaidPBLeds2;

    public static SortedProperties appinfo = new SortedProperties();

    /**
     * Hier stehen die möglichen Schlüssel aus der config.txt
     */
    public static final String FCY_TIME2CAPTURE = "fcy.time2capture";
    public static final String FCY_GAMETIME = "fcy.gametime";
    public static final String MBX_RESUME_TIME = "mbx.resume.time"; // in ms
    public static final String MBX_SIRENHANDLER = "mbx.sirenhandler";
    public static final String MBX_LOGLEVEL = "mbx.loglevel";
    public static final String FCY_RESPAWN_INTERVAL = "fcy.respawn.interval";
    public static final String MBX_RESPAWN_SIRENTIME = "mbx.respawn.sirentime";
    public static final String MBX_STARTGAME_SIRENTIME = "mbx.startgame.sirentime";

    public static final String MBX_SIREN1 = "mbx.siren1";
    public static final String MBX_RESPAWN_SIREN = "mbx.respawn.siren";
    public static final String MBX_AIRSIREN = "mbx.airsiren";
    public static final String MBX_SHUTDOWN_SIREN = "mbx.shutdown.siren";

    public static final String MBX_LED1_BTN_RED = "mbx.led1.btn.red";
    public static final String MBX_LED1_BTN_GREEN = "mbx.led1.btn.green";
    public static final String MBX_LED2_BTN_RED = "mbx.led2.btn.red";
    public static final String MBX_LED2_BTN_GREEN = "mbx.led2.btn.green";
    public static final String MBX_LED_PROGRESS1_RED = "mbx.led.progress1.red";
    public static final String MBX_LED_PROGRESS1_YELLOW = "mbx.led.progress1.yellow";
    public static final String MBX_LED_PROGRESS1_GREEN = "mbx.led.progress1.green";
    public static final String MBX_LED_PROGRESS2_RED = "mbx.led.progress2.red";
    public static final String MBX_LED_PROGRESS2_YELLOW = "mbx.led.progress2.yellow";
    public static final String MBX_LED_PROGRESS2_GREEN = "mbx.led.progress2.green";

    //    public static final String MBX_I2C_1 = "mbx.i2c.1";
//    public static final String MBX_I2C_2 = "mbx.i2c.2";

    public static final String MBX_BTN_GREEN = "mbx.button.green";
    public static final String MBX_BTN_RED = "mbx.button.red";
    public static final String MBX_BTN_START_STOP = "mbx.button.startstop";
    public static final String MBX_BTN_PAUSE = "mbx.button.pause";

    private static PinHandler pinHandler = null;

    public static Properties getAppinfo() {
        return appinfo;
    }

    public static final void main(String[] args) throws Exception {

        System.setProperty("logs", Tools.getMissionboxDirectory());
        logger = Logger.getRootLogger();

        logger.info("\n" +
                "  ____ _____  _    ____ _____ ___ _   _  ____   __  __ _         _             ____            \n" +
                " / ___|_   _|/ \\  |  _ \\_   _|_ _| \\ | |/ ___| |  \\/  (_)___ ___(_) ___  _ __ | __ )  _____  __\n" +
                " \\___ \\ | | / _ \\ | |_) || |  | ||  \\| | |  _  | |\\/| | / __/ __| |/ _ \\| '_ \\|  _ \\ / _ \\ \\/ /\n" +
                "  ___) || |/ ___ \\|  _ < | |  | || |\\  | |_| | | |  | | \\__ \\__ \\ | (_) | | | | |_) | (_) >  < \n" +
                " |____/ |_/_/   \\_\\_| \\_\\|_| |___|_| \\_|\\____| |_|  |_|_|___/___/_|\\___/|_| |_|____/ \\___/_/\\_\\\n" +
                "                                                                                               ");


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
                logger.info("\n" +
                        "  _____ _   _ ____     ___  _____   __  __ _         _             ____            \n" +
                        " | ____| \\ | |  _ \\   / _ \\|  ___| |  \\/  (_)___ ___(_) ___  _ __ | __ )  _____  __\n" +
                        " |  _| |  \\| | | | | | | | | |_    | |\\/| | / __/ __| |/ _ \\| '_ \\|  _ \\ / _ \\ \\/ /\n" +
                        " | |___| |\\  | |_| | | |_| |  _|   | |  | | \\__ \\__ \\ | (_) | | | | |_) | (_) >  < \n" +
                        " |_____|_| \\_|____/   \\___/|_|     |_|  |_|_|___/___/_|\\___/|_| |_|____/ \\___/_/\\_\\\n" +
                        "                                                                                   ");
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

        initRaspi();
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
        gameMode.runGame();


    }

    private static void initProgressSystem() {
        relaidPBLeds1 = new RelayProgressRedYellowGreen(MBX_LED_PROGRESS1_RED, MBX_LED_PROGRESS1_YELLOW, MBX_LED_PROGRESS1_GREEN);
        relaidPBLeds2 = new RelayProgressRedYellowGreen(MBX_LED_PROGRESS2_RED, MBX_LED_PROGRESS2_YELLOW, MBX_LED_PROGRESS2_GREEN);
    }

    //todo: das ganze hin und her über die Hauptklasse muss anders werden. das ist schrecklicher code.
    public static void prepareGame() {
        if (gameMode == null) return;
        ((Farcry1Assault) gameMode).prepareGame();
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
        pinHandler.add(new Relay(MBX_SIREN1, Color.ORANGE, debugPanel4Pins, 70, 60)); // Main Siren
        pinHandler.add(new Relay(MBX_RESPAWN_SIREN, Color.ORANGE, debugPanel4Pins, 70, 80));
        pinHandler.add(new Relay(MBX_SHUTDOWN_SIREN, Color.MAGENTA, debugPanel4Pins, 20, 40)); // Shutdown Signal
        pinHandler.add(new Relay(MBX_AIRSIREN, Color.ORANGE, debugPanel4Pins, 50, 90)); // Motor Siren for Start Stop Signals

        // die leds in den Dome Buttons

        pinHandler.add(new Relay(MBX_LED1_BTN_RED, Color.RED, debugPanel4Pins));
        pinHandler.add(new Relay(MBX_LED1_BTN_GREEN, Color.GREEN, debugPanel4Pins));

        pinHandler.add(new Relay(MBX_LED2_BTN_RED, Color.RED, debugPanel4Pins));
        pinHandler.add(new Relay(MBX_LED2_BTN_GREEN, Color.GREEN, debugPanel4Pins));

        // die fortschritts leds 1
        pinHandler.add(new Relay(MBX_LED_PROGRESS1_RED, Color.RED, debugPanel4Pins));
        pinHandler.add(new Relay(MBX_LED_PROGRESS1_YELLOW, Color.YELLOW, debugPanel4Pins));
        pinHandler.add(new Relay(MBX_LED_PROGRESS1_GREEN, Color.GREEN, debugPanel4Pins));

        // die fortschritts leds 2
        pinHandler.add(new Relay(MBX_LED_PROGRESS2_RED, Color.RED, debugPanel4Pins));
        pinHandler.add(new Relay(MBX_LED_PROGRESS2_YELLOW, Color.YELLOW, debugPanel4Pins));
        pinHandler.add(new Relay(MBX_LED_PROGRESS2_GREEN, Color.GREEN, debugPanel4Pins));

        // for hardware testing only
        pinHandler.add(new Relay(outputMap.get("GPIO 0"), "relay1", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("GPIO 2"), "relay2", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("GPIO 3"), "relay3", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("GPIO 12"), "relay4", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("GPIO 13"), "relay5", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("GPIO 14"), "relay6", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("GPIO 21"), "relay7", Color.BLUE, debugPanel4Pins));
        pinHandler.add(new Relay(outputMap.get("GPIO 22"), "relay8", Color.BLUE, debugPanel4Pins));

//        pinHandler.add(new Relay(outputMap.get("mcp23017-01-B0"), "mcp23017-01-B0", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-01-B1"), "mcp23017-01-B1", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-01-B2"), "mcp23017-01-B2", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-01-B3"), "mcp23017-01-B3", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-01-B4"), "mcp23017-01-B4", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-01-B5"), "mcp23017-01-B5", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-01-B6"), "mcp23017-01-B6", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-01-B7"), "mcp23017-01-B7", Color.BLUE, debugPanel4Pins));
//
//        pinHandler.add(new Relay(outputMap.get("mcp23017-01-A0"), "mcp23017-01-A0", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-01-A1"), "mcp23017-01-A1", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-01-A2"), "mcp23017-01-A2", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-01-A3"), "mcp23017-01-A3", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-01-A4"), "mcp23017-01-A4", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-01-A5"), "mcp23017-01-A5", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-01-A6"), "mcp23017-01-A6", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-01-A7"), "mcp23017-01-A7", Color.BLUE, debugPanel4Pins));
//
//        pinHandler.add(new Relay(outputMap.get("mcp23017-02-A0"), "mcp23017-02-A0", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-02-A1"), "mcp23017-02-A1", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-02-A2"), "mcp23017-02-A2", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-02-A3"), "mcp23017-02-A3", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-02-A4"), "mcp23017-02-A4", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-02-A5"), "mcp23017-02-A5", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-02-A6"), "mcp23017-02-A6", Color.BLUE, debugPanel4Pins));
//        pinHandler.add(new Relay(outputMap.get("mcp23017-02-A7"), "mcp23017-02-A7", Color.BLUE, debugPanel4Pins));

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
        config.put(MBX_STARTGAME_SIRENTIME, "5000");


//        config.put(MBX_SIREN_TIME, "750");

        config.put(MBX_LOGLEVEL, "debug");

        // config.put(MBX_I2C_1, "0x20");
        // config.put(MBX_I2C_2, "0x24");

        // hier werden die üblichen Zuordnungen der einzelnen GPIOs
        // zu den jeweiligen Signalleitungen vorgenommen.
        // Falls man mal was umstecken muss, kann man das einfach
        // später in der missionbox.cfg ändern.
        // diese Werte werden hier so gesetzt, wie es in der
        // Bauanleitung der Box steht.

        // die hier brauchen wir immer
        // Änderung auf RASPI Pins statt MCP23017 PINS am 26.07.2017
        // GPIO 0


        // Standardwerte für die config datei werden hier gesetzt.
        // danach werden evtl. die geänderten Werte aus der Datei drübergeschrieben
        // bei Programm Ende werden alle geänderten Werte wieder in die config.txt zurückgeschrieben
        config.put(MBX_AIRSIREN, "GPIO 0"); // Relais 1
        config.put(MBX_SIREN1, "GPIO 2");  // Relais 2
        config.put(MBX_SHUTDOWN_SIREN, "GPIO 3");  // Relais 3
        config.put(MBX_RESPAWN_SIREN, "GPIO 12");  // Relais 4

        config.put(MBX_LED1_BTN_RED, "GPIO 11");
        config.put(MBX_LED1_BTN_GREEN, "GPIO 26");

        config.put(MBX_LED2_BTN_RED, "GPIO 23");
        config.put(MBX_LED2_BTN_GREEN, "GPIO 24");
        
        config.put(MBX_LED_PROGRESS1_RED, "GPIO 5");
        config.put(MBX_LED_PROGRESS1_YELLOW, "GPIO 6");
        config.put(MBX_LED_PROGRESS1_GREEN, "GPIO 10");

        config.put(MBX_LED_PROGRESS2_RED, "GPIO 27");
        config.put(MBX_LED_PROGRESS2_YELLOW, "GPIO 28");
        config.put(MBX_LED_PROGRESS2_GREEN, "GPIO 29");

        config.put(MBX_BTN_RED, "GPIO 15");
        config.put(MBX_BTN_GREEN, "GPIO 16");
        config.put(MBX_BTN_START_STOP, "GPIO 1");
        config.put(MBX_BTN_PAUSE, "GPIO 4");

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

    public static void setPBLeds(BigDecimal percent) {
        if (relaidPBLeds1 != null) relaidPBLeds1.setValue(percent);
        if (relaidPBLeds2 != null) relaidPBLeds2.setValue(percent);
    }


    public static void setProgress(BigDecimal percent) {
        if (relaisSirens != null) relaisSirens.setValue(percent);
        frmTest.setProgress(percent.intValue());
        if (relaidPBLeds1 != null) relaidPBLeds1.setValue(percent);
        if (relaidPBLeds2 != null) relaidPBLeds2.setValue(percent);
    }

    public static void setProgress(long start, long now, long stop) {
        if (relaisSirens != null) relaisSirens.setValue(start, now, stop);
        frmTest.setProgress(start, now, stop);
        if (relaidPBLeds1 != null) relaidPBLeds1.setValue(start, now, stop);
        if (relaidPBLeds2 != null) relaidPBLeds2.setValue(start, now, stop);
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

    private static void initRaspi() throws IOException, I2CFactory.UnsupportedBusNumberException {

        GPIO = null;
        if (Tools.isArm()) {
            try {
                GPIO = GpioFactory.getInstance();

            } catch (Exception e) {
                logger.fatal(e);
                System.exit(0);
            }

//            MCP23017GpioProvider gpioProvider0 = new MCP23017GpioProvider(I2CBus.BUS_1, Integer.decode(config.getProperty(MissionBox.MBX_I2C_1)));
//            MCP23017GpioProvider gpioProvider1 = new MCP23017GpioProvider(I2CBus.BUS_1, Integer.decode(config.getProperty(MissionBox.MBX_I2C_2)));

//            GpioPinDigitalOutput myOutputs[] = {
//                    GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A0, "mcp23017-01-A0", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A1, "mcp23017-01-A1", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A2, "mcp23017-01-A2", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A3, "mcp23017-01-A3", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A4, "mcp23017-01-A4", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A5, "mcp23017-01-A5", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A6, "mcp23017-01-A6", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A7, "mcp23017-01-A7", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B0, "mcp23017-01-B0", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B1, "mcp23017-01-B1", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B2, "mcp23017-01-B2", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B3, "mcp23017-01-B3", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B4, "mcp23017-01-B4", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B5, "mcp23017-01-B5", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B6, "mcp23017-01-B6", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B7, "mcp23017-01-B7", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_A0, "mcp23017-02-A0", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_A1, "mcp23017-02-A1", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_A2, "mcp23017-02-A2", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_A3, "mcp23017-02-A3", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_A4, "mcp23017-02-A4", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_A5, "mcp23017-02-A5", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_A6, "mcp23017-02-A6", PinState.LOW),
//                    GPIO.provisionDigitalOutputPin(gpioProvider1, MCP23017Pin.GPIO_A7, "mcp23017-02-A7", PinState.LOW)
//            };
//            for (int ioPin = 0; ioPin < myOutputs.length; ioPin++) {
//                outputMap.put(myOutputs[ioPin].getName(), myOutputs[ioPin]);
//            }
//
//            GpioPinDigitalInput myInputs[] = {
//                    GPIO.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B0, "mcp23017-02-B0", PinPullResistance.PULL_UP),
//                    GPIO.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B1, "mcp23017-02-B1", PinPullResistance.PULL_UP),
//                    GPIO.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B2, "mcp23017-02-B2", PinPullResistance.PULL_UP),
//                    GPIO.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B3, "mcp23017-02-B3", PinPullResistance.PULL_UP),
//                    GPIO.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B4, "mcp23017-02-B4", PinPullResistance.PULL_UP),
//                    GPIO.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B5, "mcp23017-02-B5", PinPullResistance.PULL_UP),
//                    GPIO.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B6, "mcp23017-02-B6", PinPullResistance.PULL_UP),
//                    GPIO.provisionDigitalInputPin(gpioProvider1, MCP23017Pin.GPIO_B7, "mcp23017-02-B7", PinPullResistance.PULL_UP)
//            };
//            for (int ioPin = 0; ioPin < myInputs.length; ioPin++) {
//                inputMap.put(myInputs[ioPin].getName(), myInputs[ioPin]);
//            }

            /***
             *       ___  _   _ _____ ____  _   _ _____   ____  _
             *      / _ \| | | |_   _|  _ \| | | |_   _| |  _ \(_)_ __  ___
             *     | | | | | | | | | | |_) | | | | | |   | |_) | | '_ \/ __|
             *     | |_| | |_| | | | |  __/| |_| | | |   |  __/| | | | \__ \
             *      \___/ \___/  |_| |_|    \___/  |_|   |_|   |_|_| |_|___/
             *
             */
            GpioPinDigitalOutput myOutputs[] = {
                    GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_00, PinState.LOW), // Rly1
                    GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_02, PinState.LOW), // Rly2
                    GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_03, PinState.LOW), // Rly3
                    GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_12, PinState.LOW), // Rly4
                    GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_13, PinState.LOW), // Rly5
                    GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_14, PinState.LOW), // Rly6
                    GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_21, PinState.LOW), // Rly7
                    GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_22, PinState.LOW), // Rly8
                    GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_05, PinState.LOW), // PB1 RED
                    GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_06, PinState.LOW), // PB1 YLW
                    GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_10, PinState.LOW), // PB1 GRN
                    GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_11, PinState.LOW), // BTNLED1 RED
                    GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_26, PinState.LOW), // BTNLED1 GREEN
                    GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_27, PinState.LOW), // PB2 RED
                    GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_28, PinState.LOW), // PB2 YLW
                    GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_29, PinState.LOW), // PB2 GRN
                    GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_23, PinState.LOW), // BTNLED2 RED
                    GPIO.provisionDigitalOutputPin(RaspiPin.GPIO_24, PinState.LOW) // BTNLED2 GREEN
            };
            for (int ioPin = 0; ioPin < myOutputs.length; ioPin++) {
                outputMap.put(myOutputs[ioPin].getName(), myOutputs[ioPin]);
            }

            /***
             *      ___ _   _ ____  _   _ _____   ____  _
             *     |_ _| \ | |  _ \| | | |_   _| |  _ \(_)_ __  ___
             *      | ||  \| | |_) | | | | | |   | |_) | | '_ \/ __|
             *      | || |\  |  __/| |_| | | |   |  __/| | | | \__ \
             *     |___|_| \_|_|    \___/  |_|   |_|   |_|_| |_|___/
             *
             */


            // PULL_UP bei direktem Anschluss an den Raspi
            // PULL_DOWN bei Anschluss über einen MCP23017

            GpioPinDigitalInput myInputs[] = {
                    GPIO.provisionDigitalInputPin(RaspiPin.GPIO_15, PinPullResistance.PULL_UP),
                    GPIO.provisionDigitalInputPin(RaspiPin.GPIO_16, PinPullResistance.PULL_UP),
                    GPIO.provisionDigitalInputPin(RaspiPin.GPIO_01, PinPullResistance.PULL_UP),
                    GPIO.provisionDigitalInputPin(RaspiPin.GPIO_04, PinPullResistance.PULL_UP)
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
            ioPAUSE = inputMap.get((config.getProperty(MBX_BTN_PAUSE)));
        }

    }


    public static void shutdownEverything() {
        pinHandler.off();
        GPIO.shutdown();


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
