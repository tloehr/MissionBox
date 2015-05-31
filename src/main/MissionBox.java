package main;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.util.StringUtil;
import com.pi4j.wiringpi.Lcd;
import events.MessageEvent;
import events.MessageListener;
import gamemodes.Farcry1Assault;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.math.BigDecimal;


/**
 * Created by tloehr on 22.04.15.
 */
public class MissionBox {


    public static final int INFO_TIME = 2;
    public static int TIME2RESPAWN = 20, MAXCYLCES = 200, SECONDS2CAPTURE = 60*10;
    // a CYCLE takes 50 millis
    protected static Logger logger;
    public static Level logLevel = Level.DEBUG;
    public final static GpioController GPIO = GpioFactory.getInstance();
    private static int LCD_ROWS = 2;
    private static int LCD_COLUMNS = 16;
    private final static int LCD_BITS = 4;

    public static final void main(String[] args) throws Exception {

//        FrmMain frmMain = new FrmMain();
        logger = Logger.getRootLogger();
        PatternLayout layout = new PatternLayout("%d{ISO8601} %-5p [%t] %c: %m%n");
        ConsoleAppender consoleAppender = new ConsoleAppender(layout);
        logger.addAppender(consoleAppender);


        final GpioPinDigitalInput btnFlagTrigger = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_03, "FlagTrigger", PinPullResistance.PULL_DOWN);
        final GpioPinDigitalInput btnGameStartStop = GPIO.provisionDigitalInputPin(RaspiPin.GPIO_02, "GameStartStop", PinPullResistance.PULL_DOWN);



        // initialize LCD
        // the wiring is according to the examples from https://kofler.info/buecher/raspberry-pi/
        int lcdHandle = Lcd.lcdInit(LCD_ROWS,     // number of row supported by LCD
                LCD_COLUMNS,  // number of columns supported by LCD
                LCD_BITS,     // number of bits used to communicate to LCD
                11,           // LCD RS pin
                10,           // LCD strobe pin
                6,            // LCD data bit 1
                5,            // LCD data bit 2
                4,            // LCD data bit 3
                1,            // LCD data bit 4
                0,            // LCD data bit 5 (set to 0 if using 4 bit communication)
                0,            // LCD data bit 6 (set to 0 if using 4 bit communication)
                0,            // LCD data bit 7 (set to 0 if using 4 bit communication)
                0);           // LCD data bit 8 (set to 0 if using 4 bit communication)


        // verify initialization
        if (lcdHandle == -1) {
            logger.fatal(" ==>> LCD INIT FAILED");
            return;
        }


        Lcd.lcdClear(lcdHandle);

        // provision gpio output pins and make sure they are all LOW at startup
        final MCP23017GpioProvider gpioProvider = new MCP23017GpioProvider(I2CBus.BUS_1, 0x20);



        GpioPinDigitalOutput myOutputs[] = {
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_A0, "MyOutput-A0", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_A1, "MyOutput-A1", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_A2, "MyOutput-A2", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_A3, "MyOutput-A3", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_A4, "MyOutput-A4", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_A5, "MyOutput-A5", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_A6, "MyOutput-A6", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_A7, "MyOutput-A7", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_B0, "MyOutput-B0", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_B1, "MyOutput-B1", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_B2, "MyOutput-B2", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_B3, "MyOutput-B3", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_B4, "MyOutput-B4", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_B5, "MyOutput-B5", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_B6, "MyOutput-B6", PinState.LOW),
                GPIO.provisionDigitalOutputPin(gpioProvider, MCP23017Pin.GPIO_B7, "MyOutput-B7", PinState.LOW)
        };




//        // keep program running for 20 seconds
//        for (int count = 0; count < 10; count++) {
//            GPIO.setState(true, myOutputs);
//            Thread.sleep(1000);
//            GPIO.setState(false, myOutputs);
//            Thread.sleep(1000);
//        }



        MessageListener textListener = new MessageListener() {
            @Override
            public void messageReceived(MessageEvent messageEvent) {
                logger.debug("TextMessage received");
            }
        };

        MessageListener gameTimeListener = messageEvent -> {
            Lcd.lcdPosition(lcdHandle, 0, 1) ;
            Lcd.lcdPuts(lcdHandle, StringUtil.padCenter(messageEvent.getMessage().toString(), LCD_COLUMNS)) ;
        };

        MessageListener percentageListener = messageEvent -> {

            int barrier = new BigDecimal(16).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).multiply(messageEvent.getPercentage()).intValue();


            for (int ledON = 0; ledON < barrier; ledON++ ){
                GPIO.setState(true, myOutputs[ledON]);
            }
            for (int ledOFF = barrier; ledOFF < 16; ledOFF++ ){
                GPIO.setState(false, myOutputs[ledOFF]);
            }

        };

        MessageListener gameModeListener = messageEvent -> {
            logger.debug("gameMode changed: " + Farcry1Assault.GAME_MODES[messageEvent.getMode()]);
            Lcd.lcdHome(lcdHandle);
            Lcd.lcdPosition (lcdHandle, 0, 0) ;
            Lcd.lcdPuts (lcdHandle, StringUtil.padCenter(Farcry1Assault.GAME_MODES[messageEvent.getMode()], LCD_COLUMNS)) ;
        };




        Farcry1Assault farcryAssaultThread = new Farcry1Assault(textListener, gameTimeListener, percentageListener, gameModeListener, MAXCYLCES, SECONDS2CAPTURE);


        MessageListener actionListener = messageEvent -> {
            logger.debug("Button selected: " + messageEvent.isOn());
            farcryAssaultThread.toggleFlag();
        };

        btnFlagTrigger.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                logger.debug("btnFlagTrigger");
                farcryAssaultThread.toggleFlag();
            }
        });

        btnGameStartStop.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                logger.debug("btnGameStartStop");
                farcryAssaultThread.startStopGame();
            }
        });


        farcryAssaultThread.run();
//        for (;;) {
//            Thread.sleep(500);
//        }


//        FrmMain frmMain = new FrmMain(actionListener);
//        frmMain.setVisible(true);



        System.out.println("<--Pi4J--> Wiring Pi LCD test program");





//
//        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
//
//        // update time every one second
//        while(true){
//            // write time to line 2 on LCD
//            Lcd.lcdPosition (lcdHandle, 0, 1) ;
//            Lcd.lcdPuts (lcdHandle, "--- " + formatter.format(new Date()) + " ---");
//            Thread.sleep(1000);
//        }
//




    }

}
