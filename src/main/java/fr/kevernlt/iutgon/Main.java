package fr.kevernlt.iutgon;

import fr.kevernlt.iutgon.bridged.orchestrator.OrchestatorThread;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        OrchestatorThread orchestratorThread = new OrchestatorThread();
        orchestratorThread.setName("Orchestrator");
        orchestratorThread.populateBridge();
        orchestratorThread.start();
    }
}
