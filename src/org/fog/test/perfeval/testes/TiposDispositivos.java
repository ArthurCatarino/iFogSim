package org.fog.test.perfeval.testes;

public enum TiposDispositivos {
    
    RaspberryPi2("RaspberryPi2", 1500, 1024, 12500000, 12500000, 3.0, 1.5, 0.00002),
    RaspberryPi3B("RaspberryPi3B+", 3000, 1024, 37500000, 37500000, 4.0, 2.0, 0.00003),
    RaspberryPi4("RaspberryPi4", 6000, 2048, 125000000, 125000000, 6.0, 2.7, 0.00005);

    private final String nome;
    private final int mips;
    private final int ram;
    private final int upBw;
    private final int downBw;
    private final double ratePerMips;
    private final double busyPower;
    private final double idlePower;
    private final int queueSize;

    TiposDispositivos(String nome, int mips, int ram, int upBw, int downBw, double busyPower, double idlePower,double ratePerMips) {
        this.nome = nome;
        this.mips = mips;
        this.ram = ram;
        this.upBw = upBw;
        this.downBw = downBw;
        this.ratePerMips = ratePerMips;
        this.busyPower = busyPower;
        this.idlePower = idlePower;
        this.queueSize = 2*mips;
    }


    public String getNome() { return nome; }
    public int getMips() { return mips; }
    public int getRam() { return ram; }
    public double getBusyPower() { return busyPower; }
    public double getIdlePower() { return idlePower; }
    public double getRatePerMips() { return ratePerMips; }
    public int getUpBw() { return upBw; }
    public int getDownBw() { return downBw; }
    public int getQueueSize() { return queueSize; }
}