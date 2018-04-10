package threads;

import java.awt.*;

public class RGBScheduleElement {
    int red = 0;
    int green = 0;
    int blue = 0;
    long duration = 0;

    public RGBScheduleElement(String red, String green, String blue, String duration) throws NumberFormatException {
        this.red = Integer.parseInt(red);
        this.green = Integer.parseInt(green);
        this.blue = Integer.parseInt(blue);
        this.duration = Long.parseLong(duration);
    }

    public RGBScheduleElement(int red, int green, int blue, long duration) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.duration = duration;
    }

    public RGBScheduleElement(Color color, long duration) {
        this(color.getRed(), color.getGreen(), color.getBlue(), duration);
    }

    public RGBScheduleElement(Color color) {
        this(color, Long.MAX_VALUE);
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return red + "," + green + "," + blue + "," + duration;
    }
}