package main;

import com.pi4j.io.gpio.*;
import com.pi4j.io.i2c.I2CFactory;
import gamemodes.Farcry1Assault;
import gamemodes.Farcry1GameEvent;
import gamemodes.GameMode;
import hardware.abstraction.MyAbstractButton;
import hardware.abstraction.MyPin;
import interfaces.PercentageInterface;
import misc.Configs;
import misc.Tools;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import progresshandlers.RelayProgressRedYellowGreen;
import threads.MessageProcessor;
import threads.PinHandler;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;


/**
 * Created by tloehr on 22.04.15.
 */
public class Main {


    // Klemmleiste
    private static final Pin PIN_AIRSIREN = RaspiPin.GPIO_00;
    private static final String NAME_AIRSIREN = "airsiren";
    private static final Pin PIN_SIREN1 = RaspiPin.GPIO_02;
    private static final String NAME_SIREN1 = "siren1";
    private static final Pin PIN_SHUTDOWN_SIREN = RaspiPin.GPIO_03;
    private static final String NAME_SHUTDOWN_SIREN = "shutdownsiren";
    private static final Pin PIN_RESPAWN_SIREN = RaspiPin.GPIO_12;
    private static final String NAME_RESPAWN_SIREN = "respawnsiren";

    private static final Pin PIN_LED1_BTN_RED = RaspiPin.GPIO_11;
    private static final String NAME_LED1_BTN_RED = "led1_button_red";
    private static final Pin PIN_LED1_BTN_GREEN = RaspiPin.GPIO_26;
    private static final String NAME_LED1_BTN_GREEN = "led1_button_green";

    private static final Pin PIN_LED2_BTN_RED = RaspiPin.GPIO_23;
    private static final String NAME_LED2_BTN_RED = "led2_button_red";
    private static final Pin PIN_LED2_BTN_GREEN = RaspiPin.GPIO_24;
    private static final String NAME_LED2_BTN_GREEN = "led2_button_green";

    private static final Pin PIN_LED1_PROGRESS_RED = RaspiPin.GPIO_05;
    private static final String NAME_LED1_PROGRESS_RED = "led1_progress_red";
    private static final Pin PIN_LED1_PROGRESS_YELLOW = RaspiPin.GPIO_06;
    private static final String NAME_LED1_PROGRESS_YELLOW = "led1_progress_yellow";
    private static final Pin PIN_LED1_PROGRESS_GREEN = RaspiPin.GPIO_10;
    private static final String NAME_LED1_PROGRESS_GREEN = "led1_progress_green";

    private static final Pin PIN_LED2_PROGRESS_RED = RaspiPin.GPIO_27;
    private static final String NAME_LED2_PROGRESS_RED = "led2_progress_red";
    private static final Pin PIN_LED2_PROGRESS_YELLOW = RaspiPin.GPIO_28;
    private static final String NAME_LED2_PROGRESS_YELLOW = "led2_progress_yellow";
    private static final Pin PIN_LED2_PROGRESS_GREEN = RaspiPin.GPIO_29;
    private static final String NAME_LED2_PROGRESS_GREEN = "led2_progress_green";

    private static final Pin PIN_BTN_RED = RaspiPin.GPIO_15;
    private static final Pin PIN_BTN_GREEN = RaspiPin.GPIO_16;
    private static final Pin PIN_BTN_START_STOP = RaspiPin.GPIO_01;
    private static final Pin PIN_BTN_PAUSE = RaspiPin.GPIO_04;


    public static final int DEBOUNCE = 200; //ms


    private static int startup_progress = 0;
    private static Logger logger;
    private static Level logLevel = Level.DEBUG;
    private static GpioController GPIO;
    private static FrmTest frmTest;
    private static Configs configs;
    private static MessageProcessor messageProcessor;


    public static PinHandler getPinHandler() {
        return pinHandler;
    }

    private static GameMode gameMode;

    private static GpioPinDigitalInput ioRed, ioGreen, ioGameStartStop, ioPAUSE;
    private static MyAbstractButton btnRed, btnGreen, btnGameStartStop, btnPAUSE;
    private static JButton btnQuit;

    private static PercentageInterface relaidPBLeds1, relaidPBLeds2;


    private static PinHandler pinHandler = null;


