package misc;

import com.pi4j.gpio.extension.mcp.MCP23017GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.i2c.I2CBus;
import kuusisto.tinysound.Music;

import javax.swing.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ResourceBundle;

/**
 * Created by tloehr on 01.05.15.
 */
public class Tools {

    public static final String SND_WELCOME = "/local/0140_female1_OnWelcome_1.wav";
    public static final String SND_SIREN = "/local/capture_siren.wav";
    public static final String SND_FLARE = "/local/MP_flare.wav";
    public static final String SND_SHUTDOWN = "/local/Shutdown.wav";
    public static final String SND_MINIONS_SPAWNED = "/local/0112_female1_OnMinionsSpawn_1.wav";
    public static final String SND_VICTORY = "/local/0134_female1_OnVictory_1.wav";
    public static final String SND_DEFEAT = "/local/0071_female1_OnDefeat_1.wav";
    public static final String SND_MIB = "/local/mib.wav";


    public static String xx(String message) {
        String title = catchNull(message);
        try {
            ResourceBundle lang = ResourceBundle.getBundle("Messages");
            title = lang.getString(message);
        } catch (Exception e) {
            // ok, its not a langbundle key
        }
        return title;
    }

    public static String catchNull(String in) {
        return (in == null ? "" : in.trim());
    }


    public static Pin getPinByName(String provider, String pinname) {
        if (provider.equalsIgnoreCase("mcp23017")) {
            switch (pinname) {
                case "gpio_a0":
                    return MCP23017Pin.GPIO_A0;
                case "gpio_a1":
                    return MCP23017Pin.GPIO_A1;
                case "gpio_a2":
                    return MCP23017Pin.GPIO_A2;
                case "gpio_a3":
                    return MCP23017Pin.GPIO_A3;
                case "gpio_a4":
                    return MCP23017Pin.GPIO_A4;
                case "gpio_a5":
                    return MCP23017Pin.GPIO_A5;
                case "gpio_a6":
                    return MCP23017Pin.GPIO_A6;
                case "gpio_a7":
                    return MCP23017Pin.GPIO_A7;
                case "gpio_b0":
                    return MCP23017Pin.GPIO_B0;
                case "gpio_b1":
                    return MCP23017Pin.GPIO_B1;
                case "gpio_b2":
                    return MCP23017Pin.GPIO_B2;
                case "gpio_b3":
                    return MCP23017Pin.GPIO_B3;
                case "gpio_b4":
                    return MCP23017Pin.GPIO_B4;
                case "gpio_b5":
                    return MCP23017Pin.GPIO_B5;
                case "gpio_b6":
                    return MCP23017Pin.GPIO_B6;
                case "gpio_b7":
                    return MCP23017Pin.GPIO_B7;
                default:
                    return null;
            }
        }
        return null;
    }


    public static boolean isRaspberry(){
        return System.getProperty("os.arch").equalsIgnoreCase("arm") && System.getProperty("os.name").equalsIgnoreCase("linux");
    }


    public static void fadeout(Music music) {
        SwingWorker worker = new SwingWorker() {
            double volume;

            @Override
            protected Object doInBackground() throws Exception {
                volume = music.getVolume();

                for (double vol = volume; vol >= 0d; vol = vol - 0.01d) {
                    music.setVolume(vol);
                    Thread.sleep(50);
                }

                return null;
            }

            @Override
            protected void done() {
                super.done();
                music.stop();
                music.setVolume(volume);
            }
        };
        worker.run();
    }

//    public GpioPinDigitalOutput[] getProgressTo16LCDs(BigDecimal percent, GpioController gpio) throws IOException {
//        final MCP23017GpioProvider gpioProvider0 = new MCP23017GpioProvider(I2CBus.BUS_1, Integer.parseInt("20", 16));
//        final MCP23017GpioProvider gpioProvider1 = new MCP23017GpioProvider(I2CBus.BUS_1, Integer.parseInt("21", 16));
//        final MCP23017GpioProvider gpioProvider2 = new MCP23017GpioProvider(I2CBus.BUS_1, Integer.parseInt("22", 16));
//
//        int barrier = new BigDecimal(16).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP).multiply(percent).intValue();
//
//
//
//
//
//
//        GpioPinDigitalOutput myOutputs[] = {
//                gpio.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A0, "MyOutput-A0", PinState.LOW),
//                gpio.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A1, "MyOutput-A1", PinState.LOW),
//                gpio.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A2, "MyOutput-A2", PinState.LOW),
//                gpio.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A3, "MyOutput-A3", PinState.LOW),
//                gpio.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A4, "MyOutput-A4", PinState.LOW),
//                gpio.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A5, "MyOutput-A5", PinState.LOW),
//                gpio.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A6, "MyOutput-A6", PinState.LOW),
//                gpio.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_A7, "MyOutput-A7", PinState.LOW),
//                gpio.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B0, "MyOutput-B0", PinState.LOW),
//                gpio.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B1, "MyOutput-B1", PinState.LOW),
//                gpio.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B2, "MyOutput-B2", PinState.LOW),
//                gpio.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B3, "MyOutput-B3", PinState.LOW),
//                gpio.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B4, "MyOutput-B4", PinState.LOW),
//                gpio.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B5, "MyOutput-B5", PinState.LOW),
//                gpio.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B6, "MyOutput-B6", PinState.LOW),
//                gpio.provisionDigitalOutputPin(gpioProvider0, MCP23017Pin.GPIO_B7, "MyOutput-B7", PinState.LOW)
//        };
//        return myOutputs;
//    }

}
