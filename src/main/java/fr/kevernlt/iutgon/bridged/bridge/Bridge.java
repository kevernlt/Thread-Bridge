package fr.kev36rno.iutgon.bridged.bridge;

import fr.kev36rno.iutgon.bridged.FloatingVehicle;
import fr.kev36rno.iutgon.bridged.Segment;
import fr.kev36rno.iutgon.bridged.Vehicle;
import fr.kev36rno.iutgon.bridged.WheeledVehicle;
import fr.kev36rno.iutgon.bridged.enums.BridgeState;
import fr.kev36rno.iutgon.bridged.enums.Way;
import fr.kev36rno.iutgon.bridged.exceptions.IllegalVehicleWay;

import java.util.*;

public class Bridge {
    private BridgeState currentState;

    private final List<Segment> segments;

    private final BridgeRules bridgeRules = new BridgeRules(this);
    private final BridgePrinter bridgePrinter = new BridgePrinter(this);
    private WheeledVehicle activeBlockingVehicle = null;
    private WheeledVehicle suspendedBlockingVehicle = null;
    private boolean isRoadsGateClosed = false;
    private final HashMap<Way, PriorityQueue<Vehicle>> vehicleAtBridgeEntry = new HashMap<>();
    private final Queue<FloatingVehicle> boatAtBridge = new PriorityQueue<>();

    private FloatingVehicle crossingBoat = null;
    private boolean boatAlert = false;

    private final Map<Way, Set<WheeledVehicle>> activeEmergencyVehiclesByWay = new HashMap<>();

    private final Map<Way, List<WheeledVehicle>> vehiclesOnEmergencyLane = new HashMap<>();

    public Bridge() {
        this.segments = new ArrayList<>();
        this.currentState = BridgeState.CLOSED;

        BridgeCycleManager bridgeCycleManager = new BridgeCycleManager(this);
        bridgeCycleManager.start();
    }

    public synchronized void setCurrentState(BridgeState currentState) {
        this.currentState = currentState;
        notifyAll();
    }

    public BridgeState getCurrentState() {
        return currentState;
    }

    public void addSegment(Segment... segments) {
        this.segments.addAll(Arrays.asList(segments));
    }

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

    public BridgePrinter getBridgePrinter() {
        return bridgePrinter;
    }

