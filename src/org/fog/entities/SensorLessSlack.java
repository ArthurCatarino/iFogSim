package org.fog.entities;

import org.fog.utils.distribution.Distribution;
import org.fog.test.perfeval.testes.cluster.TipoSensor;
import java.util.ArrayList;


public class SensorLessSlack extends Sensor {

  private ArrayList<FogDeviceWithQueueLessSlack> destinos;
  
  public SensorLessSlack(String name, String tupleType, int userId, String appId, Distribution transmitDistribution,ArrayList<TipoSensor> tipos) {
        super(name,tupleType,userId,appId,transmitDistribution,tipos);
        destinos = new ArrayList<>();
      }

  @Override
  protected int calculaProximo(Tuple tuple) {
    FogDeviceWithQueueLessSlack proximo = null;
    long maior = -1;
      for(FogDeviceWithQueueLessSlack i : destinos){
        if(i.maxMipsQueueSize - i.mipsQueueSize > maior) {
          maior = i.maxMipsQueueSize - i.tupleQueue.size();
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

  public void addDest(FogDeviceWithQueueLessSlack device) {
    destinos.add(device);
  }
}
