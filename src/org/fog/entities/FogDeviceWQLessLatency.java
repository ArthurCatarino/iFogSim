package org.fog.entities;

import java.util.ArrayList;
import java.util.List;

import org.fog.test.perfeval.testes.cluster.Monitoramento;
import org.fog.utils.FogEvents;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
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

  public FogDeviceWQLessLatency calculaProximo(Tuple tuple) {
    return this.calculaProximo(tuple,vizinhos);
  }

  public FogDeviceWQLessLatency calculaProximo(Tuple tuple,ArrayList<FogDeviceWQLessLatency> lista ) {
    Double menor = Double.MAX_VALUE;
    FogDeviceWQLessLatency proximo = null;
    double delay;
    for(FogDeviceWQLessLatency i : lista){
      delay = super.calculaDelay(i.getId(), tuple);
      if((menor > delay) && (i.tupleQueue.size() != i.maxTupleQueueSize)) {
        if(i.getId() == tuple.getSourceDeviceId()) {continue;}
        menor = delay;
        proximo = i;
      }
    }
    return proximo;
  }

  protected void sendUp(Tuple tuple) {
      FogDeviceWQLessLatency proximo = calculaProximo(tuple,pais);
      Double delay;
      if(proximo == null) {
        int idPai = this.getParentId();
        delay = super.calculaDelay(idPai, tuple);
        Monitoramento.addUsoRede(tuple.getCloudletFileSize());
        Monitoramento.addTempoMedio(tuple.getActualTupleId(), delay);
        tuple.addLifetime(delay);
        send(idPai,delay,FogEvents.TUPLE_ARRIVAL,tuple);
      }
      else {
        delay = super.calculaDelay(proximo.getId(), tuple);
        Monitoramento.addUsoRede(tuple.getCloudletFileSize());
        Monitoramento.addTempoMedio(tuple.getActualTupleId(), delay);
        tuple.addLifetime(delay);
        send(proximo.getId(),delay,FogEvents.TUPLE_ARRIVAL,tuple);
      }
    
  }
}
