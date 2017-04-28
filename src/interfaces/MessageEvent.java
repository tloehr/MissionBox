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

    private final Object message;
    private final Object message2;
    private final int mode;

    public MessageEvent(Object source, int mode, String message) {
        super(source);
        LOGGER.setLevel(MissionBox.getLogLevel());
        this.mode = mode;
        this.message = Tools.xx(message);
        message2 = null;
    }

    public MessageEvent(Object source, int mode, Long message, Long message2) { //
        super(source);
        LOGGER.setLevel(MissionBox.getLogLevel());
        this.mode = mode;
        this.message = message;
        this.message2 = message2;
    }

    public MessageEvent(Object source, int mode, BigDecimal percentage) {
        super(source);
        LOGGER.setLevel(MissionBox.getLogLevel());
        this.mode = mode;
        this.message = percentage;
        message2 = null;
    }

    public MessageEvent(Object source, int mode) {
        super(source);
        LOGGER.setLevel(MissionBox.getLogLevel());
        this.mode = mode;
        this.message = new Integer(mode);
        message2 = null;
    }

    public MessageEvent(Object source, int mode, Boolean on) {
        super(source);
        LOGGER.setLevel(MissionBox.getLogLevel());
        this.mode = mode;
        this.message = on;
        message2 = null;
    }

    public boolean isPercentage() {
        return message instanceof BigDecimal;
    }

    public boolean isBoolean() {
        return message instanceof Boolean;
    }

    public boolean isMode() {
        return message instanceof Integer;
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

    public String getDateTimeFormatted() {
        String result = getTime().toString("mm:ss");
        if (message2 != null) {
            result += " (" + new DateTime(message2, DateTimeZone.UTC).toString("mm:ss") + ")";
        }

        return result;
    }

    public BigDecimal getPercentage() {
        return isPercentage() ? (BigDecimal) message : null;
    }

    public int getMode() {
        return mode;
    }

    public boolean isOn() {
        return isBoolean() ? (Boolean) message : false;
    }

}
