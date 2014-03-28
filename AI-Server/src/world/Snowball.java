package world;

/**
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class Snowball extends Entity implements Cloneable {
	
	private boolean _active = true;
	
	private Point _destination;
	
	private double _distancePerFrame;
	
	private double _damage;
	
	private int _id;
	
	private Point _orientation = new Point(-1,-1);
	
	private Point _startingPosition = new Point(-1, -1);
	
	public Snowball(int id, double startX, double startY)
	{
		_id = id;
		this.setX(startX);
		this.setY(startY);
		_startingPosition.setX(startX);
		_startingPosition.setY(startY);
	}
	
	public int getId()
	{
		return _id;
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		Snowball s = new Snowball(_id, _startingPosition.getX(), _startingPosition.getY());
		s.setX(getX());
		s.setY(getY());
		s.setDestination(new Point(_destination.getX(), _destination.getY()));
		s.setDistancePerFrame(_distancePerFrame);
		return s;
	}

	public boolean isActive()
	{
		return _active;
	}
	
	// this is only useful for the client side
	public double getDistancePerFrame()
	{
		return this._distancePerFrame;
	}
	
	public void setDistancePerFrame(double distancePerFrame)
	{
		this._distancePerFrame = distancePerFrame;
	}
	
	public double getDamage()
	{
		return this._damage;
	}
	
	public void setDamage(double damage)
	{
		this._damage = damage;
	}
	
	public void setActive(boolean active)
	{
		_active = active;
	}
	
	public void setDestination(Point destination)
	{
		_destination = destination;
		// compute the orientation vector
		double hypo = Math.sqrt(Math.pow(_destination.getX() - this.getX(), 2) + Math.pow(_destination.getY() - this.getY(), 2));
		_orientation = new Point((_destination.getX() - this.getX()) / hypo, (_destination.getY() - this.getY()) / hypo);
	}
	
	public Point getOrientation()
	{
		return _orientation;
	}
	
	public Point getDestination()
	{
		return _destination;
	}
	
	public Point getStartingPoint()
	{
		return _startingPosition;
	}
	
	public String toString()
	{
		//TODO
		return null;
	}
}
