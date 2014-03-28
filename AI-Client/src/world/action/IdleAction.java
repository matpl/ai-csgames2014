package world.action;

import world.Player;

/**
 * Action that cancels the player's current action.
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class IdleAction extends Action
{
	/**
	 * Action to stop anything the player was doing (besides being dead)
	 * @param player
	 */
	public IdleAction(Player player)
	{
		super(player);
	}
	
	public String toString()
	{
		return "i " + _player.getTeam().getId() + " " + _player.getId();
	}
}