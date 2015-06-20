package main;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import gamemodes.Farcry1Assault;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import misc.ConfigXML;
import misc.Tools;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.util.HashMap;


/**
 * Created by tloehr on 22.04.15.
 */
public class MissionBox {
    public static GpioController getGPIO() {
        return GPIO;
    }

    private static final Logger logger = Logger.getRootLogger();
    public static Level logLevel = Level.DEBUG;
    private static GpioController GPIO;

    private static ConfigXML configXML;


//    public static final SoundThread soundPool =  new SoundThread();

    public static final void main(String[] args) throws Exception {


        configXML = new ConfigXML();


        logLevel = Level.toLevel("DEBUG", Level.DEBUG);
        GPIO = Tools.isRaspberry() ? GpioFactory.getInstance() : null;

        PatternLayout layout = new PatternLayout("%d{ISO8601} %-5p [%t] %c: %m%n");
        ConsoleAppender consoleAppender = new ConsoleAppender(layout);
        logger.addAppender(consoleAppender);


        Farcry1Assault fc = new Farcry1Assault();


    }


}
