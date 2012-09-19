package rental.g3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import rental.g3.Relocator;
import rental.g3.Relocator.RelocatorStatus;
import rental.sim.Drive;
import rental.sim.Ride;
import rental.sim.RGid;
import rental.sim.Player.DriveRide;

public class SimpleActionGenerator extends ActionGenerator {
	public SimpleActionGenerator(Game game) { super(game); }

	@Override
	public DriveRide genDriveRide() {
		List<Drive> drives = new ArrayList<Drive>();
		List<Ride> rides = new ArrayList<Ride>();		
		Drive drive;
			
		// Three Passes
		// Pass 1: first schedule those relocators that are going to drive in this round		
		for (int i = 0; i < game.nRelocator;  i++) {
			// skip scheduled relocators and those have don't have cars	
			if (game.relocators[i].isScheduled() || !game.relocators[i].isDriving()) 
				continue;				
			
			RGid rgid = new RGid(i, game.gid);
			for(Car car : game.cars) {
				if( car.passengers.contains(rgid)) {
					Game.log("Passenger: " + i + " is now driving...");
					car.passengers.remove(rgid);
					
					if(game.relocators[i].pickuper != null) {
						game.relocators[i].pickuper.removeDropOffRoutes(i);
						game.relocators[i].pickuper = null;
						Game.log("... Removing as passenger from: " + car.driver.rid);
					}
				}
			}
			
			drive = genDriverDrive(i);	
			assert(drive!=null);		
			drives.add(drive);
		}		
		// Pass 2: schedule passengers
		for (int i = 0; i < game.nRelocator; i++) {
			if (game.relocators[i].isScheduled() || !game.relocators[i].hasCar())
				continue;			
			drive = genPassengerDrive(i);
			if (drive!=null)
				drives.add(drive);
		}
			
		// Pass 3: schedule other relocators
		for (int i = 0; i < game.nRelocator; i++) {
			if (game.relocators[i].isScheduled())
				continue;		
			drive = genOtherDrive(i);
			if (drive != null)
				drives.add(drive);
		}
		if(drives.size() == 0) {
			throw new RuntimeException("No drive objects but game is not over.");
		}
		
		return new DriveRide(drives.toArray(new Drive[0]), rides.toArray(new Ride[0]));
	}

	// a simple version: deposit at once
	private boolean depositOrNot(int nextLoc, Stack<Route> routes) {
		if (routes.size() > 1)
			return false;
		
		// if there is empty car there, directly deposit
		if (nextLoc== routes.peek().dst /* && game.getEmptyCars(dstLoc).size() > 0 */)
			return true;
		return false;
	}
	
