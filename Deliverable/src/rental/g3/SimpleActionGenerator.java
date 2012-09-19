package rental.g3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
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
		
		return new DriveRide(drives.toArray(new Drive[0]), rides.toArray(new Ride[0]));
	}

	// a simple version: deposit at once
	private boolean depositOrNot(int nextLoc, Stack<Route> routes) {
		if (routes.size() > 1)
			return false;
		
		// if there is empty car there, directly deposit
		if (nextLoc == routes.peek().dst /* && game.getEmptyCars(dstLoc).size() > 0 */)
			return true;
		return false;
	}
	
	private Drive genDriverDrive(int rid) {	
		Relocator r = game.relocators[rid];		
		assert(r.hasCar());
		int nextLoc;
		boolean toDeposit;	
		
		Set<RGid> passengers = r.car.passengers;
		// drop off
		List<RGid> dropoffs = new ArrayList<RGid>();
		for (RGid passenger : passengers) {
			if (passenger.gid == game.gid) {
				if (game.relocators[passenger.rid].car.location == r.location) { // if reach its destination
					dropoffs.add(passenger);
				}
			}			
		}
		dropoffs.removeAll(dropoffs);		
		// pick up
		for (Relocator or : game.relocators) {	
			if (or.pickuper == r && or.location == r.location) {
				passengers.add(new RGid(or.rid, game.gid));
			}
		}
		
		// pop out a route when we reach the destination
		if (r.location == r.firstRoute().dst) 
			r.popRoute();				
		
		// a driver has two options
		// 1. continue on track
		// 2. reroute to pickup new passengers		
		List<Pickup> pickups = findPickups(r);
		if (pickups.size() > 0) {
			// Figure out which pickup we want.
			List<PickupDistance> pickDs = new ArrayList<PickupDistance>(pickups.size());
			for(Pickup p : pickups) {
				pickDs.add(
						new PickupDistance(
								pickups.indexOf(p), 
								game.rndist(r.rid, p.pickLoc) + game.nndist(p.pickLoc, p.dropLoc)));
			}
			
			Collections.sort(pickDs);
			pickDs = pickDs.subList(0, Math.min(3 - passengers.size(), pickDs.size()));
			Collections.reverse(pickDs);
			
			Car car;
			Relocator otherR;
			assert pickDs.size() + passengers.size() <= 3 : pickDs.size();
			for(int i = 0; i < pickDs.size(); i++) {							
				// pop seats.
				Pickup pickup = pickups.get(pickDs.get(i).pid);
				car = game.cars[pickup.cid];
				otherR = game.relocators[pickup.rid];
				
				// We can only assign one car to a passenger
				if (passengers.contains(new RGid(otherR.rid, game.gid)))
					continue;
				
				if (otherR.hasCar())
					continue;
				
				car.assignDriver(otherR);
				otherR.assignCar(car);
				otherR.pickuper = r;
	
				
				r.pushRoute(new Route(r.car.cid, pickup.dropLoc));				
				assert r.firstRoute().dst != pickup.pickLoc : "location conflict";
				//if(r.firstRoute().dst != pickup.pickLoc) {
					r.pushRoute(new Route(r.car.cid, pickup.pickLoc));
				//}
			}
		}
		nextLoc = game.graph.nextMove(r.location, r.firstRoute().dst);
		r.move(RelocatorStatus.ENROUTE, nextLoc);
		r.car.move(nextLoc);
		
		toDeposit = depositOrNot(nextLoc, r.getRoutes());			
		Drive drive = new Drive(rid, r.car.cid, toDeposit, passengers.toArray(new RGid[0]), game.graph.getNodeName(nextLoc));
		
		// if the car is deposited, consider assigning him a new car
		if (toDeposit) {
			r.car.deposit();
			List<Car> emptyCars = game.getEmptyCars(nextLoc);
			if (emptyCars.size() > 0) { // if there are empty cars					
				Car emptyCar = pickCar(emptyCars);			
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
			
			if( !otherR.hasCar() && !otherR.isScheduled() &&
				game.rrdist(otherR.rid, relocator.rid) <= Graph.MAP_MAX_DISTANCE// Fix me
				&& relocator.location != otherR.location) {
				// assert(!otherR.isScheduled()); FAILED
				
				for(Car car : game.cars) {
					if(!car.isDeposit() && !car.isInuse() && game.rndist(relocator.rid, car.location) <= Graph.MAP_MAX_DISTANCE) {
						pickups.add(new Pickup(otherR.rid, car.cid, otherR.location, car.location));
					}
				}				
			}
		}
		
		return pickups;
	}	
	

	private Drive genOtherDrive(int i) {
		Relocator r = game.relocators[i];			
		assert(r.pickuper == null);
		r.move(RelocatorStatus.WAITING, r.location);
		return null;
	}
	
	// is assigned a car, but not at the car
	private Drive genPassengerDrive(int rid) {			
		Relocator r = game.relocators[rid];							
			
		// follows the driver
		if (r.pickuper.car.passengers.contains(new RGid(rid, game.gid))) {			
			r.move(RelocatorStatus.PASSENGER, r.pickuper.location);	
			if (r.pickuper.location == game.cars[r.firstRoute().cid].location) { // the passenger leaves the car here
				r.pickuper.car.passengers.remove(new RGid(rid, game.gid));
				r.pickuper = null;
			}
		}
		else // pickuper not here yet
			r.move(RelocatorStatus.PASSENGER, r.location);		
		return null;
	}
}