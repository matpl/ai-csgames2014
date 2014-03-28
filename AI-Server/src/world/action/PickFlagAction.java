package world.action;

import world.Player;
import world.PlayerState;
import world.World;

/**
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class PickFlagAction extends Action {

	private Player _player;
	
	public static int MIN_FLAG_DISTANCE = 10;
	
	public PickFlagAction(int teamId, int playerId)
	{
		super(teamId, playerId);
		
		this._toString = "p " + teamId + " " + playerId;
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
			
			if(!world.canPickFlag() || _player == null || _player.getPlayerState().getStateType() == PlayerState.StateType.Dead || world.getFlag().getHolder() != null || utilities.maths.Math.getEuclidianDistance(_player.getX(), _player.getY(), world.getFlag().getX(), world.getFlag().getY()) > MIN_FLAG_DISTANCE)
			{
				return false;
			}
		}
		
		return true;
	}
	
	public boolean execute(World world)
	{
		super.execute(world);
		
		_player.setFlagHolder(true);
		world.getFlag().setHolder(_player);
		
		world.getFlag().setX(_player.getX());
		world.getFlag().setY(_player.getY());
		
		return true;
	}
	
	public Object clone()
	{
		// todo: is this necessary
		return null;
	}
	
	public String toString()
	{
		return "p " + _teamId + " " + _player.getId();
	}
}
