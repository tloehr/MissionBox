package misc;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.*;
import com.pi4j.io.i2c.I2CBus;
import gamemodes.Farcry1Assault;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import main.MissionBox;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by tloehr on 20.06.15.
 */
public class Config extends DefaultHandler {


    private final HashMap<String, Sound> soundMap = new HashMap<>();
    private final HashMap<String, Music> musicMap = new HashMap<>();
    private final HashMap<String, GpioPinDigital> gpioMap = new HashMap<>();


    private final Logger logger = Logger.getLogger(getClass());
    private PatternLayout patternLayout = new PatternLayout("%d{ISO8601} %-5p [%t] %c: %m%n");
    private Level logLevel = Level.DEBUG;

    private GpioController GPIO;
    private MCP23017GpioProvider gpioProvider;

    private String currentGameMode = null, soundpath = "", homedir, sep;
    private int i2CBus = -1;


    private ConfigFC1 configFC1;
    private final HashMap<String, Properties> gameparameters;

    public Config() {
        TinySound.init();

        homedir = System.getProperty("user.home");
        sep = System.getProperty("file.separator");
        GPIO = Tools.isRaspberry() ? GpioFactory.getInstance() : null;
        configFC1 = new ConfigFC1();
        gameparameters = new HashMap<>();

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            File configFile = new File(homedir + sep + "missionbox" + sep + "missionbox.xml");


            saxParser.parse(configFile, this);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
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


        logger.debug(tagName);


        try {


            if (tagName.equalsIgnoreCase("mainconfigs")) {
                logLevel = Level.toLevel(attributes.getValue("loglevel"));
                patternLayout = new PatternLayout(attributes.getValue("patternlayout"));
            } else if (tagName.equalsIgnoreCase("soundfile")) {
                soundMap.put(attributes.getValue("id"), TinySound.loadSound(new File(soundpath + sep + attributes.getValue("filename"))));
            } else if (tagName.equalsIgnoreCase("musicfile")) {
                musicMap.put(attributes.getValue("id"), TinySound.loadMusic(new File(soundpath + sep + attributes.getValue("filename"))));
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

            } else if (tagName.equalsIgnoreCase("soundfiles")) {
                soundpath = attributes.getValue("soundpath");
            } else if (tagName.equalsIgnoreCase("physical") && GPIO != null) {

// <provisionDigitalPin providerPin="b5" state="low" direction="output"/>

                if (tagName.equalsIgnoreCase("I2CBus")) {
                    i2CBus = attributes.getValue("loglevel").equalsIgnoreCase("0") ? I2CBus.BUS_0 : I2CBus.BUS_1;
                } else if (tagName.equalsIgnoreCase("GPIOProvider")) {
                    if (attributes.getValue("type").equalsIgnoreCase("mcp23017")) {
                        gpioProvider = new MCP23017GpioProvider(i2CBus, Integer.parseInt(attributes.getValue("address").toString(), 16));
                    } else if (tagName.equalsIgnoreCase("GPIOProvider")) {

                        if (attributes.getValue("direction").equalsIgnoreCase("output")) {
                            gpioMap.put("mcp23017-01-A0", GPIO.provisionDigitalOutputPin(gpioProvider, getMCP23017Pin(attributes.getValue("providerPin")), "", PinState.valueOf(attributes.getValue("state"))));
                        } else {
                            gpioMap.put(gpioProvider.getName(), GPIO.provisionDigitalInputPin(gpioProvider, getMCP23017Pin(attributes.getValue("providerPin")), "mcp23017-01-A0", PinPullResistance.valueOf(attributes.getValue("state"))));
                        }


                    }

                    //        final GpioPinDigitalInput btnFlagTrigger = MissionBox.getConfig().getGPIO().provisionDigitalInputPin(RaspiPin.GPIO_03, "FlagTrigger", PinPullResistance.PULL_DOWN);
//        final GpioPinDigitalInput btnGameStartStop = MissionBox.getConfig().getGPIO().provisionDigitalInputPin(RaspiPin.GPIO_02, "GameStartStop", PinPullResistance.PULL_DOWN);
//        final GpioPinDigitalInput btnMisc = MissionBox.getConfig().getGPIO().provisionDigitalInputPin(RaspiPin.GPIO_00, "MISC", PinPullResistance.PULL_DOWN);
//


                }

            }
        } catch (IOException io) {
            throw new SAXException(io);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("I2CBus")) {
            gpioProvider = null;
        }
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public PatternLayout getPatternLayout() {
        return patternLayout;
    }


    public Pin getMCP23017Pin(String name) {
        if (gpioProvider == null) return null;

        Pin pin = null;

        if (name.equalsIgnoreCase("a0")) pin = MCP23017Pin.GPIO_A0;
        else if (name.equalsIgnoreCase("a1")) pin = MCP23017Pin.GPIO_A1;
        else if (name.equalsIgnoreCase("a2")) pin = MCP23017Pin.GPIO_A2;
        else if (name.equalsIgnoreCase("a3")) pin = MCP23017Pin.GPIO_A3;
        else if (name.equalsIgnoreCase("a4")) pin = MCP23017Pin.GPIO_A4;
        else if (name.equalsIgnoreCase("a5")) pin = MCP23017Pin.GPIO_A5;
        else if (name.equalsIgnoreCase("a6")) pin = MCP23017Pin.GPIO_A6;
        else if (name.equalsIgnoreCase("a7")) pin = MCP23017Pin.GPIO_A7;
        else if (name.equalsIgnoreCase("b0")) pin = MCP23017Pin.GPIO_B0;
        else if (name.equalsIgnoreCase("b1")) pin = MCP23017Pin.GPIO_B1;
        else if (name.equalsIgnoreCase("b2")) pin = MCP23017Pin.GPIO_B2;
        else if (name.equalsIgnoreCase("b3")) pin = MCP23017Pin.GPIO_B3;
        else if (name.equalsIgnoreCase("b4")) pin = MCP23017Pin.GPIO_B4;
        else if (name.equalsIgnoreCase("b5")) pin = MCP23017Pin.GPIO_B5;
        else if (name.equalsIgnoreCase("b6")) pin = MCP23017Pin.GPIO_B6;
        else if (name.equalsIgnoreCase("b7")) pin = MCP23017Pin.GPIO_B7;

        return pin;
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
