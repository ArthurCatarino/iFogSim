package org.fog.entities;

import java.util.List;

import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.fog.test.perfeval.testes.TiposDispositivos;

public class FogDeviceWQHybrid extends FogDeviceWithQueue {

  private Double maiorDelay, menorDelay;
  private long maiorFolga, menorFolga;

  public FogDeviceWQHybrid(String name, long mips, int ram, double uplinkBandwidth, double downlinkBandwidth, double ratePerMips, PowerModel powerModel,int queueSize,TiposDispositivos nodeType) throws Exception {
    super(name,mips,ram,uplinkBandwidth,downlinkBandwidth,ratePerMips,powerModel,queueSize,nodeType);
  }

  public FogDeviceWQHybrid(
          String name,
          FogDeviceCharacteristics characteristics,
          VmAllocationPolicy vmAllocationPolicy,
          List<Storage> storageList,
          double schedulingInterval,
          double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency, double ratePerMips, int queueSize,TiposDispositivos nodeType) throws Exception {
              super(name,characteristics,vmAllocationPolicy,storageList,schedulingInterval,uplinkBandwidth,downlinkBandwidth,uplinkLatency,ratePerMips,queueSize,nodeType);

            }

  private boolean calculaParametros(Tuple tuple) {
    maiorDelay = Double.MIN_VALUE;
    menorDelay = Double.MAX_VALUE;
    maiorFolga = Long.MIN_VALUE;
    menorFolga = Long.MAX_VALUE;
    boolean encontrou = false;

    for(FogDeviceWithQueue i : vizinhos) {
      long folga = i.maxMipsQueueSize - i.mipsQueueSize;
      Double delay = super.calculaDelay(i.getId(),tuple);
      if(i.mipsQueueSize + tuple.getCloudletLength() < i.maxMipsQueueSize) { //Se o candidato estiver cheio nem precisa olhar
        if(folga > maiorFolga) {
          maiorFolga = folga;
        }
        if(folga < menorFolga) {
          menorFolga = folga;
        }
        if(delay > maiorDelay) {
          maiorDelay = delay;
        }
        if(delay < menorDelay) {
          menorDelay = delay;
        }
        encontrou = true;
      }
    }
    return encontrou;
  }

  @Override
  protected FogDeviceWithQueue calculaProximo(Tuple tuple) {
    if (!calculaParametros(tuple)) {
        return null; // Ninguém disponível ou configurado
    }

    long folga;
    Double score,delayTotal,folgaNormalizada, delayNormalizado;
    double minMaxFolga = maiorFolga - menorFolga;
    Double minMaxDelay = maiorDelay - menorDelay;
    Double maiorScore = -1.0;
    FogDeviceWithQueue proximo = null;

      if (minMaxFolga == 0) minMaxFolga = 1.0; // Evita NaN se todas as folgas forem iguais
      if (minMaxDelay <= 0.00001) minMaxDelay = 1.0; // Evita NaN se todas as latências forem iguais

    for(FogDeviceWithQueue i : vizinhos) {
      if(i.mipsQueueSize + tuple.getCloudletLength() < i.maxMipsQueueSize) {
        if(i.getId() == tuple.getSourceDeviceId()) {continue;} 
        folga = i.maxMipsQueueSize - i.mipsQueueSize;

        delayTotal = super.calculaDelay(i.getId(),tuple);

        folgaNormalizada = (folga - menorFolga)/minMaxFolga;
        delayNormalizado = (maiorDelay - delayTotal)/minMaxDelay;
        score = (0.5*folgaNormalizada)+(0.5*delayNormalizado);
        if(score > maiorScore) {
          maiorScore = score;
          proximo = i;
        } 
      }
    }
    return proximo;
  }
  
}
