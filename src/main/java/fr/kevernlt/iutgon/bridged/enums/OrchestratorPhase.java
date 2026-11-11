package fr.kevernlt.iutgon.bridged.enums;

public enum OrchestratorPhase {
    PROCESS_SEGMENTS;

    public OrchestratorPhase getNextPhase() {
        return OrchestratorPhase.values()[(this.ordinal() + 1) % OrchestratorPhase.values().length];
    }
}
