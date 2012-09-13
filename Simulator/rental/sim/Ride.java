package rental.sim;

public class Ride {

	// public information
	public final int rid;            // relocator id
	public final int company;        // company to move with
	public final String destination; // destination

	// set by simulator
	int gid;                   // group of the rider relocator
	boolean executed = false;  // was the ride executed ?
	boolean waiting = true;    // waiting to execute

	public Ride(int rid, int company, String destination)
	{
		this.rid = rid;
		this.company = company;
		this.destination = destination;
	}

	public boolean executed() throws Exception
	{
		if (waiting)
			throw new Exception("Ride has not been evaluated yet");
		return executed;
	}
}
