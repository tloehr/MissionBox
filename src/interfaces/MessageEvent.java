package interfaces;

import main.MissionBox;
import misc.Tools;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EventObject;


/**
 * Created by tloehr on 25.04.15.
 */
public class MessageEvent extends EventObject {

    final Logger LOGGER = Logger.getLogger(this.getClass());

    private final Object message;

    public MessageEvent(Object source, String message) {
        super(source);
        LOGGER.setLevel(MissionBox.logLevel);
        this.message = Tools.xx(message);
//        LOGGER.debug("TextMessage: " + this.message);
    }

    public MessageEvent(Object source, BigDecimal percentage) {
        super(source);
        LOGGER.setLevel(MissionBox.logLevel);
        this.message = percentage;
//        LOGGER.debug("PercentageMessage: " + percentage.setScale(2, RoundingMode.HALF_UP).toString());
    }

    public MessageEvent(Object source, int mode) {
        super(source);
        LOGGER.setLevel(MissionBox.logLevel);
        this.message = new Integer(mode);
//        LOGGER.debug("IntegerMessage: " + mode);
    }

    public MessageEvent(Object source, Boolean on) {
        super(source);
        LOGGER.setLevel(MissionBox.logLevel);
        this.message = on;
//        LOGGER.debug("BooleanMessage: " + on);
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

    public BigDecimal getPercentage() {
        return isPercentage() ? (BigDecimal) message : null;
    }


    public Integer getMode() {
        return isMode() ? (Integer) message : null;
    }

    public boolean isOn() {
        return isBoolean() ? (Boolean) message : false;
    }

}
