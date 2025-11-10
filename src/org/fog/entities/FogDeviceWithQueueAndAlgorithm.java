package org.fog.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.test.perfeval.testes.LogsReport;
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

public class FogDeviceWithQueueAndAlgorithm extends FogDeviceWithQueue {

  private static HashMap<FogDeviceWithQueueAndAlgorithm,Float> map = new HashMap<>();

  public FogDeviceWithQueueAndAlgorithm(String name, long mips, int ram, double uplinkBandwidth, double downlinkBandwidth, double ratePerMips, PowerModel powerModel,int queueSize) throws Exception {
    super(name,mips,ram,uplinkBandwidth,downlinkBandwidth,ratePerMips,powerModel,queueSize);
    startMap();
  }

  public FogDeviceWithQueueAndAlgorithm(
          String name,
          FogDeviceCharacteristics characteristics,
          VmAllocationPolicy vmAllocationPolicy,
          List<Storage> storageList,
          double schedulingInterval,
          double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency, double ratePerMips, int queueSize) throws Exception {
              super(name,characteristics,vmAllocationPolicy,storageList,schedulingInterval,uplinkBandwidth,downlinkBandwidth,uplinkLatency,ratePerMips,queueSize);
              startMap();

            }

  private void startMap() {
    float valor = 0;
    map.put(this,valor);
  }

   protected void updateQueue(SimEvent ev) {
    Tuple tuple = (Tuple) ev.getData(); 
    if((super.tupleQueue.size() >= super.maxTupleQueueSize) && (tuple.getDirection() != Tuple.ACTUATOR)) {
      float porcentagem = map.get(this);
      map.put(this,porcentagem+1);
      float menor = 999999999;
      FogDeviceWithQueueAndAlgorithm proximo = null;
      for(FogDeviceWithQueueAndAlgorithm i : map.keySet()){
        if(map.get(i) < menor) {
          menor = map.get(i);
          proximo = i;
        }
      }
      if((proximo.getName() != this.getName()) && (proximo != null) ){
        System.out.println("Tupla redirecionada de " + this.getName() + " para " + proximo.getName());
        proximo.processOtherEvent(ev);
      }
    }
    else {
      super.updateQueue(ev);
    }
  }
}
