package fr.kev36rno.iutgon.bridged.bridge;

import fr.kev36rno.iutgon.bridged.enums.BridgeState;

public class BridgeCycleManager extends Thread {
    private final Bridge bridge;

    public BridgeCycleManager(Bridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(60000);

                BridgeState nextState = bridge.getCurrentState().getNextState();

                if (bridge.getCurrentState() == BridgeState.CLOSED && nextState == BridgeState.OPENING) {
                    bridge.setRoadsGateClosed(true);
                    System.out.println("BridgeCycleManager: Roads gate closed, waiting for bridge to be clear...");

                    bridge.waitUntilRoadsClear();
                    System.out.println("BridgeCycleManager: Bridge is clear, opening...");
                }

                bridge.setCurrentState(nextState);

                if (nextState == BridgeState.OPEN) {
                    new Thread(() -> {
                        try {
                            bridge.startCrossingBoat();
                        } catch (InterruptedException ignored) {}
                    }, "BridgeCycleBoatCrossing").start();
                }

                if (nextState == BridgeState.CLOSED) {
                    bridge.setRoadsGateClosed(false);
                    System.out.println("BridgeCycleManager: Bridge closed, roads gate opened.");
                }

            } catch (InterruptedException e) {
                interrupt();
                break;
            }
        }
    }
}
