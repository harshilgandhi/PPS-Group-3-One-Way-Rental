package rental.sim;

import java.util.HashSet;

public class Drive {

	// info
	int driver;
	int car;
	boolean deposit;
	String destination;
	RGid[] passengers;
	int taken;

	public Drive(int driver, int car, boolean deposit,
	            RGid[] passengers, String destination)
	{
		this.driver = driver;
		this.car = car;
		this.deposit = deposit;
		this.destination = destination;
		this.taken = 0;
		HashSet <RGid> passengerSet = new HashSet <RGid> ();
		for (RGid rgid : passengers)
			passengerSet.add(rgid);
		this.passengers = passengerSet.toArray(new RGid[0]);
	}
}
