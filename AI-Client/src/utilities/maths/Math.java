package utilities.maths;

import world.Point;

/**
 * Contains various math helpers. 
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class Math {

	/**
	 * Returns the Euclidian distance between the 2 points (x0, y0) and (x1, y1).
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @return
	 */
	public static double getEuclidianDistance(double x0, double y0, double x1, double y1)
	{
		return java.lang.Math.sqrt(java.lang.Math.pow(x1 - x0, 2) + java.lang.Math.pow(y1 - y0, 2));
	}
	
	/**
	 * Returns the Euclidian distance between the 2 points p0 and p1.
	 * @param p0
	 * @param p1
	 * @return
	 */
	public static double getEuclidianDistance(Point p0, Point p1)
	{
		return java.lang.Math.sqrt(java.lang.Math.pow(p1.getX() - p0.getX(), 2) + java.lang.Math.pow(p1.getY() - p0.getY(), 2));
	}
	
	
	/**
	 * Returns the dot product of vectors u(x,y) and v(x,y).
	 * @param ux
	 * @param uy
	 * @param vx
	 * @param vy
	 * @return
	 */
	public static double getDotProduct(double ux, double uy, double vx, double vy)
	{
		return ux*vx + uy*vy;
	}
	
	/**
	 * Returns the Parametric line from point p0 to p1.
	 * @param p0
	 * @param p1
	 * @return
	 */
	public static ParametricLine getLine(Point p0, Point p1)
	{
		return getLine(p0.getX(), p0.getY(), p1.getX(), p1.getY());
	}
	
	/**
	 * Returns the Parametric line from point p(x0, y0) to p(x1, y1).
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @return
	 */
	public static ParametricLine getLine(double x0, double y0, double x1, double y1)
	{
		return new ParametricLine(x0, y0, x1, y1);
	}
}
