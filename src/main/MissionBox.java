package main;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import gamemodes.Farcry1Assault;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import misc.Tools;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.File;


/**
 * Created by tloehr on 22.04.15.
 */
public class MissionBox {


    private static final Logger logger = Logger.getRootLogger();
    public static Level logLevel = Level.DEBUG;
    private static GpioController GPIO;


//    public static final SoundThread soundPool =  new SoundThread();

    public static final void main(String[] args) throws Exception {

        logLevel = Level.toLevel("DEBUG", Level.DEBUG);
        GPIO = GpioFactory.getInstance();





        PatternLayout layout = new PatternLayout("%d{ISO8601} %-5p [%t] %c: %m%n");
        ConsoleAppender consoleAppender = new ConsoleAppender(layout);
        logger.addAppender(consoleAppender);


        Farcry1Assault fc = new Farcry1Assault(GPIO);


    }





}
