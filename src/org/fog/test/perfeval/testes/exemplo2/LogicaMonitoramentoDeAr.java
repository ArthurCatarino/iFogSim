package org.fog.test.perfeval.testes.exemplo2;

import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.cloudbus.cloudsim.*;
import org.fog.entities.*;
import java.util.*;

public class LogicaMonitoramentoDeAr {
  private String appId;
  private int brokerID;

  public LogicaMonitoramentoDeAr(String appId, int brokerID) {
    this.appId = appId;
    this.brokerID = brokerID;
  }

  public Application criaAplicacao() {
    Application app = Application.createApplication(appId, brokerID);
    app.setUserId(brokerID);

    // 1. Define os módulos (como VMs lógicas) 
    defineModulos(app);

    // 2. Define as conexões entre os módulos (AppEdges)
    defineConexoes(app);
    // 3. Define as conversões de tupla entre módulos
    mapeamentoDeTuplas(app);
    
    //4. Define um ciclo lógico da aplicação para medir latência
    adicionaLoop(app);

    return app;
}

  private void defineModulos(Application app) {
    app.addAppModule("preProcessing", 2); // Postes pre-processando os dados
    app.addAppModule("anomalyDetection", 1); // Fog municipais detectando anomalias
    app.addAppModule("dataCompactor", 2); // Fog municipal compactando os dados
    app.addAppModule("reportSender", 1); // Fogs municipais enviando dados compactados pra nuvem
    app.addAppModule("cloudAnalyzer",3); // Analise de ML na nuvem. 
  }

  private void defineConexoes(Application app) {
    app.addAppEdge("sendData","preProcessing",3,3,"sendData",Tuple.UP,AppEdge.SENSOR); //Sensores mandando dados pro fogs dos bairros.

    app.addAppEdge("preProcessing","anomalyDetection",2,2,"processedData",Tuple.UP,AppEdge.MODULE); //Fogs dos bairros mandando informações processadas para os fogs regionais

    app.addAppEdge("anomalyDetection","dataCompactor",2,2,"chekedData",Tuple.UP,AppEdge.MODULE); //Dentro do fog municipal o modulo de detecção de anomalias chama o compactador de dados.

    app.addAppEdge("dataCompactor","reportSender",1,1,"compactedData",Tuple.UP,AppEdge.MODULE); //Nos fogs municipais o dado compactado e enviado para o modulo de reports

    app.addAppEdge("anomalyDetection","alert",1,1,"alert",Tuple.DOWN,AppEdge.ACTUATOR); // Nos fogs municipais caso exista alguma anomalia um alerta e enviado para os atuadores

    app.addAppEdge("reportSender","cloudAnalyzer",1,1,"reportData",Tuple.UP,AppEdge.MODULE); //O fog municipal enviando os dados para a nuvem.
  }

  private void mapeamentoDeTuplas(Application app) {
    app.addTupleMapping("preProcessing","sendData","processedData", new FractionalSelectivity(1.0));
    
    app.addTupleMapping("anomalyDetection","processedData","alert",new FractionalSelectivity(0.15));

    app.addTupleMapping("anomalyDetection","processedData","chekedData",new FractionalSelectivity(1.0));

    app.addTupleMapping("dataCompactor","chekedData","compactedData",new FractionalSelectivity(1.0));
    
    app.addTupleMapping("reportSender", "compactedData", "reportData", new FractionalSelectivity(1.0));
  }

  private void adicionaLoop(Application app) {
    final AppLoop loop1 = new AppLoop(new ArrayList<String>(){{add("sendData");add("preProcessing");add("anomalyDetection");add("dataCompactor");add("reportSender");}});
    final AppLoop loop2 = new AppLoop(new ArrayList<String>(){{add("sendData");add("preProcessing");add("anomalyDetection");}});
    List<AppLoop> loops = new ArrayList<AppLoop>(){{add(loop1);add(loop2);}};
    app.setLoops(loops);
  }
}
