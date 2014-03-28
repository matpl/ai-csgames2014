import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ui.UIManager;
import utilities.graph.Dijkstra;
import utilities.graph.Edge;
import utilities.graph.PathList;
import utilities.graph.Vertex;
import world.Map;
import world.Player;
import world.PlayerState;
import world.Point;
import world.Snowball;
import world.Team;
import world.Wall;
import world.World;
import world.action.Action;
import world.action.DropFlagAction;
import world.action.IdleAction;
import world.action.MoveAction;
import world.action.PickFlagAction;
import world.action.ThrowAction;

public class GameLoop extends TimerTask {
	
	private static double ADVANCED_VISIBILITY_RADIUS = 225.0;
	
	private Socket[] _sockets;
	private PrintWriter[] _writers;
	private Thread[] _aiThreads;

	private World _world;

	private UIManager _uiManager;

	private PathList[] _shortestPaths;

	private ServerSocket gameLoopServerSocket;

	private List<Action> _actions;

	private String[] _teamVisibilities;
	private int[] _teamFrameCount;
	private ArrayList<Byte[]> _visibilities;

	private byte[][] _playerVisibilities;
	private ArrayList<HashSet<Integer>> _snowballVisibilities;
	private ArrayList<byte[]> _byteSnowballVisibilities;

	private boolean[] _canExecuteAction;

	private ArrayList<Byte> _frameUpdate;

	private boolean _verbose;
	
	private Timer _timer;
	
	private boolean _corrector;
	
	// replay stuff
	private String _replay;
	private boolean _isReplay;
	private ArrayList<SimpleEntry<Integer, Action>> _replayActions = new ArrayList<SimpleEntry<Integer, Action>>();

	public GameLoop(World world, boolean verbose, int port, boolean corrector, String replay, Timer timer) throws IOException {
		_world = world;
		_verbose = verbose;
		_replay = replay;
		_isReplay = _replay != null;
		
		_corrector = corrector;
		
		_timer = timer;

		_canExecuteAction = new boolean[_world.getTeams().length
				* _world.getTeams()[0].getPlayers().length];

		_frameUpdate = new ArrayList<Byte>();

		_playerVisibilities = new byte[_world.getTeams().length][_world
				.getTeams()[0].getPlayers().length
				* (_world.getTeams().length - 1)];
		_snowballVisibilities = new ArrayList<HashSet<Integer>>();
		_byteSnowballVisibilities = new ArrayList<byte[]>();

		_visibilities = new ArrayList<Byte[]>();
		for (int i = 0; i < _world.getTeams().length; i++) {
			_visibilities.add(new Byte[_world.getTeams()[0].getPlayers().length
					* _world.getTeams().length]);
			_snowballVisibilities.add(new HashSet<Integer>());
		}

		_teamVisibilities = new String[_world.getTeams().length];
		_teamFrameCount = new int[_world.getTeams().length];
		
		
		computeShortestPaths();

		int playerCount = _world.getTeams().length;

		_actions = Collections.synchronizedList(new ArrayList<Action>());

		if(!_corrector)
		{
			// only shows the UI if we are not in corrector mode...
			_uiManager = new UIManager(_world);
			_uiManager.start();
		}

		_sockets = new Socket[playerCount];
		_writers = new PrintWriter[playerCount];
		_aiThreads = new Thread[playerCount];

		if(!_isReplay)
		{
			// sockets
			gameLoopServerSocket = new ServerSocket(port);
			
			for (int i = 0; i < playerCount; i++) {
				System.out.println("Waiting for player " + (i + 1)
						+ " to connect...");
				_sockets[i] = gameLoopServerSocket.accept();
				System.out.println("Player " + (i + 1) + " connected!");
	
				_writers[i] = new PrintWriter(_sockets[i].getOutputStream(), true);
				_writers[i].println(_world.toString());
				_writers[i].println(i);
				
				_world.getTeams()[i].setName((new BufferedReader(new InputStreamReader(_sockets[i].getInputStream()))).readLine());
			}
		}
		else
		{
			// parse replay file and build the command list
			try
			{
				File f = new File(replay);
				String [] names = f.getName().split("\\.")[0].split("-");
				for(int i = 0; i < names.length; i++)
				{
					_world.getTeams()[i].setName(names[i]);
				}
				BufferedReader br = new BufferedReader(new FileReader(f));
				String line;
				while ((line = br.readLine()) != null) {
				   // process the line
					String [] elements = line.split(" ");
					if(elements.length > 0)
					{
						int frameNumber = Integer.parseInt(elements[0]);
						Action a = null;
						int team = Integer.parseInt(elements[2]);
						int player = Integer.parseInt(elements[3]);
						switch(elements[1].charAt(0))
						{
						case 'd':
							a = new DropFlagAction(team, player);
							break;
						case 'i':
							a = new IdleAction(team, player);
							break;
						case 'm':
							a = new MoveAction(team, player, new Point(Double.parseDouble(elements[4]), Double.parseDouble(elements[5])));
							break;
						case 'p':
							a = new PickFlagAction(team, player);
							break;
						case 't':
							a = new ThrowAction(team, player, new Point(Double.parseDouble(elements[4]), Double.parseDouble(elements[5])));
							break;
						}
						_replayActions.add(new SimpleEntry<Integer, Action>(frameNumber, a));
					}
				}
				br.close();
			}
			catch(Exception e)
			{
				e.printStackTrace(); 
			}
		}

		updateWorld();
	}
	
