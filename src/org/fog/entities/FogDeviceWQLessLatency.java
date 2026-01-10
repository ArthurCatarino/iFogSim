package org.fog.entities;

import java.util.ArrayList;
import java.util.List;

import org.fog.entities.dataEstructures.LatencyMatrix;
import org.fog.test.perfeval.testes.cluster.Monitoramento;
import org.fog.utils.FogEvents;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.models.PowerModel;

public class FogDeviceWQLessLatency extends FogDeviceWithQueue {

  private ArrayList<FogDeviceWQLessLatency> vizinhos = new ArrayList<>();
  private ArrayList<FogDeviceWQLessLatency> pais = new ArrayList<>();

  public FogDeviceWQLessLatency(String name, long mips, int ram, double uplinkBandwidth, double downlinkBandwidth, double ratePerMips, PowerModel powerModel,int queueSize) throws Exception {
    super(name,mips,ram,uplinkBandwidth,downlinkBandwidth,ratePerMips,powerModel,queueSize);
  }

  public FogDeviceWQLessLatency(
          String name,
          FogDeviceCharacteristics characteristics,
          VmAllocationPolicy vmAllocationPolicy,
          List<Storage> storageList,
          double schedulingInterval,
          double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency, double ratePerMips, int queueSize) throws Exception {
              super(name,characteristics,vmAllocationPolicy,storageList,schedulingInterval,uplinkBandwidth,downlinkBandwidth,uplinkLatency,ratePerMips,queueSize);

            }

  public void addPais(FogDeviceWQLessLatency device) {
    pais.add(device);
  }
  
  public void addNeighbor(FogDeviceWQLessLatency device) {
    vizinhos.add(device);
  }

   protected void updateQueue(SimEvent ev) {
    Tuple tuple = (Tuple) ev.getData(); 
    if((super.tupleQueue.size() >= super.maxTupleQueueSize) && (tuple.getDirection() != Tuple.ACTUATOR)) {
      Double menor = Double.MAX_VALUE;
      FogDeviceWQLessLatency proximo = null;
      double latencia;
      for(FogDeviceWQLessLatency i : vizinhos){
        latencia = LatencyMatrix.getLatency(this.getId(), i.getId());
        if((menor > latencia) && (i.tupleQueue.size() != i.maxTupleQueueSize)) {
          menor = latencia;
          proximo = i;
        }
      }
      if(proximo != null){
        //System.out.println("Tupla redirecionada de " + this.getName() + " para " + proximo.getName());
        Monitoramento.addUsoRede(tuple.getCloudletFileSize());
        Monitoramento.addTempoMedio(tuple.getActualTupleId(), menor);
        send(proximo.getId(), menor, FogEvents.TUPLE_ARRIVAL, tuple);
      }
      else {
        sendUp(tuple); //Se nao tiver candidato envia pra algum pai
      }
    }
    else {
      Monitoramento.addUsoRede(tuple.getCloudletFileSize());
      super.updateQueue(ev);
    }
  }

  protected void sendUp(Tuple tuple) {
    if(this.getLevel() <= 0) {
      Monitoramento.addTuplaPerdida();
    }
    else {

      Double menor = Double.MAX_VALUE;
      FogDeviceWQLessLatency proximo = null;
      Double latencia;
      for(FogDeviceWQLessLatency p : pais) {
        latencia = LatencyMatrix.getLatency(this.getId(),p.getId());
        if(latencia < menor) {
          menor = latencia;
          proximo = p;
        } 
      }
      if(proximo == null) {
        int idPai = this.getParentId();
        latencia =  LatencyMatrix.getLatency(this.getId(),idPai);
        Monitoramento.addUsoRede(tuple.getCloudletFileSize());
        Monitoramento.addTempoMedio(tuple.getActualTupleId(), latencia);
        send(idPai,latencia,FogEvents.TUPLE_ARRIVAL,tuple);
      }
      else {
        Monitoramento.addUsoRede(tuple.getCloudletFileSize());
        Monitoramento.addTempoMedio(tuple.getActualTupleId(), menor);
        send(proximo.getId(),menor,FogEvents.TUPLE_ARRIVAL,tuple);
      }
    }
  }
}
