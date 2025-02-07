package eu.aston.uptime.controller;

import java.util.List;
import java.util.Map;

import eu.aston.uptime.model.ResourceStateData;
import eu.aston.uptime.service.BaseState;
import eu.aston.uptime.service.JobState;
import eu.aston.uptime.service.MultiState;
import eu.aston.uptime.service.SingleState;
import eu.aston.uptime.service.StateStore;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;

@Controller("/uptime-rescue")
public class ResourceController {

    private final StateStore stateStore;

    public ResourceController(StateStore stateStore) {
        this.stateStore = stateStore;
    }

    @Get("/state")
    public List<ResourceStateData> fetchAll() {
        return stateStore.getAllStates().stream()
                .map(s->new ResourceStateData(s.getName(), s.getType(), s.getRunning(), s.isPending()))
                .toList();
    }

    @Get("/state/{name}")
    public ResourceStateData fetchResource(@PathVariable String name) {
        BaseState state = stateStore.getState(name);       
        if(state != null) {
            return new ResourceStateData(name, state.getType(), state.getRunning(), state.isPending());
        }
        throw new HttpStatusException(HttpStatus.NOT_FOUND, "Resource not found");
    }

    @Post("/start/{name}")
    public void startJob(@PathVariable String name, @Body Map<String, String> body) {
        BaseState state = stateStore.getState(name);
        if(state instanceof JobState jobState) {
            jobState.runAsync(body);
        } else {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Resource is not a job");
        }
    }

    @Get("/run/{name}")
    public void run(@PathVariable String name, @QueryValue("count") int count, @QueryValue("up") @Nullable Boolean up) {
        BaseState state = stateStore.getState(name);   
        if(state instanceof SingleState singleState) {
            singleState.setRunning(count > 0);
        } else if(state instanceof MultiState multiState) {
            multiState.setCurrentInstances(count, up != null ? up : false);
        } else {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Resource is not a single or multi");
        }
    }

    @Get("/watchdog/{name}")
    public void watchdog(@PathVariable String name) {
        BaseState state = stateStore.getState(name);
        if(state instanceof SingleState singleState) {
            singleState.setWatchdog();
        }
    }
}