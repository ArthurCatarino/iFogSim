
package org.fog.test.perfeval.testes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.NetworkTopology;

import java.util.Collections;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.FogDeviceWithQueue;
import org.fog.entities.FogDeviceWithQueueLessSlack;
import org.fog.entities.FogDeviceWQLessLatency;
import org.fog.entities.FogDeviceWQHybrid;
import org.fog.entities.Sensor;
import org.fog.entities.SensorLessLatency;
import org.fog.entities.SensorLessSlack;
import org.fog.entities.SensorHybrid;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.distribution.DeterministicDistribution;


public class CriaDispositivos {
  private List<FogDevice> fogDevices;
  private List<Sensor> sensors;
  private List<Actuator> actuators;
  private Map<String, Integer> idByName;
  private int brokerId;
  private String appId;

  private ArrayList<TipoSensor> tipos = new ArrayList<>(Arrays.asList(TipoSensor.values()));
    
  public CriaDispositivos(int brokerId,String appId) {
    fogDevices = new ArrayList<FogDevice>();
    sensors = new ArrayList<Sensor>();
    actuators = new ArrayList<Actuator>();
    idByName = new HashMap<>();
    this.brokerId = brokerId;
    this.appId = appId;
  }

  public FogDevice createCloud(String name, int mips, int ram, int upBw, int downBw, int level,double ratePerMips, double busyPower, double idlePower,int queueSize) {
    FogDevice cloud = createFogDevice(name,mips,ram,upBw,downBw,level,ratePerMips,busyPower,idlePower,queueSize); 
    cloud.setParentId(-1); // a nuvem nao tem pai
    fogDevices.add(cloud);
    idByName.put(name,cloud.getId());
    return cloud;
  }

   public  void createFog(String name, int mips, int ram, int upBw, int downBw, int level,double ratePerMips, double busyPower, double idlePower,String pai,int latencia,int queueSize) {
    FogDevice edge = createFogDevice(name,mips,ram,upBw,downBw,level,ratePerMips,busyPower,idlePower,queueSize);
    if(pai == null) { // O fog nao necessariamente e obrigado a ter um pai.
      edge.setParentId(-1);
    }
    else{
    edge.setParentId(idByName.get(pai)); // Seta o pai do fog
    }

    edge.setUplinkLatency(latencia); // latência até o pai
    fogDevices.add(edge);
    idByName.put(name, edge.getId());
  }

  public FogDeviceWithQueueLessSlack createFogDeviceWithQueueLessSlack(String name, int mips, int ram, int upBw, int downBw, int level,double ratePerMips, double busyPower, double idlePower,String pai,int latencia,int queueSize) {
    FogDeviceWithQueueLessSlack edge = FogDeviceWithQueueLessSlack(name,mips,ram,upBw,downBw,level,ratePerMips,busyPower,idlePower,queueSize);

    

    if(pai == null) { // O fog nao necessariamente e obrigado a ter um pai.
      edge.setParentId(-1);
    }
    else{
      if(idByName.containsKey(pai)){
        int idPai = idByName.get(pai);
        edge.setParentId(idPai); // Seta o pai do fog
        
        NetworkTopology.addLink(edge.getId(), idPai, 1000, latencia);
        NetworkTopology.addLink(idPai, edge.getId(), 1000, latencia);
      }
      else {
        System.out.println("ERRO FATAL: Pai '" + pai + "' não encontrado no mapa idByName!");
      }
      edge.setUplinkLatency(latencia); // latência até o pai
    }
    fogDevices.add(edge);
    idByName.put(name, edge.getId());

    return edge;
  }

    public FogDeviceWQLessLatency createFogDeviceWQLessLatency(String name, int mips, int ram, int upBw, int downBw, int level,double ratePerMips, double busyPower, double idlePower,String pai,int latencia,int queueSize) {
    FogDeviceWQLessLatency edge = FogDeviceWQLessLatency(name,mips,ram,upBw,downBw,level,ratePerMips,busyPower,idlePower,queueSize);
    if(pai == null) { // O fog nao necessariamente e obrigado a ter um pai.
      edge.setParentId(-1);
    }
    else{
      if(idByName.containsKey(pai)){
        int idPai = idByName.get(pai);
        edge.setParentId(idPai); // Seta o pai do fog
        
        NetworkTopology.addLink(edge.getId(), idPai, 1000, latencia);
        NetworkTopology.addLink(idPai, edge.getId(), 1000, latencia);
      }
      else {
        System.out.println("ERRO FATAL: Pai '" + pai + "' não encontrado no mapa idByName!");
      }
      edge.setUplinkLatency(latencia); // latência até o pai
    }
    fogDevices.add(edge);
    System.out.println("O fog " + name + " foi adicionado no mapa");
    idByName.put(name, edge.getId());

    return edge;
  }

