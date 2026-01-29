package org.fog.entities;

import org.fog.utils.distribution.Distribution;
import java.util.ArrayList;

import org.cloudbus.cloudsim.UtilizationModelFull;
import org.fog.application.AppEdge;
import org.fog.test.perfeval.testes.cluster.Monitoramento;
import org.fog.utils.*;

public class SensorHybrid extends Sensor {

  private ArrayList<FogDeviceWQHybrid> destinos;
  private Double maiorDelay, menorDelay;
  private int maiorFolga, menorFolga;
  
  public SensorHybrid(String name, String tupleType, int userId, String appId, Distribution transmitDistribution) {
        super(name,tupleType,userId,appId,transmitDistribution);
        destinos = new ArrayList<>();
  }

  public void addDest(FogDeviceWQHybrid device) {
    destinos.add(device);
  }

  private boolean calculaParametros(ArrayList<FogDeviceWQHybrid> lista,Tuple tuple) {
    maiorDelay = Double.MIN_VALUE;
    menorDelay = Double.MAX_VALUE;
    maiorFolga = Integer.MIN_VALUE;
    menorFolga = Integer.MAX_VALUE;

    int folga;
    Double delay;
    boolean encontrou = false;
    for(FogDeviceWQHybrid i : lista) {
      folga = i.maxTupleQueueSize - i.tupleQueue.size();
      delay = super.calculaDelay(i.getId(), tuple);
      if(i.tupleQueue.size() < i.maxTupleQueueSize) { //Se o candidato estiver cheio nem precisa olhar
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

  private int calculaProximo(ArrayList<FogDeviceWQHybrid> lista, Tuple tuple) {
    if(!calculaParametros(lista, tuple)){
      return -1;
    }

    int folga;
    Double Delay,score,folgaNormalizada, DelayNormalizada;
    double minMaxFolga = maiorFolga - menorFolga;
    Double minMaxDelay = maiorDelay - menorDelay;
    Double maiorScore = -1.0;
    FogDeviceWQHybrid proximo = null;

      if (minMaxFolga == 0) minMaxFolga = 1.0; // Evita NaN se todas as folgas forem iguais
      if (minMaxDelay <= 0.00001) minMaxDelay = 1.0; // Evita NaN se todas as latências forem iguais

    for(FogDeviceWQHybrid i : lista) {
      if(i.tupleQueue.size() < i.maxTupleQueueSize) {
        folga = i.maxTupleQueueSize - i.tupleQueue.size();
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
    int proximo = calculaProximo(destinos,tuple);

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
}