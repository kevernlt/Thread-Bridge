package fr.kevernlt.iutgon.bridged.bridge;

import fr.kevernlt.iutgon.bridged.FloatingVehicle;
import fr.kevernlt.iutgon.bridged.Segment;
import fr.kevernlt.iutgon.bridged.Vehicle;
import fr.kevernlt.iutgon.bridged.WheeledVehicle;
import fr.kevernlt.iutgon.bridged.enums.BridgeState;
import fr.kevernlt.iutgon.bridged.enums.Way;
import fr.kevernlt.iutgon.bridged.objects.EmergencyCar;
import fr.kevernlt.iutgon.bridged.objects.ExceptionalLoad;
import fr.kevernlt.iutgon.bridged.objects.Truck;

import java.util.*;

/**
 * Utility class to print an ASCII 2D top-down visualization of the bridge,
 * river, waiting queues, and current simulation status in the console.
 */
public class BridgePrinter {

    private final Bridge bridge;

    /**
     * Creates the bridge printer.
     *
     * @param bridge the bridge reference
     */
    public BridgePrinter(Bridge bridge) {
        this.bridge = bridge;
    }

    /**
     * Clears the console and prints the full top-down visual map and status dashboard.
     */
    public void printCurrentState() {
        clearOutput();

        BridgeState state = bridge.getCurrentState();
        boolean gatesClosed = bridge.isRoadsGateClosed();
        long elapsedMs = bridge.getStateElapsedTime();
        long elapsedSec = elapsedMs / 1000;

        boolean urgL2R = bridge.isEmergencyVehicleActive(Way.CAR_LEFT_TO_RIGHT);
        boolean urgR2L = bridge.isEmergencyVehicleActive(Way.CAR_RIGHT_TO_LEFT);
        boolean emergencyActive = urgL2R || urgR2L;

        FloatingVehicle crossingBoat = bridge.getCrossingBoat();
        List<FloatingVehicle> waitingBoats = bridge.getBoatsWaiting();
        List<FloatingVehicle> headingNorthBoats = waitingBoats.stream().filter(b -> b.getWay() == Way.BOAT_NORTH).toList();
        List<FloatingVehicle> headingSouthBoats = waitingBoats.stream().filter(b -> b.getWay() == Way.BOAT_SOUTH).toList();

        List<Segment> l2rSegments = bridge.getSegmentsForWay(Way.CAR_LEFT_TO_RIGHT);
        List<Segment> r2lSegments = bridge.getSegmentsForWay(Way.CAR_RIGHT_TO_LEFT);
        int totalSegments = Math.max(l2rSegments.size(), r2lSegments.size());

        boolean isL2RLocked = l2rSegments.stream().anyMatch(s -> s.getVehicleOccupyingSegment() != null && s.getVehicleOccupyingSegment().isBlockingOtherWay());
        boolean isR2LLocked = r2lSegments.stream().anyMatch(s -> s.getVehicleOccupyingSegment() != null && s.getVehicleOccupyingSegment().isBlockingOtherWay());

        // Dashboard header
        System.out.println("+==================================================================================================+");
        System.out.println("|                      SUPERVISION DU PONT LEVANT - VUE GLOBALE DE HAUT                            |");
        System.out.println("+==================================================================================================+");

        String stateStr = switch (state) {
            case CLOSED -> "\u001B[92mFERME (Circulation routiere active)\u001B[0m";
            case OPENING -> "\u001B[93mEN OUVERTURE (Evacuation des voitures...)\u001B[0m";
            case OPEN -> "\u001B[96mOUVERT (Navigation maritime autorisee)\u001B[0m";
            case CLOSING -> "\u001B[93mEN FERMETURE (Abaissement du tablier)\u001B[0m";
        };
        String gatesStr = gatesClosed
                ? "\u001B[91;1mFERMEES (Acces interdit - preparation levee)\u001B[0m"
                : "\u001B[92mOUVERTES (Voitures autorisees a entrer)\u001B[0m";

        System.out.println("  Etat du pont       : " + stateStr);
        System.out.println("  Barrieres voitures : " + gatesStr);

        String timerStr;
        if (state == BridgeState.CLOSED) {
            long remaining = Math.max(0, (BridgeState.CLOSED.getDelay() - elapsedMs) / 1000);
            timerStr = String.format("Prochaine ouverture dans %2ds (Cycle d'une minute)", remaining);
        } else if (state == BridgeState.OPEN) {
            timerStr = String.format("Ouvert depuis %2ds (Se referme des qu'il n'y a plus de bateaux)", elapsedSec);
        } else if (state == BridgeState.OPENING) {
            timerStr = "Manœuvre mecanique de levee en cours...";
        } else {
            timerStr = "Manœuvre mecanique d'abaissement en cours...";
        }
        System.out.println("  Minuteur / Cycle   : " + timerStr);

        String boatStatusStr;
        if (crossingBoat != null) {
            String dir = (crossingBoat.getWay() == Way.BOAT_NORTH ? "NORD ^" : "SUD v");
            boatStatusStr = "\u001B[96;1mTRAVERSEE EN COURS : " + crossingBoat + " [Cap " + dir + "]\u001B[0m";
        } else if (state == BridgeState.OPEN) {
            boatStatusStr = "\u001B[92mCHENAL LIBRE (Bateau autorise a s'engager)\u001B[0m";
        } else {
            boatStatusStr = "\u001B[90mFERME A LA NAVIGATION (Tablier abaisse)\u001B[0m";
        }
        System.out.println("  Chenal maritime    : " + boatStatusStr);

        String alertStr;
        if (emergencyActive) {
            if (gatesClosed) {
                alertStr = "\u001B[91;1m[ALERTE] VEHICULE DE SECOURS SUR LE PONT (Barrieres fermees : les vehicules finissent leur course)\u001B[0m";
            } else {
                alertStr = "\u001B[91;1m[ALERTE] VEHICULE PRIORITAIRE ACTIF (SAMU/Urgence) - Bande d'arret d'urgence activee\u001B[0m";
            }
        } else if (isL2RLocked || isR2LLocked) {
            alertStr = "\u001B[95m[ATTENTION] CONVOI EXCEPTIONNEL SUR LE PONT (Voie opposee neutralisee)\u001B[0m";
        } else {
            alertStr = "\u001B[90m(Aucune alerte active)\u001B[0m";
        }
        System.out.println("  Alertes actives    : " + alertStr);
        System.out.println("+==================================================================================================+");
        System.out.println();

        // Top-down map view of bridge and river
        String waterColor = "\u001B[36m";
        String waves = waterColor + " ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ " + resetColor();

        System.out.println("                                         [ FLEUVE NORD ]                                         ");
        System.out.println("                               (Sens de navigation vers le SUD v)                                ");
        System.out.println(waves);

        // North waiting boats (going South)
        String northBoatsDisplay = headingSouthBoats.isEmpty()
                ? "\u001B[90m(Aucun bateau en attente au Nord)\u001B[0m"
                : "\u001B[96m[FLEUVE] File Nord (vers Sud) : " + headingSouthBoats + "\u001B[0m";
        System.out.println("            " + northBoatsDisplay);
        System.out.println(waves);

        // Banks and gates
        String westGate = gatesClosed ? "\u001B[91m[BARRIERE: FERMEE]\u001B[0m" : "\u001B[92m[BARRIERE: OUVERTE]\u001B[0m";
        String eastGate = gatesClosed ? "\u001B[91m[BARRIERE: FERMEE]\u001B[0m" : "\u001B[92m[BARRIERE: OUVERTE]\u001B[0m";

        System.out.println("  RIVE OUEST (GAUCHE)                                                            RIVE EST (DROITE)  ");
        System.out.println("  " + westGate + "                                                              " + eastGate);
        System.out.println("  ================+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+");

        // West to East Lane
        StringBuilder bauL2RLine = new StringBuilder("  BAU (G->D)    : ");
        for (int c = 1; c <= totalSegments; c++) {
            Segment seg = findSegment(l2rSegments, c);
            bauL2RLine.append("|").append(formatBauVehicle(seg != null ? seg.getEmergencyLaneVehicle() : null));
        }
        bauL2RLine.append("|");
        System.out.println(bauL2RLine);

        StringBuilder mainL2RLine = new StringBuilder("  Voie G->D ->  : ");
        for (int c = 1; c <= totalSegments; c++) {
            Segment seg = findSegment(l2rSegments, c);
            mainL2RLine.append("|").append(formatVehicle(seg != null ? seg.getVehicleOccupyingSegment() : null));
        }
        mainL2RLine.append("|");
        System.out.println(mainL2RLine);

        // Segments numbering header
        StringBuilder segHeader = new StringBuilder("  Segments      : ");
        for (int c = 1; c <= totalSegments; c++) {
            segHeader.append(String.format("|  S%02d  ", c));
        }
        segHeader.append("|");
        System.out.println(segHeader);

        // Central bridge deck separator
        if (state == BridgeState.CLOSED) {
            System.out.println("  ----------------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+");
            System.out.println("  TABLIER ROUTIER : ======= [ PONT ROUTIER FERME - CIRCULATION ROUTIERE ACTIVE ] =================|");
            System.out.println("  ----------------+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+");
        } else if (state == BridgeState.OPENING) {
            System.out.println("  \u001B[93m<<<<<<<<<<<<<<<<| ^^^ LEVEE DU PONT EN COURS - ARRET DU TRAFIC ROUTIER ^^^ |>>>>>>>>>>>>>>>>\u001B[0m");
        } else if (state == BridgeState.CLOSING) {
            System.out.println("  \u001B[93m>>>>>>>>>>>>>>>>| vvv ABAISSEMENT DU PONT EN COURS - FIN DES TRAVERSEES vvv |<<<<<<<<<<<<<<<<\u001B[0m");
        } else {
            // OPEN
            System.out.println("  " + waterColor + "~ ~ ~ ~ ~ ~ ~ ~ \\ \\ \\               [ CHENAL MARITIME OUVERT ]               / / / ~ ~ ~ ~ ~ ~ ~" + resetColor());
            if (crossingBoat != null) {
                String dirArrow = (crossingBoat.getWay() == Way.BOAT_NORTH) ? "NORD ^" : "SUD v";
                String boatName = "\u001B[96;1m[BATEAU] " + crossingBoat + " (Cap " + dirArrow + ")\u001B[0m";
                System.out.println("  " + waterColor + "~~~~ CHENAL ~~~~|" + resetColor() + "           BATEAU EN TRAVERSEE : " + boatName);
            } else {
                System.out.println("  " + waterColor + "~~~~ CHENAL ~~~~|" + resetColor() + "           [ EN ATTENTE DE BATEAUX / REFERMETURE DES QU'IL N'Y A PLUS PERSONNE ]");
            }
            System.out.println("  " + waterColor + "~ ~ ~ ~ ~ ~ ~ ~ / / /                                                       \\ \\ \\ ~ ~ ~ ~ ~ ~ ~" + resetColor());
        }

        // East to West Lane
        StringBuilder mainR2LLine = new StringBuilder("  Voie D<-G <-  : ");
        for (int c = 1; c <= totalSegments; c++) {
            Segment seg = findSegment(r2lSegments, c);
            mainR2LLine.append("|").append(formatVehicle(seg != null ? seg.getVehicleOccupyingSegment() : null));
        }
        mainR2LLine.append("|");
        System.out.println(mainR2LLine);

        StringBuilder bauR2LLine = new StringBuilder("  BAU (D<-G)    : ");
        for (int c = 1; c <= totalSegments; c++) {
            Segment seg = findSegment(r2lSegments, c);
            bauR2LLine.append("|").append(formatBauVehicle(seg != null ? seg.getEmergencyLaneVehicle() : null));
        }
        bauR2LLine.append("|");
        System.out.println(bauR2LLine);

        System.out.println("  ================+-------+-------+-------+-------+-------+-------+-------+-------+-------+-------+");
        System.out.println("  " + westGate + "                                                              " + eastGate);
        System.out.println("  RIVE OUEST (GAUCHE)                                                            RIVE EST (DROITE)  ");

        // South waiting boats (going North)
        System.out.println(waves);
        String southBoatsDisplay = headingNorthBoats.isEmpty()
                ? "\u001B[90m(Aucun bateau en attente au Sud)\u001B[0m"
                : "\u001B[96m[FLEUVE] File Sud (vers Nord) : " + headingNorthBoats + "\u001B[0m";
        System.out.println("            " + southBoatsDisplay);
        System.out.println(waves);
        System.out.println("                               (Sens de navigation vers le NORD ^)                                ");
        System.out.println("                                         [ FLEUVE SUD ]                                         ");
        System.out.println();

        // Queue details
        System.out.println("--- FILES D'ATTENTE & DETAILS DE CIRCULATION ----------------------------------------------------------");
        PriorityQueue<Vehicle> qL2R = bridge.getVehicleAtBridgeEntry().get(Way.CAR_LEFT_TO_RIGHT);
        PriorityQueue<Vehicle> qR2L = bridge.getVehicleAtBridgeEntry().get(Way.CAR_RIGHT_TO_LEFT);
        System.out.println("  Voitures en attente Rive Ouest (G->D) : " + (qL2R == null || qL2R.isEmpty() ? "\u001B[90m(vide)\u001B[0m" : qL2R));
        System.out.println("  Voitures en attente Rive Est   (D->G) : " + (qR2L == null || qR2L.isEmpty() ? "\u001B[90m(vide)\u001B[0m" : qR2L));
        System.out.println("  Bateaux en attente au Fleuve          : " + (waitingBoats.isEmpty() ? "\u001B[90m(aucun)\u001B[0m" : waitingBoats));

        List<WheeledVehicle> bauL2R = bridge.getVehiclesOnEmergencyLane(Way.CAR_LEFT_TO_RIGHT);
        List<WheeledVehicle> bauR2L = bridge.getVehiclesOnEmergencyLane(Way.CAR_RIGHT_TO_LEFT);
        if (!bauL2R.isEmpty() || !bauR2L.isEmpty()) {
            System.out.println("  \u001B[33mVehicules gares sur BAU (Priorite SAMU) :\u001B[0m G->D: " + bauL2R + " | D->G: " + bauR2L);
        }
        System.out.println("-------------------------------------------------------------------------------------------------------");
    }

