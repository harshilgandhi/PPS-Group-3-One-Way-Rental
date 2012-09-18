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
			ClassLoader loader = ToolProvider.getSystemToolClassLoader();
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

	// limit turns (useful in case of infinite loops)
	private static final int turnLimit = 0;

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
		int cars = 50;
		if (args.length > 2)
			cars = Integer.parseInt(args[2]);
		// number of relocators
		int relocators = 20;
		if (args.length > 3)
			relocators = Integer.parseInt(args[3]);
		// turn that score is counted
		int scoreTurn = 10;
		if (args.length > 4)
			scoreTurn = Integer.parseInt(args[4]);
		// print game info
		System.err.println("\n #### Info ####\n");
		System.err.println("Nodes: " + nodes.length);
		System.err.println("Edges: " + edges.length);
		System.err.println("Groups: " + players.length);
		System.err.println("Cars / group: " + cars);
		System.err.println("Relocators / group: " + relocators);
		System.err.println("Score turn: " + scoreTurn);
		// play the game
		int[][] score = play(players, edges, cars, relocators, scoreTurn);	
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
	                            int scoreTurn) throws Exception
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
			List <Offer> offerList = new LinkedList <Offer> ();
			for (int group = 0 ; group != groups.length ; ++group)
				if (toDepositPerGroup[group] != 0) {
					Offer[] curOffers = groups[group].offer();
					for (int i = 0 ; i != curOffers.length ; ++i)
						offerList.add(curOffers[i]);
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
			List <Ride> allRides = new LinkedList <Ride> ();
			for (int group = 0 ; group != groups.length ; ++group) {
				if (toDepositPerGroup[group] == 0) continue;
				Player.DriveRide pair = groups[group].action();
				Drive[] drives = pair.drive;
				Ride[] rides = pair.ride;
				// check sanity of drive actions
				HashSet <RGid> relocatorUsed = new HashSet <RGid> ();
				HashSet <Integer> carUsed = new HashSet <Integer> ();
				for (int i = 0 ; i != drives.length ; ++i) {
					int car = drives[i].car;
					Integer Icar = new Integer(car);
					if (carUsed.contains(Icar))
						throw new Exception("Car already used");
					carUsed.add(Icar);
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
						if (drives[i].taken == 3)
							throw new Exception("Relocators of owner group exceed car capacity");
						drives[i].taken++;
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
					String source = relocatorLocation[group][rid];
					if (!edges.contains(new Edge(source, rides[i].destination)))
						throw new Exception("Invalid source or destination for ride");
				}
			}
			// count and move
			HashSet <RGid> rideDone = new HashSet <RGid> ();
			for (int group = 0 ; group != groups.length ; ++group) {
				if (toDepositPerGroup[group] == 0) continue;
				for (Drive drive : allDrives[group]) {
					// move car and driver
					System.err.print("Car: " + drive.car + " (" + group + "): " + carLocation[group][drive.car] + " -> " + drive.destination);
					if (drive.deposit) System.err.println(" {d}");
					else System.err.println("");
					carLocation[group][drive.car] = drive.destination;
					relocatorLocation[group][drive.driver] = drive.destination;
					RGid drgid = new RGid(drive.driver, group);
					System.err.print(" Driver: " + drive.driver + " (" + group + ")");
					// check if deposit
					if (carDestination[group][drive.car].equals(drive.destination) && drive.deposit) {
						System.err.println(" deposit!");
						carLocation[group][drive.car] = null;
						toDeposit--;
						if (--toDepositPerGroup[group] == 0)
							finalScore[group] = turn;
					} else System.err.println("");
					// move own passengers
					for (RGid rgid : drive.passengers) {
						if (rgid.gid != group) continue;
						relocatorLocation[group][rgid.rid] = drive.destination;
						System.err.println(" Own passenger: " + rgid.rid + " (" + rgid.gid + ")");
					}
					// check for rides of other companies	
					for (Ride ride : allRides) {
						// skip completed rides
						if (ride.executed) continue;
						// stop when car is full
						if (drive.taken == 3) break;
						for (RGid rgid : drive.passengers)
							// check all requirements for other company passenger
							if (rgid.rid == ride.rid && rgid.gid == ride.gid && ride.company == group &&
							    ride.destination.equals(drive.destination) && !rideDone.contains(rgid)) {
								// ride is not completed
								ride.executed = true;
								rideDone.add(rgid);
								drive.taken++;
								System.err.println(" Non-own passenger: " + rgid.rid + " (" + rgid.gid + ")");
								break;
							}
					}
				}
			}
			// open rides for reading
			for (Ride ride : allRides)
				ride.waiting = false;
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
