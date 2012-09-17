package rental.g3;

import java.util.List;
import java.util.Stack;

class Relocator {
	public enum RelocatorStatus {
		
		DRIVING,
		
		ENROUTE, // the driver heading to his destination with no passenger
		IDLING, // the driver who has a car available at the destination
		WAITING, // a relocator who is waiting for a pick up		
		PASSENGER, // a relocator in another driver's car to the next available car
		PICKUP, // a relocator who is rerouting to pick another relocator
		DROPOFF, // a relocator who carry other relocators
		INVALID
	}
		
	int rid;
	int location;
	RelocatorStatus status;
	int cid = -1;
	int pickuper = -1;
	
	
	// Aux Structure:	
	// for enrouting relocator
	//private Route route;			
	Stack<Route> routes = new Stack<Route>();
	// for pickup relocator
	//private List<Pickup> pickups;
	
	int nextLoc = -1;
	RelocatorStatus nextStatus = RelocatorStatus.INVALID;
	
	public void move() {
		location = nextLoc;
		status = nextStatus;
		nextStatus = RelocatorStatus.INVALID;
		nextLoc = -1;
	}
	
	public Relocator(int id, RelocatorStatus s, int loc) {
		rid = id;
		status = s;
		location = loc;
	}
	
//	public List<Pickup> getPickups() {
//		return pickups;
//	}
//	public void setPickups(List<Pickup> pickups) {
//		this.pickups = pickups;
//	}
	public RelocatorStatus getStatus() {
		return status;
	}
	public void setStatus(RelocatorStatus status) {
		this.status = status;
	}
	public int getLocation() {
		return location;
	}
	public void setLocation(int location) {
		this.location = location;
	}
	public Stack<Route> getRoutes() {
		return routes;
	}
	public void setNext(RelocatorStatus nextStatus, int nextLoc) {
		this.nextStatus = nextStatus;
		this.nextLoc = nextLoc;
	}
	
	public void pushRoute(Route r) {
		routes.push(r);
	}
	public void popRoute() {
		routes.pop();
	}
	public Route firstRoute() {
		return routes.peek();
	}
}