import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;

import world.Flag;
import world.Map;
import world.Player;
import world.Point;
import world.Snowball;
import world.Team;
import world.Wall;
import world.World;

/**
 * 
 * @author Mathieu Plourde - mat.plourde@gmail.com
 *
 */
public class Main {
	
	public void Launch(World world, boolean verbose, int port, boolean corrector, String replay)
	{
		try
		{
			Timer t = new Timer();
			t.schedule(new GameLoop(world, verbose, port, corrector, replay, t), 0, 33);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static World parseWorldFile(String fileName, boolean advanced) throws IOException
	{
		World world;
		
		double width = -1;
		double height = -1;
		int teamCount;
		int playerCount;
		ArrayList<Wall> walls = new ArrayList<Wall>();
		Team[] teamList = null;
		Flag flag = null;
		
		BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
		String line;
		String[] tempArray;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if(line.length() > 0)
			{
				tempArray = line.split(" ");
				switch(line.charAt(0))
				{
					case 'm':
						tempArray = tempArray[1].split(",");
						width = Double.parseDouble(tempArray[0]);
						height = Double.parseDouble(tempArray[1]);
						break;
					case 'p':
						teamCount = Integer.parseInt(tempArray[1]);
						teamList = new Team[teamCount];
						playerCount = Integer.parseInt(tempArray[2]);
						for(int i = 0; i < teamCount; i++)
						{
							String[] pos = tempArray[3+i].split(",");
							teamList[i] = new Team(i, new Point(Double.parseDouble(pos[0]), Double.parseDouble(pos[1])));
							
							Player[] playerList = new Player[playerCount];
							for(int j = 0; j < playerCount; j++)
							{
								playerList[j] = new Player(i*playerCount+j, teamList[i]);
								
								playerList[j].setX(teamList[i].getStartingPosition().getX());
								playerList[j].setY(teamList[i].getStartingPosition().getY());
							}
							teamList[i].setPlayers(playerList);
						}
						break;
					case 'w':
						Point[] vertices = new Point[tempArray.length-1];
						for(int i = 0; i < vertices.length; i++)
						{
							String[] pos = tempArray[1+i].split(",");
							vertices[i] = new Point(Double.parseDouble(pos[0]), Double.parseDouble(pos[1]));
						}
						walls.add(new Wall(vertices));
						break;
					case 'f':
						String[] pos = tempArray[1].split(",");
						flag = new Flag();
						flag.setX(Double.parseDouble(pos[0]));
						flag.setY(Double.parseDouble(pos[1]));
						break;
				}
			}
		}
		br.close();
		Wall[] wallsArray = new Wall[walls.size()];
		world = new World(teamList, new Map(width, height, walls.toArray(wallsArray)), flag, new ArrayList<Snowball>(), advanced);
		
		return world;
	}
	
	public static void main(String [] args)
	{
		World world = null;
		boolean verbose = false;
		boolean advanced = false;
		boolean corrector = false;
		String replay = null;
		int port = 11114;
		try
		{
			for(int i = 1; i < args.length; i++)
			 {
				 if(args[i].equals("v"))
				 {
					 verbose = true;
				 }
				 else if(args[i].equals("a"))
				 {
					 advanced = true;
				 
				 }
				 else if(args[i].equals("c"))
				 {
					 corrector = true;
				 }
				 else if(args[i].equals("r"))
				 {
					 replay = args[i + 1];
					 i++;
				 }
				 else
				 {
					 port = Integer.parseInt(args[i]);
				 }
			 }
			
			 world = parseWorldFile(args[0], advanced);
		}catch(IOException ioe)
		{
			System.out.println("ERROR: Unable to parse world file.");
			ioe.printStackTrace();
			System.exit(0);
		}
		
		Main m = new Main();
		m.Launch(world, verbose, port, corrector, replay);		
	}
}
