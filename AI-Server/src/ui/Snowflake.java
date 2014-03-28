package ui;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

/**
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class Snowflake {

	private float _x;
	private float _y;
	private float _z;
	
	private float _trajectoryX = (float) (Math.random() * 6 - 3);
	private float _trajectoryY = (float) (Math.random() * 6 - 3);
	
	private float _degree;
	
	public Snowflake(float x, float y, float z)
	{
		this._x = x;
		this._y = y;
		this._z = z;
		this._degree = 0;
	}
	
	public float getX()
	{
		return _x;
	}
	
	public float getY()
	{
		return _y;
	}
	
	public float getZ()
	{
		return _z;
	}
	
	public void update()
	{
		_x += _trajectoryX;
		_y += _trajectoryY;
		
		_z -= 3.5;
		
		_degree = (_degree + 7) % 360;
	}
	
	private double _rotX = (Math.random() * 2) - 1;
	private double _rotY = (Math.random() * 2) - 1;
	private double _rotZ = (Math.random() * 2) - 1;
	
	public void draw(GL2 gl)
	{
		gl.glPushMatrix();
		
		gl.glTranslated(_x, _y, _z);
		gl.glRotated(_degree, _rotX, _rotY, _rotZ);
		
		gl.glBegin(GL.GL_TRIANGLES);
		
		gl.glColor3f(1, 1, 1);
		
		gl.glVertex3f(- 7, 0, 0);
		gl.glVertex3f(+ 7, 0, 0);
		gl.glVertex3f(0, 0, 7);
		
		gl.glEnd();
		
		gl.glLineWidth(2);
		gl.glBegin(GL.GL_LINE_LOOP);
		
		gl.glColor3f(0.93f, 0.93f, 0.93f);
		
		gl.glVertex3f(- 7, 0, 0);
		gl.glVertex3f(+ 7, 0, 0);
		gl.glVertex3f(0, 0, 7);
		
		gl.glEnd();
		gl.glLineWidth(1);
		
		gl.glPopMatrix();
	}
}
