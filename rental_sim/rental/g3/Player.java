package rental.g3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import rental.sim.Edge;
import rental.sim.Offer;
import rental.sim.RGid;
import rental.sim.Ride;

public class Player extends rental.sim.Player {
	// We can keep all the information about the game into this structure
	private Game game;
	private DriveRide driveRide;
 	
	public String name()
	{
		return "Group 3";
	}
	
	private Game initializeGame(int nrel, String[] carLocations,
			String[] carDestinations, Edge[] edges, int totalTurns) {
		// initialize the graph			
		Graph g = new Graph(edges);
		
		// initialize car location
		int ncar = carLocations.length;
		Car[] cars = new Car[ncar];
		for (int i = 0; i < ncar; i++)
			cars[i] = new Car(i, g.getNodeId(carLocations[i]), g.getNodeId(carDestinations[i]));
		
		// create a new game
		Game game = new Game(this.id, g, ncar, cars, nrel, totalTurns);
		return game;
	}
	
	@Override
	public String[] place(int nrel, String[] carLocations,
			String[] carDestinations, Edge[] edges, int groups, int totalTurns)
			throws Exception {
		// initialize the game
		game = initializeGame(nrel, carLocations, carDestinations, edges, totalTurns);
		
		// TODO: Jiacheng - Optimize initial placement to account for
		// clustered cars.
		
		// place relocators
		// If we have enough relocators for every car
		// just return those locations
		
		Relocator[] relocators = new Relocator[nrel];
		String[] startingNodes = new String[nrel];
		if(nrel >= carLocations.length) {
			// Note: Some locations may have more than one relocator
			// or extra relocators are distributed to location 0

			for (int i = 0; i < game.nCar; i++) {
				relocators[i] = new Relocator(i, Relocator.RelocatorStatus.ENROUTE, game.graph.getNodeId(carLocations[i]));
				relocators[i].assignCar(game.cars[i]);
				game.cars[i].assignDriver(relocators[i]);
				startingNodes[i] = carLocations[i];				
			}								
			for (int i = game.nCar; i < nrel; i++) {
				relocators[i] = new Relocator(i, Relocator.RelocatorStatus.WAITING, 0);
				startingNodes[i] = game.graph.getNodeName(0);
			}			
		}
		else {
			// traverse car locations & desinations and
			List<Graph.Distance> distances = new ArrayList<Graph.Distance>(game.nCar);
			Graph.Distance distance;
			int dist;
			
			// Find all the distances from S -> D
			for(int i = 0; i < game.nCar; i++) {
				int sourceId = game.graph.getNodeId(carLocations[i]);
				int destId = game.graph.getNodeId(carDestinations[i]);				
				dist = game.graph.getPaths()[sourceId][destId].dist;				
				distance = game.graph.new Distance(i, dist);
				distances.add(distance);
			}
			
			// Sort them by distance
			Collections.sort(distances);
			
			// Pick the shortest locations.						
			for (int i = 0; i < game.nRelocator; i++) {
				int carId = distances.get(i).carId;
				int dst = game.cars[carId].destination;
				startingNodes[i] = carLocations[carId];
				relocators[i] = new Relocator(i, Relocator.RelocatorStatus.ENROUTE, game.graph.getNodeId(startingNodes[i]));
				relocators[i].assignCar(game.cars[carId]);
				game.cars[carId].assignDriver(relocators[i]);
				assert(relocators[i].getLocation() == game.cars[carId].location);
			}
		}
		
		// Set relocators in game
		game.relocators = relocators;
		return startingNodes;
	}

	@Override
	public Offer[] offer() throws Exception {
		game.turn++;
		
		// Calculate the moves for this turn.
		this.driveRide = generateDriveRide();
		
		// Not for monday
		game.offers = new LinkedList<Offer>();
		game.offerRelocators = new LinkedList<Relocator>();
		
		for(Relocator r : game.relocators) {
			if(r.isDriving()) {
				
				// We only need to make one offer
				// and when we accept requests we can then
				// accept more than one.
				int seats = 3 - r.car.passengers.size();
				if(seats > 0) {
					game.offers.add(new Offer(
							game.graph.getNodeName(r.getLastLocation()),
							game.graph.getNodeName(r.getLocation()),
							game.turn,
							Player.this
							));		
					game.offerRelocators.add(r);
					Game.log("Driver: " + r.rid + " making offer.");
				}
			}
		}
		
		return game.offers.toArray(new Offer[0]);
	}

	@Override
	public void request(Offer[] offers) throws Exception {
		this.game.gameOffers = offers;
		for(Relocator r : game.relocators) {
			if(!r.isDriving() && r.hasCar() && r.pickuper != null) {
				// relocator is potential candidate.
				for(Offer offer : offers) {
					// If offer is 
					if( offer.group != game.gid && 
						offer.src == game.graph.getNodeName(r.getLastLocation()) && offer.time == game.turn) {
						int distToRelocator = game.rrdist(r.rid, r.pickuper.rid);
						int distToCar = game.rndist(r.rid, r.car.location);
						
						int offerToRelocator = game.rndist(r.pickuper.rid, game.graph.getNodeId(offer.dst));
						int offerToCar = game.nndist(r.car.location, game.graph.getNodeId(offer.dst));
						
						if(offerToRelocator < distToRelocator || offerToCar < distToCar) {
							offer.request(r.rid, Player.this);
						}
					}
				}
			}
		}
	}

	@Override
	public void verify() throws Exception {
		// Not for monday
		
		for ( int i = 0; i < game.offers.size(); i ++)
		{
			Relocator r = game.offerRelocators.get(i);
			RGid [] rgidArr = game.offers.get(i).requests();
			boolean[] verifyArr = new boolean[rgidArr.length];
			int aSeats = 3 - r.car.passengers.size();
			for ( int j = 0; j < aSeats; j++ )
			{
				Random gen = new Random();
				int index = 0;
				do
				{
					index = gen.nextInt(verifyArr.length);
				}while(verifyArr[index]==true);
				verifyArr[index] = true;
			}
			game.offers.get(i).verify(verifyArr, Player.this);
		}
	}
		
	

	@Override
	public DriveRide action() throws Exception {
		List<Ride> rides = new ArrayList<Ride>();
		Set<Integer> relocatorsRiding = new HashSet<Integer>();
		RGid rgid = null;
		
		for(Offer offer: game.gameOffers) {
			for(int i = 0; i < offer.requests().length; i++) {
				rgid = offer.requests()[i];
				// If we're same group and offer was accepted
				if(
					rgid.gid == game.gid && // Same group
					!relocatorsRiding.contains(rgid.rid) && // We didn't already accept offer for relocator. 
					offer.verifications()[i] // Request accepted
							) {
					Game.log("Driver: " + rgid.rid + " making ride for accepted offer.");
					rides.add(new Ride(rgid.rid,offer.group,offer.dst));
					relocatorsRiding.add(rgid.rid);
					continue;
				}
			}
		}
		
		this.driveRide.ride = rides.toArray(new Ride[0]);
		
		return this.driveRide;
	}
	
	private DriveRide generateDriveRide() {
		// before each turn starts, reset scheduling information
		resetAction();
		
		ActionGenerator actGen = new SimpleActionGenerator(game);		
		DriveRide dr = actGen.genDriveRide();		
		// update drive results after each run
		return dr;
	}
	
	private void resetAction() {
		for (int i = 0; i < game.nRelocator; i++)
			game.relocators[i].reset();
		for (int i = 0; i < game.nCar; i++)
			game.cars[i].reset();
	}

}