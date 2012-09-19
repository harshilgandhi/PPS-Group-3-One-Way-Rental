package rental.g3;

import rental.sim.Player.DriveRide;

public abstract class ActionGenerator {
	public ActionGenerator(Game g) { game = g; }
	
	public abstract DriveRide genDriveRide();
	
	protected Game game;
}