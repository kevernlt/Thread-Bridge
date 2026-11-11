package fr.kev36rno.iutgon.bridged;

import fr.kev36rno.iutgon.bridged.bridge.Bridge;
import fr.kev36rno.iutgon.bridged.enums.Way;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class WheeledVehicle extends Vehicle implements Comparable<WheeledVehicle> {
    private Bridge bridge;

    private final Deque<Segment> occupied = new ArrayDeque<>();

    // size in platforms: in consignee it say "un camion fait 2 platforme" and "une voiture fait une platforme"
    public WheeledVehicle(Way way, Bridge bridge) {
        super(way);
        this.bridge = bridge;
    }

    public int getSize() {
        return 1;
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

            // taking [size] segments from the path
            List<Segment> currentBlock = blocks.get(0);
            bridge.enterBridge(this, currentBlock);
            synchronized (this) {
                occupied.clear();
                occupied.addAll(currentBlock);
            }
            Thread.sleep(1000);

            // progress [size] segments per [size] segments
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

            // bridge is now closed
            bridge.leaveBridge(this, currentBlock);
            synchronized (this) { // avoid race condition
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
