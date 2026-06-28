package org.fog.test.perfeval.testes;

import java.util.Random;

public enum TipoSensor {
    
    SENSOR_A("SENSOR_A", 10,  5500, 500),
    SENSOR_B("SENSOR_B", 10, 11000, 900),
    SENSOR_C("SENSOR_C", 10, 220000, 1800);

    private String tupleType;
    private int frequenciaMs;
    private long tamanhoBytes;
    private long mips; // Quanto de CPU gasta para processar

    TipoSensor(String tupleType, int frequenciaMs, long tamanhoBytes, long mips) {
        this.tupleType = tupleType;
        this.frequenciaMs = frequenciaMs;
        this.tamanhoBytes = tamanhoBytes;
        this.mips = mips;
    }

    private static final Random RANDOM = new Random(18);
    
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