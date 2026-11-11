package fr.kevernlt.iutgon.bridged.bridge;

import fr.kevernlt.iutgon.bridged.enums.BridgeState;

/**
 * Thread managing the 60-second automatic cycle of the movable bridge.
 * The bridge stays closed for road traffic, closes the road gates,
 * waits for vehicles to exit, opens for boats until all have passed,
 * lowers the bridge deck, and reopens the road gates.
 */
public class BridgeCycleManager extends Thread {
    private final Bridge bridge;
    private volatile boolean immediateOpenRequested = false;

    /**
     * Creates the cycle manager thread.
     *
     * @param bridge the bridge reference
     */
    public BridgeCycleManager(Bridge bridge) {
        super("BridgeCycleManager");
        this.bridge = bridge;
    }

    /**
     * Interrupts the closed sleep to force an immediate bridge opening.
     */
    public void triggerImmediateOpening() {
        this.immediateOpenRequested = true;
        this.interrupt();
    }

    /**
     * Main cycle execution loop.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Bridge remains closed for road vehicles
                long sleepTime = BridgeState.CLOSED.getDelay();
                long start = System.currentTimeMillis();
                while (System.currentTimeMillis() - start < sleepTime && !immediateOpenRequested) {
                    long remaining = sleepTime - (System.currentTimeMillis() - start);
                    Thread.sleep(Math.min(remaining, 500));
                }
                immediateOpenRequested = false;

                // Close road gates so no new normal vehicles can enter
                bridge.setRoadsGateClosed(true);

                // Wait for all vehicles currently on the bridge to exit
                bridge.waitUntilRoadsClear();

                // Raise the bridge deck
                bridge.setCurrentState(BridgeState.OPENING);
                Thread.sleep(BridgeState.OPENING.getDelay());

                // Bridge is fully open: maritime navigation authorized
                bridge.setCurrentState(BridgeState.OPEN);

                // Wait until all waiting and crossing boats have passed
                bridge.waitUntilWaterwayClear();

                // Lower the bridge deck
                bridge.setCurrentState(BridgeState.CLOSING);
                Thread.sleep(BridgeState.CLOSING.getDelay());

                // Bridge is fully closed: reopen road gates for vehicles
                bridge.setCurrentState(BridgeState.CLOSED);
                bridge.setRoadsGateClosed(false);

            } catch (InterruptedException e) {
                if (immediateOpenRequested) {
                    Thread.interrupted();
                    continue;
                }
                interrupt();
                break;
            }
        }
    }
}
