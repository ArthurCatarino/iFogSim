package org.fog.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.dataEstructures.NetworkMatrix;
import org.fog.test.perfeval.testes.cluster.Monitoramento;
import org.fog.test.perfeval.testes.ColetaDados;
import org.fog.test.perfeval.testes.TipoSensor;
import org.fog.test.perfeval.testes.TiposDispositivos;
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
  protected TiposDispositivos nodeType;
  protected long maxMipsQueueSize;
  protected long mipsQueueSize;
  protected Queue<SimEvent> tupleQueue;
  protected double localMIPSAvailable;
  protected double localCPUUtilization;
  protected ArrayList<FogDeviceWithQueue> vizinhos = new ArrayList<>();
  protected ArrayList<FogDeviceWithQueue> pais = new ArrayList<>();

  public FogDeviceWithQueue(String name, long mips, int ram, double uplinkBandwidth, double downlinkBandwidth, double ratePerMips, PowerModel powerModel,int mipsQueueSize,TiposDispositivos nodeType) throws Exception {
    super(name,mips,ram,uplinkBandwidth,downlinkBandwidth,ratePerMips,powerModel);
    tupleQueue = new LinkedList<>();
    maxMipsQueueSize = mipsQueueSize;
    this.mipsQueueSize = 0;
    this.nodeType = nodeType;
    this.vizinhos = new ArrayList<>();
    this.pais = new ArrayList<>();
  }

  public FogDeviceWithQueue(
            String name,
            FogDeviceCharacteristics characteristics,
            VmAllocationPolicy vmAllocationPolicy,
            List<Storage> storageList,
            double schedulingInterval,
            double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency, double ratePerMips, int mipsQueueSize, TiposDispositivos nodeType) throws Exception {
              super(name,characteristics,vmAllocationPolicy,storageList,schedulingInterval,uplinkBandwidth,downlinkBandwidth,uplinkLatency,ratePerMips);
              tupleQueue = new LinkedList<>();
              maxMipsQueueSize = mipsQueueSize;
              this.mipsQueueSize = 0;
              this.nodeType = nodeType;
              this.vizinhos = new ArrayList<>();
              this.pais = new ArrayList<>();
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

  protected FogDeviceWithQueue calculaProximo(Tuple tuple){
    return null;
  }

  protected void updateQueue(SimEvent ev) {
    Tuple tuple = (Tuple) ev.getData(); 
    TipoSensor tipoRequisicao = TipoSensor.valueOf(tuple.getTupleType());
    localMIPSAvailable =  getLocalMipsAvailable();
    localCPUUtilization = getLocalCPUUTIlization();

    if(tuple.getLifeTime() > 300) { // Limite de tempo por tupla
      Monitoramento.addTuplaPerdida("Tempo Limite");
      Monitoramento.addTempoMedio(tuple.getCloudletId(),CloudSim.clock() - tuple.getEmitTupleTime());
      this.makeTuplaData(tuple, "TIMELIMIT", -1);
      return;
    }
    if(mipsQueueSize + tipoRequisicao.getMips() > maxMipsQueueSize && tuple.getDirection() != Tuple.ACTUATOR) {  
      if(this.getLevel() <= 0){ // a nuvem nao tem pra quem redirecionar, ela e o ultimo recurso.
        Monitoramento.addTuplaPerdida("Nuvem Perdeu");
        Monitoramento.addTempoMedio(tuple.getCloudletId(),CloudSim.clock() - tuple.getEmitTupleTime());
        this.makeTuplaData(tuple, "Lost", -1);
        return;
      }

      FogDeviceWithQueue proximo = null;
      proximo = calculaProximo(tuple);

      if(proximo != null){
        Double delayTotal = calculaDelay(proximo.getId(),tuple);
        Monitoramento.addUsoRede(tuple.getCloudletFileSize());
        tuple.addLifetime(delayTotal);
        this.makeTuplaData(tuple, "Redirected", proximo.getId());
        Monitoramento.addTuplaRedirecionada();
        String idLink = String.valueOf(this.getId()) + "-" + String.valueOf(proximo.getId());
        Monitoramento.adicionaTrafegoNoLink(idLink, tuple.getCloudletFileSize());
        send(proximo.getId(), delayTotal, FogEvents.TUPLE_ARRIVAL, tuple);
      }
      else{
        if(pais.size() <= 0) {
          Monitoramento.addTuplaPerdida("Sem vizinhos e pais disponiveis");
          Monitoramento.addTempoMedio(tuple.getCloudletId(),CloudSim.clock() - tuple.getEmitTupleTime());
          this.makeTuplaData(tuple, "Lost",-1);
        }
        else {
          proximo = pais.get(0);
          Double delay = calculaDelay(proximo.getId(), tuple);
          Monitoramento.addUsoRede(tuple.getCloudletFileSize());
          tuple.addLifetime(delay);
          this.makeTuplaData(tuple, "Redirected", proximo.getId());
          String idLink = String.valueOf(this.getId()) + "-" + String.valueOf(proximo.getId());
          Monitoramento.adicionaTrafegoNoLink(idLink, tuple.getCloudletFileSize());
          Monitoramento.addTuplaRedirecionada();
          send(proximo.getId(),delay,FogEvents.TUPLE_ARRIVAL,tuple);
        }
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
                  this.makeTuplaData(tuple, "Completed", -1);
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

  private void makeTuplaData(Tuple tuple, String status, int dest) {
		int tupleId = tuple.getActualTupleId();
		String tupleType  = tuple.getTupleType();
		String tupleSource = tuple.getSrcModuleName();
		long mipsRequired = tuple.getCloudletLength();
		long networkSize = tuple.getCloudletFileSize();

		int nodeId = this.getId();
    long queueSize = this.mipsQueueSize;
		double localMIPSAvailable = this.localMIPSAvailable;
		double localCPUUtilization = this.localCPUUtilization;
		TiposDispositivos nodeType = this.nodeType;

		ArrayList<Integer> neighborId = new ArrayList<>();
		ArrayList<TiposDispositivos> neighborType = new ArrayList<>();
		ArrayList<Double> neighborMips = new ArrayList<>();
    ArrayList<Double> neighborCPUutilization = new ArrayList<>();
		ArrayList<Long> neighborQueueSize = new ArrayList<>();

    for(FogDeviceWithQueue f : vizinhos) {
      neighborId.add(f.getId());
      neighborType.add(f.getDeviceType());
      neighborMips.add(f.getLocalMipsAvailable());
      neighborCPUutilization.add(f.getLocalCPUUTIlization());
      neighborQueueSize.add(f.mipsQueueSize);
    }

		int destNode = dest;
		double totalLatency = CloudSim.clock() - tuple.getEmitTupleTime();
		String finalStatus = status;

    ColetaDados registroAtual = new ColetaDados(tupleId,tupleType,tupleSource,mipsRequired, networkSize, nodeId, queueSize, localMIPSAvailable, localCPUUtilization, nodeType,neighborId,neighborType,neighborMips,neighborCPUutilization,neighborQueueSize,destNode,totalLatency,finalStatus);

    Monitoramento.addInfoTupla(registroAtual);
	}

  public void addPais(FogDeviceWithQueue device) {
    pais.add(device);
  }
  
  public void addNeighbor(FogDeviceWithQueue device) {
    vizinhos.add(device);
  }

  public TiposDispositivos getDeviceType() {
    return this.nodeType;
  }

  public double getLocalMipsAvailable() {
    return getHost().getAvailableMips();
  } 
  
  public double getLocalCPUUTIlization () {
    Host maquinaFisica = getHost();
    localCPUUtilization = (maquinaFisica.getTotalMips() - maquinaFisica.getAvailableMips())*100/maquinaFisica.getTotalMips();
    return localCPUUtilization;
  } 
}
