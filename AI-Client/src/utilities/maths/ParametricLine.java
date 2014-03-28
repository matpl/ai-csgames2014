package utilities.maths;

import world.Point;

/**
 * Represents a parametric line between two points.<br /><br />
 * 
 * A parametric line is represented by 2 points: a source (x0, y0) and a destination (x1, y1).<br />
 * It represents x and y as functions of t:<br />
 * x(t) = (x1 - x0)*t + x0<br />
 * y(t) = (y1 - y0)*t + y0<br /><br />
 * t = 0 will result in (x0, y0), while t=1 will result in (x1, y1).
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class ParametricLine {

	private Point _p0;
	private Point _p1;
	private double _x0;
	private double _y0;
	private double _x1;
	private double _y1;
	private double _mx;
	private double _bx;
	private double _my;
	private double _by;
	
	/**
	 * Creates a parametric line from p(x0, y0) to p(x1, y1).
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 */
	public ParametricLine(double x0, double y0, double x1, double y1)
	{
		this._x0 = x0;
		this._y0 = y0;
		this._x1 = x1;
		this._y1 = y1;
		
		this._p0 = new Point(x0, y0);
		this._p1 = new Point(x1, y1);
		
		_mx = x1 - x0;
		_bx = x0;
		
		_my = y1 - y0;
		_by = y0;
	}
	
	/**
	 * Returns X from the specified t value.
	 * @param t
	 * @return
	 */
	public double getX(double t)
	{
		return _mx * t + _bx;
	}
	
	/**
	 * Returns Y from the specified t value.
	 * @param t
	 * @return
	 */
	public double getY(double t)
	{
		return _my * t + _by;
	}
	
	/**
	 * Returns whether the specified x is within the bounds of the parametric line (t >= 0 and t <= 1).
	 * @param x
	 * @return
	 */
	public boolean isXInBounds(double x)
	{
		return x >= java.lang.Math.min(_x0, _x1) && x <= java.lang.Math.max(_x0, _x1);
	}
	
	/**
	 * Returns whether the specified y is within the bounds of the parametric line (t >= 0 and t <= 1).
	 * @param y
	 * @return
	 */
	public boolean isYInBounds(double y)
	{
		return y >= java.lang.Math.min(_y0, _y1) && y <= java.lang.Math.max(_y0, _y1);
	}
	
	/**
	 * Returns the mx factor (x1 - x0).
	 * @return
	 */
	public double getMx()
	{
		return _mx;
	}
	
	/**
	 * Returns the my factor (y1 - y0)
	 * @return
	 */
	public double getMy()
	{
		return _my;
	}
	
	/**
	 * Returns the bx factor (x0)
	 * @return
	 */
	public double getBx()
	{
		return _bx;
	}
	
	/**
	 * Returns the by factor (y0)
	 * @return
	 */
	public double getBy()
	{
		return _by;
	}
	
	/**
	 * Returns the source point (x0, y0).
	 * @return
	 */
	public Point getP0()
	{
		return this._p0;
	}
	
	/**
	 * Returns the destination point (x1, y1).
	 * @return
	 */
	public Point getP1()
	{
		return this._p1;
	}
	
	/**
	 * Returns x0.
	 * @return
	 */
	public double getX0()
	{
		return _x0;
	}
	
	/**
	 * Returns y0.
	 * @return
	 */
	public double getY0()
	{
		return _y0;
	}
	
	/**
	 * Returns x1.
	 * @return
	 */
	public double getX1()
	{
		return _x1;
	}
	
	/**
	 * Returns y1.
	 * @return
	 */
	public double getY1()
	{
		return _y1;
	}
	
	/**
	 * Returns the intersection point of the current line and the specified line. Returns null if they don't intersect.
	 * @param line
	 * @return
	 */
	public Point intersect(ParametricLine line)
	{
		double denom = (_mx*line.getMy() - line.getMx()*_my);
		if(denom != 0)
		{
			double t = (_mx*(_y0-line.getY0()) - (_x0-line.getX0())*_my)/ denom;
			
			return new Point(line.getMx()*t + line.getBx(), line.getMy()*t + line.getBy());
		}
		return null;
	}
}
