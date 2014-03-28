package world.action;

import world.Player;
import world.Point;

/**
 * Actions that makes the player throw a snowball at the desired destination. The player will charge his shot for a certain amount of time before throwing. The number of charging frames is defined by (distance / 15).<br /><br />
 * The minimum/maximum distances the player can throw are determined by MIN_DISTANCE and MAX_DISTANCE. If the destination is beyond one of those values, it will be rounded up/down to MIN_DISTANCE / MAX_DISTANCE.<br /><br />
 * The speed of the snowball depends of the throwing distance. At DISTANCE_FOR_MIN_SPEED, the speed is MIN_SPEED. Any distance below that will have the same speed. At DISTANCE_FOR_MAX_SPEED, the speed is MAX_SPEED. Any distance higher will have the same speed.<br /><br />
 * The damage of the snowball = distance * DAMAGE_PER_DISTANCE_UNIT<br /><br />
 * A snowball will hit any player in a radius of MIN_HIT_DISTANCE, and it disappears on impact. If many players are within MIN_HIT_DISTANCE, they will all be damaged.<br /><br /> 
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class ThrowAction extends Action
{
	/**
	 * Minimum distance from the snowball to be hit -> 15
	 */
	public static double MIN_HIT_DISTANCE = 15;
	
	/**
	 * Minimum throw distance -> 50
	 */
	public static double MIN_DISTANCE = 50;
	/**
	 * Maximum throw distance -> 600
	 */
	public static double MAX_DISTANCE = 600;
	
	/**
	 * Snowball damage per distance unit travelled -> 0.1
	 */
	public static double DAMAGE_PER_DISTANCE_UNIT = 0.1;
	
	/**
	 * Throw distance to reach the maximum snowball speed -> 400
	 */
	public static int DISTANCE_FOR_MAX_SPEED = 400;
	/**
	 * Throw distance to reach the minimum snowball speed -> 100
	 */
	public static int DISTANCE_FOR_MIN_SPEED = 100;
	/**
	 * Minimum snowball speed (distance per frame) -> 5
	 */
	public static int MIN_SPEED = 5;
	/**
	 * Maximum snowball speed (distance per frame) -> 20
	 */
	public static int MAX_SPEED = 20;
	
	// TODO: THOSE ARE THE THE DISTANCE WHERE THE SPEED WILL BE THE MIN / MAX
	/*public static double MINIMUM_DISTANCE = 100;
	public static double MAXIMUM_DISTANCE = 400;*/

	private Point _destination;
	
	private int _remainingFrames;
	
	/**
	 * Action for throwing a snowball at the destination (Refer to class comments for details).
	 * 
	 * @param player
	 * @param destination
	 */
	public ThrowAction(Player player, Point destination)
	{
		super(player);
		_destination = destination;
	}
	
	/**
	 * Returns the expected snowball destination.
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
	
	/**
	 * Returns the remaining charging frames of the throw action.
	 * @return
	 */
	public int getRemainingFrames()
	{
		return _remainingFrames;
	}
	
	public void setRemainingFrames(int frames)
	{
		this._remainingFrames = frames;
	}
	
	public String toString()
	{
		return "t " + _player.getTeam().getId() + " " + _player.getId() + " " + _destination.getX() + " " + _destination.getY();
	}
}