  public FogDeviceWQHybrid createFogDeviceWQHybrid(String name, int mips, int ram, int upBw, int downBw, int level,double ratePerMips, double busyPower, double idlePower,String pai,int latencia,int queueSize) {
    FogDeviceWQHybrid edge = FogDeviceWQHybrid(name,mips,ram,upBw,downBw,level,ratePerMips,busyPower,idlePower,queueSize);
    if(pai == null) { // O fog nao necessariamente e obrigado a ter um pai.
      edge.setParentId(-1);
    }
    else{
      if(idByName.containsKey(pai)){
        int idPai = idByName.get(pai);
        edge.setParentId(idPai); // Seta o pai do fog
        
        NetworkTopology.addLink(edge.getId(), idPai, 1000, latencia);
        NetworkTopology.addLink(idPai, edge.getId(), 1000, latencia);
      }
      else {
        System.out.println("ERRO FATAL: Pai '" + pai + "' não encontrado no mapa idByName!");
      }
      edge.setUplinkLatency(latencia); // latência até o pai
    }
    fogDevices.add(edge);
    idByName.put(name, edge.getId());

    return edge;
  }

  public void createSensor(String name, String tupleType, int frequenciaDeEnvio, String idPai, double latencia) {
    Sensor sensor = new Sensor(name, tupleType, brokerId, appId,
    new DeterministicDistribution(frequenciaDeEnvio)); // Define a frequencia a qual o sensor enviara dados
    sensor.setGatewayDeviceId(idByName.get(idPai)); // Define um fog como seu pai
    sensor.setLatency(latencia);
    NetworkTopology.addLink(sensor.getId(), idByName.get(idPai), 100.0, latencia);
    sensors.add(sensor);
    CloudSim.addEntity(sensor);
  }
  
  public SensorLessLatency createSensorLessLatency(String name, String tupleType, int frequenciaDeEnvio, String idPai, double latencia) {
    SensorLessLatency sensor = new SensorLessLatency(name, tupleType, brokerId, appId,
    new DeterministicDistribution(frequenciaDeEnvio),tipos); // Define a frequencia a qual o sensor enviara dados
    sensor.setGatewayDeviceId(idByName.get(idPai)); // Define um fog como seu pai
    sensor.setLatency(latencia);
    NetworkTopology.addLink(sensor.getId(), idByName.get(idPai), 100.0, latencia);
    sensors.add(sensor);
    CloudSim.addEntity(sensor);
    return sensor;
  }

  public SensorHybrid createSensorHybrid(String name, String tupleType, int frequenciaDeEnvio, String idPai, double latencia) {
    SensorHybrid sensor = new SensorHybrid(name, tupleType, brokerId, appId,
    new DeterministicDistribution(frequenciaDeEnvio)); // Define a frequencia a qual o sensor enviara dados
    sensor.setGatewayDeviceId(idByName.get(idPai)); // Define um fog como seu pai
    sensor.setLatency(latencia);
    NetworkTopology.addLink(sensor.getId(), idByName.get(idPai), 100.0, latencia);
    sensors.add(sensor);
    CloudSim.addEntity(sensor);
    return sensor;
  }

  public void createActuactor(String name, String tupleType,String idPai, double latencia) {
    Actuator actuator = new Actuator(name, brokerId, appId, tupleType);
    actuator.setGatewayDeviceId(idByName.get(idPai)); 
    actuator.setLatency(latencia);
    actuators.add(actuator);
    CloudSim.addEntity(actuator);
  }

  private FogDevice createFogDevice(String name, long mips, int ram, long upBw, long downBw,
                          int level, double ratePerMips, double busyPower, double idlePower,int queueSize) {
    List<Pe> peList = new ArrayList<>();
    peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // CPU

    int hostId = FogUtils.generateEntityId();
    long storage = 1000000; // 1 GB
    int bw = 10000;         // largura de banda

    PowerHost host = new PowerHost(
        hostId,
        new RamProvisionerSimple(ram),
        new BwProvisionerOverbooking(bw),
        storage,
        peList,
        new StreamOperatorScheduler(peList),
        new FogLinearPowerModel(busyPower, idlePower)
    );

    List<Host> hostList = new ArrayList<>();
    hostList.add(host);

    FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
        "x86", "Linux", "Xen", host,
        10.0, 3.0, 0.05, 0.001, 0.2
    );

