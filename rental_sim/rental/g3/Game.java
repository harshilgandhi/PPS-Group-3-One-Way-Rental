package rental.g3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
// Bookkeeping all the information about the game

import rental.g3.Graph.Distance;
import rental.sim.Offer;

// can I drive a car and drop it at any location on the road?
class Game {
	private static final boolean DEBUG = true;
	Graph graph;
	int nRelocator = 0;
	int nCar = 0;
	int nTurns = 0;
	int turn = 0;
	Relocator[] relocators;	
	Car[] cars;
	int gid = 0;
	
	final int MaxPickupDist = 3;
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
	
	public int rrdist(int rid1, int rid2) {
		int loc1 = relocators[rid1].getLocation();
		int loc2 = relocators[rid2].getLocation();
		return graph.getPaths()[loc1][loc2].dist;
	}
	
	public int rndist(int rid, int nid) {
		int loc = relocators[rid].getLocation();
		return graph.getPaths()[loc][nid].dist;
	}
	
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
		//highDegreePlacement();
	}
	
	// Rank nodes by their degrees
	private void highDegreePlacement() {
		// Optimize initial placement to account for
		// clustered cars.
	}
	
	
	// Rank cars by Shortest Path Distance
	private void sspPlacement() {
		relocators = new Relocator[nRelocator];
		
		if( nRelocator >= cars.length) { // if there are more relocators than cars
			// 	Assign each relocator to a car		
			for (int i = 0; i < nCar; i++) {
				relocators[i] = new Relocator(i, /*Relocator.RelocatorStatus.ENROUTE, */cars[i].location);
				relocators[i].assignCar(cars[i]);
				cars[i].assignDriver(relocators[i]);
			}
			// Extra relocators are distributed to location 0
			for (int i = nCar; i < nRelocator; i++) {
				relocators[i] = new Relocator(i, /*Relocator.RelocatorStatus.WAITING,*/ 0);
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

			// Pick the shortest locations.						
			for (int i = 0; i < nRelocator; i++) {
				int carId = distances.get(i).carId;
				//int dst = cars[carId].destination;
				relocators[i] = new Relocator(i,/* Relocator.RelocatorStatus.ENROUTE,*/ cars[carId].source);
				relocators[i].assignCar(cars[carId]);
				cars[carId].assignDriver(relocators[i]);
				assert(relocators[i].getLocation() == cars[carId].location);
			}
		}

		// Set relocators in game
		relocators = relocators;	
	}
	
	public String[] getStaringNodes() {		
		assert(turn == 0); // the initial placement should be called before the game starts
		String[] startingNodes = new String[nRelocator];
		
		for (int i = 0; i < nRelocator; i++) {
			startingNodes[i] = graph.getNodeName(relocators[i].getLocation());
		}
		
		return startingNodes;
	}
}