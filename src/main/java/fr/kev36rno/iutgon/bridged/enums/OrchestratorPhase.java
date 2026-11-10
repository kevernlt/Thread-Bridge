package fr.kev36rno.iutgon.bridged.enums;

import fr.kev36rno.iutgon.bridged.orchestrator.OrchestatorThread;

public enum OrchestratorPhase {
    PROCESS_SEGMENTS;

    public OrchestratorPhase getNextPhase() {
        return OrchestratorPhase.values()[(this.ordinal() + 1) % OrchestratorPhase.values().length];
    }
}
