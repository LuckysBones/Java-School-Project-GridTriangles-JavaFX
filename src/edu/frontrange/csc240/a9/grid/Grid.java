
package edu.frontrange.csc240.a9.grid;

import javafx.application.Application;

/**
 * Main program for showing a window, with a grid inside. The size of the grid
 * is determined by a count of the cells (the same in each direction) plus a(n)
 * (initial) size of the cells  The spacing between the lines (but not the number
 * of cells) changes with resizing the window.
 *
 * @author		Dr. Bruce K. Haddon, Instructor
 * @version		3.0, 2020-08-22, CSC-240 Assignment 9
 */
public class Grid
{
/**
 * The main entry point.
 * <p>
 * Execute: </p>
 * <pre>java edu.frontrange.csc240.a9.grid.Grid</pre>
 *
 * @param args			unused
 */
public static void main(String... args)
{
	/* Instantiate the view by using the Application launch method. */
	Application.launch(GridView.class, args);
}
}