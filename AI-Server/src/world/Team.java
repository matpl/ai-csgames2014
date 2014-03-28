package world;

/**
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class Team implements Cloneable {

	private Player[] _players;
	
	private int _teamId;
	
	private int _frameCount;
	
	private Point _startingPosition;
	
	private String _name = "";
	
	public Team(int teamId, Point startingPosition, Player[] players)
	{
		this(teamId, startingPosition);
		_players = players;
	}
	
	public Team(int teamId, Point startingPosition)
	{
		_startingPosition = startingPosition; 
		_teamId = teamId;
	}
	
	public int getId()
	{
		return _teamId;
	}
	
	public Point getStartingPosition()
	{
		return _startingPosition;
	}
	
	public Player[] getPlayers()
	{
		return _players;
	}
	
	public void setPlayers(Player[] players)
	{
		_players = players;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void setName(String name)
	{
		this._name = name;
	}
	
	public Player getPlayerById(int playerId)
	{
		for(int i = 0; i < _players.length; i++)
		{
			if(_players[i].getId() == playerId)
			{
				return _players[i]; 
			}
		}
		return null;
	}
	
	public int getFrameCount()
	{
		return _frameCount;
	}
	
	public void setFrameCount(int frameCount)
	{
		this._frameCount = frameCount;
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		//TODO
		
		Player[] players = new Player[_players.length];
		for(int i = 0; i < _players.length; i++)
		{
			players[i] = (Player)_players[i].clone();
		}
		Team t = new Team(_teamId, (Point) _startingPosition.clone(), players);
		t.setName(_name);
		return t;
	}
}
