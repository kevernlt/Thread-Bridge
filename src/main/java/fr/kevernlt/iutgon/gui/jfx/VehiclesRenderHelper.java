package fr.kevernlt.iutgon.gui.jfx;

import fr.kevernlt.iutgon.bridged.FloatingVehicle;
import fr.kevernlt.iutgon.bridged.Segment;
import fr.kevernlt.iutgon.bridged.Vehicle;
import fr.kevernlt.iutgon.bridged.WheeledVehicle;
import fr.kevernlt.iutgon.bridged.bridge.Bridge;
import fr.kevernlt.iutgon.bridged.enums.Way;
import fr.kevernlt.iutgon.bridged.objects.EmergencyCar;
import fr.kevernlt.iutgon.bridged.objects.ExceptionalLoad;
import fr.kevernlt.iutgon.bridged.objects.Truck;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import static fr.kevernlt.iutgon.gui.jfx.BridgeCanvasView.*;

/**
 * Helper class responsible for rendering dynamic elements on the canvas:
 * wheeled vehicles on the bridge segments, waiting vehicle queues on the river banks,
 * and boats waiting or crossing the river waterway.
 */
public class VehiclesRenderHelper {

    /**
     * Draws all wheeled vehicles currently on the bridge deck (main lane and emergency lane).
     *
     * @param gc       graphics context
     * @param l2r      west-to-east segments
     * @param r2l      east-to-west segments
     * @param segWidth pixel width of one segment
     */
    public static void drawVehiclesOnBridge(GraphicsContext gc, List<Segment> l2r, List<Segment> r2l, double segWidth) {
        for (Segment seg : l2r) {
            int id = seg.getSegmentId();
            double x = RIVER_X + (id - 1) * segWidth;

            WheeledVehicle v = seg.getVehicleOccupyingSegment();
            if (v != null) {
                if (isFirstSegmentOfVehicle(l2r, seg, v)) {
                    drawWheeledVehicle(gc, v, x + 4, BRIDGE_Y + 44, segWidth * v.getSize() - 8, 30, true);
                }
            }

            WheeledVehicle bauV = seg.getEmergencyLaneVehicle();
            if (bauV != null) {
                drawWheeledVehicle(gc, bauV, x + 4, BRIDGE_Y + 20, segWidth * bauV.getSize() - 8, 16, true);
            }
        }

        for (Segment seg : r2l) {
            int id = seg.getSegmentId();
            double x = RIVER_X + (id - 1) * segWidth;

            WheeledVehicle v = seg.getVehicleOccupyingSegment();
            if (v != null) {
                if (isFirstSegmentOfVehicle(r2l, seg, v)) {
                    double drawX = x + 4;
                    drawWheeledVehicle(gc, v, drawX, BRIDGE_Y + 96, segWidth * v.getSize() - 8, 30, false);
                }
            }

            WheeledVehicle bauV = seg.getEmergencyLaneVehicle();
            if (bauV != null) {
                drawWheeledVehicle(gc, bauV, x + 4, BRIDGE_Y + 134, segWidth * bauV.getSize() - 8, 16, false);
            }
        }
    }

    /**
     * Helper to verify if the segment is the front-most segment of a multi-segment vehicle.
     *
     * @param list segments list
     * @param s    segment to check
     * @param v    vehicle
     * @return true if s is the first segment of v
     */
    public static boolean isFirstSegmentOfVehicle(List<Segment> list, Segment s, WheeledVehicle v) {
        for (Segment other : list) {
            if (other.getVehicleOccupyingSegment() == v) {
                return other == s;
            }
        }
        return true;
    }

    /**
     * Draws an individual wheeled vehicle with its color, headlights, and label.
     *
     * @param gc          graphics context
     * @param v           vehicle to draw
     * @param x           X position
     * @param y           Y position
     * @param w           width
     * @param h           height
     * @param headingEast true if moving west-to-east
     */
    public static void drawWheeledVehicle(GraphicsContext gc, WheeledVehicle v, double x, double y, double w, double h, boolean headingEast) {
        Color baseColor;
        String label;
        int vid = v.getVehicleId();

        boolean isEmergency = v instanceof EmergencyCar;
        boolean isTruck = v instanceof Truck;
        boolean isExLoad = v instanceof ExceptionalLoad;

        if (isEmergency) {
            baseColor = Color.web("#dc2626");
            label = "SAMU#" + (vid % 10);
        } else if (isExLoad) {
            baseColor = Color.web("#9333ea");
            label = "CONVOI#" + (vid % 100);
        } else if (isTruck) {
            baseColor = Color.web("#d97706");
            label = "TRK#" + (vid % 100);
        } else {
            baseColor = Color.web("#059669");
            label = "CAR#" + (vid % 100);
        }

        if (v.isOnEmergencyLane()) {
            baseColor = Color.web("#ca8a04");
            label = "B:" + (vid % 100);
        }

        gc.setFill(baseColor);
        gc.fillRoundRect(x, y, w, h, 6, 6);

        Color headlightColor = Color.web("#fef08a");
        double frontX = headingEast ? (x + w - 2) : x;

        gc.setFill(headlightColor);
        gc.fillRect(frontX, y + 2, 2, 4);
        gc.fillRect(frontX, y + h - 6, 2, 4);

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(label, x + w / 2, y + h / 2 + 3);
    }

    public static FloatingVehicle lastCrossingBoat = null;
    public static double boatCrossingProgress = 0;

