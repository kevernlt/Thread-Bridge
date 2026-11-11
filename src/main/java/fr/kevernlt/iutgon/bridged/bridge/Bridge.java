package fr.kevernlt.iutgon.bridged.bridge;

import fr.kevernlt.iutgon.bridged.FloatingVehicle;
import fr.kevernlt.iutgon.bridged.Segment;
import fr.kevernlt.iutgon.bridged.Vehicle;
import fr.kevernlt.iutgon.bridged.WheeledVehicle;
import fr.kevernlt.iutgon.bridged.enums.BridgeState;
import fr.kevernlt.iutgon.bridged.enums.Way;
import fr.kevernlt.iutgon.bridged.exceptions.IllegalVehicleWay;

import java.util.*;

/**
 * Main Bridge class representing the movable bridge monitor.
 * Synchronizes vehicular and maritime traffic, manages segment access,
 * handles emergency vehicles, and coordinates opening cycles.
 */
public class Bridge {
    private BridgeState currentState;
    private long stateStartTime;

    private final List<Segment> segments;

    private final BridgeRules bridgeRules = new BridgeRules(this);
    private final BridgePrinter bridgePrinter = new BridgePrinter(this);
    private WheeledVehicle activeBlockingVehicle = null;
    private WheeledVehicle suspendedBlockingVehicle = null;
    private boolean isRoadsGateClosed = false;
    private final HashMap<Way, PriorityQueue<Vehicle>> vehicleAtBridgeEntry = new HashMap<>();

    // Maritime navigation
    private final Queue<FloatingVehicle> boatAtBridge = new PriorityQueue<>();
    private FloatingVehicle crossingBoat;
    private boolean boatAlert;
    private final Map<Way, Set<WheeledVehicle>> activeEmergencyVehiclesByWay = new HashMap<>();

    private final Map<Way, List<WheeledVehicle>> vehiclesOnEmergencyLane = new HashMap<>();

    private final BridgeCycleManager bridgeCycleManager;
    private final BridgeBoatPassManager bridgeBoatPassManager;

    /**
     * Creates a new Bridge, initializes managers and starts background cycle threads.
     */
    public Bridge() {
        this.segments = new ArrayList<>();
        this.currentState = BridgeState.CLOSED;
        this.stateStartTime = System.currentTimeMillis();

        this.bridgeCycleManager = new BridgeCycleManager(this);
        this.bridgeCycleManager.setName("BridgeCycleManager");
        this.bridgeCycleManager.start();

        this.bridgeBoatPassManager = new BridgeBoatPassManager(this);
        this.bridgeBoatPassManager.setName("BridgeBoatPassManager");
        this.bridgeBoatPassManager.start();
    }

    /**
     * Changes the current bridge state and notifies all waiting threads.
     *
     * @param currentState new bridge state
     */
    public synchronized void setCurrentState(BridgeState currentState) {
        this.currentState = currentState;
        this.stateStartTime = System.currentTimeMillis();
        notifyAll();
    }

    /**
     * Returns the current mechanical state of the bridge.
     *
     * @return current state
     */
    public synchronized BridgeState getCurrentState() {
        return currentState;
    }

    /**
     * Returns the timestamp when the current state started.
     *
     * @return start timestamp in milliseconds
     */
    public synchronized long getStateStartTime() {
        return stateStartTime;
    }

    /**
     * Returns the elapsed time in milliseconds since the current state started.
     *
     * @return elapsed time in ms
     */
    public synchronized long getStateElapsedTime() {
        return System.currentTimeMillis() - stateStartTime;
    }

    /**
     * Forces immediate bridge opening (useful for UI manual control).
     */
    public synchronized void triggerImmediateOpening() {
        if (currentState == BridgeState.CLOSED) {
            bridgeCycleManager.triggerImmediateOpening();
        }
    }

    /**
     * Adds segments to the bridge layout.
     *
     * @param segments road segments to add
     */
    public void addSegment(Segment... segments) {
        this.segments.addAll(Arrays.asList(segments));
    }

