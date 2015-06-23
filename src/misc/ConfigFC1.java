package misc;

import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;

/**
 * Created by tloehr on 23.06.15.
 */
public class ConfigFC1 {

    private int cyclemillis = 50;
    private int time2respawn = 20;
    private int maxcycles = 200;
    private int seconds2capture = 600;

    private Music playSiren, playWinningSon;
    private Sound playWelcome, playRocket;


    public ConfigFC1() {
    }

    public int getCyclemillis() {
        return cyclemillis;
    }

    public void setCyclemillis(int cyclemillis) {
        this.cyclemillis = cyclemillis;
    }

    public int getTime2respawn() {
        return time2respawn;
    }

    public void setTime2respawn(int time2respawn) {
        this.time2respawn = time2respawn;
    }

    public int getMaxcycles() {
        return maxcycles;
    }

    public void setMaxcycles(int maxcycles) {
        this.maxcycles = maxcycles;
    }

    public int getSeconds2capture() {
        return seconds2capture;
    }

    public void setSeconds2capture(int seconds2capture) {
        this.seconds2capture = seconds2capture;
    }

    public Music getPlaySiren() {
        return playSiren;
    }

    public void setPlaySiren(Music playSiren) {
        this.playSiren = playSiren;
    }

    public Music getPlayWinningSon() {
        return playWinningSon;
    }

    public void setPlayWinningSon(Music playWinningSon) {
        this.playWinningSon = playWinningSon;
    }

    public Sound getPlayWelcome() {
        return playWelcome;
    }

    public void setPlayWelcome(Sound playWelcome) {
        this.playWelcome = playWelcome;
    }

    public Sound getPlayRocket() {
        return playRocket;
    }

    public void setPlayRocket(Sound playRocket) {
        this.playRocket = playRocket;
    }
}
