package fr.kev36rno.iutgon;

import fr.kev36rno.iutgon.bridged.orchestrator.OrchestatorThread;

public class Main {
    static void main() {
        OrchestatorThread orchestratorThread = new OrchestatorThread();
        orchestratorThread.setName("Orchestrator");
        orchestratorThread.populateBridge();
        orchestratorThread.start();
    }
}
