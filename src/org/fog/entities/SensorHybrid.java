package org.fog.entities;

import org.fog.utils.distribution.Distribution;
import java.util.ArrayList;

import org.cloudbus.cloudsim.UtilizationModelFull;
import org.fog.application.AppEdge;
import org.fog.entities.dataEstructures.LatencyMatrix;
import org.fog.test.perfeval.testes.cluster.Monitoramento;
import org.fog.utils.*;

public class SensorHybrid extends Sensor {

  private ArrayList<FogDeviceWQHybrid> destinos;
  private Double maiorLatencia, menorLatencia;
  private int maiorFolga, menorFolga;
  
  public SensorHybrid(String name, String tupleType, int userId, String appId, Distribution transmitDistribution) {
        super(name,tupleType,userId,appId,transmitDistribution);
        destinos = new ArrayList<>();
  }

  public void addDest(FogDeviceWQHybrid device) {
    destinos.add(device);
  }

  private void resetaParametros(ArrayList<FogDeviceWQHybrid> lista) {
    maiorLatencia = Double.MIN_VALUE;
    menorLatencia = Double.MAX_VALUE;
    maiorFolga = Integer.MIN_VALUE;
    menorFolga = Integer.MAX_VALUE;
    calculaParametros(lista);
  }

  private void calculaParametros(ArrayList<FogDeviceWQHybrid> lista) {
    for(FogDeviceWQHybrid i : lista) {
      int folga = i.maxTupleQueueSize - i.tupleQueue.size();
      Double latencia = LatencyMatrix.getLatency(-1*this.getId(),i.getId());
      if(i.tupleQueue.size() < i.maxTupleQueueSize) { //Se o candidato estiver cheio nem precisa olhar
        if(folga > maiorFolga) {
          maiorFolga = folga;
        }
        if(folga < menorFolga) {
          menorFolga = folga;
        }
        if(latencia > maiorLatencia) {
          maiorLatencia = latencia;
        }
        if(latencia < menorLatencia) {
          menorLatencia = latencia;
        }
      }
    }
  }

  private int calculaProximo(ArrayList<FogDeviceWQHybrid> lista) {
    int folga;
    Double latencia,score,folgaNormalizada, latenciaNormalizada;
    double minMaxFolga = maiorFolga - menorFolga;
    Double minMaxLatencia = maiorLatencia - menorLatencia;
    Double maiorScore = Double.MIN_VALUE;
    FogDeviceWQHybrid proximo = null;

    for(FogDeviceWQHybrid i : lista) {
      if(i.tupleQueue.size() < i.maxTupleQueueSize) {
        folga = i.maxTupleQueueSize - i.tupleQueue.size();
        latencia = LatencyMatrix.getLatency(-1*this.getId(),i.getId());
        folgaNormalizada = (folga - menorFolga)/minMaxFolga;
        latenciaNormalizada = (maiorLatencia - latencia)/minMaxLatencia;
        score = (0.5*folgaNormalizada)+(0.5*latenciaNormalizada);
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
    resetaParametros(destinos);
    int proximo = calculaProximo(destinos);

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
}