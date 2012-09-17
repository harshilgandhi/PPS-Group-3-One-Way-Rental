package rental.g3;

import java.util.ArrayList;
import java.util.List;
// Bookkeeping all the information about the game

// can I drive a car and drop it at any location on the road?
class Game {
	Graph graph;
	int nRelocator = 0;
	int nCar = 0;
	int nTurns = 0;
	int turn = 0;
	Relocator[] relocators;	
	Car[] cars;
	int gid = 0;
	
	final int MaxPickupDist = 3;
	
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
			if (car.isDeposit()==false && car.getLocation()==loc && !car.isInuse()) {
				emptyCars.add(car);
			}
		}
		return emptyCars;
	}
}