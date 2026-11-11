package fr.kevernlt.iutgon.bridged.bridge;

/**
 * Background thread dedicated to passing waiting boats across the bridge waterway
 * one by one whenever the bridge state is OPEN.
 */
public class BridgeBoatPassManager extends Thread {
    private final Bridge bridge;

    /**
     * Creates the boat pass manager thread.
     *
     * @param bridge the bridge reference
     */
    public BridgeBoatPassManager(Bridge bridge) {
        super("BridgeBoatPassManager");
        this.bridge = bridge;
    }

    /**
     * Continuously calls passNextBoat() while the thread is alive.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                bridge.passNextBoat();
            } catch (InterruptedException e) {
                interrupt();
                break;
            }
        }
    }
}
