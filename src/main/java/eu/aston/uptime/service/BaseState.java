package eu.aston.uptime.service;

import java.io.File;
import java.util.Map;

import eu.aston.uptime.model.Resource;

public abstract class BaseState {

    protected final Resource resource;
    protected final File workDir;
    protected final StateStore stateStore;
    protected final Map<String, String> params;
    private boolean pending;

    public BaseState(Resource resource, StateStore stateStore) {
        this.resource = resource;
        this.workDir = new File(stateStore.getAppConfig().baseDir(), resource.getName());
        this.stateStore = stateStore;
        this.params = stateStore.getParamsBuilder().build(resource.getParams());
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void runNow(String script, Map<String, String> parameters){
        this.pending = true;
        try{
            if(resource.getAuth()!=null && stateStore.getState(resource.getAuth()) instanceof JobState jobState) {
                jobState.runJobAsAuth();
            }
            stateStore.getFlowRunner().run(resource.getName(), script, parameters, workDir, resource.getFiles());
        } finally {
            this.pending = false;
        }
    }

    public abstract void checkState();

    public abstract int getRunning();

    public String getName() {
        return resource.getName();
    }

    public String getType() {
        return resource.getClass().getSimpleName();
    }

    public boolean isPending() {
        return pending;
    }
}
