package misc;

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

}
