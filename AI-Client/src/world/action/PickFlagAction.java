package world.action;

import world.Player;

/**
 * Action that makes the player pick the flag if he can do it. To be able to pick the flag, the proportion of dead players has to be >= DEAD_PROPORTION_TO_PICK and the distance to the flag has to be <= MIN_DISTANCE_TO_PICK.
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class PickFlagAction extends Action
{
	/**
	 * Minimum distance to pick the flag -> 10
	 */
	public static int MIN_DISTANCE_TO_PICK = 10;
	/**
	 * Dead players proportion to pick the flag -> 0.2
	 */
	public static double DEAD_PROPORTION_TO_PICK = 0.2;
	
	/**
	 * Action that makes the player pick the flag if he can do it. To be able to pick the flag, the proportion of dead players has to be >= DEAD_PROPORTION_TO_PICK and the distance to the flag has to be <= MIN_DISTANCE_TO_PICK.
	 * @param player
	 */
	public PickFlagAction(Player player)
	{
		super(player);
	}
	
	public String toString()
	{
		return "p " + _player.getTeam().getId() + " " + _player.getId();
	}
}