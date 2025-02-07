package eu.aston.uptime.model;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class SingleResource extends Resource {
    private String start;
    private String stop;
    private String check;
    private long minimalRunningTime;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    public long getMinimalRunningTime() {
        return minimalRunningTime;
    }

    public void setMinimalRunningTime(long minimalRunningTime) {
        this.minimalRunningTime = minimalRunningTime;
    }
}
