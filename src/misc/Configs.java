package misc;

import de.flashheart.ocfflag.Main;
import gamemodes.Farcry1Assault;
import interfaces.HasLogger;
import main.MissionBox;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;
import java.util.UUID;

public class Configs implements HasLogger {
    private final SortedProperties configs;
    private final Properties applicationContext;



    public static final String MATCHID = "matchid";
    public static final String MYUUID = "uuid";
    public static final String LOGLEVEL = "loglevel";
    public static final String FTPHOST = "ftphost";
    public static final String FTPPORT = "ftpport";
    public static final String FTPUSER = "ftpuser";
    public static final String FTPPWD = "ftppwd";
    public static final String FTPS = "ftps";
    public static final String FTPREMOTEPATH = "ftpremotepath";
    public static final String MIN_STAT_SEND_TIME = "sendstats";
    public static final String FLAGNAME = "flagname";
    public static final String GAMETIME = "gametime";
    public static final String BRIGHTNESS_WHITE = "brightness_white";
    public static final String BRIGHTNESS_BLUE = "brightness_blue";
    public static final String BRIGHTNESS_RED = "brightness_red";
    public static final String BRIGHTNESS_YELLOW = "brightness_yellow";
    public static final String BRIGHTNESS_GREEN = "brightness_green";
    public static final String NUMBER_OF_TEAMS = "num_teams";
    public static final String AIRSIREN_SIGNAL = "airsiren_signal";
    public static final String COLORCHANGE_SIREN_SIGNAL = "colorchange_siren_signal";

    /**
     * Hier stehen die möglichen Schlüssel aus der config.txt
     */
    public static final String FCY_TIME2CAPTURE = "fcy.time2capture";
    public static final String FCY_GAMETIME = "fcy.gametime";
    public static final String MBX_RESUME_TIME = "mbx.resume.time"; // in ms
    public static final String MBX_SIRENHANDLER = "mbx.sirenhandler";
    public static final String MBX_LOGLEVEL = "mbx.loglevel";
    public static final String FCY_RESPAWN_INTERVAL = "fcy.respawn.interval";
    public static final String MBX_RESPAWN_SIRENTIME = "mbx.respawn.sirentime";
    public static final String MBX_STARTGAME_SIRENTIME = "mbx.startgame.sirentime";

    public static final String MBX_SIREN1 = "mbx.siren1";
    public static final String MBX_RESPAWN_SIREN = "mbx.respawn.siren";
    public static final String MBX_AIRSIREN = "mbx.airsiren";
    public static final String MBX_SHUTDOWN_SIREN = "mbx.shutdown.siren";

    public static final String MBX_LED1_BTN_RED = "mbx.led1.btn.red";
    public static final String MBX_LED1_BTN_GREEN = "mbx.led1.btn.green";
    public static final String MBX_LED2_BTN_RED = "mbx.led2.btn.red";
    public static final String MBX_LED2_BTN_GREEN = "mbx.led2.btn.green";
    public static final String MBX_LED_PROGRESS1_RED = "mbx.led.progress1.red";
    public static final String MBX_LED_PROGRESS1_YELLOW = "mbx.led.progress1.yellow";
    public static final String MBX_LED_PROGRESS1_GREEN = "mbx.led.progress1.green";
    public static final String MBX_LED_PROGRESS2_RED = "mbx.led.progress2.red";
    public static final String MBX_LED_PROGRESS2_YELLOW = "mbx.led.progress2.yellow";
    public static final String MBX_LED_PROGRESS2_GREEN = "mbx.led.progress2.green";

    //    public static final String MBX_I2C_1 = "mbx.i2c.1";
//    public static final String MBX_I2C_2 = "mbx.i2c.2";

    public static final String MBX_BTN_GREEN = "mbx.button.green";
    public static final String MBX_BTN_RED = "mbx.button.red";
    public static final String MBX_BTN_START_STOP = "mbx.button.startstop";
    public static final String MBX_BTN_PAUSE = "mbx.button.pause";

