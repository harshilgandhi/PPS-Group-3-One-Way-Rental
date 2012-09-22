package rental.g3;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class Relocator {
//	public enum RelocatorStatus {		
//		ENROUTE, // the driver heading to his destination with no passenger
//		IDLING, // the driver who has a car available at the destination
//		WAITING, // a relocator who is waiting for a pick up		
//		PASSENGER, // a relocator in another driver's car to the next available car
//		PICKUP, // a relocator who is rerouting to pick another relocator
//		DROPOFF, // a relocator who carry other relocators
//		INVALID
//	}
//		
	int rid;
	private int lastLocation;
	private int location;
//	RelocatorStatus status;
	Car car = null;
	Relocator pickuper = null;	
	private boolean scheduled;
	
	
	// Aux Structure:	
	// for enrouting relocator
	//private Route route;			
	private Stack<Route> routes = new Stack<Route>();
	// for pickup relocator
	//private List<Pickup> pickups;	
	public int baseDestination;
	private int chainCar = -1;
	
	public Relocator(int id, /*RelocatorStatus s, */int loc) {
		rid = id;
//		status = s;
		location = loc;
		lastLocation = loc;
	}
	
//	public RelocatorStatus getStatus() {
//		return status;
//	}
//	public void setStatus(RelocatorStatus status) {
//		this.status = status;
//	}
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
	
	public Stack<Route> getRoutes() {
		return routes;
	}
	
	public void move(/*RelocatorStatus nextStatus, */int nextLoc) {
		setLocation(nextLoc);
		this.scheduled = true;
	}
	
	public void pushRoute(Route r) {
		if(routes.size() == 0) {
			baseDestination = r.dst;
		}
		routes.push(r);
	}
	
	private void removeRoutes(int rid, int type) {
		List<Route> removeRoutes = new ArrayList<Route>();
		for(Route r : routes) {
			if(r.forRelocator == rid && r.type == type) {
				removeRoutes.add(r);
			}
		}
		routes.removeAll(removeRoutes);
	}
	
	public void removePickupRoutes(int rid) {
		removeRoutes(rid, Route.PICKUP);
	}
	
	public void removeDropOffRoutes(int rid) {
		removeRoutes(rid, Route.DROPOFF);
	}
	
	public void popRoute() {
		routes.pop();
	}
	public Route firstRoute() {
		return routes.peek();
	}

	public boolean isScheduled() {
		return scheduled;
	}
	
	public void reset() {
		this.scheduled = false;
	}
	
	public void assignCar(Car car) {
		if(car.cid == chainCar) {
			chainCar = -1;
		}
		this.car = car;
		pushRoute(new Route(car.cid, car.destination));
		this.baseDestination = car.destination;
		
	}
	
	public boolean hasCar() {
		return car != null;
	}
	
	public boolean isDriving() {
		return car != null && car.location == location;
	}
	
	public void setChainCar(int cid) {
		this.chainCar = cid;
	}
	
	public boolean hasChainCar() {
		return chainCar >= 0;
	}
	
	public int getChainCar() {
		return this.chainCar;
	}
}