package rental.g3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import rental.sim.Offer;

/**
 * 
 * Game class is used for book-keeping of all the game data and 
 * for the initial placement of the relocators on the map.
 *
 */

class Game {
	private static final boolean DEBUG = false;
	private static final double LongDstThreshold = 0.3; //Used for the goodPlacement() method
	public static final int MAX_PASSENGERS = 1; //Maximum no. of own passengers in each car.
	
	Graph graph;
	int nRelocator = 0;
	int nCar = 0;
	int nTurns = 0;
	int turn = 0;
	Relocator[] relocators;	
	Car[] cars;
	int gid = 0;
	
	public List<Offer> offers;
	public LinkedList<Relocator> offerRelocators;
	public Offer[] gameOffers;
	
	public void log(String message) {
		if(DEBUG) {
			System.err.println("(" + gid + "): " + message);
		}
	}
	
	public Game(int gid, Graph g, int ncar, Car[] cars, int nrel, int nTurns) {
		this.nCar = ncar;
		this.graph = g;
		this.cars = cars;
		this.nRelocator = nrel;
		this.nTurns = nTurns;
		this.gid = gid;
	}

	public class Path {
		int dist;
		int nextId;
	}
	
	/**
	 *  Shortest distance between 2 relocators. 
	 */
	public int rrdist(int rid1, int rid2) {	 
		int loc1 = relocators[rid1].getLocation();
		int loc2 = relocators[rid2].getLocation();
		return graph.getPaths()[loc1][loc2].dist;
	}
	
	/**
	 * 
	 *  Shortest distance between relocator and node.
	 */
	public int rndist(int rid, int nid) {
		int loc = relocators[rid].getLocation();
		return graph.getPaths()[loc][nid].dist;
	}
	
	/**
	 *  Shortest distance between 2 nodes.
	 */
	public int nndist(int nid1, int nid2) {
		return graph.getPaths()[nid1][nid2].dist;
	}
	
	public List<Car> getEmptyCars(int loc) {
		List<Car> emptyCars = new ArrayList<Car>();
		for (Car car : cars) {
			if (car.isDeposit()==false && car.isScheduled()==false 
				&& car.getLocation()==loc && car.isInuse() == false) {
				emptyCars.add(car);
			}
		}
		return emptyCars;
	}
	
	
	public void doPlacement() {
		sspPlacement();
		//goodPlacement();
	}
	
		
	// The placement strategy that considers:
	// 1. #empty cars @ destination
	// 2. #degree of destination (important/hub)
	// 3. path distance
	private void goodPlacement() {
		
		relocators = new Relocator[nRelocator];		
		int[] degrees = new int[graph.nodeCount()];
		int[] carCount = new int[graph.nodeCount()];
		for (Car car : cars) {			
			int source = car.source;
			carCount[source]++;
			if (degrees[car.destination] == 0)
				degrees[car.destination] = graph.degreeOf(car.destination);
		}
		
		List<CarScore> carScores = new ArrayList<CarScore>(nCar);
		for (Car car : cars) {
			carScores.add(new CarScore(car.cid, degrees[car.destination], carCount[car.destination], 
					graph.getPaths()[car.source][car.destination].dist));
		}
		Collections.sort(carScores);
		Collections.reverse(carScores);
		
		// Pick nRelocator number of cars
		int assigned = 0;
		double threshold = LongDstThreshold;
		while (assigned < nRelocator) {			
			for (int i = 0; i < nCar && assigned < nRelocator; i++) {
				int carId = carScores.get(i).cid;
				if (cars[carId].isInuse())
					continue;				
				int dst = carScores.get(i).pathDistance;				
				
				// refine by criteria 3, if the initial distance is too long, postpone that car
				if (dst < threshold * Graph.MAP_MAX_DISTANCE) {					
					relocators[assigned] = new Relocator(assigned, cars[carId].source);
					relocators[assigned].assignCar(cars[carId]);
					cars[carId].assignDriver(relocators[assigned]);
					assigned++;
				}							
			}
			// after each round, release the constraint of criteria 3
			threshold += 0.1;			
		}
	}
	