    private static void initBaseSystem() throws IOException {


        System.setProperty("logs", Tools.getWorkingPath());
        logger = Logger.getRootLogger();
        Logger.getRootLogger().setLevel(logLevel);

        logger.info("\n" +
                "  ____ _____  _    ____ _____ ___ _   _  ____   __  __ _         _             ____            \n" +
                " / ___|_   _|/ \\  |  _ \\_   _|_ _| \\ | |/ ___| |  \\/  (_)___ ___(_) ___  _ __ | __ )  _____  __\n" +
                " \\___ \\ | | / _ \\ | |_) || |  | ||  \\| | |  _  | |\\/| | / __/ __| |/ _ \\| '_ \\|  _ \\ / _ \\ \\/ /\n" +
                "  ___) || |/ ___ \\|  _ < | |  | || |\\  | |_| | | |  | | \\__ \\__ \\ | (_) | | | | |_) | (_) >  < \n" +
                " |____/ |_/_/   \\_\\_| \\_\\|_| |___|_| \\_|\\____| |_|  |_|_|___/___/_|\\___/|_| |_|____/ \\___/_/\\_\\\n" +
                "                                                                                               ");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            pinHandler.off();
            if (messageProcessor != null) messageProcessor.interrupt();
            if (GPIO != null) {


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


        pinHandler = new PinHandler();
        configs = new Configs();

        if (Long.parseLong(configs.get(Configs.MIN_STAT_SEND_TIME)) > 0 && configs.isFTPComplete()) {
            messageProcessor = new MessageProcessor();
            messageProcessor.start();
        }

        logger.info(configs.getApplicationInfo("program.BUILDDATE") + " [" + configs.getApplicationInfo("program.BUILDNUM") + "]");
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
    }

    private static void initDebugFrame() throws Exception {
        UIManager.setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName());
        frmTest = new FrmTest();
        frmTest.setVisible(true);
        if (Tools.isArm()) frmTest.setExtendedState(JFrame.MAXIMIZED_BOTH);
        btnQuit.addActionListener(e -> {
            Main.shutdownEverything();
            System.exit(0);
        });
    }


    public static final void main(String[] args) throws Exception {


        Tools.printProgBar(startup_progress);

        pinHandler = new PinHandler();
        configs = new Configs();


        initBaseSystem();
        initDebugFrame();
        initRaspi();
        initPinHandler();
        initProgressSystem();
        initGameSystem();


    }

    private static void initProgressSystem() {
        relaidPBLeds1 = new RelayProgressRedYellowGreen(NAME_LED1_PROGRESS_RED, NAME_LED1_PROGRESS_YELLOW, NAME_LED1_PROGRESS_GREEN);
        relaidPBLeds2 = new RelayProgressRedYellowGreen(NAME_LED2_PROGRESS_RED, NAME_LED2_PROGRESS_YELLOW, NAME_LED2_PROGRESS_GREEN);
    }

    //todo: das ganze hin und her über die Hauptklasse muss anders werden. das ist schrecklicher code.
    public static void prepareGame() {
        if (gameMode == null) return;
        ((Farcry1Assault) gameMode).prepareGame();
    }


