package org.fog.test.perfeval.testes.cluster;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;

import org.fog.entities.FogDevice;

public class AnalisadorResultados {

    public static ResultadoSimulacao coletarDados(List<FogDevice> fogs, int iteracaoAtual) {
        double custoTotal = 0.0;
        double energiaTotal = 0.0;

        for (FogDevice fog : fogs) {
            custoTotal += fog.getTotalCost();
            energiaTotal += fog.getEnergyConsumption();
        }

        double usoRedeMB = Monitoramento.getTotalBytesTrafegados() / 1024.0 / 1024.0;
        int perdas = Monitoramento.getTuplasPerdidas();
        int enviadas = Monitoramento.getTuplasEnviadas();
        Double tempoMedio = Monitoramento.getTempoMedio();

        return new ResultadoSimulacao(iteracaoAtual, enviadas, perdas, usoRedeMB, custoTotal, energiaTotal,tempoMedio);
    }

    public static void salvarResultados(List<FogDevice> fogs, String nomeArquivo) {

        // --- 1. CÁLCULOS ---
        double custoTotal = 0.0;
        double energiaTotal = 0.0;

        // Recupera valores do Monitoramento
        long totalEnviadas = Monitoramento.getTuplasEnviadas();
        long totalPerdidas = Monitoramento.getTuplasPerdidas();
        Double tempoMedio = Monitoramento.getTempoMedio();

        double usoRedeMB = Monitoramento.getTotalBytesTrafegados() / 1024.0 / 1024.0;

        // Cálculo de porcentagem
        double porcentagemDePerda = 0.0;
        if (totalEnviadas > 0) {
            porcentagemDePerda = ((double) totalPerdidas * 100) / totalEnviadas;
        }

        // Soma custos dos Fogs
        for (FogDevice fog : fogs) {
            custoTotal += fog.getTotalCost();
            energiaTotal += fog.getEnergyConsumption();
        }

        // --- 2. FORMATAÇÃO ---
        // Define formato com 2 casas decimais. 
        // Se seu sistema estiver em PT-BR, usará vírgula automaticamente (ex: 12,98).
        DecimalFormat df = new DecimalFormat("0.00"); 

        // Formata os campos conforme solicitado
        // Formato: 100 (12%)
        String campoTuplas = String.format("%d (%.0f%%)", totalPerdidas, porcentagemDePerda);
        
        // Formato: 12,98 MB
        String campoRede = df.format(usoRedeMB) + " MB";
        
        // Formato: $1574,98
        String campoCusto = "$" + df.format(custoTotal);
        
        // Formato: 51584,41 J
        String campoEnergia = df.format(energiaTotal) + " J";

        String campoTempo = df.format(tempoMedio) + " ms";

        // --- 3. SALVAMENTO (APPEND) ---
        File arquivo = new File(nomeArquivo);
        boolean arquivoExiste = arquivo.exists() && arquivo.length() > 0;

        // O parâmetro 'true' no FileWriter habilita o modo append (adicionar ao final)
        try (FileWriter fileWriter = new FileWriter(nomeArquivo, true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            
            // Se o arquivo não existia, cria o cabeçalho primeiro
            if (!arquivoExiste) {
                printWriter.println("Tuplas perdidas;Uso de rede (MB);Custo financeiro (creditos);Custo em energia (J)");
            }

            // Escreve a linha de dados separada por ponto e vírgula
            printWriter.println(campoTuplas + ";" + campoRede + ";" + campoCusto + ";" + campoEnergia + ";" + campoTempo);
            
            System.out.println("Dados adicionados ao relatório CSV: " + nomeArquivo);
            
        } catch (IOException e) {
            System.err.println("Erro ao salvar o arquivo de relatório: " + e.getMessage());
        }
    }
}