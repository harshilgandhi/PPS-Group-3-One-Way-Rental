package rental.g3;

/**
 * 
 * Describes the destination of the relocator.
 *
 */
class Route {
	public static final int PICKUP = 1;
	public static final int DROPOFF = 2;
	
	int cid;
	int dst;
	int forRelocator = -1;
	int type;

	public Route(int cid, int dst) {
		this.cid = cid;
		this.dst = dst;
	}
	
	public Route(int cid, int dst, int forRelocator, int type) {
		this.cid = cid;
		this.dst = dst;
		this.forRelocator = forRelocator;
		this.type = type;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Route) {
			Route r = (Route) obj;
			return r.cid == cid &&
					r.dst == dst && 
					r.type == type &&
					r.forRelocator == forRelocator;
		}
		
		return false;
	}
}

/**
 * 
 * Wrapper class to sort pickups by their distances added for Global Pickup search.
 *
 */
class PickupWrapper implements Comparable<PickupWrapper> {
	PickupDistance pickupDist;
	Pickup pickup;
	
	public PickupWrapper(Pickup pickup, PickupDistance pickupDist) {
		this.pickup = pickup;
		this.pickupDist = pickupDist;
	}

	@Override
	public int compareTo(PickupWrapper wrapper) {
		return pickupDist.compareTo(wrapper.pickupDist);
	}
}

/**
 * 
 * Class describing the pick up and its distance.
 *
 */
class PickupDistance implements Comparable<PickupDistance> {
	int pid;
	int distance;
	
	public PickupDistance(int id, int dist) {		
		this.pid = id;
		this.distance = dist;
	}

	@Override
	public int compareTo(PickupDistance pd) {
		return (distance < pd.distance) ? -1 :
			   (distance > pd.distance) ?  1 : 0;
	}
}

/**
 * 
 * Class that describes the pick up.
 *
 */
class Pickup {
	int driverRid;
	int passengerRid;
	int pickupingUpCid;
	int pickLoc;
	int dropLoc;
	
	public Pickup(int rid, int cid, int pick, int drop, int driver) {
		this.passengerRid = rid;
		this.driverRid = driver;
		this.pickupingUpCid = cid;
		pickLoc = pick;
		dropLoc = drop;
	}
	
	@Override
	public String toString() {
		return "Pickup[driver:" + passengerRid + ", car:" + pickupingUpCid + "]"; 
	}
}

