package de.flashheart.missionbox;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CFactory;
import de.flashheart.missionbox.gamemodes.FC1SavePoint;
import de.flashheart.missionbox.gamemodes.Farcry1Assault;
import de.flashheart.missionbox.gui.FrmTest;
import de.flashheart.missionbox.hardware.abstraction.MyAbstractButton;
import de.flashheart.missionbox.hardware.abstraction.MyPin;
import de.flashheart.missionbox.misc.Configs;
import de.flashheart.missionbox.misc.FTPWrapper;
import de.flashheart.missionbox.misc.Tools;
import de.flashheart.missionbox.progresshandlers.ProgressInterface;
import de.flashheart.missionbox.progresshandlers.RelayProgressRedYellowGreen;
import de.flashheart.missionbox.progresshandlers.TickingSlowAndSilent;
import de.flashheart.missionbox.threads.MessageProcessor;
import de.flashheart.missionbox.threads.PinHandler;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

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
    public static final Pin PIN_START_STOP_SIREN = RaspiPin.GPIO_00;
    public static final String NAME_START_STOP_SIREN = "startstopsiren";
    public static final Pin PIN_SIREN1 = RaspiPin.GPIO_02;
    public static final String NAME_SIREN1 = "siren1";
    public static final Pin PIN_SHUTDOWN_SIREN = RaspiPin.GPIO_03;
    public static final String NAME_SHUTDOWN_SIREN = "shutdownsiren";
    public static final Pin PIN_RESPAWN_SIREN = RaspiPin.GPIO_12;
    public static final String NAME_RESPAWN_SIREN = "respawnsiren";

    public static final Pin PIN_LED1_BTN_RED = RaspiPin.GPIO_11;
    public static final String NAME_LED1_BTN_RED = "led1_button_red";
    public static final Pin PIN_LED1_BTN_GREEN = RaspiPin.GPIO_26;
    public static final String NAME_LED1_BTN_GREEN = "led1_button_green";

    public static final Pin PIN_LED2_BTN_RED = RaspiPin.GPIO_23;
    public static final String NAME_LED2_BTN_RED = "led2_button_red";
    public static final Pin PIN_LED2_BTN_GREEN = RaspiPin.GPIO_24;
    public static final String NAME_LED2_BTN_GREEN = "led2_button_green";

    public static final Pin PIN_LED1_PROGRESS_RED = RaspiPin.GPIO_05;
    public static final String NAME_LED1_PROGRESS_RED = "led1_progress_red";
    public static final Pin PIN_LED1_PROGRESS_YELLOW = RaspiPin.GPIO_06;
    public static final String NAME_LED1_PROGRESS_YELLOW = "led1_progress_yellow";
    public static final Pin PIN_LED1_PROGRESS_GREEN = RaspiPin.GPIO_10;
    public static final String NAME_LED1_PROGRESS_GREEN = "led1_progress_green";

    public static final Pin PIN_LED2_PROGRESS_RED = RaspiPin.GPIO_27;
    public static final String NAME_LED2_PROGRESS_RED = "led2_progress_red";
    public static final Pin PIN_LED2_PROGRESS_YELLOW = RaspiPin.GPIO_28;
    public static final String NAME_LED2_PROGRESS_YELLOW = "led2_progress_yellow";
    public static final Pin PIN_LED2_PROGRESS_GREEN = RaspiPin.GPIO_29;
    public static final String NAME_LED2_PROGRESS_GREEN = "led2_progress_green";

    public static final Pin PIN_BTN_RED = RaspiPin.GPIO_15;
    public static final Pin PIN_BTN_GREEN = RaspiPin.GPIO_16;
    public static final Pin PIN_BTN_START_STOP = RaspiPin.GPIO_01;
    public static final Pin PIN_BTN_PAUSE = RaspiPin.GPIO_04;
    private static FTPWrapper ftpWrapper;

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

    private static Farcry1Assault gameMode;

    private static MyAbstractButton btnRed, btnGreen, btnGameStartStop, btnPAUSE;
    private static JButton btnQuit;

    private static ProgressInterface relaidPBLeds1, relaidPBLeds2, relaisSirens;


    private static PinHandler pinHandler = null;


    public static FTPWrapper getFtpWrapper() {
        return ftpWrapper;
    }

    private static void initBaseSystem() throws IOException {
        Tools.printProgBar(startup_progress);

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
            System.exit(0);
        });


        pinHandler = new PinHandler();
        configs = new Configs();

        String title = "MissionBox " + Main.getConfigs().getApplicationInfo("my.version") + "." + Main.getConfigs().getApplicationInfo("buildNumber") + " [" + Main.getConfigs().getApplicationInfo("project.build.timestamp") + "]";
        ftpWrapper = new FTPWrapper();

        logger.info(title);


        messageProcessor = new MessageProcessor();
        messageProcessor.start();


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


    }


    public static final void main(String[] args) throws Exception {
        initBaseSystem();
        initDebugFrame();
        initRaspi();
        initButtons();
        initPinHandler();
        initProgressSystem();
        initGameSystem();
    }

    private static void initButtons() {
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
        btnPAUSE = new MyAbstractButton(GPIO, PIN_BTN_PAUSE, getGUIBtnPause());
        btnQuit = frmTest.getBtn2();

        btnQuit.addActionListener(e -> {
            Main.shutdownEverything();
            System.exit(0);
        });
    }

    private static void initProgressSystem() {
        relaidPBLeds1 = new RelayProgressRedYellowGreen(NAME_LED1_PROGRESS_RED, NAME_LED1_PROGRESS_YELLOW, NAME_LED1_PROGRESS_GREEN);
        relaidPBLeds2 = new RelayProgressRedYellowGreen(NAME_LED2_PROGRESS_RED, NAME_LED2_PROGRESS_YELLOW, NAME_LED2_PROGRESS_GREEN);
    }

    //todo: das ganze hin und her über die Hauptklasse muss anders werden. das ist schrecklicher code.
    public static void prepareGame() {
        if (gameMode == null) return;
        gameMode.prepareGame();
    }


    private static void initPinHandler() {

        pinHandler = new PinHandler();

        JPanel debugPanel4Pins = frmTest.getDebugPanel4Pins();


        pinHandler.add(new MyPin(GPIO, PIN_SIREN1, Color.ORANGE, debugPanel4Pins, NAME_SIREN1, 70, 60)); // Main Siren
        pinHandler.add(new MyPin(GPIO, PIN_RESPAWN_SIREN, Color.ORANGE, debugPanel4Pins, NAME_RESPAWN_SIREN, 70, 80));
        pinHandler.add(new MyPin(GPIO, PIN_SHUTDOWN_SIREN, Color.MAGENTA, debugPanel4Pins, NAME_SHUTDOWN_SIREN, 20, 40)); // Shutdown Signal
        pinHandler.add(new MyPin(GPIO, PIN_START_STOP_SIREN, Color.ORANGE, debugPanel4Pins, NAME_START_STOP_SIREN, 50, 90)); // Motor Siren for Start Stop Signals

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


        relaisSirens = new TickingSlowAndSilent(NAME_SIREN1);

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

    public static Farcry1Assault getGameMode() {
        return gameMode;
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

    public static ProgressInterface getRelaisSirens() {
        return relaisSirens;
    }

    public static void shutdownEverything() {
        ftpWrapper.cleanupStatsFile();
        pinHandler.off();
        if (GPIO != null) GPIO.shutdown();
    }

    public static Configs getConfigs() {
        return configs;
    }

    public static void setRevertEvent(FC1SavePoint revert2Event) {
        ((Farcry1Assault) Main.gameMode).setRevertEvent(revert2Event);
    }

    private static void initGameSystem() throws IOException {

        gameMode = new Farcry1Assault();
        gameMode.runGame();


    }

    public static MessageProcessor getMessageProcessor() {
        return messageProcessor;
    }
}
