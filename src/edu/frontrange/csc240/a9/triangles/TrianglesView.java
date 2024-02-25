
package edu.frontrange.csc240.a9.triangles;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * A Window for displaying the triangles, with a label for the window.
 *
 * @author		Dr. Bruce K. Haddon, Instructor
 * @version		2.2, 2020-08-22, CSC-240 Assignment 9
 */
@SuppressWarnings("serial")
public class TrianglesView extends Application
{
/**
 * BackgroundColor
 */
private static final Color BACKGROUND_COLOR = Color.BLACK;

/**
 * The controller for this view.
 */
private TrianglesController controller;

/**
 * The scene that is the content of the window.
 */
private Scene scene;

/**
 * Constructor: a window to hold a grid.
 */
public TrianglesView() { }

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
	/* Establish a root node. */
	Pane root = new Pane();
	root.setBackground(new Background(
								new BackgroundFill(BACKGROUND_COLOR, null, null)));
	/* The scene contains the root. */
	scene = new Scene(root);
	/* ... and the stage (window) contains the scene. */
	stage.setScene(scene);

	/* Create and remember the controller. */
	this.controller = new TrianglesController(this, BACKGROUND_COLOR);
	controller.initialize(null, null);

	/* Get the canvas, and then add that component to the window as its only
	   content. */
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
}
}
