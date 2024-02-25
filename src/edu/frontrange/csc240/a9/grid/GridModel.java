
package edu.frontrange.csc240.a9.grid;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import static java.lang.Math.max;

 /**
 * The model for this exercise. It is a very simple model, holding only the
 * parameters for the count of cells each way, and the size of each cell of the
 * grid, as well as the default values (which are the minimal) for each.
 *
 * @author		Dr. Bruce K. Haddon, Instructor
 * @version		3.0, 2020-08-22, CSC-240 Assignment 9
 */
public class GridModel
{
/**
 * Margin to left around the board.
 */
private static final double MARGIN = 12.0;				// pixels

/**
 * Minimum spacing of squares in the overall board (also used as the default).
 */
private static final int MININUM_COUNT = 10;			// count of cells

/**
 * Minimum spacing of cells in the overall grid (also used as the default).
 */
private static final double MININUM_SIZE = 10.0;

/**
 * The number of cells in the grid, to form a square grid.
 */
private int cellCount;

/**
 * The size of each individual cell.
 */
private double cellSize;

/**
 * Constructor:
 */
public GridModel() { }

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

	/* Compute size of each of the cells of the grid. */
	double availableSize = Math.max(MININUM_SIZE, Math.min(width, height));
	cellSize = (availableSize - 2.0 * getMargin()) / getCellCount();
	/* The length of the lines is just the spacing by the number of cells.  */
	double length = getCellCount() * cellSize;

	double xbound = Math.floor(length + cellSize + MARGIN);
	double ybound =  Math.floor(length + cellSize + MARGIN);

	/* Draw the horizontal lines starting at the x margin, for each y value. */
	for( double y = MARGIN, x = MARGIN; y < ybound; y += cellSize )
		g.strokeLine(x, y, x + length, y);

	/* Draw the vertical lines starting at the y margin, for each x values. */
	for( double x = MARGIN, y = MARGIN; x < xbound; x += cellSize )
		g.strokeLine(x, y, x, y + length);
}

/**
 * Get the canvas size based on the number of squares, the size of the squares, and
 * the space left for a margin.
 *
 * @return				the canvas size
 */
public double getCanvasSize()
{
	return getCellCount() * getCellSize() +	2.0 * getMargin();
}

/**
 * Get the margin that is left around the board
 *
 * @return				the margin (in pixels)
 */
public double getMargin()
{
	return MARGIN;
}

/**
 * @return				the count of squares (read only)
 */
public int getCellCount()
{
	return cellCount;
}

/**
 * Set the number of cells required in the grid, or to the default if the given
 * value is less that the minimum.
 *
 * @param cellCount		desired cell count
 * @return				true if this value is used
 */
public boolean setCellCount(int cellCount)
{
	this.cellCount = max(cellCount, MININUM_COUNT);
	return this.cellCount == cellCount;
}

/**
 * Get the cell size (in pixels)
 *
 * @return				the cell size
 */
public double getCellSize()
{
	return cellSize;
}

/**
 * Set the cell size to the given value, or to the default if the given value is
 * less that the minimum.
 *
 * @param cellSize		initial size of a square on the board (pixels)
 * @return				true if the given value was used
 */
public boolean setCellSize(double cellSize)
{
	this.cellSize = max(cellSize, MININUM_SIZE);
	return this.cellSize == cellSize;
}
}
