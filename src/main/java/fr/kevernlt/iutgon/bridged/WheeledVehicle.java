package fr.kevernlt.iutgon.bridged;

import fr.kevernlt.iutgon.bridged.bridge.Bridge;
import fr.kevernlt.iutgon.bridged.enums.Way;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Base class for all road vehicles (cars, trucks, convoys, emergency vehicles).
 * Manages segment-by-segment movement across the bridge using threads.
 */
public class WheeledVehicle extends Vehicle {
    private static final java.util.concurrent.atomic.AtomicInteger ID_GEN = new java.util.concurrent.atomic.AtomicInteger(1);
    private final int vehicleId = ID_GEN.getAndIncrement();
    private final Bridge bridge;

    private final Deque<Segment> occupied = new ArrayDeque<>();

    private boolean onEmergencyLane = false;
    private List<Segment> resumeSegments = new ArrayList<>();

    /**
     * Creates a new wheeled vehicle with a given direction and bridge.
     *
     * @param way    the direction of travel (CAR_LEFT_TO_RIGHT or CAR_RIGHT_TO_LEFT)
     * @param bridge reference to the bridge
     */
    public WheeledVehicle(Way way, Bridge bridge) {
        super(way);
        this.bridge = bridge;
        setName(getClass().getSimpleName() + "-" + vehicleId);
    }

    /**
     * Returns the unique vehicle ID.
     *
     * @return unique ID
     */
    public int getVehicleId() {
        return vehicleId;
    }

    /**
     * Returns the size of the vehicle in segments (default is 1).
     *
     * @return number of segments occupied
     */
    public int getSize() {
        return 1;
    }

    /**
     * Returns whether this vehicle blocks the opposite lane (convoys).
     *
     * @return true if opposite way is blocked
     */
    public boolean isBlockingOtherWay() {
        return false;
    }

    /**
     * Returns whether this is an emergency priority vehicle (SAMU).
     *
     * @return true if emergency vehicle
     */
    public boolean isEmergencyVehicle() {
        return false;
    }

    /**
     * Checks if the vehicle is pulled over onto the emergency lane.
     *
     * @return true if on emergency lane
     */
    public synchronized boolean isOnEmergencyLane() {
        return onEmergencyLane;
    }

    /**
     * Sets whether the vehicle is pulled over onto the emergency lane.
     *
     * @param onEmergencyLane emergency lane status
     */
    public synchronized void setOnEmergencyLane(boolean onEmergencyLane) {
        this.onEmergencyLane = onEmergencyLane;
    }

    /**
     * Returns the segments to resume onto when returning from emergency lane.
     *
     * @return list of segments
     */
    public synchronized List<Segment> getResumeSegments() {
        return new ArrayList<>(resumeSegments);
    }

    /**
     * Sets the segments to resume onto when returning from emergency lane.
     *
     * @param resumeSegments list of segments
     */
    public synchronized void setResumeSegments(List<Segment> resumeSegments) {
        this.resumeSegments = new ArrayList<>(resumeSegments);
    }

    /**
     * Returns the segments currently occupied by this vehicle.
     *
     * @return list of occupied segments
     */
    public synchronized List<Segment> getOccupiedSegments() {
        return new ArrayList<>(occupied);
    }

    /**
     * Returns the bridge reference.
     *
     * @return bridge
     */
    public Bridge getBridge() {
        return bridge;
    }

    /**
     * Main vehicle thread execution: enters bridge, moves forward block by block, then leaves.
     */
    @Override
    public void run() {
        try {
            List<Segment> path = bridge.getSegmentsForWay(getWay());
            if (path.isEmpty()) {
                return;
            }

            int step = Math.max(1, this.getSize());
            List<List<Segment>> blocks = new ArrayList<>();
            for (int i = 0; i < path.size(); i += step) {
                int end = Math.min(i + step, path.size());
                blocks.add(path.subList(i, end));
            }

            // Enter the bridge on the initial block of segments
            List<Segment> currentBlock = blocks.get(0);
            bridge.enterBridge(this, currentBlock);
            synchronized (this) {
                occupied.clear();
                occupied.addAll(currentBlock);
            }
            Thread.sleep(1000);

            // Advance block by block
            for (int b = 1; b < blocks.size(); b++) {
                List<Segment> nextBlock = blocks.get(b);
                bridge.advanceToSegments(this, currentBlock, nextBlock);
                synchronized (this) {
                    occupied.clear();
                    occupied.addAll(nextBlock);
                }
                currentBlock = nextBlock;
                Thread.sleep(1000);
            }

            // Exit the bridge
            bridge.leaveBridge(this, currentBlock);
            synchronized (this) {
                occupied.clear();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            synchronized (this) {
                if (!occupied.isEmpty()) {
                    bridge.leaveBridge(this, new ArrayList<>(occupied));
                    occupied.clear();
                }
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + vehicleId;
    }

    @Override
    public int compareTo(Vehicle o) {
        if (o instanceof WheeledVehicle wv) {
            if (this.isEmergencyVehicle() != wv.isEmergencyVehicle()) {
                return this.isEmergencyVehicle() ? -1 : 1;
            }
        }
        return 0;
    }
}
