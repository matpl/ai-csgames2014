package utilities.graph;

import java.util.ArrayList;
import java.util.List;

import world.Point;

/**
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class ParametricPointList {

	protected List<Point> _pointList;
	protected List<Double> _xMList;
	protected List<Double> _yMList;
	protected List<Double> _xBList;
	protected List<Double> _yBList;
	protected List<Double> _cumulativeTotalsList;
	
	protected double _totalDistance = 0.0;
	
	public List<Point> getPointList()
	{
		return _pointList;
	}
	
	public double getTotalDistance()
	{
		return _totalDistance;
	}
	
	public ParametricPointList(List<Point> pointList)
	{
		_pointList = pointList;
		_xMList = new ArrayList<Double>();
		_yMList = new ArrayList<Double>();
		_xBList = new ArrayList<Double>();
		_yBList = new ArrayList<Double>();
		_cumulativeTotalsList = new ArrayList<Double>();
		
		for(int i = 0; i < _pointList.size() - 1; i++)
		{
			double dist = Math.sqrt(Math.pow(_pointList.get(i).getX() - _pointList.get(i+1).getX(), 2) + Math.pow(_pointList.get(i).getY() - _pointList.get(i+1).getY(), 2));
			_cumulativeTotalsList.add(_totalDistance);
			_totalDistance += dist;
		}
		_cumulativeTotalsList.add(_totalDistance);
		
		// using the parametric equation with t = 0 -> first point
		// using the parametric equation with t = 1 -> last point
		
		// x(t) = (x2-x1)t+x1
		// y(t) = (y2-y1)t+y1
		
		for(int i = 0; i < _pointList.size() - 1; i++)
		{
			_xMList.add(_pointList.get(i+1).getX() - _pointList.get(i).getX());
			_yMList.add(_pointList.get(i+1).getY() - _pointList.get(i).getY());
			
			_xBList.add(_pointList.get(i).getX());
			_yBList.add(_pointList.get(i).getY());
		}
	}
	
	// t has to be between 0 and 1
	// extend this to be able to give a t < 0 ant t > 1 for when going from a source to a destination that are not initially in the graph
	public Point getPositionAt(double t)
	{
		//Point p;
		double distance = t*_totalDistance;
		
		for(int i = 0; i < _cumulativeTotalsList.size(); i++)
		{
			if(distance >= _cumulativeTotalsList.get(i) && 
					distance <= _cumulativeTotalsList.get(i+1))
			{
				double realT = (distance - _cumulativeTotalsList.get(i)) / (_cumulativeTotalsList.get(i+1) - _cumulativeTotalsList.get(i));
				return new Point(
						_xMList.get(i)*realT + _xBList.get(i),
						_yMList.get(i)*realT + _yBList.get(i));
			}
		}
		
		if(t == 1)
		{
			return _pointList.get(_pointList.size()-1);
		}
		
		return null;
	}
	
}
