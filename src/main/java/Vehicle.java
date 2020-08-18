import java.util.ArrayList;

public class Vehicle {
	// Class variables
	public int VehId; 					// Vehicle id
	public ArrayList<Node> Route; 		// ArrayList containing the route of the vehicle
	public double capacity; 			// Vehicle capacity
	public double load; 				// Load of the vehicle
	public int CurLoc; 					// Current location of the vehicle
	public boolean Closed;
	public double profitVehicle;
	public double index;

	public Vehicle(int id, double cap) { // Vehicle constructor
		this.VehId = id;
		this.capacity = cap;
		this.load = 0;
		this.Route = new ArrayList<>();
		this.index = 0;

	}

	public Vehicle(Vehicle vehicleCopy) {
		this.VehId = vehicleCopy.VehId;
		this.capacity = vehicleCopy.capacity;
		this.load = vehicleCopy.load;
		this.profitVehicle = vehicleCopy.profitVehicle;
		this.index = vehicleCopy.index;
		this.Route = new ArrayList<>();
		for (int i = 0; i < vehicleCopy.Route.size(); i++) {
			this.Route.add(vehicleCopy.Route.get(i));
		}
	}

	public PositionNodeInRoute bestPositionToEnter(Node testNode, Instance instance) {
		PositionNodeInRoute enterNode = new PositionNodeInRoute();
		double addCost = 0;
		for (int j = 1; j < (Route.size()); j++) { 				// Look for best position
			addCost = instance.distanceMatrix[Route.get(j - 1).NodeId][testNode.NodeId];
			addCost += instance.distanceMatrix[testNode.NodeId][Route.get(j).NodeId];
			addCost -= instance.distanceMatrix[Route.get(j - 1).NodeId][Route.get(j).NodeId];
			if (!CheckIfFits((testNode.serviceTime + addCost))) {
				break;
			}
			if (addCost < enterNode.enteringCost) { // if position j is causen less additional cost
				enterNode.node = testNode;
				enterNode.position = j;
				enterNode.enteringCost = addCost;
			}
		}
		return enterNode;
	}

	public PositionNodeInRoute bestPositionToEnterWithoutCapacityLimit(Node testNode, Instance instance) {
		PositionNodeInRoute enterNode = new PositionNodeInRoute();
		double addCost = 0;
		for (int j = 1; j < (Route.size()); j++) { // Look for best position
			addCost = instance.distanceMatrix[Route.get(j - 1).NodeId][testNode.NodeId];
			addCost += instance.distanceMatrix[testNode.NodeId][Route.get(j).NodeId];
			addCost -= instance.distanceMatrix[Route.get(j - 1).NodeId][Route.get(j).NodeId];
			if (addCost < enterNode.enteringCost) { // if position j is causen less additional cost
				enterNode.node = testNode;
				enterNode.position = j;
				enterNode.enteringCost = addCost;
			}
		}
		return enterNode;
	}
	
	public PositionNodeInRoute bestPositionToEnterWithUpdatedCapacityLimit(Node testNode, double updateCapacityLimit, Instance instance) {
		PositionNodeInRoute enterNode = new PositionNodeInRoute();
		double addCost = 0;
		for (int j = 1; j < (Route.size()); j++) { // Look for best position
			addCost = instance.distanceMatrix[Route.get(j - 1).NodeId][testNode.NodeId];
			addCost += instance.distanceMatrix[testNode.NodeId][Route.get(j).NodeId];
			addCost -= instance.distanceMatrix[Route.get(j - 1).NodeId][Route.get(j).NodeId];
			if (!CheckIfFits((testNode.serviceTime + addCost - (capacity* (1 + updateCapacityLimit) - capacity)))) {
			}
			if (addCost < enterNode.enteringCost) { // if position j is causen less additional cost
				enterNode.node = testNode;
				enterNode.position = j;
				enterNode.enteringCost = addCost;
			}
		}
		return enterNode;
	}

