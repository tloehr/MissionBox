package main;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.*;
import com.pi4j.io.i2c.I2CBus;
import gamemodes.Farcry1Assault;
import interfaces.MyAbstractButton;
import interfaces.Relay;
import interfaces.RelaySiren;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import misc.Tools;
import org.apache.log4j.*;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;


/**
 * Created by tloehr on 22.04.15.
 */
public class MissionBox {

    private static final Logger logger = Logger.getRootLogger();
    private static Level logLevel = Level.DEBUG;
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

    private static MyAbstractButton btnRed, btnGreen, btnGameStartStop, btnMisc;

    private static RelaySiren relaisSirens, relaisLEDs;

    public static final String FCY_TIME2CAPTURE = "fcy.time2capture";
    public static final String FCY_GAMETIME = "fcy.gametime";
    public static final String FCY_SOUND = "fcy.sound";
    public static final String FCY_SIREN = "fcy.siren";
    public static final String MBX_GUI = "mbx.gui";
    public static final String MBX_LOGLEVEL = "mbx.loglevel";

    public static boolean GUI = false;
    private static boolean SOUND = false;
    private static boolean SIREN = false;

    private static HashMap<String, Object> soundMap = new HashMap<>();
    private static ArrayList<Music> winnerSongs = new ArrayList<>();
    private static ArrayList<Music> looserSongs = new ArrayList<>();

    private static Sound[] countdown = new Sound[11];


    public static final void main(String[] args) throws Exception {
        loadLocalProperties();

        initSound();
        hwinit();

//        try {
//            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            // If Nimbus is not available, you can set the GUI to another look and feel.
//        }

        PatternLayout layout = new PatternLayout("%d{ISO8601} %-5p [%t] %c: %m%n");
        logger.addAppender(new FileAppender(layout, Tools.getMissionboxDirectory() + File.separator + "missionbox.log"));

        if (GUI) {
            frmTest = new FrmTest();
            frmTest.pack();
            frmTest.setVisible(true);
        }


        btnRed = new MyAbstractButton(getIoRed(), getGUIBtnRed());
        btnGreen = new MyAbstractButton(getIoGreen(), getGUIBtnGreen());
        btnGameStartStop = new MyAbstractButton(getIoGameStartStop(), getGUIBtn1());
        btnMisc = new MyAbstractButton(getIoMisc(), getGUIBtn2());

        Farcry1Assault fc = new Farcry1Assault();
    }

    public static MyAbstractButton getBtnRed() {
        return btnRed;
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

        if (!SOUND) return;

        TinySound.init();


        soundMap.put("siren", TinySound.loadMusic(new File(Tools.getSoundPath() + File.separator + Tools.SND_SIREN)));
        soundMap.put("welcome", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_WELCOME)));
        soundMap.put("rocket", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_FLARE)));
        soundMap.put("gameover", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_GAME_OVER)));
        soundMap.put("minions", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_MINIONS_SPAWNED)));
        soundMap.put("victory", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_VICTORY)));
        soundMap.put("defeat", TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.SND_DEFEAT)));

        looserSongs.add(TinySound.loadMusic(new File(Tools.getSoundPath() + File.separator + Tools.SND_LOSER)));
        winnerSongs.add(TinySound.loadMusic(new File(Tools.getSoundPath() + File.separator + Tools.SND_MIB)));
        winnerSongs.add(TinySound.loadMusic(new File(Tools.getSoundPath() + File.separator + Tools.SND_QUEEN)));

        for (int i = 0; i <= 10; i++) {
            countdown[i] = TinySound.loadSound(new File(Tools.getSoundPath() + File.separator + Tools.COUNTDOWN[i]));
        }


    }


    public static void enableSettings(boolean enable) {
        if (GUI) frmTest.enableSettings(enable);
    }

    public static void playWinner() {
        playRandomSong(winnerSongs);
    }

    public static void playLooser() {
        playRandomSong(looserSongs);
    }


    public static void playRandomSong(ArrayList<Music> list) {
        Random random = new Random();
        random.nextInt();
        int randomNumber = random.nextInt(winnerSongs.size());
        list.get(randomNumber).play(false);
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
        config.put(MBX_GUI, "true");
        config.put(MBX_LOGLEVEL, "debug");

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

        GUI = Boolean.parseBoolean(config.getProperty(MissionBox.MBX_GUI));
        SOUND = Boolean.parseBoolean(MissionBox.getConfig().getProperty(MissionBox.FCY_SOUND));
        SIREN = Boolean.parseBoolean(MissionBox.getConfig().getProperty(MissionBox.FCY_SIREN));
        logLevel = Level.toLevel(MissionBox.getConfig().getProperty(MissionBox.MBX_LOGLEVEL), Level.DEBUG);

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

    public static GpioPinDigitalInput getIoRed() {
        return ioRed;
    }

    public static void setMessage(String message) {
        if (GUI) frmTest.setMessage(message);
    }

    public static void setProgress(int p) {
        if (GUI) frmTest.setProgress(p);
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

    public static GpioPinDigitalInput getIoGreen() {
        return ioGreen;
    }

    public static GpioPinDigitalInput getIoGameStartStop() {
        return ioGameStartStop;
    }

    public static GpioPinDigitalInput getIoMisc() {
        return ioMisc;
    }

    public static GpioPinDigitalOutput getIoLedGreen() {
        return ioLedGreen;
    }

    public static GpioPinDigitalOutput getIoLedRed() {
        return ioLedRed;
    }

    public static GpioPinDigitalOutput getIoLedBarGreen() {
        return ioLedBarGreen;
    }

    public static GpioPinDigitalOutput getIoLedBarYellow() {
        return ioLedBarYellow;
    }

    public static GpioPinDigitalOutput getIoLedBarRed() {
        return ioLedBarRed;
    }

    public static RelaySiren getRelaisLEDs() {
        return relaisLEDs;
    }

    public static void setRelaySirenPercentage(BigDecimal percent) {
        if (!SIREN) return;
        MissionBox.getRelaisSirens().setValue(percent);
    }

    private static void hwinit() throws IOException {

        GPIO = null;
        if (Tools.isArm()) {
            try {
                GPIO = GpioFactory.getInstance();

            } catch (Exception e) {
            }


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


            ledGreen = new Relay(ioLedGreen);
            ledRed = new Relay(ioLedRed);
            ledBarGreen = new Relay(ioLedBarGreen);
            ledBarYellow = new Relay(ioLedBarYellow);
            ledBarRed = new Relay(ioLedBarRed);

            progressLEDs.add(ledBarGreen);
            progressLEDs.add(ledBarYellow);
            progressLEDs.add(ledBarRed);

            relaisLEDs = new RelaySiren(MissionBox.getProgressLEDs());
            relaisSirens = new RelaySiren(MissionBox.getRelayBoard());
        }

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