    FogDevice device = null;
    try {
        device = new FogDeviceWithQueue(name, characteristics,
                new AppModuleAllocationPolicy(hostList), new LinkedList<>(),
                10, upBw, downBw, 0, ratePerMips,queueSize);
    } catch (Exception e) {
        e.printStackTrace();
    }

    device.setLevel(level);
    return device;
}
  
  private FogDeviceWithQueueLessSlack FogDeviceWithQueueLessSlack(String name, long mips, int ram, long upBw, long downBw,
                          int level, double ratePerMips, double busyPower, double idlePower,int queueSize) {
    List<Pe> peList = new ArrayList<>();
    peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // CPU

    int hostId = FogUtils.generateEntityId();
    long storage = 1000000; // 1 GB
    int bw = 10000;         // largura de banda

    PowerHost host = new PowerHost(
        hostId,
        new RamProvisionerSimple(ram),
        new BwProvisionerOverbooking(bw),
        storage,
        peList,
        new StreamOperatorScheduler(peList),
        new FogLinearPowerModel(busyPower, idlePower)
    );

    List<Host> hostList = new ArrayList<>();
    hostList.add(host);

    FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
        "x86", "Linux", "Xen", host,
        10.0, 3.0, 0.05, 0.001, 0.2
    );

    FogDeviceWithQueueLessSlack device = null;
    try {
        device = new FogDeviceWithQueueLessSlack(name, characteristics,
                new AppModuleAllocationPolicy(hostList), new LinkedList<>(),
                10, upBw, downBw, 0, ratePerMips,queueSize);
    } catch (Exception e) {
        e.printStackTrace();
    }

    device.setLevel(level);
    return device;
}

 private FogDeviceWQLessLatency FogDeviceWQLessLatency(String name, long mips, int ram, long upBw, long downBw,
                          int level, double ratePerMips, double busyPower, double idlePower,int queueSize) {
    List<Pe> peList = new ArrayList<>();
    peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // CPU

    int hostId = FogUtils.generateEntityId();
    long storage = 1000000; // 1 GB
    int bw = 10000;         // largura de banda

    PowerHost host = new PowerHost(
        hostId,
        new RamProvisionerSimple(ram),
        new BwProvisionerOverbooking(bw),
        storage,
        peList,
        new StreamOperatorScheduler(peList),
        new FogLinearPowerModel(busyPower, idlePower)
    );

    List<Host> hostList = new ArrayList<>();
    hostList.add(host);

    FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
        "x86", "Linux", "Xen", host,
        10.0, 3.0, 0.05, 0.001, 0.2
    );

    FogDeviceWQLessLatency device = null;
    try {
        device = new FogDeviceWQLessLatency(name, characteristics,
                new AppModuleAllocationPolicy(hostList), new LinkedList<>(),
                10, upBw, downBw, 0, ratePerMips,queueSize);
    } catch (Exception e) {
        e.printStackTrace();
    }

    device.setLevel(level);
    return device;
}

private FogDeviceWQHybrid FogDeviceWQHybrid(String name, long mips, int ram, long upBw, long downBw,
                          int level, double ratePerMips, double busyPower, double idlePower,int queueSize) {
    List<Pe> peList = new ArrayList<>();
    peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // CPU

    int hostId = FogUtils.generateEntityId();
    long storage = 1000000; // 1 GB
    int bw = 10000;         // largura de banda

    PowerHost host = new PowerHost(
        hostId,
        new RamProvisionerSimple(ram),
        new BwProvisionerOverbooking(bw),
        storage,
        peList,
        new StreamOperatorScheduler(peList),
        new FogLinearPowerModel(busyPower, idlePower)
    );

    List<Host> hostList = new ArrayList<>();
    hostList.add(host);

    FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
        "x86", "Linux", "Xen", host,
        10.0, 3.0, 0.05, 0.001, 0.2
    );

    FogDeviceWQHybrid device = null;
    try {
        device = new FogDeviceWQHybrid(name, characteristics,
                new AppModuleAllocationPolicy(hostList), new LinkedList<>(),
                10, upBw, downBw, 0, ratePerMips,queueSize);
    } catch (Exception e) {
        e.printStackTrace();
    }

    device.setLevel(level);
    return device;
}

  public List<FogDevice> getFogDevices() {
    return Collections.unmodifiableList(fogDevices);
  }

  public List<Sensor> getSensors() {
    return Collections.unmodifiableList(sensors);
  }

  public List<Actuator> getActuators() {
    return Collections.unmodifiableList(actuators);
  }
}
