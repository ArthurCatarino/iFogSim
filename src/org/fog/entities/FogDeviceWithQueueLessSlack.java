package org.fog.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.fog.test.perfeval.testes.TiposDispositivos;
import org.fog.test.perfeval.testes.cluster.Monitoramento;
import org.fog.utils.FogEvents;

public class FogDeviceWithQueueLessSlack extends FogDeviceWithQueue {

  public FogDeviceWithQueueLessSlack(String name, long mips, int ram, double uplinkBandwidth, double downlinkBandwidth, double ratePerMips, PowerModel powerModel,int queueSize,TiposDispositivos nodeType) throws Exception {
    super(name,mips,ram,uplinkBandwidth,downlinkBandwidth,ratePerMips,powerModel,queueSize, nodeType);

  }

  public FogDeviceWithQueueLessSlack(
          String name,
          FogDeviceCharacteristics characteristics,
          VmAllocationPolicy vmAllocationPolicy,
          List<Storage> storageList,
          double schedulingInterval,
          double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency, double ratePerMips, int queueSize,TiposDispositivos nodeType) throws Exception {
              super(name,characteristics,vmAllocationPolicy,storageList,schedulingInterval,uplinkBandwidth,downlinkBandwidth,uplinkLatency,ratePerMips,queueSize,nodeType);

            }

  @Override
  public FogDeviceWithQueue calculaProximo(Tuple tuple){
    long maior = -1;
    FogDeviceWithQueue proximo = null;
    for(FogDeviceWithQueue i : vizinhos){
      if(i.maxMipsQueueSize - i.mipsQueueSize < tuple.getCloudletLength()) {continue;} //Se o vizinho não tiver espaço na fila pula ele.
      if(i.maxMipsQueueSize - i.mipsQueueSize > maior) {
        if(i.getId() == tuple.getSourceDeviceId()) {continue;}
        maior = i.maxMipsQueueSize - i.mipsQueueSize;
        proximo = i;
      }
    }
    return proximo;
  }  
}
