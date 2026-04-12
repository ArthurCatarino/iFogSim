package org.fog.entities;

import org.fog.test.perfeval.testes.cluster.TipoSensor;
import org.fog.utils.distribution.Distribution;
import java.util.ArrayList;

public class SensorLessLatency extends Sensor {

  private ArrayList<FogDeviceWQLessLatency> destinos;
  
  public SensorLessLatency(String name, String tupleType, int userId, String appId, Distribution transmitDistribution,ArrayList<TipoSensor> tipos) {
        super(name,tupleType,userId,appId,transmitDistribution,tipos);
        destinos = new ArrayList<>();
      }
  
  @Override
  protected int calculaProximo(Tuple tuple) {
    FogDeviceWQLessLatency proximo = null;
    Double menor = Double.MAX_VALUE;
      for(FogDeviceWQLessLatency i : destinos){
        Double delay = super.calculaDelay(i.getId(),tuple);
        if((delay < menor) && (i.tupleQueue.size() < i.maxTupleQueueSize)) {
          menor = delay;
          proximo = i;
        }
      }
      if(proximo != null){
       return proximo.getId();
      }

      else {
        return -1;
      }
    }

  public void addDest(FogDeviceWQLessLatency device) {
    destinos.add(device);
  }
}
