package org.fog.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.test.perfeval.testes.exemplo2.LogsReport;
import org.fog.utils.FogEvents;
import org.fog.utils.TimeKeeper;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.models.PowerModel;

public class FogDeviceWithQueue extends FogDevice {
  private int maxTupleQueueSize;
  private Queue<SimEvent> tupleQueue;

  public FogDeviceWithQueue(String name, long mips, int ram, double uplinkBandwidth, double downlinkBandwidth, double ratePerMips, PowerModel powerModel,int queueSize) throws Exception {
    super(name,mips,ram,uplinkBandwidth,downlinkBandwidth,ratePerMips,powerModel);
    tupleQueue = new LinkedList<>();
    maxTupleQueueSize = queueSize;
  }

  public FogDeviceWithQueue(
            String name,
            FogDeviceCharacteristics characteristics,
            VmAllocationPolicy vmAllocationPolicy,
            List<Storage> storageList,
            double schedulingInterval,
            double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency, double ratePerMips, int queueSize) throws Exception {
              super(name,characteristics,vmAllocationPolicy,storageList,schedulingInterval,uplinkBandwidth,downlinkBandwidth,uplinkLatency,ratePerMips);
              tupleQueue = new LinkedList<>();
              maxTupleQueueSize = queueSize;
            }

  @Override
  protected void processOtherEvent(SimEvent ev) {
    if(ev.getTag() == FogEvents.TUPLE_ARRIVAL) {
      updateQueue(ev);
    }
    else {
      super.processOtherEvent(ev);
    }
    }

  protected void updateQueue(SimEvent ev) {
    if(tupleQueue.size() >= maxTupleQueueSize) {
      Tuple tuple = (Tuple) ev.getData();      
      if(tuple.getDirection() == Tuple.ACTUATOR) {
        tupleQueue.add(ev);
        processTupleArrival();
      }
      else {
      LogsReport.lossPacketReport(getLevel(),tuple.getActualTupleId());
      }
      return;
    }
    boolean wasEmpty = tupleQueue.isEmpty();
    tupleQueue.add(ev);
    if(wasEmpty) {
      processTupleArrival();
    }
  }
 @Override
  protected void checkCloudletCompletion() {
    boolean cloudletCompleted = false;
    List<? extends Host> list = getVmAllocationPolicy().getHostList();
    for (int i = 0; i < list.size(); i++) {
        Host host = list.get(i);
        for (Vm vm : host.getVmList()) {
            while (vm.getCloudletScheduler().isFinishedCloudlets()) {
                Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
                if (cl != null) {
                  cloudletCompleted = true;
                  Tuple tuple = (Tuple) cl;
                  TimeKeeper.getInstance().tupleEndedExecution(tuple);
                  Application application = getApplicationMap().get(tuple.getAppId());
                  //Logger.debug(getName(), "Completed execution of tuple " + tuple.getCloudletId() + "on " + tuple.getDestModuleName());
                  List<Tuple> resultantTuples = application.getResultantTuples(tuple.getDestModuleName(), tuple, getId(), vm.getId());
                  for (Tuple resTuple : resultantTuples) {
                    resTuple.setModuleCopyMap(new HashMap<String, Integer>(tuple.getModuleCopyMap()));
                    resTuple.getModuleCopyMap().put(((AppModule) vm).getName(), vm.getId());
                    updateTimingsOnSending(resTuple);
                    sendToSelf(resTuple);
                  }
                  sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
                }
            }
        }
    }
    if (cloudletCompleted){
      updateAllocatedMips(null);
      tupleQueue.poll();
      if(!tupleQueue.isEmpty()){
        processTupleArrival();
      }
    }
  }

  protected void processTupleArrival() {
    if(tupleQueue.isEmpty()) {
        return;
    }
    
    SimEvent ev = tupleQueue.peek();
    Tuple tuple = (Tuple) ev.getData();
    LogsReport.fogsLogs(getName(),tuple.getActualTupleId(),tuple,getLevel());

    if (getName().equals("cloud")) {
      updateCloudTraffic();
    }	
    send(ev.getSource(), CloudSim.getMinTimeBetweenEvents(), FogEvents.TUPLE_ACK);

    if (tuple.getDirection() == Tuple.ACTUATOR) {
      sendTupleToActuator(tuple);
      tupleQueue.poll();
      processTupleArrival();
      return;
    }

    if (getHost().getVmList().size() > 0) {
      final AppModule operator = (AppModule) getHost().getVmList().get(0);
      if (CloudSim.clock() > 0) {
        getHost().getVmScheduler().deallocatePesForVm(operator);
        getHost().getVmScheduler().allocatePesForVm(operator, new ArrayList<Double>() {
            protected static final long serialVersionUID = 1L;
            {
                add((double) getHost().getTotalMips());
            }
        });
      }
    }
    if (getName().equals("cloud") && tuple.getDestModuleName() == null) {
      sendNow(getControllerId(), FogEvents.TUPLE_FINISHED, null);
    }

    if (appToModulesMap.containsKey(tuple.getAppId())) {
      if(appToModulesMap.get(tuple.getAppId()).contains(tuple.getDestModuleName())) {
        int vmId = -1;
        for (Vm vm : getHost().getVmList()) {
          if (((AppModule) vm).getName().equals(tuple.getDestModuleName()))
            vmId = vm.getId();
          }
          if (vmId < 0
              || (tuple.getModuleCopyMap().containsKey(tuple.getDestModuleName()) &&
              tuple.getModuleCopyMap().get(tuple.getDestModuleName()) != vmId)) {
          
              tupleQueue.poll();
              processTupleArrival();
              return;
            }

            tuple.setVmId(vmId);
            updateTimingsOnReceipt(tuple);

            executeTuple(ev, tuple.getDestModuleName());

      } else if (tuple.getDestModuleName() != null) {
          if (tuple.getDirection() == Tuple.UP)
                sendUp(tuple);
          else if (tuple.getDirection() == Tuple.DOWN) {
            for (int childId : getChildrenIds())
              sendDown(tuple, childId);
            }
            tupleQueue.poll();
            processTupleArrival();
        } else {
            if (tuple.getDirection() == Tuple.UP)
              sendUp(tuple);
            else if (tuple.getDirection() == Tuple.DOWN) {
              for (int childId : getChildrenIds())
                sendDown(tuple, childId);
            }
          tupleQueue.poll();
          processTupleArrival();
        }
    }
}

}
