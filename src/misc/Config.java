package misc;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import gamemodes.Farcry1Assault;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import sun.rmi.runtime.Log;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by tloehr on 20.06.15.
 */
public class Config extends DefaultHandler {


    private final HashMap<String, Sound> soundMap = new HashMap<>();
    private final HashMap<String, Music> musicMap = new HashMap<>();
    private final HashMap<String, GpioPinDigitalOutput> gpioMap = new HashMap<>();

    private final HashMap<String, Properties> gameparameters = new HashMap<>();

    private final Logger logger = Logger.getLogger(getClass());
    private PatternLayout patternLayout = new PatternLayout("%d{ISO8601} %-5p [%t] %c: %m%n");
    private Level logLevel = Level.DEBUG;

    private GpioController GPIO;

    private String currentGameMode = null;

    private ConfigFC1 configFC1;

    public Config() throws Exception {

        GPIO = Tools.isRaspberry() ? GpioFactory.getInstance() : null;
        configFC1 = new ConfigFC1();

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            File configFile = new File("/Users/tloehr/Dropbox/develop/MissionBox/missionbox.xml");
            saxParser.parse(configFile, this);
        } catch (Exception e) {
            logger.fatal(e);
            System.exit(0);
        }

    }

    public GpioController getGPIO() {
        return GPIO;
    }

    public ConfigFC1 getConfigFC1() {
        return configFC1;
    }

    @Override
    public void startElement(String nsURI, String strippedName, String tagName, Attributes attributes) throws SAXException {

        if (tagName.equalsIgnoreCase("soundfile")) {
            soundMap.put(attributes.getValue("id"), TinySound.loadSound(new File(attributes.getValue("filename"))));
        } else if (tagName.equalsIgnoreCase("musicfile")) {
            musicMap.put(attributes.getValue("id"), TinySound.loadMusic(new File(attributes.getValue("filename"))));
        } else if (tagName.equalsIgnoreCase("mainconfigs")) {
            logLevel = Level.toLevel(attributes.getValue("loglevel"));
            patternLayout = new PatternLayout(attributes.getValue("patternlayout"));
        } else if (tagName.equalsIgnoreCase("game")) {
            currentGameMode = attributes.getValue("name");
            gameparameters.put(currentGameMode, new Properties());
            gameparameters.get(currentGameMode).setProperty(currentGameMode, attributes.getValue("label"));
        } else if (tagName.equalsIgnoreCase("play")) {
            String type = attributes.getValue("type");
            String file = attributes.getValue("file");

            if (type.equalsIgnoreCase(Farcry1Assault.SND_TYPE_SIREN)) {
                configFC1.setPlaySiren(musicMap.get(file));
            } else if (type.equalsIgnoreCase(Farcry1Assault.SND_TYPE_ROCKET)) {
                configFC1.setPlayRocket(soundMap.get(file));
            } else if (type.equalsIgnoreCase(Farcry1Assault.SND_TYPE_WINNING)) {
                configFC1.setPlayWinningSon(musicMap.get(file));
            } else if (type.equalsIgnoreCase(Farcry1Assault.SND_TYPE_WELCOME)) {
                configFC1.setPlayWelcome(soundMap.get(file));
            }

            gameparameters.get(currentGameMode).setProperty(currentGameMode, attributes.getValue("label"));
        }
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public PatternLayout getPatternLayout() {
        return patternLayout;
    }

    //    @Override
//    public void endElement(String uri, String localName, String qName) throws SAXException {
//        if (qName.equalsIgnoreCase("icd")) {
//            listICDs.add(new ICD(code, content.toString()));
//            code = null;
//            content = null;
//        }
//    }

}
