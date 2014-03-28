package world;

/**
 * Point containing x and y coordinates.
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class Point {
	
 	private double _x;
	private double _y;
	
	/**
	 * Creates a new point with the specified x and y coordinates.
	 * @param x
	 * @param y
	 */
	public Point(double x, double y)
	{
		_x = x;
		_y = y;
	}
	
	/**
	 * Returns X
	 * @return
	 */
	public double getX()
	{
		return _x;
	}
	
	/**
	 * Returns Y
	 * @return
	 */
	public double getY()
	{
		return _y;
	}
	
	/**
	 * Sets X
	 * @param x
	 */
	public void setX(double x)
	{
		_x = x;
	}
	
	/**
	 * Sets Y
	 * @param y
	 */
	public void setY(double y)
	{
		_y = y;
	}
}
