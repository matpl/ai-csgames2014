package world.action;

import world.Player;
import world.Point;

/**
 * Action that moves the player to the desired destination. If the player can't reach the destination in a straight line, the shortest path will be used by the server. If the destination is in a wall, the action won't be executed.
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class MoveAction extends Action
{

	private Point _destination;
	
	/**
	 * Action that moves the player to the desired destination. If the player can't reach the destination in a straight line, the shortest path will be used by the server. If the destination is in a wall, the action won't be executed.
	 * @param player
	 * @param destination
	 */
	public MoveAction(Player player, Point destination)
	{
		super(player);
		_destination = destination;
	}
	
	/**
	 * Returns the expected destination of the player.
	 * @return
	 */
	public Point getDestination()
	{
		return _destination;
	}
	
	public void setDestination(Point destination)
	{
		_destination = destination;
	}
	
	public String toString()
	{
		return "m " + _player.getTeam().getId() + " " + _player.getId() + " " + _destination.getX() + " " + _destination.getY();
	}
}
