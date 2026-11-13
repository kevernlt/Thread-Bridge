package fr.kevernlt.iutgon.gui.jfx;

import fr.kevernlt.iutgon.bridged.Segment;
import fr.kevernlt.iutgon.bridged.bridge.Bridge;
import fr.kevernlt.iutgon.bridged.enums.BridgeState;
import fr.kevernlt.iutgon.bridged.enums.Way;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.List;

import static fr.kevernlt.iutgon.gui.jfx.BridgeCanvasView.*;
import static fr.kevernlt.iutgon.gui.jfx.VehiclesRenderHelper.drawVehiclesOnBridge;

/**
 * Helper class responsible for rendering static terrain elements:
 * river banks, access roads, river channel, bridge deck, and gates.
 */
public class TerrainRenderHelper {

    /**
     * Draws the west and east banks of the river.
     *
     * @param gc graphics context
     * @param w  canvas width
     * @param h  canvas height
     */
    public static void drawBanks(GraphicsContext gc, double w, double h) {
        gc.setFill(Color.web("#2d5a40"));
        gc.fillRect(0, 0, WEST_BANK_WIDTH, h);

        gc.setFill(Color.web("#2d5a40"));
        gc.fillRect(EAST_BANK_X, 0, w - EAST_BANK_X, h);

        gc.setFill(Color.web("#475569"));
        gc.fillRect(WEST_BANK_WIDTH - 12, 0, 12, h);
        gc.fillRect(EAST_BANK_X, 0, 12, h);

        drawRoadApproach(gc, 0, WEST_BANK_WIDTH - 12);
        drawRoadApproach(gc, EAST_BANK_X + 12, w - (EAST_BANK_X + 12));
    }

    /**
     * Draws the road approach leading to the bridge.
     *
     * @param gc     graphics context
     * @param startX X coordinate
     * @param width  road width
     */
    public static void drawRoadApproach(GraphicsContext gc, double startX, double width) {
        double roadY = BRIDGE_Y + 16;
        double roadH = 138;

        gc.setFill(Color.web("#334155"));
        gc.fillRect(startX, roadY, width, roadH);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeLine(startX, roadY + roadH / 2, startX + width, roadY + roadH / 2);

        gc.setStroke(Color.web("#f59e0b"));
        gc.setLineWidth(1.5);
        gc.strokeLine(startX, roadY + 22, startX + width, roadY + 22);
        gc.strokeLine(startX, roadY + roadH - 22, startX + width, roadY + roadH - 22);
    }

    /**
     * Draws the river water channel.
     *
     * @param bridge bridge reference
     * @param gc     graphics context
     * @param w      width
     * @param h      height
     */
    public static void drawRiver(Bridge bridge, GraphicsContext gc, double w, double h) {
        gc.setFill(Color.web("#0e3d59"));
        gc.fillRect(RIVER_X, 0, RIVER_WIDTH, h);

        gc.setFill(Color.web("#94a3b8", 0.4));
    }

