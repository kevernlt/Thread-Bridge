package fr.kev36rno.iutgon.bridged.orchestrator;

import fr.kev36rno.iutgon.bridged.Bridge;
import fr.kev36rno.iutgon.bridged.Segment;
import fr.kev36rno.iutgon.bridged.enums.OrchestratorPhase;
import fr.kev36rno.iutgon.bridged.enums.Way;
import fr.kev36rno.iutgon.bridged.objects.Car;

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
            bridge.printCurrentState();
            try {
                Thread.sleep(100);
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

        Segment segment_gd_1 = new Segment(1, Way.CAR_LEFT_TO_RIGHT);
        Segment segment_gd_2 = new Segment(2, Way.CAR_LEFT_TO_RIGHT);
        Segment segment_gd_3 = new Segment(3, Way.CAR_LEFT_TO_RIGHT);
        Segment segment_gd_4 = new Segment(4, Way.CAR_LEFT_TO_RIGHT);
        Segment segment_gd_5 = new Segment(5, Way.CAR_LEFT_TO_RIGHT);
        bridge.addSegment(segment_gd_1, segment_gd_2, segment_gd_3, segment_gd_4, segment_gd_5);

        Segment segment_dg_1 = new Segment(6, Way.CAR_RIGHT_TO_LEFT);
        Segment segment_dg_2 = new Segment(7, Way.CAR_RIGHT_TO_LEFT);
        Segment segment_dg_3 = new Segment(8, Way.CAR_RIGHT_TO_LEFT);
        Segment segment_dg_4 = new Segment(9, Way.CAR_RIGHT_TO_LEFT);
        Segment segment_dg_5 = new Segment(10, Way.CAR_RIGHT_TO_LEFT);
        bridge.addSegment(segment_dg_1, segment_dg_2, segment_dg_3, segment_dg_4, segment_dg_5);

        Car car1 = new Car(Way.CAR_LEFT_TO_RIGHT, bridge);

        bridge.addVehicle(car1);

    }
}
