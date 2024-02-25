
package edu.frontrange.csc240.a9.grid;

import software.haddon.util.FXMessage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import static software.haddon.util.FXMessage.display;
import static javafx.scene.control.Alert.AlertType.INFORMATION;

/**
 * A Window for displaying a grid, with a label for the window.
 *
 * @author		Dr. Bruce K. Haddon, Instructor
 * @version		3.0, 2020-08-22, CSC-240 Assignment 9
 */
public class GridView extends Application
{
/**
 * The controller for this view.
 */
private GridController controller;

/**
 * Any messages to be displayed when the window starts.
 */
private String message;

/**
 * The scene that is the content of the window.
 */
private Scene scene;

/**
 * Constructor:
 */
public GridView() { }

/**
 * The scene that is the content of the window (Stage).
 *
 * @return				the scene
 */
public Scene getScene()
{
	return scene;
}

/**
 * Build the window, and initiate the processing. Tell the controller where to find
 * its view
 *
 * @param stage			stage (window) supplied by JavaFX
 */
@SuppressWarnings("Convert2Lambda")
@Override
public void start(Stage stage)
{
	Pane root = new Pane();
	root.setStyle("-fx-background-color: #EEEEEE");
	scene = new Scene(root);
	stage.setScene(scene);

	this.controller = new GridController(this);
	controller.initialize(null, null);

	Canvas canvas = controller.getCanvas();
	root.getChildren().add(canvas);
	root.setPrefSize(canvas.getWidth(), canvas.getHeight());

	/* Title the window. */
	stage.setTitle(controller.getTitle());
	/* Initiate the showing of the window using the Event Dispatch Thread. */
	stage.show();
	/* Allow the window to be resized after starting. */
	stage.setMinWidth(stage.getWidth());
	stage.setMinHeight(stage.getHeight());
	stage.setResizable(true);
	root.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
	/* If there is a message, show it. */
	if( message != null )
		display(INFORMATION, "Values changes", message);
}

/**
 * Any starting message to be displayed once the window is in place.
 *
 * @param message		the message
 */
public void setMessage(String message)
{
	this.message = message;
}
}
