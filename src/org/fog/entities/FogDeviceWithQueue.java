package org.fog.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.dataEstructures.NetworkMatrix;
import org.fog.test.perfeval.testes.LogsReport;
import org.fog.test.perfeval.testes.cluster.Monitoramento;
import org.fog.test.perfeval.testes.TipoSensor;
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
  protected long maxMipsQueueSize;
  protected long mipsQueueSize;
  protected Queue<SimEvent> tupleQueue;

  public FogDeviceWithQueue(String name, long mips, int ram, double uplinkBandwidth, double downlinkBandwidth, double ratePerMips, PowerModel powerModel,int mipsQueueSize) throws Exception {
    super(name,mips,ram,uplinkBandwidth,downlinkBandwidth,ratePerMips,powerModel);
    tupleQueue = new LinkedList<>();
    maxMipsQueueSize = mipsQueueSize;
    this.mipsQueueSize = 0;
  }

  public FogDeviceWithQueue(
            String name,
            FogDeviceCharacteristics characteristics,
            VmAllocationPolicy vmAllocationPolicy,
            List<Storage> storageList,
            double schedulingInterval,
            double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency, double ratePerMips, int mipsQueueSize) throws Exception {
              super(name,characteristics,vmAllocationPolicy,storageList,schedulingInterval,uplinkBandwidth,downlinkBandwidth,uplinkLatency,ratePerMips);
              tupleQueue = new LinkedList<>();
              maxMipsQueueSize = mipsQueueSize;
              this.mipsQueueSize = 0;
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

  private FogDeviceWithQueue calculaProximo(Tuple tuple){
    return null;
  }

  protected void updateQueue(SimEvent ev) {
    Tuple tuple = (Tuple) ev.getData(); 
    TipoSensor tipoRequisicao = TipoSensor.valueOf(tuple.getTupleType());

    if(tuple.getLifeTime() > 50) { // Limite de tempo por tupla
      Monitoramento.addTuplaPerdida();
      Monitoramento.addTempoMedio(tuple.getCloudletId(),CloudSim.clock() - tuple.getEmitTupleTime());
      return;
    }
    if(mipsQueueSize + tipoRequisicao.getMips() > maxMipsQueueSize && tuple.getDirection() != Tuple.ACTUATOR) {  
      if(this.getLevel() <= 0){ // a nuvem nao tem pra quem redirecionar, ela e o ultimo recurso.
        Monitoramento.addTuplaPerdida();
        Monitoramento.addTempoMedio(tuple.getCloudletId(),CloudSim.clock() - tuple.getEmitTupleTime());
        return;
      }

      FogDeviceWithQueue proximo = null;
      proximo = calculaProximo(tuple);

      if(proximo != null){
        Double delayTotal = calculaDelay(proximo.getId(),tuple);
        Monitoramento.addUsoRede(tuple.getCloudletFileSize());
        tuple.addLifetime(delayTotal);
        send(proximo.getId(), delayTotal, FogEvents.TUPLE_ARRIVAL, tuple);
      }
      else{
        sendUp(tuple);
      }
    
    }
    else{
      boolean wasEmpty = tupleQueue.isEmpty();
      mipsQueueSize += tipoRequisicao.getMips();
      tupleQueue.add(ev);
      if(wasEmpty) {
        processTupleArrival();
      }
    }
  }
 @Override
  protected void checkCloudletCompletion() {
    boolean cloudletCompleted = false;
    Tuple tuplaAtual = null;
    List<? extends Host> list = getVmAllocationPolicy().getHostList();
    for (int i = 0; i < list.size(); i++) {
        Host host = list.get(i);
        for (Vm vm : host.getVmList()) {
            while (vm.getCloudletScheduler().isFinishedCloudlets()) {
                Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
                if (cl != null) {
                  cloudletCompleted = true;
                  Tuple tuple = (Tuple) cl;
                  tuplaAtual = tuple;
                  double tempoVidaTotal = CloudSim.clock() - tuple.getEmitTupleTime();
                  Monitoramento.addTempoMedio(tuple.getCloudletId(),tempoVidaTotal);
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
      long mipsTupla = TipoSensor.valueOf(tuplaAtual.getTupleType()).getMips();
      mipsQueueSize -= mipsTupla;
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
    long mipsTupla = TipoSensor.valueOf(tuple.getTupleType()).getMips();
    LogsReport.fogsLogs(getName(),tuple.getCloudletId(),tuple,getLevel());

    if (getName().equals("cloud")) {
      updateCloudTraffic();
    }	
    send(ev.getSource(), CloudSim.getMinTimeBetweenEvents(), FogEvents.TUPLE_ACK);

    if (tuple.getDirection() == Tuple.ACTUATOR) {
      sendTupleToActuator(tuple);
      tupleQueue.poll();
      mipsQueueSize -= mipsTupla;
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
              mipsQueueSize -= mipsTupla;
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
            mipsQueueSize -= mipsTupla;
            processTupleArrival();
        } else {
            if (tuple.getDirection() == Tuple.UP)
              sendUp(tuple);
            else if (tuple.getDirection() == Tuple.DOWN) {
              for (int childId : getChildrenIds())
                sendDown(tuple, childId);
            }
          tupleQueue.poll();
          mipsQueueSize -= mipsTupla;
          processTupleArrival();
        }
    }
}

  protected double calculaDelay(int proximo, Tuple tuple) {
    Double latency = NetworkMatrix.getLatency(this.getId(),proximo);
    Double banda = NetworkMatrix.getBand(this.getId(),proximo);
    Double tempoTransmissao = tuple.getCloudletFileSize() / banda;
    return latency + tempoTransmissao;
  }

}
