package world;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import world.Flag;
import world.Map;
import world.Player;
import world.PlayerState;
import world.Point;
import world.Snowball;
import world.Team;
import world.Wall;
import world.World;
import world.action.Action;
import world.action.MoveAction;
import world.action.ThrowAction;

/**
 * Represents the game client. To provide an implementation, override the compute method, which is called every 33 ms (30 FPS).
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public abstract class Gang {
	
	private static String _host = "127.0.0.1";
	private int _port;
	
	private Socket _socket;
	private PrintWriter _writer;
	private BufferedReader _reader;
	private InputStream _byteReader;
	
	private World _world;
	
	private int _teamId;
	
	/**
	 * Returns the current team id.
	 * @return
	 */
	public int getTeamId()
	{
		return _teamId;
	}
	
	private ArrayList<Snowball> _snowballs;
	
	public Gang(int port, String name) throws IOException
	{
		_port = port;
		System.out.println("Trying to connect to host...");
		_socket = new Socket(_host, _port);
		System.out.println("Connected!");
		_writer = new PrintWriter(_socket.getOutputStream(), true);
		_reader = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
		_byteReader = _socket.getInputStream();
		
		String[] worldStr = _reader.readLine().split(" ");
		
		_teamId = Integer.parseInt(_reader.readLine());
		
		_writer.println(name);
		
		_snowballs = new ArrayList<Snowball>();
		
		double width = Double.parseDouble(worldStr[0]);
		double height = Double.parseDouble(worldStr[1]);

		Wall[] walls = new Wall[Integer.parseInt(worldStr[2])];
		
		int currentPosition = 3;
		
		for(int i = 0; i < walls.length; i++)
		{
			Point[] vertices = new Point[Integer.parseInt(worldStr[currentPosition])];
			
			currentPosition++;
			
			for(int j = 0; j < vertices.length; j++)
			{
				String [] vertex = worldStr[currentPosition].split(",");
				vertices[j] = new Point(Double.parseDouble(vertex[0]), Double.parseDouble(vertex[1]));
				currentPosition++;
			}
			
			walls[i] = new Wall(vertices);
		}
		/*if(walls.length == 0)
		{
			currentPosition++;
		}*/
		
		Map map = new Map(width, height, walls);
		
		Team[] teams = new Team[Integer.parseInt(worldStr[currentPosition])];
		
		for(int i = 0; i < teams.length; i++)
		{
			currentPosition++;
			Player[] players = new Player[Integer.parseInt(worldStr[currentPosition])];			
			
			currentPosition++;
			String [] startingPositionStr = worldStr[currentPosition].split(",");
			Point startingPosition = new Point(Double.parseDouble(startingPositionStr[0]), Double.parseDouble(startingPositionStr[1]));
			
			for(int j = 0; j < players.length; j++)
			{
				currentPosition++;
				
				players[j] = new Player(Integer.parseInt(worldStr[currentPosition]));
				players[j].setX(startingPosition.getX());
				players[j].setY(startingPosition.getY());
			}
			
			teams[i] = new Team(i, startingPosition, players);
			
			for(Player p : players)
			{
				p.setTeam(teams[i]);
			}
		}
		
		currentPosition++;
		double flagX = Double.parseDouble(worldStr[currentPosition]);
		currentPosition++;
		double flagY = Double.parseDouble(worldStr[currentPosition]);
		
		Flag flag = new Flag();
		flag.setX(flagX);
		flag.setY(flagY);
		_world = new World(teams, map, flag, new ArrayList<Snowball>());
		flag.setWorld(_world);
		for(Team t : teams)
		{
			for(Player p : t.getPlayers())
			{
				p.setWorld(_world);
			}
		}
		_world.setCurrentTeamId(_teamId);
		_containsPlayerAction = new boolean[_world.getTeams()[_teamId].getPlayers().length];
	}
	
	private boolean[] _containsPlayerAction;
	
	public void loop()
	{
		try
		{
			byte[] visibilities = new byte[(_world.getTeams().length - 1) * _world.getTeams()[0].getPlayers().length];
			while(true)
			{
				int count = 0;
				for(int i = 0; i < 4; i++)
				{
					count = (_byteReader.read() << (i*8)) | count;
				}
				
				byte[] updates = new byte[count];
				_byteReader.read(updates);
				
				_byteReader.read(visibilities);
				
				byte[] snowballSize = new byte[4];
				byte[] visibleSnowballs = null;
				_byteReader.read(snowballSize);
				
				int snowballCount = bytesToInt(snowballSize, 0);
				if(snowballCount > 0)
				{
					visibleSnowballs = new byte[snowballCount * 4];
					_byteReader.read(visibleSnowballs);
				}
				
				parseUpdates(updates);
				
				parsePlayerVisibilities(visibilities);
				parseSnowballVisibilities(visibleSnowballs);
				
				//_reader.re
				//String line = _reader.readLine();
				//parseUpdates(line.split(" "));
				
				//int playerCount = Integer.parseInt(args[0]);
				 
				//new World()
				/*int cpt = 1;
				for(int j = 0; j < playerCount)*/
				
				//reconstruction of the world
				
				
				ArrayList<Action> actions = compute(_world);
				
				for(int i = 0; i < _containsPlayerAction.length; i++)
				{
					_containsPlayerAction[i] = false;
				}
				
				for(int i = actions.size() - 1; i >= 0; i--)
				{
					if(actions.get(i).getPlayer().getTeam().getId() != getTeamId() || _containsPlayerAction[actions.get(i).getPlayer().getId() % _world.getTeams()[_teamId].getPlayers().length])
					{
						actions.remove(i);
					}
					else
					{
						_containsPlayerAction[actions.get(i).getPlayer().getId() % _world.getTeams()[_teamId].getPlayers().length] = true;
					}
				}
				
				String cmd = "";
				for(int i = 0; i < actions.size(); i++)
				{
					if(i != 0)
					{
						cmd += ",";
					}

					actions.get(i).getPlayer().getPlayerState().setPendingAction(actions.get(i));
					actions.get(i).getPlayer().getPlayerState().setFrameCount(_world.getCurrentTeam().getFrameCount() + 1);
					
					cmd += actions.get(i).toString();
				}
				
				if(actions.size() != 0)
				{
					_world.getCurrentTeam().setFrameCount(_world.getCurrentTeam().getFrameCount() + 1);
				}
				
				//print actions
				_writer.println(cmd);
			}
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void parseUpdates(/*String [] updates*/byte[] updates)
	{
		_world.setCurrentFrame(bytesToInt(updates, 0));
		int current = 3;
		
		int frameCount;
		for(int i = 0; i < _world.getTeams().length; i++)
		{
			current++;
			frameCount =  bytesToInt(updates, current);
			current += 3;
			_world.getTeams()[i].setFrameCount(frameCount);
			int alivePlayerCount = 0;
			
			
			for(int j = 0; j < _world.getTeams()[i].getPlayers().length; j++)
			{
				current++;
				Player p = _world.getTeams()[i].getPlayers()[j];
				p.setVisible(true);
				// health
				current++;
				p.setHealth(bytesToDouble(updates, current));
				
				// position
				current += 8;
				p.setX(bytesToDouble(updates, current));
				current += 8;
				p.setY(bytesToDouble(updates, current));
				
				// orientation
				current += 8;
				p.getOrientation().setX(bytesToDouble(updates, current));
				current += 8;
				p.getOrientation().setY(bytesToDouble(updates, current));
				
				// flag
				current += 8;
				if(updates[current] == 0)
				{
					p.setFlagHolder(false);
				}
				else
				{
					p.setFlagHolder(true);
					_world.getFlag().setHolder(p);
				}
				
				//state
				current++;
				switch(updates[current])
				{
					case 1:
						// Idle
						p.getPlayerState().setCurrentAction(null);
						p.getPlayerState().setStateType(PlayerState.StateType.Idle);
						
						alivePlayerCount++;
						break;
					case 2:
						// Moving
						p.getPlayerState().setStateType(PlayerState.StateType.Moving);
						current++;
						if(p.getTeam().getId() == _teamId)
						{
							if((p.getPlayerState().getCurrentAction() == null || !MoveAction.class.isInstance(p.getPlayerState().getCurrentAction())))
							{
								p.getPlayerState().setCurrentAction(new MoveAction(p, new Point(bytesToDouble(updates, current), bytesToDouble(updates, current+8))));
							}
						}
						else
						{
							p.getPlayerState().setCurrentAction(null);
						}
						current+=15;
						
						alivePlayerCount++;
						break;
					case 3:
						// Throwing
						p.getPlayerState().setStateType(PlayerState.StateType.Throwing);
						current++;
						if(p.getTeam().getId() == _teamId)
						{
							if(p.getPlayerState().getCurrentAction() == null || !ThrowAction.class.isInstance(p.getPlayerState().getCurrentAction()))
							{
								p.getPlayerState().setCurrentAction(new ThrowAction(p, new Point(bytesToDouble(updates, current), bytesToDouble(updates, current+8))));
							}
							((ThrowAction)(p.getPlayerState().getCurrentAction())).setRemainingFrames(bytesToInt(updates, current + 16));
						}
						else
						{
							p.getPlayerState().setCurrentAction(null);
						}
						current+=19;
						
						alivePlayerCount++;
						break;
					case 4:
						// Dead
						p.getPlayerState().setCurrentAction(null);
						p.getPlayerState().setStateType(PlayerState.StateType.Dead);
						break;
				}
				
				if(i == _teamId && p.getPlayerState().getPendingAction() != null && frameCount >= p.getPlayerState().getFrameCount())
				{
					p.getPlayerState().setPendingAction(null);
				}
			}
			
			_world.getTeams()[i].setAlivePlayersCount(alivePlayerCount);
		}
		
		// flag stuff
		current++;
		_world.getFlag().setX(bytesToDouble(updates, current));
		current+=8;
		_world.getFlag().setY(bytesToDouble(updates, current));
		current+=8;
		
		if(updates[current] == 0)
		{
			_world.setCanPickFlag(false);
		}
		else
		{
			_world.setCanPickFlag(true);
		}
		current++;
		if(updates[current] == 0)
		{
			_world.getFlag().setHolder(null);
		}
		
		// TODO: PARSE SNOWBALLS
		
		current++;
		int snowballCount = bytesToInt(updates, current);
		current += 3;
		
		for(int i = 0; i < _snowballs.size(); i++)
		{
			// little hack... we use visible instead of creating another list...
			_snowballs.get(i).setVisible(false);
		}
		
		for(int i = 0; i < snowballCount; i++)
		{
			current++;
			int id = bytesToInt(updates, current);
			Snowball s = null;
			for(int j = 0; j < _snowballs.size(); j++)
			{
				if(_snowballs.get(j).getId() == id)
				{
					s = _snowballs.get(j);
					break;
				}
			}
			if(s == null)
			{
				s = new Snowball(id, _world);
				_snowballs.add(s);
			}
			s.setVisible(true);
			
			current += 4;
			s.setX(bytesToDouble(updates, current));
			current += 8;
			s.setY(bytesToDouble(updates, current));
			current += 8;
			s.getOrientation().setX(bytesToDouble(updates, current));
			current += 8;
			s.getOrientation().setY(bytesToDouble(updates, current));
			current += 8;
			s.getDestination().setX(bytesToDouble(updates, current));
			current += 8;
			s.getDestination().setY(bytesToDouble(updates, current));
			current += 8;
			s.setDamage(bytesToDouble(updates, current));
			current += 8;
			s.setSpeed(bytesToDouble(updates, current));
			current += 7;
		}
		
		for(int i = _snowballs.size()-1; i >= 0; i--)
		{
			// little hack... we use visible instead of creating another list...
			if(!_snowballs.get(i).isVisible())
			{
				_snowballs.remove(i);
			}
		}
		
		// player visibilities
		/*for(int i = 0; i < _world.getTeams().length; i++)
		{
			for(int j = 0; j < _world.getTeams()[i].getPlayers().length; j++)
			{
				current++;
				if(i != _teamId && updates[current] == 0)
				{
					System.out.println("wawa");
					_world.getTeams()[i].getPlayers()[j].setVisible(false);
				}
			}
		}*/
		
		/*
		// first make all players visible
		for(int i = 0; i < _world.getTeams().length; i++)
		{
			for(int j = 0; j < _world.getTeams()[i].getPlayers().length; j++)
			{
				_world.getTeams()[i].getPlayers()[j].setVisible(true);
			}
		}
		int current = 0;
		while(current < updates.length)
		{
			if(updates[current].equals("m"))
			{
				// move command
				
				int teamId = Integer.parseInt(updates[current+1]);
				int playerId = Integer.parseInt(updates[current+2]);
				double x = Double.parseDouble(updates[current+3]);
				double y = Double.parseDouble(updates[current+4]);
				double destX = Double.parseDouble(updates[current+5]);
				double destY = Double.parseDouble(updates[current+6]);
				double oriX = Double.parseDouble(updates[current+7]);
				double oriY = Double.parseDouble(updates[current+8]);
				
				Player p = _world.getTeams()[teamId].getPlayerById(playerId);
				if(p != null)
				{
					p.setX(x);
					p.setY(y);
					p.getOrientation().setX(oriX);
					p.getOrientation().setY(oriY);
					if(p.getPlayerState().getStateType() != PlayerState.StateType.Moving)
					{
						p.getPlayerState().setStateType(PlayerState.StateType.Moving);
					}
					MoveAction a;
					
					if(p.getPlayerState().getCurrentAction() == null || !MoveAction.class.isInstance(p.getPlayerState().getCurrentAction()))
					{
						p.getPlayerState().setCurrentAction(new MoveAction(p, new Point(destX, destY)));
					}
					a = (MoveAction) p.getPlayerState().getCurrentAction();
					
					if(a.getDestination().getX() != destX || a.getDestination().getY() != destY)
					{
						a.getDestination().setX(destX);
						a.getDestination().setY(destY);
					}
					
					if(p.isFlagHolder())
					{
						_world.getFlag().setX(x);
						_world.getFlag().setY(y);
					}
				}
				current += 9;
			}
			else if(updates[current].equals("i"))
			{
				int teamId = Integer.parseInt(updates[current+1]);
				int playerId = Integer.parseInt(updates[current+2]);
				Player p = _world.getTeams()[teamId].getPlayerById(playerId);
				p.getPlayerState().setCurrentAction(null);
				p.getPlayerState().setStateType(PlayerState.StateType.Idle);
				current += 3;
			}
			else if(updates[current].equals("d"))
			{
				int teamId = Integer.parseInt(updates[current+1]);
				int playerId = Integer.parseInt(updates[current+2]);
				Player p = _world.getTeams()[teamId].getPlayerById(playerId);
				p.setFlagHolder(false);
				_world.getFlag().setHolder(null);
				current += 3;
			}
			else if(updates[current].equals("p"))
			{
				int teamId = Integer.parseInt(updates[current+1]);
				int playerId = Integer.parseInt(updates[current+2]);
				Player p = _world.getTeams()[teamId].getPlayerById(playerId);
				p.setFlagHolder(true);
				_world.getFlag().setHolder(p);
				_world.getFlag().setX(p.getX());
				_world.getFlag().setY(p.getY());
				current += 3;
			}
			else if(updates[current].equals("c"))
			{
				int teamId = Integer.parseInt(updates[current+1]);
				int playerId = Integer.parseInt(updates[current+2]);
				double destX = Double.parseDouble(updates[current+3]);
				double destY = Double.parseDouble(updates[current+4]);
				Player p = _world.getTeams()[teamId].getPlayerById(playerId);
				if(p.getPlayerState().getStateType() != PlayerState.StateType.Throwing)
				{
					p.getPlayerState().setStateType(PlayerState.StateType.Throwing);
				}
				
				if(p.getPlayerState().getCurrentAction() == null || !ThrowAction.class.isInstance(p.getPlayerState().getCurrentAction()))
				{
					p.getPlayerState().setCurrentAction(new ThrowAction(p, new Point(destX, destY)));
				}
				
				ThrowAction a = (ThrowAction) p.getPlayerState().getCurrentAction();
				
				if(a.getDestination().getX() != destX || a.getDestination().getY() != destY)
				{
					a.getDestination().setX(destX);
					a.getDestination().setY(destY);
				}
				
				current += 5;
			}
			else if(updates[current].equals("t"))
			{
				int snowballId = Integer.parseInt(updates[current+1]);
				double x = Double.parseDouble(updates[current+2]);
				double y = Double.parseDouble(updates[current+3]);
				
				Snowball snowball = null;
				for(int i = 0; i < _world.getSnowballs().size(); i++)
				{
					if(_world.getSnowballs().get(i).getId() == snowballId)
					{
						snowball = _world.getSnowballs().get(i);
					}
				}
				
				if(snowball == null)
				{
					snowball = new Snowball(snowballId);
					_world.getSnowballs().add(snowball);
				}
				
				if(snowball.getX() != x)
				{
					snowball.setX(x);
				}
				if(snowball.getY() != y)
				{
					snowball.setY(y);
				}
				
				current += 4;
			}
			else if(updates[current].equals("r"))
			{
				int snowballId = Integer.parseInt(updates[current+1]);
				
				for(int i = 0; i < _world.getSnowballs().size(); i++)
				{
					if(_world.getSnowballs().get(i).getId() == snowballId)
					{
						_world.getSnowballs().remove(i);
						break;
					}
				}
				
				current += 2;
			}
			else
			{
				////player visibilities
				for(int i = 0; i < _world.getTeams().length; i++)
				{
					for(int j = 0; j < _world.getTeams()[i].getPlayers().length; j++)
					{
						if(i != _teamId)
						{
							if(updates[current].equals("0"))
							{
								_world.getTeams()[i].getPlayers()[j].setVisible(false);
							}
							else
							{
								_world.getTeams()[i].getPlayers()[j].setVisible(true);
							}
						}
						current++;
					}
				}
				current++;
			}
		}*/
	}

	private void parsePlayerVisibilities(byte[] visibilities)
	{
		int pos = 0;
		for(int i = 0; i < _world.getTeams().length; i++)
		{
			if(i != _teamId)
			{
				Team t = _world.getTeams()[i];
				for(int j = 0; j < t.getPlayers().length; j++)
				{
					if(visibilities[pos] == 0 && !t.getPlayers()[j].isFlagHolder())
					{
						t.getPlayers()[j].setVisible(false);
					}
					pos++;
				}
			}
		}
	}
	
	private void parseSnowballVisibilities(byte[] visibilities)
	{
		for(int i = 0; i < _snowballs.size(); i++)
		{
			_snowballs.get(i).setVisible(false);
		}
		if(visibilities != null)
		{
			for(int i = 0; i < visibilities.length; i+= 4)
			{
				int no = bytesToInt(visibilities, i);
				for(int j = 0; j < _snowballs.size(); j++)
				{
					if(no == _snowballs.get(j).getId())
					{
						_snowballs.get(j).setVisible(true);
						break;
					}
				}
			}
		}
		
		_world.getSnowballs().clear();
		for(int i = 0; i < _snowballs.size(); i++)
		{
			if(_snowballs.get(i).isVisible())
			{
				_world.getSnowballs().add(_snowballs.get(i));
			}
		}
	}
	
	private double bytesToDouble(byte[] array, int position)
	{
		double d;
		long l = 0;
		
		for(int i = 0; i < 8; i++)
		{
			l += (((long) array[position+i]  & 0xFFL) << (i<<3));
		}
		
		d = Double.longBitsToDouble(l);
		return d;
	}
	
	private int bytesToInt(byte[] array, int position)
	{
		int v = 0;
		
		for(int i = 0; i < 4; i++)
		{
			v += (((int) array[position+i] & 0xFF) << (i<<3));
		}
		
		return v;
	}
	
	/**
	 * The one method that rules them all. It is called every 33 ms (30 FPS). Implement this and return a list of actions. Only one action per player of your own team is permitted. Sending an action for a player will cancel its current one. If you take more than 33 ms to respond, your AI will simply skip a frame. Warning: The actions you send won't necessarily be parsed in the next frame, so make sure to only send an action if the player's getPlayerState().getPendingAction() == null.
	 * @param world
	 * @return
	 */
	public abstract ArrayList<Action> compute(World world);
	
}
