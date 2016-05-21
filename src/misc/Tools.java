package misc;

import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.Pin;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Created by tloehr on 01.05.15.
 */
public class Tools {

    public static final String SND_WELCOME = "0140_female1_OnWelcome_1.wav";
    public static final String SND_SIREN = "capture_siren.wav";
    public static final String SND_FLARE = "MP_flare.wav";
    public static final String SND_SHUTDOWN = "Shutdown.wav";
    public static final String SND_MINIONS_SPAWNED = "0112_female1_OnMinionsSpawn_1.wav";
    public static final String SND_VICTORY = "0134_female1_OnVictory_1.wav";
    public static final String SND_DEFEAT = "0071_female1_OnDefeat_1.wav";
    public static final String SND_MIB = "mib.wav";
    public static final String SND_LOSER = "loser.wav";
    public static final String SND_QUEEN = "We Will Rock You - Queen - Greatest Hits - 16.wav";
    public static final String SND_START = "196889__ionicsmusic__race-robot-start.wav";
    public static final String SND_GAME_OVER = "196868__ionicsmusic__race-robot-game-over.wav";

    public static final String[] COUNTDOWN = new String[]{"10.wav","09.wav","08.wav","07.wav","06.wav","05.wav","04.wav","03.wav","02.wav","01.wav","00.wav"};
    public static final String[] WINNING_SONGS = new String[]{SND_MIB, SND_QUEEN};
    public static final String[] LOSING_SONGS = new String[]{SND_LOSER};


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


    // http://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
    public static boolean isWindows() {

        String os = System.getProperty("os.name").toLowerCase();
        //windows
        return (os.indexOf("win") >= 0);

    }

    // http://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
    public static boolean isMac() {

        String os = System.getProperty("os.name").toLowerCase();
        //Mac
        return (os.indexOf("mac") >= 0);

    }

    // http://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
    public static boolean isArm() {

        String os = System.getProperty("os.arch").toLowerCase();
        //Mac
        return (os.indexOf("arm") >= 0);

    }

    // http://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
    public static boolean isUnix() {

        String os = System.getProperty("os.name").toLowerCase();
        //linux or unix
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);

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

    public static String getMissionboxDirectory() {
        return System.getProperty("user.home") + File.separator + "missionbox";
    }

    public static String getSoundPath() {
        return getMissionboxDirectory() + File.separator + "sounds";
    }

    public static String getWinningSong() {
        int rand = ThreadLocalRandom.current().nextInt(0, WINNING_SONGS.length);
        return WINNING_SONGS[rand];
    }

    public static String getLosingSong() {
        int rand = ThreadLocalRandom.current().nextInt(0, LOSING_SONGS.length);
        return LOSING_SONGS[rand];
    }


//    public static Animator flashBackground(Animator animator, final JComponent component, final Color flashcolor, int repeatTimes) {
//        if (component == null)
//            return null; // this prevents NULL pointer exceptions when quickly switching the residents after the entry
//        final Color originalColor = component.getBackground();
//
//
//        if (animator == null || !animator.isRunning()) {
//
//            final TimingSource ts = new SwingTimerTimingSource();
//            final boolean wasOpaque = component.isOpaque();
//            Animator.setDefaultTimingSource(ts);
//            ts.init();
//            component.setOpaque(true);
//
//
//            animator = new Animator.Builder().setDuration(750, TimeUnit.MILLISECONDS).setRepeatCount(repeatTimes).setRepeatBehavior(Animator.RepeatBehavior.REVERSE).setStartDirection(Animator.Direction.FORWARD).addTarget(new TimingTargetAdapter() {
//                @Override
//                public void begin(Animator source) {
//                }
//
//                @Override
//                public void timingEvent(Animator animator, final double fraction) {
//                    SwingUtilities.invokeLater(() -> {
//                        component.setBackground(interpolateColor(originalColor, flashcolor, fraction));
//                        component.repaint();
//                    });
//                }
//
//                @Override
//                public void end(Animator source) {
//                    component.setOpaque(wasOpaque);
//                    component.repaint();
//                }
//            }).build();
//        } else {
//            animator.stop();
//        }
//        animator.start();
//
//        return animator;
//    }