	/**
	 * Precomputes the shortest paths when creating the game
	 */
	private void computeShortestPaths()
	{
		ArrayList<Point> points = new ArrayList<Point>();

		Map m = _world.getMap();
		for (Wall w : m.getWalls()) {
			for (Point p : w.getVertices()) {
				points.add(p);
			}
		}
		Vertex[] vertices = new Vertex[points.size()];
		for (int i = 0; i < points.size(); i++) {
			vertices[i] = new Vertex(i + "");
		}

		for (int i = 0; i < points.size(); i++) {
			ArrayList<Edge> edges = new ArrayList<Edge>();
			for (int j = 0; j < points.size(); j++) {
				if (i != j) {
					if (j <= i) {
						// try to find edge to vertices[i] in vertices[j]. if
						// so, add it to edges.
						int cpt = 0;
						boolean found = false;
						while (!found && cpt < vertices[j].adjacencies.length) {
							if (vertices[j].adjacencies[cpt].target == vertices[i]) {
								found = true;
								edges.add(new Edge(vertices[j],
										vertices[j].adjacencies[cpt].weight));
							} else {
								cpt++;
							}
						}
					} else {
						if (!m.isCrossingWall(points.get(i), points.get(j))) {
							edges.add(new Edge(vertices[j], Math.sqrt(Math
									.pow(points.get(i).getX()
											- points.get(j).getX(), 2)
									+ Math.pow(points.get(i).getY()
											- points.get(j).getY(), 2))));
						}
					}
				}
			}

			vertices[i].adjacencies = edges.toArray(new Edge[edges.size()]);
		}

		_shortestPaths = new PathList[points.size()];

		for (int i = 0; i < vertices.length; i++) {
			_shortestPaths[i] = new PathList(points.get(i));
			// System.out.println(vertices[i].adjacencies.length + " wawa");

			// clear the values
			for (Vertex v : vertices) {
				v.minDistance = Double.POSITIVE_INFINITY;
				v.previous = null;
			}

			Dijkstra.computePaths(vertices[i]);

			for (int j = 0; j < vertices.length; j++) {
				if (i != j) {
					List<Point> path = null;
					if (j < i) {
						// the list exists somewhere
						path = new ArrayList<Point>(
								_shortestPaths[j].getPathTo(points.get(i)));
						Collections.reverse(path);

					} else {
						// the list doesn't exist
						List<Vertex> tempList = Dijkstra
								.getShortestPathTo(vertices[j]);
						path = new ArrayList<Point>();
						for (Vertex v : tempList) {
							path.add(points.get(Integer.parseInt(v.name)));
						}
					}
					_shortestPaths[i].setPathTo(points.get(j), path);
				}
			}
		}

		m.setPathList(_shortestPaths);
	}

