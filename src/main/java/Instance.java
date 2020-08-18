import java.io.IOException;
import java.util.Random;
import java.io.*;

class Instance {
	int NUMBER_OF_CUSTOMERS;
	int NUMBER_OF_VEHICLES;
	double VEHICLE_CAPACITY;
	double CONVERTER_DISTANCE_TO_COST = 0;
	double CONVERTER_DISTANCE_TO_TIME = 1;
	Node[] NODES;
	double[][] distanceMatrix;

	public Instance (String readIn) throws IOException {

		if (readIn == "random") {
			Random ran = new Random(151190); // sein Geburtstag? :D
			// Initialise
			NUMBER_OF_CUSTOMERS = 20;
			NUMBER_OF_VEHICLES = 10; // 10
			VEHICLE_CAPACITY = 400; // 400
			CONVERTER_DISTANCE_TO_COST = 0;
			CONVERTER_DISTANCE_TO_TIME = 1;
			// Depot Coordinates
			int Depot_x = 50;
			int Depot_y = 50;

			// Create Random Customers
			NODES = new Node[NUMBER_OF_CUSTOMERS + 2]; // Contains all NODES including the depot
			Node depot = new Node(Depot_x, Depot_y); // Initialize the depot node

			NODES[0] = depot; // The first node is the depot
			NODES[(NUMBER_OF_CUSTOMERS + 1)] = depot;

			// Create customer NODES with random coordinates and demand
			for (int i = 1; i <= NUMBER_OF_CUSTOMERS; i++) {
				NODES[i] = new Node(i, // Customer id
						ran.nextInt(100), // Random X-Coordinate
						ran.nextInt(100), // Random Y-Coordinate
						4 + ran.nextInt(100), // Random Profit
						ran.nextInt(30) // Random required Service Time
				);
			}
		}

		else {
			String pathOfFile = "AlleInstanzen/" + readIn + ".txt";
			FileReader fr = new FileReader(pathOfFile);
			BufferedReader br = new BufferedReader(fr);

			String zeile1 = br.readLine();
			String[] split = zeile1.split(" ");
			NUMBER_OF_CUSTOMERS = Integer.parseInt(split[1]) - 2;
			String zeile2 = br.readLine();
			split = zeile2.split(" ");
			NUMBER_OF_VEHICLES = Integer.parseInt(split[1]) + 1;
			String zeile3 = br.readLine();
			split = zeile3.split(" ");
			VEHICLE_CAPACITY = Double.parseDouble(split[1]);

			NODES = new Node[NUMBER_OF_CUSTOMERS + 2];
			int customerId = 0;

			String zeile = " ";
			while ((zeile = br.readLine()) != null) {

				split = zeile.split("	");
				double NODE_X = Double.parseDouble(split[0]);
				double NODE_Y = Double.parseDouble(split[1]);
				double profit = Double.parseDouble(split[2]);
				NODES[customerId] = new Node(customerId, NODE_X, NODE_Y, profit, 0);
				customerId++;
			}

			br.close();

		}

		NODES[0].isRouted = true;
		NODES[NODES.length - 1].isRouted = true;
	}

	public void resetStatusRouted() {
		for (int i = 1; i < NODES.length - 1; i++) {
			NODES[i].isRouted = false;
		}
	}

	public void setStatusRoutedOfNode(Node node) {
		NODES[node.NodeId].isRouted = true;
	}

	public void updateStatusIsRouted(Vehicle[] currentSolution) {
		for (int i = 0; i < currentSolution.length; i++)
			for (int j = 1; j < currentSolution[i].Route.size() - 1; j++) {
				NODES[currentSolution[i].Route.get(j).NodeId].isRouted = true;
			}
	}

	public void calculateDistanceMatrix() {
		// Calculate the distance matrix
		distanceMatrix = new double[this.NODES.length][this.NODES.length]; // Instantiate the distance matrix
		double Delta_x, Delta_y; // Difference in x-coordinates

		for (int i = 0; i <= this.NODES.length - 1; i++) {
			for (int j = i + 1; j <= this.NODES.length - 1; j++) // The table is symmetric to the first diagonal
			{ // Use this to compute distances in O(n/2)

				Delta_x = (this.NODES[i].NODE_X - this.NODES[j].NODE_X);
				Delta_y = (this.NODES[i].NODE_Y - this.NODES[j].NODE_Y);

				double distance = Math.sqrt((Delta_x * Delta_x) + (Delta_y * Delta_y)); // Euclidean distance

				// distance = Math.round(distance); //Distance is Casted in Integer
				// distance = Math.round(distance*100.0)/100.0; //Distance in double

				// Symmetric distance matrix:
				distanceMatrix[i][j] = distance;
				distanceMatrix[j][i] = distance;
			}
		}
	}

	public void printInstanceInformation() {
		// Print the distance matrix:
		String printDistanceMatrix = new String();
		printDistanceMatrix = "distanceMatrix =[";

		System.out.println("maxNumbVeh = " + (NUMBER_OF_VEHICLES) + ";\n");
		System.out.println("maxNumbCust = " + NUMBER_OF_CUSTOMERS + ";\n");

		String serviceTimeData = new String();
		String profitData = new String();
		serviceTimeData = "serviceTime = [ ";
		profitData = "profit = [ ";
		for (int z = 0; z <= NUMBER_OF_CUSTOMERS + 1; z++) {
			if (z == (NUMBER_OF_CUSTOMERS + 1)) {
				serviceTimeData += NODES[z].serviceTime + " ];";
				profitData += NODES[z].profit + " ];";
			} else {
				serviceTimeData += NODES[z].serviceTime + ", ";
				profitData += NODES[z].profit + ", ";
			}
		}
		System.out.println(serviceTimeData + "\n");
		System.out.println(profitData + "\n");
		System.out.println("capacityLimit = " + VEHICLE_CAPACITY + ";\n");
		System.out.println("TravelCostConv =" + CONVERTER_DISTANCE_TO_COST + ";\n");
		System.out.println("TravelTimeConv = " + CONVERTER_DISTANCE_TO_TIME + ";\n");
		for (int i = 0; i <= NUMBER_OF_CUSTOMERS + 1; i++) {
			printDistanceMatrix += " [";
			for (int j = 0; j <= NUMBER_OF_CUSTOMERS + 1; j++) {
				if (j == NUMBER_OF_CUSTOMERS + 1) {
					if (i == NUMBER_OF_CUSTOMERS + 1) {
						printDistanceMatrix += distanceMatrix[i][j] + "]];\n";
					} else
						printDistanceMatrix += distanceMatrix[i][j] + "], \n";
				} else
					printDistanceMatrix += distanceMatrix[i][j] + ", ";
				System.out.print(printDistanceMatrix);
				printDistanceMatrix = "";
			}
		}
	}
}