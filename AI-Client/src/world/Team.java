package world;

/**
 * Contains everything related to a team, including the players, the starting position, the number of alive players, etc.
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class Team {

	private Player[] _players;
	private Point _startingPosition;
	private int _teamId;
	private int _frameCount;
	
	private int _alivePlayersCount;
	
	public Team(int teamId, Point startingPosition, Player[] players)
	{
		_players = players;
		_alivePlayersCount = _players.length;
		_startingPosition = startingPosition; 
		_teamId = teamId;
	}
	
	/**
	 * Returns the id of the team.
	 * @return
	 */
	public int getId()
	{
		return _teamId;
	}
	
	/**
	 * Returns the starting position of the team in the map
	 * @return
	 */
	public Point getStartingPosition()
	{
		return _startingPosition;
	}
	
	/**
	 * Returns an array of the players contained in the team
	 * @return
	 */
	public Player[] getPlayers()
	{
		return _players;
	}
	
	/**
	 * Returns a player by its id (null if the id is not valid).
	 * @param playerId
	 * @return
	 */
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
	
	/**
	 * Returns the flag holder (null if no one holds the flag)
	 * @return
	 */
	public Player getFlagHolder()
	{
		for(int i = 0; i < _players.length; i++)
		{
			if(_players[i].isFlagHolder())
			{
				return _players[i]; 
			}
		}
		return null;
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
	 * Returns the number of alive players in the team.
	 * @return
	 */
	public int getAlivePlayersCount()
	{
		return _alivePlayersCount;
	}
	
	protected void setAlivePlayersCount(int alivePlayersCount)
	{
		this._alivePlayersCount = alivePlayersCount;
	}
}
