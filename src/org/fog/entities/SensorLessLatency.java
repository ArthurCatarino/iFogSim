package org.fog.entities;

import org.fog.utils.distribution.Distribution;
import java.util.ArrayList;

import org.cloudbus.cloudsim.UtilizationModelFull;
import org.fog.application.AppEdge;
import org.fog.entities.dataEstructures.NetworkMatrix;
import org.fog.test.perfeval.testes.cluster.Monitoramento;
import org.fog.utils.*;

public class SensorLessLatency extends Sensor {

  private ArrayList<FogDeviceWQLessLatency> destinos;
  
  public SensorLessLatency(String name, String tupleType, int userId, String appId, Distribution transmitDistribution) {
        super(name,tupleType,userId,appId,transmitDistribution);
        destinos = new ArrayList<>();
      }

  private int findDeviceWithLessLatency(Tuple tuple) {
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
    int proximo = findDeviceWithLessLatency(tuple);
    if(proximo != -1) {
		tuple.setDestinationDeviceId(proximo);
    }
    else {
      tuple.setDestinationDeviceId(getGatewayDeviceId());
    }
		int actualTupleId = updateTimings(getSensorName(), tuple.getDestModuleName());
		tuple.setActualTupleId(actualTupleId);

    Double delay = super.calculaDelay(tuple.getDestinationDeviceId(),tuple);
		
		Monitoramento.addUsoRede(tuple.getCloudletFileSize());
    Monitoramento.addTuplaEnviada();
    Monitoramento.addTempoMedio(tuple.getActualTupleId() ,delay);
    tuple.addLifetime(delay);
		send(tuple.getDestinationDeviceId(),delay , FogEvents.TUPLE_ARRIVAL,tuple);
	}

  public void addDest(FogDeviceWQLessLatency device) {
    destinos.add(device);
  }
}
