package org.fog.test.perfeval.testes.exemplo1;

import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.cloudbus.cloudsim.*;
import org.fog.entities.*;


import java.util.*;

public class Logica {

  private String appId;
  private int userId;

  public Logica(String appId, int userId) {
    this.appId = appId;
    this.userId = userId;
  }

  public Application criaAplicacao() {
    Application app = Application.createApplication(appId, userId);
    app.setUserId(userId);

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
    app.addAppModule("AnalisaTemperatura", 10);   
    // MIPS = 10 - Milhoes de Instruções por segundo define a potencia do fog.  
    app.addAppModule("AnalisaNuvem",10);
  }

  private void defineConexoes(Application app) {
    app.addAppEdge("SensorDeTemperatura", "AnalisaTemperatura", 1000, 500,"SensorDeTemperatura", Tuple.UP, AppEdge.SENSOR); // Sensor envia dado pro fog.

    app.addAppEdge("AnalisaTemperatura", "AnalisaNuvem",1000,500,"AnalisaTemperaturaNuvem",Tuple.UP,AppEdge.MODULE);

    app.addAppEdge("AnalisaTemperatura", "Alerta", 100, 50,"Alerta", Tuple.DOWN, AppEdge.ACTUATOR); // Fog manda o alerta pro actuator
  }

  private void mapeamentoDeTuplas(Application app) {
    app.addTupleMapping("AnalisaTemperatura", "SensorDeTemperatura", "Alerta", new FractionalSelectivity(1.0)); 
    // Aqui e definido o tipo de dado que sera enviado entre os modulos, alem de no FractionalSelectivity : definir a frequencia. 
    //Neste casso 40% das temperaturas coletadas enviarao um alerta.
        app.addTupleMapping("AnalisaTemperatura", "SensorDeTemperatura", "AnalisaTemperaturaNuvem", new FractionalSelectivity(0.4)); 

  }

  private void adicionaLoop(Application app) {
    final AppLoop loop = new AppLoop(Arrays.asList(
        "SensorDeTemperatura", "AnalisaTemperatura","Alerta" //Define o caminho do dado.
    ));
    app.setLoops(Collections.singletonList(loop));
    System.out.println("Loops definidos: " + app.getLoops());

  }

}
