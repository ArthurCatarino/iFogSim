package org.fog.test.perfeval.testes;

import java.util.ArrayList;

public class ColetaDados {
  //Info da Tupla
  private int tupleId;
  private String tupleType;
  private String tupleSource;
  private long mipsRequired;
  private long networkSize;
  //Info do nó atual
  private int nodeId;
  private long queueSize;
  private double localMIPSAvailable;
  private double localCPUUtilization;
  private TiposDispositivos nodeType;

  //Info dos vizinhos
  private ArrayList<Integer> neighborId;
  private ArrayList<TiposDispositivos> neighborType;
  private ArrayList<Double> neighborMips;
  private ArrayList<Long> neighborQueueSize;
  private ArrayList<Double> neighborCPUUtilization;

  //Decisão
  private int destNode; //Se não houver no de destino então -1
  private double totalLatency;
  private String finalStatus;

public ColetaDados(int tupleId, String tupleType, String tupleSource, 
                     long mipsRequired, long networkSize,                      int nodeId,long queueSize,
                     double localMIPSAvailable, double localCPUUtilization, 
                     TiposDispositivos nodeType,ArrayList<Integer> neighborId,ArrayList<TiposDispositivos> neighborType, 
                     ArrayList<Double> neighborMips,ArrayList<Double> neighborCPUUtilization,ArrayList<Long> neighborQueueSize,int destNode, double totalLatency, String finalStatus) {
                     
    this.tupleId = tupleId;
    this.tupleType = tupleType;
    this.tupleSource = tupleSource;
    this.mipsRequired = mipsRequired;
    this.networkSize = networkSize;
    this.nodeId = nodeId;
    this.localMIPSAvailable = localMIPSAvailable;
    this.localCPUUtilization = localCPUUtilization;
    this.nodeType = nodeType;
    this.neighborId = neighborId; 
    this.neighborType = neighborType;
    this.neighborMips = neighborMips;
    this.neighborCPUUtilization = neighborCPUUtilization;
    this.neighborQueueSize = neighborQueueSize;
    this.destNode = destNode;
    this.totalLatency = totalLatency;
    this.finalStatus = finalStatus;
  }


public int getTupleId() {
    return tupleId;
  }

  public String getTupleType() {
    return tupleType;
  }

  public String getTupleSource() {
    return tupleSource;
  }

  public long getMipsRequired() {
    return mipsRequired;
  }

  public long getNetworkSize() {
    return networkSize;
  }

  public int getNodeId() {
    return nodeId;
  }

  public long getQueueSize() {
    return queueSize;
  }

  public double getLocalMIPSAvailable() {
    return localMIPSAvailable;
  }

  public double getLocalCPUUtilization() {
    return localCPUUtilization;
  }

  public TiposDispositivos getNodeType() {
    return nodeType;
  }

  public ArrayList<Integer> getNeighborId() {
    return neighborId;
  }

  public ArrayList<TiposDispositivos> getNeighborType() {
    return neighborType;
  }

  public ArrayList<Double> getNeighborMips() {
    return neighborMips;
  }

  public ArrayList<Long> getNeighborQueueSize() {
    return neighborQueueSize;
  }

  public ArrayList<Double> getNeighborCPUUtilization() {
    return neighborCPUUtilization;
  }

  public int getDestNode() {
    return destNode;
  }

  public double getTotalLatency() {
    return totalLatency;
  }

  public String getFinalStatus() {
    return finalStatus;
  }
}


