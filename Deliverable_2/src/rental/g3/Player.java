package rental.g3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

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
	private Map<Integer, String> carDestinations;
	
	private List<String> nodes;
	private Path[][] paths;
	
	private class Path implements Comparable<Path> {
		private int dist = Integer.MAX_VALUE;
		private int nextId;
		private boolean known;
		private int id;
		
		@Override
		public int compareTo(Path p) {
			return  (dist < p.dist) ? -1 :
					(dist > p.dist) ?  1 : 0;
		}
		
		@Override
		public String toString() {
			return "[" + id + ":" + dist + "]";
		}
	}
	
	private class Distance implements Comparable<Distance> {
		private int carId;
		private int distance;
		
		public Distance(int carId, int distance) {
			this.carId = carId;
			this.distance = distance;
		}
		
		@Override
		public int compareTo(Distance d) {
			return  (distance < d.distance) ? -1 :
					(distance > d.distance) ?  1 : 0;
		}
	}
 	
	public static void main(String[] args) {
		/* Testing with dot:
graph {
Berlin -- Warsaw
Berlin -- Munich
Munich -- Warsaw
Berlin -- Zurich
Zurich -- Zagreb
Zagreb -- Budapest
Zurich -- Vienna
Vienna -- Berlin
Vienna -- Budapest
Budapest -- Bucharest
Budapest -- Moscow
Zagreb -- Vienna
Moscow -- Warsaw
Warsaw -- Copenhagen
Munich -- Copenhagen
}

		*/
		Edge[] edges = new Edge[] {
			new Edge("Berlin", "Warsaw"),
			new Edge("Berlin", "Munich"),
			new Edge("Munich", "Warsaw"),
			new Edge("Berlin", "Zurich"),
			new Edge("Zurich", "Zagreb"),
			new Edge("Zagreb", "Budapest"),
			new Edge("Zurich", "Vienna"),
			new Edge("Vienna", "Berlin"),
			new Edge("Vienna", "Budapest"),
			new Edge("Budapest", "Bucharest"),
			new Edge("Budapest", "Moscow"),
			new Edge("Zagreb", "Vienna"),
			new Edge("Moscow", "Warsaw"),
			new Edge("Warsaw", "Copenhagen"),
			new Edge("Munich", "Copenhagen")
		};
		
		String[] carL = new String[] {
			"Berlin",
			"Vienna",
			"Warsaw"
		};
		
		String[] carD = new String[] {
			"Zagreb",
			"Budapest",
			"Vienna"
		};
		
		Player p = new Player();
		try {
			String[] starts = p.place(1, carL, carD, edges, 0);
			System.out.println("Starting locations:");
			System.out.println(Arrays.toString(starts));
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for(int i = 0; i < p.paths.length; i++) {
			System.out.print(i + ": " + p.nodes.get(i) + " ");
			System.out.println(Arrays.toString(p.paths[i]));
		}
	}
	
	private void initializeGraph(int relocators, String[] carLocations,
			String[] carDestinations, Edge[] edges) {
		// Initialized instance vars
		this.carLocations = new HashMap<Integer, String>();
		this.carDestinations = new HashMap<Integer, String>();
		
		// Initialized car map
		// We can do this since they're both the same length.
		for(int i = 0; i < carLocations.length; i++) {
			this.carLocations.put(i, carLocations[i]);
			this.carDestinations.put(i, carDestinations[i]);
		}
		
		Map<String, Set<String>> neighbors = new HashMap<String, Set<String>>();
		Set<String> destNeighbor = null;
		Set<String> sourceNeighbor = null;
		
		Set<String> nodeSet = new HashSet<String>();
		for(Edge edge: edges) {
			nodeSet.add(edge.destination);
			nodeSet.add(edge.source);
			
			if((destNeighbor = neighbors.get(edge.destination)) == null) {
				destNeighbor = new HashSet<String>();
				neighbors.put(edge.destination, destNeighbor);
			}
			
			if((sourceNeighbor = neighbors.get(edge.source)) == null) {
				sourceNeighbor = new HashSet<String>();
				neighbors.put(edge.source, sourceNeighbor);
			}
			
			destNeighbor.add(edge.source);
			sourceNeighbor.add(edge.destination);
		}
		
		nodes = new ArrayList<String>(nodeSet);
		paths = new Path[nodes.size()][nodes.size()];
		
		// Shortest path for Source -> Desination
		String node = null;
		Path path = null;
		Path neighborPath = null;
		PriorityQueue<Path> queue;
		for(int id = 0; id < nodes.size(); id++) {
			
			queue = new PriorityQueue<Path>();
			for(int neighborId = 0; neighborId < nodes.size(); neighborId++) {
				if(id != neighborId) {
					path = new Path();
					path.id = neighborId;
					paths[id][neighborId] = path;
					queue.add(path);
				} else {
					path = new Path();
					path.id = id;
					path.nextId = id;
					path.dist = 0;
					paths[id][id] = path;
					queue.add(path);
				}
			}
			
			while((path = queue.poll()) != null) {
				path.known = true;
				
				node = nodes.get(path.id);
				for(String neighbor : neighbors.get(node)) {
					int neighborId = nodes.indexOf(neighbor);
					neighborPath = paths[id][neighborId];
					if(!neighborPath.known) {
						int d = path.dist + 1;
						if( d < neighborPath.dist ) {
							neighborPath.dist = d;
							neighborPath.nextId = path.id;
							
							// Need to re-insert so
							// queue resorts to changed data.
							queue.remove(neighborPath);
							queue.add(neighborPath);
						}
					}
				}
			}
		}
	}

	@Override
	public String[] place(int relocators, String[] carLocations,
			String[] carDestinations, Edge[] edges, int groups)
			throws Exception {
		
		initializeGraph(relocators, carLocations, carDestinations, edges);
		
		// If we have enough relocators for every car
		// just return those locations
		if(relocators >= carLocations.length) {
			return carLocations;
		}
		
		// traverse varlocations & desinations and
		List<Distance> distances = new ArrayList<Distance>(carLocations.length);
		Distance distance;
		int dist;
		for(int i = 0; i < carLocations.length; i++) {
			int sourceId = nodes.indexOf(carLocations[i]);
			int destId = nodes.indexOf(carDestinations[i]);
			
			dist = paths[sourceId][destId].dist;
			
			distance = new Distance(i, dist);
			distances.add(distance);
		}
		
		Collections.sort(distances);
		String[] startingNodes = new String[relocators];
		for(int i = 0; i < relocators; i++) {
			startingNodes[i] = carLocations[distances.get(i).carId];
		}
		
		return startingNodes;
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