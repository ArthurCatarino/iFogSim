package org.fog.test.perfeval.testes.exemplo1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
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
import org.fog.entities.Sensor;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.distribution.DeterministicDistribution;

public class CriaDispositivos {

  private int fogCount;
  private int sensorCount;
  private int actuatorCount;
  private List<FogDevice> fogDevices;
  private List<Sensor> sensors;
  private List<Actuator> actuators;
  private Map<String, Integer> idByName;
  private int userId;
  private String appId;
    
  public CriaDispositivos(int userId,String appId) {
    fogCount = 0;
    sensorCount = 0;
    actuatorCount = 0;
    fogDevices = new ArrayList<FogDevice>();
    sensors = new ArrayList<Sensor>();
    actuators = new ArrayList<Actuator>();
    idByName = new HashMap<>();
    this.userId = userId;
    this.appId = appId;
  }

  public void createDevices() {
    // 1. Cria o dispositivo Cloud
    FogDevice cloud = createFogDevice("cloud", 10000, 4000, 10000, 10000, 0, 0.01, 107.339, 83.4333);
    cloud.setParentId(-1); // sem pai
    fogDevices.add(cloud);
    idByName.put("cloud", cloud.getId());

    createFog();
    createFog();
    createFog();

    for(int i = 0;i<2;i++) {
      createSensor(idByName.get("Fog0"));
    }
    for(int i = 0;i<7;i++) {
      createSensor(idByName.get("Fog1"));
    }
    for(int i = 0;i<5;i++) {
      createSensor(idByName.get("Fog2"));
    }

    createActuactor(idByName.get("Fog0"));
    createActuactor(idByName.get("Fog1"));
    createActuactor(idByName.get("Fog2")); 

    for (Sensor sensor : sensors) {
      CloudSim.addEntity(sensor);
    }
    for (Actuator actuator : actuators) {
      CloudSim.addEntity(actuator);
  }
  }

  private void createSensor(int idFather) {
    Sensor sensor = new Sensor("Sensor"+sensorCount, "SensorDeTemperatura", userId, appId,
    new DeterministicDistribution(5)); // envia a cada 5s
    sensor.setGatewayDeviceId(idFather); // Define o fog  como seu pai
    sensor.setLatency(2.0);

    sensors.add(sensor);
    sensorCount++;
  }

  private  void createFog() {
    FogDevice edge = createFogDevice("Fog"+fogCount, 2000, 1000, 10000, 270, 1, 0.0, 10,5);
    edge.setParentId(idByName.get("cloud")); // Tem a nuvem como pai
    edge.setUplinkLatency(5); // latência até o Cloud
    fogDevices.add(edge);
    idByName.put("Fog"+fogCount, edge.getId());
    fogCount++;
  }

  private  void createActuactor(int idFather) {
    // Cria Atuador de alerta
    Actuator actuator = new Actuator("Actuactor" + actuatorCount, userId, appId, "Alerta");
    actuator.setGatewayDeviceId(idFather); // Define o fog criado anteriormente como seu pai
    actuator.setLatency(1.0);
    actuators.add(actuator);
    actuatorCount++;
  }

  private FogDevice createFogDevice(String name, long mips, int ram, long upBw, long downBw,
                          int level, double ratePerMips, double busyPower, double idlePower) {
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
        device = new FogDevice(name, characteristics,
                new AppModuleAllocationPolicy(hostList), new LinkedList<>(),
                10, upBw, downBw, 0, ratePerMips);
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
