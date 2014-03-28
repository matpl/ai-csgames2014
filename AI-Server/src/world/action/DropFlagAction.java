package world.action;

import world.Player;
import world.PlayerState;
import world.World;

/**
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class DropFlagAction extends Action {

	private Player _player;
	
	public DropFlagAction(int teamId, int playerId)
	{
		super(teamId, playerId);
		
		this._toString = "d " + teamId + " " + playerId;
	}
	
	public boolean initialize(World world)
	{
		if(!super.initialize(world))
		{
			return false;
		}
		if(_player == null)
		{
			_player = world.getTeams()[_teamId].getPlayerById(_playerId);
			
			if(_player == null || world.getFlag().getHolder() != _player || _player.getPlayerState().getStateType() == PlayerState.StateType.Dead)
			{
				return false;
			}
		}
		
		return true;
	}
	
	public boolean execute(World world)
	{
		super.execute(world);
		
		_player.setFlagHolder(false);
		world.getFlag().setHolder(null);
		
		return true;
	}
	
	public Object clone()
	{
		// todo: is this necessary
		return null;
	}
	
	public String toString()
	{
		return "d " + _teamId + " " + _player.getId();
	}
}
