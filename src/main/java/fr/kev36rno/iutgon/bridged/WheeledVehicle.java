package fr.kev36rno.iutgon.bridged;

import fr.kev36rno.iutgon.bridged.enums.Way;

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
            Segment currentSegment = null;

            for (Segment nextSegment : path) {
                bridge.enterSegment(this, nextSegment);

                if (currentSegment != null) {
                    bridge.leaveSegment(this, currentSegment);
                }
                currentSegment = nextSegment;

                Thread.sleep(1000);
            }

            if (currentSegment != null) {
                bridge.leaveSegment(this, currentSegment);
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