    private Segment findSegment(List<Segment> list, int id) {
        for (Segment s : list) {
            if (s.getSegmentId() == id) {
                return s;
            }
        }
        return null;
    }

    private String formatVehicle(WheeledVehicle v) {
        if (v == null) {
            return " \u001B[90m . \u001B[0m ";
        }
        String color;
        String code;
        int id = v.getVehicleId();
        if (v instanceof EmergencyCar) {
            color = "\u001B[91;1m";
            code = String.format("SAMU%d", id % 10);
        } else if (v instanceof ExceptionalLoad) {
            color = "\u001B[95m";
            code = String.format("CNV#%d", id % 100);
        } else if (v instanceof Truck) {
            color = "\u001B[93m";
            code = String.format("TRK#%d", id % 100);
        } else {
            color = "\u001B[92m";
            code = String.format("CAR#%d", id % 100);
        }
        if (code.length() > 5) code = code.substring(0, 5);
        while (code.length() < 5) code += " ";
        return "[" + color + code + resetColor() + "]";
    }

    private String formatBauVehicle(WheeledVehicle v) {
        if (v == null) {
            return "       ";
        }
        String code = String.format("B:%d", v.getVehicleId() % 1000);
        if (code.length() > 5) code = code.substring(0, 5);
        while (code.length() < 5) code += " ";
        return "\u001B[33m[" + code + "]\u001B[0m";
    }

    public void clearOutput() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    String resetColor() {
        return "\u001B[0m";
    }
}
