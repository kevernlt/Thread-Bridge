package fr.kevernlt.iutgon;

import fr.kevernlt.iutgon.gui.cli.OrchestatorThread;

/**
 * Console entry point for testing the bridge orchestrator without JavaFX GUI.
 */
public class CliLauncher {
    /**
     * Runs the console orchestrator demo.
     *
     * @param args command line arguments
     * @throws InterruptedException if interrupted while running
     */
    public static void main(String[] args) throws InterruptedException {
        OrchestatorThread orchestratorThread = new OrchestatorThread();
        orchestratorThread.setName("Orchestrator");
        orchestratorThread.populateBridge();
        orchestratorThread.start();
    }
}
