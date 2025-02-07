package eu.aston.uptime.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

import eu.aston.uptime.AppConfig;
import eu.aston.uptime.model.JobResource;
import eu.aston.uptime.model.MultiResource;
import eu.aston.uptime.model.Resource;
import eu.aston.uptime.model.SingleResource;
import eu.aston.uptime.utils.FlowRunner;
import eu.aston.uptime.utils.ParamsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateStore.class);

    private final FlowRunner flowRunner;
    private final ParamsBuilder paramsBuilder;
    private final AppConfig appConfig;
    private final Executor executor = Executors.newVirtualThreadPerTaskExecutor();

    private final Map<String, BaseState> states = new ConcurrentHashMap<>();
    private final Map<Class<? extends Resource>, BiFunction<Resource, StateStore, BaseState>> factories = new ConcurrentHashMap<>() {{
        put(JobResource.class, (resource, stateStore) -> new JobState((JobResource) resource, stateStore));
        put(SingleResource.class, (resource, stateStore) -> new SingleState((SingleResource) resource, stateStore));
        put(MultiResource.class, (resource, stateStore) -> new MultiState((MultiResource) resource, stateStore));
    }};

    public StateStore(FlowRunner flowRunner, ParamsBuilder paramsBuilder, AppConfig appConfig) {
        this.flowRunner = flowRunner;
        this.paramsBuilder = paramsBuilder;
        this.appConfig = appConfig;
    }

    public BaseState getState(String name) {
        return states.get(name);
    }

    public List<BaseState> getAllStates(){
        return new ArrayList<>(states.values());
    }

    public void checkState() {
        for(BaseState state : states.values()) {
            try{
                state.checkState();
            }catch (Exception e){
                LOGGER.info("checkState {} {}", state.getName(), e.getMessage());
            }
        }
    }

    public Executor getExecutor() {
        return executor;
    }

    public FlowRunner getFlowRunner() {
        return flowRunner;
    }

    public ParamsBuilder getParamsBuilder() {
        return paramsBuilder;
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public BaseState addResource(Resource resource) {
        BiFunction<Resource, StateStore, BaseState> factory = Optional.ofNullable(factories.get(resource.getClass()))
            .orElseThrow(() -> new IllegalArgumentException("Unsupported resource type: " + resource.getClass().getName()));
        BaseState state = factory.apply(resource, this);
        states.put(resource.getName(), state);
        LOGGER.info("Add state: {}", state.getName());
        return state;
    }
}
