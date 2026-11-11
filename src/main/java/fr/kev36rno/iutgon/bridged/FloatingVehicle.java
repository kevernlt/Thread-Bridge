package fr.kev36rno.iutgon.bridged;

import fr.kev36rno.iutgon.bridged.bridge.Bridge;
import fr.kev36rno.iutgon.bridged.enums.Way;

public class FloatingVehicle extends Vehicle {

    private Bridge bridge;
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
    }
}