    /**
     * Adds a vehicle to the bridge queue and starts its thread.
     *
     * @param vehicle vehicle to add
     */
    public void addVehicle(Vehicle vehicle) {
        if (vehicle instanceof FloatingVehicle floating) {
            addBoat(floating);
            return;
        }
        synchronized (this) {
            vehicleAtBridgeEntry.computeIfAbsent(vehicle.getWay(), k -> new PriorityQueue<>()).add(vehicle);
            if (vehicle instanceof WheeledVehicle wheeled && wheeled.isEmergencyVehicle()) {
                handleEmergencyVehicleArrival(wheeled);
            }
        }
        vehicle.start();
    }

    /**
     * Returns the bridge ASCII printer.
     *
     * @return bridge printer
     */
    public BridgePrinter getBridgePrinter() {
        return bridgePrinter;
    }

    /**
     * Handles emergency vehicle arrival:
     * - If road gates are open: moves vehicles on the same way to the emergency lane (BAU).
     * - If road gates are closed: vehicles on the bridge finish their course without moving to BAU.
     *
     * @param emergencyVehicle the arriving emergency vehicle
     */
    public synchronized void handleEmergencyVehicleArrival(WheeledVehicle emergencyVehicle) {
        if (!emergencyVehicle.isEmergencyVehicle()) {
            return;
        }
        if (!getCurrentState().equals(BridgeState.CLOSED)) return;

        Way way = emergencyVehicle.getWay();
        Set<WheeledVehicle> activeSet = activeEmergencyVehiclesByWay.computeIfAbsent(way, k -> new LinkedHashSet<>());
        if (activeSet.contains(emergencyVehicle)) {
            if (isRoadsGateClosed) {
                resumeVehiclesFromEmergencyLane(way);
            }
            return;
        }

        activeSet.add(emergencyVehicle);

        // If road gates are closed, vehicles already on the bridge must finish their course and not move to BAU
        if (isRoadsGateClosed) {
            resumeVehiclesFromEmergencyLane(way);
            notifyAll();
            return;
        }

        List<Segment> waySegments = getSegmentsForWay(way);

        // Search for vehicles on main lane and move them to emergency lane
        Set<WheeledVehicle> vehiclesToMove = new LinkedHashSet<>();
        for (Segment s : waySegments) {
            WheeledVehicle occupying = s.getVehicleOccupyingSegment();
            if (occupying != null && !occupying.isEmergencyVehicle() && !occupying.isOnEmergencyLane()) {
                vehiclesToMove.add(occupying);
            }
        }

        List<WheeledVehicle> laneList = vehiclesOnEmergencyLane.computeIfAbsent(way, k -> new ArrayList<>());

        for (WheeledVehicle v : vehiclesToMove) {
            List<Segment> occupiedSegs = waySegments.stream()
                    .filter(s -> s.getVehicleOccupyingSegment() == v)
                    .toList();

            if (!occupiedSegs.isEmpty()) {
                v.setResumeSegments(new ArrayList<>(occupiedSegs));
                v.setOnEmergencyLane(true);
                if (!laneList.contains(v)) {
                    laneList.add(v);
                }

                for (Segment s : occupiedSegs) {
                    s.setEmergencyLaneVehicle(v);
                    s.setVehicleOccupyingSegment(null);
                }

                if (v.equals(activeBlockingVehicle)) {
                    suspendedBlockingVehicle = activeBlockingVehicle;
                    activeBlockingVehicle = null;
                }
            }
        }

        notifyAll();
    }

    /**
     * Resumes vehicles parked on the emergency lane (BAU) back to the main lane.
     *
     * @param way the direction to resume
     */
    private synchronized void resumeVehiclesFromEmergencyLane(Way way) {
        List<WheeledVehicle> interruptedVehicles = vehiclesOnEmergencyLane.remove(way);
        if (interruptedVehicles != null) {
            for (WheeledVehicle v : interruptedVehicles) {
                List<Segment> resumeSegs = v.getResumeSegments();
                for (Segment s : resumeSegs) {
                    s.setVehicleOccupyingSegment(v);
                    s.setEmergencyLaneVehicle(null);
                }
                v.setOnEmergencyLane(false);

                if (v.equals(suspendedBlockingVehicle)) {
                    activeBlockingVehicle = suspendedBlockingVehicle;
                    suspendedBlockingVehicle = null;
                }
            }
        }
    }

