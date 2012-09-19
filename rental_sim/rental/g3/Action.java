package rental.g3;

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


class Pickup {
	public static final int MaxPickupDist = 3;
	public Pickup(int rid, int cid, int pick, int drop) {
		this.rid = rid;
		this.cid = cid;
		pickLoc = pick;
		dropLoc = drop;
	}
	int rid;
	int cid;
	int pickLoc;
	int dropLoc;
	
	@Override
	public String toString() {
		return "Pickup[driver:" + rid + ", car:" + cid + "]"; 
	}
}

