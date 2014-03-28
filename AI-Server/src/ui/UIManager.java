package ui;

import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_LEQUAL;
import static javax.media.opengl.GL.GL_NICEST;
import static javax.media.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.media.opengl.glu.GLUtessellator;
import javax.media.opengl.glu.GLUtessellatorCallback;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import world.Map;
import world.Player;
import world.PlayerState;
import world.Point;
import world.Team;
import world.Wall;
import world.World;

import com.jogamp.opengl.util.FPSAnimator;


/**
 * JOGL UI
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class UIManager extends Thread{

	private static float SNOWFLAKE_DENSITY = 1f;
	
	private GLCanvas _canvas;
	private SimpleScene _scene;
	
	private World _globalWorld;
	
	private int _slices = 6;
	
	private JLabel[] _labels;
	
	private boolean _teamNamesSet = false;
	private JPanel _namePanel;
	
	public UIManager(World globalWorld)
	{
		this._globalWorld = globalWorld;
	}
	
	public void run()
	{
		// jogl test
		GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        caps.setSampleBuffers(true);
        caps.setNumSamples(100);
        _canvas = new GLCanvas(caps);
        _scene = new SimpleScene();
        
        _canvas.addGLEventListener(_scene);
        
        JFrame frame = new JFrame("Snowball fight");
        
        final JLayeredPane pane = new JLayeredPane();
        ComponentAdapter componentAdapter = new ComponentAdapter()
        {
        	public void componentResized(ComponentEvent e)
        	{
        		_canvas.setSize((int) pane.getSize().getWidth(), (int) pane.getSize().getHeight());
        		
        		_namePanel.setBounds(0, (int) (pane.getSize().getHeight() - pane.getSize().getHeight()/10.0), (int) (pane.getSize().getWidth()/5.0), (int) (pane.getSize().getHeight()/10.0));
        	}
        };
        pane.addComponentListener(componentAdapter);
        
        pane.setOpaque(true);
        pane.setBackground(new Color(0.757f, 0.89f, 1.0f));
        
        _canvas.setBounds(0, 0, 1100, 900);
        pane.setPreferredSize(new Dimension(1100, 900));
        pane.add(_canvas, 1);
        
        
        _namePanel = new JPanel();
        _namePanel.setOpaque(true);
        _namePanel.setBackground(new Color(0.757f, 0.89f, 1.0f));
        _namePanel.setBounds(0,900 - 90,220, 90);
        _namePanel.setVisible(false);
        pane.add(_namePanel, 0);
        
        frame.add(pane);
        frame.pack();
        
        KeyAdapter keyAdapter;
        keyAdapter = new KeyAdapter()
        {
        	public void keyPressed(KeyEvent e)
        	{
        		if(e.getKeyCode() == KeyEvent.VK_CONTROL)
        		{
        			_namePanel.setVisible(true);
        		}
        	}
        	
        	public void keyReleased(KeyEvent e)
        	{
        		if(e.getKeyCode() == KeyEvent.VK_CONTROL)
        		{
        			_namePanel.setVisible(false);
        		}
        	}
        };
        
        MouseAdapter adapter;
        adapter = new MouseAdapter()
        {
        	private int x = Integer.MAX_VALUE;
        	private int y = Integer.MAX_VALUE;
        	
        	public void mousePressed(MouseEvent e)
        	{
        		x = e.getX();
        		y = e.getY();
        	}
        	
        	public void mouseWheelMoved(MouseWheelEvent e)
        	{
        		float zoom = (float)(_scene.getA() + e.getUnitsToScroll()*20);
        		if(zoom > 0)
        		{
        			_scene.setA(zoom);
        		}
        	}
        	
        	public void mouseDragged(MouseEvent e)
        	{
        		int newX = e.getX();
        		int newY = e.getY();
        		
        		if(x == Integer.MAX_VALUE)
        		{
        			x = newX - 1;
        		}
        		
        		if(y == Integer.MAX_VALUE)
        		{
        			y = newY - 1;
        		}
        		
        		if(e.getModifiers() == InputEvent.BUTTON1_MASK)
        		{
        			_scene.setT((_scene.getT() - (newX - x)/2500.0f) % 1);
        		}
        		else if(e.getModifiers() == InputEvent.BUTTON2_MASK || e.getModifiers() == InputEvent.BUTTON3_MASK)
        		{
    				// normal vector to the plane
        			float vx = _scene.eyeX - _scene.centerX;
        			float vy = _scene.eyeY - _scene.centerY;
        			float vz = _scene.eyeZ - _scene.centerZ;
        			
        			// vx * x + vy * y + vz * z = k
        			float k = vx * _scene.centerX + vy * _scene.centerY + vz * _scene.centerZ;
        			
        			// vx * x + vy * y + vz * _scene.centerZ = k
        			
        			
        			// get another point anywhere on the plane at the same Z
        			// todo: BAD THIS IS BAD BAD BAD
        			double otherX = _scene.centerX + 100.0f;
        			double otherY = (k - vz * _scene.centerZ - vx * otherX) / vy;
        			
        			// this dist = 1 in parametric
        			double totalDist = utilities.maths.Math.getEuclidianDistance(otherX, otherY, _scene.centerX, _scene.centerY);
        			
        			
        			double t = (newX - x)*(_scene.getA()/925.0) / totalDist;
        			
        			
        			if(vy < 0)
        			{
        				t *= -1.0;
        			}
        			
        			
        			double offsetX = (otherX - _scene.centerX) * t + _scene.centerX;
        			double offsetY = (otherY - _scene.centerY) * t + _scene.centerY;
        			
        			_scene.setCenterX((float)offsetX);
        			_scene.setCenterY((float)offsetY);
        			
        			_scene.eyeZ += (newY - y) * (_scene.getA()/925.0f);
        			_scene.setCenterZ(_scene.centerZ + (newY - y) * (_scene.getA()/925.0f));
        		}
        		
        		x = e.getX();
        		y = e.getY();
        	}
        };
        
        _canvas.addMouseListener(adapter);
        _canvas.addMouseWheelListener(adapter);
        _canvas.addMouseMotionListener(adapter);
        _canvas.addKeyListener(keyAdapter);
        frame.setVisible(true);
        
        FPSAnimator animator = new FPSAnimator(_canvas, 60);
        animator.start();
        
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });
	}
	
	public class SimpleScene implements GLEventListener {
		
		private World _world;
		
		private GLU glu;  // for the GL Utility
		GLAutoDrawable _drawable = null;
		   
		@Override
		public void display(GLAutoDrawable drawable) {
			// TODO Auto-generated method stub
			update();
		    render(drawable);
		}
		
		public void update()
		{
			try
			{
				//TODO: clone should probably not be there, but in the gameloop. this should only like FETCH the latest saved clone
				// to prevent bugs since not everything may be synchronized
				_world = (World) _globalWorld.clone();
				
				if(!_teamNamesSet)
				{
					_namePanel.setLayout(new GridLayout(_world.getTeams().length, 1));
					_labels = new JLabel[_world.getTeams().length];
					for(int i = 0; i < _world.getTeams().length; i++)
					{
						float teamRed = 0.0f;
					    float teamGreen = 0.0f;
					    float teamBlue = 0.0f;
					    switch(i)
					    {
						    case 0:
						    	teamRed = 1.0f;
						    	teamGreen = 0.0f;
						    	teamBlue = 0.0f;
						    	break;
						    case 1:
						    	teamRed = 0.0f;
						    	teamGreen = 1.0f;
						    	teamBlue = 0.0f;
						    	break;
						    case 2:
						    	teamRed = 0.0f;
						    	teamGreen = 0.0f;
						    	teamBlue = 1.0f;
						    	break;
						    case 3:
						    	teamRed = 0.0f;
						    	teamGreen = 1.0f;
						    	teamBlue = 1.0f;
						    	break;
					    }
						
						_labels[i] = new JLabel("    ");
						_labels[i].setForeground(new Color(teamRed, teamGreen, teamBlue));
						_namePanel.add(_labels[i]);
					}
					
					_teamNamesSet = true;
				}
				
				for(int i = 0; i < _world.getTeams().length; i++)
				{
					if(!_labels[i].getText().equals("    " + _world.getTeams()[i].getName()))
					{
						_labels[i].setText("    " + _world.getTeams()[i].getName());
					}
				}
				
				for(int i = _snowflakes.size() - 1; i >= 0; i--)
				{
					_snowflakes.get(i).update();
					if(_snowflakes.get(i).getZ() < -10)
					{
						_snowflakes.remove(i);
					}
				}
				
				if(_world.isAdvanced())
				{
					
					if(snowflakeFrame % 16 == 0)
					{
						snowflakeFrame = 0;
						// 100 new flakes?
						for(int i = 0; i < 20 * SNOWFLAKE_DENSITY; i++)
						{
							_snowflakes.add(new Snowflake((float)(Math.random()*_world.getMap().getWidth()*1.3 - _world.getMap().getWidth()*0.15), (float)(Math.random()*_world.getMap().getHeight()*1.3 - _world.getMap().getHeight()*0.15), 500.0f));
						}
					}
					snowflakeFrame++;
				}
				
				
				if(movingSince == null)
				{
					movingSince = new int[_world.getTeams().length][_world.getTeams()[0].getPlayers().length];
					timeSinceWinner = new int[_world.getTeams().length];
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			
			theta += 0.01;
		    s = Math.sin(theta);
		    c = Math.cos(theta);	
		}
		
		private double _wallHeight = 50.0;
		
		private double theta = 0;
		private double s = 0;
		private double c = 0;
		
		GLUquadric quad;
		
		private int [][] movingSince;
		private int [] timeSinceWinner;
		
		private final int JUMP_FRAMES = 30;
		
		private ArrayList<Snowflake> _snowflakes = new ArrayList<Snowflake>();
		
		private int snowflakeFrame = 0;
		
		private void render(GLAutoDrawable drawable) {
			GL2 gl = drawable.getGL().getGL2();
			
			if(_requestCameraUpdate)
			{
				gl.glMatrixMode(GL_PROJECTION);  // choose projection matrix
			    gl.glLoadIdentity();             // reset projection matrix
			    glu.gluPerspective(45.0, _aspect, 0.1, 10000.0); // fovy, aspect, zNear, zFar
			    lookAt(glu);
			      
			    // Enable the model-view transform
			    gl.glMatrixMode(GL_MODELVIEW);
			    gl.glLoadIdentity(); // reset
				
				_requestCameraUpdate = false;
			}
		    
			gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color and depth 
		    
			gl.glLoadIdentity();
			
			if(_world != null)
			{
				gl.glPushMatrix();
				
				Map map = _world.getMap();
				
				gl.glTranslatef((float)(0 - map.getWidth()/2.0), (float)(0 - map.getHeight()/2), -2000f);
		        
		        gl.glEnable(GL2.GL_COLOR_MATERIAL);
		        
				gl.glBegin(GL2.GL_POLYGON);
				
				gl.glColor3f(1, 1, 1);
				
				gl.glVertex3f((float)0, (float)0, -10);
				gl.glVertex3f((float)map.getWidth(), 0, -10);
				gl.glVertex3f((float)map.getWidth(), (float)map.getHeight(), -10);
				gl.glVertex3f((float)0, (float)map.getHeight(), -10);
				
				gl.glEnd();
				
				gl.glLineWidth(0.5f);
				gl.glBegin(GL.GL_LINE_LOOP);
				
				gl.glColor3f(0.75f, 0.75f, 0.75f);
				
				gl.glVertex3f((float)0, (float)0, -10);
				gl.glVertex3f((float)map.getWidth(), 0, -10);
				gl.glVertex3f((float)map.getWidth(), (float)map.getHeight(), -10);
				gl.glVertex3f((float)0, (float)map.getHeight(), -10);
				
				gl.glEnd();
				gl.glLineWidth(1);
				
				if(_world.isAdvanced())
				{
					for(Snowflake snowflake : _snowflakes)
					{
						snowflake.draw(gl);
					}
				}
				
				Wall[] walls = map.getWalls();
				
				GLUtessellator tess = GLU.gluNewTess();
				
				TessellatorCallBack tessCallback = new TessellatorCallBack(gl,glu);

				GLU.gluTessCallback(tess, javax.media.opengl.glu.GLU.GLU_TESS_VERTEX,   tessCallback);
				GLU.gluTessCallback(tess, javax.media.opengl.glu.GLU.GLU_TESS_BEGIN,    tessCallback);
				GLU.gluTessCallback(tess, javax.media.opengl.glu.GLU.GLU_TESS_END,      tessCallback);
				GLU.gluTessCallback(tess, javax.media.opengl.glu.GLU.GLU_TESS_ERROR,    tessCallback);
				GLU.gluTessCallback(tess, javax.media.opengl.glu.GLU.GLU_TESS_COMBINE,  tessCallback);
		        
				gl.glColor3f(0.96f, 0.96f, 0.96f);
				for(Wall w : walls)
				{
					Point [] points = w.getVertices();
					
					//gl.glBegin(GL2.GL_POLYGON);
					GLU.gluTessBeginPolygon(tess, null);
					GLU.gluTessBeginContour(tess);
					for(Point p : points)
					{
						double [] arr = new double[]{p.getX(), p.getY(), _wallHeight};
						GLU.gluTessVertex(tess, arr, 0, arr);
					}

					GLU.gluTessEndContour(tess);
					GLU.gluTessEndPolygon(tess);
					
					for(int i = 0; i < points.length; i++)
					{	
						int nextI = (i + 1) % points.length;
						gl.glBegin(GL2.GL_QUADS);
						
						gl.glVertex3d(points[i].getX(), points[i].getY(), 0);
						gl.glVertex3d(points[i].getX(), points[i].getY(), _wallHeight);
						gl.glVertex3d(points[nextI].getX(), points[nextI].getY(), _wallHeight);
						gl.glVertex3d(points[nextI].getX(), points[nextI].getY(), 0);
						
						gl.glEnd();
					}  
					
				}
				GLU.gluDeleteTess(tess);
				
				gl.glColor3f(0.75f, 0.75f, 0.75f);
				gl.glLineWidth(0.5f);
				for(Wall w : walls)
				{
					Point [] points = w.getVertices();
					
					for(int i = 0; i < points.length; i++)
					{
						int nextI = (i + 1) % points.length;
						
						
						gl.glBegin(GL.GL_LINE_LOOP);
						
						
						gl.glVertex3d(points[i].getX(), points[i].getY(), 0);
						gl.glVertex3d(points[i].getX(), points[i].getY(), _wallHeight);
						gl.glVertex3d(points[nextI].getX(), points[nextI].getY(), _wallHeight);
						gl.glVertex3d(points[nextI].getX(), points[nextI].getY(), 0);
						
						gl.glEnd();
					}
				}
				gl.glLineWidth(1);
				
				for(int j = 0; j < _world.getTeams().length; j++)
		    	{
					if(_world.getWinningTeams() != null && _world.getWinningTeams().contains(_world.getTeams()[j]))
					{
						timeSinceWinner[j] = (timeSinceWinner[j] + 1) % JUMP_FRAMES;
					}
					
				    Player[] players = _world.getTeams()[j].getPlayers();
				    float teamRed = 0.0f;
				    float teamGreen = 0.0f;
				    float teamBlue = 0.0f;
				    switch(j)
				    {
					    case 0:
					    	teamRed = 1.0f;
					    	teamGreen = 0.0f;
					    	teamBlue = 0.0f;
					    	break;
					    case 1:
					    	teamRed = 0.0f;
					    	teamGreen = 1.0f;
					    	teamBlue = 0.0f;
					    	break;
					    case 2:
					    	teamRed = 0.0f;
					    	teamGreen = 0.0f;
					    	teamBlue = 1.0f;
					    	break;
					    case 3:
					    	teamRed = 0.0f;
					    	teamGreen = 1.0f;
					    	teamBlue = 1.0f;
					    	break;
				    }
				    
				    for(int i = 0; i < players.length; i++)
				    {
				    	if(players[i].getPlayerState().getStateType() == PlayerState.StateType.Moving)
				    	{
				    		movingSince[j][i]++;
				    	}
				    	else
				    	{
				    		movingSince[j][i] = 0;
				    	}
				    	
				    	
				    	gl.glPushMatrix();
				    	
				    	gl.glColor3f(teamRed, teamGreen, teamBlue);
				    	
				    	gl.glPushMatrix();
				    	
				    	gl.glTranslated(players[i].getX(), players[i].getY(), 0);
				    	
				    	if(players[i].getPlayerState().getStateType() == PlayerState.StateType.Dead || (_world.getWinningTeams() != null && !_world.getWinningTeams().contains(_world.getTeams()[j])))
				    	{
				    		gl.glTranslatef(0f, 0f, -10f);
				    		gl.glRotatef(90, 1f, 0f, 0f);
				    	}
				    	
				    	
				    	gl.glPushMatrix();
				    	
				    	Point orientation = players[i].getOrientation();
				    	double angle = 0;
				    	
				    	double orientationX = Math.round(orientation.getX() * 10000) / 10000.0;
				    	double orientationY = Math.round(orientation.getY() * 10000) / 10000.0;
				    	
				    	if(orientationX != -1 || orientationY != -1)
				    	{
					    	if(orientationY == 1)
					    	{
					    		angle = 0;
					    	}
					    	else if(orientationY == -1)
					    	{
					    		angle = 180;
					    	}
					    	else if(orientationX == 1)
					    	{
					    		angle = -90;
					    	}
					    	else if(orientationX == -1)
					    	{
					    		angle = 90;
					    	}
					    	else if(orientationX > 0 && orientationY > 0)
					    	{
					    		angle = 0 - (90 - (Math.atan(orientationY / orientationX)*180/Math.PI));
					    	}
					    	else if(orientationX < 0 && orientationY > 0)
					    	{
					    		angle = 90 - Math.abs(Math.atan(orientationY / orientationX)*180/Math.PI);
					    	}
					    	else if(orientationX < 0 && orientationY < 0)
					    	{
					    		angle = 90 + Math.atan(orientationY / orientationX)*180/Math.PI;
					    	}
					    	else if(orientationX > 0 && orientationY < 0)
					    	{
					    		angle = 270 + Math.atan(orientationY / orientationX)*180/Math.PI;
					    	}
				    	}
				    	//if(orientation.getX() > 0 && orientation.)
				    	if(players[i].getPlayerState().getStateType() != PlayerState.StateType.Dead)
				    	{
				    		gl.glRotated(angle, 0, 0, 1);
				    		
				    		if(timeSinceWinner[j] < JUMP_FRAMES/2.0) {
				    			gl.glTranslatef(0.0f, 0.0f, timeSinceWinner[j]);
				    		}
				    		else
				    		{
				    			gl.glTranslatef(0.0f, 0.0f, JUMP_FRAMES - timeSinceWinner[j]);
				    		}
				    	}
				    	
				    	gl.glPushMatrix();
				    	gl.glColor3f(0.97f, 0.78f, 0.58f);
				    	gl.glTranslatef(0f, 0f, 40.0f);
				    	glu.gluSphere(quad, 15, _slices, _slices);
				    	
				    	gl.glColor3f(teamRed, teamGreen, teamBlue);

				    	
				    	gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
						drawHalfSphere(_slices, _slices, 17, gl);
				    	
						gl.glPopMatrix();
				    	
						
						gl.glPushMatrix();
						gl.glTranslatef(0f, 0f,7.5f);
						
						gl.glColor3f(0.62f, 0.35f, 0.26f);
						
						glu.gluCylinder(quad, 12, 6, 30, _slices, _slices);						
						gl.glPopMatrix();
						
						gl.glPushMatrix();
						gl.glTranslatef(0f, 0f, 57f);
						glu.gluSphere(quad, 4.3f, _slices, _slices);						
						gl.glPopMatrix();
				    	
						gl.glPushMatrix();
						gl.glColor3f(teamRed, teamGreen, teamBlue);
						gl.glTranslatef(0f, 0f, 1f);
						glu.gluCylinder(quad, 9, 9, 6.5f, _slices, _slices);						
						gl.glPopMatrix();
				    	
				    	
						gl.glPushMatrix();
						gl.glColor3f(teamRed, teamGreen, teamBlue);
						if(players[i].getPlayerState().getStateType() == PlayerState.StateType.Throwing)
						{
							gl.glTranslatef(20f, 0f, 34f);
						}
						else if(_world.getWinningTeams() == null)
						{
							gl.glTranslatef(12f, (movingSince[j][i]/1.5f) % 10, 14f);
						}
						else if(players[i].getPlayerState().getStateType() != PlayerState.StateType.Dead &&_world.getWinningTeams().contains(_world.getTeams()[j]))
						{
							gl.glTranslatef(20f, 0f, 34f);
						}
						else
						{
							gl.glTranslatef(12f, 0f, 14f);
						}
						glu.gluSphere(quad, 5, _slices, _slices);						
						gl.glPopMatrix();
						
						gl.glPushMatrix();
						gl.glColor3f(teamRed, teamGreen, teamBlue);
						if(movingSince[j][i] != 0 && _world.getWinningTeams() == null)
						{
							gl.glTranslatef(-12f, (movingSince[j][i]/1.5f + 5.0f) % 10, 14f);
						}
						else if(players[i].getPlayerState().getStateType() != PlayerState.StateType.Dead && _world.getWinningTeams() != null && _world.getWinningTeams().contains(_world.getTeams()[j]))
						{
							gl.glTranslatef(-20f, 0f, 34f);
						}
						else
						{
							gl.glTranslatef(-12f, 0f, 14f);
						}
						glu.gluSphere(quad, 5, _slices, _slices);						
						gl.glPopMatrix();
						
						gl.glPushMatrix();
						gl.glColor3f(0.0f, 0.0f, 0.0f);
						if(_world.getWinningTeams() == null)
						{
							gl.glTranslatef(-4f, (4f + movingSince[j][i]/1.5f) % 10, 0.5f);
						}
						else
						{
							gl.glTranslatef(-4f, 4f, 0.5f);
						}
						gl.glScalef(2.5f, 6f, 1f);
						glu.gluSphere(quad, 2, _slices, _slices);
						gl.glPopMatrix();
						
						gl.glPushMatrix();
						gl.glColor3f(0.0f, 0.0f, 0.0f);
						if(movingSince[j][i] != 0 && _world.getWinningTeams() == null)
						{
							gl.glTranslatef(4f, (4f + movingSince[j][i]/1.5f + 5.0f) % 10, 0.5f);
						}
						else
						{
							gl.glTranslatef(4f, 4f, 0.5f);
						}
						gl.glScalef(2.5f, 6f, 1f);
						glu.gluSphere(quad, 2, _slices, _slices);
						gl.glPopMatrix();
						
						
				    	
				    	gl.glPopMatrix();
				    	
				    	if(players[i].getPlayerState().getStateType() != PlayerState.StateType.Dead && _world.getWinningTeams() == null)
				    	{
				    		gl.glColor3f(teamRed, teamGreen, teamBlue);
					    	gl.glBegin(GL.GL_LINE_LOOP);
					    	
					    	gl.glVertex3d(-40, 0, 70);
					    	gl.glVertex3d(40, 0, 70);
					    	gl.glVertex3d(40, 0, 80);
					    	gl.glVertex3d(-40, 0, 80);
					    	
					    	gl.glEnd();
					    	
					    	gl.glBegin(GL2.GL_QUADS);
					    	
					    	gl.glVertex3d(-40, 0, 70);
					    	gl.glVertex3d(-40 + players[i].getHealth()*.8, 0, 70);
					    	gl.glVertex3d(-40 + players[i].getHealth()*.8, 0, 80);
					    	gl.glVertex3d(-40, 0, 80);
					    	
					    	gl.glEnd();
					    	
					    	gl.glPushMatrix();
					    	
					    	gl.glPopMatrix();
				    	}
				    	
				    	gl.glPopMatrix();
				    	
				    	
				    	gl.glPopMatrix();
				    }
		    	}
				
				if(_world.getWinningTeams() == null)
				{
					for(int j = 0; j < _world.getSnowballs().size(); j++)
			    	{
						//_world.
						gl.glPushMatrix();
						
						// g -> gravity
						double g = 0.45 /*0.6*/;
						
						// t = distance / distancePerFrame
						double distance = utilities.maths.Math.getEuclidianDistance(_world.getSnowballs().get(j).getStartingPoint(), _world.getSnowballs().get(j).getDestination());
						double t = distance / _world.getSnowballs().get(j).getDistancePerFrame();
						
						// vz0 = - z0 / t - a * t / 2
						double vz0 = 34f / t + g * t / 2.0;
						
						double currentT = (utilities.maths.Math.getEuclidianDistance(_world.getSnowballs().get(j).getPoint(), _world.getSnowballs().get(j).getDestination()) / distance) * t;
						
						//z = z0 + vz0 * t + 1/2 * a * t^2
						double z = vz0 * currentT + 0.5 * (-g) * currentT * currentT;
						
						gl.glColor3f(0.8f, 0.8f, 0.8f);
						
						gl.glTranslated(_world.getSnowballs().get(j).getX(), _world.getSnowballs().get(j).getY(), z);
						glu.gluSphere(quad, 7, _slices, _slices);
						
				        gl.glPopMatrix();
			    	}
				}
				
				gl.glBegin(GL2.GL_QUADS);
				gl.glColor3f(1,0,1);
				gl.glVertex3d(_world.getFlag().getX() - 75, _world.getFlag().getY(), 100);
				gl.glVertex3d(_world.getFlag().getX() - 75, _world.getFlag().getY(), 140);
				gl.glVertex3d(_world.getFlag().getX(), _world.getFlag().getY(), 140);
				gl.glVertex3d(_world.getFlag().getX(), _world.getFlag().getY(), 100);
				gl.glEnd();
				
				gl.glBegin(GL2.GL_LINES);
				gl.glVertex3d(_world.getFlag().getX(), _world.getFlag().getY(), 0);
				gl.glVertex3d(_world.getFlag().getX(), _world.getFlag().getY(), 145);
				gl.glEnd();
				
				gl.glPopMatrix();
			}
		}

		private void drawHalfSphere(int scaley, int scalex, float r, GL2 gl) {
			int i, j;
			float [][] v = new float[scalex*scaley][3];
		 
			for (i=0; i<scalex; ++i) {
				for (j=0; j<scaley; ++j) {
					v[i*scaley+j][0]=(float) (r*Math.cos(j*2*Math.PI/scaley)*Math.cos(i*Math.PI/(2*scalex)));
					v[i*scaley+j][1]=(float) (r*Math.sin(i*Math.PI/(2*scalex)));
					v[i*scaley+j][2]=(float) (r*Math.sin(j*2*Math.PI/scaley)*Math.cos(i*Math.PI/(2*scalex)));
				}
			}
		 
			gl.glBegin(GL2.GL_QUADS);
			for (i=0; i<scalex-1; ++i) {
				for (j=0; j<scaley; ++j) {
					gl.glVertex3f(v[i*scaley+j][0], v[i*scaley+j][1], v[i*scaley+j][2]);
					gl.glVertex3f(v[i*scaley+(j+1)%scaley][0], v[i*scaley+(j+1)%scaley][1], v[i*scaley+(j+1)%scaley][2]);
					gl.glVertex3f(v[(i+1)*scaley+(j+1)%scaley][0], v[(i+1)*scaley+(j+1)%scaley][1], v[(i+1)*scaley+(j+1)%scaley][2]);
					gl.glVertex3f(v[(i+1)*scaley+j][0], v[(i+1)*scaley+j][1], v[(i+1)*scaley+j][2]);
				}
			}
			gl.glEnd();
		}
		
		@Override
		public void dispose(GLAutoDrawable arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void init(GLAutoDrawable drawable) {
			_drawable = drawable;
			GL2 gl = drawable.getGL().getGL2();// get the OpenGL graphics context
		    glu = new GLU();                         // get GL Utilities
		    quad = glu.gluNewQuadric();
	        //gl.glClearColor(0.56f, 0.7f, 0.87f, 0.0f); // set background (clear) color
		    gl.glClearColor(0.757f, 0.89f, 1.0f, 0.0f); // set background (clear) color
	        gl.glClearDepth(1.0f);      // set clear depth value to farthest
	        gl.glEnable(GL_DEPTH_TEST); // enables depth testing
	        gl.glDepthFunc(GL_LEQUAL);  // the type of depth test to do
	        gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best perspective correction
	        gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out lighting
	        gl.glEnable(GL.GL_BLEND);
	        gl.glEnable( GL2.GL_POINT_SMOOTH );
	        gl.glEnable( GL2.GL_LINE_SMOOTH );
	        gl.glEnable (GL2.GL_POLYGON_SMOOTH);
		}
		
		private float _aspect;

		@Override
		public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		      GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context
		      
		      if (height == 0) height = 1;   // prevent divide by zero
		      _aspect = (float)width / height;
		 
		      // Set the view port (display area) to cover the entire window
		      gl.glViewport(0, 0, width, height);		      
		 
		      // Setup perspective projection, with aspect ratio matches viewport
		      gl.glMatrixMode(GL_PROJECTION);  // choose projection matrix
		      gl.glLoadIdentity();             // reset projection matrix
		      glu.gluPerspective(45.0, _aspect, 0.1, 10000.0); // fovy, aspect, zNear, zFar
		      
		      
		      //System.out.println(utilities.maths.Math.getEuclidianDistance(0, 0, (a * Math.cos(t*2*Math.PI)), (a * Math.sin(t*2*Math.PI))));
		      
		      //System.out.println((a * Math.cos(t*2*Math.PI)) + " " + (a * Math.sin(t*2*Math.PI)) + " " +  -750 + " " + 0 + " " + 0);
		      
			  a = (float) ((eyeZ - centerZ) / Math.tan(angle));
		      
		      lookAt(glu);
		      
		      
		      // Enable the model-view transform
		      gl.glMatrixMode(GL_MODELVIEW);
		      gl.glLoadIdentity(); // reset
	   }
		
		
		private void lookAt(GLU glu)
		{
			eyeX = (float) (a * Math.cos(t*2*Math.PI) + centerX);
			eyeY = (float) (a * Math.sin(t*2*Math.PI) + centerY);
			
			glu.gluLookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, 0, 0, 1);
		}
		
		
		public float centerX = 0;
		public float centerY = 0;
		public float centerZ = -2000;
		public float eyeZ = -750;
		public float eyeX;
		public float eyeY;
		
		public void setCenterX(float centerX)
		{
			this.centerX = centerX;
			
			_requestCameraUpdate = true;
		}
		
		public void setCenterY(float centerY)
		{
			this.centerY = centerY;
			
			_requestCameraUpdate = true;
		}
		
		public void setCenterZ(float centerZ)
		{
			this.centerZ = centerZ;
			
			_requestCameraUpdate = true;
		}
		
		private float t = 0.75f;
		private float a = 0;
		
		private float angle = (float) (34.04f * Math.PI / 180.0f);
		
		public float getT()
		{
			return t;
		}
		
		private boolean _requestCameraUpdate = false;
		public void setT(float t)
		{
			this.t = t;
			
			_requestCameraUpdate = true;
		}
		
		public float getA()
		{
			return a;
		}
		
		public void setA(float a)
		{
			this.a = a;
			
			eyeZ = (float) (a*Math.tan(angle) + centerZ);
			_requestCameraUpdate = true;
		}
		
		class TessellatorCallBack implements GLUtessellatorCallback
		{
			private GL2 gl;
		    private GLU glu;

		    public TessellatorCallBack(GL2 gl, GLU glu)
		    {
		    	this.gl = gl;
		    	this.glu = glu;
		    }

		    public void begin(int type)
		    {
		    	gl.glBegin(type);
		    }

		    public void end()
		    {
		    	gl.glEnd();
		    }

		    public void vertex(Object vertexData)
		    {
		    	double[] pointer;
		    	if (vertexData instanceof double[])
		    	{
		    		pointer = (double[]) vertexData;
		    		if (pointer.length == 6)
		    			gl.glColor3dv(pointer, 3);
		    		gl.glVertex3dv(pointer, 0);
	    		}

		    }

		    public void vertexData(Object vertexData, Object polygonData)
		    {
		    }

		    /*
		     * combineCallback is used to create a new vertex when edges intersect.
		     * coordinate location is trivial to calculate, but weight[4] may be used to
		     * average color, normal, or texture coordinate data. In this program, color
		     * is weighted.
		     */
		    public void combine(double[] coords, Object[] data, //
		        float[] weight, Object[] outData)
		    {
		    	double[] vertex = new double[6];
		    	int i;

		    	vertex[0] = coords[0];
		    	vertex[1] = coords[1];
		    	vertex[2] = coords[2];
		    	for (i = 3; i < 6/* 7OutOfBounds from C! */; i++)
		    		vertex[i] = weight[0] //
		                    * ((double[]) data[0])[i] + weight[1]
		                    * ((double[]) data[1])[i] + weight[2]
		                    * ((double[]) data[2])[i] + weight[3]
		                    * ((double[]) data[3])[i];
	    		outData[0] = vertex;
	    	}

		    public void combineData(double[] coords, Object[] data, //
		        float[] weight, Object[] outData, Object polygonData)
		    {
		    }

		    public void error(int errnum)
		    {
		    	String estring;
		    	
		    	estring = glu.gluErrorString(errnum);
		    	System.err.println("Tessellation Error: " + estring);
		    }

		    public void beginData(int type, Object polygonData)
		    {
		    }

		    public void endData(Object polygonData)
		    {
		    }

		    public void edgeFlag(boolean boundaryEdge)
		    {
		    }

		    public void edgeFlagData(boolean boundaryEdge, Object polygonData)
		    {
		    }

		    public void errorData(int errnum, Object polygonData)
		    {
		    }
	    }
	}
}
