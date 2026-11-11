package fr.kevernlt.iutgon.bridged.orchestrator;

import fr.kevernlt.iutgon.bridged.bridge.Bridge;
import fr.kevernlt.iutgon.bridged.Segment;
import fr.kevernlt.iutgon.bridged.enums.OrchestratorPhase;
import fr.kevernlt.iutgon.bridged.enums.Way;
import fr.kevernlt.iutgon.bridged.objects.Car;
import fr.kevernlt.iutgon.bridged.objects.ExceptionalLoad;
import fr.kevernlt.iutgon.bridged.objects.Truck;

import java.util.ArrayList;
import java.util.List;

public class OrchestatorThread extends Thread {

    private Bridge bridge = new Bridge();
    private OrchestratorPhase currentState = OrchestratorPhase.PROCESS_SEGMENTS;

    @Override
    public void run() {

        while (true) {

            switch (currentState) {
                case PROCESS_SEGMENTS:
//                    bridge.processSegments();
                    break;
            }

            currentState = currentState.getNextPhase();
            bridge.getBridgePrinter().printCurrentState();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void populateBridge() {
        /**
         * Lets imagine a 5 segments bridge
         * From top view :
         *
         * -----+                               +-----*
         *      |                               |     *
         *      |    SENS G->D ----->           |     *
         *      |                               |     *
         *      +-------------------------------+     *
         *          P1    P2    P3    P4    P5        *
         *      +-------------------------------+     *
         *          P5    P4    P3    P2    P1        *
         *      +-------------------------------+     *
         *      |                               |     *
         *      |   <---- SENS D->G             |     *
         * -----+                               +-----*
         */

        bridge.addSegment(generateSegments(10).toArray(new Segment[0]));

        /**
         * Pour la démo
         *
         * En premier : un cametar suivi d'une voiture
         * puis en face : une voiture toute seul
         */

        ExceptionalLoad exceptionalLoad1 = new ExceptionalLoad(Way.CAR_RIGHT_TO_LEFT, bridge);
        bridge.addVehicle(exceptionalLoad1);

        Car car2 = new Car(Way.CAR_RIGHT_TO_LEFT, bridge);
        bridge.addVehicle(car2);

        Truck truck1 = new Truck(Way.CAR_LEFT_TO_RIGHT, bridge);
        bridge.addVehicle(truck1);

        Car car1 = new Car(Way.CAR_LEFT_TO_RIGHT, bridge);
        bridge.addVehicle(car1);
    }

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