	private Drive genDriverDrive(int rid) {	
		Relocator r = game.relocators[rid];	
		assert(r.hasCar());
		int nextLoc;
		boolean toDeposit;
		
		List<RGid> passengers = r.car.passengers;
		// drop off
		List<RGid> dropoffs = new ArrayList<RGid>();
		for (RGid passenger : passengers) {
			if (passenger.gid == game.gid) {
				Relocator otherR = game.relocators[passenger.rid];
				if (otherR.isDriving()) { // if reach its destination
					Game.log("Driver: " + r.rid + " dropped off passenger: " + passenger.rid);

					dropoffs.add(passenger);
					game.relocators[passenger.rid].pickuper = null; // reset pickuper
					r.removeDropOffRoutes(otherR.rid);
					Game.log("Next destination: " + game.graph.getNodeName(r.firstRoute().dst));
				}
			}			
		}
		passengers.removeAll(dropoffs);		
		
		int ourPassengers = 0;
		for(Relocator otherR : game.relocators) {
			if(otherR.pickuper == r) {
				ourPassengers++;
			}
		}
		// a driver has two options
		// 1. continue on track
		// 2. reroute to pickup new passengers		
		List<Pickup> pickups = findPickups(r);
		if (ourPassengers == 0) // Only do 1 passenger.
		if (pickups.size() > 0) {
			// Figure out which pickup we want.
			List<PickupDistance> pickDs = new ArrayList<PickupDistance>(pickups.size());
			for(Pickup p : pickups) {
				pickDs.add(
						new PickupDistance(
								pickups.indexOf(p), 
								game.rndist(r.rid, p.pickLoc) + game.nndist(p.pickLoc, p.dropLoc)));
			}
			
			int minSize = Math.min(3 - passengers.size(), pickDs.size());
			
			Collections.sort(pickDs);
			pickDs.subList(0, minSize);
			Collections.reverse(pickDs);
			
			Car car;
			Relocator otherR;
			for(int i = 0; i < minSize && ourPassengers < 1; i++) {
				// pop seats.
				Pickup pickup = pickups.get(pickDs.get(i).pid);
				
				if(!game.relocators[pickup.rid].hasCar()) { // Because he may have already qualified for a better pickup route.
					car = game.cars[pickup.cid];
					
					if(r.baseDestination == pickup.dropLoc && !r.hasChainCar()) {
						// If the destination of this car happens to be our current destination...
						// chain it to this car.
						Game.log("Driver: " + r.rid + " is claming car : " + car.cid + " for his chain.");
						car.assignDriver(r);
						r.setChainCar(car.cid);
						continue;
					} else {
						otherR = game.relocators[pickup.rid];
						car.assignDriver(otherR);
						otherR.assignCar(car);
						otherR.pickuper = r;
						
						Game.log("Adding pickup for " + r.rid + ": " + pickup);
						
						if(r.firstRoute().dst != pickup.dropLoc) {
							r.pushRoute(new Route(r.car.cid, pickup.dropLoc, otherR.rid, Route.DROPOFF));
						}
						
						
						
						Game.log("Pushed pickup route to: " + game.graph.getNodeName(pickup.pickLoc));
						r.pushRoute(new Route(r.car.cid, pickup.pickLoc, otherR.rid, Route.PICKUP));
						
						ourPassengers++;
					}
					
					
				}
			}
		}
		
		// pick up
		// This goes after because sometimes we pickup in our immediate spot.
		for (Relocator or : game.relocators) {
			// If we arrive at same location as or and we're marked as pick up and
			// or is not already our passenger.  pick his ass up.
			if (	or.pickuper == r && 
					or.getLocation() == r.getLocation() && 
					!passengers.contains(new RGid(or.rid, game.gid))
					) {
				if(or.isDriving()) {
					// this was a coincidence, the driver hasn't been scheduled yet but we still think
					// he's a passenger since our locations and pickups match.
					or.pickuper = null;
					continue;
				}
				
				passengers.add(new RGid(or.rid, game.gid));
				Game.log("Driver: " + r.rid + " We've arrived to pickup:" + or.rid);
				r.removePickupRoutes(or.rid);
				Game.log("Next destination: " + game.graph.getNodeName(r.firstRoute().dst));
			}
		}
		
		nextLoc = game.graph.nextMove(r.getLocation(), r.firstRoute().dst);
		r.move(RelocatorStatus.ENROUTE, nextLoc);
		r.car.move(nextLoc);
		
		if(r.getLastLocation() == r.getLocation()) {
			Game.log("Driver: " + r.rid + " was schedule for illegal move.");
		}
		
		Game.log("Driver: "+ r.rid + " going to " + game.graph.getNodeName(nextLoc));
		
		toDeposit = depositOrNot(nextLoc, r.getRoutes());
		
		
		if(toDeposit) {
			// Determine if we're the last available car.
			// This can happen if all cars are deposited at the same time.
			boolean moreRelocators = false;
			for(Relocator otherR : game.relocators) {
				if(otherR != r && otherR.hasCar()) {
					moreRelocators = true;
					break;
				}
			}
			
			List<Car> availCars = new ArrayList<Car>(Arrays.asList(game.cars));
			for(int i = 0; i < availCars.size();) {
				if(availCars.get(i).isDeposit()) {
					availCars.remove(i);
				} else {
					i++;
				}
			}
			
			if(!moreRelocators && !(availCars.size() <= 1)) {
				// This is the last relocator with a car so don't deposit.
				Game.log("Driver: " + r.rid + " is last driver so refusing to deposit.");
				toDeposit = false;
			}
			
			
			Game.log("Driver: " + r.rid + " will deposit car.");
			if(passengers.size() > 0) {
				// this car gets deposited so our passenger sits in it
				// and is no longer updated since we use the pickup relocator
				// to see if he's a passenger (which by now has a new car).
				// so we update him here.
				for(RGid rgid : passengers) {
					if(rgid.gid == game.gid) {
						game.relocators[rgid.rid].pickuper = null;
						game.relocators[rgid.rid].move(RelocatorStatus.ENROUTE, nextLoc);
					}
				}
			}
		}
		
		Drive drive = new Drive(rid, r.car.cid, toDeposit, passengers.toArray(new RGid[0]), game.graph.getNodeName(nextLoc));
		
		// if the car is deposited, consider assigning him a new car
		if (toDeposit) {
			r.car.deposit();
			
			Car emptyCar = null;
			if(r.hasChainCar()) { // has a car already reserved at location.
				emptyCar = game.cars[r.getChainCar()];
				Game.log("Driver: " + r.rid + " taking already chained car: " + emptyCar.cid);
				
			} else {
				List<Car> emptyCars = game.getEmptyCars(nextLoc);
				if (emptyCars.size() > 0) { // if there are empty cars
					emptyCar = pickCar(emptyCars);
					Game.log("Driver: " + r.rid + " found car " + emptyCar.cid + " at " + game.graph.getNodeName(nextLoc) + " and will take it.");
				}
			}
			
			if(emptyCar != null) {
				r.assignCar(emptyCar);
				emptyCar.assignDriver(r);
			}
			
		}		
		return drive;		
	}
	
