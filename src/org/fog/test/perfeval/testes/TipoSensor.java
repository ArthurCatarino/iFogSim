package org.fog.test.perfeval.testes;

import java.util.Random;

public enum TipoSensor {
    
    SENSOR_A("SENSOR_A", 15, 150, 140),     // Alta freq, Leve
    SENSOR_B("SENSOR_B", 60, 1200, 550); // Baixa freq, Pesado

    private String tupleType;
    private int frequenciaMs;
    private long tamanhoBytes;
    private long mips; // Quanto de CPU gasta para processar

    // Construtor do Enum
    TipoSensor(String tupleType, int frequenciaMs, long tamanhoBytes, long mips) {
        this.tupleType = tupleType;
        this.frequenciaMs = frequenciaMs;
        this.tamanhoBytes = tamanhoBytes;
        this.mips = mips;
    }

    // Método Utilitário para sortear aleatoriamente
    private static final Random RANDOM = new Random();
    
    public static TipoSensor sortear() {
        TipoSensor[] sensores = values();
        return sensores[RANDOM.nextInt(sensores.length)];
    }

    public static double mediaBanda() {
        double soma = 0;
        for(TipoSensor ts : TipoSensor.values()){
            soma += ts.getTamanhoBytes();
        }
        return soma/TipoSensor.values().length;
    }

    // Getters
    public String getTupleType() { return tupleType; }
    public int getFrequenciaMs() { return frequenciaMs; }
    public long getTamanhoBytes() { return tamanhoBytes; }
    public long getMips() { return mips; }
}