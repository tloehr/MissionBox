package interfaces;

import main.MissionBox;
import misc.Tools;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.math.BigDecimal;
import java.util.EventObject;


/**
 * Created by tloehr on 25.04.15.
 */
public class MessageEvent extends EventObject {

    final Logger LOGGER = Logger.getLogger(this.getClass());

    protected final Object message;
//    protected final Object message2;
    protected final int gameState;
    protected final Logger logger = Logger.getLogger(getClass());



    public MessageEvent(Object source, int gameState, String message) {
        super(source);
        LOGGER.setLevel(MissionBox.getLogLevel());
        this.gameState = gameState;
        this.message = Tools.xx(message);

    }

    public MessageEvent(Object source, int gameState, Long message) { //
        super(source);
        LOGGER.setLevel(MissionBox.getLogLevel());
        this.gameState = gameState;
        this.message = message;

    }

    public MessageEvent(Object source, int gameState, BigDecimal percentage) {
        super(source);
        LOGGER.setLevel(MissionBox.getLogLevel());
        this.gameState = gameState;
        this.message = percentage;

    }

    public MessageEvent(Object source, int gameState) {
        super(source);
        LOGGER.setLevel(MissionBox.getLogLevel());
        this.gameState = gameState;
        this.message = new Integer(gameState);

    }

    public MessageEvent(Object source, int gameState, Boolean on) {
        super(source);
        LOGGER.setLevel(MissionBox.getLogLevel());
        this.gameState = gameState;
        this.message = on;

    }

    public boolean isPercentage() {
        return message instanceof BigDecimal;
    }

    public boolean isBoolean() {
        return message instanceof Boolean;
    }


    public boolean isText() {
        return message instanceof String;
    }

    public Object getMessage() {
        return message;
    }

    public DateTime getTime() {
        return new DateTime(message, DateTimeZone.UTC);
    }

//    public String getDateTimeFormatted() {
//        String result = getTime().toString("mm:ss");
//        if (message2 != null) {
//            long m2 = (long) message2;
//
//            result += " (" + new DateTime(Math.abs(m2), DateTimeZone.UTC).toString("mm:ss") + (m2 < 0 ? " overtime)" : ")");
//        }
//
//        return result;
//    }

    public BigDecimal getPercentage() {
        return isPercentage() ? (BigDecimal) message : null;
    }

    public int getGameState() {
        return gameState;
    }

    public boolean isOn() {
        return isBoolean() ? (Boolean) message : false;
    }

}
