package world;

/**
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class Flag extends Entity implements Cloneable {

	private Player _holder;
	
	public Player getHolder()
	{
		return _holder;
	}
	
	public void setHolder(Player holder)
	{
		_holder = holder;
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		Flag f = new Flag();
		if(_holder != null)
		{
			f.setHolder((Player)_holder.clone());
		}
		f.setX(this.getX());
		f.setY(this.getY());
		return f;
	}

	public String toString()
	{
		String str = getX() + " " + getY() + " ";
		if(_holder != null)
		{
			str += _holder.getId();
		}
		else
		{
			str += -1;
		}
		return str;
	}
}
