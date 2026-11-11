package fr.kevernlt.iutgon.bridged;

import fr.kevernlt.iutgon.bridged.bridge.Bridge;
import fr.kevernlt.iutgon.bridged.enums.Way;

public class FloatingVehicle extends Vehicle {

    private final Bridge bridge;
    private volatile boolean exit = false;

    public FloatingVehicle(Way way,Bridge bridge) {
        super(way);
        this.bridge=bridge;
    }

    @Override
    public void run() {
        while (!exit) {
            Thread.onSpinWait();
        }
    }

    public void cross() throws InterruptedException {
        sleep(1000);
        exit=true;
        bridge.setCrossingBoat(null);
    }
}
