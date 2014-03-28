package world;

import java.util.ArrayList;

/**
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class World implements Cloneable {

	public static double DEAD_PROPORTION = 0.2;
	
	// 9000 frames at 30 fps == 5 minutes
	public static int MAX_FRAME_COUNT = 9000;
	
	private Team[] _teams;
	private Map _map;
	private Flag _flag;

	private ArrayList<Snowball> _snowballs;
	
	private ArrayList<Team> _winningTeams;
	
	private boolean _isAdvanced;
	
	private int _frameCount = 0;

	public World(Team[] teams, Map map, Flag flag, ArrayList<Snowball> snowballs, boolean isAdvanced) {
		_teams = teams;
		_map = map;
		_flag = flag;
		_snowballs = snowballs;
		_isAdvanced = isAdvanced;
	}
	
	public void updateFrameCount()
	{
		_frameCount++;
	}
	
	public int getFrameCount()
	{
		return _frameCount;
	}

	public Map getMap() {
		return _map;
	}

	public Flag getFlag() {
		return _flag;
	}
	
	public ArrayList<Team> getWinningTeams()
	{
		return _winningTeams;
	}
	
	public void setWinningTeams(ArrayList<Team> winningTeams)
	{
		_winningTeams = winningTeams;
	}

	public Team[] getTeams() {
		return _teams;
	}
	
	public boolean canPickFlag()
	{
		if(_frameCount >= MAX_FRAME_COUNT / 2)
		{
			return true;
		}
		int total = 0;
		int deadCount = 0;
		for(int i = 0; i < getTeams().length; i++)
		{
			total += getTeams()[i].getPlayers().length;
			for(int j = 0; j < getTeams()[i].getPlayers().length; j++)
			{
				if(getTeams()[i].getPlayers()[j].getPlayerState().getStateType() == PlayerState.StateType.Dead)
				{
					deadCount++;
				}
			}
		}		
		if(deadCount < total * DEAD_PROPORTION)
		{
			// not enough dead players to pick the flag
			return false;
		}
		
		return true;
	}

	public ArrayList<Snowball> getSnowballs() {
		return _snowballs;
	}

	public boolean isAdvanced()
	{
		return _isAdvanced;
	}
	
	protected void setIsAdvanced(boolean isAdvanced)
	{
		_isAdvanced = isAdvanced;
	}
	
	public Object clone() throws CloneNotSupportedException {
		ArrayList<Team> winningTeams = new ArrayList<Team>();
		
		Team[] teams = new Team[_teams.length];

		for (int i = 0; i < _teams.length; i++) {
			teams[i] = (Team) _teams[i].clone();
			if(_winningTeams != null && _winningTeams.contains(_teams[i]))
			{
				winningTeams.add(teams[i]);
			}
		}

		ArrayList<Snowball> snowballs = new ArrayList<Snowball>();
		for (int i = 0; i < _snowballs.size(); i++) {
			snowballs.add((Snowball) _snowballs.get(i).clone());
		}

		World w = new World(teams, (Map) _map.clone(), (Flag) _flag.clone(),
				snowballs, _isAdvanced);
		
		if(winningTeams.size() != 0)
		{
			w.setWinningTeams(winningTeams);
		}
		
		return w;
	}

	public String toString() {
		String str = "";
		
		str += _map.toString();
		
		str += " " + _teams.length;
		
		for (Team t : _teams) {
			str += " ";
			Player[] players = t.getPlayers();
			str += players.length;
			
			str += " " + t.getStartingPosition().getX() + "," + t.getStartingPosition().getY();
			
			for (int i = 0; i < t.getPlayers().length; i++) {
				str += " " + players[i].toString();
			}
		}

		str += " " + _flag.toString();

		return str;
	}
}
