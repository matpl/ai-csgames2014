package world.action;

import world.Player;

/**
 * Action that makes the player drop the flag at its current position.
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class DropFlagAction extends Action
{
	/**
	 * Action to drop the flag at the current player position
	 * @param player
	 */
	public DropFlagAction(Player player)
	{
		super(player);
	}
	
	public String toString()
	{
		return "d " + _player.getTeam().getId() + " " + _player.getId();
	}
}