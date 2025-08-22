package org.fog.test.perfeval.testes.exemplo2;

import org.fog.placement.Controller;
import org.fog.application.Application;
import org.fog.entities.*;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacement;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.test.perfeval.testes.CriaDispositivos;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.utils.*;

import java.util.*;

public class MonitoramentoDeAr {
  private static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
  private static List<Sensor> sensors = new ArrayList<Sensor>();
  private static List<Actuator> actuators = new ArrayList<Actuator>();
  private static String appId = "AirMonitoring";


  public static void main(String[] args) {

    try{
      System.out.println("Starting Air monitoring Service...");
      Log.enable();
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
    
      controller.submitApplication(app,new ModulePlacementEdgewards(fogDevices,sensors,actuators,app,mapping));

      TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
      CloudSim.startSimulation();
      CloudSim.stopSimulation();

    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  private static void createDevices(int brokerId) {
    CriaDispositivos devicesGenerator = new CriaDispositivos(brokerId, appId);

    devicesGenerator.createCloud("cloud",100,20,100,100,0,1,10,5);

    devicesGenerator.createFog("fogCity",50,10,50,50,1,1,5,2.5,"cloud",10);

    String nameFogNeighbothood = "fogNeighborhood";
    String nameSensor = "sensor";

    for(int i=0;i<3;i++) {
      devicesGenerator.createFog(nameFogNeighbothood+i,20,5,20,10,2,1,2,1,"fogCity",5);
      for(int j=0;j<2;j++) {
        devicesGenerator.createSensor(nameFogNeighbothood + i + nameSensor+j,"sendData",100,nameFogNeighbothood+i,2);
      }
    }
    devicesGenerator.createActuactor("alert", "alert","fogCity",5);

    fogDevices = devicesGenerator.getFogDevices();
    sensors = devicesGenerator.getSensors();
    actuators = devicesGenerator.getActuators();

    for(FogDevice i : fogDevices) {
      LogsReport.startFogReports(i.getName());
    }

    for(Actuator i : actuators) {
      LogsReport.startActuatorReports(i.getName());
    }
  }

  private static ModuleMapping criaMapeamento() {

    ModuleMapping mapping = ModuleMapping.createModuleMapping();

    mapping.addModuleToDevice("cloudAnalyzer", "cloud");

    for(FogDevice fog : fogDevices) {
      if(fog.getName().startsWith("fogNeighborhood")) {
        mapping.addModuleToDevice("preProcessing",fog.getName());
      }
      if(fog.getName().startsWith("fogCity")) {
         mapping.addModuleToDevice("anomalyDetection",fog.getName());
         mapping.addModuleToDevice("dataCompactor",fog.getName());
         mapping.addModuleToDevice("reportSender",fog.getName());
      }
    }
    return mapping;
  }
}
