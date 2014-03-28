package world.action;

import world.Player;
import world.PlayerState;
import world.World;

/**
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class IdleAction extends Action {

	private Player _player;
	
	public IdleAction(int teamId, int playerId)
	{
		super(teamId, playerId);
		
		this._toString = "i " + teamId + " " + playerId;
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
			
			if(_player == null || _player.getPlayerState().getStateType() == PlayerState.StateType.Dead)
			{
				return false;
			}
		}
		
		return true;
	}
	
	public boolean execute(World world)
	{
		super.execute(world);
		
		_player.getPlayerState().setCurrentAction(null);
		_player.getPlayerState().setStateType(PlayerState.StateType.Idle);
		
		return true;
	}
	
	public Object clone()
	{
		// todo: is this necessary
		return null;
	}
}
