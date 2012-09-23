package rental.sim;

import java.io.*;
import java.util.*;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

public class Rental {

	// list files below a certain directory
	// can filter those having a specific extension constraint
	private static List <File> directoryFiles(String path, String extension) {
		List <File> allFiles = new ArrayList <File> ();
		allFiles.add(new File(path));
		int index = 0;
		while (index != allFiles.size()) {
			File currentFile = allFiles.get(index);
			if (currentFile.isDirectory()) {
				allFiles.remove(index);
				for (File newFile : currentFile.listFiles())
					allFiles.add(newFile);
			} else if (!currentFile.getPath().endsWith(extension))
				allFiles.remove(index);
			else index++;
		}
		return allFiles;
	}

	// compile and load players dynamically
	private static Player[] loadPlayers(String txtPath) {
		// list of players
		List <Player> playersList = new LinkedList <Player> ();
		try {
			// get file of players
			BufferedReader in = new BufferedReader(new FileReader(new File(txtPath)));
			// get tools
			ClassLoader loader = ClassLoader.getSystemClassLoader();
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
			// load players
			String group;
			while ((group = in.readLine()) != null) {
				System.err.println("Group: " + group);
				// delete all class files
				List <File> classFiles = directoryFiles("rental/" + group, ".class");
				System.err.print("Deleting " + classFiles.size() + " class files...   ");
				for (File file : classFiles)
					file.delete();
				System.err.println("OK");
				// compile all files
				List <File> javaFiles = directoryFiles("rental/" + group, ".java");
				System.err.print("Compiling " + javaFiles.size() + " source files...   ");
				Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromFiles(javaFiles);
				boolean ok = compiler.getTask(null, fileManager, null, null, null, units).call();
				if (!ok) throw new Exception("compile error");
				System.err.println("OK");
				// load class
				System.err.print("Loading player class...   ");
				Class playerClass = loader.loadClass("rental." + group + ".Player");
				System.err.println("OK");
				// set name of player and append on list
				Player player = (Player) playerClass.newInstance();
				playersList.add(player);
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
		return playersList.toArray(new Player[0]);
	}

	// clear string from white space
	private static String clearString(String mix)
	{
		StringBuffer buf = new StringBuffer();
		String[] parts = mix.split(" ");
		boolean first = true;
		for (String part : parts)
			if (!part.isEmpty()) {
				if (!first)
					buf.append(" ");
				else
					first = false;
				buf.append(part);
			}
		return buf.toString();
	}

	// load map (dot format) from file in provided path
	private static Edge[] loadMap(String mapPath)
	{
		// list of edges
		List <Edge> edges = new LinkedList <Edge> ();
		boolean start = true;
		boolean end = false;
		try {
			// get map file and read edges
			BufferedReader in = new BufferedReader(new FileReader(new File(mapPath)));
			String edge;
			while ((edge = in.readLine()) != null) {
				// clear spaces
				edge = clearString(edge);
				// omit empty
				if (edge.isEmpty()) continue;
				// already ended
				if (end) {
					System.err.println("End of file expected after \"}\"");
					return null;
				}
				// omit start
				if (start) {
					if (!edge.equals("graph {")) {
						System.err.println("Missing \"graph {\"");
						return null;
					}
					start = false;
					continue;
				}
				// check end
				if (edge.equals("}")) {
					end = true;
					continue;
				}
				// get source and destination
				String[] srcDst = edge.split("--");
				if (srcDst.length != 2) {
					System.err.println(edge + ": Invalid edge");
					return null;
				}
				// clear source and destination
				String src = clearString(srcDst[0]);
				String dst = clearString(srcDst[1]);
				// append in list
				edges.add(new Edge(src, dst));
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
		// must have ended
		if (!end) {
			System.err.println("Missing ending \"}\"");
			return null;
		}
		// print info
		System.err.println("Loaded map:  (" + edges.size() + " edges)");
		for (Edge srcDst : edges)
			System.err.println("  " + srcDst.source + " <-> " + srcDst.destination);
		return edges.toArray(new Edge[0]);
	}

	// assemble all nodes of map
	private static String[] edgesToNodes(Edge[] edges)
	{
		HashSet <String> nodes = new HashSet <String> ();
		for (Edge edge : edges) {
			nodes.add(edge.source);
			nodes.add(edge.destination);
		}
		return nodes.toArray(new String[0]);
	}

	// shuffle rides
	private static void shuffle(Ride[] rides, Random gen)
	{
		for (int i = 0 ; i != rides.length ; ++i) {
			int r = gen.nextInt(rides.length - i) + i;
			Ride temp = rides[r];
			rides[r] = rides[i];
			rides[i] = temp;
		}
	}

	// main function
	// 1st argument is list of players
	// 2nd argument is map file (dot format)
	// 3rd argument is number of cars per group
	// 4th argument is number of relocators per group
	// 5th argument is the specified turn at which score is counted
	public static void main(String[] args) throws Exception
	{
		// load players
		String txtFile = "rental/players.list";
		if (args.length > 0) txtFile = args[0];
		Player[] players = loadPlayers(txtFile);
		if (players == null)
			System.exit(1);
		// load map
		String mapFile = "rental/maps/europe.dot";
		if (args.length > 1) mapFile = args[1];
		Edge[] edges = loadMap(mapFile);
		// get nodes of map
		String[] nodes = edgesToNodes(edges);
		// number of cars
		int cars = 1;
		if (args.length > 2)
			cars = Integer.parseInt(args[2]);
		// number of relocators
		int relocators = 1;
		if (args.length > 3)
			relocators = Integer.parseInt(args[3]);
		// turn that score is counted
		int scoreTurn = 10;
		if (args.length > 4)
			scoreTurn = Integer.parseInt(args[4]);
		// turn limit (for debugging purposes)
		int turnLimit = 0;
		if (args.length > 5)
			turnLimit = Integer.parseInt(args[5]);
		// print game info
		System.err.println("\n### Information ###\n");
		System.err.println("Nodes: " + nodes.length);
		System.err.println("Edges: " + edges.length);
		System.err.println("Groups: " + players.length);
		System.err.println("Cars / group: " + cars);
		System.err.println("Relocators / group: " + relocators);
		System.err.println("Score turn: " + scoreTurn);
		if (turnLimit > 0)
			System.err.println("Turn limit: " + turnLimit);
		// play the game
		int[][] score = play(players, edges, cars, relocators, scoreTurn, turnLimit);	
		int[] turnScore = score[0];
		int[] finalScore = score[1];
		// print game results
		if (turnScore != null) {
			System.err.println("\n### Turn Score (# of Deposited Cars at Turn " + scoreTurn + ") ###");
			for (int g = 0 ; g != players.length ; ++g)
				System.err.println(" Group " + players[g].name() + " (" + g + "): " + turnScore[g]);
		}
		System.err.println("\n### Final Score (# of Turns to Deposit All Cars) ###");
		for (int g = 0 ; g != players.length ; ++g)
			if (finalScore[g] == 0)
				System.err.println(" Group " + players[g].name() + " (" + g + "): N/A");
			else
				System.err.println(" Group " + players[g].name() + " (" + g + "): " + finalScore[g]);
	}

	// play the game and return the score
	private static int[][] play(Player[] groups, Edge[] edgesArr,
	                            int carsPerGroup, int relsPerGroup,
	                            int scoreTurn, int turnLimit) throws Exception
	{
		// keep cars and relocators by id
		String[][] carDestination = new String[groups.length][carsPerGroup];
		// keep locations of cars and relocators
		String[][] carLocation = new String[groups.length][carsPerGroup];
		String[][] relocatorLocation = new String[groups.length][relsPerGroup];
		// store map and get reversed edges
		HashSet <Edge> edges = new HashSet <Edge> ();
		for (Edge e : edgesArr) {
			edges.add(e);
			edges.add(e.reverse());
		}
		edgesArr = edges.toArray(new Edge[0]);
		// count total deposited
		int toDeposit = carsPerGroup * groups.length;
		int[] toDepositPerGroup = new int [groups.length];
		Arrays.fill(toDepositPerGroup, carsPerGroup);
		// set zero scores
		int[] turnScore = null;
		int[] finalScore = new int [groups.length];
		Arrays.fill(finalScore, 0);
		// set random car locations
		String[] nodes = edgesToNodes(edgesArr);
		Random gen = new Random();
		for (int g = 0 ; g != groups.length ; ++g) {
			for (int c = 0 ; c != carsPerGroup ; ++c) {
				// pick random source and destination
				int s = gen.nextInt(nodes.length);
				int d = gen.nextInt(nodes.length);
				while (s == d)
					s = gen.nextInt(nodes.length);
				String src = nodes[s];
				String dst = nodes[d];
				// save car
				carDestination[g][c] = dst;
				carLocation[g][c] = src;
			}
			// call player to place his relocators
			String[] carLocCopy = new String [carLocation[g].length];
			for (int i = 0 ; i != carLocation[g].length ; ++i)
				carLocCopy[i] = carLocation[g][i];
			String[] carDestCopy = new String [carDestination[g].length];
			for (int i = 0 ; i != carDestination[g].length ; ++i)
				carDestCopy[i] = carDestination[g][i];
			Edge[] edgesCopy = new Edge[edgesArr.length];
			for (int i = 0 ; i != edgesArr.length ; ++i)
				edgesCopy[i] = new Edge(edgesArr[i].source, edgesArr[i].destination);
			String[] relLocations = groups[g].place(relsPerGroup, carLocCopy, carDestCopy,
			                                        edgesCopy, groups.length, scoreTurn);
			// check number of placements
			if (relLocations.length != relsPerGroup)
				throw new Exception("Invalid number of locations");
			// store relocator locations
			for (int r = 0 ; r != relsPerGroup ; ++r)
				relocatorLocation[g][r] = relLocations[r];
		}
		for (int turn = 1 ; toDeposit != 0 && turn != turnLimit ; ++turn) {
			// print verbose info helpful for knowing what's going on
			System.err.println("\n########## Turn " + turn + " ##########\n");
			for (int group = 0 ; group != groups.length ; ++group) {
				if (toDepositPerGroup[group] == 0) continue;
				System.err.println("### Group " + groups[group].name() + " (" + group + ") ###");
				System.err.println("  Cars:");
				for (int car = 0 ; car != carsPerGroup ; ++car)
					if (carLocation[group][car] != null)
						System.err.println("    (" + car + ") " + carLocation[group][car] + " [-> " + carDestination[group][car] + "]");
				System.err.println("  Relocators:");
				for (int rel = 0 ; rel != relsPerGroup ; ++rel)
					System.err.println("    (" + rel + ") " + relocatorLocation[group][rel]);
				System.err.println("");
			}
			// get offers and accumulate in a single array
			int totalOffers = 0;
			Offer[][] offerArr = new Offer [groups.length][];
			List <Offer> offerList = new LinkedList <Offer> ();
			for (int group = 0 ; group != groups.length ; ++group)
				if (toDepositPerGroup[group] != 0) {
					offerArr[group] = groups[group].offer();
					for (int i = 0 ; i != offerArr[group].length ; ++i)
						offerList.add(offerArr[group][i]);
				}
			Offer[] offers = offerList.toArray(new Offer[0]);
			// ask for replies in the offers
			for (Offer o : offers)
				o.requestsAllowed = true;
			for (int group = 0 ; group != groups.length ; ++group)
				if (toDepositPerGroup[group] != 0)
					groups[group].request(offers);
			// ask for players to verify replies
			for (Offer o : offers) {
				o.requestsAllowed = false;
				o.seeRequestsAllowed = true;
				o.verifyAllowed = true;
			}
			for (int group = 0 ; group != groups.length ; ++group)
				if (toDepositPerGroup[group] != 0)
					groups[group].verify();
			// reply no to non replied cases
			for (Offer o : offers)
				o.denyNonReplied();
			for (Offer o : offers) {
				o.verifyAllowed = false;
				o.seeVerificationsAllowed = true;
			}
			// ask for players to post drive and ride commands
			Drive[][] allDrives = new Drive[groups.length][];
			Vector <Ride> allRides = new Vector <Ride> ();
			HashSet <RGid> relocatorUsed = new HashSet <RGid> ();
			for (int group = 0 ; group != groups.length ; ++group) {
				if (toDepositPerGroup[group] == 0) continue;
				Player.DriveRide pair = groups[group].action();
				Drive[] drives = pair.drive;
				Ride[] rides = pair.ride;
				// set self group
				for (Drive drive : drives)
					drive.group = group;
				for (Ride ride : rides)
					ride.gid = group;
				// count own passengers
				int[] count = new int [drives.length];
				Arrays.fill(count, 0);
				// print offers
				System.err.println("### Group " + groups[group].name() + " (" + group + ") ###");
				System.err.println("  Offers:");
				for (Offer offer : offerArr[group])
					System.err.println("    " + offer.toStringSim());
				// print drives
				System.err.println("  Drives:");
				for (Drive drive : drives)
					System.err.println("    " + drive.toStringSim());
				// print rides
				System.err.println("  Rides:");
				for (Ride ride : rides)
					System.err.println("    " + ride.toStringSim());
				System.err.println("");
				// check sanity of drive actions
				HashSet <Integer> carUsed = new HashSet <Integer> ();
				for (int i = 0 ; i != drives.length ; ++i) {
					int car = drives[i].car;
					if (carUsed.contains(car))
						throw new Exception("Car already used");
					carUsed.add(car);
					int driver = drives[i].driver;
					if (driver < 0 || driver >= relsPerGroup)
						throw new Exception("Driver invalid");
					String dest = drives[i].destination;
					if (car < 0 || car >= carsPerGroup)
						throw new Exception("Car invalid");
					if (carLocation[group][car] == null)
						throw new Exception("Car deposited");
					String src = carLocation[group][car];
					if (!edges.contains(new Edge(src, dest)))
						throw new Exception("Car move invalid: " + src + " -> " + dest);
					if (relocatorUsed.contains(new RGid(driver, group)))
						throw new Exception("Driver already used by owner group");
					relocatorUsed.add(new RGid(driver, group));
					if (!relocatorLocation[group][driver].equals(carLocation[group][car]))
						throw new Exception("Driver and car are not in the same location");
					for (RGid rgid : drives[i].passengers) {
						if (rgid.rid < 0 || rgid.rid >= relsPerGroup)
							throw new Exception("Passenger relocator invalid");
						if (rgid.gid != group) continue;
						if (relocatorUsed.contains(rgid))
							throw new Exception("Relocator already used by owner group");
						relocatorUsed.add(rgid);
						if (!relocatorLocation[group][rgid.rid].equals(relocatorLocation[group][driver]))
							throw new Exception("Relocator of owner group is in the same location with car and driver");
						if (count[i]++ >= 3)
							throw new Exception("Relocators of owner group exceed car capacity");
					}
				}
				allDrives[group] = drives;
				// check sanity of ride actions
				for (int i = 0 ; i != rides.length ; ++i) {
					int rid = rides[i].rid;
					int carOwnerGroup = rides[i].company;
					allRides.add(rides[i]);
					rides[i].gid = group;
					if (rid < 0 || rid >= relsPerGroup)
						throw new Exception("Ride relocator invalid");
					if (relocatorUsed.contains(new RGid(rid, group)))
						throw new Exception("Relocator already scheduled to move with his/her own company's car");
					if (carOwnerGroup < 0 || carOwnerGroup >= groups.length)
						throw new Exception("Invalid company to provide ride");
					if (group == carOwnerGroup)
						throw new Exception("Rides must not be specified for own company");
					Edge edge = new Edge(relocatorLocation[group][rid], rides[i].destination);
					if (!edges.contains(edge))
						throw new Exception("Invalid source or destination for ride: " + edge);
				}
			}
			// set finished groups
			Drive[] empty = new Drive[0];
			for (int group = 0 ; group != groups.length ; ++group)
				if (toDepositPerGroup[group] == 0)
					allDrives[group] = empty;
			// move cars
			for (int group = 0 ; group != groups.length ; ++group)
				for (Drive drive : allDrives[group]) {
					drive.source = carLocation[group][drive.car];
					// move car and driver
					carLocation[group][drive.car] = drive.destination;
					relocatorLocation[group][drive.driver] = drive.destination;
					RGid drgid = new RGid(drive.driver, group);
					// check if deposit
					if (carDestination[group][drive.car].equals(drive.destination) && drive.deposit) {
						carLocation[group][drive.car] = null;
						toDeposit--;
						if (--toDepositPerGroup[group] == 0)
							finalScore[group] = turn;
					}
					// move own passengers
					for (RGid rgid : drive.passengers)
						if (rgid.gid == group) {
							relocatorLocation[group][rgid.rid] = drive.destination;
							drive.movedPassengers.add(rgid);
						}
				}
			// create drive map
			HashMap <Edge, HashMap <Integer, ArrayList <Drive>>> driveMap = new HashMap <Edge, HashMap <Integer, ArrayList <Drive>>> ();
			for (int group = 0 ; group != groups.length ; ++group)
				for (Drive drive : allDrives[group])
					// add in edge map
					if (drive.movedPassengers.size() < 3) {
						Edge edge = new Edge(drive.source, drive.destination);
						if (!driveMap.containsKey(edge))
							driveMap.put(edge, new HashMap <Integer, ArrayList <Drive>> ());
						if (!driveMap.get(edge).containsKey(group))
							driveMap.get(edge).put(group, new ArrayList <Drive> ());
						driveMap.get(edge).get(group).add(drive);
					}
			// check rides
			Ride[] rides = allRides.toArray(new Ride[0]);
			shuffle(rides, gen);
			for (Ride ride : rides) {
				Edge edge = new Edge(relocatorLocation[ride.gid][ride.rid], ride.destination);
				// number of possible drives
				if (driveMap.get(edge) != null && driveMap.get(edge).get(ride.company) != null) {
					ArrayList <Drive> list = driveMap.get(edge).get(ride.company);
					ArrayList <Integer> okList = new ArrayList <Integer> ();
					// check passenger lists
					RGid rgid = new RGid(ride.rid, ride.gid);
					for (int i = 0 ; i != list.size() ; ++i)
						if (list.get(i).passengers.contains(rgid))
							okList.add(i);
					if (!okList.isEmpty()) {
						// pick a random element from the ok list
						int index = okList.get(gen.nextInt(okList.size()));
						Drive drive = list.get(index);
						// remove drive if full
						drive.movedPassengers.add(rgid);
						if (drive.movedPassengers.size() >= 3) {
							list.remove(index);
							if (list.isEmpty()) {
								driveMap.get(edge).remove(ride.company);
								if (driveMap.get(edge).isEmpty())
									driveMap.remove(edge);
							}
						}
						// modify location and set accepted
						relocatorLocation[ride.gid][ride.rid] = ride.destination;
						ride.executed = true;
					}
				}
				ride.waiting = false;
			}
			// print info
			for (int group = 0 ; group != groups.length ; ++group)
				for (Drive drive : allDrives[group]) {
					System.err.println("Car " + drive.car + " (" + drive.group + ") " + drive.source + " -> " + drive.destination);
					System.err.println("  Driver " + drive.driver + " (" + drive.group + ")");
					for (RGid rgid : drive.movedPassengers)
						if (rgid.gid == drive.group)
							System.err.println("  Own Passenger " + rgid.rid + " (" + rgid.gid + ")");
						else
							System.err.println("  Non-Own Passenger " + rgid.rid + " (" + rgid.gid + ")");
					if (carDestination[drive.group][drive.car].equals(drive.destination))
						System.err.println("    !!! Deposit !!!");
				}
			// copy turn score
			if (turn == scoreTurn) {
				turnScore = new int [groups.length];
				for (int group = 0 ; group != groups.length ; ++group)
					turnScore[group] = carsPerGroup - toDepositPerGroup[group];
			}
		}
		return new int [][] {turnScore, finalScore};
	}
}
