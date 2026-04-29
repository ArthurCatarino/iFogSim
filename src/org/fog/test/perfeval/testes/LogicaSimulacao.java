package org.fog.test.perfeval.testes;

import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.test.perfeval.testes.TipoSensor;
import org.fog.entities.*;
import java.util.*;

public class LogicaSimulacao {
  
  private String appId;
  private int brokerID;

  public LogicaSimulacao(String appId, int brokerID) {
    this.appId = appId;
    this.brokerID = brokerID;
  }

  public Application criaApp() {
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
    app.addAppModule("Process",1);
  }

  private void defineConexoes(Application app) {
    app.addAppEdge("TUPLA","Process",140,150,"TUPLA",Tuple.UP,AppEdge.SENSOR);
  }

  private void mapeamentoDeTuplas(Application app) {
        // Mapeia a entrada (SENSOR_A) para uma saída fictícia (TUPLA_FANTASMA)
        // Isso força a criação e execução do Cloudlet
        app.addTupleMapping("Process", "TUPLA", "TUPLA_FANTASMA", new FractionalSelectivity(1.0));
  }

   private void adicionaLoop(Application app) {
    List<AppLoop> loops = new ArrayList<>();
    final AppLoop loop = new AppLoop(
      new ArrayList<String>(){{
            add("TUPLA"); 
            add("Process");
              }});

    loops.add(loop);
    app.setLoops(loops);

   }
}
