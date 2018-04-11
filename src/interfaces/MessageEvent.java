package interfaces;

import main.Main;
import misc.Tools;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.EventObject;


/**
 * Created by tloehr on 25.04.15.
 */
public class MessageEvent extends EventObject {

    protected final Object message;
    protected final int gameState;
    protected final Logger logger = Logger.getLogger(getClass());



    public MessageEvent(Object source, int gameState, String message) {
        super(source);
        logger.setLevel(Main.getLogLevel());
        this.gameState = gameState;
        this.message = Tools.xx(message);

    }

    public MessageEvent(Object source, int gameState, Long message) { //
        super(source);
        logger.setLevel(Main.getLogLevel());
        this.gameState = gameState;
        this.message = message;

    }

    public MessageEvent(Object source, int gameState, BigDecimal percentage) {
        super(source);
        logger.setLevel(Main.getLogLevel());
        this.gameState = gameState;
        this.message = percentage;

    }

    public MessageEvent(Object source, int gameState) {
        super(source);
        logger.setLevel(Main.getLogLevel());
        this.gameState = gameState;
        this.message = new Integer(gameState);

    }

    public MessageEvent(Object source, int gameState, Boolean on) {
        super(source);
        logger.setLevel(Main.getLogLevel());
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

//    public DateTime getTime() {
//
//        FC1DetailsMessageEvent ev = (FC1DetailsMessageEvent) message;
//
//        return new DateTime(ev., DateTimeZone.UTC);
//    }

//    public String getDateTimeFormatted() {
//        String result = getTime().toString("mm:ss");
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
