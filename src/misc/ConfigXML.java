package misc;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
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

/**
 * Created by tloehr on 20.06.15.
 */
public class ConfigXML extends DefaultHandler {

    private String firstElement;
    private StringBuilder content;
    String code = null;

    private final HashMap<String, Sound> soundMap = new HashMap<>();
    private final HashMap<String, Music> musicMap = new HashMap<>();
    private final HashMap<String, GpioPinDigitalOutput> gpioMap = new HashMap<>();
    private final Logger logger = Logger.getLogger(getClass());
    private PatternLayout patternLayout = new PatternLayout("%d{ISO8601} %-5p [%t] %c: %m%n");
    private Level logLevel = Level.DEBUG;

    public ConfigXML() throws Exception {


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


    @Override
    public void startElement(String nsURI, String strippedName, String tagName, Attributes attributes) throws SAXException {

        if (firstElement == null) {
            firstElement = tagName;
            if (!firstElement.equalsIgnoreCase("missionbox")) throw new SAXException("not my kind of document");
        }

        if (tagName.equalsIgnoreCase("soundfile")) {
            soundMap.put(attributes.getValue("id"), TinySound.loadSound(new File(attributes.getValue("filename"))));
        }

        if (tagName.equalsIgnoreCase("musicfile")) {
            musicMap.put(attributes.getValue("id"), TinySound.loadMusic(new File(attributes.getValue("filename"))));
        }

        if (tagName.equalsIgnoreCase("mainconfigs")) {
            logLevel = Level.toLevel(attributes.getValue("loglevel"));
            patternLayout = new PatternLayout(attributes.getValue("patternlayout"));

        }

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