    /**
     * Draws all boats: both waiting boats at North and South docks, and the crossing boat.
     *
     * @param bridge bridge reference
     * @param gc     graphics context
     * @param w      width
     * @param h      height
     */
    public static void drawBoats(Bridge bridge, GraphicsContext gc, double w, double h) {
        List<FloatingVehicle> waiting = bridge.getBoatsWaiting();
        FloatingVehicle crossing = bridge.getCrossingBoat();

        List<FloatingVehicle> northWait = waiting.stream().filter(b -> b.getWay() == Way.BOAT_SOUTH).toList();
        double nx = RIVER_X + (RIVER_WIDTH / 2) - 80;
        double ny = 55;
        for (int i = 0; i < Math.min(northWait.size(), 4); i++) {
            drawBoat(gc, northWait.get(i), nx + (i * 45), ny, Way.BOAT_SOUTH, false);
        }

        List<FloatingVehicle> southWait = waiting.stream().filter(b -> b.getWay() == Way.BOAT_NORTH).toList();
        double sx = RIVER_X + (RIVER_WIDTH / 2) - 80;
        double sy = h - 90;
        for (int i = 0; i < Math.min(southWait.size(), 4); i++) {
            drawBoat(gc, southWait.get(i), sx + (i * 45), sy, Way.BOAT_NORTH, false);
        }

        if (crossing != null) {
            if (lastCrossingBoat != crossing) {
                lastCrossingBoat = crossing;
                boatCrossingProgress = 0;
            }
            boatCrossingProgress += 0.015;
            if (boatCrossingProgress > 1.0) boatCrossingProgress = 1.0;

            double cx = RIVER_X + RIVER_WIDTH / 2;
            double cy;
            if (crossing.getWay() == Way.BOAT_SOUTH) {
                cy = 90 + (h - 180) * boatCrossingProgress;
            } else {
                cy = (h - 90) - (h - 180) * boatCrossingProgress;
            }
            drawBoat(gc, crossing, cx, cy, crossing.getWay(), true);
        } else {
            lastCrossingBoat = null;
            boatCrossingProgress = 0;
        }
    }

    /**
     * Draws a single boat with hull, wake effect, and label.
     *
     * @param gc         graphics context
     * @param boat       boat to draw
     * @param x          X position
     * @param y          Y position
     * @param way        navigation way
     * @param isCrossing true if currently crossing under the bridge
     */
    public static void drawBoat(GraphicsContext gc, FloatingVehicle boat, double x, double y, Way way, boolean isCrossing) {
        double bw = 32;
        double bh = 54;
        boolean headingSouth = (way == Way.BOAT_SOUTH);

        gc.save();
        gc.translate(x, y);

        if (isCrossing) {
            gc.setStroke(Color.web("#e0f2fe", 0.5));
            gc.setLineWidth(2.5);
            double wakeY = headingSouth ? -bh / 2 : bh / 2;
            gc.strokeLine(-12, wakeY, -24, wakeY + (headingSouth ? -18 : 18));
            gc.strokeLine(12, wakeY, 24, wakeY + (headingSouth ? -18 : 18));
        }

        gc.setFill(Color.web("#0284c7"));
        gc.beginPath();
        if (headingSouth) {
            gc.moveTo(-bw / 2, -bh / 2);
            gc.lineTo(bw / 2, -bh / 2);
            gc.lineTo(bw / 2, bh / 4);
            gc.lineTo(0, bh / 2);
            gc.lineTo(-bw / 2, bh / 4);
        } else {
            gc.moveTo(-bw / 2, bh / 2);
            gc.lineTo(bw / 2, bh / 2);
            gc.lineTo(bw / 2, -bh / 4);
            gc.lineTo(0, -bh / 2);
            gc.lineTo(-bw / 2, -bh / 4);
        }
        gc.closePath();
        gc.fill();

        gc.setFill(Color.WHITE);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("#" + boat.getBoatId(), 0, headingSouth ? -bh / 2 - 4 : bh / 2 + 12);

        gc.restore();
    }

    /**
     * Draws vehicles waiting at the entrance gates on both river banks.
     *
     * @param bridge bridge reference
     * @param gc     graphics context
     */
    public static void drawWaitingVehicles(Bridge bridge, GraphicsContext gc) {
        HashMap<Way, PriorityQueue<Vehicle>> map = bridge.getVehicleAtBridgeEntry();

        PriorityQueue<Vehicle> qL2R = map.get(Way.CAR_LEFT_TO_RIGHT);
        if (qL2R != null && !qL2R.isEmpty()) {
            List<Vehicle> list = new ArrayList<>(qL2R);
            double x = WEST_BANK_WIDTH - 60;
            for (int i = 0; i < Math.min(list.size(), 3); i++) {
                Vehicle v = list.get(i);
                if (v instanceof WheeledVehicle wv) {
                    drawWheeledVehicle(gc, wv, x - (i * 48), BRIDGE_Y + 44, 40, 24, true);
                }
            }
        }

        PriorityQueue<Vehicle> qR2L = map.get(Way.CAR_RIGHT_TO_LEFT);
        if (qR2L != null && !qR2L.isEmpty()) {
            List<Vehicle> list = new ArrayList<>(qR2L);
            double x = EAST_BANK_X + 28;
            for (int i = 0; i < Math.min(list.size(), 3); i++) {
                Vehicle v = list.get(i);
                if (v instanceof WheeledVehicle wv) {
                    drawWheeledVehicle(gc, wv, x + (i * 48), BRIDGE_Y + 96, 40, 24, false);
                }
            }
        }
    }
}