    /**
     * Draws the bridge structure, segments, and calls vehicle rendering.
     *
     * @param bridge bridge reference
     * @param gc     graphics context
     * @param w      width
     * @param h      height
     */
    public static void drawBridge(Bridge bridge, GraphicsContext gc, double w, double h) {
        BridgeState state = bridge.getCurrentState();
        List<Segment> l2r = bridge.getSegmentsForWay(Way.CAR_LEFT_TO_RIGHT);
        List<Segment> r2l = bridge.getSegmentsForWay(Way.CAR_RIGHT_TO_LEFT);
        int totalSegments = Math.max(l2r.size(), r2l.size());
        if (totalSegments == 0) totalSegments = 10;

        double segWidth = RIVER_WIDTH / totalSegments;

        gc.setFill(Color.web("#1e293b"));
        gc.fillRect(RIVER_X, BRIDGE_Y, RIVER_WIDTH, BRIDGE_HEIGHT);

        double openGap = 0;
        if (state == BridgeState.OPEN) {
            openGap = 220;
        } else if (state == BridgeState.OPENING || state == BridgeState.CLOSING) {
            openGap = 120;
        }

        double bridgeCenter = RIVER_X + RIVER_WIDTH / 2;

        drawDeckSection(gc, RIVER_X, bridgeCenter - openGap / 2);
        drawDeckSection(gc, bridgeCenter + openGap / 2, RIVER_X + RIVER_WIDTH);

        if (openGap > 0) {
            drawMovableDeck(gc, bridgeCenter, openGap, state);
        } else {
            drawDeckSection(gc, bridgeCenter - 110, bridgeCenter + 110);
        }

        gc.setStroke(Color.web("#64748b", 0.5));
        gc.setLineWidth(1);
        for (int i = 1; i < totalSegments; i++) {
            double sx = RIVER_X + i * segWidth;
            if (openGap == 0 || sx < bridgeCenter - openGap / 2 || sx > bridgeCenter + openGap / 2) {
                gc.strokeLine(sx, BRIDGE_Y + 16, sx, BRIDGE_Y + BRIDGE_HEIGHT - 16);
            }
        }

        gc.setFill(Color.web("#94a3b8"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        gc.setTextAlign(TextAlignment.CENTER);
        for (int i = 0; i < totalSegments; i++) {
            double sx = RIVER_X + i * segWidth + segWidth / 2;
            gc.fillText(String.format("S%02d", i + 1), sx, BRIDGE_Y + BRIDGE_HEIGHT / 2 + 4);
        }

        drawVehiclesOnBridge(gc, l2r, r2l, segWidth);
    }

    /**
     * Draws a rectangular section of the fixed bridge deck.
     *
     * @param gc graphics context
     * @param x1 start X
     * @param x2 end X
     */
    public static void drawDeckSection(GraphicsContext gc, double x1, double x2) {
        if (x2 <= x1) return;
        double width = x2 - x1;

        gc.setFill(Color.web("#334155"));
        gc.fillRect(x1, BRIDGE_Y + 16, width, 138);

        gc.setFill(Color.web("#0f172a"));
        gc.fillRect(x1, BRIDGE_Y + 80, width, 10);

        gc.setFill(Color.web("#94a3b8"));
        gc.fillRect(x1, BRIDGE_Y + 10, width, 6);
        gc.fillRect(x1, BRIDGE_Y + BRIDGE_HEIGHT - 16, width, 6);

        gc.setStroke(Color.web("#f59e0b", 0.8));
        gc.setLineWidth(1.5);
        gc.strokeLine(x1, BRIDGE_Y + 38, x2, BRIDGE_Y + 38);
        gc.strokeLine(x1, BRIDGE_Y + 132, x2, BRIDGE_Y + 132);
    }

    /**
     * Draws the animated movable central section of the bridge.
     *
     * @param gc     graphics context
     * @param center center X coordinate
     * @param gap    gap size
     * @param state  current bridge state
     */
    public static void drawMovableDeck(GraphicsContext gc, double center, double gap, BridgeState state) {
        gc.setFill(Color.web("#17577e"));
        gc.fillRect(center - gap / 2, BRIDGE_Y + 10, gap, BRIDGE_HEIGHT - 20);

        double leftPylon = center - gap / 2;
        double rightPylon = center + gap / 2;

        gc.setFill(Color.web("#475569"));
        gc.fillRect(leftPylon - 16, BRIDGE_Y, 16, BRIDGE_HEIGHT);
        gc.fillRect(rightPylon, BRIDGE_Y, 16, BRIDGE_HEIGHT);

        Color beaconColor = switch (state) {
            case OPEN -> Color.web("#22c55e");
            case OPENING, CLOSING -> Color.web("#78350f");
            case CLOSED -> Color.web("#ef4444");
        };

        gc.setFill(beaconColor);
        gc.fillOval(leftPylon - 12, BRIDGE_Y - 8, 10, 10);
        gc.fillOval(rightPylon + 2, BRIDGE_Y - 8, 10, 10);
        gc.fillOval(leftPylon - 12, BRIDGE_Y + BRIDGE_HEIGHT - 2, 10, 10);
        gc.fillOval(rightPylon + 2, BRIDGE_Y + BRIDGE_HEIGHT - 2, 10, 10);

        gc.setTextAlign(TextAlignment.CENTER);
        if (state == BridgeState.OPEN) {
            gc.setFill(Color.web("#48dbfb"));
            gc.fillText("BRIDGE OPEN", center, BRIDGE_Y + BRIDGE_HEIGHT / 2 - 12);
            gc.setFill(Color.web("#e2e8f0"));
            gc.fillText("Boats passing OK", center, BRIDGE_Y + BRIDGE_HEIGHT / 2 + 10);
        } else {
            gc.setFill(Color.web("#f59e0b"));
            gc.fillText("BRIDGE MOVING", center, BRIDGE_Y + BRIDGE_HEIGHT / 2);
        }
    }

    /**
     * Draws the road gates on both banks.
     *
     * @param bridge bridge reference
     * @param gc     graphics context
     */
    public static void drawGates(Bridge bridge, GraphicsContext gc) {
        boolean closed = bridge.isRoadsGateClosed();

        drawGateBarrier(gc, WEST_BANK_WIDTH - 24, BRIDGE_Y + 16, closed, true);
        drawGateBarrier(gc, EAST_BANK_X + 16, BRIDGE_Y + 16, closed, false);
    }

    /**
     * Draws a single gate barrier (closed red horizontal bar or open green angled bar).
     *
     * @param gc     graphics context
     * @param postX  X position of the barrier post
     * @param roadY  Y position of the road
     * @param closed true if barrier is closed
     * @param isWest true if on west bank
     */
    public static void drawGateBarrier(GraphicsContext gc, double postX, double roadY, boolean closed, boolean isWest) {
        double roadH = 138;
        if (closed) {
            gc.setStroke(Color.web("#ef4444"));
            gc.setLineWidth(5);
            gc.strokeLine(postX, roadY, postX, roadY + roadH);
            gc.setStroke(Color.RED);
            gc.strokeLine(postX, roadY, postX, roadY + roadH);
            gc.setLineDashes(null);
        } else {
            gc.setStroke(Color.web("#22c55e"));
            gc.setLineWidth(5);
            double armLen = 40;
            gc.strokeLine(postX, roadY, isWest ? postX - armLen : postX + armLen, roadY - armLen);
            gc.strokeLine(postX, roadY + roadH, isWest ? postX - armLen : postX + armLen, roadY + roadH + armLen);
        }
    }
}
