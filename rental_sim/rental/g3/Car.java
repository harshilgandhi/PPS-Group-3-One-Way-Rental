package rental.g3;

import java.util.ArrayList;
import java.util.List;

import rental.sim.RGid;

/**
 * 
 * Describes the Car object.
 *
 */
class Car {
	int cid;
	int source;
	int destination;
	
	private int location;
	private int lastLocation = -1;
	Relocator driver = null;
	boolean isDeposit = false;
	List<RGid> passengers = new ArrayList<RGid>();	
	private boolean scheduled;
	
	public Car(int id, int src, int dst) {
		this.cid = id;
		this.source = src;
		this.destination = dst;
		this.location = this.source;
	}
	
	public Relocator getDriver() {
		return driver;
	}
	public void setDriver(Relocator driver) {
		this.driver = driver;
	}
	public int getDestination() {
		return destination;
	}
	public int getSource() {
		return source;
	}
	public int getLocation() {
		return location;
	}
	public void setLocation(int location) {
		this.lastLocation = this.location;
		this.location = location;
	}
	
	public int getLastLocation() {
		return lastLocation;
	}
	
	public int getCid() {
		return cid;
	}
	public boolean isDeposit() {
		return isDeposit;
	}
	
	public void assignDriver(Relocator driver) {
		this.driver = driver;	
	}
	
	public void deposit() {
		this.isDeposit = true;
		driver.car = null; // the driver no longer own the car
		driver.popRoute();
		driver = null;		
	}
	
	public void move(int nextLoc) {
		setLocation(nextLoc);
		setScheduled(true);
	}

	public boolean isInuse() {
		return this.driver != null;
	}

	public List<RGid> getPassengers() {
		return passengers;
	}

	public void setPassengers(List<RGid> passengers) {
		this.passengers = passengers;
	}
	
	// if the car is deposited, always displayed as scheduled
	public void reset() {
		if (isDeposit) {
			scheduled = true;
			return;
		}
		scheduled = false;			
	}

	public boolean isScheduled() {
		return scheduled;
	}

	public void setScheduled(boolean scheduled) {
		this.scheduled = scheduled;
	}
	
}
	
