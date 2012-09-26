package rental.g3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import rental.sim.Edge;

/**
 * Graph contains all the nodes of the given map and shortest 
 * paths for each node and their adjacencies.
 */
class Graph {	
	private Map<String, Integer> nodeIds;
	private List<String> nodes;
	
	private int[][] adjacency; // adjacency-matrix
	private Path[][] paths; // shortest paths.
	public static int MAP_MAX_DISTANCE = 1; //Set to diameter of the graph.
	public static int PICKUP_DISTANCE = 0;  //Initial pickup distance.
	

	public Graph(Edge[] edges) {
		Map<String, Set<String>> neighbors = new HashMap<String, Set<String>>();
		Set<String> destNeighbor = null;
		Set<String> sourceNeighbor = null;
		
		// Construct set of all nodes in graph.
		// and building neighbor sets
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
					
		// Build id-str & str-id map
		nodes = new ArrayList<String>(nodeSet);
		nodeIds = new HashMap<String, Integer>();
		for (int i = 0; i < nodes.size(); i++)
			nodeIds.put(nodes.get(i), i);
	
		// Build adjacency matrix
		int nodeCount = nodeSet.size();
		adjacency= new int[nodeCount][nodeCount];
		for(Edge edge: edges) {
			int src = getNodeId(edge.source);
			int dst = getNodeId(edge.destination);
			adjacency[src][dst] = 1;
			adjacency[dst][src] = 1;
		}
		
		paths = new Path[nodes.size()][nodes.size()];
		shortestPath(neighbors);
	}
	
	public int degreeOf(int nodeId) {
		int degree = 0;
		for (int i = 0; i < nodeCount(); i++) {
			degree += adjacency[nodeId][i];
		}
		return degree;
	}
	
	public int nodeCount() {
		return nodeIds.size();
	}
	
	public int nextMove(int loc, int dst) {
		return paths[dst][loc].nextId;
	}
	
	private void shortestPath(Map<String, Set<String>> neighbors) {
		// Shortest path for Source -> Desination
		// Using Dijkstra
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
						
						if(d > MAP_MAX_DISTANCE) {
							MAP_MAX_DISTANCE = d;
						}
						
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
	
	public int getNodeId(String str) {
		return nodeIds.get(str);
	}
	
	public String getNodeName(int id) {
		return nodes.get(id);
	}
	
	/**
	 * 
	 * Utility class for Dijkstra's algorithm.
	 *
	 */
	class Path implements Comparable<Path> {
		int dist = Integer.MAX_VALUE;
		int nextId;
		boolean known;
		int id;
		
		@Override
		public int compareTo(Path p) {
			return  (dist < p.dist) ? -1 :
					(dist > p.dist) ?  1 : 0;
		}
		
		@Override
		public String toString() {
			return "[" + nextId + ":" + dist + "]";
		}
	}
	
	/**
	 * 
	 *  Utility Class for sorting by the shortest path.
	 *
	 */
	class Distance implements Comparable<Distance> {
		int carId;
		int distance;
		
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
	
	public Path[][] getPaths() {
		return paths;
	}

	public void setPaths(Path[][] paths) {
		this.paths = paths;
	}
}
