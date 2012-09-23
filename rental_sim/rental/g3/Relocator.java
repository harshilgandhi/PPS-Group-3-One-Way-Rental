package rental.g3;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class Relocator {
	int rid;
	private int lastLocation;
	private int location;
	Car car = null;
	Relocator pickuper = null;	
	private boolean scheduled;
	List<Pickup> pickups = new ArrayList<Pickup>(Game.MAX_PASSENGERS);
	
	// Aux Structure:	
	// for enrouting relocator
	//private Route route;			
	private Stack<Route> routes = new Stack<Route>();
	// for pickup relocator
	//private List<Pickup> pickups;	
	public int baseDestination;
	private int chainCar = -1;
	
	public Relocator(int id, int loc) {
		rid = id;
		location = loc;
		lastLocation = loc;
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
	
	public Stack<Route> getRoutes() {
		return routes;
	}
	
	public void move(int nextLoc) {
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

	public void removePickup(int rid) {
		Pickup removal = null;
		for(Pickup pickup : pickups) {
			if(pickup.passengerRid == rid) {
				removal = pickup;
				break;
			}
		}
		
		if(removal != null) {
			pickups.remove(removal);
		}
		
	}

	public void updatePickupRoute(Relocator r) {
		Route pickupRoute = null;
		for(Route route : routes) {
			if(route.forRelocator == r.rid && route.dst == r.getLastLocation()) {
				pickupRoute = route;
				break;
			}
		}
		
		if(pickupRoute != null) {
			pickupRoute.dst = r.getLocation();
		}
	}
}