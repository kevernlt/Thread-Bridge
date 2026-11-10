package fr.kev36rno.iutgon.bridged.bridge;

import fr.kev36rno.iutgon.bridged.Segment;
import fr.kev36rno.iutgon.bridged.Vehicle;
import fr.kev36rno.iutgon.bridged.WheeledVehicle;
import fr.kev36rno.iutgon.bridged.enums.Way;

import java.util.List;
import java.util.PriorityQueue;

public class BridgeRules {

    private final Bridge bridge;

    public BridgeRules(Bridge bridge) {
        this.bridge = bridge;
    }


    public synchronized boolean isWayCompletelyClear(Way way) {
        return bridge.getSegmentsForWay(way).stream()
                .allMatch(segment -> segment.getVehicleOccupyingSegment() == null);
    }


    public synchronized boolean areFirstSegmentsFree(Way way, int count) {
        List<Segment> segments = bridge.getSegmentsForWay(way);
        int limit = Math.min(count, segments.size());
        for (int i = 0; i < limit; i++) {
            if (segments.get(i).getVehicleOccupyingSegment() != null) {
                return false;
            }
        }
        return true;
    }

    public synchronized boolean hasBlockingVehicleAtEntry(Way oppositeWay) {
        PriorityQueue<Vehicle> queue = bridge.getVehicleAtBridgeEntry().get(oppositeWay);
        if (queue == null || queue.isEmpty()) return false;
        Vehicle head = queue.peek();
        return head instanceof WheeledVehicle wheeled && wheeled.isBlockingOtherWay();
    }

}
