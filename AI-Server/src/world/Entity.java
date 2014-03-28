package world;

/**
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public abstract class Entity {

	private Point _p = new Point(-1, -1);
	
	public double getX()
	{
		return _p.getX();
	}
	
	public void setX(double x)
	{
		_p.setX(x);
	}
	
	public double getY()
	{
		return _p.getY();
	}
	
	public void setY(double y)
	{
		_p.setY(y);
	}
	
	public Point getPoint()
	{
		return _p;
	}
}
