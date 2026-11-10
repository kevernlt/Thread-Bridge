package fr.kev36rno.iutgon.bridged.bridge;

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

    private final HashMap<Way, PriorityQueue<Vehicle>> vehicleAtBridgeEntry = new HashMap<>();

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

    public void addVehicle(Vehicle vehicle) {
        vehicleAtBridgeEntry.computeIfAbsent(vehicle.getWay(), k -> new PriorityQueue<>()).add(vehicle);
        vehicle.start();
    }

    public BridgePrinter getBridgePrinter() {
        return bridgePrinter;
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
            if(vehicle.equals(activeBlockingVehicle) && this.bridgeRules.isWayCompletelyClear(vehicle.getWay())) {
                activeBlockingVehicle = null;
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

        if(vehicle.isBlockingOtherWay()) {
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
            if (this.bridgeRules.hasExceptionalLoadWaitingAtEntry(oppositeWay) || this.activeBlockingVehicle != null) {
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

        Segment firstSegment = getSegmentsForWay(myWay).getFirst();
        return firstSegment.getVehicleOccupyingSegment() != null;
    }
}
