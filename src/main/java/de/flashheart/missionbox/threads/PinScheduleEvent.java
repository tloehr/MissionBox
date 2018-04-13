package de.flashheart.missionbox.threads;

public class PinScheduleEvent {
    boolean on;
    long duration;

    public PinScheduleEvent(String on, String duration) {
        this.on = on.equalsIgnoreCase("on");
        if (duration.equals("∞")) this.duration = Long.MAX_VALUE;
        else this.duration = Long.parseLong(duration);
    }

    public boolean isOn() {
        return on;
    }

    public long getDuration() {
        return duration;
    }
}
