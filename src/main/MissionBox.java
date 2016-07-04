package main;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.*;
import com.pi4j.io.i2c.I2CBus;
import gamemodes.Farcry1Assault;
import interfaces.MyAbstractButton;
import interfaces.Relay;
import interfaces.RelaySiren;
import interfaces.RelaySirenPulse;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import misc.Tools;
import org.apache.log4j.*;
import org.apache.log4j.or.ThreadGroupRenderer;

import javax.swing.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

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
    private static Properties config;
    private static int gamemode;

    private static final HashMap<String, GpioPinDigitalOutput> mapGPIO = new HashMap<>();
    private static final HashMap<String, SwingWorker> mapWorker = new HashMap<>();

    private static MyAbstractButton btnRed, btnGreen, btnGameStartStop, btnMisc;
    private static RelaySiren relaisLEDs;
    private static RelaySirenPulse relaisSirenProgress;

    public static Properties appinfo = new Properties();

    public static final String FCY_TIME2CAPTURE = "fcy.time2capture";
    public static final String FCY_GAMETIME = "fcy.gametime";
    public static final String FCY_SOUND = "fcy.sound";
    public static final String FCY_SIREN = "fcy.siren";
    public static final String MBX_SIREN_TIME = "mbx.siren.time"; // in ms
    public static final String MBX_GUI = "mbx.gui";
    public static final String MBX_LOGLEVEL = "mbx.loglevel";
    public static final String FCY_RESPAWN = "fcy.respawn";

    public static boolean GUI = false;
    private static boolean SOUND = false;
    private static boolean SIREN = false;

    private static HashMap<String, Object> soundMap = new HashMap<>();
    private static HashMap<String, GpioPinDigitalOutput> outputMap = new HashMap<>();
    private static HashMap<String, GpioPinDigitalInput> inputMap = new HashMap<>();
    private static HashMap<String, Relay> relayMap = new HashMap<>();

    private static ArrayList<Music> winnerSongs = new ArrayList<>();
    private static ArrayList<Music> looserSongs = new ArrayList<>();
    private static HashMap<String, Sound> timeAnnouncements = new HashMap();
    private static Sound[] countdown = new Sound[11];

    public static final void main(String[] args) throws Exception {

        PatternLayout layout = new PatternLayout("%d{ISO8601} %-5p [%t] %c: %m%n");
        logger.addAppender(new ConsoleAppender(layout));
        logger.addAppender(new FileAppender(layout, Tools.getMissionboxDirectory() + File.separator + "missionbox.log"));

//        try {
//            // Lade Build Informationen   2
//            InputStream in2 = null;
//            //Class clazz = getClass();
//            in2 = getResourceAsStream("/appinfo.properties");
//            appinfo.load(in2);
//            in2.close();
//        } catch (IOException iOException) {
//            iOException.printStackTrace();
//        }

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

        initSound();
        hwinit();

        startup_progress = startup_progress + 10;
        Tools.printProgBar(startup_progress);

        if (GUI) {
            frmTest = new FrmTest();
            frmTest.pack();
            frmTest.setVisible(true);
        }

        startup_progress = startup_progress + 10;
        Tools.printProgBar(startup_progress);

        btnRed = new MyAbstractButton(inputMap.get("btnRed"), getGUIBtnRed());
        btnGreen = new MyAbstractButton(inputMap.get("btnGreen"), getGUIBtnGreen());
        btnGameStartStop = new MyAbstractButton(inputMap.get("btnGameStartStop"), getGUIBtn1());
        btnMisc = new MyAbstractButton(inputMap.get("btnMisc"), getGUIBtn2());

        startup_progress = 100;
        Tools.printProgBar(startup_progress);

        Farcry1Assault fc = new Farcry1Assault();
    }

    public static HashMap<String, Sound> getTimeAnnouncements() {
        return timeAnnouncements;
    }

    public static MyAbstractButton getBtnRed() {
        return btnRed;
    }

    public static boolean isGameStartable() {
        return GUI ? frmTest.isGameStartable() : true;
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
        return GUI ? frmTest.getBtnRed() : null;
    }

    public static Level getLogLevel() {
        return logLevel;
    }

    public static JButton getGUIBtnGreen() {
        return GUI ? frmTest.getBtnGreen() : null;
    }

    public static JButton getGUIBtn1() {
        return GUI ? frmTest.getBtn1() : null;
    }

    public static JButton getGUIBtn2() {
        return GUI ? frmTest.getBtn2() : null;
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

        TinySound.init();


        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        soundMap.put("siren", TinySound.loadMusic(new File(Tools.getSoundPath() + File.separator + Tools.SND_SIREN)));

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        soundMap.put("welcome", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_WELCOME)));

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        soundMap.put("rocket", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_FLARE)));

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        soundMap.put("gameover", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_GAME_OVER)));

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        soundMap.put("minions", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_MINIONS_SPAWNED)));

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        soundMap.put("victory", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_VICTORY)));

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        soundMap.put("defeat", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_DEFEAT)));

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        soundMap.put("shutdown", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_SHUTDOWN)));

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        soundMap.put("beep1", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_BEEP)));


        /*

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        looserSongs.add(TinySound.loadMusic(new File(Tools.getSoundPath() + File.separator + Tools.SND_LOSER)));

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        looserSongs.add(TinySound.loadMusic(new File(Tools.getSoundPath() + File.separator + Tools.SND_WHO_WANTS_TO_LIVE_FOREVER)));

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        looserSongs.add(TinySound.loadMusic(new File(Tools.getSoundPath() + File.separator + Tools.SND_SKYFALL)));

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        winnerSongs.add(TinySound.loadMusic(new File(Tools.getSoundPath() + File.separator + Tools.SND_MIB)));

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        winnerSongs.add(TinySound.loadMusic(new File(Tools.getSoundPath() + File.separator + Tools.SND_QUEEN)));

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        winnerSongs.add(TinySound.loadMusic(new File(Tools.getSoundPath() + File.separator + Tools.SND_EVERYBODY_DANCE_NOW)));
        */
        for (int i = 0; i <= 10; i++) {
            startup_progress = startup_progress + 2;
            Tools.printProgBar(startup_progress);
            countdown[i] = TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.COUNTDOWN[i]));
        }

        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        timeAnnouncements.put("20:00", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_20_MINUTES)));
        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        timeAnnouncements.put("10:00", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_10_MINUTES)));
        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        timeAnnouncements.put("05:00", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_5_MINUTES)));
        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        timeAnnouncements.put("04:00", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_4_MINUTES)));
        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        timeAnnouncements.put("03:00", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_3_MINUTES)));
        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        timeAnnouncements.put("02:00", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_2_MINUTES)));
        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        timeAnnouncements.put("01:00", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_1_MINUTE)));
        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        timeAnnouncements.put("00:30", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_30_SECONDS)));
        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        timeAnnouncements.put("00:20", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_20_SECONDS)));
        startup_progress = startup_progress + 2;
        Tools.printProgBar(startup_progress);
        timeAnnouncements.put("00:10", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_10_SECONDS)));


    }


    public static void blink(final String key, final long duration, final int repeat) {
        if (!outputMap.containsKey(key)) return;

        synchronized (mapWorker) {
            logger.debug("pin: " + key + "   duration: " + duration + "   repeat: " + repeat);
            if (mapWorker.containsKey(key)) {
                SwingWorker mySW = mapWorker.get(key);
                if (!mySW.isDone()){
                    mySW.cancel(true);
                }
                mapWorker.remove(key);
                outputMap.get(key).setState(PinState.LOW);
            }
            if (duration == 0) {
                logger.debug("switching off: " + key);
                outputMap.get(key).setState(PinState.LOW);
                return;
            }

            SwingWorker sw = new SwingWorker() {
                int i = 0;
                String uuid = UUID.randomUUID().toString();

                @Override
                protected Object doInBackground() throws Exception {
                    logger.debug(key + " ==> " + uuid + "  starting");
                    while (!isCancelled() && i < repeat) {
                        i++;
                        try {
                            outputMap.get(key).setState(PinState.HIGH);
                            Thread.sleep(duration);
                            outputMap.get(key).setState(PinState.LOW);
                            Thread.sleep(duration);
                        } catch (InterruptedException ex) {
                            logger.debug("blink() for '" + key + "' (" + uuid + " interrupted");
                        }
                    }
                    return null;
                }

                @Override
                protected void done() {
                    super.done();
                    logger.debug("blink() for '" + key + "' (" + uuid + " DONE !!!");
                    outputMap.get(key).setState(PinState.LOW);
                    mapWorker.remove(key);
                }
            };
            mapWorker.put(key, sw);
            sw.execute();
        }
    }

    public static void blink(String key, long duration) {
        blink(key, duration, Integer.MAX_VALUE);
    }

    public static HashMap<String, GpioPinDigitalOutput> getOutputMap() {
        return outputMap;
    }

    public static HashMap<String, GpioPinDigitalInput> getInputMap() {
        return inputMap;
    }

    public static HashMap<String, Relay> getRelayMap() {
        return relayMap;
    }

    public static void enableSettings(boolean enable) {
        if (GUI) frmTest.enableSettings(enable);
    }

    public static void playWinner() {
//        playRandomSong(winnerSongs);
    }

    public static void playLooser() {
//        playRandomSong(looserSongs);
    }


    public static void playRandomSong(ArrayList<Music> list) {
        list.get(random_int(0, list.size() - 1)).play(false);
    }

    public static int random_int(int Min, int Max) {
        return (int) (Math.random() * (Max - Min)) + Min;
    }

    public static void stopAllSongs() {
        for (Music song : winnerSongs) {
            song.stop();
        }
        for (Music song : looserSongs) {
            song.stop();
        }
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
        config.put(FCY_SIREN, "false");
        config.put(MBX_SIREN_TIME, "750");
        config.put(FCY_RESPAWN, "40");
        config.put(MBX_GUI, "true");
        config.put(MBX_LOGLEVEL, "debug");


        File configFile = new File(Tools.getMissionboxDirectory() + File.separator + "config.txt");

        configFile.getParentFile().mkdirs();

        configFile.createNewFile();

        FileInputStream in = new FileInputStream(configFile);
        Properties p = new Properties();
        p.load(in);
        config.putAll(p);
        p.clear();
        in.close();

        GUI = Boolean.parseBoolean(config.getProperty(MissionBox.MBX_GUI));
        SOUND = Boolean.parseBoolean(MissionBox.getConfig().getProperty(MissionBox.FCY_SOUND));
        SIREN = Boolean.parseBoolean(MissionBox.getConfig().getProperty(MissionBox.FCY_SIREN));
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

    public static void setMessage(String message) {
        if (GUI) frmTest.setMessage(message);
    }

    public static int getGamemode() {
        return gamemode;
    }

    public static void setGamemode(int gamemode) {
        MissionBox.gamemode = gamemode;
    }

    public static void setProgress(BigDecimal percent) {
        if (SIREN) relaisSirenProgress.setValue(percent);
        if (GUI) frmTest.setProgress(percent.intValue());
        if (SIREN) relaisLEDs.setValue(new BigDecimal(100).subtract(percent));
    }

    public static boolean isSOUND() {
        return SOUND;
    }

    public static boolean isSIREN() {
        return SIREN;
    }

    public static void setTimerMessage(String message) {
        if (GUI) frmTest.setTimer(message);
    }

    public static void minuteSignal(int minutes) {
        blink("timeSignal", 1000, minutes);
    }

    public static void secondsSignal(int seconds) {
        blink("timeSignal", 500, seconds);
    }

    public static void setRespawnTimer(String message) {
        if (GUI) frmTest.setRespawnTimer(message);
    }

    private static void hwinit() throws IOException {

        GPIO = null;
        if (Tools.isArm()) {
            try {
                GPIO = GpioFactory.getInstance();

            } catch (Exception e) {
            }


            // this map provides an easier access to the gpioProvider0
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

            GpioPinDigitalInput ioRed = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_00, "RedTrigger", PinPullResistance.PULL_DOWN); // Board 11
            GpioPinDigitalInput ioGreen = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_02, "GreenTrigger", PinPullResistance.PULL_DOWN); // Board 13
            GpioPinDigitalInput ioGameStartStop = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_03, "GameStartStop", PinPullResistance.PULL_DOWN); // Board 15
            GpioPinDigitalInput ioMisc = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_21, "MISC", PinPullResistance.PULL_DOWN); // Board 29

            inputMap.put("btnRed", ioRed);
            inputMap.put("btnGreen", ioGreen);
            inputMap.put("btnGameStartStop", ioGameStartStop);
            inputMap.put("btnMisc", ioMisc);

            GpioPinDigitalOutput ioLedGreen = mapGPIO.get("mcp23017-01-A0");
            GpioPinDigitalOutput ioLedRed = mapGPIO.get("mcp23017-01-A1");
            GpioPinDigitalOutput ioLedBarGreen = mapGPIO.get("mcp23017-01-A2");
            GpioPinDigitalOutput ioLedBarYellow = mapGPIO.get("mcp23017-01-A3");
            GpioPinDigitalOutput ioLedBarRed = mapGPIO.get("mcp23017-01-A4");

            outputMap.put("ledGreen", ioLedGreen);
            outputMap.put("ledRed", ioLedRed);
            outputMap.put("ledBarGreen", ioLedBarGreen);
            outputMap.put("ledBarYellow", ioLedBarYellow);
            outputMap.put("ledBarRed", ioLedBarRed);

