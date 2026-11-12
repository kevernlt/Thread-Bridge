package fr.kevernlt.iutgon.gui.jfx.controller;

import fr.kevernlt.iutgon.bridged.FloatingVehicle;
import fr.kevernlt.iutgon.bridged.Segment;
import fr.kevernlt.iutgon.bridged.bridge.Bridge;
import fr.kevernlt.iutgon.bridged.enums.BridgeState;
import fr.kevernlt.iutgon.bridged.enums.Way;
import fr.kevernlt.iutgon.bridged.objects.Boat;
import fr.kevernlt.iutgon.bridged.objects.Car;
import fr.kevernlt.iutgon.bridged.objects.EmergencyCar;
import fr.kevernlt.iutgon.bridged.objects.ExceptionalLoad;
import fr.kevernlt.iutgon.bridged.objects.Truck;
import fr.kevernlt.iutgon.gui.jfx.BridgeCanvasView;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * JavaFX Controller for the bridge simulation.
 * Manages user actions (adding vehicles with direction selection, forcing bridge opening)
 * and updates simple status text indicators.
 */
public class Controler implements Initializable {

    private Bridge bridge;
    private BridgeCanvasView canvasView;

    @FXML private Label lblState;
    @FXML private Label lblGates;
    @FXML private Label lblTimer;
    @FXML private Label lblWaterway;

    @FXML private StackPane canvasContainer;

    @FXML private RadioButton rbLeftToRight;
    @FXML private RadioButton rbRightToLeft;
    @FXML private RadioButton rbNorthToSouth;
    @FXML private RadioButton rbSouthToNorth;

    @FXML private Button btnCar;
    @FXML private Button btnTruck;
    @FXML private Button btnConvoi;
    @FXML private Button btnSamu;
    @FXML private Button btnBoat;
    @FXML private Button btnForceOpen;

    /**
     * Default constructor required by JavaFX.
     */
    public Controler() {
    }

    /**
     * Constructor with an existing Bridge instance.
     *
     * @param bridge the bridge to control
     */
    public Controler(Bridge bridge) {
        this.bridge = bridge;
    }

    /**
     * Sets the bridge instance.
     *
     * @param bridge the bridge
     */
    public void setBridge(Bridge bridge) {
        this.bridge = bridge;
    }

    /**
     * Returns the bridge instance.
     *
     * @return the bridge
     */
    public Bridge getBridge() {
        return bridge;
    }

    /**
     * Initializes the controller, sets up the 2D canvas, handlers, and update loop.
     *
     * @param location  URL location
     * @param resources resource bundle
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (bridge == null) {
            initDefaultBridge();
        }

        // Create and add the 2D animated canvas
        canvasView = new BridgeCanvasView(bridge, 1160, 500);
        if (canvasContainer != null) {
            canvasContainer.getChildren().clear();
            canvasContainer.getChildren().add(canvasView);
        }

        setupActionHandlers();
        startStatusUpdateLoop();
    }

    /**
     * Initializes a default bridge setup with 20 segments per direction.
     */
    private void initDefaultBridge() {
        bridge = new Bridge();

        int howManySegments = 20;
        List<Segment> segments = new ArrayList<>();
        for (int i = 1; i <= howManySegments; i++) {
            segments.add(new Segment(i, Way.CAR_LEFT_TO_RIGHT));
        }
        for (int i = howManySegments; i >= 1; i--) {
            segments.add(new Segment(i, Way.CAR_RIGHT_TO_LEFT));
        }
        bridge.addSegment(segments.toArray(new Segment[0]));

        // Add some initial vehicles
        bridge.addVehicle(new ExceptionalLoad(Way.CAR_RIGHT_TO_LEFT, bridge));
        bridge.addVehicle(new Car(Way.CAR_RIGHT_TO_LEFT, bridge));
        bridge.addVehicle(new Truck(Way.CAR_LEFT_TO_RIGHT, bridge));
        bridge.addVehicle(new Car(Way.CAR_LEFT_TO_RIGHT, bridge));

        // Add some initial boats
        bridge.addBoat(new Boat(Way.BOAT_NORTH, bridge));
        bridge.addBoat(new Boat(Way.BOAT_SOUTH, bridge));
    }

