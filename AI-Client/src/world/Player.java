package world;

import world.PlayerState.StateType;
import world.action.ThrowAction;

/**
 * Player in a team. Provides basic getters and some helpers.
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class Player extends Entity {

	/**
	 * Max health of a player -> 100
	 */
	public static double MAX_HEALTH = 100;

	private Team _team;

	private PlayerState _state;

	private int _playerId;

	private boolean _isFlagHolder = false;

	private double _health = MAX_HEALTH;

	public Player(int playerId) {
		_playerId = playerId;
		_state = new PlayerState(PlayerState.StateType.Idle, null);
	}

	public Player(int playerId, Team team, PlayerState state) {
		_playerId = playerId;
		_team = team;
		_state = state;
	}

	/**
	 * Returns the player id.
	 * @return
	 */
	public int getId() {
		return _playerId;
	}
	
	protected void setVisible(boolean visible)
	{
		_state.setVisible(visible);
		super.setVisible(visible);
	}

	/**
	 * Returns the player state, which includes the state type, current action, pending action. Returns null if the player isn't currently visible.
	 * @return
	 */
	public PlayerState getPlayerState() {
		if(!isVisible())
		{
			return null;
		}
		return _state;
	}

	/**
	 * Returns the unit vector (length of 1) of the orientation of the player. Returns null if the player isn't currently visible.
	 * @return
	 */
	public Point getOrientation() {
		if(!isVisible())
		{
			return null;
		}
		return _orientation;
	}

	/**
	 * Returns the team containing the current player.
	 * @return
	 */
	public Team getTeam() {
		return _team;
	}

	protected void setTeam(Team team) {
		_team = team;
	}

	/**
	 * Returns the health of the player. Returns -1 if the player isn't currently visible.
	 * @return
	 */
	public double getHealth() {
		if(!isVisible())
		{
			return -1;
		}
		return _health;
	}

	protected void setHealth(double health) {
		this._health = health;
	}

	protected void setPlayerState(PlayerState state) {
		_state = state;
	}

	/**
	 * Returns whether the player is holding the flag. If the player is holding the flag, he becomes visible to ALL.
	 * @return
	 */
	public boolean isFlagHolder() {
		return _isFlagHolder;
	}

	protected void setFlagHolder(boolean isFlagHolder) {
		_isFlagHolder = isFlagHolder;
	}
	
	/**
	 * Returns whether the player can be hit by the snowball. Snowball hits if it travels close to the point (<= ThrowAction.MIN_HIT_DISTANCE). It doesn't check for collisions with other players along the way, but checks for collisions with walls. Returns false if the snowball or player isn't currently visible.
	 * @param snowball
	 * @return
	 */
	public boolean canBeHitBy(Snowball snowball)
	{
		if(!isVisible() || !snowball.isVisible())
		{
			return false;
		}
		
		return snowball.canHit(this); 
	}
	
	/**
	 * Returns whether the player can be hit by the specified player. The specified player has to be currently throwing. If the specified player is not in the current team, the throwing distance is assumed to be ThrowAction.MAX_DISTANCE. The specified player orientation is used to determine the snowball trajectory. Snowball hits if it travels close to the point (<= ThrowAction.MIN_HIT_DISTANCE). It doesn't check for collisions with other players along the way, but checks for collisions with walls. Returns false if one of the players isn't currently visible.
	 * @param player
	 * @return
	 */
	public boolean canBeHitBy(Player player)
	{
		if(!isVisible() || !player.isVisible())
		{
			return false;
		}
		if(player.getPlayerState().getStateType() == StateType.Throwing)
		{
			double startX = player.getX();
			double startY = player.getY();
			
			
			double dist = utilities.maths.Math.getEuclidianDistance(0, 0, player.getOrientation().getX(), player.getOrientation().getY());
			double endX;
			double endY;
			ThrowAction action = (ThrowAction) player.getPlayerState().getCurrentAction();
			if(action != null)
			{
				endX = action.getDestination().getX();
				endY = action.getDestination().getY();
			}
			else
			{
				endX = (startX + player.getOrientation().getX() - startX) * (ThrowAction.MAX_DISTANCE / dist) + startX;
				endY = (startY + player.getOrientation().getY() - startY) * (ThrowAction.MAX_DISTANCE / dist) + startY;
			}
			startX = (startX + player.getOrientation().getX() - startX) * (20.0 / dist) + startX;
			startY = (startY + player.getOrientation().getY() - startY) * (20.0 / dist) + startY;
			
			return player.canHit(getX(), getY(), startX, startY, endX, endY, null);
			
		}
		
		return false;
	}
	
	/**
	 * Returns whether the player can hit the specified player if he shoots at the specified destination. It doesn't check for collisions with other players along the way, but checks for collisions with walls. Returns false if one of the players isn't currently visible.
	 * @param player
	 * @param destination
	 * @return
	 */
	public boolean wouldHitPlayer(Player player, Point destination)
	{
		if(!isVisible() || !player.isVisible() || (destination.getX() == getX() && destination.getY() == getY()))
		{
			return false;
		}
		double startX = getX();
		double startY = getY();
		
		double dist = utilities.maths.Math.getEuclidianDistance(destination, getPoint());
		
		double oriX = (destination.getX() - getX()) / dist;
		double oriY = (destination.getY() - getY()) / dist;
		
		double endX = destination.getX();
		double endY = destination.getY();
		if(dist > ThrowAction.MAX_DISTANCE)
		{
			endX = (startX + oriX - startX) * (ThrowAction.MAX_DISTANCE) + startX;
			endY = (startY + oriY - startY) * (ThrowAction.MAX_DISTANCE) + startY;
		}
				
		startX = (startX + oriX - startX) * (20.0) + startX;
		startY = (startY + oriY - startY) * (20.0) + startY;
		
		return player.canHit(player.getX(), player.getY(), startX, startY, endX, endY, new Point(oriX, oriY));
	}
	
	/**
	 * Returns whether the player can hit the specified player. The player has to be currently throwing. If the player is not in the current team, the throwing distance is assumed to be ThrowAction.MAX_DISTANCE. The player orientation is used to determine the snowball trajectory. Snowball hits if it travels close to the point (<= ThrowAction.MIN_HIT_DISTANCE). It doesn't check for collisions with other players along the way, but checks for collisions with walls. Returns false if one of the players isn't currently visible.
	 * @param player
	 * @return
	 */
	public boolean canHit(Player player)
	{
		// just check if 
		return player.canBeHitBy(this);
	}
	
	/**
	 * Returns whether the player can see the specified entity (flag is always visible). Returns false if the player or entity isn't currently visible.
	 * @param entity
	 * @return
	 */
	public boolean canSee(Entity entity)
	{
		if(!isVisible())
		{
			return false;
		}
		
		if(entity instanceof Flag)
		{
			return true;
		}
		else if(!entity.isVisible())
		{
			return false;
		}
		return !_world.getMap().isCrossingWall(getPoint(), entity.getPoint());
	}
	
	/**
	 * Returns the distance traveled per frame the player, which is directly related to its health. Returns -1 if the player isn't currently visible.<br /><br />
	 * 
	 * distancePerFrame = 5.0 (or 2.5 if the player is the flag holder)<br />
	 * speed = (distancePerFrame/2.0)*((Player.MAX_HEALTH - getHealth()) / Player.MAX_HEALTH)
	 * 
	 * @return
	 */
	public double getSpeed()
	{
		if(!isVisible())
		{
			return -1;
		}
		double dist = 5.0;
		if(isFlagHolder())
		{
			dist = dist/2.0;
		}
		return dist - (dist/2.0)*((Player.MAX_HEALTH - getHealth()) / Player.MAX_HEALTH);
	}
	
	/**
	 * Returns whether the current player is within the specified player hit range (according to ThrowAction.MAX_DISTANCE, ThrowAction.MIN_HIT_DISTANCE, canSee).
	 * @param player
	 * @return
	 */
	public boolean isInHitRange(Player player)
	{
		if(!canSee(player) || !player.isVisible())
		{
			return false;
		}
		
		return utilities.maths.Math.getEuclidianDistance(this.getX(), this.getY(), player.getX(), player.getY()) <= ThrowAction.MAX_DISTANCE + ThrowAction.MIN_HIT_DISTANCE && this.canSee(player);
	}
}
