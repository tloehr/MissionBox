package main;

import com.pi4j.io.gpio.*;
import com.pi4j.io.i2c.I2CFactory;
import gamemodes.Farcry1Assault;
import gamemodes.Farcry1GameEvent;
import gamemodes.GameMode;
import hardware.abstraction.MyAbstractButton;
import hardware.abstraction.MyPin;
import interfaces.PercentageInterface;
import interfaces.Relay;
import misc.Configs;
import misc.SortedProperties;
import misc.Tools;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import progresshandlers.RelayProgressRedYellowGreen;
import threads.PinHandler;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Properties;

import static com.sun.org.apache.xalan.internal.utils.SecuritySupport.getResourceAsStream;


/**
 * Created by tloehr on 22.04.15.
 */
public class MissionBox {


    public static final String PH_POLE = "flagPole";
    public static final String PH_LED_RED_BTN = "ledRedButton";
    public static final String PH_LED_BLUE_BTN = "ledBlueButton";
    public static final String PH_LED_GREEN_BTN = "ledGreenButton";
    public static final String PH_LED_YELLOW_BTN = "ledYellowButton";
    public static final String PH_LED_GREEN = "ledGreen";
    public static final String PH_LED_WHITE = "ledWhite";
    public static final String PH_SIREN_COLOR_CHANGE = "colorchangesiren";
    public static final String PH_AIRSIREN = "airsiren";

    public static final String PH_RESERVE01 = "reserve01";
    public static final String PH_RESERVE02 = "reserve02";
    public static final String PH_RESERVE03 = "reserve03";
    public static final String PH_RESERVE04 = "reserve04";
    public static final String PH_RESERVE05 = "reserve05";
    public static final String PH_RESERVE06 = "reserve06";
    public static final String PH_RESERVE07 = "reserve07";
    public static final String PH_RESERVE08 = "reserve08";
    public static final String PH_RESERVE09 = "reserve09";
    public static final String PH_RESERVE10 = "reserve10";

    // Parameter für die einzelnen PINs am Raspi sowie die I2C Adressen.
    private static final int DISPLAY_BLUE = 0x71;
    private static final int DISPLAY_RED = 0x72;
    private static final int DISPLAY_WHITE = 0x70;
    private static final int DISPLAY_YELLOW = 0x73;
    private static final int DISPLAY_GREEN = 0x74;

    private static final int MCP23017_1 = 0x20;

    // Linke Seite des JP8 Header

    // Klemmleiste
    /* btn01 */ private static final Pin MBX_AIRSIREN = RaspiPin.GPIO_00;
    /* btn02 */ private static final Pin MBX_SIREN1 = RaspiPin.GPIO_02;
    /* btn03 */ private static final Pin MBX_SHUTDOWN_SIREN = RaspiPin.GPIO_03;
    /* btn04 */ private static final Pin MBX_RESPAWN_SIREN = RaspiPin.GPIO_12;

    /* btn05 */ private static final Pin MBX_LED1_BTN_RED = RaspiPin.GPIO_11;
    /* btn06 */ private static final Pin MBX_LED1_BTN_GREEN = RaspiPin.GPIO_26;

    /* btn07 */ private static final Pin MBX_LED2_BTN_RED = RaspiPin.GPIO_23;
    /* btn08 */ private static final Pin MBX_LED2_BTN_GREEN = RaspiPin.GPIO_24;

    private static final Pin MBX_LED_PROGRESS1_RED = RaspiPin.GPIO_05;
    private static final Pin MBX_LED_PROGRESS1_YELLOW = RaspiPin.GPIO_06;
    private static final Pin MBX_LED_PROGRESS1_GREEN = RaspiPin.GPIO_10;

    private static final Pin MBX_LED_PROGRESS2_RED = RaspiPin.GPIO_27;
    private static final Pin MBX_LED_PROGRESS2_YELLOW = RaspiPin.GPIO_28;
    private static final Pin MBX_LED_PROGRESS2_GREEN = RaspiPin.GPIO_29;