	private long _previous;

	private Object _lock = new Object();

	@Override
	public void run() {
		synchronized (_lock) {
			int actionCount;
			
			if(_isReplay)
			{
				// add the replay actions
				while(_replayActions.size() > 0 && _replayActions.get(0).getKey() == _world.getFrameCount())
				{
					_actions.add(_replayActions.remove(0).getValue());
				}
			}
			
			synchronized(_actions)
			{
				actionCount = _actions.size();
				for(Team t : _world.getTeams())
				{
					_teamFrameCount[t.getId()] = t.getFrameCount();
				}
			}

			updateWinners();
			if (_world.getWinningTeams() != null
					&& _world.getWinningTeams().size() != 0) {
				
				_timer.cancel();
				_timer.purge();
				
				if(_corrector)
				{
					// save Action.getCommands in a file
					try
					{
						String fileName = "";
						for(int i = 0; i < _world.getTeams().length; i++)
						{
							if(i != 0)
							{
								fileName += "-";
							}
							fileName += _world.getTeams()[i].getName();
						}
						fileName += ".txt";
						
						File file = new File(fileName);
						if(!file.exists())
						{
							file.createNewFile();
						}
						
						PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
						for(String cmd : Action.getCommands())
						{
							out.println(cmd);
						}
						out.close();
					}
					catch(Exception e)
					{
						System.out.println("!!! FAILED AT WRITING RESULT TO FILE !!!");
						e.printStackTrace();
					}
				}
				
				for(int i = 0; i < _world.getWinningTeams().size(); i++)
				{
					if(i != 0)
					{
						System.out.print(" ");
					}
					System.out.print(_world.getWinningTeams().get(i).getName());
				}
				System.out.println(" " + _world.getFrameCount());
				
				if(_corrector)
				{
					System.exit(0);
				}
				
				return;
			}

			long current = System.nanoTime();
			if (_verbose) {
				System.out.println("GameLoop tick: " + (current - _previous)
						/ 1000000.0);
			}
			_previous = current;

			if (_world.getFrameCount() > World.MAX_FRAME_COUNT) {
				// game over
				
				if (_world.getWinningTeams() == null) {
					// compute winning team
					double highestHealth = -1;
					
					ArrayList<Team> winningTeams = new ArrayList<Team>();

					for (int i = 0; i < _world.getTeams().length; i++) {
						double health = 0;
						for (int j = 0; j < _world.getTeams()[i].getPlayers().length; j++) {
							health += _world.getTeams()[i].getPlayers()[j]
									.getHealth();
						}
						if (health > highestHealth) {
							winningTeams.clear();
							highestHealth = health;
							winningTeams.add(_world.getTeams()[i]);
						} else if (health == highestHealth) {
							winningTeams.add(_world.getTeams()[i]);
						}
					}

					_world.setWinningTeams(winningTeams);
				}
				return;
			}

			notifyClients();

			for (int i = 0; i < _canExecuteAction.length; i++) {
				_canExecuteAction[i] = true;
			}

			boolean finished;
			for (int i = actionCount - 1; i >= 0; i--) {
				if ((_actions.get(i).getPlayerId() == -1 || _canExecuteAction[_actions.get(i).getPlayerId()])
						&& _actions.get(i).initialize(_world)
						&& (_actions.get(i).getPlayerId() == -1 || _world.getTeams()[_actions.get(i).getTeamId()].getPlayerById(_actions.get(i).getPlayerId()).getPlayerState().getStateType() != PlayerState.StateType.Dead))
				{
					finished = _actions.get(i).execute(_world);
					if (_actions.get(i).getPlayerId() != -1) {
						_canExecuteAction[_actions.get(i).getPlayerId()] = false;
					}

					// check for a winner
					updateWinners();
					if (_world.getWinningTeams() != null
							&& _world.getWinningTeams().size() != 0) {
						break;
					}
				} else {
					finished = true;
				}
				Action a = _actions.remove(i);
				if (!finished) {
					_actions.add(a);
				}
			}

			updateWorld();

			_world.updateFrameCount();
		}
	}

