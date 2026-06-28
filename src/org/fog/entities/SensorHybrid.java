package org.fog.entities;

import org.fog.test.perfeval.testes.TipoSensor;
import org.fog.utils.distribution.Distribution;
import java.util.ArrayList;

public class SensorHybrid extends Sensor {

  private ArrayList<FogDeviceWQHybrid> destinos;
  private Double maiorDelay, menorDelay;
  private long maiorFolga, menorFolga;
  
  public SensorHybrid(String name, String tupleType, int userId, String appId, Distribution transmitDistribution,ArrayList<TipoSensor> tipos) {
        super(name,tupleType,userId,appId,transmitDistribution,tipos);
        destinos = new ArrayList<>();
  }

  public void addDest(FogDeviceWQHybrid device) {
    destinos.add(device);
  }

  private boolean calculaParametros(Tuple tuple) {
    maiorDelay = Double.MIN_VALUE;
    menorDelay = Double.MAX_VALUE;
    maiorFolga = Long.MIN_VALUE;
    menorFolga = Long.MAX_VALUE;

    long folga;
    Double delay;
    boolean encontrou = false;
    for(FogDeviceWQHybrid i : destinos) {
      folga = i.maxMipsQueueSize - i.mipsQueueSize;
      delay = super.calculaDelay(i.getId(), tuple);
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
  protected int calculaProximo(Tuple tuple) {
    if(!calculaParametros(tuple)){
      return -1;
    }

    long folga;
    Double Delay,score,folgaNormalizada, DelayNormalizada;
    double minMaxFolga = maiorFolga - menorFolga;
    Double minMaxDelay = maiorDelay - menorDelay;
    Double maiorScore = -1.0;
    FogDeviceWQHybrid proximo = null;

      if (minMaxFolga == 0) minMaxFolga = 1.0; // Evita NaN se todas as folgas forem iguais
      if (minMaxDelay <= 0.00001) minMaxDelay = 1.0; // Evita NaN se todas as latências forem iguais

    for(FogDeviceWQHybrid i : destinos) {
      if(i.mipsQueueSize + tuple.getCloudletLength() < i.maxMipsQueueSize) {
        folga = i.maxMipsQueueSize - i.mipsQueueSize;
        Delay = super.calculaDelay(i.getId(), tuple);
        folgaNormalizada = (folga - menorFolga)/minMaxFolga;
        DelayNormalizada = (maiorDelay - Delay)/minMaxDelay;
        score = (0.5*folgaNormalizada)+(0.5*DelayNormalizada);
        if(score > maiorScore) {
          maiorScore = score;
          proximo = i;
        }
      }
   }
    if(proximo != null) {
      return proximo.getId();
    }
    else {
      return -1;
    }
  }

   
}