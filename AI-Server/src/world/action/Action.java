package world.action;

import java.util.ArrayList;

import world.Player;
import world.PlayerState;
import world.World;

/**
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public abstract class Action implements Cloneable {

	private static ArrayList<String> _commands = new ArrayList<String>();
	
	protected int _teamId;
	protected int _playerId;
	protected boolean _isActive = true;
	protected String _toString = "";
	
	protected Action(int teamId, int playerId)
	{
		_teamId = teamId;
		_playerId = playerId;
	}
	
	public boolean isActive()
	{
		return _isActive;
	}
	
	public void setActive(boolean isActive)
	{
		_isActive = isActive;
	}
	
	private Player _player;
	
	public boolean initialize(World world)
	{
		if(!_isActive)
		{
			return false;
		}
		if(_player == null)
		{
			_player = world.getTeams()[_teamId].getPlayerById(_playerId);
		}
		if(_playerId != -1 && _teamId != -1)
		{
			Action a = _player.getPlayerState().getCurrentAction();
			if(a != null && a != this)
			{
				a.setActive(false);
				_player.getPlayerState().setCurrentAction(null);
				if(_player.getPlayerState().getStateType() != PlayerState.StateType.Dead)
				{
					_player.getPlayerState().setStateType(PlayerState.StateType.Idle);
				}
			}
		}
		return true;
	}
	
	boolean _firstExecution = true;
	public boolean execute(World world)
	{
		if(_firstExecution)
		{
			_firstExecution = false;
			
			_commands.add(world.getFrameCount() + " " + _toString);
		}
		return true;
	}
	
	public abstract Object clone() throws CloneNotSupportedException;
	
	public int getTeamId()
	{
		return _teamId;
	}
	
	public int getPlayerId()
	{
		return _playerId;
	}
	
	public static ArrayList<String> getCommands()
	{
		return _commands;
	}
	
	public String toString()
	{
		return _toString;
	}
}
