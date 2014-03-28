package world;

import java.util.PriorityQueue;

/**
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class Wall {

	private Point[] _vertices;
	
	// parametric stuff
	private double[] _x1ParamList;
	private double[] _x2Mx1ParamList;
	private double[] _y1ParamList;
	private double[] _y2My1ParamList;
	
	private double _minX = Double.MAX_VALUE;
	private double _minY = Double.MAX_VALUE;
	private double _maxX = 0;
	private double _maxY = 0;
	
	public Wall(Point[] vertices)
	{
		_vertices = vertices;
		
		_x1ParamList = new double[_vertices.length];
		_x2Mx1ParamList = new double[_vertices.length];
		_y1ParamList = new double[_vertices.length];
		_y2My1ParamList = new double[_vertices.length];
		
		Point p1;
		Point p2;
		
		for(int i = 0; i < vertices.length; i++)
		{
			if(_vertices[i].getX() < _minX)
			{
				_minX = _vertices[i].getX();
			}
			if(_vertices[i].getY() < _minY)
			{
				_minY = _vertices[i].getY();
			}
			if(_vertices[i].getX() > _maxX)
			{
				_maxX = _vertices[i].getX();
			}
			if(_vertices[i].getY() > _maxY)
			{
				_maxY = _vertices[i].getY();
			}
			
			p1 = new Point(_vertices[i].getX(), _vertices[i].getY());
			p2 = new Point(_vertices[(i+1)%vertices.length].getX(), _vertices[(i+1)%vertices.length].getY());
			if(p1.getX() > p2.getX())
			{
				// swap two variables magically using doubles converted to longs and XOR operator
				p1.setX(Double.longBitsToDouble(Double.doubleToRawLongBits(p1.getX()) ^ Double.doubleToRawLongBits(p2.getX())));
				p1.setY(Double.longBitsToDouble(Double.doubleToRawLongBits(p1.getY()) ^ Double.doubleToRawLongBits(p2.getY())));
				p2.setX(Double.longBitsToDouble(Double.doubleToRawLongBits(p1.getX()) ^ Double.doubleToRawLongBits(p2.getX())));
				p2.setY(Double.longBitsToDouble(Double.doubleToRawLongBits(p1.getY()) ^ Double.doubleToRawLongBits(p2.getY())));
				p1.setX(Double.longBitsToDouble(Double.doubleToRawLongBits(p1.getX()) ^ Double.doubleToRawLongBits(p2.getX())));
				p1.setY(Double.longBitsToDouble(Double.doubleToRawLongBits(p1.getY()) ^ Double.doubleToRawLongBits(p2.getY())));
			}
			
			// PARAMETRIC STUFF
			// 0 IS THE LOWER LIMIT, AND 1 IS THE UPPER LIMIT
			_x1ParamList[i] = p1.getX();
			_x2Mx1ParamList[i] = p2.getX() - p1.getX();
			_y1ParamList[i] = p1.getY();
			_y2My1ParamList[i] = p2.getY() - p1.getY();
		}
	}
	
	/**
	 * Whether the point is in the wall or not
	 * @param point
	 * @return
	 */
	public boolean isPointInWall(Point point)
	{
		return isPointInWall(point.getX(), point.getY());
	}
	
	/**
	 * Whether the point is in the wall or not
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isPointInWall(double x, double y)
	{
		if(x <= _minX || x >= _maxX || y <= _minY || y >= _maxY)
		{
			return false;
		}
		
		int i, j;
		boolean c = false;
	    for (i = 0, j = _vertices.length-1; i < _vertices.length; j = i++) {
	        if ( ((_vertices[i].getY()>y) != (_vertices[j].getY()>y)) &&
	        	    (x < (_vertices[j].getX()-_vertices[i].getX()) * (y-_vertices[i].getY()) / (_vertices[j].getY()-_vertices[i].getY()) + _vertices[i].getX()) )
	        {
	            c = !c;
	        }
	    }
	    
	    if(c)
	    {
	    	double t;
	    	//check if the point is ON the edges
	    	for(i = 0; i < _x1ParamList.length; i++)
	    	{
	    		//t = ((_x2Mx1ParamList[i])*(_y1ParamList[i]-y1) - (_x1ParamList[i]-x1)*_y2My1ParamList[i])/(_x2Mx1ParamList[i]*y2My1 - x2Mx1*_y2My1ParamList[i]);
	    		
	    		if(_x2Mx1ParamList[i] == 0)
	    		{
	    			t = (y - _y1ParamList[i]) / _y2My1ParamList[i];
	    			if(x == _x1ParamList[i] && t >=0 && t <= 1)
	    			{
	    				return false;
	    			}
	    		}
	    		else if(_y2My1ParamList[i] == 0)
	    		{
	    			t = (x - _x1ParamList[i]) / _x2Mx1ParamList[i];
	    			if(y == _y1ParamList[i] && t >=0 && t <= 1)
	    			{
	    				return false;
	    			}
	    		}
	    		else
	    		{
	    			// compute 2 t
	    			t = ((int)((x - _x1ParamList[i]) / _x2Mx1ParamList[i] * 10000.0))/10000.0;
	    			if(t == ((int)((y - _y1ParamList[i]) / _y2My1ParamList[i] * 10000.0))/10000.0)
	    			{
	    				return false;
	    			}
	    		}
	    	}
	    }
	    
	    return c;
	}
	
	protected boolean isLineInWall(double x1, double y1, double x2Mx1, double y2My1)
	{
		Double t;			
		PriorityQueue<Double> queue = new PriorityQueue<Double>();
			
		for(int i = 0; i < _vertices.length; i++)
		{
			// parametric nightmare
			t = ((int)((((_x2Mx1ParamList[i])*(_y1ParamList[i]-y1) - (_x1ParamList[i]-x1)*_y2My1ParamList[i])/(_x2Mx1ParamList[i]*y2My1 - x2Mx1*_y2My1ParamList[i])) * 10000.0))/10000.0;
			
			if(t >= 0 && t <= 1)
			{
				queue.add(t);
			}
		}
		
		if(queue.size() > 0)
		{
			double middleT;
			double lastT = queue.poll();
			double xt;
			double yt;
			
			while((t = queue.poll()) != null)
			{
				// average of t and lastT
				middleT = (t + lastT) / 2.0;
				
				xt = x2Mx1 * middleT + x1;
				yt = y2My1 * middleT + y1;
				
				if(isPointInWall(xt, yt))
				{
					return true;
				}
				
				lastT = t;
			}
		}
		return false;
	}
	
	public Point[] getVertices()
	{
		return _vertices;
	}
}
