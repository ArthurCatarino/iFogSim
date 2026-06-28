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
import org.fog.entities.FogDeviceWQHybrid;

import java.util.Random;

public class ExtraiTopologia {

    public static class FogNode {
        public String id;
        public double x;
        public double y;
        public FogDeviceWQHybrid device;

        public FogNode(String id, double x, double y, FogDeviceWQHybrid device) {
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
    private Random geradorAleatorio;

    public ExtraiTopologia(Random r){
        geradorAleatorio = r;
    }

    private TiposDispositivos sorteador(){

        int sorteio = geradorAleatorio.nextInt(100);

        if(sorteio < 20){
            return TiposDispositivos.RaspberryPi2;
        }
        else if (sorteio < 80) {
            return TiposDispositivos.RaspberryPi3B;
        }

        return TiposDispositivos.RaspberryPi4;
    }

public void carregarTopologiaXML(String caminhoArquivo, CriaDispositivos deviceFactory) {
        try {
            double yMedio = 0;
            double xMedio = 0;
            
            // Criar a Nuvem inicial
            FogDeviceWQHybrid cloud = deviceFactory.createFogDeviceWQHybrid("cloud", 100000, 20, 10000, 10000, 0, 0.0005, 150, 50, null, 0, 100000, null);
            
            File inputFile = new File(caminhoArquivo);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            // --- 1. EXTRAÇÃO DOS NÓS (GraphML) ---
            NodeList nList = doc.getElementsByTagName("node");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    
                    // Limpa o ID
                    String idRaw = eElement.getAttribute("id");
                    String idLimpo = idRaw.replaceAll("\\D+", ""); 

                    int idNumerico = Integer.parseInt(idLimpo) + 1;
                    String idFinal = String.valueOf(idNumerico);
                    
                    double x = 0; // Longitude
                    double y = 0; // Latitude

                    // No GraphML, as coordenadas estão em tags <data> com keys específicas
                    NodeList dataTags = eElement.getElementsByTagName("data");
                    for (int j = 0; j < dataTags.getLength(); j++) {
                        Element dataElem = (Element) dataTags.item(j);
                        String key = dataElem.getAttribute("key");
                        
                        if (key.equals("d32")) { // d32 = Longitude (eixo X)
                            x = Double.parseDouble(dataElem.getTextContent());
                        } else if (key.equals("d29")) { // d29 = Latitude (eixo Y)
                            y = Double.parseDouble(dataElem.getTextContent());
                        }
                    }

                    xMedio += x;
                    yMedio += y;
            
                    TiposDispositivos tipoSorteado = sorteador();

                    // Criar o nó Fog
                    FogDeviceWQHybrid novoFog = deviceFactory.createFogDeviceWQHybrid(
                        tipoSorteado.getNome() + "_" + idFinal,
                        tipoSorteado.getMips(), tipoSorteado.getRam(),
                        tipoSorteado.getUpBw(), tipoSorteado.getDownBw(),
                        1, tipoSorteado.getRatePerMips(),
                        tipoSorteado.getBusyPower(), tipoSorteado.getIdlePower(),
                        "cloud", 5, tipoSorteado.getQueueSize(),
                        tipoSorteado
                    );
                    
                    nodes.add(new FogNode(idFinal, x, y, novoFog));
                }
            }

            // Adicionar o nó da Nuvem no centro geográfico 
            if (!nodes.isEmpty()) {
                nodes.add(new FogNode("0", xMedio / nodes.size(), yMedio / nodes.size(), cloud));
            }

            // --- 2. EXTRAÇÃO DOS LINKS (No GraphML usa-se a tag <edge>) ---
            NodeList eList = doc.getElementsByTagName("edge");
            for (int temp = 0; temp < eList.getLength(); temp++) {
                Node nNode = eList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    // Origem e destino estão nos atributos da tag edge
                    String sourceRaw = eElement.getAttribute("source");
                    String targetRaw = eElement.getAttribute("target");

                    int sourceInt = Integer.parseInt(sourceRaw.replaceAll("\\D+", ""));
                    int targetInt = Integer.parseInt(targetRaw.replaceAll("\\D+", ""));
                    
                    double capacity = 1000.0; // Valor padrão de segurança para Fiber

                    // Verifica o tipo de link lendo a key d34
                    NodeList dataTags = eElement.getElementsByTagName("data");
                    for (int j = 0; j < dataTags.getLength(); j++) {
                        Element dataElem = (Element) dataTags.item(j);
                        if (dataElem.getAttribute("key").equals("d34")) {
                            String tipoLink = dataElem.getTextContent();
                            if (tipoLink.equalsIgnoreCase("Optical")) {
                                capacity = 4000.0;
                            } else {
                                capacity = 1000.0;
                            }
                        }
                    }
                    
                    links.add(new FogLink(sourceInt + 1 , targetInt+1, capacity));
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao ler o arquivo XML da rede Oxford: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<FogNode> getNodes() { return nodes; }
    public List<FogLink> getLinks() { return links; }

}