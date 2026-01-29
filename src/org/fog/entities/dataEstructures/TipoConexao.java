package org.fog.entities.dataEstructures;

import java.util.Random;

public enum TipoConexao {
    
    LENTO(100.0),
    MEDIO(500.0), 
    RAPIDO(1500.0); 

    private final double banda;
    private static final Random RANDOM = new Random();

    TipoConexao(double banda) {
        this.banda = banda;
    }

    public double getBanda() {
        return banda;
    }

    public static double sortear() {
        TipoConexao[] conexoes = values();
        return conexoes[RANDOM.nextInt(conexoes.length)].getBanda();
    }
}