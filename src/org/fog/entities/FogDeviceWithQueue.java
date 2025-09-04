package org.fog.entities;

import java.util.LinkedList;
import java.util.Queue;
import org.fog.test.perfeval.testes.exemplo2.LogsReport;

import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.models.PowerModel;

public class FogDeviceWithQueue extends FogDevice {
  private int queueSize;
  private Queue<Tuple> tupleQueue;

  public FogDeviceWithQueue(int queueSize, String name, long mips, int ram, double uplinkBandwidth, double downlinkBandwidth, double ratePerMips, PowerModel powerModel) throws Exception {
    super(name,mips,ram,uplinkBandwidth,downlinkBandwidth,ratePerMips,powerModel);
    this.queueSize = queueSize;
    tupleQueue = new LinkedList<>();
  }

  protected void processTupleArrival(SimEvent ev) {
    Tuple tuple = (Tuple) ev.getData();
    if(tupleQueue.size() >= queueSize) {
      LogsReport.lossPacketReport(tuple);
      return; // Descarta a tupla pois a fila esta cheia
    }
    tupleQueue.add(tuple);
    super.processTupleArrival(ev);
    tupleQueue.remove();
  }
  
}
