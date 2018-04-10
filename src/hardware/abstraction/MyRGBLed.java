package hardware.abstraction;

import com.pi4j.io.gpio.Pin;
import com.pi4j.wiringpi.SoftPwm;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;

public class MyRGBLed {
    private final Logger logger = Logger.getLogger(getClass());
    private final JLabel lbl;
    private final Pin pinRed;
    private final Pin pinGreen;
    private final Pin pinBlue;
    private String name;

    public MyRGBLed(Pin pinRed, Pin pinGreen, Pin pinBlue, JLabel lbl, String name) {
        this.lbl = lbl;

        this.pinRed = pinRed;
        this.pinGreen = pinGreen;
        this.pinBlue = pinBlue;
        this.name = name;

    }

//    public void setText(String text) {
//        if (lbl == null) return;
//        lbl.setText(text);
//    }

//    public String getText() {
//        if (lbl == null) return "--";
//        return lbl.getText();
//    }

    public void setToolTipText(String text) {
        if (lbl == null) return;
        lbl.setToolTipText(text);
    }

    public String getToolTipText() {
        if (lbl == null) return "";
        return lbl.getToolTipText();
    }


    public String getName() {
        return name;
    }

    public void off() {
        setRGB(0, 0, 0);
    }

    public void setRGB(int red, int green, int blue) {
        if (lbl != null) {
            Color color = new Color(red, green, blue);
            lbl.setBackground(color);
            lbl.setForeground(Tools.getContrastColor(color));
        }

        if (pinRed != null) {
            SoftPwm.softPwmWrite(pinRed.getAddress(), red);
            SoftPwm.softPwmWrite(pinGreen.getAddress(), green);
            SoftPwm.softPwmWrite(pinBlue.getAddress(), blue);
        }
    }
}
