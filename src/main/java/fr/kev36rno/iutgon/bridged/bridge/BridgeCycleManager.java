package fr.kev36rno.iutgon.bridged.bridge;

import fr.kev36rno.iutgon.bridged.enums.Way;

public class BridgeCycleManager extends Thread {
    private final Bridge bridge;

    public BridgeCycleManager(Bridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public void run() {
        while (this.isAlive()) {
            try {
                Thread.sleep(5000);
                // consignee says "toutes les minutes"
                while (!bridge.getBridgeRules().isWayCompletelyClear(Way.CAR_LEFT_TO_RIGHT) || !bridge.getBridgeRules().isWayCompletelyClear(Way.CAR_RIGHT_TO_LEFT)) {
                    wait();
                }
                bridge.setCurrentState(bridge.getCurrentState().getNextState());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
