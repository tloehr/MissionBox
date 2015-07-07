package misc;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import interfaces.GameButton;
import interfaces.GameModeConfigs;
import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import main.MissionBox;

/**
 * Created by tloehr on 23.06.15.
 */
public class ConfigFC1 extends GameModeConfigs {

    public static final String ID = "farcry1";

    private int cyclemillis = 50;
    private int time2respawn = 20;
    private int maxcycles = 200;
    private int seconds2capture = 600;

    @Override
    public void setProperty(String key, String value) {
        if (key.equalsIgnoreCase("cyclemillis")){
            cyclemillis = Integer.parseInt(value);
        } else if (key.equalsIgnoreCase("time2respawn")){
            time2respawn = Integer.parseInt(value);
        } else if (key.equalsIgnoreCase("maxcycles")){
            maxcycles = Integer.parseInt(value);
        } else if (key.equalsIgnoreCase("seconds2capture")){
            seconds2capture = Integer.parseInt(value);
        }
    }

    @Override
    public void setButton(String key, GameButton btn, String gui) {

        if (key.equalsIgnoreCase("flag"))
            setBtnFlag(btn);
        else if (key.equalsIgnoreCase("reset"))
            setBtnReset(btn);
        else if (key.equalsIgnoreCase("quit"))
            setBtnQuit(btn);
    }

    private Music playSiren, playWinningSon;
    private Sound playWelcome, playRocket;
    private GameButton btnFlag = null;
    private GameButton btnReset = null;
    private GameButton btnQuit = null;

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

    public GameButton getBtnFlag() {
        return btnFlag;
    }

    public void setBtnFlag(GameButton btnFlag) {
        this.btnFlag = btnFlag;
    }

    public GameButton getBtnReset() {
        return btnReset;
    }

    public void setBtnReset(GameButton btnReset) {
        this.btnReset = btnReset;
    }

    public GameButton getBtnQuit() {
        return btnQuit;
    }

    public void setBtnQuit(GameButton btnQuit) {
        this.btnQuit = btnQuit;
    }
}
