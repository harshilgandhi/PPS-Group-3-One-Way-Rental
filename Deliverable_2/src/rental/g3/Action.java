package rental.g3;

class Route {
	int cid;
	int dst;

	public Route(int cid, int dst) {
		this.cid = cid;
		this.dst = dst;
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
}
