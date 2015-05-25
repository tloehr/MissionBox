package events;

import com.sun.org.apache.xpath.internal.operations.Bool;
import main.MissionBox;
import misc.Tools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EventObject;
import java.util.logging.Logger;

/**
 * Created by tloehr on 25.04.15.
 */
public class MessageEvent extends EventObject {

    final Logger LOGGER = Logger.getLogger("eventObject");
    private final Object message;

    public MessageEvent(Object source, String message) {
        super(source);
        LOGGER.setLevel(MissionBox.LOGLEVEL);
        this.message = Tools.xx(message);
        LOGGER.finest("TextMessage: " + this.message);
    }

    public MessageEvent(Object source, BigDecimal percentage) {
        super(source);
        this.message = percentage;
        LOGGER.finest("PercentageMessage: " + percentage.setScale(2, RoundingMode.HALF_UP).toString());
    }

    public MessageEvent(Object source, int mode) {
        super(source);
        this.message = new Integer(mode);
        LOGGER.finest("IntegerMessage: " + mode);
    }

    public MessageEvent(Object source, Boolean on) {
        super(source);
        this.message = on;
        LOGGER.finest("BooleanMessage: " + on);
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

    public BigDecimal getPercentage() {
        return isPercentage() ? (BigDecimal) message : null;
    }


    public Integer getMode() {
        return isMode() ? (Integer) message : null;
    }

    public boolean isOn(){
        return isBoolean() ? (Boolean) message : false;
    }

}
