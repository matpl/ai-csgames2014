package world;

/**
 * The flag to bring back to end the game.
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class Flag extends Entity {

	public Flag()
	{
		this.setVisible(true);
	}
	
	private Player _holder;
	
	/**
	 * Returns the current flag holder.
	 * @return
	 */
	public Player getHolder()
	{
		return _holder;
	}
	
	protected void setHolder(Player holder)
	{
		_holder = holder;
	}
}
