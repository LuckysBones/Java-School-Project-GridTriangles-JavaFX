
package edu.frontrange.csc240.a9.triangles;


import java.util.Random;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;


/**
 * This generates an (almost random) triangle. The first point is at a random point,
 * but the sides are drawn randomly from a split normal distribution. Triangles
 * with areas that are too small are rejected.
 * <p>
 * The method used here is more to demonstrate that it is possible to get more than
 * just random integers drawn from a rectangular distribution from the class Random.
 * In this method random floating-point values are used, as well as random numbers
 * drawn from a Gaussian (the so-called "normal") distribution.
 *
 * @author		Dr. Bruce K. Haddon, Instructor
 * @version		2.2, 2020-08-22, CSC-240 Assignment 9
 */
@SuppressWarnings("serial")
public class RandomTriangle implements Comparable<RandomTriangle>
{
/**
 * Nearness to background limit.
 */
private static final float LIMIT = 0.20F;

/**
 * Fraction of size of area to be left as a margin around the triangles. This is to
 * ensure that not too much of the triangle is outside the boundary.
 */
private static final float MARGIN = 0.1F;

/**
 * Triangles that are very small are not used, as they can appear as just dots or
 * lines on the display. This constant sets the minimum size as a ratio of the total
 * area of the panel.
 */
private static final double MINIMUM_SIZE_RATIO = 0.05;

/**
 * Source object for random numbers of all kinds.
 */
private static final Random RANDOM = new Random();

/**
 * Number of points in a triangle.
 */
private static final int TRIANGLE_POINTS = 3;

/**
 * The width of the area is weighted by this amount in determining the offset from
 * the first point to the other points. The larger this number, the larger the
 * resulting triangles. This number may be adjusted until a pleasing effect is
 * found.
 */
private static final double WEIGHT = 0.25;

/**
 * The RGB components of the background color, so as to not use any value near
 * this (see LIMIT).
 */
private double[] backGround;

/**
 * Color of the random triangle
 */
private final Color color;

/**
 * The area of this triangle: used to ensure a triangle is big enough to be seen
 * and also to order the triangle by size.
 */
private double triangleArea;

/**
 * The x-coordinate points of the triangle
 */
private final double[] xPoints;

/**
 * The y-coordinate points of the triangle
 */
private final double[] yPoints;

/**
 * Compute a triangle that an origin at a random points, and the other two
 * points at random from a normal distribution. The area of this area is computed,
 * and if it is not bigger than a given percentage of the total area, it is ignored,
 * and another triangle is computed.
 *
 * @param width			width of the canvas
 * @param height		height of the canvas
 * @param backGround	background color to be avoided
 */
public RandomTriangle(double width, double height, Color backGround)
{
	/* Get the dimensions of the total area, find a smaller space within that area
	   (to ensure the some part of the triangle is visible in the window), and
	   compute the area of that space. */
	double horizontalMargin = width * MARGIN;
	double availableWidth = width - 2.0 * horizontalMargin;
	double verticalMargin = height * MARGIN;
	double availableHeight = height - 2.0 * verticalMargin;
	double availableArea = width * height;

	analyzeBackGroundColor(backGround);

	/* Collect the points of the triangle in this array. */
	xPoints = new double[TRIANGLE_POINTS];
	yPoints = new double[TRIANGLE_POINTS];

	/* Compute a triangle that has an origin at a random point. Continue until a
	   reasonably large triangle is found. */
	do
	{
		/* Origin point. */
		xPoints[0] = RANDOM.nextDouble() * availableWidth + horizontalMargin;
		yPoints[0] = RANDOM.nextDouble() * availableHeight + verticalMargin;

		/* Get the components of the background color. */
		analyzeBackGroundColor(backGround);

		/* The other points. */
		for( int i = 1; i != TRIANGLE_POINTS; ++i )
		{
			xPoints[i] = xPoints[0] +
								RANDOM.nextGaussian() * availableWidth * WEIGHT;
			yPoints[i]=  yPoints[0] +
								RANDOM.nextGaussian() * availableHeight * WEIGHT;
		}

		/* Compute the length of the three sides, and then use Heron's formula to
		   compute the area. If the area does not exceed the given fraction of the
		   total area, it is rejected, and a new triangle found. */
		double[] sides = new double[TRIANGLE_POINTS];
		double semiPerimeter = 0.0;
		/* Accumulate the semiperimeter. */
		for( int i = 0; i != sides.length; ++i )
		{
			sides[i] = Math.hypot(xPoints[i] - xPoints[(i + 1) % TRIANGLE_POINTS],
								yPoints[i] - yPoints[(i + 1) % TRIANGLE_POINTS]);
			semiPerimeter += sides[i] / 2.0;
		}

		/* Heron's formula. */
		double product = semiPerimeter;
		for( int i = 0; i != sides.length; ++i )
			product *= semiPerimeter - sides[i];
		triangleArea = sqrt(abs(product));

		/* The call on abs (above) is included here to overcome the small rounding
		   errors that occur in double computation. In triangles that are very
		   small, or extremely narrow, the subtraction of a side from the
		   semiperimeter can lead to a negative (but very small) value--of which
		   the square root cannot be taken. It is easier to ignore the sign than
		   test for negative and replace by 0.0. */

	} while( triangleArea < availableArea * MINIMUM_SIZE_RATIO );

	/* Give the triangle a (sort of) random color. */
	color = randomColor();
}

/**
 * Returns positive if this triangle is smaller than the other triangle, negative
 * if it is larger, and zero if the same. (The use of the Comparable interface
 * will be studied in detail in Lesson 12).
 *
 * @param other			the other triangle with which to compare
 */
@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
@Override
public int compareTo(RandomTriangle other)
{
	return Double.compare(other.triangleArea, this.triangleArea);
}

/**
 * Given an graphics environment, draw this triangle on that environment.
 *
 * @param g				the graphics environment on which to draw
 */
public void draw(GraphicsContext g)
{
	/* Get the color; use to draw a filled version of the triangle. */
	g.setFill(color);
	g.fillPolygon(xPoints, yPoints, TRIANGLE_POINTS);
}

/**
 * Extract the components of the background color. The random color chosen will not
 * be permitted to use values near these components.
 *
 * @param backGround	the color to analyze
 */
private void analyzeBackGroundColor(Color backGround)
{
	this.backGround = new double[3];			// this "3" is inherent to Color
	this.backGround[0] = backGround.getRed();
	this.backGround[1] = backGround.getGreen();
	this.backGround[2] = backGround.getBlue();
}

/**
 * Generate a random color in the RBG color space, but not too near to the given
 * color that is being used as the background.
 *
 * @return	a random color in the RGB color space.
 */
private Color randomColor()
{
	final double[] rgb = new double[this.backGround.length];

	/* Select random rgb color values that are out of the range defined by the LIMIT
	   value. This guarantees that the color of the triangle will never be
	   completely the same as the background color, and can always be seen against
	   the background. */
	for( int h = 0; h != rgb.length; ++h )
	{
		boolean OK;
		do
		{
			rgb[h] = RANDOM.nextDouble();
			/* The range is LIMIT wide, but not outside the range 0.0F-1.0F. */
			double bottom_of_range = this.backGround[h] - LIMIT / 2.0F;
			double top_of_range = this.backGround[h] + LIMIT / 2.0F;
			double increment = max(0.0F, -bottom_of_range);
			double decrement = max(0.0F, top_of_range - 1.0F);
			bottom_of_range += increment - decrement;
			top_of_range += increment - decrement;
			OK = rgb[h] < bottom_of_range || rgb[h] > top_of_range;
		} while( !OK );
	}

	/* Create a color in the sRGB space for those values. */
	return Color.color(rgb[0], rgb[1], rgb[2]);
}
}
