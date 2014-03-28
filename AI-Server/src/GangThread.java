
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import world.Player;
import world.Point;
import world.Team;
import world.action.Action;
import world.action.DropFlagAction;
import world.action.IdleAction;
import world.action.MoveAction;
import world.action.PickFlagAction;
import world.action.ThrowAction;

public class GangThread extends Thread {

	private Socket _socket;
	private OutputStream _writer;
	private BufferedReader _reader;

	private List<Action> _actions;

	private int _teamId;
	private Team _team;

	private byte[] _worldUpdates;

	private byte[] _playerVisibilities;
	private byte[] _visibleSnowballs;

	public GangThread(Socket socket, Team team, List<Action> actions,
			ArrayList<Byte> worldUpdates, byte[] playerVisibilities,
			byte[] visibleSnowballs) throws IOException {
		_worldUpdates = new byte[worldUpdates.size()];
		for (int i = 0; i < worldUpdates.size(); i++) {
			_worldUpdates[i] = worldUpdates.get(i);
		}

		_team = team;
		_actions = actions;
		_socket = socket;
		_teamId = team.getId();
		_playerVisibilities = playerVisibilities;
		_visibleSnowballs = visibleSnowballs;
		_writer = _socket.getOutputStream();
		_reader = new BufferedReader(new InputStreamReader(
				_socket.getInputStream()));
	}

	@Override
	public void run() {

		try {
			_writer.write(_worldUpdates);
			_writer.write(_playerVisibilities);
			_writer.write(_visibleSnowballs);

			// should be actions that are added to a (synchronized) stack!!!!
			String str = _reader.readLine();

			if(str == null)
			{
				// python client crash
				throw new SocketException();
			}
			
			// parse commands... would be faster if it wasn't strings
			String[] actionsStr = str.split(",");
			List<Action> newActions = new ArrayList<Action>();
			for (int i = 0; i < actionsStr.length; i++) {
				String[] args = actionsStr[i].split(" ");

				if (args.length > 2 && Integer.parseInt(args[1]) == _teamId) {
					int playerId = Integer.parseInt(args[2]);

					if (args[0].equals("m")) {
						// move command
						double destX = Double.parseDouble(args[3]);
						double destY = Double.parseDouble(args[4]);

						newActions.add(new MoveAction(_teamId, playerId,
								new Point(destX, destY)));
					} else if (args[0].equals("t")) {
						// throw command
						double destX = Double.parseDouble(args[3]);
						double destY = Double.parseDouble(args[4]);

						newActions.add(new ThrowAction(_teamId, playerId,
								new Point(destX, destY)));
					} else if (args[0].equals("p")) {
						newActions.add(new PickFlagAction(_teamId, playerId));
					} else if (args[0].equals("d")) {
						newActions.add(new DropFlagAction(_teamId, playerId));
					} else if (args[0].equals("i")) {
						newActions.add(new IdleAction(_teamId, playerId));
					}
				}
			}

			if (newActions.size() > 0) {
				synchronized (_actions) {
					_team.setFrameCount(_team.getFrameCount() + 1);
					_actions.addAll(newActions);
				}
			}

		} catch (SocketException se) {
			se.printStackTrace();
			// the team loses automatically
			for(Player p : _team.getPlayers())
			{
				p.kill();
			}			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

}