	private void doubleToBytes(ArrayList<Byte> updates, double d) {
		long l = Double.doubleToLongBits(d);

		// long = 64 bits -> 8 bytes
		for (int k = 0; k < 8; k++) {
			_frameUpdate.add((byte) ((l >> (k * 8)) & 0xFF));
		}
	}

	private void intToBytes(ArrayList<Byte> updates, int i) {
		// int = 32 bits -> 4 bytes
		for (int k = 0; k < 4; k++) {
			_frameUpdate.add((byte) ((i >> (k * 8)) & 0xFF));
		}
	}

	private void intToBytes(ArrayList<Byte> updates, int i, int insertIndex) {
		// int = 32 bits -> 4 bytes
		for (int k = 0; k < 4; k++) {
			_frameUpdate.add(insertIndex + k, (byte) ((i >> (k * 8)) & 0xFF));
		}
	}

	private void computePlayerVisibilities() {
		for (int i = 0; i < _teamVisibilities.length; i++) {
			_teamVisibilities[i] = "";
			for (int j = 0; j < _visibilities.get(i).length; j++) {
				_visibilities.get(i)[j] = 0;
			}
		}

		boolean tempCross;
		for (int i = 0; i < _world.getTeams().length; i++) {
			for (int j = i + 1; j < _world.getTeams().length; j++) {
				for (int k = 0; k < _world.getTeams()[i].getPlayers().length; k++) {
					for (int l = 0; l < _world.getTeams()[j].getPlayers().length; l++) {
						//this works but it's shit
						tempCross = true;
						if(_world.isAdvanced())
						{
							tempCross = utilities.maths.Math.getEuclidianDistance(_world.getTeams()[i].getPlayers()[k].getPoint(), _world.getTeams()[j].getPlayers()[l].getPoint()) <= ADVANCED_VISIBILITY_RADIUS;
						}
						
						tempCross = tempCross && !_world
								.getMap()
								.isCrossingWall(
										_world.getTeams()[i].getPlayers()[k]
												.getPoint(),
										_world.getTeams()[j].getPlayers()[l]
												.getPoint());
						
						if (tempCross) {
							if (_world.getTeams()[i].getPlayers()[k]
									.getPlayerState().getStateType() != PlayerState.StateType.Dead) {
								_visibilities.get(i)[_world.getTeams()[j]
										.getPlayers()[l].getId()] = 1;
							}
							if (_world.getTeams()[j].getPlayers()[l]
									.getPlayerState().getStateType() != PlayerState.StateType.Dead) {
								_visibilities.get(j)[_world.getTeams()[i]
										.getPlayers()[k].getId()] = 1;
							}
						}
					}
				}
			}
		}
	}

	boolean _firstFrame = true;

