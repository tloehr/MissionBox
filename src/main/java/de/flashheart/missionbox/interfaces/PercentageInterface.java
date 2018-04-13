package de.flashheart.missionbox.interfaces;

import java.math.BigDecimal;

/**
 * Created by tloehr on 28.04.15.
 */
public abstract class PercentageInterface {
    private String name;
    protected long start;
    protected long now;
    protected long end;

    public PercentageInterface(String name) {
        this.name = name;
    }

    public abstract void setValue(BigDecimal percent);

    public void setValue(long start, long now, long end){
        this.start = start;
        this.now = now;
        this.end = end;
        BigDecimal progress = new BigDecimal(now - start).divide(new BigDecimal(end - start), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
        setValue(progress);
    };

    @Override
    public String toString() {
        return name;
    }
}