    private static void initPinHandler() {

        pinHandler = new PinHandler();

        JPanel debugPanel4Pins = frmTest.getDebugPanel4Pins();

        // these relays belong to one cd. They are all connected to the same siren circuit.

        // three sirens now.
        // Siren 1


        // hier gehts weiter

        pinHandler.add(new MyPin(GPIO, PIN_SIREN1, Color.ORANGE, debugPanel4Pins, NAME_SIREN1, 70, 60)); // Main Siren
        pinHandler.add(new MyPin(GPIO, PIN_RESPAWN_SIREN, Color.ORANGE, debugPanel4Pins, NAME_RESPAWN_SIREN, 70, 80));
        pinHandler.add(new MyPin(GPIO, PIN_SHUTDOWN_SIREN, Color.MAGENTA, debugPanel4Pins, NAME_SHUTDOWN_SIREN, 20, 40)); // Shutdown Signal
        pinHandler.add(new MyPin(GPIO, PIN_AIRSIREN, Color.ORANGE, debugPanel4Pins, NAME_AIRSIREN, 50, 90)); // Motor Siren for Start Stop Signals

        // die leds in den Dome Buttons

        pinHandler.add(new MyPin(GPIO, PIN_LED1_BTN_RED, Color.RED, debugPanel4Pins, NAME_LED1_BTN_RED));
        pinHandler.add(new MyPin(GPIO, PIN_LED1_BTN_GREEN, Color.GREEN, debugPanel4Pins, NAME_LED1_BTN_GREEN));

        pinHandler.add(new MyPin(GPIO, PIN_LED2_BTN_RED, Color.RED, debugPanel4Pins, NAME_LED2_BTN_RED));
        pinHandler.add(new MyPin(GPIO, PIN_LED2_BTN_GREEN, Color.GREEN, debugPanel4Pins, NAME_LED2_BTN_GREEN));

        // die fortschritts leds 1
        pinHandler.add(new MyPin(GPIO, PIN_LED1_PROGRESS_RED, Color.RED, debugPanel4Pins, NAME_LED1_PROGRESS_RED));
        pinHandler.add(new MyPin(GPIO, PIN_LED1_PROGRESS_YELLOW, Color.YELLOW, debugPanel4Pins, NAME_LED1_PROGRESS_YELLOW));
        pinHandler.add(new MyPin(GPIO, PIN_LED1_PROGRESS_GREEN, Color.GREEN, debugPanel4Pins, NAME_LED1_PROGRESS_GREEN));

        // die fortschritts leds 2
        pinHandler.add(new MyPin(GPIO, PIN_LED2_PROGRESS_RED, Color.RED, debugPanel4Pins, NAME_LED2_PROGRESS_RED));
        pinHandler.add(new MyPin(GPIO, PIN_LED2_PROGRESS_YELLOW, Color.YELLOW, debugPanel4Pins, NAME_LED2_PROGRESS_YELLOW));
        pinHandler.add(new MyPin(GPIO, PIN_LED2_PROGRESS_GREEN, Color.GREEN, debugPanel4Pins, NAME_LED2_PROGRESS_GREEN));


    }

//    public static void setScheme(String name, String scheme, Object... objects) {
//        pinHandler.setScheme(name, String.format(scheme, objects));
//    }
//
//    public static void setScheme(String name, String scheme) {
//        pinHandler.setScheme(name, scheme);
//    }
//
//    public static void off(String name) {
//        pinHandler.off(name);
//    }
//
//    public static void on(String name) {
//        pinHandler.on(name);
//    }

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

    public static void setPBLeds(BigDecimal percent) {
        if (relaidPBLeds1 != null) relaidPBLeds1.setValue(percent);
        if (relaidPBLeds2 != null) relaidPBLeds2.setValue(percent);
    }


    public static void setProgress(BigDecimal percent) {
        frmTest.setProgress(percent.intValue());
        if (relaidPBLeds1 != null) relaidPBLeds1.setValue(percent);
        if (relaidPBLeds2 != null) relaidPBLeds2.setValue(percent);
    }

    public static void setProgress(long start, long now, long stop) {
        frmTest.setProgress(start, now, stop);
        if (relaidPBLeds1 != null) relaidPBLeds1.setValue(start, now, stop);
        if (relaidPBLeds2 != null) relaidPBLeds2.setValue(start, now, stop);
    }


    public static void setTimerMessage(String message) {
        frmTest.setTimer(message);
//        logger.debug(message);
    }

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

    public static Configs getConfigs() {
        return configs;
    }

    public static void setRevertEvent(Farcry1GameEvent revert2Event) {
        ((Farcry1Assault) Main.gameMode).setRevertEvent(revert2Event);
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
        btnRed = new MyAbstractButton(GPIO, PIN_BTN_RED, getGUIBtnRed());
        btnGreen = new MyAbstractButton(GPIO, PIN_BTN_GREEN, getGUIBtnGreen());
        btnGameStartStop = new MyAbstractButton(GPIO, PIN_BTN_START_STOP, getGUIBtn1());
        btnQuit = frmTest.getBtn2();
        btnPAUSE = new MyAbstractButton(GPIO, PIN_BTN_PAUSE, getGUIBtnPause());

        gameMode = new Farcry1Assault();
        gameMode.runGame();


    }
}
