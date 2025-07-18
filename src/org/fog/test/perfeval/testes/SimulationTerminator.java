package org.fog.test.perfeval.testes; // ou o pacote que usar

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

public class SimulationTerminator extends SimEntity {
    private double stopTime;
    public static final int STOP_SIMULATION = 100;

    public SimulationTerminator(String name, double stopTime) {
        super(name);
        this.stopTime = stopTime;
    }

    @Override
    public void startEntity() {
        schedule(getId(), stopTime, STOP_SIMULATION);
    }

    @Override
    public void processEvent(SimEvent ev) {
        if (ev.getTag() == STOP_SIMULATION) {
            CloudSim.stopSimulation();
        }
    }

    @Override
    public void shutdownEntity() {}
}
