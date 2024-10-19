package eu.aston.uptime;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

import eu.aston.uptime.model.Resource;
import eu.aston.uptime.service.ConfigStore;
import eu.aston.uptime.service.StateStore;
import eu.aston.uptime.utils.FlowRunner;
import eu.aston.uptime.utils.ParamsBuilder;
import io.micronaut.context.annotation.Factory;
import io.micronaut.scheduling.ScheduledExecutorTaskScheduler;
import jakarta.inject.Singleton;

@Factory
public class AppFactory {

    @Singleton
    public HttpClient httpClient() {
        return HttpClient.newBuilder().build();
    }

    @Singleton
    public StateStore stateStore(FlowRunner flowRunner, ParamsBuilder paramsBuilder, AppConfig appConfig, ScheduledExecutorTaskScheduler scheduledExecutorTaskScheduler) throws IOException {
        ConfigStore configStore = new ConfigStore();
        List<Resource> resources = configStore.loadResources(appConfig.configDir().toPath());
        StateStore stateStore = new StateStore(flowRunner, paramsBuilder, appConfig);
        for(Resource resource : resources) {
            stateStore.addResource(resource);
        }
        scheduledExecutorTaskScheduler.scheduleWithFixedDelay(
                Duration.ofSeconds(5),
                Duration.ofSeconds(15),
                stateStore::checkState);

        return stateStore;
    }
}
