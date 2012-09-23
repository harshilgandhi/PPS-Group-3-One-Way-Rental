package rental.sim;

import java.util.HashSet;

public class Drive {

	int car;
	int group;
	int driver;
	boolean deposit;
	String source;
	String destination;
	HashSet <RGid> passengers;
	HashSet <RGid> movedPassengers;

	public Drive(int driver, int car, boolean deposit,
	            RGid[] passengers, String destination)
	{
		this.driver = driver;
		this.car = car;
		this.group = -1;
		this.deposit = deposit;
		this.destination = destination;
		this.passengers = new HashSet <RGid> ();
		this.movedPassengers = new HashSet <RGid> ();
		for (RGid rgid : passengers)
			this.passengers.add(rgid);
	}

	String toStringSim()
	{
		StringBuffer buf = new StringBuffer("Car: " + car + " | " + destination + " | Driver: " + driver);
		if (!passengers.isEmpty())
			buf.append(" | Passengers: ");
		for (RGid rgid : passengers)
			buf.append(" " + rgid);
		return buf.toString();
	}
}
