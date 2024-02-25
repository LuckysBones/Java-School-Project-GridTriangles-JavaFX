
package edu.frontrange.csc240.a9.triangles;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

/**
 * Controller for the View for displaying triangles.
 *
 * @author		Dr. Bruce K. Haddon, Instructor
 * @version		2.2, 2020-08-22, CSC-240 Assignment 9
 */
public class TrianglesController implements Initializable
{
/**
 * The number of triangles to be included in the drawing.
 */
private static final int NUMBER_OF_TRIANGLES = 10;

/**
 * The title to go on the window.
 */
private static final String TITLE = "Random Triangles";

/**
 * (GUI) The panel holding and showing the actual board.
 */
private Canvas canvas;

/**
 * Overall size of the canvas.
 */
private double canvasSize;

/**
 * The model for this controller.
 */
private final TrianglesModel model;

/**
 * (GUI) Title
 */
private String title;

/**
 * The view for which this is the controller
 */
private final TrianglesView view;

/**
 * Constructor.
 *
 * @param view			the view for which this is the controller
 * @param backGround	the background color selected by the View
 */
public TrianglesController(TrianglesView view, Color backGround)
{
	/* The view for which this is the controller. */
	this.view = view;

	/* Get the model being used by this controller. */
	model = new TrianglesModel(NUMBER_OF_TRIANGLES, backGround);
}

/**
 * Get the panel on which the triangles are to be drawn.
 *
 * @return				the boardPanel
 */
public Canvas getCanvas()
{
	return canvas;
}

/**
 * Inform the caller of the model used by this controller.
 *
 * @return				the model
 */
public TrianglesModel getModel()
{
	return model;
}

/**
 * @return				the title
 */
public String getTitle()
{
	return title;
}

/**
 * Create the components for the controls needed in the view. Validate the given
 * cell count and cell size.
 *
 * @param url			not used
 * @param rb			not used
 */
@Override
public void initialize(URL url, ResourceBundle rb)
{
	/* This listener listens to the scene in which the canvas is embedded, when
	   the size of the scene is change, the canvas is redrawn (and also resized). */
	@SuppressWarnings("Convert2Lambda")
	InvalidationListener listener =	new InvalidationListener()
				{
					 @Override
					public void invalidated(Observable o)
					{
						double width =  view.getScene().getWidth();
						double height = view.getScene().getHeight();
						if( width == 0.0 || Double.isNaN(width)) return;
						if( height == 0.0 || Double.isNaN(height)) return;
						model.createTriangles(width, height);
						model.draw(canvas, width, height);
					}
				};

	/* Add this listener to the width and height properties of the scene. */
	view.getScene().widthProperty().addListener(listener);
	view.getScene().heightProperty().addListener(listener);

	/* Set the intial size to hold the count of triangles. If the window is
	   resized, a new set of triangles is computed and shown. */
	canvasSize = model.getInitialCanvasSize();
	canvas = new Canvas(canvasSize, canvasSize);
	model.createTriangles(canvasSize, canvasSize);
	model.draw(canvas, canvasSize, canvasSize);

	title = TITLE;
}
}
