package world;

/**
 * Map of the game. Contains an array of walls and some helpers.
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class Map {

	private double _width;
	private double _height;
	
	private Wall[] _walls;
	
	public Map(double width, double height, Wall[] walls)
	{
		this._width = width;
		this._height = height;
		this._walls = walls;
	}
	
	/**
	 * Returns the width of the map.
	 * @return
	 */
	public double getWidth()
	{
		return _width;
	}
	
	/**
	 * Returns the height of the map.
	 * @return
	 */
	public double getHeight()
	{
		return _height;
	}
	
	/**
	 * Returns the array of walls.
	 * @return
	 */
	public Wall[] getWalls()
	{
		return _walls;
	}
	
	/**
	 * Returns whether the point is in a wall or not. To be in a wall, the point has to be inside the wall and not on the border.
	 * @param point
	 * @return
	 */
	public boolean isPointInWall(Point point)
	{
		return isPointInWall(point.getX(), point.getY());
	}
	
	/**
	 * Returns whether p(x, y) is in a wall or not. To be in a wall, the point has to be inside the wall and not on the border.
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isPointInWall(double x, double y)
	{
		for(Wall w : _walls)
		{
			if(w.isPointInWall(x, y))
			{
				return true;
			}
		}
		
		return false;
	}	
	
	/**
	 * Returns whether the line (source, destination) is crossing a wall or not. Touching a border doesn't count as crossing a wall.
	 * @param source
	 * @param destination
	 * @return
	 */
	public boolean isCrossingWall(Point source, Point destination)
	{
		return isCrossingWall(source.getX(), source.getY(), destination.getX(), destination.getY());
	}
	
	/**
	 * Returns whether the line (p(x0,y0), p(x1, y1)) is crossing a wall or not. Touching a border doesn't count as crossing a wall.
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @return
	 */
	public boolean isCrossingWall(double x0, double y0, double x1, double y1)
	{
		if(isPointInWall(x0, y0) || isPointInWall(x1, y1))
		{
			return true;
		}
		
		double x1Mx0 = x1 - x0;
		double y1My0 = y1 - y0;
		
		for(Wall w : _walls)
		{
			if(w.isLineInWall(x0, y0, x1Mx0, y1My0))
			{
				return true;
			}
		}
		
		return false;
	}
}
