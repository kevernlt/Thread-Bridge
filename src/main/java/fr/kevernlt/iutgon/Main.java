package fr.kevernlt.iutgon;

import fr.kevernlt.iutgon.bridged.orchestrator.OrchestatorThread;

public class Main {
    static void main() {
        OrchestatorThread orchestratorThread = new OrchestatorThread();
        orchestratorThread.setName("Orchestrator");
        orchestratorThread.populateBridge();
        orchestratorThread.start();
    }
}
