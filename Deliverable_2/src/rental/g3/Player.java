package rental.g3;

import java.util.List;
import java.util.Map;

import rental.sim.Edge;
import rental.sim.Offer;

public class Player extends rental.sim.Player {
	
	private enum RelocatorStatus {
		ENROUTE,
		IDLING,
		WAITING,
		PASSENGER,
		PICKUP,
		DROPOFF
	}
	
	private RelocatorStatus[] rStatuses;
	private String[] rLocations;
	
	private Map<Integer, String> carLocations;
	private Map<Integer, String> carDesinations;
	
	private List<String> nodes;
	private Path[][] paths;
	
	private class Path {
		int dist;
		int nextId;
	}
	
	private void initializeGraph(int relocators, String[] carLocations,
			String[] carDestinations, Edge[] edges) {
		// Initialized instance vars
		// Shortest path for Source -> Desination
	}

	@Override
	public String[] place(int relocators, String[] carLocations,
			String[] carDestinations, Edge[] edges, int groups)
			throws Exception {
		// Jose
		initializeGraph(relocators, carLocations, carDestinations, edges);
		
		// traverse varlocations & desinations and
		// shortest path
		
		
		return null;
	}

	@Override
	public Offer[] offer() throws Exception {
		// Not for monday
		return null;
	}

	@Override
	public void request(Offer[] offers) throws Exception {
		// Not for monday
		
	}

	@Override
	public void verify() throws Exception {
		// Not for monday
		
	}

	@Override
	public DriveRide action() throws Exception {
		
//		ENROUTE,
//		PICKUP,
//		DROPOFF
		// Jiacheng
		// Create drive object for next position
		
//		IDLING,
		// H
		// Find out whether we need drive object or not.
//		WAITING,
		// Hangout 
//		PASSENGER,
		// Inlcuded in Drive object
		
		return null;
	}

	
}