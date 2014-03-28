package world;

import java.util.ArrayList;

import world.action.ThrowAction;

/**
 * Provides everything related to the snowball fight: map, teams, snowballs, flag, etc.
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class World {

	/**
	 * Total number of frames in a full length game -> 9000<br />
	 * 9000 frames at 30 fps -> 5 minutes.
	 */
	public static int MAX_FRAME_COUNT = 9000;
	
	/**
	 * Visibility radius of players in advanced mode -> 225
	 */
	public static double ADVANCED_VISIBILITY_RADIUS = 225.0;
	
	private Team[] _teams;
	private Map _map;
	private Flag _flag;
	private boolean _canPickFlag;
	private int _currentTeamId;
	private int _currentFrame = 0;

	private ArrayList<Snowball> _snowballs;

	public World(Team[] teams, Map map, Flag flag, ArrayList<Snowball> snowballs) {
		_teams = teams;
		_map = map;
		_flag = flag;
		_snowballs = snowballs;
	}

	/**
	 * Returns the map of the game.
	 * @return
	 */
	public Map getMap() {
		return _map;
	}

	/**
	 * Returns the flag of the game.
	 * @return
	 */
	public Flag getFlag() {
		return _flag;
	}

	/**
	 * Returns the array of teams.
	 * @return
	 */
	public Team[] getTeams() {
		return _teams;
	}
	
	/**
	 * Returns an array containing all teams other than the current one.
	 * @return
	 */
	public Team[] getOtherTeams()
	{
		Team[] teams = new Team[_teams.length - 1];
		int pos = 0;
		for(Team t : _teams)
		{
			if(t.getId() != this._currentTeamId)
			{
				teams[pos] = t;
				pos++;
			}
		}
		return teams;
	}
	
	/**
	 * Returns the current team.
	 * @return
	 */
	public Team getCurrentTeam()
	{
		return _teams[_currentTeamId];
	}

	/**
	 * Returns the current frame of the game
	 * @return
	 */
	public int getCurrentFrame()
	{
		return _currentFrame;
	}
	
    protected void setCurrentFrame(int currentFrame)
	{
		this._currentFrame = currentFrame;
	}
	
    /**
     * Returns a list of snowballs visible to the current team.
     * @return
     */
	public ArrayList<Snowball> getSnowballs() {
		return _snowballs;
	}
	
	protected void setCurrentTeamId(int teamId)
	{
		this._currentTeamId = teamId;
	}
	
	/**
	 * Returns whether the flag can/can't be picked by players.
	 * The flag can be picked if the proportion of dead players >= PickFlagAction.DEAD_PROPORTION_TO_PICK, or if the game is halfway done.
	 * @return
	 */
	public boolean canPickFlag()
	{
		return _canPickFlag;
	}
	
	protected void setCanPickFlag(boolean canPickFlag)
	{
		_canPickFlag = canPickFlag;
	}
}
