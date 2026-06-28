package org.fog.test.perfeval.testes.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import org.fog.test.perfeval.testes.ExtraiTopologia.FogLink;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.test.perfeval.testes.ColetaDados;

public class Monitoramento {
    private static int tuplasPerdidas = 0;
    private static int tuplasEnviadas = 0;
    private static long totalBytesTrafegados = 0;
    private static int tuplasRedirecionadas = 0;
    private static ArrayList<ColetaDados> infoTuplas = new ArrayList<>();
    private static HashMap<Integer,Double> tempoMedioTuplas = new HashMap<>();
    private static HashMap<String,Integer> motivoDaTuplaSerPerdida = new HashMap<>();
    private static HashMap<String,Long> usoDosLinks = new HashMap<>();
    private static HashMap<String,Double> capacidadeDosLinks = new HashMap<>();
    
    public static void addTuplaPerdida(String motivo) {
        tuplasPerdidas++;
        motivoDaTuplaSerPerdida.merge(motivo,1,Integer::sum);
    }
    
    public static void addInfoTupla(ColetaDados info) {
        infoTuplas.add(info);
    }

    public static void addUsoRede(long bytes) {
        totalBytesTrafegados += bytes;
    }

    public static void addTuplaEnviada() {
        tuplasEnviadas++;
    }
    
    public static void addTempoMedio(Integer source, Double time) {
        Double atual = tempoMedioTuplas.get(source);
        if(atual == null) {
            atual = time;
        }
        else {
            atual += time;
        }

        tempoMedioTuplas.put(source,atual);
    }

    public static void reset() {
        tuplasPerdidas = 0;
        tuplasRedirecionadas = 0;
        totalBytesTrafegados = 0;
        tuplasEnviadas = 0;
    }

    public static void addTuplaRedirecionada() {
        tuplasRedirecionadas++;
    }

    public static void inicializaMapDoUsoDeBanda(List<FogLink> links){
        String chave;
        for(FogLink i : links){
            chave = String.valueOf(i.source) + "-" + String.valueOf(i.target); 
            usoDosLinks.put(chave,0l);
            capacidadeDosLinks.put(chave,i.capacity);
        }
    }

    public static void adicionaTrafegoNoLink(String idLink,long tamanhoTrafego ){
        Long usoAntigo = usoDosLinks.get(idLink);
        if(usoAntigo == null) { //O link e bidirecional
            String[] partes = idLink.split("-");
            idLink = partes[1] + "-" + partes[0];
            usoAntigo = usoDosLinks.get(idLink);
        }
        usoDosLinks.put(idLink,usoAntigo+tamanhoTrafego);
    }

    public static double getUsoDosLinks(){
        double media = 0;
        double porcentagem;
        double usoDoLink;

        for(String i : usoDosLinks.keySet()){

            usoDoLink = usoDosLinks.get(i);

            porcentagem = (usoDoLink/capacidadeDosLinks.get(i)) *100;
            media +=porcentagem*100;
        }
        
        return media/usoDosLinks.size();
    }

    public static int getTuplasEnviadas() {return tuplasEnviadas; }
    public static int getTuplasPerdidas() { return tuplasPerdidas; }
    public static long getTotalBytesTrafegados() { return totalBytesTrafegados; }
    public static HashMap<String,Integer> getMotivoTuplas() {return motivoDaTuplaSerPerdida;}
    public static int getTuplaRedirecionadas() {return tuplasRedirecionadas;}
    public static Double getTempoMedio() {
        Double somatorio = 0.0;
        for(Integer i : tempoMedioTuplas.keySet()) {
            somatorio+= tempoMedioTuplas.get(i);
        }
         return somatorio/tempoMedioTuplas.size();
    }
}