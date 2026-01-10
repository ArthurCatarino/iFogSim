package org.fog.entities;

import java.util.ArrayList;
import java.util.List;

import org.fog.utils.FogEvents;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.fog.entities.dataEstructures.LatencyMatrix;
import org.fog.test.perfeval.testes.cluster.Monitoramento;

public class FogDeviceWithQueueLessSlack extends FogDeviceWithQueue {

  private ArrayList<FogDeviceWithQueueLessSlack> vizinhos = new ArrayList<>();

  public FogDeviceWithQueueLessSlack(String name, long mips, int ram, double uplinkBandwidth, double downlinkBandwidth, double ratePerMips, PowerModel powerModel,int queueSize) throws Exception {
    super(name,mips,ram,uplinkBandwidth,downlinkBandwidth,ratePerMips,powerModel,queueSize);

  }

  public FogDeviceWithQueueLessSlack(
          String name,
          FogDeviceCharacteristics characteristics,
          VmAllocationPolicy vmAllocationPolicy,
          List<Storage> storageList,
          double schedulingInterval,
          double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency, double ratePerMips, int queueSize) throws Exception {
              super(name,characteristics,vmAllocationPolicy,storageList,schedulingInterval,uplinkBandwidth,downlinkBandwidth,uplinkLatency,ratePerMips,queueSize);

            }

  public void addNeighbor(FogDeviceWithQueueLessSlack device) {
    vizinhos.add(device);
  }

   protected void updateQueue(SimEvent ev) {
    Tuple tuple = (Tuple) ev.getData(); 
    if((super.tupleQueue.size() >= super.maxTupleQueueSize) && (tuple.getDirection() != Tuple.ACTUATOR)) {
      Integer maior = -1;
      FogDeviceWithQueueLessSlack proximo = null;
      for(FogDeviceWithQueueLessSlack i : vizinhos){
        if((i.maxTupleQueueSize - i.tupleQueue.size() > maior) && (i.tupleQueue.size() != i.maxTupleQueueSize)) {
          maior = i.maxTupleQueueSize - i.tupleQueue.size();
          proximo = i;
        }
      }
      if(proximo != null){
       //System.out.println("Tupla redirecionada de " + this.getName() + " para " + proximo.getName());
        Double latency = LatencyMatrix.getLatency(this.getId(),proximo.getId());
        Monitoramento.addUsoRede(tuple.getCloudletFileSize());
        Monitoramento.addTempoMedio(tuple.getActualTupleId(), latency);
        send(proximo.getId(), latency, FogEvents.TUPLE_ARRIVAL, tuple);
      }
      else {
        int idPai = this.getParentId();

        if(idPai > 0) {
          Double latencia = LatencyMatrix.getLatency(this.getId(),this.getParentId());
          Monitoramento.addUsoRede(tuple.getCloudletFileSize());
          Monitoramento.addTempoMedio(tuple.getActualTupleId(), latencia);
          send(idPai,latencia,FogEvents.TUPLE_ARRIVAL,tuple);
        }
        else {
          Monitoramento.addTuplaPerdida();
        }
      }
    }
    else {
      Monitoramento.addUsoRede(tuple.getCloudletFileSize());
      super.updateQueue(ev);
    }
    
  }
}