	// pick a car when we have more than one available cars at the location
	private Car pickCar(List<Car> emptyCars) {
		int mindist = Integer.MAX_VALUE;
		Car topick = null;
		int dist;
		for (Car car : emptyCars) {
			if ((dist = game.nndist(car.source, car.destination)) < mindist) {
				mindist = dist;
				topick = car;
			}
		}
		return topick;
	}
	
	private List<Pickup> findPickups(Relocator relocator) {
		
		// if there is a waiting relocator (!hasCar) within distance d
		// && is not scheduled a pickup
		// && there is an available car within distance d from the pickup location
		
		List<Pickup> pickups = new ArrayList<Pickup>();
		
		for(Relocator otherR : game.relocators) {
			
			// TODO: Optimize pickups until turn limit then relax perimeter to entire map.
			if( !otherR.hasCar() && 
				!otherR.isScheduled() &&
				game.rrdist(otherR.rid, relocator.rid) <= Graph.MAP_MAX_DISTANCE
				) {
				assert(!otherR.isScheduled());
				
				for(Car car : game.cars) {
					if(!car.isInuse() && !car.isDeposit && game.rndist(relocator.rid, car.location) <= Graph.MAP_MAX_DISTANCE) {
						pickups.add(new Pickup(otherR.rid, car.cid, otherR.getLocation(), car.location));
					}
				}
				
			}
		}
		
		return pickups;
	}	
	

	private Drive genOtherDrive(int i) {
		Relocator r = game.relocators[i];			
		assert(r.pickuper == null);
		r.move(RelocatorStatus.WAITING, r.getLocation());
		return null;
	}
	
	// is assigned a car, but not at the car
	private Drive genPassengerDrive(int rid) {			
		Relocator r = game.relocators[rid];	
		
		// TODO: Choose pickups optimized from Waiting perspective.
		
		if(r.pickuper == null || r.pickuper.car == null) {
			// We were kicked out of car in previous step, so drive!
			return null;
//			return genDriverDrive(rid);
		}
		Game.log("passengerDrive for: " + rid);
		// follows the driver
		if (r.pickuper.car.passengers.contains(new RGid(rid, game.gid)))
			r.move(RelocatorStatus.PASSENGER, r.pickuper.getLocation());		
		else  // pickuper not here yet
			r.move(RelocatorStatus.PASSENGER, r.getLocation());		
		return null;
	}
}