	private void notifyClients() {
		if(_isReplay)
		{
			return;
		}
		
		int i = 0;
		try {
			for (i = 0; i < _sockets.length; i++) {
				if (_aiThreads[i] == null || !_aiThreads[i].isAlive()) {

					_aiThreads[i] = new GangThread(_sockets[i], _world.getTeams()[i], _actions,
							_frameUpdate, _playerVisibilities[i].clone(),
							_byteSnowballVisibilities.get(i));
					// second loop to improve performance???
					_aiThreads[i].start();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateWorld() {
		// create the update byte array to send to clients
		
		computePlayerVisibilities();

		_frameUpdate.clear();

		intToBytes(_frameUpdate, _world.getFrameCount());
		
		// player stuff
		for (int i = 0; i < _world.getTeams().length; i++) {
			Team t = _world.getTeams()[i];
			intToBytes(_frameUpdate, t.getFrameCount());
			for (int j = 0; j < t.getPlayers().length; j++) {
				Player p = t.getPlayers()[j];

				_frameUpdate.add((byte) p.getId());

				doubleToBytes(_frameUpdate, p.getHealth());
				doubleToBytes(_frameUpdate, p.getX());
				doubleToBytes(_frameUpdate, p.getY());
				doubleToBytes(_frameUpdate, p.getOrientation().getX());
				doubleToBytes(_frameUpdate, p.getOrientation().getY());

				if (p.isFlagHolder()) {
					_frameUpdate.add((byte) 1);
				} else {
					_frameUpdate.add((byte) 0);
				}

				if (p.getPlayerState().getStateType() == PlayerState.StateType.Idle) {
					_frameUpdate.add((byte) 1);
				} else if (p.getPlayerState().getStateType() == PlayerState.StateType.Moving) {
					// provide the destination fucking shit
					_frameUpdate.add((byte) 2);
					doubleToBytes(_frameUpdate, ((MoveAction) p
							.getPlayerState().getCurrentAction())
							.getDestination().getX());
					doubleToBytes(_frameUpdate, ((MoveAction) p
							.getPlayerState().getCurrentAction())
							.getDestination().getY());
				} else if (p.getPlayerState().getStateType() == PlayerState.StateType.Throwing) {
					// provide the destination fucking shit
					_frameUpdate.add((byte) 3);

					doubleToBytes(_frameUpdate, ((ThrowAction) p
							.getPlayerState().getCurrentAction())
							.getDestination().getX());
					doubleToBytes(_frameUpdate, ((ThrowAction) p
							.getPlayerState().getCurrentAction())
							.getDestination().getY());
					intToBytes(_frameUpdate,((ThrowAction) p.getPlayerState().getCurrentAction()).getRemainingFrames());
				} else if (p.getPlayerState().getStateType() == PlayerState.StateType.Dead) {
					_frameUpdate.add((byte) 4);
				}
			}
		}

		// flag stuff
		doubleToBytes(_frameUpdate, _world.getFlag().getX());
		doubleToBytes(_frameUpdate, _world.getFlag().getY());
		if (!_world.canPickFlag()) {
			_frameUpdate.add((byte) 0);
		} else {
			_frameUpdate.add((byte) 1);
		}
		if (_world.getFlag().getHolder() == null) {
			_frameUpdate.add((byte) 0);
		} else {
			_frameUpdate.add((byte) 1);
		}

		// snowball stuff
		int snowballsPosition = _frameUpdate.size();
		int snowballCount = 0;
		for (int i = 0; i < _world.getSnowballs().size(); i++) {
			Snowball s = _world.getSnowballs().get(i);

			if (s.isActive()) {
				snowballCount++;
				intToBytes(_frameUpdate, s.getId());

				// send id, position and direction

				// TODO: WE NEED THE SPEED
				doubleToBytes(_frameUpdate, s.getX());
				doubleToBytes(_frameUpdate, s.getY());
				doubleToBytes(_frameUpdate, s.getOrientation().getX());
				doubleToBytes(_frameUpdate, s.getOrientation().getY());
				doubleToBytes(_frameUpdate, s.getDestination().getX());
				doubleToBytes(_frameUpdate, s.getDestination().getY());
				doubleToBytes(_frameUpdate, s.getDamage());

				// this is the speed...
				doubleToBytes(_frameUpdate, s.getDistancePerFrame());
			}
		}
		intToBytes(_frameUpdate, snowballCount, snowballsPosition);

		// remove inactive snowballs
		for (int i = _world.getSnowballs().size() - 1; i >= 0; i--) {
			if (!_world.getSnowballs().get(i).isActive()) {
				_world.getSnowballs().remove(i);
			}
		}

		_byteSnowballVisibilities.clear();
		int teamSize = _world.getTeams()[0].getPlayers().length;
		for (int i = 0; i < _world.getTeams().length; i++) {
			int currentPos = 0;
			for (int j = 0; j < _visibilities.get(i).length; j++) {
				if (!(j >= teamSize * i && j < teamSize * (i + 1))) {
					_playerVisibilities[i][currentPos] = _visibilities.get(i)[j];
					currentPos++;
				}
			}

			_snowballVisibilities.get(i).clear();

			for (int j = 0; j < _world.getSnowballs().size(); j++) {
				if (!_snowballVisibilities.get(i).contains(
						_world.getSnowballs().get(j).getId())) {
					for (int k = 0; k < teamSize; k++) {
						if (_world.getTeams()[i].getPlayers()[k].getPlayerState().getStateType() != PlayerState.StateType.Dead) {
							if ((!_world.isAdvanced() || utilities.maths.Math.getEuclidianDistance(_world.getSnowballs().get(j).getPoint(),_world.getTeams()[i].getPlayers()[k].getPoint()) <= ADVANCED_VISIBILITY_RADIUS) &&
									!_world.getMap().isCrossingWall(_world.getSnowballs().get(j).getPoint(),_world.getTeams()[i].getPlayers()[k].getPoint())) {
								_snowballVisibilities.get(i).add(_world.getSnowballs().get(j).getId());
								break;
							}
						}
					}
				}
			}
			byte[] array = new byte[_snowballVisibilities.get(i).size() * 4 + 4];

			array[0] = (byte) (_snowballVisibilities.get(i).size() & 0xFF);
			array[1] = (byte) ((_snowballVisibilities.get(i).size() >> 8) & 0xFF);
			array[2] = (byte) ((_snowballVisibilities.get(i).size() >> 16) & 0xFF);
			array[3] = (byte) ((_snowballVisibilities.get(i).size() >> 24) & 0xFF);

			int pos = 4;
			for (int value : _snowballVisibilities.get(i)) {
				array[pos] = (byte) (value & 0xFF);
				array[pos + 1] = (byte) ((value >> 8) & 0xFF);
				array[pos + 2] = (byte) ((value >> 16) & 0xFF);
				array[pos + 3] = (byte) ((value >> 24) & 0xFF);
				pos += 4;
			}

			_byteSnowballVisibilities.add(array);
		}

		// fixed size for player visibilities
		int size = _frameUpdate.size();

		_frameUpdate.add(0, (byte) (size & 0xFF));
		_frameUpdate.add(1, (byte) ((size >> 8) & 0xFF));
		_frameUpdate.add(2, (byte) ((size >> 16) & 0xFF));
		_frameUpdate.add(3, (byte) ((size >> 24) & 0xFF));
	}
	
	private void updateWinners() {
		// if the flag is dropped within 10 of the area
		ArrayList<Team> winners = new ArrayList<Team>();

		int notDead = -1;

		for (int i = 0; i < _world.getTeams().length; i++) {
			if (_world.getFlag().getHolder() == null
					&& utilities.maths.Math.getEuclidianDistance(_world
							.getFlag().getX(), _world.getFlag().getY(), _world
							.getTeams()[i].getStartingPosition().getX(), _world
							.getTeams()[i].getStartingPosition().getY()) <= PickFlagAction.MIN_FLAG_DISTANCE) {
				winners.add(_world.getTeams()[i]);
			}
		}
		if (winners.size() == 0) {
			for (int i = 0; i < _world.getTeams().length; i++) {
				boolean alive = false;
				for (int j = 0; j < _world.getTeams()[i].getPlayers().length; j++) {
					if (_world.getTeams()[i].getPlayers()[j].getPlayerState()
							.getStateType() != PlayerState.StateType.Dead) {
						alive = true;
						break;
					}
				}
				if (alive) {
					if (notDead != -1) {
						notDead = -2;
						break;
					} else {
						notDead = i;
					}
				}
			}

			// -1 means everyone is dead
			if (notDead == -1) {
				for (int i = 0; i < _world.getTeams().length; i++) {
					winners.add(_world.getTeams()[i]);
				}
			} else if (notDead >= 0) {
				// >= 0 means only one team is alive
				winners.add(_world.getTeams()[notDead]);
			}
			// if notDead == -2 -> more than one team is alive
		}

		if (winners.size() != 0) {
			_world.setWinningTeams(winners);
		}
	}

	public World getWorld()
	{
		return _world;
	}
}
