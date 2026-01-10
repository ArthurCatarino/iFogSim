package org.fog.entities.dataEstructures;

import java.util.HashMap;

public class LatencyMatrix {
  private static HashMap<Integer,HashMap<Integer,Double>> map = new HashMap<>(); //Mapeia a latencia de um fog a os seus vizinhos, sensores tem o id negativo.

  public static void addLatency(Integer source, Integer dest,Double latency) {
    if(map.get(source) == null) {
      HashMap<Integer,Double> newMap = new HashMap<>();
      map.put(source,newMap);
    }
    if(map.get(dest) == null) {
      HashMap<Integer,Double> newMap = new HashMap<>();
      map.put(dest,newMap);
    }
    map.get(source).put(dest,latency);
    map.get(dest).put(source,latency);
  }

  public static Double getLatency(Integer source, Integer dest) {
    if(map.get(source) == null) {
      return null;
    }
    if(map.get(source).get(dest) == null) {
      return 1.0;
    }
    return map.get(source).get(dest);
  }


}
