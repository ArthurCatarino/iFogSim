package org.fog.entities;

import org.fog.utils.distribution.Distribution;
import java.util.ArrayList;

import org.cloudbus.cloudsim.UtilizationModelFull;
import org.fog.application.AppEdge;
import org.fog.entities.dataEstructures.LatencyMatrix;
import org.fog.test.perfeval.testes.cluster.Monitoramento;
import org.fog.utils.*;

public class SensorLessSlack extends Sensor {

  private ArrayList<FogDeviceWithQueueLessSlack> destinos;
  
  public SensorLessSlack(String name, String tupleType, int userId, String appId, Distribution transmitDistribution) {
        super(name,tupleType,userId,appId,transmitDistribution);
        destinos = new ArrayList<>();
      }

  private int findDeviceWithLessSlack() {
    FogDeviceWithQueueLessSlack proximo = null;
    Integer maior = -1;
      for(FogDeviceWithQueueLessSlack i : destinos){
        if(i.maxTupleQueueSize - i.tupleQueue.size() > maior) {
          maior = i.maxTupleQueueSize - i.tupleQueue.size();
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

  public void transmit(){
		AppEdge _edge = null;
		for(AppEdge edge : getApp().getEdges()){
			if(edge.getSource().equals(getTupleType()))
				_edge = edge;
		}
		long cpuLength = (long) _edge.getTupleCpuLength();
		long nwLength = (long) _edge.getTupleNwLength();
		
		Tuple tuple = new Tuple(getAppId(), FogUtils.generateTupleId(), Tuple.UP, cpuLength, 1, nwLength, 3, 
				new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
		tuple.setUserId(getUserId());
		tuple.setTupleType(getTupleType());
		
		tuple.setDestModuleName(_edge.getDestination());
		tuple.setSrcModuleName(getSensorName());
    int proximo = findDeviceWithLessSlack();
    if(proximo != -1) {
		tuple.setDestinationDeviceId(proximo);
    }
    else {
      tuple.setDestinationDeviceId(getGatewayDeviceId());
    }
		int actualTupleId = updateTimings(getSensorName(), tuple.getDestModuleName());
		tuple.setActualTupleId(actualTupleId);

    Double latencia = LatencyMatrix.getLatency(-1*getId(),tuple.getDestinationDeviceId());
		
		Monitoramento.addUsoRede(tuple.getCloudletFileSize());
    Monitoramento.addTuplaEnviada();
    Monitoramento.addTempoMedio(tuple.getActualTupleId() ,latencia);
		send(tuple.getDestinationDeviceId(),latencia , FogEvents.TUPLE_ARRIVAL,tuple);
	}

  public void addDest(FogDeviceWithQueueLessSlack device) {
    destinos.add(device);
  }
}
