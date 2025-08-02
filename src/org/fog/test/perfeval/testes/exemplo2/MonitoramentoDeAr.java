package org.fog.test.perfeval.testes.exemplo2;

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

public class MonitoramentoDeAr {
  static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
  static List<Sensor> sensors = new ArrayList<Sensor>();
  static List<Actuator> actuators = new ArrayList<Actuator>();
  //static Map<String, Integer> idByName = new HashMap<>();
  static int userId = 1;
  static String appId = "TempService";
}
