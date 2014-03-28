import java.io.IOException;
import java.util.ArrayList;

import utilities.maths.ParametricLine;
import world.Gang;
import world.Player;
import world.PlayerState;
import world.Point;
import world.Snowball;
import world.World;
import world.action.Action;
import world.action.DropFlagAction;
import world.action.MoveAction;
import world.action.PickFlagAction;
import world.action.ThrowAction;


public class ConcreteGang extends Gang {

	private long _previous;
	
	public ConcreteGang(int port, String name) throws IOException
	{
		super(port, name);
	}
	
	int wawaFrameShit = 0;
	
	@Override
	public ArrayList<Action> compute(World world) {
		long current = System.nanoTime();
		
		//System.out.println("Nanos : " + (current - _previous));
		
		_previous = current;
		
		/*if(getTeamId() == 0)
		{
			System.out.println(world.getCurrentFrame());
		}*/
		
		ArrayList<Action> actions = new ArrayList<Action>();
		
		/*System.out.println("CURRENT " + getTeamId());
		for(Team t : world.getOtherTeams())
		{
			System.out.println("alive " + t.getAlivePlayersCount());
		}*/
		
		/*if(getTeamId() == 0)
		{
			wawaFrameShit++;
		}
		if(wawaFrameShit == 100)
		{
			int wawa = 0;
			int toto = wawaFrameShit / wawa;
		}*/
		
		/*if(getTeamId() == 0)
		{
			for(Snowball ball : world.getSnowballs())
			{
				if(ball.isVisible())
				{					
					System.out.println("VISIBLE " + utilities.maths.Math.getEuclidianDistance(world.getCurrentTeam().getPlayers()[0].getX(), world.getCurrentTeam().getPlayers()[0].getY(), ball.getX(), ball.getY()));
				}
			}
		}*/
		
		/*if(getTeamId() == 0)
		{
			for(Snowball ball : world.getSnowballs())
			{
				if(ball.isVisible())
				{					
					System.out.println("VISIBLE " + utilities.maths.Math.getEuclidianDistance(world.getCurrentTeam().getPlayers()[0].getX(), world.getCurrentTeam().getPlayers()[0].getY(), ball.getX(), ball.getY()));
				}
			}
		}
		
		
		if(world.getOtherTeams()[0].getPlayers()[0].isVisible())
		{
			
			System.out.println("WAWA " + utilities.maths.Math.getEuclidianDistance(world.getCurrentTeam().getPlayers()[0].getX(), world.getCurrentTeam().getPlayers()[0].getY(), world.getOtherTeams()[0].getPlayers()[0].getX(), world.getOtherTeams()[0].getPlayers()[0].getY()));
			
			
		}*/
		
		
		/*if(getTeamId() == 0)
		{
			for(Snowball s : world.getSnowballs())
			{
				for(Player player : world.getTeams()[1].getPlayers())
				{
					if(s.canHit(player.getX(), player.getY()))
					{
						System.out.println("OMGOMGOMG I WILL BE HIT LOLE");
					}
				}
			}
		}*/
		
		/*for(Player player : world.getTeams()[getTeamId()].getPlayers())
		{
			if(getTeamId() == 0)
			{
				if(player.getPlayerState().getStateType() == PlayerState.StateType.Idle)
				{
					Point p;
					
					do
					{
						p = new Point(Math.random() * world.getMap().getWidth(), Math.random() * world.getMap().getHeight());
					}while(world.getMap().isPointInWall(p) && Math.sqrt(Math.pow(player.getX() - p.getX(),2) + Math.pow(player.getY() - p.getY(),2)) > 50);
					
					actions.add(new ThrowAction(player, p));
				}
			}
		}*/
		
		/*for(Player player : world.getCurrentTeam().getPlayers())
		{
			if(player.getId() == 0)
			{
				if(player.getPlayerState().getStateType() == StateType.Idle && player.getPlayerState().getPendingAction() == null)
				{
					System.out.println("IDLE!! " + player.getPlayerState().getPendingAction());
					
					Point p;
					
					do
					{
						p = new Point(Math.random() * world.getMap().getWidth(), Math.random() * world.getMap().getHeight());
					}while(world.getMap().isPointInWall(p) && Math.sqrt(Math.pow(player.getX() - p.getX(),2) + Math.pow(player.getY() - p.getY(),2)) > 50);
					
					actions.add(new MoveAction(player, p));
				}
				else
				{
					System.out.println("NOT IDLE!! " + player.getPlayerState().getPendingAction());
				}
			}
		}*/
		
		/*for(Team t : world.getOtherTeams())
		{
			for(Player p : t.getPlayers())
			{
				System.out.println(p.getPlayerState().getCurrentAction());
				System.out.println(p.getPlayerState().getPendingAction());
			}
		}*/
		
		/*if(getTeamId() == 0)
		{
			if(world.getCurrentTeam().getPlayers()[0].getPlayerState().getStateType() == StateType.Idle &&
					world.getCurrentTeam().getPlayers()[0].getPlayerState().getPendingAction() == null)
			{
				Point p = new Point(Math.random() * world.getMap().getWidth(), Math.random() * world.getMap().getHeight());
				actions.add(new ThrowAction(world.getCurrentTeam().getPlayers()[0],p));
				System.out.println(utilities.maths.Math.getEuclidianDistance(world.getCurrentTeam().getPlayers()[0].getX(), world.getCurrentTeam().getPlayers()[0].getY(), p.getX(), p.getY()));
			}	
		}*/
		
		for(Player player : world.getCurrentTeam().getPlayers())
		{
			// if no one holds the flag and the flag can be picked (can only be picked if 20% of the players are dead)
			if(world.getFlag().getHolder() == null && world.canPickFlag())
			{
				// if the player is close enough, we pick it, else we move to it. distance to pick flag is <= 10				
				if(utilities.maths.Math.getEuclidianDistance(player.getPoint(), world.getFlag().getPoint()) <= 10.0)
				{
					actions.add(new PickFlagAction(player));
				}
				else if(!(player.getPlayerState().getStateType() == PlayerState.StateType.Moving
						&& ((MoveAction)(player.getPlayerState().getCurrentAction())).getDestination().getX() == world.getFlag().getX()
						&& ((MoveAction)(player.getPlayerState().getCurrentAction())).getDestination().getY() == world.getFlag().getY()))
				{
					// if the player is not going to the flag, send him to the flag
					actions.add(new MoveAction(player, world.getFlag().getPoint()));
				}
			}
			else
			{
				// if the holder is from the OTHER team, we chase him! (follow stupidly)
				if(world.getFlag().getHolder() != null && world.getFlag().getHolder().getTeam().getId() != getTeamId())
				{
					// chase
					actions.add(new MoveAction(player,world.getFlag().getHolder().getPoint()));
				}
				else if(player.isFlagHolder() && player.getPoint().getX() == world.getTeams()[getTeamId()].getStartingPosition().getX() && player.getPoint().getY() == world.getTeams()[getTeamId()].getStartingPosition().getY())
				{
					// if the player is holding the flag and is on the starting position, we drop the flag to win the game
					actions.add(new DropFlagAction(player));
				}
				else if(player.isFlagHolder() && player.getPlayerState().getStateType() != PlayerState.StateType.Moving)
				{
					// if the player is the flag holder and is not moving
					
					// if the player is not at the starting point, we move it there
					if(player.getPoint().getX() != world.getTeams()[getTeamId()].getStartingPosition().getX() || player.getPoint().getY() != world.getTeams()[getTeamId()].getStartingPosition().getY())
					{
						actions.add(new MoveAction(player, world.getTeams()[getTeamId()].getStartingPosition()));
					}
				}
				else
				{
					if(player.getPlayerState().getStateType() == PlayerState.StateType.Idle && player.getPlayerState().getPendingAction() == null)
					{
						// if the player isn't doing anything and has no pending actions, do something random!!! Throw or move!
						Point p;
						
						do
						{
							// random point
							p = new Point(Math.random() * world.getMap().getWidth(), Math.random() * world.getMap().getHeight());
							
							// while the point is not in a wall and far enough from the player
						}while(world.getMap().isPointInWall(p));
						
						if(Math.random() * 2 < 0.5)
						{
							actions.add(new MoveAction(player,p));
						}
						else
						{
							actions.add(new ThrowAction(player,p));
						}
					}	
				}
			}
			
			// examples of helpers you can use!!! read the doc!
			for(Player otherPlayer : world.getOtherTeams()[0].getPlayers())
			{
				player.canHit(otherPlayer);
				player.canBeHitBy(otherPlayer);
				player.wouldHitPlayer(otherPlayer, new Point(500,500));
				player.canSee(otherPlayer);
			}
			
			for(Snowball snowball : world.getSnowballs())
			{
				snowball.canHit(player);
			}
			
			ParametricLine line1 = utilities.maths.Math.getLine(player.getPoint(), world.getFlag().getPoint());
			ParametricLine line2 = utilities.maths.Math.getLine(new Point(0,0), new Point(1000, 1000));
			line1.intersect(line2);
			line1.getX(0.0);
			line1.getX(0.5);
			line1.getX(1.0);
			
			world.getMap().isCrossingWall(new Point(0,0), new Point(1000,1000));
			world.getMap().isPointInWall(50.0, 50.0);
			// end of the examples
		}
		
		/*for(Player p : world.getCurrentTeam().getPlayers())
		{
			if(p.getId() == 0)
			{
				System.out.println(world.getOtherTeams()[0].getPlayers()[0].getY());
				System.out.println(p.wouldHitPlayer(world.getOtherTeams()[0].getPlayers()[0], new Point(500,650)));
			}
			if(p.getPlayerState().getStateType() == StateType.Idle &&
					p.getPlayerState().getPendingAction() == null)
			{
				int action = (int)(Math.random()*5);
				switch(action)
				{
				case 0:
					actions.add(new DropFlagAction(p));
					break;
				case 1:
					actions.add(new IdleAction(p));
					break;
				case 2:
					actions.add(new MoveAction(p, new Point(Math.random() * world.getMap().getWidth(), Math.random() * world.getMap().getHeight())));
					break;
				case 3:
					actions.add(new PickFlagAction(p));
					break;
				case 4:
					actions.add(new ThrowAction(p, new Point(Math.random() * world.getMap().getWidth(), Math.random() * world.getMap().getHeight())));
				}
			}
		}*/
		
		
		/*for(Player p : world.getCurrentTeam().getPlayers())
		{
			if(p.getId() == 0)
			{
				if(p.getPlayerState().getStateType() == StateType.Idle &&
						p.getPlayerState().getPendingAction() == null)
				{
					//actions.add(new ThrowAction(p, new Point(500,1500)));
					
					actions.add(new MoveAction(p, new Point(250, 1490)));
				}
				
				//for(Snowball s : world.getSnowballs())
				//{
				//	System.out.println(s.canHit(world.getOtherTeams()[0].getPlayers()[0]));
				//}
				
				if(p.canSee(world.getOtherTeams()[0].getPlayers()[0]))
				{
					System.out.println("can see player");
				}
				
				for(Snowball s : world.getSnowballs())
				{
					s.canHit(p);
					
					if(p.canSee(s))
					{
						//System.out.println(p.getX() + " " + p.getY() + " " + s.getX() + " " + s.getY());
						System.out.println("can see snowball");
					}
					
					if(p.canBeHitBy(s))
					{
						System.out.println("WILL BE HIT BY SNOWBALL");
					}
				}
			}
		}*/
		
		/*for(Player p : world.getCurrentTeam().getPlayers())
		{
			if(p.getId() == 10)
			{
				if(p.getPlayerState().getStateType() == StateType.Idle &&
						p.getPlayerState().getPendingAction() == null)
				{
					actions.add(new ThrowAction(p, new Point(0,1450)));
					//System.out.println("new");
				}
				
				if(p.getPlayerState().getStateType() == StateType.Throwing)
				{
					//System.out.println("remaining " + ((ThrowAction)p.getPlayerState().getCurrentAction()).getRemainingFrames());
				}
			}
			else if(p.getId() == 11)
			{
				if(p.getPlayerState().getStateType() == StateType.Idle &&
						p.getPlayerState().getPendingAction() == null)
				{
					actions.add(new ThrowAction(p, new Point(10000,1450)));
				}
			}
		}*/
		
		/*Point p;
		// null
		System.out.println(utilities.maths.Math.getLine(0, 0, 0, 100).intersect(utilities.maths.Math.getLine(50, 100, 50, 100)));
		
		// 0, -20
		ParametricLine l = utilities.maths.Math.getLine(0, 0, 0, 100);
		p = l.intersect(utilities.maths.Math.getLine(-20, -20, 20, -20));
		System.out.println(p.getX() + " " + p.getY());
		System.out.println("TRUE " + l.isXInBounds(p.getX()));
		System.out.println("FALSE " + l.isYInBounds(p.getY()));
		
		// 0, 100
		p = utilities.maths.Math.getLine(0, 0, 0, 100).intersect(utilities.maths.Math.getLine(-50, 100, 50, 100));
		System.out.println(p.getX() + " " + p.getY());
		
		// 50, 50
		p = utilities.maths.Math.getLine(0, 0, 100, 100).intersect(utilities.maths.Math.getLine(0, 100, 100, 0));
		System.out.println(p.getX() + " " + p.getY());
		*/
		return actions;
	}

	boolean _move = false;
}
