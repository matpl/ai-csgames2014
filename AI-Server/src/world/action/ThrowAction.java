package world.action;

import world.Player;
import world.PlayerState;
import world.Point;
import world.Snowball;
import world.Team;
import world.World;

/**
 * 
 * KNOW BUG: a snowball can explode on a player that is behind a very slim wall. this is because at each frame, I check the collisions with the players before...
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class ThrowAction extends Action {
	
	private Point _destination;
	private Player _player;
	
	private int _chargingFrames;
	
	private int _currentFrame = 0;
	
	private Snowball _snowball;
	
	private double _mX;
	private double _mY;
	private double _bX;
	private double _bY;
	
	private double _startX;
	private double _startY;
	
	private World _world;
	
	private double _distance;
	
	public Point getDestination()
	{
		return _destination;
	}
	
	public ThrowAction(int teamId, int playerId, Point destination)
	{
		super(teamId, playerId);
		this._destination = destination;
		
		this._toString = "t " + teamId + " " + playerId + " " + destination.getX() + " " + destination.getY();
	}
	
	public boolean initialize(World world)
	{
		if(!super.initialize(world))
		{
			return false;
		}
		append = "";
		if(_world == null)
		{
			_world = world;
			_player = world.getTeams()[_teamId].getPlayerById(_playerId);
			if(_player == null || _player.getPlayerState().getStateType() == PlayerState.StateType.Dead)
			{
				return false;
			}
			_startX = _player.getX();
			_startY = _player.getY();
			
			_distance = Math.sqrt(Math.pow(_destination.getX() - _startX, 2) + Math.pow(_destination.getY() - _startY, 2));

			if(_player.getHealth() <= 0 || _distance == 0 /* < OFFSET || _distance > MAXIMUM_DISTANCE*/)
			{
				// this is not permitted, it will hit the player
				return false;
			}
			
			double destX = -1;
			double destY = -1;
			if(_distance < MINIMUM_DISTANCE)
			{
				destX = (_destination.getX() - _startX) * (MINIMUM_DISTANCE / _distance) + _startX;
				destY = (_destination.getY() - _startY) * (MINIMUM_DISTANCE / _distance) + _startY;
			}
			else if(_distance > MAXIMUM_DISTANCE)
			{
				destX = (_destination.getX() - _startX) * (MAXIMUM_DISTANCE / _distance) + _startX;
				destY = (_destination.getY() - _startY) * (MAXIMUM_DISTANCE / _distance) + _startY;
			}
			
			
			// we need to offset the snowball, or else it will hit the player
			_startX = (_destination.getX() - _startX) * (OFFSET / _distance) + _startX;
			_startY = (_destination.getY() - _startY) * (OFFSET / _distance) + _startY;
			
			if(destX != -1)
			{
				_destination.setX(destX);
				_destination.setY(destY);
			}
			
			_mX = _destination.getX() - _startX;
			_mY = _destination.getY() - _startY;
			_bX = _startX;
			_bY = _startY;
			
			_distance = Math.sqrt(Math.pow(_destination.getX() - _startX, 2) + Math.pow(_destination.getY() - _startY, 2));
			
			if(_distance < MINIMUM_SPEED_DISTANCE)
			{
				_steps = _distance / (MINIMUM_SPEED_DISTANCE/STEPS_COUNT);
			}
			else if(_distance > MAXIMUM_SPEED_DISTANCE)
			{
				_steps = _distance / (MAXIMUM_SPEED_DISTANCE/STEPS_COUNT);
			}
			else
			{
				_steps = STEPS_COUNT; 
			}
			
			// we compute the number of frames to "charge" depending of the distance
			// let's say it's 10 per frame
			_chargingFrames = (int) Math.round(_distance / CHARGING_FRAMES_DIVIDER);
			
			
			double dist = utilities.maths.Math.getEuclidianDistance(_player.getX(), _player.getY(), _destination.getX(), _destination.getY());
			_player.getOrientation().setX((_destination.getX() - _player.getX())/dist);
			_player.getOrientation().setY((_destination.getY() - _player.getY())/dist);
		}
		else
		{
			if(_snowball != null && !_snowball.isActive()/* || _player.getHealth() <= 0*/)
			{
				if(_snowball != null)
				{
					_snowball.setActive(false);
					//world.getSnowballs().remove(_snowball);
				}
				return false;
			}
		}
		
		return true;
	}
	
	private double _steps;

	private static double MAXIMUM_DISTANCE = 600;
	private static double MINIMUM_DISTANCE = 50;

	private static double MINIMUM_SPEED_DISTANCE = 100;
	private static double MAXIMUM_SPEED_DISTANCE = 400;
	private static double STEPS_COUNT = 20;
	
	private static double CHARGING_FRAMES_DIVIDER = 15;
	public static double MINIMUM_HIT_DISTANCE = 15;
	private static double OFFSET = 20;
	public static double DAMAGE_DIVIDER = 10;
	
	public boolean execute(World world)
	{	
		super.execute(world);
		
		if(_currentFrame >= _chargingFrames)
		{			
			//if the snowball doesn't exist, create it. if it does, make it move forward
			if(_snowball == null)
			{
				if(_playerId != -1 || _teamId != -1)
				{
					_player.getPlayerState().setCurrentAction(null);
					_player.getPlayerState().setStateType(PlayerState.StateType.Idle);
					append += " i " + _teamId + " " + _playerId;
					
					_playerId = -1;
					_teamId = -1;
				}
				
				int snowballId;
				if(_world.getSnowballs().size() > 0)
				{
					snowballId = _world.getSnowballs().get(_world.getSnowballs().size()-1).getId() + 1;
				}
				else
				{
					snowballId = 0;
				}
				_snowball = new Snowball(snowballId, _startX, _startY);
				_snowball.setDestination(_destination);
				_snowball.setDistancePerFrame(_distance / _steps);				
				_snowball.setDamage(_distance / DAMAGE_DIVIDER);
				
				// add it to the world!!
				_world.getSnowballs().add(_snowball);
			}
			else
			{
				//move the snowball
				// x(t) = mx*t + bx
				double t = Math.min((_currentFrame - _chargingFrames)/_steps, 1.0);
				
				if(t <= 1)
				{
					double x0 = _snowball.getPoint().getX();
					double y0 = _snowball.getPoint().getY();
					_snowball.setX(t*_mX + _bX);
					_snowball.setY(t*_mY + _bY);
					
					
					Team[] teams = _world.getTeams();
					
					boolean hit = false;
					for(int i = 0; i < teams.length; i++)
					{
						Player[] players = teams[i].getPlayers();
						
						for(int j = 0; j < players.length; j++)
						{
							if(players[j].getPlayerState().getStateType() != PlayerState.StateType.Dead && utilities.maths.Math.getDistance(players[j].getX(), players[j].getY(), x0, y0, _snowball.getX(), _snowball.getY()) <= MINIMUM_HIT_DISTANCE)
							{
								players[j].setHealth(Math.max(0, players[j].getHealth() - _snowball.getDamage()));
								if(players[j].getHealth() == 0)
								{
									if(players[j].isFlagHolder())
									{
										players[j].setFlagHolder(false);
										world.getFlag().setHolder(null);
									} 
									players[j].getPlayerState().setCurrentAction(null);
									players[j].getPlayerState().setStateType(PlayerState.StateType.Dead);
									append += " d " + players[j].getTeam().getId() + " " + players[j].getId();
								}
								
								hit = true;
							}
						}
					}
					
					if(hit || _world.getMap().isCrossingWall(x0,  y0, _snowball.getX(), _snowball.getY()))
					{
						_snowball.setActive(false);
						_world.getSnowballs().remove(_snowball);
						return true;
					}
				}

				if(t >= 1)
				{
					_snowball.setActive(false);
					_world.getSnowballs().remove(_snowball);
					return true;
				}
			}
		}
		else
		{
			_player.getPlayerState().setCurrentAction(this);
			_player.getPlayerState().setStateType(PlayerState.StateType.Throwing);
		}
		_currentFrame++;
		return false;
	}
	
	public int getRemainingFrames()
	{
		return _chargingFrames - _currentFrame;
	}
	
	//TODO: this. Or not??
	public Object clone() throws CloneNotSupportedException
	{
		return null;
	}
	
	private String append;
	
	public String toString()
	{
		if(_snowball == null)
		{
			// charging
			return "c " + _teamId + " " + _player.getId() + " " + _destination.getX() + " " + _destination.getY() + append;
		}
		else if(_snowball.isActive())
		{
			return "t " + _snowball.getId() + " " + _snowball.getX() + " " + _snowball.getY() + append;
		}
		else
		{
			return "r " + _snowball.getId() + append;
		}
	}
}