    public Configs() throws IOException {
        configs = new SortedProperties(); // Einstellungen, die verändert werden
        applicationContext = new Properties(); // inhalte der application.properties (von Maven)





               // Hier stehen die Standardwerte, falls keine missionbox.cfg existiert.
        configs.put(FCY_TIME2CAPTURE, "180");
        configs.put(FCY_GAMETIME, "6");
        configs.put(FCY_RESPAWN_INTERVAL, "60");
        configs.put(MBX_RESUME_TIME, "10000"); // Zeit in ms, bevor es nach eine Pause weiter geht

        configs.put(MBX_RESPAWN_SIRENTIME, "2000");
        configs.put(MBX_STARTGAME_SIRENTIME, "5000");


       //        config.put(MBX_SIREN_TIME, "750");

        configs.put(MBX_LOGLEVEL, "debug");

               // config.put(MBX_I2C_1, "0x20");
               // config.put(MBX_I2C_2, "0x24");

               // hier werden die üblichen Zuordnungen der einzelnen GPIOs
               // zu den jeweiligen Signalleitungen vorgenommen.
               // Falls man mal was umstecken muss, kann man das einfach
               // später in der missionbox.cfg ändern.
               // diese Werte werden hier so gesetzt, wie es in der
               // Bauanleitung der Box steht.

               // die hier brauchen wir immer
               // Änderung auf RASPI Pins statt MCP23017 PINS am 26.07.2017
               // GPIO 0


               // Standardwerte für die config datei werden hier gesetzt.
               // danach werden evtl. die geänderten Werte aus der Datei drübergeschrieben
               // bei Programm Ende werden alle geänderten Werte wieder in die config.txt zurückgeschrieben


               File configFile = new File(Tools.getMissionboxDirectory() + File.separator + "config.txt");

               configFile.getParentFile().mkdirs();

               configFile.createNewFile();

               FileInputStream in = new FileInputStream(configFile);
               Properties p = new SortedProperties();
               p.load(in);
               config.putAll(p);
               p.clear();
               in.close();

               logLevel = Level.toLevel(config.getProperty(MissionBox.MBX_LOGLEVEL), Level.DEBUG);




        // defaults
        configs.put(MATCHID, "1");
        configs.put(NUMBER_OF_TEAMS, "2");
        configs.put(LOGLEVEL, "debug");
        configs.put(FLAGNAME, "OCF Flagge #" + new java.util.Random().nextInt());
        configs.put(GAMETIME, "0");
        configs.put(FTPS, "false");
        configs.put(FTPPORT, "21");
        configs.put(BRIGHTNESS_WHITE, "10");
        configs.put(BRIGHTNESS_RED, "10");
        configs.put(BRIGHTNESS_BLUE, "10");
        configs.put(BRIGHTNESS_GREEN, "10");
        configs.put(BRIGHTNESS_YELLOW, "10");
        configs.put(MIN_STAT_SEND_TIME, "0"); // in Millis, wie oft sollen die Stastiken spätestens gesendet werden. 0 = gar nicht
        configs.put(AIRSIREN_SIGNAL, "1:on,5000;off,1");
        configs.put(COLORCHANGE_SIREN_SIGNAL, "2:on,50;off,50");

        // configdatei einlesen
        loadConfigs();
        loadApplicationContext();

        // und der Rest
        logger.setLevel(Level.toLevel(configs.getProperty(LOGLEVEL), Level.DEBUG));
        if (!configs.containsKey(MYUUID)) {
            configs.put(MYUUID, UUID.randomUUID().toString());
        }
    }


    private void loadApplicationContext() throws IOException {
        InputStream in2 = Main.class.getResourceAsStream("/application.properties");
        applicationContext.load(in2);
        in2.close();
    }

    private void loadConfigs() throws IOException {
        File configFile = new File(Tools.getWorkingPath() + File.separator + "config.txt");
        configFile.getParentFile().mkdirs();
        configFile.createNewFile(); // falls nicht vorhanden

        FileInputStream in = new FileInputStream(configFile);
        Properties p = new SortedProperties();
        p.load(in);
        configs.putAll(p);
        p.clear();
        in.close();
    }

    public void put(Object key, Object value) {
        configs.put(key, value.toString());
        saveConfigs();
    }

    public boolean isFTPComplete() {
        return configs.containsKey(FTPUSER) && configs.containsKey(FTPHOST) && configs.containsKey(FTPPORT) && configs.containsKey(FTPPWD) && configs.containsKey(FTPS) && configs.containsKey(FTPREMOTEPATH);
    }


    public String getApplicationInfo(Object key) {
        return applicationContext.containsKey(key) ? applicationContext.get(key).toString() : "null";
    }

    public int getInt(Object key) {
            return Integer.parseInt(configs.containsKey(key) ? configs.get(key).toString() : "-1");
        }

    public String get(Object key) {
        return configs.containsKey(key) ? configs.get(key).toString() : "null";
    }

    private void saveConfigs() {
        try {
            File configFile = new File(Tools.getWorkingPath() + File.separator + "config.txt");
            FileOutputStream out = new FileOutputStream(configFile);
            configs.store(out, "Settings OCFFlag");
            out.close();
        } catch (Exception ex) {
            logger.fatal(ex);
            System.exit(1);
        }
    }
    public static void saveLocalProps() {
          logger.debug("saving local props");
          try {
              File configFile = new File(Tools.getMissionboxDirectory() + File.separator + "config.txt");
              FileOutputStream out = new FileOutputStream(configFile);
              config.store(out, "Settings MissionBox");
              out.close();
          } catch (Exception ex) {
              logger.fatal(ex);
              System.exit(1);
          }
      }
    public static void setConfig(String key, String value) {
           config.setProperty(key, value);

           if (key.equalsIgnoreCase(FCY_TIME2CAPTURE)) {
               ((Farcry1Assault) gameMode).setCapturetime(Long.parseLong(value));
           }
           if (key.equalsIgnoreCase(FCY_GAMETIME)) {
               ((Farcry1Assault) gameMode).setMaxgametime(Long.parseLong(value));
           }
           if (key.equalsIgnoreCase(FCY_RESPAWN_INTERVAL)) {
               ((Farcry1Assault) gameMode).setRespawninterval(Long.parseLong(value));
           }

           saveLocalProps();
       }
}