    /**
     * Handles when an emergency vehicle leaves the bridge, resuming interrupted vehicles.
     *
     * @param emergencyVehicle departing emergency vehicle
     */
    public synchronized void handleEmergencyVehicleDeparture(WheeledVehicle emergencyVehicle) {
        Way way = emergencyVehicle.getWay();
        Set<WheeledVehicle> activeSet = activeEmergencyVehiclesByWay.get(way);
        if (activeSet != null) {
            activeSet.remove(emergencyVehicle);
        }

        if (!hasActiveEmergencyVehicle(way)) {
            resumeVehiclesFromEmergencyLane(way);
        }

        notifyAll();
    }

    /**
     * Checks if there is an active emergency vehicle on the bridge or waiting at the entry for a way.
     *
     * @param way traffic direction
     * @return true if an emergency vehicle is active
     */
    public synchronized boolean hasActiveEmergencyVehicle(Way way) {
        Set<WheeledVehicle> activeSet = activeEmergencyVehiclesByWay.get(way);
        if (activeSet != null && !activeSet.isEmpty()) {
            return true;
        }
        PriorityQueue<Vehicle> queue = vehicleAtBridgeEntry.get(way);
        if (queue != null && !queue.isEmpty()) {
            Vehicle head = queue.peek();
            if (head instanceof WheeledVehicle wheeled && wheeled.isEmergencyVehicle()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Alias for {@link #hasActiveEmergencyVehicle(Way)}.
     *
     * @param way traffic direction
     * @return true if emergency vehicle active
     */
    public synchronized boolean isEmergencyVehicleActive(Way way) {
        return hasActiveEmergencyVehicle(way);
    }

    /**
     * Returns the vehicles currently parked on the emergency lane for a direction.
     *
     * @param way traffic direction
     * @return list of vehicles on emergency lane
     */
    public synchronized List<WheeledVehicle> getVehiclesOnEmergencyLane(Way way) {
        List<WheeledVehicle> list = vehiclesOnEmergencyLane.get(way);
        return list == null ? Collections.emptyList() : new ArrayList<>(list);
    }

    /**
     * Returns the set of active emergency vehicles for a given way.
     *
     * @param way traffic direction
     * @return set of emergency vehicles
     */
    public synchronized Set<WheeledVehicle> getActiveEmergencyVehicles(Way way) {
        Set<WheeledVehicle> set = activeEmergencyVehiclesByWay.get(way);
        return set == null ? Collections.emptySet() : new LinkedHashSet<>(set);
    }

    /**
     * Synchronized method called by a vehicle to enter the bridge.
     * Blocks if conditions are not met.
     *
     * @param vehicle        entering vehicle
     * @param initialSegments first segments to occupy
     * @throws InterruptedException if interrupted while waiting
     */
    public synchronized void enterBridge(WheeledVehicle vehicle, List<Segment> initialSegments) throws InterruptedException {
        if (!vehicle.getWay().isLogic(vehicle)) {
            throw new IllegalVehicleWay("Vehicle type does not match the way.");
        }

        if (vehicle.isEmergencyVehicle()) {
            handleEmergencyVehicleArrival(vehicle);
        }

        PriorityQueue<Vehicle> queue = vehicleAtBridgeEntry.get(vehicle.getWay());
        Way myWay = vehicle.getWay();
        Way oppositeWay = myWay.getOppositeWay();

        while (shouldWaitToEnter(vehicle, queue, myWay, oppositeWay)) {
            wait();
        }

        if (queue != null) {
            queue.remove(vehicle);
        }

        if (vehicle.isBlockingOtherWay()) {
            activeBlockingVehicle = vehicle;
        }

        for (Segment segment : initialSegments) {
            segment.setVehicleOccupyingSegment(vehicle);
        }

        notifyAll();
    }

    /**
     * Synchronized method called by a vehicle to advance from current block to next block.
     *
     * @param vehicle      advancing vehicle
     * @param currentBlock currently occupied segments
     * @param nextBlock    target segments to occupy
     * @throws InterruptedException if interrupted while waiting
     */
    public synchronized void advanceToSegments(WheeledVehicle vehicle, List<Segment> currentBlock, List<Segment> nextBlock) throws InterruptedException {
        while (vehicle.isOnEmergencyLane()
                || (nextBlock.stream().anyMatch(Segment::hasEmergencyLaneVehicle) && !vehicle.isEmergencyVehicle())
                || !areSegmentsFree(nextBlock, vehicle)
                || currentState != BridgeState.CLOSED) {
            wait();
        }

        for (Segment segment : currentBlock) {
            if (segment.getVehicleOccupyingSegment() == vehicle) {
                segment.setVehicleOccupyingSegment(null);
            }
        }

        for (Segment segment : nextBlock) {
            segment.setVehicleOccupyingSegment(vehicle);
        }

        notifyAll();
    }

    /**
     * Synchronized method called by a vehicle to leave the bridge.
     *
     * @param vehicle   departing vehicle
     * @param lastBlock last occupied segments
     */
    public synchronized void leaveBridge(WheeledVehicle vehicle, List<Segment> lastBlock) {
        while (vehicle.isOnEmergencyLane()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        for (Segment segment : lastBlock) {
            if (segment != null && segment.getVehicleOccupyingSegment() == vehicle) {
                segment.setVehicleOccupyingSegment(null);
            }
        }

        if (vehicle.equals(activeBlockingVehicle)) {
            boolean convoiStillOnBridge = getSegmentsForWay(vehicle.getWay()).stream()
                    .anyMatch(s -> s.getVehicleOccupyingSegment() == vehicle);

            if (!convoiStillOnBridge) {
                activeBlockingVehicle = null;
            }
        }

        if (vehicle.isEmergencyVehicle()) {
            handleEmergencyVehicleDeparture(vehicle);
        }

        notifyAll();
    }

    /**
     * Checks if all segments in the given list are free or already occupied by this vehicle.
     *
     * @param segments list of segments to check
     * @param vehicle  vehicle requesting access
     * @return true if segments are available
     */
    public synchronized boolean areSegmentsFree(List<Segment> segments, WheeledVehicle vehicle) {
        for (Segment segment : segments) {
            WheeledVehicle occupying = segment.getVehicleOccupyingSegment();
            if (occupying != null && occupying != vehicle) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns all segments belonging to the given traffic way.
     *
     * @param way traffic way
     * @return list of segments
     */
    public List<Segment> getSegmentsForWay(Way way) {
        return segments.stream().filter(segment -> segment.getWay() == way).toList();
    }

    /**
     * Returns the map of vehicle entry queues per way.
     *
     * @return entry queues map
     */
    public HashMap<Way, PriorityQueue<Vehicle>> getVehicleAtBridgeEntry() {
        return vehicleAtBridgeEntry;
    }

    /**
     * Evaluates whether a vehicle must wait before entering the bridge.
     */
    private boolean shouldWaitToEnter(WheeledVehicle vehicle, PriorityQueue<Vehicle> queue, Way myWay, Way oppositeWay) {
        if (queue == null || queue.peek() != vehicle) {
            return true;
        }

        // Bridge deck physically unavailable (opening, open, closing)
        if (currentState != BridgeState.CLOSED) {
            return true;
        }

        // If road gates are closed: normal vehicles cannot enter, but emergency vehicles can enter
        if (isRoadsGateClosed && !vehicle.isEmergencyVehicle()) {
            return true;
        }

        // If emergency vehicle: wait only until the initial segments are free
        if (vehicle.isEmergencyVehicle()) {
            return !this.bridgeRules.areFirstSegmentsFree(myWay, Math.max(1, vehicle.getSize()));
        }

        // If not blocking, check if an opposite convoy is on or entering the bridge
        if (!vehicle.isBlockingOtherWay()) {
            boolean convoiApproachingOrCrossingOpposite = this.bridgeRules.hasBlockingVehicleAtEntry(oppositeWay)
                    || (this.activeBlockingVehicle != null && this.activeBlockingVehicle.getWay() == oppositeWay);

            if (convoiApproachingOrCrossingOpposite) {
                return true;
            }
        }

        // If this vehicle is an exceptional convoy
        if (vehicle.isBlockingOtherWay()) {
            if (!this.bridgeRules.isWayCompletelyClear(oppositeWay)) {
                return true;
            }
            if (!this.bridgeRules.areFirstSegmentsFree(myWay, vehicle.getSize())) {
                return true;
            }
            return false;
        }

        return !this.bridgeRules.areFirstSegmentsFree(myWay, Math.max(1, vehicle.getSize()));
    }

    /**
     * Adds a boat to the waiting queue at the bridge and starts its thread.
     *
     * @param boat boat to add
     */
    public synchronized void addBoat(FloatingVehicle boat) {
        boatAlert = true;
        if (!boatAtBridge.contains(boat)) {
            boatAtBridge.add(boat);
        }
        notifyAll();
        if (!boat.isAlive() && boat.getState() == Thread.State.NEW) {
            boat.start();
        }
    }

    /**
     * Executed by BridgeBoatPassManager: lets boats cross one by one while bridge is OPEN.
     *
     * @throws InterruptedException if interrupted while waiting
     */
    public void passNextBoat() throws InterruptedException {
        FloatingVehicle boat = null;
        synchronized (this) {
            while (currentState != BridgeState.OPEN || boatAtBridge.isEmpty()) {
                wait();
            }
            boat = boatAtBridge.poll();
            crossingBoat = boat;
            notifyAll();
        }

        if (boat != null) {
            try {
                boat.cross();
            } finally {
                synchronized (this) {
                    if (crossingBoat == boat) {
                        crossingBoat = null;
                    }
                    if (boatAtBridge.isEmpty()) {
                        boatAlert = false;
                    }
                    notifyAll();
                }
            }
        }
    }

    /**
     * Waits until all boats have finished crossing before closing the bridge.
     *
     * @throws InterruptedException if interrupted while waiting
     */
    public synchronized void waitUntilWaterwayClear() throws InterruptedException {
        wait(2000); // 2 second grace period
        while (!boatAtBridge.isEmpty() || crossingBoat != null) {
            wait();
        }
    }

    /**
     * Returns a copy of the boat queue.
     *
     * @return boat queue copy
     */
    public synchronized Queue<FloatingVehicle> getBoatAtBridge() {
        return new PriorityQueue<>(boatAtBridge);
    }

    /**
     * Returns the list of waiting boats.
     *
     * @return waiting boats list
     */
    public synchronized List<FloatingVehicle> getBoatsWaiting() {
        return new ArrayList<>(boatAtBridge);
    }

    /**
     * Returns the total count of waiting boats.
     *
     * @return waiting boats count
     */
    public synchronized int getWaitingBoatsCount() {
        return boatAtBridge.size();
    }

    /**
     * Returns the count of waiting boats for a given direction.
     *
     * @param way navigation direction
     * @return count of boats
     */
    public synchronized int getWaitingBoatsCount(Way way) {
        return (int) boatAtBridge.stream().filter(b -> b.getWay() == way).count();
    }

    /**
     * Returns the boat currently crossing the bridge.
     *
     * @return crossing boat, or null if none
     */
    public synchronized FloatingVehicle getCrossingBoat() {
        return crossingBoat;
    }

    /**
     * Checks if boat alert is active.
     *
     * @return true if boat alert
     */
    public synchronized boolean isBoatAlert() {
        return boatAlert || !boatAtBridge.isEmpty() || crossingBoat != null;
    }

    /**
     * Checks if road gates are closed.
     *
     * @return true if gates are closed
     */
    public boolean isRoadsGateClosed() {
        return isRoadsGateClosed;
    }

    /**
     * Sets whether road gates are closed. When closing, vehicles on the emergency lane resume.
     *
     * @param isRoadsGateClosed gate status
     */
    public synchronized void setRoadsGateClosed(boolean isRoadsGateClosed) {
        this.isRoadsGateClosed = isRoadsGateClosed;
        if (isRoadsGateClosed) {
            resumeVehiclesFromEmergencyLane(Way.CAR_LEFT_TO_RIGHT);
            resumeVehiclesFromEmergencyLane(Way.CAR_RIGHT_TO_LEFT);
        }
        notifyAll();
    }

    /**
     * Waits until both road directions are completely clear of vehicles.
     *
     * @throws InterruptedException if interrupted while waiting
     */
    public synchronized void waitUntilRoadsClear() throws InterruptedException {
        while (!this.bridgeRules.isWayCompletelyClear(Way.CAR_LEFT_TO_RIGHT) ||
                !this.bridgeRules.isWayCompletelyClear(Way.CAR_RIGHT_TO_LEFT)) {
            wait();
        }
    }

    /**
     * Sets boat alert status.
     *
     * @param b alert status
     */
    public void setBoatAlert(boolean b) {
        this.boatAlert = b;
    }

    /**
     * Sets the currently crossing boat.
     *
     * @param o crossing boat
     */
    public void setCrossingBoat(FloatingVehicle o) {
        this.crossingBoat = o;
    }
}