	public PositionNodeInRoute addBestNodeToRoute(Instance instance) {
		double candValue;
		double minValue = Double.MAX_VALUE;
		PositionNodeInRoute enteringNode = new PositionNodeInRoute();
		for (int i = 1; i < instance.NODES.length - 1; i++) { // For all the customer NODES (SE)
			if (!instance.NODES[i].isRouted) { // If the node has not been assigned to a vehicle
				if (CheckIfFits(instance.NODES[i].serviceTime)) { // Check, if the demand of the node i fits the vehicle
																	// capacity
					PositionNodeInRoute potentialPositionNode = new PositionNodeInRoute(bestPositionToEnter(instance.NODES[i], instance));
					candValue = (potentialPositionNode.enteringCost + instance.NODES[i].serviceTime)
							/ instance.NODES[i].profit; // if node i gives new min
					if ((minValue > candValue) && (potentialPositionNode.position != -1)) {
						minValue = candValue;
						enteringNode = new PositionNodeInRoute(potentialPositionNode); // Stores the current customer
																						// index
					}
				}
			}
		}
		return enteringNode;
	}

	public void enterRandomNodeToRoute(Instance instance) {

		AddNode(instance.NODES[0], instance.NODES, instance.distanceMatrix); // Add the depot as the first node;
		ArrayList<Node> NODESNotRouted = new ArrayList<Node>(); // List with all not Routed vehicles

		for (int r = 1; r < instance.NODES.length - 1; r++) {
			if (instance.NODES[r].isRouted == false) {
				NODESNotRouted.add(instance.NODES[r]);
			}
		}

		int NotRoutedLength;
		int randomCustomer;
		PositionNodeInRoute testNode = new PositionNodeInRoute();
		PositionNodeInRoute enteringNode = new PositionNodeInRoute();
		boolean terminateRandomCustomerAssignment = false;
		while (!terminateRandomCustomerAssignment)	{
			NotRoutedLength = NODESNotRouted.size();
			if (NODESNotRouted.isEmpty()) {
				break;
			}
			randomCustomer = (int) (Math.random() * NotRoutedLength);
			testNode.node = NODESNotRouted.get(randomCustomer); // Select Customer Randomly to enter Route
			double addCost = instance.distanceMatrix[0] [testNode.node.NodeId];
			addCost += instance.distanceMatrix[testNode.node.NodeId] [instance.NODES.length-1];
			addCost -= instance.distanceMatrix[0] [instance.NODES.length-1];
			addCost += testNode.node.serviceTime;
			if (CheckIfFits(addCost))	{
				terminateRandomCustomerAssignment = true;
				enteringNode = testNode;
			} else {
				NODESNotRouted.remove(randomCustomer);
			}
		}
		if (enteringNode.node != null)	{
			enteringNode.position = 1;
			enterNodeToRoute(instance, enteringNode);
		}
	}

	public void enterNodeToRoute(Instance instance, PositionNodeInRoute enteringNode) {
		Route.add(enteringNode.position, enteringNode.node);
		instance.setStatusRoutedOfNode(enteringNode.node);
		UpdateLoad(instance.distanceMatrix);
	}

	public void AddNode(Node Customer, Node[] Node, double[][] CostMatrix) // Add Customer to Vehicle Route
	{
		if (Customer.NodeId == 0) {
			this.Route.add(Customer);
			this.Route.add(Node[Node.length - 1]);// Customer is added to the route ArrayList
		} else {
			if (this.Route.isEmpty()) {
				this.Route.add(Customer);
			} else {
				Node swapNode;
				swapNode = this.Route.get(this.Route.size() - 1);
				this.Route.remove(this.Route.size() - 1);
				this.Route.add(Customer);
				this.Route.add(swapNode);
			}
		}
		UpdateLoad(CostMatrix);
		this.CurLoc = Customer.NodeId; // Update the current location of the vehicle
	}

