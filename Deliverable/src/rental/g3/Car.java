package rental.g3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rental.sim.RGid;

class Car {
	int cid;
	int source;
	int destination;
	
	int location;
	Relocator driver = null;
	boolean isDeposit = false;
	Set<RGid> passengers = new HashSet<RGid>();	
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
		this.location = location;
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
		this.location = nextLoc;
		setScheduled(true);
	}

	public boolean isInuse() {
		return this.driver != null;
	}

	public Set<RGid> getPassengers() {
		return passengers;
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
	
