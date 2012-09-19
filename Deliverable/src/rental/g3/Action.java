package rental.g3;

class Route {
	int cid;
	int dst;

	public Route(int cid, int dst) {
		this.cid = cid;
		this.dst = dst;
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