    /**
     * @param distance a double between 0.0f and 1.0f to express the distance between the source and destination color
     *                 see http://stackoverflow.com/questions/27532/generating-gradients-programatically
     * @return
     */
    public static Color interpolateColor(Color source, Color destination, double distance) {
        int red = (int) (destination.getRed() * distance + source.getRed() * (1 - distance));
        int green = (int) (destination.getGreen() * distance + source.getGreen() * (1 - distance));
        int blue = (int) (destination.getBlue() * distance + source.getBlue() * (1 - distance));
        return new Color(red, green, blue);
    }

//    public static void flashBackground(final JComponent component, final Color flashcolor, int repeatTimes) {
//        // https://github.com/tloehr/Offene-Pflege.de/issues/37
//        if (component == null)
//            return; // this prevents NULL pointer exceptions when quickly switching the residents after the entry
//        flashBackground(component, flashcolor, component.getBackground(), repeatTimes);
//    }

//    public static void flashBackground(final JComponent component, final Color flashcolor, final Color originalColor, int repeatTimes) {
//        if (component == null)
//            return; // this prevents NULL pointer exceptions when quickly switching the residents after the entry
//        //            final Color originalColor = component.getBackground();
//        final TimingSource ts = new SwingTimerTimingSource();
//        final boolean wasOpaque = component.isOpaque();
//        Animator.setDefaultTimingSource(ts);
//        ts.init();
//        component.setOpaque(true);
//        Animator animator = new Animator.Builder().setDuration(750, TimeUnit.MILLISECONDS).setRepeatCount(repeatTimes).setRepeatBehavior(Animator.RepeatBehavior.REVERSE).setStartDirection(Animator.Direction.FORWARD).addTarget(new TimingTargetAdapter() {
//            @Override
//            public void begin(Animator source) {
//            }
//
//            @Override
//            public void timingEvent(Animator animator, final double fraction) {
//                SwingUtilities.invokeLater(() -> {
//                    component.setBackground(interpolateColor(originalColor, flashcolor, fraction));
//                    component.repaint();
//                });
//            }
//
//            @Override
//            public void end(Animator source) {
//                component.setOpaque(wasOpaque);
//                component.repaint();
//            }
//        }).build();
//        animator.start();
//    }

//    public static void flashIcon(final AbstractButton btn, final Icon icon) {
//        flashIcon(btn, icon, 2);
//    }
//
//    public static void flashIcon(final AbstractButton btn, final Icon icon, int repeat) {
//
//        if (btn == null)
//            return; // this prevents NULL pointer exceptions when quickly switching the residents after the entry
//
//        int textposition = btn.getHorizontalTextPosition();
//        btn.setHorizontalTextPosition(SwingConstants.LEADING);
//
//        final Icon originalIcon = btn.getIcon();
//        final TimingSource ts = new SwingTimerTimingSource();
//        Animator.setDefaultTimingSource(ts);
//        ts.init();
//
//        Animator animator = new Animator.Builder().setDuration(750, TimeUnit.MILLISECONDS).setRepeatCount(repeat).setRepeatBehavior(Animator.RepeatBehavior.REVERSE).setStartDirection(Animator.Direction.FORWARD).addTarget(new TimingTargetAdapter() {
//            Animator.Direction dir;
//
//            public void begin(Animator source) {
//                dir = null;
//            }
//
//            @Override
//            public void timingEvent(Animator animator, final double fraction) {
//
//                if (dir == null || !dir.equals(animator.getCurrentDirection())) {
//
//                    dir = animator.getCurrentDirection();
//
//                    SwingUtilities.invokeLater(() -> {
//
//                        if (animator.getCurrentDirection().equals(Animator.Direction.FORWARD)) {
//                            btn.setIcon(icon);
//                        } else {
//                            btn.setIcon(originalIcon);
//                        }
//
//                        //                    Logger.getLogger(getClass()).debug(fraction);
//                        //                    btn.setIcon();
//                        //                    component.setBackground(interpolateColor(originalColor, flashcolor, fraction));
//                        btn.revalidate();
//                        btn.repaint();
//                    });
//                }
//            }
//
//            @Override
//            public void end(Animator source) {
//                SwingUtilities.invokeLater(() -> {
//                    btn.setHorizontalTextPosition(textposition);
//                    btn.setIcon(originalIcon);
//                    btn.repaint();
//                });
//            }
//        }).build();
//        animator.start();
//
//
//    }


}
