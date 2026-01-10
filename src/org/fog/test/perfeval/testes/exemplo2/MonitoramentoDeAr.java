package org.fog.test.perfeval.testes.exemplo2;

import org.fog.placement.Controller;
import org.fog.application.Application;
import org.fog.entities.*;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacement;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.test.perfeval.testes.CriaDispositivos;
import org.fog.test.perfeval.testes.LogsReport;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.utils.*;


import java.util.*;

public class MonitoramentoDeAr {
  private static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
  private static List<Sensor> sensors = new ArrayList<Sensor>();
  private static List<Actuator> actuators = new ArrayList<Actuator>();
  private static String appId = "AirMonitoring";
//zzz

  public static void main(String[] args) {

    try{
      System.out.println("Starting Air monitoring Service...");
      Log.disable();
      int num_user = 1; // number of cloud users
      Calendar calendar = Calendar.getInstance();
      boolean trace_flag = false; //Logs

      CloudSim.init(num_user, calendar, trace_flag);
      FogBroker broker = new FogBroker("broker"); 
      createDevices(broker.getId());

      LogicaMonitoramentoDeAr logica = new LogicaMonitoramentoDeAr(appId, broker.getId());
      Application app = logica.criaAplicacao();

      ModuleMapping mapping = criaMapeamento();

      Controller controller = new Controller("controller", fogDevices, sensors, actuators);
    
      controller.submitApplication(app,new ModulePlacementMapping(fogDevices,app,mapping));

      TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
      CloudSim.startSimulation();
      CloudSim.stopSimulation();

    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  private static void createDevices(int brokerId) {
    CriaDispositivos devicesGenerator = new CriaDispositivos(brokerId, appId);

    devicesGenerator.createCloud("cloud",100,20,100,100,0,1,10,5,50);

    devicesGenerator.createFog("anomalyDetector",50,10,50,50,1,1,5,2.5,"cloud",10,1);

    String nameFogNeighborhood = "pre-processFog";
    String nameSensorA = "sensorA";
    String nameSensorB = "sensorB";    

    int frequenciaA = 1000;
    int frequenciaB = 1000;

    //Um no fog com 3 sensores ligados a ele sendo dois sensores A e um B
    devicesGenerator.createFogWithAlgorithm(nameFogNeighborhood+"1",20,3,20,10,2,1,6,3,"anomalyDetector",5,10);
    devicesGenerator.createSensor(nameFogNeighborhood + "1" + nameSensorA + "1","sendDataA", frequenciaA, nameFogNeighborhood+"1", 5);
    devicesGenerator.createSensor(nameFogNeighborhood + "1" + nameSensorA + "2","sendDataA", frequenciaA, nameFogNeighborhood+"1", 5);
    devicesGenerator.createSensor(nameFogNeighborhood + "1" + nameSensorB + "1","sendDataB", frequenciaB, nameFogNeighborhood+"1", 5);
    
    // UM no fog com 1 sensor do tipo B ligado a ele 
    devicesGenerator.createFogWithAlgorithm(nameFogNeighborhood+"2",5,1,20,10,2,1,2,1,"anomalyDetector",5,5);
    devicesGenerator.createSensor(nameFogNeighborhood + "2" + nameSensorB + "1","sendDataB", frequenciaB, nameFogNeighborhood+"2", 5);

    //Um no fog com um sensor A e um B ligado a ele
    devicesGenerator.createFogWithAlgorithm(nameFogNeighborhood+"3",10,2,20,10,2,1,4,2,"anomalyDetector",5,2);
    devicesGenerator.createSensor(nameFogNeighborhood + "3" + nameSensorB + "1","sendDataB", frequenciaB, nameFogNeighborhood+"3", 5);
    devicesGenerator.createSensor(nameFogNeighborhood + "3" + nameSensorA + "1","sendDataA", frequenciaA, nameFogNeighborhood+"3", 5);

    // UM no fog com 1 sensor do tipo A ligado a ele 
    devicesGenerator.createFogWithAlgorithm(nameFogNeighborhood+"4",5,1,20,10,2,1,2,1,"anomalyDetector",5,5);
    devicesGenerator.createSensor(nameFogNeighborhood + "4" + nameSensorA + "1","sendDataA", frequenciaA, nameFogNeighborhood+"4", 5);


    devicesGenerator.createActuactor("alert", "alert","anomalyDetector",5);

    fogDevices = devicesGenerator.getFogDevices();
    sensors = devicesGenerator.getSensors();
    actuators = devicesGenerator.getActuators();

    for(FogDevice i : fogDevices) {
      LogsReport.startFogReports(i.getName(),i.getLevel());
    }

    for(Actuator i : actuators) {
      LogsReport.startActuatorReports(i.getName());
    }
    addNeighboor(brokerId);
  }

  private static void addNeighboor(int brokerId){
    CriaDispositivos devicesGenerator = new CriaDispositivos(brokerId, appId);
    String fogBairro = "pre-processFog";

    devicesGenerator.addNeighboor(fogBairro+"1",fogBairro+"2");
    devicesGenerator.addNeighboor(fogBairro+"1",fogBairro+"2");

    devicesGenerator.addNeighboor(fogBairro+"2",fogBairro+"1");
    devicesGenerator.addNeighboor(fogBairro+"2",fogBairro+"3");

    devicesGenerator.addNeighboor(fogBairro+"3",fogBairro+"2");
    devicesGenerator.addNeighboor(fogBairro+"3",fogBairro+"4");

    devicesGenerator.addNeighboor(fogBairro+"4",fogBairro+"3");


  }

  private static ModuleMapping criaMapeamento() {

    ModuleMapping mapping = ModuleMapping.createModuleMapping();

    mapping.addModuleToDevice("cloudAnalyzer", "cloud");
    mapping.addModuleToDevice("pre-processFog", "cloud");
    mapping.addModuleToDevice("anomalyDetector", "cloud");

    for(FogDevice fog : fogDevices) {
      if(fog.getName().startsWith("pre-processFog")) {
        mapping.addModuleToDevice("preProcessing",fog.getName());
      }
      if(fog.getName().startsWith("anomalyDetector")) {
         mapping.addModuleToDevice("anomalyDetection",fog.getName());
      }
    }
    return mapping;
  }
}
