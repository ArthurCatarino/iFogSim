package org.fog.test.perfeval.testes.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.HashMap;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.Application;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceWithQueueLessSlack;
import org.fog.entities.Sensor;
import org.fog.entities.SensorLessSlack;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementMapping;
import org.fog.test.perfeval.testes.CriaDispositivos;
import org.fog.entities.dataEstructures.NetworkMatrix;

public class GeradorTopologiaClusterLessSlack {

    private static Random rand = new Random();
    private static double gridSize = 100.0;
    
    // Configurações da Topologia
    private static final int NUM_SENSORES_POR_CLUSTER = 100 ;    
    private static final int NUM_DISPOSITIVOS_POR_CLUSTER = (int) (Math.log(NUM_SENSORES_POR_CLUSTER)/Math.log(2)); 
    private static final double RAIO_COMUNICACAO_P2P = 5.0;   
    private static final double DESVIO_PADRAO_CLUSTER = 10.0;

    private static FogBroker broker;
    private static CriaDispositivos deviceFactory; 
    private static List<Sensor> sensores = new ArrayList<>();
    private static List<FogDevice> fogs = new ArrayList<>();
    private static List<Actuator> actuators = new ArrayList<>();
    private static String appId = "ClusterApp";

    public static void main(String[] args) {
        try {
            Log.disable();
            CloudSim.init(1, java.util.Calendar.getInstance(), false);

            broker = new FogBroker("broker");

            deviceFactory = new CriaDispositivos(broker.getId(), appId);
            criaTopologia();
            //actuators.add(new Actuator("Atuador", 1, appId, "Alert"));
  
            sensores = deviceFactory.getSensors();
            fogs = deviceFactory.getFogDevices();
            
            LogicaCluster logica = new LogicaCluster(appId,broker.getId());
            Application app = logica.criaApp();
            ModuleMapping mapping = criaMapeamento();
            Controller controller = new Controller("controller", fogs, sensores,actuators);
            
            controller.submitApplication(app,new ModulePlacementMapping(fogs, app, mapping));

            System.out.println("\nTopologia montada. Iniciando simulação...");
            CloudSim.startSimulation();
            CloudSim.stopSimulation();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ModuleMapping criaMapeamento() {
        ModuleMapping mapping = ModuleMapping.createModuleMapping();

        for(FogDevice f : fogs) {
            mapping.addModuleToDevice("Process", f.getName());
        }
        return mapping;
    } 
   
    private static void criaTopologia() {
        int filaNuvem = 12;
        int filaLider = 9;
        int filaWorker = 6;

       // 1. Criar a NUVEM no Centro (50, 50)
            FogDeviceWithQueueLessSlack cloud = deviceFactory.createFogDeviceWithQueueLessSlack("cloud", 1100 , 20, 10000, 10000, 0, 0.7, 10,5,null,0,filaNuvem);
            DeviceLocation cloudLocation = new DeviceLocation(cloud, new Point(50, 50));
            System.out.println("Nuvem criada em 50 50");

            // 2. Definir os Centros dos 4 Quadrantes
            List<Point> centrosQuadrantes = new ArrayList<>();
            centrosQuadrantes.add(new Point(25, 25)); // Q1
            centrosQuadrantes.add(new Point(75, 25)); // Q2
            centrosQuadrantes.add(new Point(25, 75)); // Q3
            centrosQuadrantes.add(new Point(75, 75)); // Q4

            int clusterId = 1;
            // Lista que vai guardar TODOS os nós (Líderes + Workers) para cálculo de distância
            List<DeviceLocation> todosOsFogs = new ArrayList<>();

            // --- LOOP PRINCIPAL: GERAÇÃO DOS CLUSTERS ---
            for (Point centro : centrosQuadrantes) {
                System.out.println("\n---------------------------------------------");
                System.out.println("Gerando Cluster " + clusterId + " em torno de " + centro);

                String nomeLider = "Lider_Q" + clusterId;
                
                // A. CRIAR LÍDER (Conectado à Nuvem)
                // Usei cast para garantir que seja do tipo certo
                FogDeviceWithQueueLessSlack fogLider = (FogDeviceWithQueueLessSlack) deviceFactory.createFogDeviceWithQueueLessSlack(
                    nomeLider, 
                    550, 4, 10000, 10000, 1, 0.2, 6.0, 2.0, 
                    "cloud", // Pai = Cloud
                    50, 
                    filaLider
                );
                NetworkMatrix.addLatency(fogLider.getId(), cloudLocation.device.getId(),centro.distancia(cloudLocation.p));

                

                // B. CRIAR DISPOSITIVOS WORKERS (Conectados ao Líder)
                for (int i = 0; i < NUM_DISPOSITIVOS_POR_CLUSTER; i++) {
                    Point pos = gerarPosicaoGaussiana(centro);
                    String nomeFog = "Fog_Q" + clusterId + "_" + i;

                    FogDeviceWithQueueLessSlack fogNode = (FogDeviceWithQueueLessSlack) deviceFactory.createFogDeviceWithQueueLessSlack(
                        nomeFog, 
                        300, 1, 10000, 10000, 2, 0.01, 4, 1, 
                        nomeLider, // Pai = Líder do Cluster (Conexão Hierárquica)
                        5, 
                        filaWorker
                    );
                    
                    // CORREÇÃO: Adicionar na lista global para P2P e Sensores
                    todosOsFogs.add(new DeviceLocation(fogNode, pos));
                    fogNode.addNeighbor(fogLider);
                    NetworkMatrix.addLatency(fogNode.getId(),fogLider.getId(),pos.distancia(centro));
                    System.out.println("  -> Fog Criado: " + nomeFog + " em " + pos);
                }

                // C. CRIAR SENSORES (Conectados ao dispositivo mais próximo deste cluster ou vizinhos)
                for (int s = 0; s < NUM_SENSORES_POR_CLUSTER; s++) {
                    Point posSensor = gerarPosicaoGaussiana(centro);
                    String nomeSensor = "Sensor_Q" + clusterId + "_" + s;

                    // Procura o gateway mais próximo na lista geral (pode ser o líder ou um worker)
                    DeviceLocation gateway = encontrarGatewayMaisProximo(posSensor, todosOsFogs);
                    
                    
                    //Sorteio o tipo do sensor
                    TipoSensor tipoSorteado = TipoSensor.sortear();

                    // Cria sensor (Atenção: verifique os parametros do seu createSensor)
                    SensorLessSlack sensor = deviceFactory.createSensorLessSlack(nomeSensor, tipoSorteado.getTupleType(),tipoSorteado.getFrequenciaMs(),gateway.device.getName(),2.0);
                    NetworkMatrix.addLatency(-1*sensor.getId(),gateway.device.getId(),posSensor.distancia(gateway.p));
                    
                    for(DeviceLocation d : todosOsFogs) {
                        if(d.p.distancia(posSensor) < RAIO_COMUNICACAO_P2P) {
                            sensor.addDest(d.device);
                            NetworkMatrix.addLatency(-1*sensor.getId(),d.device.getId(),posSensor.distancia(d.p)); // adiciona o id dos sensores como numeros negativos para nao haver conflito e nem a necessidade de criar outra matriz
                        }
                    }

                    //System.out.println("  -> Sensor " + nomeSensor + " (" + posSensor + ") conectado a " + gateway.device.getName());
                }
                clusterId++;
            }

            // D. CALCULAR VIZINHANÇA P2P (Horizontal)
            System.out.println("\n--- Calculando Vizinhança (Raio " + RAIO_COMUNICACAO_P2P + ") ---");
            int linksP2P = 0;
            
            for (int i = 0; i < todosOsFogs.size(); i++) {
                for (int j = i + 1; j < todosOsFogs.size(); j++) {
                    DeviceLocation d1 = todosOsFogs.get(i);
                    DeviceLocation d2 = todosOsFogs.get(j);

                    // Verifica se ambos são da classe correta (segurança) e calcula distância
                    if (d1.p.distancia(d2.p) <= RAIO_COMUNICACAO_P2P) {
        
                        NetworkMatrix.addLatency(d1.device.getId(),d2.device.getId(),d1.p.distancia(d2.p));
                        NetworkMatrix.addLatency(d2.device.getId(),d1.device.getId(),d1.p.distancia(d2.p));

                        d1.device.addNeighbor(d2.device); // Adiciona-os como vizinhos
                        d2.device.addNeighbor(d1.device);
                        
                        System.out.println("    Link P2P: " + d1.device.getName() + " <--> " + d2.device.getName() + " (Dist: " + String.format("%.1f", d1.p.distancia(d2.p)) + ")");
                        linksP2P++;
                    }
                }
            }
            System.out.println("Total de Links P2P criados: " + linksP2P);
    }

    private static Point gerarPosicaoGaussiana(Point centro) {
        double x = centro.x + (rand.nextGaussian() * DESVIO_PADRAO_CLUSTER);
        double y = centro.y + (rand.nextGaussian() * DESVIO_PADRAO_CLUSTER);
        x = Math.max(0, Math.min(gridSize, x));
        y = Math.max(0, Math.min(gridSize, y));
        return new Point(x, y);
    }

    private static DeviceLocation encontrarGatewayMaisProximo(Point sensorPos, List<DeviceLocation> candidates) {
        DeviceLocation bestGw = null;
        double minDist = Double.MAX_VALUE; 

        for(DeviceLocation dl : candidates) {
            double d = sensorPos.distancia(dl.p);
            if(d < minDist) {
                minDist = d;
                bestGw = dl;
            }
        }
        return bestGw;
    }


    static class DeviceLocation {
        FogDeviceWithQueueLessSlack device;
        Point p;
        public DeviceLocation(FogDeviceWithQueueLessSlack device, Point p) {
            this.device = device;
            this.p = p;
        }
    }
}