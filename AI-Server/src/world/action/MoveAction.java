package world.action;

import java.util.ArrayList;

import utilities.graph.ParametricPointList;
import utilities.graph.PathList;
import world.Map;
import world.Player;
import world.PlayerState;
import world.Point;
import world.Snowball;
import world.World;

/**
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class MoveAction extends Action {

	private Point _destination;
	
	private ParametricPointList _parametricPath;
	
	private Player _player;
	
	private double _firstXM;
	private double _firstXB;
	private double _firstYM;
	private double _firstYB;
	
	private double _lastXM;
	private double _lastXB;
	private double _lastYM;
	private double _lastYB;
	
	public MoveAction(int teamId, int playerId, Point destination)
	{
		super(teamId, playerId);
		this._destination = destination;
		
		this._toString = "m " + teamId + " " + playerId + " " + destination.getX() + " " + destination.getY();
	}

	String append;
	
	public boolean initialize(World world)
	{
		if(!super.initialize(world))
		{
			return false;
		}
		
		append = "";
		double dist;
		if(_parametricPath == null)
		{
			_player = world.getTeams()[_teamId].getPlayerById(_playerId);
			if(_player == null  || _player.getPlayerState().getStateType() == PlayerState.StateType.Dead)
			{
				return false;
			}
			Point playerPoint = _player.getPoint();			
			
			Map map = world.getMap();
			PathList[] list = map.getPathList();
			
			if(_player.getHealth() <= 0 || map.isPointInWall(_destination) || (playerPoint.getX() == _destination.getX() && playerPoint.getY() == _destination.getY()) ||
					_destination.getX() < 0 || _destination.getY() < 0 || _destination.getX() > world.getMap().getWidth() || _destination.getY() > world.getMap().getHeight())
			{
				return false;
			}
			
			// TODO: OPTIMIZE THIS
			
			if(!map.isCrossingWall(playerPoint, _destination))
			{
				//straight line. easy as shit
				ArrayList<Point> points = new ArrayList<Point>();
				points.add(playerPoint);
				points.add(_destination);
				_parametricPath = new ParametricPointList(points);
				
				_tStart = 0.0;
				_tEnd = 1.0;
			}
			else
			{
				double[] playerDistances = new double[list.length];
				double[] destinationDistances = new double[list.length];
				
				Point source;
				for(int i = 0; i < list.length; i++)
				{
					source = list[i].getSource();
					if(!map.isCrossingWall(playerPoint, source))
					{
						playerDistances[i] = Math.sqrt(Math.pow(playerPoint.getX() - source.getX(), 2) + Math.pow(playerPoint.getY() - source.getY(), 2));
					}
					else
					{
						playerDistances[i] = -1;
					}
					
					if(!map.isCrossingWall(_destination, source))
					{
						destinationDistances[i] =  Math.sqrt(Math.pow(_destination.getX() - source.getX(), 2) + Math.pow(_destination.getY() - source.getY(), 2));
					}
					else
					{
						destinationDistances[i] = -1;
					}
				}
				
				double minDistance = Double.MAX_VALUE;
				double currentDistance;
				
				int minI = -1;
				int minJ = -1;
				
				for(int i = 0; i < playerDistances.length; i++)
				{
					if(playerDistances[i] >= 0)
					{
						for(int j = 0; j < destinationDistances.length; j++)
						{
							if(destinationDistances[j] >= 0)
							{
								currentDistance = playerDistances[i] + list[i].getDistanceTo(list[j].getSource()) + destinationDistances[j];
								
								if(currentDistance < minDistance)
								{
									minDistance = currentDistance;
									_parametricPath = list[i].getParametricPathTo(list[j].getSource());
									
									minI = i;
									minJ = j;
								}
							}
						}
					}
				}
				if(_parametricPath != null)
				{
					_tStart = 0 - playerDistances[minI]/_parametricPath.getTotalDistance();
					_tEnd = 1.0 + destinationDistances[minJ]/_parametricPath.getTotalDistance();
					
					Point firstPoint = _parametricPath.getPointList().get(0);
					Point lastPoint = _parametricPath.getPointList().get(_parametricPath.getPointList().size()-1);
					
					_firstXM = firstPoint.getX() - playerPoint.getX();
					_firstYM = firstPoint.getY() - playerPoint.getY();
					_firstXB = playerPoint.getX();
					_firstYB = playerPoint.getY();
					
					
					_lastXM = _destination.getX() - lastPoint.getX();
					_lastYM = _destination.getY() - lastPoint.getY();
					_lastXB = lastPoint.getX();
					_lastYB = lastPoint.getY();
				}
				else
				{
					ArrayList<Point> points = new ArrayList<Point>();
					points.add(playerPoint);
					try
					{
						points.add(list[minJ].getSource());
					}catch(Exception e)
					{
						e.printStackTrace();
					}
					points.add(_destination);
					_parametricPath = new ParametricPointList(points);
					_tStart = 0.0;
					_tEnd = 1.0;
					
				}
				
			}
			
			_player.getPlayerState().setStateType(PlayerState.StateType.Moving);
			_player.getPlayerState().setCurrentAction(this);
			
			_tCurrent = _tStart;
		}
		else
		{
			// check for the player's HP and such
			if(_player.getHealth() <= 0)
			{
				_player.getPlayerState().setStateType(PlayerState.StateType.Dead);
				_player.getPlayerState().setCurrentAction(null);
				return false;
			}
		}
		
		if(_player.isFlagHolder())
		{
			dist = _speed/2.0;
		}
		else
		{
			dist = _speed;
		}
		
		// player is slower if he's hurt
		dist = dist - (dist/2.0)*((Player.MAX_HEALTH - _player.getHealth()) / Player.MAX_HEALTH);
		
		_tIncrement = dist / _parametricPath.getTotalDistance();
		
		return true;
	}
	
	private double _speed = 5.0;
	
	private double _tStart;
	private double _tEnd;
	private double _tIncrement;
	
	private double _tCurrent;
	
	public boolean execute(World world)
	{
		super.execute(world);
		
		_tCurrent = Math.min(_tCurrent + _tIncrement, _tEnd);
		
		double oldX = _player.getX();
		double oldY = _player.getY();
		
		if(_tCurrent < 0)
		{
			_player.setX(_firstXM * ((_tCurrent - _tStart) / (_tStart * -1.0)) + _firstXB);
			_player.setY(_firstYM * ((_tCurrent - _tStart) / (_tStart * -1.0)) + _firstYB);
		}
		else if(_tCurrent > 1)
		{			
			_player.setX(_lastXM * ((_tCurrent - 1.0) / (_tEnd - 1.0)) + _lastXB);
			_player.setY(_lastYM * ((_tCurrent - 1.0) / (_tEnd - 1.0)) + _lastYB);
		}
		else
		{
			Point p = _parametricPath.getPositionAt(_tCurrent);
			
			_player.setX(p.getX());
			_player.setY(p.getY());
		}
		
		if(_player.isFlagHolder())
		{
			world.getFlag().setX(_player.getX());
			world.getFlag().setY(_player.getY());
		}

		double dist = utilities.maths.Math.getEuclidianDistance(oldX, oldY, _player.getX(), _player.getY());
		
		_player.getOrientation().setX((_player.getX() - oldX)/dist);
		_player.getOrientation().setY((_player.getY() - oldY)/dist);
		
		ArrayList<Snowball> snowballs = world.getSnowballs();
		for(int i = snowballs.size() - 1; i >= 0; i--)
		{
			if(utilities.maths.Math.getEuclidianDistance(_player.getX(), _player.getY(), snowballs.get(i).getX(), snowballs.get(i).getY()) <= ThrowAction.MINIMUM_HIT_DISTANCE)
			{
				_player.setHealth(Math.max(0, _player.getHealth() - snowballs.get(i).getDamage()));
				if(_player.getHealth() == 0)
				{
					if(_player.isFlagHolder())
					{
						_player.setFlagHolder(false);
						world.getFlag().setHolder(null);
					}
					
					_player.getPlayerState().setCurrentAction(null);
					_player.getPlayerState().setStateType(PlayerState.StateType.Dead);
					append += " d " + _teamId + " " + _player.getId();
				}
				
				snowballs.get(i).setActive(false);
				append += " r " + snowballs.get(i).getId();
			}
		}
		
		if(_player.getHealth() <= 0 && _player.isFlagHolder())
		{
			_player.setFlagHolder(false);
			world.getFlag().setHolder(null);
		}
		
		if(_tCurrent >= _tEnd)
		{			
			_player.getPlayerState().setStateType(PlayerState.StateType.Idle);
			_player.getPlayerState().setCurrentAction(null);
			return true;
		}
		else
		{
			return false;	
		}
	}
	
	public Point getDestination()
	{
		return _destination;
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		return null;
	}
	
	public String toString()
	{
		return "m " + _teamId + " "  + _playerId + " " + _player.getX() + " " + _player.getY() + " " + _destination.getX() + " " + _destination.getY() + " " + _player.getOrientation().getX() + " " + _player.getOrientation().getY() + append;
	}
}
