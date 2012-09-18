package rental.dumb;

import rental.sim.*;
import java.util.*;

public class Player extends rental.sim.Player {

	// how many ride attempts to do
	private static int rideAttempts = 2;

	// random generator
	private Random gen;

	// store map locally
	private Edge[] edges;

	// store car locations locally
	private String[] cars;

	// store car destinations locally
	private String[] carDest;

	// store relocator locations locally
	private String[] rels;

	// keep turn
	private int turn;

	// total players
	private int groups;

	// save offers of turn
	private Offer[] offers;

	// rides of last turn
	private Ride[] rides;

	public String name()
	{
		return "Dumb & Dumber";
	}

	// place relocators
	// use random locations of cars
	public String[] place(int relocators,           // relocators of your company
	                      String[] carLocations,    // locations of your cars
	                      String[] carDestinations, // destinations of your cars
	                      Edge[] edges,             // the edges of the map
	                      int groups)               // total number of groups
	{
		this.edges = edges;
		this.cars = carLocations;
		this.carDest = carDestinations;
		this.groups = groups;
		turn = 0;
		rels = new String[relocators];
		gen = new Random();
		for (int i = 0 ; i != relocators ; ++i)
			rels[i] = cars[gen.nextInt(cars.length)];
		rides = new Ride[0];
		return rels;
	}

	// publish offers
	// dump player generate up to five random offers
	public Offer[] offer() throws Exception
	{
		// update turn
		turn++;
		// check previous rides and update locations
		for (Ride r : rides)
			if (r.executed())
				rels[r.rid] = r.destination;
		// create new offers
		int offers = gen.nextInt(5);
		Offer[] offer = new Offer [offers];
		for (int i = 0 ; i != offers ; ++i) {
			// get an edge at random
			Edge edge = edges[gen.nextInt(edges.length)];
			// generate an offer for the random edge
			offer[i] = new Offer(edge.source, edge.destination, turn + gen.nextInt(3), this);
		}
		return offer;
	}

	// reply to offers
	// dump players reply randomly
	public void request(Offer[] offers) throws Exception
	{
		// save for verification
		this.offers = offers;
		for (Offer o : offers) {
			// reply to other's offers only
			if (o.group == id) continue;
			// get a random relocator
			int r = gen.nextInt(rels.length);
			o.request(r, this);
		}
	}

	// verify requests
	// dump players accepts randomly
	public void verify() throws Exception
	{
		for (Offer o : offers) {
			// reply to your own offers only
			if (o.group != id) continue;
			// get array of requests
			RGid[] requests = o.requests();
			// generate same size array of replies
			boolean[] reply = new boolean [requests.length];
			// reply randomly
			for (int i = 0 ; i != requests.length ; ++i)
				reply[i] = gen.nextInt(2) == 1;
			// give back the reply array
			o.verify(reply, this);
		}
	}

	// post drives and rides
	// random rides using your own people
	public Player.DriveRide action() throws Exception
	{
		// generate rides
		List <Ride> rides = new LinkedList <Ride> ();
		// keep used relocators
		HashSet <RGid> used = new HashSet <RGid> ();
		// generate X rides and stop if a deadend
		int tries = 100;
		for (int r = 0 ; r != rideAttempts ; ++r) {
			if (--tries == 0) break;
			// get a random other group
			int otherGroup = gen.nextInt(groups);
			while (otherGroup == id)
				otherGroup = gen.nextInt(groups);
			// get a random relocator
			int rid = gen.nextInt(rels.length);
			String src = rels[rid];
			// find a neighboring edge of relocator
			HashSet <String> dests = new HashSet <String> ();
			for (Edge edge : edges)
				if (edge.source.equals(src))
					dests.add(edge.destination);
			if (dests.size() == 0) continue;
			String dest = dests.toArray(new String[0])[gen.nextInt(dests.size())];
		//	System.err.println(src + " -> " + dest);
			rides.add(new Ride(rid, otherGroup, dest));
			used.add(new RGid(rid, id));
		}
		// generate drives
		List <Drive> drives = new LinkedList <Drive> ();
		// move every car if possible
		for (int c = 0 ; c != cars.length ; ++c) {
			// get car location
			String src = cars[c];
			// get all possible destinations
			HashSet <String> dests = new HashSet <String> ();
			for (Edge edge : edges)
				if (edge.source.equals(src))
					dests.add(edge.destination);
			if (dests.size() == 0) {
				System.err.println("! [" + id + "] Car " + c + ": no destination");
				continue;
			}
			String dest = dests.toArray(new String[0])[gen.nextInt(dests.size())];
			// search for a driver on the same location
			int driver = -1;
			for (int r = 0 ; r != rels.length ; ++r)
				// driver must be on same location and unused
				if (rels[r].equals(src) && !used.contains(new RGid(r, id))) {
					driver = r;
					break;
				}
			// no driver found skip car
			if (driver < 0) {
				System.err.println("! [" + id + "] Car " + c + ": no driver");
				continue;
			}
			// driver found drive is going
		//	System.err.println(src + " -> " + dest);
			used.add(new RGid(driver, id));
			rels[driver] = dest;
			cars[c] = dest;
			if (carDest[c].equals(dest))
				cars[c] = null;
			// search for more relocators and put them in the car
			List <RGid> extra = new LinkedList <RGid> ();
			for (int r = 0 ; r != rels.length ; ++r)
				if (rels[r].equals(src) && r != driver && extra.size() < 3) {
					RGid rgid = new RGid(r, id);
					if (used.contains(rgid)) continue;
					extra.add(rgid);
					rels[r] = dest;
					used.add(rgid);
				}
			// put some random extra rides from other teams
			if (groups > 1)
				while (extra.size() < 3) {
					// get a random other group
					int otherGroup = gen.nextInt(groups);
					while (otherGroup == id)
						otherGroup = gen.nextInt(groups);
					// get a random edge
					Edge edge = edges[gen.nextInt(edges.length)];
					// get a random relocator
					int rid = gen.nextInt(rels.length);
					// add extra passenger
					RGid rgid = new RGid(rid, otherGroup);
					if (used.contains(rgid)) break;
					extra.add(rgid);
					used.add(rgid);
				}
			drives.add(new Drive(driver, c, true, extra.toArray(new RGid[0]), dest));
		}
		// return pair of rides and drives
		return new DriveRide(drives.toArray(new Drive[0]), rides.toArray(new Ride[0]));
	}
}
