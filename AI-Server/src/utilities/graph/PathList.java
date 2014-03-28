package utilities.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import world.Point;

/**
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class PathList {

	private List<Point> _destinations;
	
	private Point _source;
	
	private HashMap<Point, List<Point>> _paths;
	private HashMap<Point, ParametricPointList> _parametricPaths;
	
	private HashMap<Point, Double> _distances;
	
	public PathList(Point source)
	{
		_source = source;
		_destinations = new ArrayList<Point>();
		_distances = new HashMap<Point, Double>();
		_paths = new HashMap<Point, List<Point>>();
		_parametricPaths = new HashMap<Point, ParametricPointList>();
	}
	
	
	public Point getSource()
	{
		return _source;
	}
	
	public List<Point> getDestinations()
	{
		return _destinations;
	}
	
	public void setPathTo(Point point, List<Point> path)
	{
		_destinations.add(point);
		double distance = 0.0;
		for(int i = 0; i < path.size() - 1; i++)
		{
			distance += Math.sqrt(Math.pow(path.get(i).getX() - path.get(i+1).getX(), 2) + Math.pow(path.get(i).getY() - path.get(i+1).getY(), 2));
		}
		
		_distances.put(point, distance);
		_paths.put(point, path);
		_parametricPaths.put(point, new ParametricPointList(path));
	}
	
	public List<Point> getPathTo(Point point)
	{
		return _paths.get(point);
	}
	
	public ParametricPointList getParametricPathTo(Point point)
	{
		return _parametricPaths.get(point);
	}
	
	public double getDistanceTo(Point point)
	{
		if(point == _source)
		{
			return 0.0;
		}
		return _distances.get(point);
	}
	
}
