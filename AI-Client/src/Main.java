import java.lang.reflect.Constructor;

import world.Gang;
import world.Snowball;


public class Main {

	public void Launch(int port, String name)
	{
		try
		{
			Class<?> c = Class.forName(name);
			Constructor<?> constructor = c.getConstructor(int.class, String.class);
			Gang g = (Gang) constructor.newInstance(new Object[] { 
				port, name	
			});
			g.loop();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String [] args)
	{
		String name = "";
		int port = 11114;
		if(args.length == 1)
		{
			name = args[0];
		}
		else if(args.length == 2)
		{
			name = args[0];
			port = Integer.parseInt(args[1]);
		}
		Main m = new Main();
		m.Launch(port, name);
	}
	
}
