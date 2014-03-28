package world;

import world.PlayerState.StateType;

/**
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class Player extends Entity implements Cloneable {

	public static double MAX_HEALTH = 100;
	
	private Team _team;
	
	private PlayerState _state;
	
	private int _playerId;
	
	boolean _isFlagHolder = false;
	
	private double _health = MAX_HEALTH;
	
	// orientation vector (norm of 1)
	private Point _orientation = new Point(-1,-1);
	
	public Player(int playerId, Team team)
	{
		_playerId = playerId;
		_team = team;
		_state = new PlayerState(PlayerState.StateType.Idle, null);
	}
	
	public Player(int playerId, Team team, PlayerState state)
	{
		_playerId = playerId;
		_team = team;
		_state = state;
	}
	
	public int getId()
	{
		return _playerId;
	}
	
	public Team getTeam()
	{
		return _team;
	}
	
	public PlayerState getPlayerState()
	{
		return _state;
	}
	
	public Point getOrientation()
	{
		return _orientation;
	}
	
	public double getHealth()
	{
		return _health;
	}
	
	public void setHealth(double health)
	{
		this._health = health;
	}
	
	public void setPlayerState(PlayerState state)
	{
		_state = state;
	}
	
	public boolean isFlagHolder()
	{
		return _isFlagHolder;
	}
	
	public void setFlagHolder(boolean isFlagHolder)
	{
		_isFlagHolder = isFlagHolder;
	}
	
	public void kill()
	{
		_health = 0.0;
		_state.setCurrentAction(null);
		_state.setStateType(StateType.Dead);
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		//TODO
		Player p = new Player(_playerId, _team, (PlayerState) _state.clone());
		p.setX(getX());
		p.setY(getY());
		p.setHealth(getHealth());
		p.getOrientation().setX(_orientation.getX());
		p.getOrientation().setY(_orientation.getY());
		return p;
	}

	public String toString()
	{
		return _playerId + "" /*+ " " + getX() + " " + getY()*/ /*+ " " + _state.toString()*/;
	}
}
