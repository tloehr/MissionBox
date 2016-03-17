package main;

import com.pi4j.io.gpio.GpioController;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import gamemodes.Farcry1Assault;
import org.apache.log4j.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private static VoiceManager voiceManager;

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


//        String text = "10 minutes";
//
//


//        voiceManager = VoiceManager.getInstance();

//        voice.speak(text);


        PatternLayout layout = new PatternLayout("%d{ISO8601} %-5p [%t] %c: %m%n");
        logger.addAppender(new ConsoleAppender(layout));
        logger.addAppender(new FileAppender(layout, System.getenv("user.home") + File.separator + "missionbox" + File.separator + "missionbox.log"));

        frmTest = new FrmTest();
        frmTest.pack();
        frmTest.setVisible(true);

        Farcry1Assault fc = new Farcry1Assault(GPIO, frmTest);
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
        config.put(FCY_SOUND, "1");
        config.put(FCY_SIREN, "0");

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
}
