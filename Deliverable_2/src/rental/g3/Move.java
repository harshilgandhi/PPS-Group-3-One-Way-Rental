package rental.g3;

import java.util.HashSet;
import rental.sim.RGid;

// record the move of each step
class Move {
	int car;
	int driver;
	boolean deposit;
	RGid[] passengers;
	int destination;
	
	public Move(int driver, int car, boolean deposit,
	            RGid[] passengers, int destination)
	{
		this.driver = driver;
		this.car = car;
		this.deposit = deposit;
		this.destination = destination;
		HashSet <RGid> passengerSet = new HashSet <RGid> ();
		for (RGid rgid : passengers)
			passengerSet.add(rgid);
		this.passengers = passengerSet.toArray(new RGid[0]);
	}
}
