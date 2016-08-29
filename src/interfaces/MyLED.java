package interfaces;

import misc.Tools;

import javax.swing.*;
import java.awt.*;

/**
 * Created by tloehr on 16.03.16.
 */
public class MyLED extends JLabel implements OnOffInterface {
    private Icon imageOn;
    private Icon imageOff;

    public MyLED() {
        this(null, Color.RED);
    }

    public MyLED(String text, Color color) {
        super(text);
        chooseLEDFor(color);
        setOn(false);
    }

    public MyLED(String text) {
        this(text, Color.RED);
    }

    void chooseLEDFor(Color color) {
        if (color.equals(Color.BLUE)) {
            imageOn = Tools.icon22ledBlueOn;
            imageOff = Tools.icon22ledBlueOff;
            return;
        }
        if (color.equals(Color.RED)) {
            imageOn = Tools.icon22ledRedOn;
            imageOff = Tools.icon22ledRedOff;
            return;
        }
        if (color.equals(Color.YELLOW)) {
            imageOn = Tools.icon22ledYellowOn;
            imageOff = Tools.icon22ledYellowOff;
            return;
        }
        if (color.equals(Color.GREEN)) {
            imageOn = Tools.icon22ledGreenOn;
            imageOff = Tools.icon22ledGreenOff;
            return;
        }
        if (color.equals(Color.ORANGE)) {
            imageOn = Tools.icon22ledOrangeOn;
            imageOff = Tools.icon22ledOrangeOff;
            return;
        }
        if (color.equals(Color.MAGENTA)) {
            imageOn = Tools.icon22ledPurpleOn;
            imageOff = Tools.icon22ledPurpleOff;
            return;
        }
    }

//
//    public MyLED(String text, Icon icon, int horizontalAlignment) {
//        super(text, icon, horizontalAlignment);
//        imageOn = null;
//        imageOff = icon;
//        off = true;
//    }

    @Override
    public void setOn(boolean on) {
        SwingUtilities.invokeLater(() -> {
            setIcon(on ? imageOn : imageOff);
            revalidate();
            repaint();
        });
    }
//
//    public MyLED(String text, int horizontalAlignment) {
//        super(text, horizontalAlignment);
//        imageOn = null;
//        imageOff = null;
//        off = true;
//    }


//    public MyLED(Icon image, int horizontalAlignment) {
//        super(image, horizontalAlignment);
//        imageOn = null;
//        imageOff = image;
//        off = true;
//    }

//    public MyLED(Icon image) {
//        super(image);
//        imageOn = null;
//        imageOff = image;
//        off = true;
//    }
//
//    public MyLED(Icon imageOn, Icon imageOff) {
//        this(imageOff);
//        this.imageOn = imageOn;
//    }


}