    /**
     * Returns the currently selected road direction from the radio buttons.
     *
     * @return CAR_RIGHT_TO_LEFT if rbRightToLeft is selected, else CAR_LEFT_TO_RIGHT
     */
    private Way getSelectedRoadWay() {
        if (rbRightToLeft != null && rbRightToLeft.isSelected()) {
            return Way.CAR_RIGHT_TO_LEFT;
        }
        return Way.CAR_LEFT_TO_RIGHT;
    }

    /**
     * Returns the currently selected boat direction from the radio buttons.
     *
     * @return BOAT_NORTH if rbSouthToNorth is selected, else BOAT_SOUTH
     */
    private Way getSelectedBoatWay() {
        if (rbSouthToNorth != null && rbSouthToNorth.isSelected()) {
            return Way.BOAT_NORTH;
        }
        return Way.BOAT_SOUTH;
    }

    /**
     * Sets up action handlers for vehicle addition buttons.
     */
    private void setupActionHandlers() {
        if (btnCar != null) {
            btnCar.setOnAction(e -> {
                Way way = getSelectedRoadWay();
                bridge.addVehicle(new Car(way, bridge));
            });
        }

        if (btnTruck != null) {
            btnTruck.setOnAction(e -> {
                Way way = getSelectedRoadWay();
                bridge.addVehicle(new Truck(way, bridge));
            });
        }

        if (btnConvoi != null) {
            btnConvoi.setOnAction(e -> {
                Way way = getSelectedRoadWay();
                bridge.addVehicle(new ExceptionalLoad(way, bridge));
            });
        }

        if (btnSamu != null) {
            btnSamu.setOnAction(e -> {
                Way way = getSelectedRoadWay();
                bridge.addVehicle(new EmergencyCar(way, bridge));
            });
        }

        if (btnBoat != null) {
            btnBoat.setOnAction(e -> {
                Way way = getSelectedBoatWay();
                bridge.addBoat(new Boat(way, bridge));
            });
        }

        if (btnForceOpen != null) {
            btnForceOpen.setOnAction(e -> bridge.triggerImmediateOpening());
        }
    }

    /**
     * Starts the periodic loop that updates simple UI status labels every frame.
     */
    private void startStatusUpdateLoop() {
        AnimationTimer statusTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (bridge == null) return;

                BridgeState state = bridge.getCurrentState();
                boolean gatesClosed = bridge.isRoadsGateClosed();
                long elapsed = bridge.getStateElapsedTime();

                // Bridge state label
                if (lblState != null) {
                    lblState.setText("Pont : " + state.name());
                }

                // Road gates label
                if (lblGates != null) {
                    lblGates.setText("Barrières : " + (gatesClosed ? "Fermées" : "Ouvertes"));
                }

                // Timer label
                if (lblTimer != null) {
                    if (state == BridgeState.CLOSED) {
                        long remaining = Math.max(0, (60000 - elapsed) / 1000);
                        lblTimer.setText("Prochaine levée : " + remaining + "s");
                    } else if (state == BridgeState.OPEN) {
                        lblTimer.setText("Ouvert depuis : " + (elapsed / 1000) + "s");
                    } else {
                        lblTimer.setText("Manœuvre en cours...");
                    }
                }

                // Waterway label
                if (lblWaterway != null) {
                    int waitingCount = bridge.getWaitingBoatsCount();
                    FloatingVehicle crossing = bridge.getCrossingBoat();
                    if (crossing != null) {
                        lblWaterway.setText("Bateau en cours : #" + crossing.getBoatId());
                    } else {
                        lblWaterway.setText("Bateaux en attente : " + waitingCount);
                    }
                }
            }
        };
        statusTimer.start();
    }

    /**
     * Starts background threads that periodically inject cars and boats for demonstration.
     */


    /**
     * Stops the canvas animation timer.
     */
    public void stopAnimation() {
        if (canvasView != null) {
            canvasView.stopAnimation();
        }
    }
}
