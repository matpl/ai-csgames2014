package world.action;

import world.Player;

/**
 * Abstract class that represents an action that can be executed by a player.
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public abstract class Action {

	protected Player _player;
	
	protected Action(Player player)
	{
		_player = player;
	}
	
	/**
	 * Returns the player that executes this action.
	 * @return
	 */
	public Player getPlayer()
	{
		return _player;
	}
}
