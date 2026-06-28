package org.fog.entities;

import java.util.List;

import org.fog.test.perfeval.testes.TiposDispositivos;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.power.models.PowerModel;

public class FogDeviceWQLessLatency extends FogDeviceWithQueue {

  public FogDeviceWQLessLatency(String name, long mips, int ram, double uplinkBandwidth, double downlinkBandwidth, double ratePerMips, PowerModel powerModel,int queueSize,TiposDispositivos nodeType) throws Exception {
    super(name,mips,ram,uplinkBandwidth,downlinkBandwidth,ratePerMips,powerModel,queueSize,nodeType);
  }

  public FogDeviceWQLessLatency(
          String name,
          FogDeviceCharacteristics characteristics,
          VmAllocationPolicy vmAllocationPolicy,
          List<Storage> storageList,
          double schedulingInterval,
          double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency, double ratePerMips, int queueSize,TiposDispositivos nodeType) throws Exception {
              super(name,characteristics,vmAllocationPolicy,storageList,schedulingInterval,uplinkBandwidth,downlinkBandwidth,uplinkLatency,ratePerMips,queueSize,nodeType);

            }

  @Override
  public FogDeviceWithQueue calculaProximo(Tuple tuple) {
    Double menor = Double.MAX_VALUE;
    FogDeviceWithQueue proximo = null;
    double delay;
    for(FogDeviceWithQueue i : vizinhos){
      if(i.maxMipsQueueSize - i.mipsQueueSize < tuple.getCloudletLength()) {continue;} //Se o vizinho não tiver capacidade pra processar nem olha pra ele
      delay = super.calculaDelay(i.getId(), tuple);
      if((menor > delay) && ((i.maxMipsQueueSize - i.mipsQueueSize) > tuple.getCloudletLength() )) {
        if(i.getId() == tuple.getSourceDeviceId()) {continue;}
        menor = delay;
        proximo = i;
      }
    }
    return proximo;
  }

}
