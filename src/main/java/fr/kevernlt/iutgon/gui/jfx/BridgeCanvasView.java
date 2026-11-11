package fr.kevernlt.iutgon.gui.jfx;

import fr.kevernlt.iutgon.bridged.bridge.Bridge;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static fr.kevernlt.iutgon.gui.jfx.TerrainRenderHelper.*;
import static fr.kevernlt.iutgon.gui.jfx.VehiclesRenderHelper.*;

/**
 * Custom 60 FPS JavaFX Canvas that renders the top-down 2D simulation
 * including the river banks, water, moving bridge deck, vehicles, and boats.
 */
public class BridgeCanvasView extends Canvas {
    public final Bridge bridge;
    public AnimationTimer timer;

    // Viewport layout dimensions
    public static final double WEST_BANK_WIDTH = 200;
    public static final double EAST_BANK_X = 960;
    public static final double RIVER_X = 200;
    public static final double RIVER_WIDTH = 760;

    public static final double BRIDGE_Y = 200;
    public static final double BRIDGE_HEIGHT = 170;

    /**
     * Creates the canvas view with dimensions and starts the animation timer.
     *
     * @param bridge bridge to render
     * @param width  canvas width
     * @param height canvas height
     */
    public BridgeCanvasView(Bridge bridge, double width, double height) {
        super(width, height);
        this.bridge = bridge;

        startAnimation();
    }

    /**
     * Starts the 60 FPS render loop.
     */
    public void startAnimation() {
        if (timer != null) {
            timer.stop();
        }
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                render();
            }
        };
        timer.start();
    }

    /**
     * Stops the animation timer.
     */
    public void stopAnimation() {
        if (timer != null) {
            timer.stop();
        }
    }

    /**
     * Renders each layer: background, river banks, water channel, bridge deck, boats, gates, and waiting queues.
     */
    private void render() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.rgb(0, 0, 0));
        gc.fillRect(0, 0, w, h);

        drawBanks(gc, w, h);
        drawRiver(this.bridge, gc, w, h);
        drawBridge(this.bridge, gc, w, h);
        drawBoats(this.bridge, gc, w, h);
        drawGates(this.bridge, gc);
        drawWaitingVehicles(this.bridge, gc);
    }
}
