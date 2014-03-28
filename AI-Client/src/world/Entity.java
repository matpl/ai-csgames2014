package world;

import world.action.ThrowAction;

/**
 * Abstract Entity class for objects in a world.
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public abstract class Entity {

	private Point _p = new Point(-1, -1);
	
	protected Point _orientation = new Point(-1,-1);
	protected World _world;
	
	private boolean _visible = false;
	
	protected void setWorld(World world)
	{
		this._world = world;
	}
	
	/**
	 * Returns the X coordinate of the entity.
	 * @return
	 */
	public double getX()
	{
		if(!_visible)
		{
			return -1;
		}
		return _p.getX();
	}
	
	protected void setX(double x)
	{
		_p.setX(x);
	}
	
	/**
	 * Returns the Y coordinate of the entity.
	 * @return
	 */
	public double getY()
	{
		if(!_visible)
		{
			return -1;
		}
		return _p.getY();
	}
	
	protected void setY(double y)
	{
		_p.setY(y);
	}
	
	/**
	 * Returns the position of the entity.
	 * @return
	 */
	public Point getPoint()
	{
		if(!_visible)
		{
			return null;
		}
		return _p;
	}
	
	/**
	 * Returns whether the entity is visible or not.
	 * @return
	 */
	public boolean isVisible()
	{
		return _visible;
	}
	
	protected void setVisible(boolean visible)
	{
		_visible = visible;
	}
	
	protected boolean canHit(double x, double y, double startX, double startY, double destX, double destY, Point orientation)
	{
		if(!isVisible())
		{
			return false;
		}
		
		if(orientation == null)
		{
			orientation = _orientation;
		}
		
		boolean canHit = false;
		
		double ux = x - startX;
		double uy = y - startY;
		
		double dotP = utilities.maths.Math.getDotProduct(orientation.getX(), orientation.getY(), ux, uy) / Math.pow(utilities.maths.Math.getEuclidianDistance(0, 0, orientation.getX(), orientation.getY()), 2);
		
		if(dotP >= 0)
		{
			double px = startX + dotP*orientation.getX();
			double py = startY + dotP*orientation.getY();
			
			// px py should be on the line!!
			if(px >= Math.min(startX, destX) && px <= Math.max(startX, destX) &&
			   py >= Math.min(startY, destY) && py <= Math.max(startY, destY) &&
			   !_world.getMap().isCrossingWall(startX, startY, px, py) &&
			   utilities.maths.Math.getEuclidianDistance(px, py, x, y) <= ThrowAction.MIN_HIT_DISTANCE)
			{
				canHit = true;
			}
			else if(utilities.maths.Math.getEuclidianDistance(destX, destY, x, y) <= ThrowAction.MIN_HIT_DISTANCE &&
					!_world.getMap().isCrossingWall(getX(), getY(), destX, destY) &&
					!_world.getMap().isCrossingWall(x, y, destX, destY))
			{
				canHit = true;
			}
		}
		
		return canHit;
	}
}