    /* btn05 */ private static final Pin MBX_BTN_RED = RaspiPin.GPIO_15;
    /* btn06 */ private static final Pin MBX_BTN_GREEN = RaspiPin.GPIO_16;
    /* btn07 */ private static final Pin MBX_BTN_START_STOP = RaspiPin.GPIO_01;
    /* btn08 */ private static final Pin MBX_BTN_PAUSE = RaspiPin.GPIO_04;


    public static final int DEBOUNCE = 200; //ms


    private static int startup_progress = 0;
    private static Logger logger;
    private static Level logLevel = Level.DEBUG;
    private static GpioController GPIO;
    private static FrmTest frmTest;
    private static Configs configs;


    public static PinHandler getPinHandler() {
        return pinHandler;
    }

    private static GameMode gameMode;

    private static GpioPinDigitalInput ioRed, ioGreen, ioGameStartStop, ioPAUSE;
    private static MyAbstractButton btnRed, btnGreen, btnGameStartStop, btnPAUSE;
    private static JButton btnQuit;

//    private static final HashMap<String, GpioPinDigitalOutput> outputMap = new HashMap<>();
//    private static final HashMap<String, GpioPinDigitalInput> inputMap = new HashMap<>();

    private static PercentageInterface relaisSirens, relaidPBLeds1, relaidPBLeds2;

    public static SortedProperties appinfo = new SortedProperties();


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

        logger.info(MissionBox.getAppinfo().getProperty("program.BUILDDATE") + " [" + MissionBox.getAppinfo().getProperty("program.BUILDNUM") + "]");


        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {

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

        pinHandler = new PinHandler();
        configs = new Configs();


        frmTest = new FrmTest();
        frmTest.setVisible(true);

        initRaspi();
        initGameSystem();
        initProgressSystem();


        if (GPIO != null) frmTest.setExtendedState(JFrame.MAXIMIZED_BOTH);

        startup_progress = startup_progress + 10;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);

        startup_progress = startup_progress + 10;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);


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

    private static void initPinHandler() {

        pinHandler = new PinHandler();

        JPanel debugPanel4Pins = frmTest.getDebugPanel4Pins();

        // these relays belong to one cd. They are all connected to the same siren circuit.

        // three sirens now.
        // Siren 1




        // hier gehts weiter

        pinHandler.add(new MyPin(GPIO, MBX_SIREN1, Color.ORANGE, debugPanel4Pins, "", 70, 60)); // Main Siren
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


//    public static Properties getConfig() {
//        return config;
//    }


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

        }
    }


    public static void shutdownEverything() {
        pinHandler.off();
        if (GPIO != null) GPIO.shutdown();
    }


    public static void setRevertEvent(Farcry1GameEvent revert2Event) {
        ((Farcry1Assault) MissionBox.gameMode).setRevertEvent(revert2Event);
    }

    private static void initGameSystem() throws IOException {
        /***
               *      ____        _   _
               *     | __ ) _   _| |_| |_ ___  _ __  ___
               *     |  _ \| | | | __| __/ _ \| '_ \/ __|
               *     | |_) | |_| | |_| || (_) | | | \__ \
               *     |____/ \__,_|\__|\__\___/|_| |_|___/
               *
               */
        btnRed = new MyAbstractButton(GPIO, MBX_BTN_RED, getGUIBtnRed());
        btnGreen = new MyAbstractButton(GPIO, MBX_BTN_GREEN, getGUIBtnGreen());
        btnGameStartStop = new MyAbstractButton(GPIO, MBX_BTN_START_STOP, getGUIBtn1());
        btnQuit = frmTest.getBtn2();
        btnPAUSE = new MyAbstractButton(GPIO, MBX_BTN_PAUSE, getGUIBtnPause());



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


    }
}
