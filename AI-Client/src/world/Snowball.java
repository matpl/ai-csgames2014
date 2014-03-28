package world;

/**
 * Represents an active snowball travelling in the map.
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class Snowball extends Entity {
	
	private Point _destination = new Point(-1,-1);
	
	private double _speed;
	
	private double _damage;
	
	private int _id;
	
	public Snowball(int id, World world)
	{
		setWorld(world);
		_id = id;
	}
	
	/**
	 * Returns the snowball id.
	 * @return
	 */
	public int getId()
	{
		return _id;
	}
	
	/**
	 * Returns the unit vector (length of 1) of the orientation of the snowball. Returns null if the snowball isn't currently visible.
	 * @return
	 */
	public Point getOrientation()
	{
		if(!isVisible())
		{
			return null;
		}
		return _orientation;
	}
	
	/**
	 * Returns the distance traveled per frame by the snowball. The ball travels faster if thrown farther (see ThrowAction for details). Returns -1 if the snowball isn't currently visible.
	 * @return
	 */
	public double getSpeed()
	{
		if(!isVisible())
		{
			return -1;
		}
		return this._speed;
	}
	
	protected void setSpeed(double speed)
	{
		this._speed = speed;
	}
	
	/**
	 * Returns the damage induced to a player if the snowball hits. The damage is related to the snowball speed (see ThrowAction for details). Returns -1 if the snowball isn't currently visible.
	 * @return
	 */
	public double getDamage()
	{
		if(!isVisible())
		{
			return -1;
		}
		return this._damage;
	}
	
	protected void setDamage(double damage)
	{
		this._damage = damage;
	}
	
	protected void setDestination(Point destination)
	{
		_destination = destination;
	}
	
	/**
	 * Returns the destination where the snowball will land. Returns null if the snowball isn't currently visible.
	 * @return
	 */
	public Point getDestination()
	{
		if(!isVisible())
		{
			return null;
		}
		return _destination;
	}

	/**
	 * Returns whether the snowball can hit p(x,y). Snowball hits if it travels close to the point (<= ThrowAction.MIN_HIT_DISTANCE). It doesn't check for collisions with players along the way, but checks for collisions with walls. Returns false if the snowball isn't currently visible.
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean canHit(double x, double y)
	{
		// TODO: IF THE BALL EXPLODES ON A WALL -> NO SPLASH. FIX THE ONLY KNOWN BUG TO MAN!!!
		return this.canHit(x, y, getX(), getY(), getDestination().getX(), getDestination().getY(), null);
	}
	
	/**
	 * Returns whether the snowball can the player. Snowball hits if it travels close to the players (<= ThrowAction.MIN_HIT_DISTANCE). It doesn't check for collisions with other players along the way, but checks for collisions with walls. Returns false if the snowball isn't currently visible.
	 * @param p
	 * @return
	 */
	public boolean canHit(Player p)
	{
		if(!p.isVisible())
		{
			return false;
		}
		return canHit(p.getX(), p.getY());
	}
}
