package org.fog.test.perfeval.testes.cluster;

import java.util.ArrayList;
import java.util.HashMap;

public class Monitoramento {
    private static int tuplasPerdidas = 0;
    private static int tuplasEnviadas = 0;
    private static long totalBytesTrafegados = 0;
    private static Double tempoTuplas = 0.0;
    
    public static void addTuplaPerdida() {
        tuplasPerdidas++;
    }
    
    public static void addUsoRede(long bytes) {
        totalBytesTrafegados += bytes;
    }

    public static void addTuplaEnviada() {
        tuplasEnviadas++;
    }
    
    public static void addTempoMedio(Double time) {
        tempoTuplas+= time;
    }

    public static void reset() {
        tuplasPerdidas = 0;
        totalBytesTrafegados = 0;
        tuplasEnviadas = 0;
    }


    public static int getTuplasEnviadas() {return tuplasEnviadas; }
    public static int getTuplasPerdidas() { return tuplasPerdidas; }
    public static long getTotalBytesTrafegados() { return totalBytesTrafegados; }
    public static Double getTempoMedio() {
        return tempoTuplas/tuplasEnviadas;
    }
}