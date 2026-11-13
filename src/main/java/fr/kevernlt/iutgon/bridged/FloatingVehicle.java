package fr.kevernlt.iutgon.bridged;

import fr.kevernlt.iutgon.bridged.bridge.Bridge;
import fr.kevernlt.iutgon.bridged.enums.Way;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a water vehicle (boat) navigating across the river under the bridge.
 */
public class FloatingVehicle extends Vehicle {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(1);

    private final int boatId;
    private final Bridge bridge;
    private volatile boolean exit = false;

    /**
     * Creates a new boat with a direction and bridge reference.
     *
     * @param way    navigation direction (BOAT_NORTH or BOAT_SOUTH)
     * @param bridge reference to the bridge
     */
    public FloatingVehicle(Way way, Bridge bridge) {
        super(way);
        this.bridge = bridge;
        this.boatId = ID_GENERATOR.getAndIncrement();
        setName(getClass().getSimpleName() + "-" + boatId);
    }

    /**
     * Creates a new boat without a bridge reference.
     *
     * @param way navigation direction
     */
    public FloatingVehicle(Way way) {
        this(way, null);
    }

    /**
     * Returns the unique boat ID.
     *
     * @return boat ID
     */
    public int getBoatId() {
        return boatId;
    }

    /**
     * Returns the bridge associated with this boat.
     *
     * @return the bridge
     */
    public Bridge getBridge() {
        return bridge;
    }

    /**
     * Checks if this boat has finished crossing and exited.
     *
     * @return true if exited
     */
    public boolean isExited() {
        return exit;
    }

    /**
     * Thread loop that keeps running while waiting to cross.
     */
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

    /**
     * Simulates crossing through the opened bridge waterway (takes 2 seconds).
     *
     * @throws InterruptedException if interrupted during traversal
     */
    public void cross() throws InterruptedException {
        Thread.sleep(2000);
        exit = true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + boatId + "(" + (getWay() == Way.BOAT_NORTH ? "NORD" : "SUD") + ")";
    }

    @Override
    public int compareTo(Vehicle o) {
        if (o instanceof FloatingVehicle fv) {
            return Integer.compare(this.boatId, fv.boatId);
        }
        return super.compareTo(o);
    }
}
