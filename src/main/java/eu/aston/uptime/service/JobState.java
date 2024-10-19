package eu.aston.uptime.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

import eu.aston.uptime.model.JobResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobState extends BaseState {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobState.class);

    private final JobResource jobResource;
    private final Executor executor;
    private final Semaphore semaphore;

    public JobState(JobResource jobResource, StateStore stateStore) {
        super(jobResource, stateStore);
        this.jobResource = jobResource;
        this.executor = stateStore.getExecutor();
        this.semaphore = new Semaphore(jobResource.getMaxConcurrentOperations());
    }

    public void runAsync(Map<String, String> runParameters) {
        if(jobResource.isPendingIfNoCapacity() && semaphore.availablePermits()==0) {
            LOGGER.info("pending job {}", resource.getName());
            return;
        }
        if(semaphore.tryAcquire()) {
            executor.execute(() -> {
                try {
                    run(runParameters);
                } finally {
                    semaphore.release();
                }
            });
        }
    }

    public void run(Map<String, String> runParameters) {
        Map<String, String> parameters = new HashMap<>(getParams());
        if(runParameters !=null) parameters.putAll(runParameters);
        runNow(jobResource.getStart(), parameters);
    }

    private long lastAuth = 0L;

    public void runJobAsAuth(){
        long now = System.currentTimeMillis()/Duration.ofDays(1).toMillis();
        if(lastAuth!=now) {
            this.lastAuth = now;
            run(Map.of());
        }
    }

    @Override
    public void checkState() {
    }

    @Override
    public int getRunning() {
        return semaphore.availablePermits();
    }
}
