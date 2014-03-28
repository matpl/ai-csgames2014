package world;

import world.action.Action;

/**
 * Represents the current state of the player. Contains its current action, state type, pending action.
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class PlayerState implements Cloneable {

	// todo: needs way to know if action is completed, or near completion 
	
	private Action _currentAction = null;
	private StateType _stateType;
	
	private Action _pendingAction = null;
	private int _frameCount;
	
	private boolean _visible = false;
	
	/**
	 * Returns the current action being executed by the player. The current action is ALWAYS null if the player doesn't belong to the current team.
	 * @return
	 */
	public Action getCurrentAction()
	{
		if(!_visible)
		{
			return null;
		}
		return _currentAction;
	}
	
	/**
	 * Returns the type of state the player is in. Refer to the StateType enum for details. Returns StateType.Unknown if the player isn't currently visible.
	 * @return
	 */
	public StateType getStateType()
	{
		if(!_visible)
		{
			return StateType.Unknown;
		}
		return _stateType;
	}
	
	protected void setCurrentAction(Action currentAction)
	{
		_currentAction = currentAction;
	}
	
	protected void setStateType(StateType stateType)
	{
		_stateType = stateType;
	}
	
	protected boolean isVisible()
	{
		return _visible;
	}
	
	protected void setVisible(boolean visible)
	{
		this._visible = visible;
	}
	
	public PlayerState(StateType stateType, Action currentAction)
	{
		this._stateType = stateType;
		this._currentAction = currentAction;
	}	
	
	protected void setPendingAction(Action action)
	{
		this._pendingAction = action;
	}
	
	/**
	 * Returns the pending action for the player. A pending action is an action sent to the server that hasn't been processed yet. The pending action is ALWAYS null if the player doesn't belong to the current team.
	 * @return
	 */
	public Action getPendingAction()
	{
		return _pendingAction;
	}
	
	protected void setFrameCount(int count)
	{
		this._frameCount = count;
	}
	
	protected int getFrameCount()
	{
		return this._frameCount;
	}
	
	/**
	 * Different state types for player. The state type depends of what the player is currently doing.
	 * 
	 * @author Mathieu Plourde - mat.plourde@gmail.com
	 *
	 */
	public enum StateType
	{
		/**
		 * Player isn't doing anything. Probably eating snow or something.
		 */
		Idle,
		/**
		 * Player is moving.
		 */
		Moving,
		/**
		 * Player is throwing a snowball (charging his shot).
		 */
		Throwing,
		/**
		 * Player is dead.
		 */
		Dead,
		/**
		 * Player isn't visible to the current team.
		 */
		Unknown
	}
}