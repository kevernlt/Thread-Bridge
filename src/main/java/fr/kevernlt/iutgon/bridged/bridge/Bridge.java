package fr.kevernlt.iutgon.bridged.bridge;

import fr.kevernlt.iutgon.bridged.FloatingVehicle;
import fr.kevernlt.iutgon.bridged.Segment;
import fr.kevernlt.iutgon.bridged.Vehicle;
import fr.kevernlt.iutgon.bridged.WheeledVehicle;
import fr.kevernlt.iutgon.bridged.enums.BridgeState;
import fr.kevernlt.iutgon.bridged.enums.Way;
import fr.kevernlt.iutgon.bridged.exceptions.IllegalVehicleWay;

import java.util.*;

public class Bridge {
    private BridgeState currentState;

    private final List<Segment> segments;

    private final BridgeRules bridgeRules = new BridgeRules(this);
    private final BridgePrinter bridgePrinter = new BridgePrinter(this);
    private WheeledVehicle activeBlockingVehicle = null;

    private final HashMap<Way, PriorityQueue<Vehicle>> vehicleAtBridgeEntry = new HashMap<>();
    private final Queue<FloatingVehicle> boatAtBridge = new PriorityQueue<>();

    private FloatingVehicle crossingBoat = null;
    private boolean boatAlert = false;

    public Bridge() {
        this.segments = new ArrayList<>();
        this.currentState = BridgeState.CLOSED;
    }

    public void setCurrentState(BridgeState currentState) {
        this.currentState = currentState;
    }

    public BridgeState getCurrentState() {
        return currentState;
    }

    public void addSegment(Segment... segments) {
        this.segments.addAll(Arrays.asList(segments));
    }

    public void addVehicle(WheeledVehicle vehicle) {
        vehicleAtBridgeEntry.computeIfAbsent(vehicle.getWay(), k -> new PriorityQueue<>()).add(vehicle);
        vehicle.start();
    }

    public BridgePrinter getBridgePrinter() {
        return bridgePrinter;
    }

    public synchronized void enterBridge(WheeledVehicle vehicle, List<Segment> initialSegments) throws InterruptedException {
        if (!vehicle.getWay().isLogic(vehicle)) {
            throw new IllegalVehicleWay("Vehicle type does not match the way.");
        }

        PriorityQueue<Vehicle> queue = vehicleAtBridgeEntry.get(vehicle.getWay());
        Way myWay = vehicle.getWay();
        Way oppositeWay = myWay.getOppositeWay();

        while (shouldWaitToEnter(vehicle, queue, myWay, oppositeWay)) {
            wait();
        }

        queue.poll();

        if (vehicle.isBlockingOtherWay()) {
            activeBlockingVehicle = vehicle;
        }

        for (Segment segment : initialSegments) {
            segment.setVehicleOccupyingSegment(vehicle);
        }

        notifyAll();
    }

    public synchronized void advanceToSegments(WheeledVehicle vehicle, List<Segment> currentBlock, List<Segment> nextBlock) throws InterruptedException {
        while (!areSegmentsFree(nextBlock, vehicle) || currentState != BridgeState.CLOSED) {
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

    public synchronized void enterSegment(WheeledVehicle vehicle, Segment nextSegment) throws InterruptedException {
        while (nextSegment.getVehicleOccupyingSegment() != null || currentState != BridgeState.CLOSED) {
            wait();
        }
        nextSegment.setVehicleOccupyingSegment(vehicle);
    }

    public synchronized void leaveSegment(WheeledVehicle vehicle, Segment segment) {
        if (segment != null && segment.getVehicleOccupyingSegment() == vehicle) {
            segment.setVehicleOccupyingSegment(null);

            // If i'm the blocking another vehicle, i can leave the bridge
            if (vehicle.equals(activeBlockingVehicle)) {
                boolean convoiStillOnBridge = getSegmentsForWay(vehicle.getWay()).stream()
                        .anyMatch(s -> s.getVehicleOccupyingSegment() == vehicle);

                if (!convoiStillOnBridge) {
                    activeBlockingVehicle = null;
                }
            }
            notifyAll();
        }
    }

    public List<Segment> getSegmentsForWay(Way way) {
        return segments.stream().filter(segment -> segment.getWay() == way).toList();
    }

    public HashMap<Way, PriorityQueue<Vehicle>> getVehicleAtBridgeEntry() {
        return vehicleAtBridgeEntry;
    }

    public synchronized Vehicle getNextVehicleWaitingAtEntry(Way way) {
        PriorityQueue<Vehicle> oppositeQueue = vehicleAtBridgeEntry.get(way.getOppositeWay());
        return (oppositeQueue != null) ? oppositeQueue.peek() : null;
    }

    public synchronized void waitForEntry(WheeledVehicle vehicle) throws InterruptedException {
        if (!vehicle.getWay().isLogic(vehicle)) {
            throw new IllegalVehicleWay("Vehicle type does not match the way.");
        }

        PriorityQueue<Vehicle> queue = vehicleAtBridgeEntry.get(vehicle.getWay());
        Way myWay = vehicle.getWay();
        Way oppositeWay = myWay.getOppositeWay();

        while (shouldWaitToEnter(vehicle, queue, myWay, oppositeWay)) {
            wait();
        }

        queue.poll();

        if (vehicle.isBlockingOtherWay()) {
            activeBlockingVehicle = vehicle;
        }
    }

    private boolean shouldWaitToEnter(WheeledVehicle vehicle, PriorityQueue<Vehicle> queue, Way myWay, Way oppositeWay) {
        if (queue == null || queue.peek() != vehicle) {
            return true;
        }

        // Bridge is not closed, vehicle will fall in water
        if (currentState != BridgeState.CLOSED) {
            return true;
        }

        // If another blocking vehicle is already on the bridge (and i'm not blocking myself (cars, trucks))
        assert vehicle != null;
        if (!vehicle.isBlockingOtherWay()) {
            boolean convoiApproachingOrCrossingOpposite = this.bridgeRules.hasBlockingVehicleAtEntry(oppositeWay) || (this.activeBlockingVehicle != null && this.activeBlockingVehicle.getWay() == oppositeWay);

            if (convoiApproachingOrCrossingOpposite) {
                return true;
            }
        }

        //  If i'm myselfblocking
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

        // Un véhicule qui occupe deux (ou plus) segments consécutifs en raison de sa longueur
        // doit pouvoir réserver tous ses segments initiaux simultanément avant d'avancer.
        return !this.bridgeRules.areFirstSegmentsFree(myWay, Math.max(1, vehicle.getSize()));
    }

    public void addBoat(FloatingVehicle boat){
        if(boatAtBridge.isEmpty())boatAlert=true;
        if(!boatAtBridge.contains(boat))boatAtBridge.add(boat);
        boat.start();
    }

    public void startCrossingBoat() throws InterruptedException {
        while(!boatAtBridge.isEmpty()){
            if(crossingBoat==null){
                crossingBoat= boatAtBridge.poll();
                crossingBoat.cross();
            }
        }
        boatAlert=false;
    }

    public void setCrossingBoat(FloatingVehicle crossingBoat) {
        this.crossingBoat = crossingBoat;
    }
}
