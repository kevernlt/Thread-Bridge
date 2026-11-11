package fr.kev36rno.iutgon.bridged;

import fr.kev36rno.iutgon.bridged.bridge.Bridge;
import fr.kev36rno.iutgon.bridged.enums.Way;

import java.util.concurrent.atomic.AtomicInteger;

public class FloatingVehicle extends Vehicle {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);

    private final int boatId;
    private final Bridge bridge;
    private volatile boolean exit = false;

    public FloatingVehicle(Way way, Bridge bridge) {
        super(way);
        this.bridge = bridge;
        this.boatId = ID_GENERATOR.getAndIncrement();
        setName(getClass().getSimpleName() + "-" + boatId);
    }

    public FloatingVehicle(Way way) {
        this(way, null);
    }

    public int getBoatId() {
        return boatId;
    }

    public Bridge getBridge() {
        return bridge;
    }

    public boolean isExited() {
        return exit;
    }

    @Override
    public void run() {
        while (!exit && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void cross() throws InterruptedException {
        sleep(1000);
        exit = true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + boatId +
                ", way=" + getWay() +
                '}';
    }

    @Override
    public int compareTo(Vehicle o) {
        if (o instanceof FloatingVehicle fv) {
            return Integer.compare(this.boatId, fv.boatId);
        }
        return super.compareTo(o);
    }
}
