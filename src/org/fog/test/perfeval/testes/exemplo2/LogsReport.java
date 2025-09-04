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
import java.util.Map;
import org.fog.entities.Tuple;

public class LogsReport {
  private static HashMap<String,Integer> sensors = new HashMap<>();
  private static HashMap<String,Set<Integer>> fogs = new HashMap<>();
  private static HashMap<String,Set<Integer>> actuators = new HashMap<>();
  private static HashMap<Integer,ArrayList<String>> tupleMap = new HashMap<>();
  private static HashMap<String,ArrayList<Integer>> sensorMap = new HashMap<>();
  private static ArrayList<Integer> anomalies = new ArrayList<>();
  private static int lostPacket = 0;
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
 
  public static void sensorLogs(String device,Integer id,Tuple tuple) {
    printSensor(tuple,device);
    Integer incrementa = sensors.get(device);
    tupleMapping(tuple,tuple.getDestModuleName());
    if(incrementa == null) {
      sensors.put(device,1);
    }
    else {
      sensors.put(device,incrementa+1);
    }
    sensorCount++;
    generalReport();
  } 

  public static void fogsLogs(String device,Integer id, Tuple tuple) {
    tupleMapping(tuple,tuple.getDestModuleName());
    if(id > 1) {
      if(fogs.get(device).add(id)) {
        tuplasFogs++;
        generalReport();
      }
    }
  }
  
   public static void actuatorLogs(String device,Integer id, Tuple tuple) {
    anomalies.add(id);
    tupleMapping(tuple,tuple.getDestModuleName());
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
      writer.write(Math.round(porcetagemTotal * 100) / 100f + "% das tuplas apresentaram anomalias" + "\n");

      float porcentagemPerda = ((lostPacket*100)/(float)sensorCount);
      writer.write(lostPacket + " pacotes perdidos (" + Math.round(porcentagemPerda) + "%)" + "\n");

      writer.write("Tuplas com anomalias: ");
      for(Integer k : anomalies) {
        writer.write(k+ " ");
      }
      writer.close();
    } catch (IOException e) {
      System.out.println("Ocorreu um erro ao criar o arquivo.");
      e.printStackTrace();
      }
    }

  private static void tupleMapping(Tuple tuple,String way){
    if(tupleMap.get(tuple.getActualTupleId()) == null) {
      tupleMap.put(tuple.getActualTupleId(),new ArrayList<>());
      tupleMap.get(tuple.getActualTupleId()).add(way);
    }
    else {
      tupleMap.get(tuple.getActualTupleId()).add(way);
    }
    tuplePrint();
  }
  
  private static void tuplePrint() {
    String filename = "C:/Users/arthu/OneDrive/Área de Trabalho/ReportIFog/tupleMap.txt"; // nome do arquivo
    try (FileWriter writer = new FileWriter(filename, false)) { // false = sobrescrever
      for (Map.Entry<Integer, ArrayList<String>> entry : tupleMap.entrySet()) {
        writer.write("Chave: " + entry.getKey() + " ");
        ArrayList<String> valores = entry.getValue();
        for (String valor : valores) {
          writer.write(" --> " + valor);
        }
        writer.write("\n");
      }
      //System.out.println("HashMap escrito no arquivo com sucesso!");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void printSensor(Tuple tuple,String device) {
    if(sensorMap.get(device) == null){
      sensorMap.put(device,new ArrayList<>());
      sensorMap.get(device).add(tuple.getActualTupleId());
    }
    else{
      sensorMap.get(device).add(tuple.getActualTupleId());
    }

    String filename = "C:/Users/arthu/OneDrive/Área de Trabalho/ReportIFog/sensorMap.txt"; // nome do arquivo
    try (FileWriter writer = new FileWriter(filename, false)) { // false = sobrescrever
      for (Map.Entry<String, ArrayList<Integer>> entry : sensorMap.entrySet()) {
        writer.write(entry.getKey() + ": ");
        ArrayList<Integer> valores = entry.getValue();
        for (Integer valor : valores) {
          writer.write(valor + ", ");
        }
        writer.write("\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void lossPacketReport(Tuple tuple) {
    lostPacket++;
  }

}
