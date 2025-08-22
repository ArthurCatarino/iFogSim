package org.fog.test.perfeval.testes.exemplo2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;

public class LogsReport {
  private static HashMap<String,Integer> sensors = new HashMap<>();
  private static HashMap<String,Set<Integer>> fogs = new HashMap<>();
  private static HashMap<String,Set<Integer>> actuators = new HashMap<>();
  private static int actuatorCount = 0;
  private static int sensorCount = 0;
  private static int tuplasFogs = 0;
  private static LocalDateTime agora = LocalDateTime.now(); 

  public static void startFogReports(String device) {
    Set<Integer>tuplasFog = new HashSet<>();
    fogs.put(device,tuplasFog);
  }
  
  public static void startActuatorReports(String device) {
    Set<Integer> tuplasActuator = new HashSet<>();
    actuators.put(device,tuplasActuator);
  }
 
  public static void startSensorReports(String device) {
    Set<Integer> tuplasSensor = new HashSet<>();
    sensors.put(device,tuplasSensor);
  }

  public static void sensorLogs(String device,Integer id) {
    Integer incrementa = sensors.get(device);
    if(incrementa == null) {
      sensors.put(device,1);
    }
    else {
      sensors.put(device,incrementa+1);
    }
    sensorCount++;
    generalReport();
  } 

  public static void fogsLogs(String device,Integer id) {
    if(id > 1) {
      if(fogs.get(device).add(id)) {
        tuplasFogs++;
        generalReport();
      }
    }
  }
  
   public static void actuatorLogs(String device,Integer id) {
    if(id > 1) {
      if(actuators.get(device).add(id)) {
        actuatorCount++;
        generalReport();
      }
    }
  }

  private static void generalReport() {
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
      String caminho = "C:/Users/arthu/OneDrive/Área de Trabalho/ReportIFog/report"+ agora.format(formatter)+".txt";
      
      File file = new File(caminho);
      if (!file.exists()) {
        file.createNewFile();
      }
      FileWriter writer = new FileWriter(caminho);
      writer.write("=======================================Sensores======================================="+ System.lineSeparator());
      float porcentagem;

      for(String i : sensors.keySet()) {
        porcentagem = (sensors.get(i)*100)/(float)sensorCount;
        writer.write("O sensor: " + i + " enviou: " + sensors.get(i) + " tuplas (" +  Math.round(porcentagem * 100) / 100f + "%)" + System.lineSeparator());
      }
      writer.write(sensorCount + " tuplas enviadas" + System.lineSeparator());

      writer.write("=======================================Fogs======================================="+ System.lineSeparator());
      if(tuplasFogs != 0) {
        for(String i : fogs.keySet()){
          porcentagem = (fogs.get(i).size()*100)/tuplasFogs;
          writer.write("O device: " + i + " processou: " + fogs.get(i).size() + " tuplas ("+Math.round(porcentagem * 100) / 100f+"%)" + System.lineSeparator());
        }
        writer.write(tuplasFogs + " tuplas processadas"+System.lineSeparator());
      }
       writer.write("=======================================Actuators======================================="+ System.lineSeparator());
      if(actuatorCount !=0) {
        for(String i : actuators.keySet()){
          porcentagem = (actuators.get(i).size()*100)/actuatorCount;
          writer.write("O actuator: " + i + " processou: " + actuators.get(i).size() + " tuplas ("+Math.round(porcentagem * 100) / 100f+"%)" + System.lineSeparator());
        }
      }

      writer.write(actuatorCount + " tuplas processadas"+ System.lineSeparator());
      float porcetagemTotal = (actuatorCount*100)/(float)sensorCount;
      writer.write(Math.round(porcetagemTotal * 100) / 100f + "% das tuplas apresentaram anomalias");

      writer.close();
    } catch (IOException e) {
      System.out.println("Ocorreu um erro ao criar o arquivo.");
      e.printStackTrace();
      }
    }
}
