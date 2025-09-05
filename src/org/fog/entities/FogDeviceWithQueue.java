package org.fog.entities;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.fog.test.perfeval.testes.exemplo2.LogsReport;
import org.fog.utils.FogEvents;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.models.PowerModel;

public class FogDeviceWithQueue extends FogDevice {
  private int queueSize = 5;
  private Queue<Tuple> tupleQueue;

  public FogDeviceWithQueue(String name, long mips, int ram, double uplinkBandwidth, double downlinkBandwidth, double ratePerMips, PowerModel powerModel) throws Exception {
    super(name,mips,ram,uplinkBandwidth,downlinkBandwidth,ratePerMips,powerModel);
    tupleQueue = new LinkedList<>();
  }

  public FogDeviceWithQueue(
            String name,
            FogDeviceCharacteristics characteristics,
            VmAllocationPolicy vmAllocationPolicy,
            List<Storage> storageList,
            double schedulingInterval,
            double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency, double ratePerMips) throws Exception {
              super(name,characteristics,vmAllocationPolicy,storageList,schedulingInterval,uplinkBandwidth,downlinkBandwidth,uplinkLatency,ratePerMips);
              tupleQueue = new LinkedList<>();
            }

  @Override
  protected void processTupleArrival(SimEvent ev) {
    Tuple tuple = (Tuple) ev.getData();
    if(tupleQueue.size() >= queueSize) {
      LogsReport.lossPacketReport(tuple);
      return;
    } 
    tupleQueue.add(tuple);
    super.processTupleArrival(ev);
    tupleQueue.poll();
  }
}
