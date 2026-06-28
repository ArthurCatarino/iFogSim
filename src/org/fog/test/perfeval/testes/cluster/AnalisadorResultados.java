package org.fog.test.perfeval.testes.cluster;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import org.fog.entities.FogDevice;
import org.fog.test.perfeval.testes.ColetaDados;

public class AnalisadorResultados {

    // Atualizado para receber os parâmetros da simulação
    public static ResultadoSimulacao coletarDados(List<FogDevice> fogs, int iteracaoAtual, int numSensores, double porcentagemNuvem) {
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

        // Nota: Certifique-se de que o construtor de ResultadoSimulacao suporte os novos campos caso queira usá-los no objeto
        return new ResultadoSimulacao(iteracaoAtual, enviadas, perdas, usoRedeMB, custoTotal, energiaTotal, tempoMedio);
    }

    public static void salvarResultados(List<FogDevice> fogs, String nomeArquivo, int numSensores, double porcentagemNuvem) {
        computaMotivos();
        
        // --- 1. CÁLCULOS BÁSICOS ---
        double custoTotal = 0.0;
        double energiaTotal = 0.0;

        long totalEnviadas = Monitoramento.getTuplasEnviadas();
        long totalPerdidas = Monitoramento.getTuplasPerdidas();
        long totalRedirecionadas = Monitoramento.getTuplaRedirecionadas(); // NOVA MÉTRICA
        
        Double tempoMedio = Monitoramento.getTempoMedio();
        double usoRedeMB = Monitoramento.getTotalBytesTrafegados() / 1024.0 / 1024.0;
        double porcentagemUsoLinks = Monitoramento.getUsoDosLinks(); // NOVA MÉTRICA

        // --- 2. CÁLCULOS DE PORCENTAGEM ---
        double porcentagemDePerda = 0.0;
        double porcentagemDeRedirecionamento = 0.0;
        
        if (totalEnviadas > 0) {
            porcentagemDePerda = ((double) totalPerdidas * 100) / totalEnviadas;
            porcentagemDeRedirecionamento = ((double) totalRedirecionadas * 100) / totalEnviadas;
        }

        for (FogDevice fog : fogs) {
            custoTotal += fog.getTotalCost();
            energiaTotal += fog.getEnergyConsumption();
        }

        // --- 3. FORMATAÇÃO ---
        DecimalFormat df = new DecimalFormat("0.00"); 

        String campoTuplas = String.format("%d (%.0f%%)", totalPerdidas, porcentagemDePerda);
        String campoRedirecionadas = String.format("%d (%.0f%%)", totalRedirecionadas, porcentagemDeRedirecionamento); // FORMATADO COM %
        String campoRede = df.format(usoRedeMB) + " MB";
        String campoUsoLinks = df.format(porcentagemUsoLinks) + "%"; // FORMATADO COM %
        String campoCusto = "$" + df.format(custoTotal);
        String campoEnergia = df.format(energiaTotal) + " J";
        String campoTempo = df.format(tempoMedio) + " ms";
        
        // Identificadores de Parâmetros
        String campoSensores = String.valueOf(numSensores);
        String campoNuvem = (porcentagemNuvem * 100) + "%";

        // --- 4. SALVAMENTO (APPEND) ---
        File arquivo = new File(nomeArquivo);
        boolean arquivoExiste = arquivo.exists() && arquivo.length() > 0;

        try (FileWriter fileWriter = new FileWriter(nomeArquivo, true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            
            if (!arquivoExiste) {
                // Cabeçalho atualizado com as novas colunas
                printWriter.println("Sensores;Conexao Nuvem;Tuplas perdidas;Tuplas redirecionadas;Uso de rede (MB);Uso dos links (%);Custo financeiro;Energia (J);Latencia Media (ms)");
            }

            // Escreve a linha de dados acompanhando a exata ordem do cabeçalho novo
            printWriter.println(campoSensores + ";" + campoNuvem + ";" + campoTuplas + ";" + campoRedirecionadas + ";" + campoRede + ";" + campoUsoLinks + ";" + campoCusto + ";" + campoEnergia + ";" + campoTempo);
            
            System.out.println("Dados salvos para cenário: " + numSensores + " sensores / " + campoNuvem + " nuvem.");
            
        } catch (IOException e) {
            System.err.println("Erro ao salvar o arquivo de relatório: " + e.getMessage());
        }
    }

    public static void exportarParaCSV(List<ColetaDados> listaDados, String caminhoArquivo) { 
        String delimitador = ";";
        String novaLinha = "\n";

        // Cabeçalho do CSV
        String cabecalho = "Tuple_ID;Tuple_Type;Tuple_Source;MIPS_Required;Network_Size;"
                + "Node_ID;Queue_Size;Local_MIPS_Available;Local_CPU_Utilization;Node_Type;"
                + "Neighbor_IDs;Neighbor_Types;Neighbor_MIPS;Neighbor_CPU_Utilization;Neighbor_Queue_Size;"
                + "Dest_Node;Total_Latency;Final_Status";

        try (FileWriter writer = new FileWriter(caminhoArquivo)) {
            writer.append(cabecalho);
            writer.append(novaLinha);

            for (ColetaDados dado : listaDados) {
                writer.append(String.valueOf(dado.getTupleId())).append(delimitador);
                writer.append(dado.getTupleType()).append(delimitador);
                writer.append(dado.getTupleSource()).append(delimitador);
                writer.append(String.valueOf(dado.getMipsRequired())).append(delimitador);
                writer.append(String.valueOf(dado.getNetworkSize())).append(delimitador);
                
                writer.append(String.valueOf(dado.getNodeId())).append(delimitador);
                writer.append(String.valueOf(dado.getQueueSize())).append(delimitador);
                
                // Troca a vírgula do double por ponto para não quebrar a leitura no Python
                writer.append(String.valueOf(dado.getLocalMIPSAvailable()).replace(",", ".")).append(delimitador);
                writer.append(String.valueOf(dado.getLocalCPUUtilization()).replace(",", ".")).append(delimitador);
                
                // Enum
                writer.append(dado.getNodeType().toString()).append(delimitador);
                
                // Listas (O método toString() de ArrayList já formata como "[item1, item2]")
                writer.append(dado.getNeighborId().toString()).append(delimitador);
                writer.append(dado.getNeighborType().toString()).append(delimitador);
                writer.append(dado.getNeighborMips().toString().replace(",", ".")).append(delimitador);
                writer.append(dado.getNeighborCPUUtilization().toString().replace(",", ".")).append(delimitador);
                writer.append(dado.getNeighborQueueSize().toString()).append(delimitador);
                
                // Decisão e Status
                writer.append(String.valueOf(dado.getDestNode())).append(delimitador);
                writer.append(String.valueOf(dado.getTotalLatency()).replace(",", ".")).append(delimitador);
                writer.append(dado.getFinalStatus());
                
                writer.append(novaLinha);
            }
            
            System.out.println("Arquivo CSV gerado com sucesso em: " + caminhoArquivo);

        } catch (IOException e) {
            System.err.println("Erro ao escrever o arquivo CSV: " + e.getMessage());
        }
    }

    public static void computaMotivos() {
        HashMap<String,Integer> map = Monitoramento.getMotivoTuplas();
        int tuplasEnviadas = Monitoramento.getTuplasEnviadas();
        
        if (tuplasEnviadas == 0) return; // Evita divisão por zero se chamado sem rodar a rede

        int porcentagem = (Monitoramento.getTuplasPerdidas() * 100) / tuplasEnviadas;
        System.out.println(porcentagem + " % tuplas perdidas");
        
        for(String i : map.keySet()){
            System.out.println("Tuplas perdidas por " + i +" : " + map.get(i) + " (" + (100 * map.get(i) / tuplasEnviadas) + "%)");
        }
        System.out.println("Tuplas redirecionadas: " + Monitoramento.getTuplaRedirecionadas() + " (" + (100 * Monitoramento.getTuplaRedirecionadas() / tuplasEnviadas) + "%)");
    }

}