package rental.g3;

public abstract class ActionGenerator {
	public ActionGenerator(Game g) { game = g; }
	
	public abstract DriveBuilder[] genDriveRide();
	
	protected Game game;
}