package rental.g3;
import java.util.HashSet;
import java.util.Set;

import rental.sim.Drive;
import rental.sim.RGid;

/**
 * 
 * Class for building Drive objects since Drives are immutable.
 *
 */
public class DriveBuilder {

	// info
	int driver;
	int car;
	boolean deposit;
	String destination;
	int taken;
	Set<RGid> passengerSet;

	public DriveBuilder(int driver, int car, boolean deposit,
	            RGid[] passengers, String destination) {
		this.driver = driver;
		this.car = car;
		this.deposit = deposit;
		this.destination = destination;
		this.taken = 0;
		passengerSet = new HashSet <RGid> ();
		for (RGid rgid : passengers)
			passengerSet.add(rgid);
	}
	
	public Drive build() {
		return new Drive(driver, car, deposit, passengerSet.toArray(new RGid[0]), destination);
	}

}
