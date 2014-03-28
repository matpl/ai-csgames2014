package world;

/**
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class Point implements Cloneable{
	
 	private double _x;
	private double _y;
	
	public Point(double x, double y)
	{
		_x = x;
		_y = y;
	}
	
	public double getX()
	{
		return _x;
	}
	
	public double getY()
	{
		return _y;
	}
	
	public void setX(double x)
	{
		_x = x;
	}
	
	public void setY(double y)
	{
		_y = y;
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		return new Point(_x, _y);
	}
}
