package interfaces;

import javax.swing.*;

/**
 * Created by tloehr on 16.03.16.
 */
public class MyLED extends JLabel {
    private Icon imageOn;
    private Icon imageOff;
    private boolean off;

    public MyLED() {
        imageOn = null;
        imageOff = null;
        off = true;
    }

    public MyLED(String text, Icon icon, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
        imageOn = null;
        imageOff = icon;
        off = true;
    }

    public MyLED(String text, int horizontalAlignment) {
        super(text, horizontalAlignment);
        imageOn = null;
        imageOff = null;
        off = true;
    }

    public MyLED(String text) {
        super(text);
        imageOn = null;
        imageOff = null;
        off = true;
    }

    public MyLED(Icon image, int horizontalAlignment) {
        super(image, horizontalAlignment);
        imageOn = null;
        imageOff = image;
        off = true;
    }

    public MyLED(Icon image) {
        super(image);
        imageOn = null;
        imageOff = image;
        off = true;
    }

    public MyLED(Icon imageOn, Icon imageOff) {
        this(imageOff);
        this.imageOn = imageOn;
    }

    public void on() {
        SwingUtilities.invokeLater(() -> {
            setIcon(imageOn);
            off = false;
            revalidate();
            repaint();
        });
    }

    public void off() {
        SwingUtilities.invokeLater(() -> {
            setIcon(imageOff);
            off = true;
            revalidate();
            repaint();
        });
    }

}
