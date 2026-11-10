package fr.kev36rno.iutgon.bridged;

import fr.kev36rno.iutgon.bridged.bridge.Bridge;
import fr.kev36rno.iutgon.bridged.enums.Way;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class WheeledVehicle extends Vehicle implements Comparable<WheeledVehicle> {
    private Bridge bridge;

    // size in platforms: in consignee it say "un camion fait 2 platforme" and "une voiture fait une platforme"
    public WheeledVehicle(Way way, Bridge bridge) {
        super(way);
        this.bridge = bridge;
    }

    public int getSize() {
        return 0;
    }

    public boolean isBlockingOtherWay() {
        return false;
    }

    public boolean isEmergencyVehicle() {
        return false;
    }

    @Override
    public void run() {
        try {
            bridge.waitForEntry(this);

            List<Segment> path = bridge.getSegmentsForWay(getWay());
            Deque<Segment> occupied = new ArrayDeque<>();

            for (Segment nextSegment : path) {
                bridge.enterSegment(this, nextSegment);
                occupied.addLast(nextSegment);

                if(occupied.size() > this.getSize()) {
                    Segment segmentToLeave = occupied.removeFirst();
                    bridge.leaveSegment(this, segmentToLeave);
                }

                Thread.sleep(1000);
            }

            while (!occupied.isEmpty()) {
                Segment segmentToLeave = occupied.removeFirst();
                bridge.leaveSegment(this, segmentToLeave);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String toString() {
        return "WheeledVehicle{" +
                "way=" + getWay() +
                ", size=" + getSize() +
                ", isBlockingOtherWay=" + isBlockingOtherWay() +
                ", isEmergencyVehicle=" + isEmergencyVehicle() +
                '}';
    }

    @Override
    public int compareTo(WheeledVehicle o) {
        return isEmergencyVehicle() ? -1 : o.isEmergencyVehicle() ? 1 : 0;
    }
}
