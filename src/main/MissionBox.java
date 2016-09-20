package main;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.*;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import gamemodes.Farcry1Assault;
import interfaces.MyAbstractButton;
import interfaces.PercentageInterface;
import interfaces.Relay;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import misc.Tools;
import org.apache.log4j.*;
import progresshandlers.RelayProgressRGB;
import progresshandlers.RelayProgressRedYellowGreen;
import threads.PinHandler;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static com.sun.org.apache.xalan.internal.utils.SecuritySupport.getResourceAsStream;


/**
 * Created by tloehr on 22.04.15.
 */
public class MissionBox {

    private static int startup_progress = 0;
    private static final Logger logger = Logger.getRootLogger();
    private static Level logLevel = Level.DEBUG;
    private static GpioController GPIO;
    private static FrmTest frmTest;
    //    private static FrmSimulator frmSimulator;
    private static Properties config;

    public static PinHandler getPinHandler() {
        return pinHandler;
    }

    private static int gamemode;

    private static GpioPinDigitalInput ioRed, ioGreen, ioGameStartStop, ioMisc, ioUndo;
    private static MyAbstractButton btnRed, btnGreen, btnGameStartStop, btnMisc, btnUndo;

    private static final HashMap<String, GpioPinDigitalOutput> outputMap = new HashMap<>();
    private static final HashMap<String, GpioPinDigitalInput> inputMap = new HashMap<>();

    private static PercentageInterface relaisSirens, relaisLEDs, relaisFlagpole;

    public static Properties appinfo = new Properties();

    public static final String FCY_TIME2CAPTURE = "fcy.time2capture";
    public static final String FCY_GAMETIME = "fcy.gametime";
    public static final String FCY_MUSIC = "fcy.music";
    public static final String FCY_RESPAWN_SIGNAL = "fcy.respawn.signal";
    public static final String FCY_SOUND = "fcy.sound";
    public static final String FCY_SIREN = "fcy.siren";
    public static final String MBX_SIREN_TIME = "mbx.siren.time"; // in ms
    //    public static final String MBX_GUI = "mbx.gui";
    public static final String MBX_SIRENHANDLER = "mbx.sirenhandler";
    public static final String MBX_LOGLEVEL = "mbx.loglevel";
    public static final String FCY_RESPAWN = "fcy.respawn";

    public static final String MBX_SIREN1 = "mbx.siren1";
    public static final String MBX_SIREN2 = "mbx.siren2";
    public static final String MBX_SIREN3 = "mbx.siren3";
    public static final String MBX_SHUTDOWN_SIREN = "mbx.shutdown.siren";
    public static final String MBX_TIME_SIREN = "mbx.time.siren";
    public static final String MBX_RESPAWN_SIREN = "mbx.respawn.siren";
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
    public static final String MBX_BTN_QUIT = "mbx.button.quit";
    public static final String MBX_BTN_UNDO = "mbx.button.undo";

    private static boolean SOUND = false;
    private static boolean SIREN = false;
    private static boolean MUSIC = false;
    private static boolean RESPAWN = false;

    private static HashMap<String, Object> soundMap = new HashMap<>();
    //    private static HashMap<String, GpioPinDigitalOutput> outputMap = new HashMap<>();
//    private static HashMap<String, GpioPinDigitalInput> inputMap = new HashMap<>();
    private static HashMap<String, Relay> relayMap = new HashMap<>();

    private static ArrayList<String> winnerSongs = new ArrayList<>();
    private static ArrayList<String> looserSongs = new ArrayList<>();
    private static int currentLooser = 0, currentWinner = 0;
    private static Music winner = null, looser = null;

    private static HashMap<String, Sound> timeAnnouncements = new HashMap();
    private static Sound[] countdown = new Sound[11];

    private static PinHandler pinHandler = null;


    public static Properties getAppinfo() {
        return appinfo;
    }

    public static final void main(String[] args) throws Exception {

        PatternLayout layout = new PatternLayout("%d{ISO8601} %-5p [%t] %c: %m%n");
        logger.addAppender(new ConsoleAppender(layout));
        logger.addAppender(new FileAppender(layout, Tools.getMissionboxDirectory() + File.separator + "missionbox.log"));

        try {
            // Lade Build Informationen   2
            InputStream in2 = null;
            //Class clazz = getClass();
            in2 = getResourceAsStream("appinfo.properties");
            appinfo.load(in2);
            in2.close();
        } catch (IOException iOException) {
            iOException.printStackTrace();
        }

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
//        frmSimulator = new FrmSimulator();


        frmTest = new FrmTest();
        frmTest.setVisible(true);

        initSound();
        hwinit();
        initPinHandler();
        initProgressSystem();

        if (GPIO == null) SIREN = false; // override for local pc usage

        startup_progress = startup_progress + 10;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);

