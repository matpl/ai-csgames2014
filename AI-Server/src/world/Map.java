package world;

import utilities.graph.PathList;

/**
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class Map implements Cloneable {

	private double _width;
	private double _height;
	
	private Wall[] _walls;
	
	private PathList[] _pathList;
	
	public Map(double width, double height, Wall[] walls)
	{
		this._width = width;
		this._height = height;
		this._walls = walls;
	}
	
	public double getWidth()
	{
		return _width;
	}
	
	public double getHeight()
	{
		return _height;
	}
	
	public Wall[] getWalls()
	{
		return _walls;
	}
	
	public void setPathList(PathList[] pathList)
	{
		_pathList = pathList;
	}
	
	public PathList[] getPathList()
	{
		return _pathList;
	}
	
	/**
	 * Whether the point is in a wall or not
	 * @param point
	 * @return
	 */
	public boolean isPointInWall(Point point)
	{
		return isPointInWall(point.getX(), point.getY());
	}
	
	/**
	 * Whether the point (x,y) is in a wall or not
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
	
	/*
	public boolean isPointInWall2(Point point)
	{
		for(Wall w : _walls)
		{
			if(w.isPointInWall2(point))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isCrossingWall2(Point source, Point destination)
	{
		Double m;
		double b;
		
		Point lowerLimit;
		Point upperLimit;
		
		double sx = source.getX();
		double sy = source.getY();
		double dx = destination.getX();
		double dy = destination.getY();
		
		if(sx > dx)
		{
			if(sy > dy)
			{
				lowerLimit = new Point(dx, dy);
				upperLimit = new Point(sx, sy);
			}
			else
			{
				lowerLimit = new Point(dx, sy);
				upperLimit = new Point(sx, dy);
			}
		}
		else
		{
			if(sy > dy)
			{
				lowerLimit = new Point(sx, dy);
				upperLimit = new Point(dx, sy);
			}
			else
			{
				lowerLimit = new Point(sx, sy);
				upperLimit = new Point(dx, dy);
			}
		}
		
		if(sx == dx)
		{
			m = null;
			b = sx;
		}
		else
		{
			m = (sy - dy) / (sx - dx);
			b = sy - m * sx;
		}
		
		Point middle = new Point((sx + dx)/2.0, (sy + dy)/2.0);
		
		for(Wall w : _walls)
		{
			if(w.isLineInWall2(m, b, lowerLimit, upperLimit, middle))
			{
				return true;
			}
		}
		
		return false;
	}
	*/
	
	/**
	 * Whether the line is crossing a wall or not
	 * @param source
	 * @param destination
	 * @return
	 */
	public boolean isCrossingWall(Point source, Point destination)
	{
		return isCrossingWall(source.getX(), source.getY(), destination.getX(), destination.getY());
	}
	
	/**
	 * Whether the line is crossing a wall or not
	 * line -> p0 to p1
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
	
	public Object clone() throws CloneNotSupportedException
	{
		// if map is static, no need to clone
		//TODO
		return new Map(_width, _height, _walls);
	}
	
	public String toString()
	{
		String str;
		
		str = _width + " " + _height;
		
		str += " " + _walls.length;
		
		for(int i = 0; i < _walls.length; i++)
		{			
			Point[] vertices = _walls[i].getVertices();
			
			str += " " + vertices.length;
			
			for(Point p : vertices)
			{
				str += " " + p.getX() + "," + p.getY();
			}
		}
		
		return str;
	}
}
