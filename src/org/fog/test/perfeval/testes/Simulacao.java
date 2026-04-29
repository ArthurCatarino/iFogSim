package org.fog.test.perfeval.testes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.Comparator;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.Application;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.entities.SensorLessLatency;
import org.fog.entities.SensorLessSlack;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementMapping;
import org.fog.test.perfeval.testes.CriaDispositivos;
import org.fog.test.perfeval.testes.ExtraiTopologia.FogLink;
import org.fog.test.perfeval.testes.ExtraiTopologia.FogNode;
import org.fog.entities.dataEstructures.NetworkMatrix;
import org.fog.entities.FogDeviceWQLessLatency;

public class Simulacao {
  private static Random rand = new Random();
  private static final int numeroSensores = 100;
  private static FogBroker broker;
  private static CriaDispositivos deviceFactory;
  private static List<Sensor> sensores = new ArrayList<>();
  private static List<FogDevice> fogs = new ArrayList<>();
  private static List<Actuator> actuators = new ArrayList<>();
  private static final String appId = "Simulacao";
  private static final double bandaSensores = TipoSensor.mediaBanda();
  private static final double porcentagemConectadaANuvem = 0.20;

  private static void distribuirSensores(List<FogNode> nos) {
    int nFogs = nos.size(); 
    int[] sensoresNoFog = new int[nFogs];
    int totalAlocado = 0;

    for (int i = 0; i < nFogs; i++) { //Garante que cada fog tera pelo menos 3 sensores
        for (int j = 0; j < 3; j++) {
            if (totalAlocado < numeroSensores) {
                criaSensorNoFog(nos.get(i), totalAlocado);
                sensoresNoFog[i]++;
                totalAlocado++;
            }
        }
    }

    while (totalAlocado < numeroSensores) { // Distribui o resto dos sensores
        int indiceFog = rand.nextInt(nFogs);
        criaSensorNoFog(nos.get(indiceFog), totalAlocado);
        totalAlocado++;
    }
}

public static double gerarPosicaoAoRedorDoFog(double n) { 
    double desvioPadrao = 30.0; 
    double xAleatorio = n + (rand.nextGaussian() * desvioPadrao);
    return xAleatorio;
}

private static void criaSensorNoFog(FogNode pai, int idSensor) {
    TipoSensor tipoSorteado = TipoSensor.sortear();
    SensorLessLatency sensor = deviceFactory.createSensorLessLatency(String.valueOf(idSensor),"TUPLA",tipoSorteado.getFrequenciaMs(),pai.device.getName(),2.0);
    double sensorPosX = gerarPosicaoAoRedorDoFog(pai.x);
    double sensorPosY = gerarPosicaoAoRedorDoFog(pai.y);
    double distanciaEucli = Math.sqrt(Math.pow((pai.x-sensorPosX), 2) + Math.pow(pai.y-sensorPosY,2));
    NetworkMatrix.addLatency(-1*sensor.getId(),pai.device.getId(), distanciaEucli,bandaSensores);
    //System.out.println("Sensor S_" + idSensor + " -> Conectado ao Fog: " + pai.getName());
}

  private static void criaTopologia(List<FogNode> nos, List<FogLink> links) {
    FogNode nuvem = nos.remove(nos.size() - 1);
    for(FogLink link: links){ //Cria os links entre os fogs
      FogDeviceWQLessLatency d1 = nos.get(link.source -1 ).device;
      FogDeviceWQLessLatency d2 = nos.get(link.target - 1).device;

      d1.addNeighbor(d2);
      d2.addNeighbor(d1);
      
      double dist = nos.get(link.source-1).distanciaEuclidiana(nos.get(link.target-1));

      NetworkMatrix.addLatency(d1.getId(), d2.getId(), dist, link.capacity);
      NetworkMatrix.addLatency(d2.getId(), d1.getId(), dist, link.capacity);

      // System.out.println("Link P2P: " + d1.getName() + " <--> " + d2.getName());
    }
    distribuirSensores(nos); //Gera e atribui os sensores
    //Conectar os nos a nuvem
    conectarNosProximosNuvem(nos,links,nuvem);
  }
  
  private static ModuleMapping criaMapeamento() {
    ModuleMapping mapping = ModuleMapping.createModuleMapping();
    for(FogDevice f : fogs) {
      mapping.addModuleToDevice("Process", f.getName());
    }
    return mapping;
  } 
  
  public static void main(String[] args) {
    try {
      
      Log.disable();
      CloudSim.init(1,java.util.Calendar.getInstance(),false);
      broker = new FogBroker("broker");
      deviceFactory = new CriaDispositivos(broker.getId(), appId);
      
      ExtraiTopologia topologia = new ExtraiTopologia();
      topologia.carregarTopologiaXML("src\\org\\fog\\test\\perfeval\\testes\\topologias\\atlanta.xml",deviceFactory);
      List<FogNode> nosTopologia = topologia.getNodes();
      List<FogLink> linksTopologia = topologia.getLinks();
      criaTopologia(nosTopologia,linksTopologia);

      sensores = deviceFactory.getSensors();
      fogs = deviceFactory.getFogDevices();
      
      LogicaSimulacao logica = new LogicaSimulacao(appId,broker.getId());
      Application app = logica.criaApp();
      ModuleMapping mapping = criaMapeamento();
      Controller controller = new Controller("controller", fogs, sensores, actuators);
      controller.submitApplication(app,new ModulePlacementMapping(fogs, app, mapping));
      System.out.println("\nTopologia montada. Iniciando simulação...");
      CloudSim.startSimulation();
      CloudSim.stopSimulation();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }


private static void conectarNosProximosNuvem(List<FogNode> nos,List<FogLink> links, FogNode nuvem) {
    List<NodeDistance> distancias = new ArrayList<>();
    
    for (FogNode fog : nos) {
        double d = fog.distanciaEuclidiana(nuvem); 
        distancias.add(new NodeDistance(fog, d));
    }
    double bandaMedia = 0;
    for(FogLink l : links){
      bandaMedia += l.capacity;
    }
    bandaMedia = bandaMedia/links.size();
    distancias.sort(Comparator.comparingDouble(nd -> nd.distance));
    int limite = (int) Math.ceil(nos.size() * porcentagemConectadaANuvem);

    for (int i = 0; i < limite; i++) {
        FogNode selecionado = distancias.get(i).node;  
        NetworkMatrix.addLatency(selecionado.device.getId(), nuvem.device.getId(), distancias.get(i).distance,bandaMedia);
        System.out.println("Nó " + selecionado.id + " conectado à Nuvem (Top " + porcentagemConectadaANuvem + "%)");
    }
}

// Classe auxiliar para a ordenação
private static class NodeDistance {
    FogNode node;
    double distance;
    NodeDistance(FogNode n, double d) { this.node = n; this.distance = d; }
  }
}