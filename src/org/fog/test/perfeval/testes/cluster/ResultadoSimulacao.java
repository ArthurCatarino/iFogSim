package org.fog.test.perfeval.testes.cluster;

public class ResultadoSimulacao {
    public int iteracao;
    public long tuplasEnviadas;
    public long tuplasPerdidas;
    public double usoRedeMB;
    public double custoTotal;
    public double energiaTotal;
    public Double tempoMedio;

    public ResultadoSimulacao(int iteracao, long tuplasEnviadas, long tuplasPerdidas, double usoRedeMB, double custoTotal, double energiaTotal, Double tempoMedio) {
        this.iteracao = iteracao;
        this.tuplasEnviadas = tuplasEnviadas;
        this.tuplasPerdidas = tuplasPerdidas;
        this.usoRedeMB = usoRedeMB;
        this.custoTotal = custoTotal;
        this.energiaTotal = energiaTotal;
        this.tempoMedio = tempoMedio;
        }
}