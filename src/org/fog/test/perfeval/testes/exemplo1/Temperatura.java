package org.fog.test.perfeval.testes.exemplo1;


import java.io.FileWriter;
import java.io.PrintWriter;
import org.fog.placement.Controller;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.*;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.utils.*;
import org.fog.utils.distribution.DeterministicDistribution;

import java.util.*;
//import java.io.IOException;


public class Temperatura {

  static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
  static List<Sensor> sensors = new ArrayList<Sensor>();
  static List<Actuator> actuators = new ArrayList<Actuator>();
 // static Map<String, Integer> idByName = new HashMap<>();
  static int userId = 1;
  static String appId = "TempService";
  public static void main(String[] args) {
    try {
    System.out.println("Starting Temperature analyse Service...");
    Log.disable();
    int num_user = 1; // number of cloud users
    Calendar calendar = Calendar.getInstance();
    boolean trace_flag = false; //Logs

    CloudSim.init(num_user, calendar, trace_flag);
    FogBroker broker = new FogBroker("broker"); 

    // Cria os dispositivos fisicos da simulação
    CriaDispositivos dispositivos = new CriaDispositivos(broker.getId(), appId);
    dispositivos.createDevices();
    setDevices(dispositivos);

    //Define a parte logica da simulação
    Logica logica = new Logica(appId,broker.getId());
    Application app = logica.criaAplicacao();

    // Mapeia os modulos logicos aos dispositivos que irao o executar;
    ModuleMapping mapping = criaMapeamento();

    // O controller e o gerente de toda a simulação, aqui estamos falando pra ele quais os itens da nossa simulação (fogDevices, sensors e actuators) e qual o nosso plano pra simulação (app e mapping)
    Controller controller = new Controller("controller", fogDevices, sensors, actuators); 
    controller.submitApplication(app,new ModulePlacementEdgewards(fogDevices,sensors,actuators, app, mapping));
    
    TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
    CloudSim.startSimulation();
    CloudSim.stopSimulation();

    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  private static void setDevices(CriaDispositivos dispositivos) {
    fogDevices = dispositivos.getFogDevices();
    sensors = dispositivos.getSensors();
    actuators = dispositivos.getActuators();
  }

  private static ModuleMapping criaMapeamento() {
    ModuleMapping mapping = ModuleMapping.createModuleMapping();
    
    for(FogDevice fog : fogDevices) { //Aloca os modulos aos devices que o executarão.
      if(fog.getName()!="cloud") { mapping.addModuleToDevice("AnalisaTemperatura", fog.getName());}
    }
    mapping.addModuleToDevice("AnalisaNuvem","cloud");

    return mapping;
  }

}