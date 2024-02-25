
package edu.frontrange.csc240.a9.triangles;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

 /**
 * Model, containing the current collection of displayed triangles.
 *
 * @author		Dr. Bruce K. Haddon, Instructor
 * @version		2.2, 2020-08-22, CSC-240 Assignment 9
 */
public class TrianglesModel
{
/**
 * The initial size of the canvas on which to draw the triangle
 */
private static final int INITIAL_SIZE = 400;

/**
 * The background color of the view on which the triangles are drawn.
 */
private final Color backGroundColor;

/**
 * The number of triangles to hold in the model.
 */
private final int numberOfTriangles;

/**
 * The list of triangles generated.
 */
private List<RandomTriangle> triangles;

/**
 * Create the model.
 *
 * @param numberOfTriangles the number of triangles in the model
 * @param backGroundColor the background color over which the triangles appear
 */
public TrianglesModel(int numberOfTriangles, Color backGroundColor)
{
	/* Remember the number of trianges to create and draw. */
	this.numberOfTriangles = numberOfTriangles;

	/* Note the background color that is in use. */
	this.backGroundColor = backGroundColor;
}

/**
 * Create the needed triangles within the given dimensions.
 *
 * @param width			width of the canvas on which to paint the triangles
 * @param height		height of the canvas on which to paint the triangles
 */
public void createTriangles(double width, double height)
{
	/* Reset the list of triangles. */
	triangles = new LinkedList<>();

	/* For each of the required number of triangles ... */
	for( int i = 0; i != numberOfTriangles; ++i )
	{
		/* Get the triangle shape, with a size larger than the minimum
		   size, and no larger than the largest size, and add it to the
		   collection. */
		RandomTriangle triangle = new RandomTriangle(width, height, backGroundColor);
		triangles.add(triangle);
	}

	/* Sort triangles from largest to smallest, to ensure all triangles can be
	   seen. */
	Collections.sort(triangles);
}

/**
 * Draw the current state of the model on the given graphics environment at the
 * given origin.
 * <p>
 * Note that this drawing does have the outside lines.
 *
 * @param canvas		the canvas on which to draw
 * @param width			the desired width of that canvas
 * @param height		the desired height of that canvas
 */
public void draw(Canvas canvas, double width, double height)
{
	/* Get the graphics context for this canvas. */
	GraphicsContext g = canvas.getGraphicsContext2D();
	/* Clear out the previous drawing. */
	g.clearRect(0.0, 0.0, canvas.getWidth(), canvas.getHeight());

	/* Set the new desired size for the canvas. */
	canvas.setWidth(width);
	canvas.setHeight(height);

	/* Get the given number of triangles, and draw each of them on the
	   GraphicsContext of the canvas, placing each at the randomly computed
	   origin.

	   The following statement (using a lambda expression) is equivalent to
	   this statement (using an enhanced for loop).

	   for( RandomTriangle triangle : triangles ) triangle.draw(g); */

	triangles.forEach((triangle) ->	triangle.draw(g) );
}

/**
 * Get the initial size for the drawing area, as the initial canvas size.
 *
 * @return				the initial canvas size
 */
public double getInitialCanvasSize()
{
	return INITIAL_SIZE;
}
}
