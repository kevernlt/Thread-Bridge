package fr.kevernlt.iutgon.bridged.bridge;

import fr.kevernlt.iutgon.bridged.Segment;
import fr.kevernlt.iutgon.bridged.Vehicle;
import fr.kevernlt.iutgon.bridged.WheeledVehicle;
import fr.kevernlt.iutgon.bridged.enums.Way;

import java.util.List;
import java.util.PriorityQueue;

/**
 * Helper class that checks bridge traffic rules:
 * - Checks if an entire direction is clear.
 * - Checks if the first segments are free for entry.
 * - Checks if a blocking convoy is waiting at the opposite entry.
 */
public class BridgeRules {

    private final Bridge bridge;

    /**
     * Creates the bridge rules validator.
     *
     * @param bridge the bridge
     */
    public BridgeRules(Bridge bridge) {
        this.bridge = bridge;
    }

    /**
     * Checks if all segments of a way are completely empty (no vehicle on main or emergency lane).
     *
     * @param way the direction to check
     * @return true if completely clear
     */
    public synchronized boolean isWayCompletelyClear(Way way) {
        return bridge.getSegmentsForWay(way).stream()
                .allMatch(segment -> segment.getVehicleOccupyingSegment() == null && !segment.hasEmergencyLaneVehicle());
    }

    /**
     * Checks if the first 'count' segments of a way are currently unoccupied.
     *
     * @param way   direction of travel
     * @param count number of segments needed
     * @return true if all initial segments are free
     */
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

    /**
     * Checks if the head of the opposite entry queue is an exceptional convoy.
     *
     * @param oppositeWay the opposite direction
     * @return true if a convoy is waiting at the entrance
     */
    public synchronized boolean hasBlockingVehicleAtEntry(Way oppositeWay) {
        PriorityQueue<Vehicle> queue = bridge.getVehicleAtBridgeEntry().get(oppositeWay);
        if (queue == null || queue.isEmpty()) return false;
        Vehicle head = queue.peek();
        return head instanceof WheeledVehicle wheeled && wheeled.isBlockingOtherWay();
    }
}
