package fr.kev36rno.iutgon.bridged;

import fr.kev36rno.iutgon.bridged.bridge.Bridge;
import fr.kev36rno.iutgon.bridged.enums.Way;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class WheeledVehicle extends Vehicle implements Comparable<WheeledVehicle> {
    private Bridge bridge;

    private Deque<Segment> occupied = new ArrayDeque<>();

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
            // call synchronized method to avoid entering the bridge
            // if one the defined rules is not ok
            bridge.waitForEntry(this);

            List<Segment> path = bridge.getSegmentsForWay(getWay());


            // we let the vehicle enter the first segment and going through the rest of the path
            for (Segment nextSegment : path) {
                bridge.enterSegment(this, nextSegment);
                occupied.addLast(nextSegment);

                // fill the segments behind depending of size
                if(occupied.size() > this.getSize()) {
                    Segment segmentToLeave = occupied.removeFirst();
                    bridge.leaveSegment(this, segmentToLeave);
                }

                Thread.sleep(1000);
            }

            // at this state, the vehicle is in the last segment of the path
            // so we have [size] segements behind
            while (!occupied.isEmpty()) {
                Segment segmentToLeave = occupied.removeFirst();
                bridge.leaveSegment(this, segmentToLeave);

                if(!occupied.isEmpty()) {
                    // not that "loop-busy" as idea can say but if this sleep is not here
                    // the vehicle will leave the bridge in a single tick
                    Thread.sleep(1000);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized boolean hasClearedBridge() {
        return occupied.isEmpty();
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
