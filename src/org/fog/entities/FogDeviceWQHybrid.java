package org.fog.entities;

import java.util.ArrayList;
import java.util.List;

import org.fog.utils.FogEvents;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.fog.entities.dataEstructures.NetworkMatrix;
import org.fog.test.perfeval.testes.cluster.Monitoramento;

public class FogDeviceWQHybrid extends FogDeviceWithQueue {
;
  private ArrayList<FogDeviceWQHybrid> vizinhos = new ArrayList<>();
  private ArrayList<FogDeviceWQHybrid> pais = new ArrayList<>();
  private Double maiorDelay, menorDelay;
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
    maiorDelay = Double.MIN_VALUE;
    menorDelay = Double.MAX_VALUE;
    maiorFolga = Integer.MIN_VALUE;
    menorFolga = Integer.MAX_VALUE;
    boolean encontrou = false;

    for(FogDeviceWQHybrid i : lista) {
      int folga = i.maxTupleQueueSize - i.tupleQueue.size();
      Double delay = NetworkMatrix.getLatency(this.getId(),i.getId());
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

  public void addPais(FogDeviceWQHybrid device) {
    pais.add(device);
  }

  public FogDeviceWQHybrid calculaProximo(Tuple tuple) {
    return this.calculaProximo(tuple,vizinhos);
  }

  private FogDeviceWQHybrid calculaProximo(Tuple tuple,ArrayList<FogDeviceWQHybrid> lista) {
    if (!calculaParametros(lista)) {
        return null; // Ninguém disponível ou configurado
    }

    int folga;
    Double score,delayTotal,folgaNormalizada, delayNormalizado;
    double minMaxFolga = maiorFolga - menorFolga;
    Double minMaxDelay = maiorDelay - menorDelay;
    Double maiorScore = -1.0;
    FogDeviceWQHybrid proximo = null;

      if (minMaxFolga == 0) minMaxFolga = 1.0; // Evita NaN se todas as folgas forem iguais
      if (minMaxDelay <= 0.00001) minMaxDelay = 1.0; // Evita NaN se todas as latências forem iguais

    for(FogDeviceWQHybrid i : lista) {
      if(i.tupleQueue.size() < i.maxTupleQueueSize) {
        if(i.getId() == tuple.getSourceDeviceId()) {continue;}
        folga = i.maxTupleQueueSize - i.tupleQueue.size();

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

  protected void sendUp(Tuple tuple) {
    FogDeviceWQHybrid proximo;
    proximo = calculaProximo(tuple,pais);

    if(proximo == null) {
      int idPai = this.getParentId();
      Double delay = super.calculaDelay(idPai,tuple);
      
      Monitoramento.addUsoRede(tuple.getCloudletFileSize());
      Monitoramento.addTempoMedio(tuple.getCloudletId(), delay);
      tuple.addLifetime(delay);
      send(idPai,delay,FogEvents.TUPLE_ARRIVAL,tuple);
    }
    else {
      Double delay = super.calculaDelay(proximo.getId(),tuple);
      Monitoramento.addUsoRede(tuple.getCloudletFileSize());
      tuple.addLifetime(delay);
      send(proximo.getId(),delay,FogEvents.TUPLE_ARRIVAL,tuple);
    }
  }


  
}
