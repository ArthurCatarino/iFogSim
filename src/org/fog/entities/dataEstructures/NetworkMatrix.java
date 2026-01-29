package org.fog.entities.dataEstructures;

import java.util.HashMap;

public class NetworkMatrix {
  private static HashMap<Integer,HashMap<Integer,Double>> latencia = new HashMap<>(); //Mapeia a latencia de um fog a os seus vizinhos, sensores tem o id negativo.

  private static HashMap<Integer,HashMap<Integer,Double>> banda = new HashMap<>(); // Mapeia a banda de rede de um fog a os seus vizinhos, sensores tem o id negativo.

  public static void addLatency(Integer source, Integer dest,Double latency) {

    latencia.computeIfAbsent(source, k -> new HashMap<>()).put(dest, latency);
    latencia.computeIfAbsent(dest, k -> new HashMap<>()).put(source, latency);

    // Passa o valor sorteado direto para evitar recriar objetos se não precisar
    addBand(source, dest, TipoConexao.sortear());
}

private static void addBand(Integer source, Integer dest, Double band) {
    banda.computeIfAbsent(source, k -> new HashMap<>()).put(dest, band);
    banda.computeIfAbsent(dest, k -> new HashMap<>()).put(source, band);
}

  public static Double getLatency(Integer source, Integer dest) {
    if(latencia.get(source) == null) {
      return null;
    }
    if(latencia.get(source).get(dest) == null) {
      return 1.0;
    }
    return latencia.get(source).get(dest);
  }

  public static Double getBand(Integer source, Integer dest) {
    if(banda.get(source) == null) {
      return null;
    }
    if(banda.get(source).get(dest) == null) {
      return 1.0;
    }
    return banda.get(source).get(dest);
  }

}
