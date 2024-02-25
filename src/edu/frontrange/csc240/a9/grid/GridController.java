
package edu.frontrange.csc240.a9.grid;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;

import static software.haddon.util.FXMessage.error;
import static software.haddon.util.FXMessage.input;

/**
 * Controller for the exercise of displaying a tic-tac-toe board on the screen.
 *
 * @author		Dr. Bruce K. Haddon, Instructor
 * @version		3.0, 2020-08-22, CSC-240 Assignment 9
 */
public class GridController implements Initializable
{
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
private final GridModel model;

/**
 * (GUI) Title
 */
private String title;

/**
 * The view for which this is the controller
 */
private final GridView view;

/**
 * Constructor:
 *
 * @param view			the view for which this is the controller
 */
public GridController(GridView view)
{
	/* The view for which this is the controller. */
	this.view = view;

	/* Get the model being used by this controller. */
	model = new GridModel();
}

/**
 * Get the canvas on which the tic tac toe board is to be drawn.
 *
 * @return				the canvas
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
public GridModel getModel()
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
	/* Get the parameter values from the commandline, or, if there are no
	   commandline values, ask the user for a pair of values. If there is still no
	   values, use the default values. */
	Application.Parameters params = view.getParameters();
	List<String> values = new ArrayList<>(params.getRaw());
	/* Deal with the values input by the user. */
	processInputs(values);

	/* This listener listens to the scene in which the canvas is embedded, when
	   the size of the scene is change, the canvas is redrawn (and also resize). */
	@SuppressWarnings("Convert2Lambda")
	InvalidationListener listener = new InvalidationListener()
			{
				@Override
				public void invalidated(Observable o)
				{
					/* Find the current size of the scene. */
					double width = view.getScene().getWidth();
					double height = view.getScene().getHeight();
					/* Give up if the values are not yet initialized. */
					if( width == 0.0 || Double.isNaN(width) ) return;
					if( height == 0.0 || Double.isNaN(height) ) return;
					/* Draw the grid using the new sizes. */
					model.draw(canvas, width, height);
				}
			};

	/* Add this listener to the width and height properties of the scene. */
	view.getScene().widthProperty().addListener(listener);
	view.getScene().heightProperty().addListener(listener);

	/* Set the intial size to hold the count of cells with the given spacing. If
	   the window is resized, the count of squares is kept the same, but the cell
	   size is changed to accomodate the window size. */
	canvasSize = model.getCanvasSize();
	canvas = new Canvas(canvasSize, canvasSize);
	model.draw(canvas, canvasSize, canvasSize);

	/* The title for the application. */
	title = "Grid";
}

/**
 * Process the values specified by the user, either via the commandline or
 * by responding to pop-ups requesting values.
 *
 * @param values		list of values (either pre-defined or found here)
 */
private void processInputs(List<String> values)
{
	if( values.size() < 1 )					// value 1 here is 1 -- please leave
	{
		String result = input("Cell Count", "Enter a count of cells");
		if( result == null ) result = "";
		values.add(result);
	}

	int cellCount = 0;
	String countString = values.get(0).replaceAll("\\s|_|,", "");
	try
	{
		cellCount = Integer.valueOf(countString);
	} catch( NumberFormatException ex ) { /* do nothing */ }
	/* Set/check the cell count. Check is done by the model, returning true if OK. */
	if( !model.setCellCount(cellCount) )
		error("Cell count \"" + countString + "\" is invalid",
				"Replaced by " + model.getCellCount());

	/* Check the given cell size. */
	if( values.size() < 2 )					// value 2 here is 2 -- please leave
	{
		String result = input("Cell Size", "Enter size of cells (pixels)");
		if( result == null ) result = "";
		values.add(result);
	}
	double cellSize = 0;
	String sizeString = values.get(1).replaceAll("\\s|_|,", "");
	try
	{
		cellSize = Double.valueOf(sizeString);
	} catch( NumberFormatException ex ) { /* do nothing */ }

	/* Set and check the cell count. */
	if( !model.setCellSize(cellSize) )
		error("Cell size \"" + sizeString + "\" is invalid",
				"Replaced by " + model.getCellSize());
}
}
