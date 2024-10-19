package eu.aston.uptime.service;

import java.util.HashMap;
import java.util.Map;

import eu.aston.uptime.model.SingleResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleState extends BaseState {
    private static final Logger LOGGER = LoggerFactory.getLogger(SingleState.class);

    private final SingleResource singleResource;
    private final long watchdogInterval;
    private final long checkInterval;
    private Boolean expectedState;
    private boolean aktState = false;
    private long lastWatchdog = 0;
    private long lastCheck = 0;
    private long lastStart = 0;

    public SingleState(SingleResource singleResource, StateStore stateStore) {
        super(singleResource, stateStore);
        this.singleResource = singleResource;
        this.watchdogInterval = stateStore.getAppConfig().watchdogInterval()*1000L;
        this.checkInterval = stateStore.getAppConfig().checkInterval()*1000L;
    }

    public void setRunning(boolean running) {
        this.expectedState = running;
    }

    public void setWatchdog() {
        this.lastWatchdog = System.currentTimeMillis();
    }

    @Override
	public void checkState() {
        long now = System.currentTimeMillis();
        if(singleResource.getCheck() != null && lastWatchdog < now - watchdogInterval && lastCheck < now - checkInterval) {
            Map<String, String> parameters = new HashMap<>(getParams());
            runNow(singleResource.getCheck(), parameters);
            this.lastCheck = now;
            if(parameters.containsKey("state")) {
                this.aktState = !parameters.get("state").trim().isEmpty();
                LOGGER.info("change state {} {}", getName(), aktState);
            }
        }
        if(this.expectedState==null) {
            this.expectedState = aktState;
            LOGGER.info("resource {} set init expectedState {}", getName(), expectedState);
        }
        if(this.expectedState && !aktState) {
            Map<String, String> parameters = new HashMap<>(getParams());
            runNow(singleResource.getStart(), parameters);
            this.aktState = expectedState;
            this.lastCheck = now;
            this.lastWatchdog = now+watchdogInterval*3;
            this.lastStart = now;
        } else if(!this.expectedState && aktState) {
            if(singleResource.getMinimalRunningTime()>0 && lastStart+singleResource.getMinimalRunningTime()>now) {
                return;
            }
            Map<String, String> parameters = new HashMap<>(getParams());
            runNow(singleResource.getStop(), parameters);
            this.aktState = expectedState;
            this.lastCheck = now;
            this.lastWatchdog = now+watchdogInterval*3;
               
        }
	}

    @Override
    public int getRunning() {
        return aktState ? 1 : 0;
    }
}
