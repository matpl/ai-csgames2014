package world;

import world.action.Action;

/**
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class PlayerState implements Cloneable {

	private Action _currentAction = null;
	private StateType _stateType;
	
	public Action getCurrentAction()
	{
		return _currentAction;
	}
	
	public StateType getStateType()
	{
		return _stateType;
	}
	
	public void setCurrentAction(Action currentAction)
	{
		_currentAction = currentAction;
	}
	
	public void setStateType(StateType stateType)
	{
		_stateType = stateType;
	}
	
	public PlayerState(StateType stateType, Action currentAction)
	{
		this._stateType = stateType;
		this._currentAction = currentAction;
	}	
	
	public enum StateType
	{
		Idle,
		Moving,
		Throwing,
		Dead
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		if(_currentAction != null)
		{
			return new PlayerState(_stateType, (Action) _currentAction.clone());
		}
		else
		{
			return new PlayerState(_stateType, null);
		}
	}
	
	public String toString()
	{
		String str = _stateType.toString();
		if(_currentAction != null)
		{
			str += " " + _currentAction.toString();
		}
		return str;
	}
}