    /**
     * This method handles emergency vehicle arrival and place all the cars on the emergency lane.
     */
    public synchronized void handleEmergencyVehicleArrival(WheeledVehicle emergencyVehicle) {
        if (!emergencyVehicle.isEmergencyVehicle()) {
            return;
        }

        Way way = emergencyVehicle.getWay();
        Set<WheeledVehicle> activeSet = activeEmergencyVehiclesByWay.computeIfAbsent(way, k -> new LinkedHashSet<>());
        if (activeSet.contains(emergencyVehicle)) {
            return;
        }

        activeSet.add(emergencyVehicle);

        List<Segment> waySegments = getSegmentsForWay(way);

        // Search for vehicles that are not on the emergency lane and move them to the emergency lane
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
     * Handle when emergency vehicle quit the bridge.
     */
    public synchronized void handleEmergencyVehicleDeparture(WheeledVehicle emergencyVehicle) {
        Way way = emergencyVehicle.getWay();
        Set<WheeledVehicle> activeSet = activeEmergencyVehiclesByWay.get(way);
        if (activeSet != null) {
            activeSet.remove(emergencyVehicle);
        }

        if (!hasActiveEmergencyVehicle(way)) {
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

        notifyAll();
    }

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

    public synchronized boolean isEmergencyVehicleActive(Way way) {
        return hasActiveEmergencyVehicle(way);
    }

    public synchronized List<WheeledVehicle> getVehiclesOnEmergencyLane(Way way) {
        List<WheeledVehicle> list = vehiclesOnEmergencyLane.get(way);
        return list == null ? Collections.emptyList() : new ArrayList<>(list);
    }

    public synchronized Set<WheeledVehicle> getActiveEmergencyVehicles(Way way) {
        Set<WheeledVehicle> set = activeEmergencyVehiclesByWay.get(way);
        return set == null ? Collections.emptySet() : new LinkedHashSet<>(set);
    }

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

    public synchronized void advanceToSegments(WheeledVehicle vehicle, List<Segment> currentBlock, List<Segment> nextBlock) throws InterruptedException {
        while (vehicle.isOnEmergencyLane()
                // check if on the next segment, there is vehicle on emergency lane
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

    public synchronized void leaveBridge(WheeledVehicle vehicle, List<Segment> lastBlock) {
        while (vehicle.isOnEmergencyLane() || (!vehicle.isEmergencyVehicle() && hasActiveEmergencyVehicle(vehicle.getWay()))) {
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

    public synchronized boolean areSegmentsFree(List<Segment> segments, WheeledVehicle vehicle) {
        for (Segment segment : segments) {
            WheeledVehicle occupying = segment.getVehicleOccupyingSegment();
            if (occupying != null && occupying != vehicle) {
                return false;
            }
        }
        return true;
    }

    public List<Segment> getSegmentsForWay(Way way) {
        return segments.stream().filter(segment -> segment.getWay() == way).toList();
    }

    public HashMap<Way, PriorityQueue<Vehicle>> getVehicleAtBridgeEntry() {
        return vehicleAtBridgeEntry;
    }

    private boolean shouldWaitToEnter(WheeledVehicle vehicle, PriorityQueue<Vehicle> queue, Way myWay, Way oppositeWay) {
        if (queue == null || queue.peek() != vehicle) {
            return true;
        }

        // Bridge is not closed or roads gate is closed to prepare opening
        if (currentState != BridgeState.CLOSED || isRoadsGateClosed) {
            return true;
        }

        // if i'm an emergency vehicle, i need to wait if the first segments are not free for my size anyway (but theoricly other vehicles go to BAU)
        if (vehicle.isEmergencyVehicle()) {
            return !this.bridgeRules.areFirstSegmentsFree(myWay, Math.max(1, vehicle.getSize()));
        }

        // If another blocking vehicle is already on the bridge (and i'm not blocking myself (cars, trucks))
        assert vehicle != null;
        if (!vehicle.isBlockingOtherWay()) {
            boolean convoiApproachingOrCrossingOpposite = this.bridgeRules.hasBlockingVehicleAtEntry(oppositeWay)
                    || (this.activeBlockingVehicle != null && this.activeBlockingVehicle.getWay() == oppositeWay);

            if (convoiApproachingOrCrossingOpposite) {
                return true;
            }
        }

        // If i'm myself blocking
        if (vehicle.isBlockingOtherWay()) {
            // if the opposite way is not completely clear, i need to wait
            if (!this.bridgeRules.isWayCompletelyClear(oppositeWay)) {
                return true;
            }

            // if 4 segments are not clear wait
            if (!this.bridgeRules.areFirstSegmentsFree(myWay, vehicle.getSize())) {
                return true;
            }

            return false;
        }

        return !this.bridgeRules.areFirstSegmentsFree(myWay, Math.max(1, vehicle.getSize()));
    }

    public synchronized void addBoat(FloatingVehicle boat) {
        boatAlert = true;
        if (!boatAtBridge.contains(boat)) {
            boatAtBridge.add(boat);
        }
        notifyAll();
        boat.start();
        if (currentState == BridgeState.OPEN && crossingBoat == null) {
            new Thread(() -> {
                try {
                    startCrossingBoat();
                } catch (InterruptedException ignored) {}
            }, "BoatCrossingThread").start();
        }
    }

    public void startCrossingBoat() throws InterruptedException {
        while (true) {
            FloatingVehicle boat;
            synchronized (this) {
                if (currentState != BridgeState.OPEN || boatAtBridge.isEmpty()) {
                    if (boatAtBridge.isEmpty()) {
                        boatAlert = false;
                    }
                    crossingBoat = null;
                    notifyAll();
                    break;
                }
                boat = boatAtBridge.poll();
                crossingBoat = boat;
            }
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

    public synchronized Queue<FloatingVehicle> getBoatAtBridge() {
        return new PriorityQueue<>(boatAtBridge);
    }

    public synchronized List<FloatingVehicle> getBoatsWaiting() {
        return new ArrayList<>(boatAtBridge);
    }

    public synchronized FloatingVehicle getCrossingBoat() {
        return crossingBoat;
    }

    public synchronized boolean isBoatAlert() {
        return boatAlert || !boatAtBridge.isEmpty() || crossingBoat != null;
    }

    public boolean isRoadsGateClosed() {
        return isRoadsGateClosed;
    }

    public synchronized void setRoadsGateClosed(boolean isRoadsGateClosed) {
        this.isRoadsGateClosed = isRoadsGateClosed;
        notifyAll();
    }

    public synchronized void waitUntilRoadsClear() throws InterruptedException {
        while (!this.bridgeRules.isWayCompletelyClear(Way.CAR_LEFT_TO_RIGHT) ||
               !this.bridgeRules.isWayCompletelyClear(Way.CAR_RIGHT_TO_LEFT)) {
            wait();
        }
        boatAlert=false;
    }

    public void setCrossingBoat(FloatingVehicle crossingBoat) {
        this.crossingBoat = crossingBoat;
    }
}
