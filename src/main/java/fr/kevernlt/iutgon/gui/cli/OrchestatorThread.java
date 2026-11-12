package fr.kevernlt.iutgon.gui.cli;

import fr.kevernlt.iutgon.bridged.Segment;
import fr.kevernlt.iutgon.bridged.bridge.Bridge;
import fr.kevernlt.iutgon.bridged.enums.Way;
import fr.kevernlt.iutgon.bridged.objects.Boat;
import fr.kevernlt.iutgon.bridged.objects.Car;
import fr.kevernlt.iutgon.bridged.objects.EmergencyCar;
import fr.kevernlt.iutgon.bridged.objects.ExceptionalLoad;
import fr.kevernlt.iutgon.bridged.objects.Truck;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrator thread used for the terminal/console simulation mode.
 * Periodically prints the bridge status to the console and generates traffic.
 */
public class OrchestatorThread extends Thread {

    private Bridge bridge;

    /**
     * Creates a new orchestrator with a newly instantiated bridge.
     */
    public OrchestatorThread() {
        this.bridge = new Bridge();
    }

    /**
     * Creates a new orchestrator with an existing bridge.
     *
     * @param bridge existing bridge
     */
    public OrchestatorThread(Bridge bridge) {
        this.bridge = bridge;
    }

    /**
     * Returns the bridge monitored by this orchestrator.
     *
     * @return bridge
     */
    public Bridge getBridge() {
        return bridge;
    }

    /**
     * Main loop printing the bridge status once per second.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            bridge.getBridgePrinter().printCurrentState();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                interrupt();
                break;
            }
        }
    }

    /**
     * Populates the bridge with initial segments, vehicles, and background vehicle generators.
     *
     * @throws InterruptedException if interrupted while starting
     */
    public void populateBridge() throws InterruptedException {
        bridge.addSegment(generateSegments(10).toArray(new Segment[0]));

        ExceptionalLoad exceptionalLoad1 = new ExceptionalLoad(Way.CAR_RIGHT_TO_LEFT, bridge);
        bridge.addVehicle(exceptionalLoad1);

        Car car2 = new Car(Way.CAR_RIGHT_TO_LEFT, bridge);
        bridge.addVehicle(car2);

        Truck truck1 = new Truck(Way.CAR_LEFT_TO_RIGHT, bridge);
        bridge.addVehicle(truck1);

        Car car1 = new Car(Way.CAR_LEFT_TO_RIGHT, bridge);
        bridge.addVehicle(car1);

        Boat boat1 = new Boat(Way.BOAT_NORTH, bridge);
        bridge.addBoat(boat1);

        Boat boat2 = new Boat(Way.BOAT_SOUTH, bridge);
        bridge.addBoat(boat2);
    }

    /**
     * Generates 'n' segments for both directions.
     *
     * @param n number of segments per direction
     * @return list of segments
     */
    static List<Segment> generateSegments(int n) {
        List<Segment> segments = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            segments.add(new Segment(i, Way.CAR_LEFT_TO_RIGHT));
        }

        for (int i = n; i >= 1; i--) {
            segments.add(new Segment(i, Way.CAR_RIGHT_TO_LEFT));
        }

        return segments;
    }
}