//            outputMap.put("flagSiren", mapGPIO.get("mcp23017-01-B2"));
            outputMap.put("shutdownSiren", mapGPIO.get("mcp23017-01-B1"));
            outputMap.put("respawnSiren", mapGPIO.get("mcp23017-01-B6"));
            outputMap.put("timeSignal", mapGPIO.get("mcp23017-01-B3"));

            outputMap.put("siren1/3", mapGPIO.get("mcp23017-01-B2"));
            outputMap.put("siren2/3", mapGPIO.get("mcp23017-01-B0"));
            outputMap.put("siren3/3", mapGPIO.get("mcp23017-01-B4"));

            ArrayList relaisKeys = new ArrayList<String>();
            relaisKeys.add("siren1/3");
            relaisKeys.add("siren2/3");
            relaisKeys.add("siren3/3");
            relaisSirenProgress = new RelaySirenPulse(relaisKeys);

//            Relay ledGreen = new Relay(ioLedGreen);
//            Relay ledRed = new Relay(ioLedRed);
            Relay ledBarGreen = new Relay(ioLedBarGreen);
            Relay ledBarYellow = new Relay(ioLedBarYellow);
            Relay ledBarRed = new Relay(ioLedBarRed);

            ArrayList<Relay> progressLEDs = new ArrayList<>();
            progressLEDs.add(ledBarGreen);
            progressLEDs.add(ledBarYellow);
            progressLEDs.add(ledBarRed);

            relaisLEDs = new RelaySiren(progressLEDs);
//            relaisSirens = new RelaySirenPulse(relayBoard, 500);

//            respawnSiren = new Relay(mapGPIO.get("mcp23017-01-B1"));

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
