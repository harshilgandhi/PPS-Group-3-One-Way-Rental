package rental.sim;

public abstract class Player {

	// moving ids
	private static int globalID = 0;

	// id of player
	public final int id;

	// constructor for base class
	public Player()
	{
		id = globalID++;
	}

	// name of group
	public String name()
	{
		return "N/A";
	}

	// set relocator initial positions
	// save information arrays in variables
	// to re-use them throughout the game
	public abstract String[] place(int relocators,     // relocators of your company
	                               String[] carLocations,
	                               String[] carDestinations,
	                               Edge[] edges,       // edges of the map
	                               int groups)         // total number of groups
	                               throws Exception;

	// publish offers
	public abstract Offer[] offer() throws Exception;

	// add replies to offers
	// all offers are givevn
	// do NOT reply to your own offers
	public abstract void request(Offer[] offers) throws Exception;

	// set acknowledgement to your offers
	// if not simulator sets to false
	// do NOT verify offers made by others
	public abstract void verify() throws Exception;

	// publish intention to ride and drive
	public abstract Player.DriveRide action() throws Exception;

	// a class to return drives and rides together
	public static class DriveRide {

		public Drive[] drive;
		public Ride[] ride;

		public DriveRide(Drive[] drive, Ride[] ride)
		{
			this.drive = drive;
			this.ride = ride;
		}
	}
}