        startup_progress = startup_progress + 10;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);

        btnRed = new MyAbstractButton(ioRed, getGUIBtnRed());
        btnGreen = new MyAbstractButton(ioGreen, getGUIBtnGreen());
        btnGameStartStop = new MyAbstractButton(ioGameStartStop, getGUIBtn1());
        btnMisc = new MyAbstractButton(ioMisc, getGUIBtn2());
        btnUndo = new MyAbstractButton(ioUndo, getGUIBtnUndo());

        startup_progress = 100;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);

        Farcry1Assault fc = new Farcry1Assault();
    }

    private static void initProgressSystem() {
        relaisLEDs = new RelayProgressRedYellowGreen(MBX_LED_PB_RED, MBX_LED_PB_YELLOW, MBX_LED_PB_GREEN);
        relaisFlagpole = new RelayProgressRGB(MBX_LED_RGB_RED, MBX_LED_RGB_BLUE, MBX_LED_RGB_GREEN);
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
        pinHandler.add(1, new Relay(MBX_SIREN1, Color.ORANGE, debugPanel4Pins)); // Original Siren Button 3
        pinHandler.add(1, new Relay(MBX_SIREN2, Color.ORANGE, debugPanel4Pins)); // Original Siren Button 3
        pinHandler.add(1, new Relay(MBX_SIREN3, Color.ORANGE, debugPanel4Pins)); // Original Siren Button 5
        pinHandler.add(1, new Relay(MBX_SHUTDOWN_SIREN, Color.MAGENTA, debugPanel4Pins)); // Original Siren Button 1

        // Siren 2
        pinHandler.add(1, new Relay(MBX_TIME_SIREN, Color.BLUE, debugPanel4Pins)); // Original Siren Button 2

        // Siren 3
        pinHandler.add(1, new Relay(MBX_RESPAWN_SIREN, Color.BLUE, debugPanel4Pins)); // Original Siren Button 6

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

    public static void setScheme(String name, String scheme) {
        pinHandler.setScheme(name, scheme);
    }

    public static void off(String name) {
        pinHandler.off(name);
    }

    public static void on(String name) {
        pinHandler.on(name);
    }

    public static HashMap<String, Sound> getTimeAnnouncements() {
        return timeAnnouncements;
    }

    public static MyAbstractButton getBtnUndo() {
        return btnUndo;
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

    public static MyAbstractButton getBtnMisc() {
        return btnMisc;
    }

    public static JButton getGUIBtnRed() {
        return frmTest.getBtnRed();
    }

    public static JButton getGUIBtnUndo() {
        return frmTest.getBtnUndo();
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

    public static JButton getGUIBtn2() {
        return frmTest.getBtn2();
    }


    public static void countdown(int countdown_index) {
        if (SOUND) countdown[countdown_index].play();
    }

    private static void initSound() {

        if (!SOUND) {
            startup_progress = 77;

            // dummy time annoucements for the sirens

            timeAnnouncements.put("05:00", null);
            timeAnnouncements.put("04:00", null);
            timeAnnouncements.put("03:00", null);
            timeAnnouncements.put("02:00", null);
            timeAnnouncements.put("01:00", null);
            timeAnnouncements.put("00:30", null);
            timeAnnouncements.put("00:20", null);
            timeAnnouncements.put("00:10", null);

            return;
        }

        startup_progress = 5;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);

        TinySound.init();


        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);
        soundMap.put("siren", TinySound.loadMusic(new File(Tools.getSoundPath() + File.separator + Tools.SND_SIREN)));

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);
        soundMap.put("welcome", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_WELCOME)));

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);
        soundMap.put("rocket", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_FLARE)));

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);
        soundMap.put("minions", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_MINIONS_SPAWNED)));

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);
        soundMap.put("victory", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_VICTORY)));

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);
        soundMap.put("defeat", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_DEFEAT)));

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);
        soundMap.put("shutdown", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_SHUTDOWN)));

        startup_progress = startup_progress + 6;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);
        soundMap.put("tranquility", TinySound.loadMusic(new File(Tools.getSoundPath() + File.separator + Tools.SND_TRANQUILITY)));

        for (int i = 0; i <= 10; i++) {
            startup_progress = startup_progress + 2;
            Tools.printProgBar(startup_progress);
            frmTest.setProgress(startup_progress);
            countdown[i] = TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.COUNTDOWN[i]));
        }

        winnerSongs.add(Tools.getSoundPath() + File.separator + Tools.SND_QUEEN);
        winnerSongs.add(Tools.getSoundPath() + File.separator + Tools.SND_MIB);
        winnerSongs.add(Tools.getSoundPath() + File.separator + Tools.SND_EVERYBODY_DANCE_NOW);

        looserSongs.add(Tools.getSoundPath() + File.separator + Tools.SND_SKYFALL);
        looserSongs.add(Tools.getSoundPath() + File.separator + Tools.SND_LOSER);
        looserSongs.add(Tools.getSoundPath() + File.separator + Tools.SND_WHO_WANTS_TO_LIVE_FOREVER);
        looserSongs.add(Tools.getSoundPath() + File.separator + Tools.SND_BE_HAPPY);


        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);
        timeAnnouncements.put("20:00", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_20_MINUTES)));
        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);
        timeAnnouncements.put("10:00", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_10_MINUTES)));
        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);
        timeAnnouncements.put("05:00", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_5_MINUTES)));
        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);
        timeAnnouncements.put("04:00", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_4_MINUTES)));
        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);
        timeAnnouncements.put("03:00", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_3_MINUTES)));
        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);
        timeAnnouncements.put("02:00", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_2_MINUTES)));
        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);
        timeAnnouncements.put("01:00", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_1_MINUTE)));
        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);
        timeAnnouncements.put("00:30", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_30_SECONDS)));
        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);
        timeAnnouncements.put("00:20", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_20_SECONDS)));
        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        frmTest.setProgress(startup_progress);
        timeAnnouncements.put("00:10", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_10_SECONDS)));


    }


    public static void enableSettings(boolean enable) {
        frmTest.enableSettings(enable);
    }

    public static void playWinner() {
        if (!MUSIC) return;
        stopWinner();

        currentWinner++;
        if (currentWinner >= winnerSongs.size()) {
            currentWinner = 0;
        }


        SwingWorker<Music, Music> sw = new SwingWorker() {
            @Override
            protected Music doInBackground() throws Exception {
                return TinySound.loadMusic(new File(winnerSongs.get(currentWinner)));
            }

            @Override
            protected void done() {
                try {
                    winner = (Music) get();
                    if (winner != null) winner.play(false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };

        sw.execute();

    }

    public static void playLooser() {
        if (!MUSIC) return;
        stopLooser();

        currentLooser++;
        if (currentLooser >= looserSongs.size()) {
            currentLooser = 0;
        }

        SwingWorker<Music, Music> sw = new SwingWorker() {
            @Override
            protected Music doInBackground() throws Exception {
                return TinySound.loadMusic(new File(looserSongs.get(currentLooser)));
            }

            @Override
            protected void done() {
                try {
                    looser = (Music) get();
                    looser.play(false);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };

        sw.execute();

    }


    public static void playRandomSong(ArrayList<Music> list) {
        list.get(random_int(0, list.size() - 1)).play(false);
    }

    public static int random_int(int Min, int Max) {
        return (int) (Math.random() * (Max - Min)) + Min;
    }


    private static void stopWinner() {
        if (winner == null) return;

        if (!winner.done()) {
            winner.stop();
            winner.unload();
        }

        winner = null;
    }

    private static void stopLooser() {
        if (looser == null) return;

        if (!looser.done()) {
            looser.stop();
            looser.unload();
        }
        looser = null;
    }

    public static void stopAllSongs() {
        stopWinner();
        stopLooser();
    }

    public static void play(String key) {
        play(key, false);
    }

    public static void play(String key, boolean loop) {
        if (!SOUND) return;

        if (soundMap.get(key) instanceof Music) {
            ((Music) soundMap.get(key)).play(loop);
        } else {
            ((Sound) soundMap.get(key)).play();
        }
    }

    public static void stop(String key) {
        if (!SOUND) return;

        if (soundMap.get(key) instanceof Music) {
            ((Music) soundMap.get(key)).stop();
        } else {
            ((Sound) soundMap.get(key)).stop();
        }
    }

    private static void loadLocalProperties() throws IOException {
        config = new Properties();
        // some defaults

        config.put(FCY_TIME2CAPTURE, "20");
        config.put(FCY_GAMETIME, "5");
        config.put(FCY_SOUND, "true");
        config.put(FCY_MUSIC, "true");
        config.put(FCY_RESPAWN_SIGNAL, "true");
        config.put(FCY_SIREN, "true");
        config.put(MBX_SIREN_TIME, "750");
        config.put(FCY_RESPAWN, "40");
        config.put(MBX_LOGLEVEL, "debug");
        config.put(MBX_I2C_1, "0x20");
        config.put(MBX_I2C_2, "0x24");
        config.put(MBX_SIREN1, "mcp23017-01-B3");
        config.put(MBX_SIREN2, "mcp23017-01-B4");
        config.put(MBX_SIREN3, "mcp23017-01-B5");
        config.put(MBX_SHUTDOWN_SIREN, "mcp23017-01-B1");
        config.put(MBX_TIME_SIREN, "mcp23017-01-B2");
        config.put(MBX_RESPAWN_SIREN, "mcp23017-01-B6");
        config.put(MBX_LED_GREEN, "mcp23017-01-A1");
        config.put(MBX_LED_RED, "mcp23017-01-A0");
        config.put(MBX_LED_PB_GREEN, "mcp23017-01-A4");
        config.put(MBX_LED_PB_YELLOW, "mcp23017-01-A3");
        config.put(MBX_LED_PB_RED, "mcp23017-01-A2");
        config.put(MBX_LED_RGB_BLUE, "mcp23017-01-A7");
        config.put(MBX_LED_RGB_RED, "mcp23017-01-A6");
        config.put(MBX_LED_RGB_GREEN, "mcp23017-01-A5");

        config.put(MBX_BTN_RED, "mcp23017-02-B0");
        config.put(MBX_BTN_GREEN, "mcp23017-02-B1");
        config.put(MBX_BTN_START_STOP, "mcp23017-02-B2");
        config.put(MBX_BTN_QUIT, "mcp23017-02-B3");
        config.put(MBX_BTN_UNDO, "mcp23017-02-B4");

        File configFile = new File(Tools.getMissionboxDirectory() + File.separator + "config.txt");

        configFile.getParentFile().mkdirs();

        configFile.createNewFile();

        FileInputStream in = new FileInputStream(configFile);
        Properties p = new Properties();
        p.load(in);
        config.putAll(p);
        p.clear();
        in.close();

        SOUND = Boolean.parseBoolean(MissionBox.getConfig().getProperty(MissionBox.FCY_SOUND));
        SIREN = Boolean.parseBoolean(MissionBox.getConfig().getProperty(MissionBox.FCY_SIREN));
        MUSIC = Boolean.parseBoolean(MissionBox.getConfig().getProperty(MissionBox.FCY_MUSIC));
        RESPAWN = Boolean.parseBoolean(MissionBox.getConfig().getProperty(MissionBox.FCY_RESPAWN_SIGNAL));

        logLevel = Level.toLevel(MissionBox.getConfig().getProperty(MissionBox.MBX_LOGLEVEL), Level.DEBUG);

    }

    public static void saveLocalProps() {

        try {
            File configFile = new File(Tools.getMissionboxDirectory() + File.separator + "config.txt");
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


    public static void clearLog() {
        frmTest.log(null);
    }

    public static void log(String text) {
        frmTest.log(text);
    }

    public static void setMessage(String message) {
        frmTest.setMessage(message);
    }

    public static int getGamemode() {
        return gamemode;
    }

    public static void setGamemode(int gamemode) {
        MissionBox.gamemode = gamemode;
    }

    public static void setProgress(BigDecimal percent) {
        // if (SIREN)

        if (relaisSirens != null) relaisSirens.setValue(percent);
        frmTest.setProgress(percent.intValue());
        if (relaisLEDs != null) relaisLEDs.setValue(percent);
        if (relaisFlagpole != null) relaisFlagpole.setValue(percent);
    }

    public static boolean isSOUND() {
        return SOUND;
    }

    public static boolean isMUSIC() {
        return MUSIC;
    }

    public static boolean isRESPAWN() {
        return RESPAWN;
    }

    public static boolean isSIREN() {
        return SIREN;
    }

    public static void setTimerMessage(String message) {
        frmTest.setTimer(message);
        logger.debug(message);
    }

    public static void minuteSignal(int minutes) {
        setScheme("timeSignal", minutes + ";1000,1000");
    }

    public static void secondsSignal(int seconds) {
        setScheme("timeSignal", seconds + ";500,500");
    }

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
            MCP23017GpioProvider gpioProvider0 = new MCP23017GpioProvider(I2CBus.BUS_1, Integer.decode(getConfig().getProperty(MissionBox.MBX_I2C_1)));
            MCP23017GpioProvider gpioProvider1 = new MCP23017GpioProvider(I2CBus.BUS_1, Integer.decode(getConfig().getProperty(MissionBox.MBX_I2C_2)));

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


            ioRed = inputMap.get(getConfig().getProperty(MBX_BTN_RED));
            ioGreen = inputMap.get(getConfig().getProperty(MBX_BTN_GREEN));
            ioGameStartStop = inputMap.get((getConfig().getProperty(MBX_BTN_START_STOP)));
            ioMisc = inputMap.get((getConfig().getProperty(MBX_BTN_QUIT)));
            ioUndo = inputMap.get((getConfig().getProperty(MBX_BTN_UNDO)));
        }

    }


    public static void shutdownEverything() {
//        synchronized (mapWorker) {
//            Set<String> keys = mapWorker.keySet();
//            for (String key : keys) {
//                logger.debug("cleaning key: "+key);
//                MissionBox.blink(key, 0);
//            }
//        }

    }


}
