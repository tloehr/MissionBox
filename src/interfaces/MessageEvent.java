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
        this.message = Tools.xx(message);
    }

    public MessageEvent(Object source, BigDecimal percentage) {
        super(source);
        this.message = percentage;
    }

    public MessageEvent(Object source, int mode) {
        super(source);
        this.message = new Integer(mode);
    }

    public MessageEvent(Object source, Boolean on) {
        super(source);
        this.message = on;
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