	public void AddNode(Node Customer, Node Customer2, double[][] CostMatrix) // Add Customer to Vehicle Route
	{
		if (Customer.NodeId == 0) {
			this.Route.add(Customer);
			this.Route.add(Customer2);// Customer is added to the route ArrayList
		} else {
			Node swapNode;
			swapNode = this.Route.get(this.Route.size() - 1);
			this.Route.remove(this.Route.size() - 1);
			this.Route.add(Customer);
			this.Route.add(swapNode);
			System.out.println(Route.get(100));
		}
		UpdateLoad(CostMatrix);
		this.CurLoc = Customer.NodeId; // Update the current location of the vehicle
	}

	public boolean CheckIfFits(double extra) // Check if we have a Capacity Violation
	{
		return ((this.load + extra <= this.capacity));
	}

	public void UpdateLoad(double[][] CostMatrix) {
		load = 0;
		profitVehicle = 0;
		for (int i = 1; i < this.Route.size(); i++) {
			load += CostMatrix[this.Route.get(i - 1).NodeId][this.Route.get(i).NodeId]; // Load increase to serve the
																						// customer
			load += this.Route.get(i).serviceTime;
			profitVehicle += this.Route.get(i).profit;
		}
	}

	public void removeNode(Node removeNode, Instance instance) {
		Route.remove(removeNode);
		UpdateLoad(instance.distanceMatrix);
	}

	public Vehicle removeNumberOfNodes(int numberOfNodesToBeRemoved, Instance instance) {
		Vehicle vehicleRemainingSet = new Vehicle(Integer.MAX_VALUE, Double.MAX_VALUE);
		for (int i = 0; i < numberOfNodesToBeRemoved; i++) {
			int randomIndexToBeRemvoed = 1 + (int) (Math.random() * (this.Route.size() - 2));
			Node SwapNode = this.Route.get(randomIndexToBeRemvoed);
			removeNode(SwapNode, instance);
			vehicleRemainingSet.AddNode(SwapNode, instance.NODES, instance.distanceMatrix);
		}
		return vehicleRemainingSet;
	}
	
	public Vehicle removeNumberOfNodesNotTabu(int numberOfNodesToBeRemoved, ArrayList<Node> removeableNodes, Instance instance) {
		Vehicle vehicleRemainingSet = new Vehicle(Integer.MAX_VALUE, Double.MAX_VALUE);
		for (int i = 0; i < numberOfNodesToBeRemoved; i++) {
			int randomIndex = (int) (Math.random() * removeableNodes.size());
			Node SwapNode = removeableNodes.get(randomIndex);
			removeNode(SwapNode, instance);
			vehicleRemainingSet.AddNode(SwapNode, instance.NODES, instance.distanceMatrix);
			removeableNodes.remove(randomIndex);
		}
		return vehicleRemainingSet;
	}
	
	public Vehicle executeFarthestInsertion (Vehicle removedNodes, Instance instance) {
		
		//find unused Customer which has the highest distance to any other customer in tour
		Vehicle storageOfRemovedNodes = new Vehicle(removedNodes);
		Node nodeWithHighestDistance = null;
		double highestDistance = -1*Double.MAX_VALUE;
		double distanceNodeToCandidate =0;
		for ( int j = 1; j < storageOfRemovedNodes.Route.size()-1;j++)	{
			for ( int z = 0; z < Route.size(); z++)	{
				distanceNodeToCandidate = instance.distanceMatrix[Route.get(z).NodeId] [storageOfRemovedNodes.Route.get(j).NodeId];
				if ( distanceNodeToCandidate > highestDistance) {
					highestDistance = distanceNodeToCandidate;
					nodeWithHighestDistance = storageOfRemovedNodes.Route.get(j);
				}
			}
		}
		//Insert Node with highest distance at cheapest place in Route
		
		PositionNodeInRoute enterNode = new PositionNodeInRoute();
		enterNode = bestPositionToEnterWithoutCapacityLimit(nodeWithHighestDistance,instance);
		enterNodeToRoute(instance, enterNode);			
		storageOfRemovedNodes.Route.remove(enterNode.node);
		return storageOfRemovedNodes;		
	}
}