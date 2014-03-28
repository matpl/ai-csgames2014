package utilities.maths;

import world.Point;

public class Math {

	//source: http://geomalgorithms.com/a02-_lines.html
	public static double getDistance(double px, double py, double x0, double y0, double x1, double y1)
	{
		double vx = x1 - x0;
		double vy = y1 - y0;
		
		double wx = px - x0;
		double wy = py - y0;

	    double c1 = getDotProduct(wx, wy, vx, vy);
	    if ( c1 <= 0 )
	    {
	    	return getEuclidianDistance(px, py, x0, y0);
	    }

	    double c2 = getDotProduct(vx, vy, vx, vy);
	    if ( c2 <= c1 )
	    {
	    	return getEuclidianDistance(px, py, x1, y1);
	    }

	    double b = c1 / c2;
	    
	    double pbx = x0 + b * vx;
	    double pby = y0 + b * vy;
	    return getEuclidianDistance(px, py, pbx, pby);
	}
		
	public static double getEuclidianDistance(double x0, double y0, double x1, double y1)
	{
		return java.lang.Math.sqrt(java.lang.Math.pow(x1 - x0, 2) + java.lang.Math.pow(y1 - y0, 2));
	}
	
	public static double getEuclidianDistance(Point pt1, Point pt2)
	{
		return getEuclidianDistance(pt1.getX(), pt1.getY(), pt2.getX(), pt2.getY());
	}
	
	public static double getDotProduct(double ux, double uy, double vx, double vy)
	{
		return ux*vx + uy*vy;
	}
}
