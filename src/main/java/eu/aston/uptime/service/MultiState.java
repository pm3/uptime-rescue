package eu.aston.uptime.service;

import java.util.List;

import eu.aston.uptime.model.MultiItem;
import eu.aston.uptime.model.MultiResource;
import eu.aston.uptime.model.SingleResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiState extends BaseState {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiState.class);
    private final MultiResource multiResource;
    private final List<SingleState> items;
    private Integer currentInstances;
    private int lastStartedIndex = 0;

    MultiState(MultiResource multiResource, StateStore stateStore) {
        super(multiResource, stateStore);
        this.multiResource = multiResource;
        this.items = multiResource.getItems().stream().map(item -> mapSingle(item, multiResource, stateStore)).toList();
    }

    public void setCurrentInstances(int _currentInstances, boolean up) {
        if(_currentInstances>multiResource.getItems().size()) {
            _currentInstances = multiResource.getItems().size();
        }
        if(this.currentInstances!=null) {
            if(_currentInstances==this.currentInstances) {
                return;
            }
            if(_currentInstances<=this.currentInstances && up) {
                return;
            }
        }
        this.currentInstances = _currentInstances;
    }

    @Override
    public void checkState() {
        int aktRunning = 0;
        for(SingleState item: items) {
            try{
                item.checkState();
                aktRunning += item.getRunning();
            }catch (Exception e){
                LOGGER.info("checkState {} {}", item.getName(), e.getMessage());
            }
        }
        if(currentInstances==null) {
            currentInstances = aktRunning;
            LOGGER.info("resource {} set init currentInstances {}", getName(), currentInstances);
        }
        for(int i=lastStartedIndex; i<lastStartedIndex+items.size(); i++) {
            if(aktRunning<currentInstances) {
                SingleState item = items.get(i%items.size());
                if(item.getRunning()==0) {
                    try{
                        item.setRunning(true);
                        item.checkState();
                        aktRunning++;
                    }catch (Exception e){
                        LOGGER.info("error start {} {}", item.getName(), e.getMessage());
                    }
                    lastStartedIndex = i%items.size();
                }
            }
        }
        for(int i=lastStartedIndex+1; i<lastStartedIndex+1+items.size(); i++) {
            if(aktRunning>currentInstances) {
                SingleState item = items.get(i%items.size());
                if(item.getRunning()==1) {
                    try{
                        item.setRunning(false);
                        item.checkState();
                        aktRunning--;
                    }catch (Exception e){
                        LOGGER.warn("error stop {} {}", item.getName(), e.getMessage());
                    }
                    lastStartedIndex = i%items.size();
                }
            }
        }
    }

    @Override
    public int getRunning() {
        return items.stream().mapToInt(SingleState::getRunning).sum();
    }

    private SingleState mapSingle(MultiItem item, MultiResource multiResource, StateStore stateStore) {
        SingleResource singleResource = new SingleResource();
        singleResource.setAuth(multiResource.getAuth());
        singleResource.setMinimalRunningTime(multiResource.getMinimalRunningTime());
        singleResource.setName(item.getName());
        singleResource.setParams(item.getParams());
        singleResource.setStart(item.getStart());
        singleResource.setStop(item.getStop());
        singleResource.setCheck(item.getCheck());
        singleResource.setFiles(multiResource.getFiles());
        return (SingleState) stateStore.addResource(singleResource);
    }
}
