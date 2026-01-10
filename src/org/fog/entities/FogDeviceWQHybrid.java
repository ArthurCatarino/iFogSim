package org.fog.entities;

import java.util.ArrayList;
import java.util.List;

import org.fog.utils.FogEvents;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.fog.entities.dataEstructures.LatencyMatrix;
import org.fog.test.perfeval.testes.cluster.Monitoramento;

public class FogDeviceWQHybrid extends FogDeviceWithQueue {
;
  private ArrayList<FogDeviceWQHybrid> vizinhos = new ArrayList<>();
  private ArrayList<FogDeviceWQHybrid> pais = new ArrayList<>();
  private Double maiorLatencia, menorLatencia;
  private int maiorFolga, menorFolga;
 
  public FogDeviceWQHybrid(String name, long mips, int ram, double uplinkBandwidth, double downlinkBandwidth, double ratePerMips, PowerModel powerModel,int queueSize) throws Exception {
    super(name,mips,ram,uplinkBandwidth,downlinkBandwidth,ratePerMips,powerModel,queueSize);
  }

  public FogDeviceWQHybrid(
          String name,
          FogDeviceCharacteristics characteristics,
          VmAllocationPolicy vmAllocationPolicy,
          List<Storage> storageList,
          double schedulingInterval,
          double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency, double ratePerMips, int queueSize) throws Exception {
              super(name,characteristics,vmAllocationPolicy,storageList,schedulingInterval,uplinkBandwidth,downlinkBandwidth,uplinkLatency,ratePerMips,queueSize);

            }

  public void addNeighbor(FogDeviceWQHybrid device) {
    vizinhos.add(device);
  }

  private boolean calculaParametros(ArrayList<FogDeviceWQHybrid> lista) {
    maiorLatencia = Double.MIN_VALUE;
    menorLatencia = Double.MAX_VALUE;
    maiorFolga = Integer.MIN_VALUE;
    menorFolga = Integer.MAX_VALUE;
    boolean encontrou = false;

    for(FogDeviceWQHybrid i : lista) {
      int folga = i.maxTupleQueueSize - i.tupleQueue.size();
      Double latencia = LatencyMatrix.getLatency(this.getId(),i.getId());
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
        encontrou = true;
      }
    }
    return encontrou;
  }

  public void addPais(FogDeviceWQHybrid device) {
    pais.add(device);
  }

  private FogDeviceWQHybrid calculaProximo(ArrayList<FogDeviceWQHybrid> lista) {
    if (!calculaParametros(lista)) {
        return null; // Ninguém disponível ou configurado
    }

    int folga;
    Double latencia,score,folgaNormalizada, latenciaNormalizada;
    double minMaxFolga = maiorFolga - menorFolga;
    Double minMaxLatencia = maiorLatencia - menorLatencia;
    Double maiorScore = -1.0;
    FogDeviceWQHybrid proximo = null;
    double rangeFolga = maiorFolga - menorFolga;  
      if (rangeFolga == 0) rangeFolga = 1.0; // Evita NaN se todas as folgas forem iguais

    double rangeLatencia = maiorLatencia - menorLatencia;
      if (rangeLatencia == 0) rangeLatencia = 1.0; // Evita NaN se todas as latências forem iguais

    for(FogDeviceWQHybrid i : lista) {
      if(i.tupleQueue.size() < i.maxTupleQueueSize) {
        folga = i.maxTupleQueueSize - i.tupleQueue.size();
        latencia = LatencyMatrix.getLatency(this.getId(),i.getId());
        folgaNormalizada = (folga - menorFolga)/minMaxFolga;
        latenciaNormalizada = (maiorLatencia - latencia)/minMaxLatencia;
        score = (0.5*folgaNormalizada)+(0.5*latenciaNormalizada);
        if(score > maiorScore) {
          maiorScore = score;
          proximo = i;
        } 
      }
    }
    return proximo;
  }

  protected void updateQueue(SimEvent ev) {
    Tuple tuple = (Tuple) ev.getData(); 
    if((super.tupleQueue.size() >= super.maxTupleQueueSize) && (tuple.getDirection() != Tuple.ACTUATOR)) {
      if(this.getLevel() <= 0){ // a nuvem nao tem pra quem redirecionar, ela e o ultimo recurso.
        Monitoramento.addTuplaPerdida();
      }
      else {
        FogDeviceWQHybrid proximo = null;
        proximo = calculaProximo(vizinhos);

        if(proximo != null){
          Double latency = LatencyMatrix.getLatency(this.getId(),proximo.getId());
          Monitoramento.addUsoRede(tuple.getCloudletFileSize());
          Monitoramento.addTempoMedio(tuple.getActualTupleId(), latency);
          send(proximo.getId(), latency, FogEvents.TUPLE_ARRIVAL, tuple);
        }
        else{
          sendUp(tuple);
        }
      }
    }
    else {
      super.processTupleArrival(ev);
    }
  }

  protected void sendUp(Tuple tuple) {
    FogDeviceWQHybrid proximo;
    proximo = calculaProximo(pais);

    if(proximo == null) {
      int idPai = this.getParentId();
      Double latencia = LatencyMatrix.getLatency(this.getId(),idPai);
      Monitoramento.addUsoRede(tuple.getCloudletFileSize());
      Monitoramento.addTempoMedio(tuple.getActualTupleId(), latencia);
      send(idPai,latencia,FogEvents.TUPLE_ARRIVAL,tuple);
    }
    else {
      Double latencia = LatencyMatrix.getLatency(this.getId(),proximo.getId());
      Monitoramento.addUsoRede(tuple.getCloudletFileSize());
      Monitoramento.addTempoMedio(tuple.getActualTupleId(), latencia);
      send(proximo.getId(),latencia,FogEvents.TUPLE_ARRIVAL,tuple);
    }
  }
}