	/**
	 * 
	 *  Utility class for "goodPlacement()"
	 *
	 */
	private class CarScore implements Comparable<CarScore> {
		private int cid;
		private int dstDegree;
		private int dstCarCount;
		private int pathDistance;
		
		public CarScore(int cid, int dstDegree, int dstCarCount, int pathDistance) {
			this.cid = cid;
			this.dstDegree = dstDegree;
			this.dstCarCount = dstCarCount;
			this.pathDistance = pathDistance;
		}

		@Override
		public int compareTo(CarScore otherCar) {
			if (this.dstCarCount > otherCar.dstCarCount)
				return 1;
			else if (this.dstCarCount < otherCar.dstCarCount)
				return -1;
			if (this.dstDegree > otherCar.dstDegree)
				return 1;
			else if (this.dstDegree < otherCar.dstDegree)
				return -1;
			return 0;
		}
	}
	
	// Rank cars by Shortest Path Distance
	@SuppressWarnings("unused")
	private void sspPlacement() {
		relocators = new Relocator[nRelocator];
		
		if( nRelocator >= cars.length) { // if there are more relocators than cars
			// 	Assign each relocator to a car		
			for (int i = 0; i < nCar; i++) {
				relocators[i] = new Relocator(i, cars[i].getLocation());
				relocators[i].assignCar(cars[i]);
				cars[i].assignDriver(relocators[i]);
			}
			// Extra relocators are distributed to location 0
			for (int i = nCar; i < nRelocator; i++) {
				relocators[i] = new Relocator(i, 0);
			}			
		}
		else {
			List<Graph.Distance> distances = new ArrayList<Graph.Distance>(nCar);
			Graph.Distance distance;
			int dist;

			// Find all the distances from S -> D
			for(int i = 0; i < nCar; i++) {
				int sourceId = cars[i].source;
				int destId = cars[i].destination;				
				dist = graph.getPaths()[sourceId][destId].dist;				
				distance = graph.new Distance(i, dist);
				distances.add(distance);
			}

			// Sort them by distance
			Collections.sort(distances);

			boolean considerChain = true;
			
			if (!considerChain) {
				for (int i = 0; i < nRelocator; i++) {
					int carId = distances.get(i).carId;
					Car assigning = cars[carId];
					relocators[i] = new Relocator(i, assigning.source);
					relocators[i].assignCar(assigning);
					assigning.assignDriver(relocators[i]);
				}
			}
			else {
				// Pick nRelocator number of cars
				// But we want to ensure there is another empty car at
				// the destination so that there is a chain
				int assigned = 0;				
				for (int i = 0; i < nCar && assigned < nRelocator; i++) {
					int carId = distances.get(i).carId;
					Car assigning = cars[carId];

					boolean hasChain = false;
					for (Car car : cars) {
						if (!car.isInuse() && car.source == assigning.destination) {
							hasChain = true;
							break;
						}						
					}
					if (hasChain) {					
						relocators[assigned] = new Relocator(assigned, assigning.source);
						relocators[assigned].assignCar(assigning);
						assigning.assignDriver(relocators[assigned]);
						assigned++;
					}
				}				
				// if we cannot find enough chains
				// assign by shortest path distance
				if (assigned < nRelocator) {		
					for (int i = 0; i < nCar && assigned < nRelocator; i++) {
						int carId = distances.get(i).carId;
						Car assigning = cars[carId];
						if (!assigning.isInuse()) {			
							relocators[assigned] = new Relocator(assigned, assigning.source);
							relocators[assigned].assignCar(assigning);
							assigning.assignDriver(relocators[assigned]);
							assigned++;
						}
					}		
				}				
				assert(assigned == nRelocator);
			}
		}
	}
	
	
	public String[] getStartingNodes() {		
		assert(turn == 0); // the initial placement should be called before the game starts
		String[] startingNodes = new String[nRelocator];
		
		for (int i = 0; i < nRelocator; i++) {
			startingNodes[i] = graph.getNodeName(relocators[i].getLocation());
		}
		
		return startingNodes;
	}
}
