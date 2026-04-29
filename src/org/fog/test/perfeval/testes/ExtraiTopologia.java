package org.fog.test.perfeval.testes;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.fog.entities.FogDeviceWQLessLatency;

import java.util.Random;

public class ExtraiTopologia {

    public static class FogNode {
        public String id;
        public double x;
        public double y;
        public FogDeviceWQLessLatency device;

        public FogNode(String id, double x, double y, FogDeviceWQLessLatency device) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.device = device;
        }
        
        public double distanciaEuclidiana(FogNode d2){
            double deltaX = d2.x - this.x;
            double deltaY = d2.y - this.y;
            return Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
        }
    }

    public static class FogLink {
        public int source;
        public int target;
        public double capacity;

        public FogLink(int source, int target, double capacity) {
            this.source = source;
            this.target = target;
            this.capacity = capacity;
        }
    }

    private List<FogNode> nodes = new ArrayList<>();
    private List<FogLink> links = new ArrayList<>();

    private TiposDispositivos sorteador(){
        Random geradorAleatorio = new Random();
        int sorteio = geradorAleatorio.nextInt(100);

        if(sorteio < 20){
            return TiposDispositivos.RaspberryPi2;
        }
        else if (sorteio < 80) {
            return TiposDispositivos.RaspberryPi3B;
        }

        return TiposDispositivos.RaspberryPi4;
    }

    public void carregarTopologiaXML(String caminhoArquivo,CriaDispositivos deviceFactory) {
        try {
            int yMedio = 0;
            int xMedio = 0;
            FogDeviceWQLessLatency cloud = deviceFactory.createFogDeviceWQLessLatency("cloud", 100000 , 20, 10000, 10000, 0, 0.0005, 150,50,null,0,100000);
            File inputFile = new File(caminhoArquivo);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            // Extraindo os Nós
            NodeList nList = doc.getElementsByTagName("node");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    
                    String id = eElement.getAttribute("id");
                    double x = Double.parseDouble(eElement.getElementsByTagName("x").item(0).getTextContent());
                    xMedio += x;
                    double y = Double.parseDouble(eElement.getElementsByTagName("y").item(0).getTextContent());
                    yMedio += y;
            
                    TiposDispositivos tipoSorteado = sorteador();

                    //Aqui e decidido o tipo de no Fog usado
                    FogDeviceWQLessLatency novoFog = deviceFactory.createFogDeviceWQLessLatency(tipoSorteado.getNome()+"_"+id,tipoSorteado.getMips(),tipoSorteado.getRam(),tipoSorteado.getUpBw(),tipoSorteado.getDownBw(),1,tipoSorteado.getRatePerMips(),tipoSorteado.getBusyPower(),tipoSorteado.getIdlePower(),"cloud",5,tipoSorteado.getQueueSize());
                    
                    nodes.add(new FogNode(id, x, y,novoFog));
                }
            }
            nodes.add(new FogNode("0",xMedio/nodes.size(),yMedio/nodes.size(),cloud));

            // Extraindo os Links
            NodeList lList = doc.getElementsByTagName("link");
            for (int temp = 0; temp < lList.getLength(); temp++) {
                Node nNode = lList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    
                    NodeList preInstalled = eElement.getElementsByTagName("preInstalledModule");
                    if (preInstalled.getLength() == 0) {
                        continue; 
                    }

                    String sourceRaw = eElement.getElementsByTagName("source").item(0).getTextContent();
                    String targetRaw = eElement.getElementsByTagName("target").item(0).getTextContent();

                    int sourceInt = Integer.parseInt(sourceRaw.replaceAll("\\D+", ""));
                    int targetInt = Integer.parseInt(targetRaw.replaceAll("\\D+", ""));
                    
                    Element preInstElement = (Element) preInstalled.item(0);
                    double capacity = Double.parseDouble(preInstElement.getElementsByTagName("capacity").item(0).getTextContent());
                    
                    links.add(new FogLink(sourceInt, targetInt, capacity));
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao ler o arquivo XML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<FogNode> getNodes() { return nodes; }
    public List<FogLink> getLinks() { return links; }

}