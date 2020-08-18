import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class TabuSearch {

	// Tabu Search Parameters
	int maxNonImprovIterations;	 	// alpha
	int numbRemoveAndReinsert; 		// kappa
	double tourDurationUpdate; 		// epsilon
	int improvementFrequency; 		// chi
	int noNeighbourhoodSolutions; 	// beta
	int DELTA; 						// delta
	double penaltyCoefficient = 1; 		// eta
	int TABU_TENURE_MIN; 			// theta minimum
	int TABU_TENURE_RANGE; 			// theta range
	int[] [] Tabu_Matrix;
	
	int neigborhoodSizeIndicator; 	// indicator for size of Neighbourhood

	ArrayList<Node> NODESInSolution;

	Instance instance;
	PastSolutionsClass currentSolution;
	PastSolutionsClass currentBestSolution;
	PastSolutionsClass currentBestFeasibleSolution;
	PastSolutionsClass lastNonTabuSolution;
	PastSolutionsClass lastSelectedSolution;
	Vehicle[] vehicleTabu = new Vehicle[3];

	// Step B Parameters
	int selectedTour1 = -1;
	int selectedTour2 = -1;
	int qtabu = 0;
	public ArrayList<PastSolutionsClass> pastSolutionsTabuStepB = new ArrayList<>();

	// Step C Parameters
	PastSolutionsClass solutionTabu;
	PastSolutionsClass solutionNonTabu;
	public ArrayList<PastSolutionsClass> bestSolution = new ArrayList<>();
	public ArrayList<PastSolutionsClass> pastSolutionStepC = new ArrayList<>();

	public TabuSearch(Instance instance) {
		Tabu_Matrix = new int [instance.NUMBER_OF_VEHICLES-1] [instance.NODES.length];
		neigborhoodSizeIndicator = 0;
		this.instance = instance;
		NODESInSolution = new ArrayList<Node>();
		currentSolution = new PastSolutionsClass();
		currentBestSolution = new PastSolutionsClass();
		currentBestFeasibleSolution = new PastSolutionsClass();
		lastNonTabuSolution = new PastSolutionsClass();
		lastSelectedSolution = new PastSolutionsClass();
	}

	public void setTabuParameters() {
		if (neigborhoodSizeIndicator == 0) {
			adjustTabuParamtersToNeighbourhoodSize(instance.NODES.length, 2 * (instance.NODES.length), 4, 6, 0.02, 5, 5,		//0.02 or 0.0 for set 5 and 6
					10);
		} else {
			adjustTabuParamtersToNeighbourhoodSize(instance.NODES.length, 8 * (instance.NODES.length), 4, 6, 0.05, 7, 6,		//0.05 or 0.0 for set 5 and 6
					12);
		}
	}

	public void initializeTabuSearch(Vehicle[] initialSolution) {
		currentSolution = new PastSolutionsClass(initialSolution, 1);

		neigborhoodSizeIndicator = 0; // change to boolean
		qtabu = 0;
		setTabuParameters();

		for (int i = 0; i < Tabu_Matrix.length; i++) {
			for ( int j = 0; j < Tabu_Matrix[i].length; j++)	{
				Tabu_Matrix[i] [j] = 0;
			}

		}

		NODESInSolution.clear();
		updateNODESinSolution(initialSolution);
		lastSelectedSolution = currentSolution.copySolution();
	}

	public void determineBestNeighbourSolution(ArrayList<PastSolutionsClass> allNeighbourhoodSolutions) {
		solutionTabu = new PastSolutionsClass();
		solutionNonTabu = new PastSolutionsClass();
		double bestSolutionSnt = -1 * Double.MAX_VALUE;
		double bestSolutionSt = -1 * Double.MAX_VALUE;

		// C.1
		for (int i = 0; i < allNeighbourhoodSolutions.size(); i++) {
			if (allNeighbourhoodSolutions.get(i).tabu) {
				if (allNeighbourhoodSolutions.get(i).quasiReward > bestSolutionSt) {
					solutionTabu = allNeighbourhoodSolutions.get(i);
					bestSolutionSt = solutionTabu.quasiReward;
					// System.out.println("ST found " + bestSolutionSt);
				}
			} else {
				if (allNeighbourhoodSolutions.get(i).quasiReward > bestSolutionSnt) {
					solutionNonTabu = allNeighbourhoodSolutions.get(i);
					bestSolutionSnt = solutionNonTabu.quasiReward;
					// System.out.println("SnT found" + bestSolutionSnt);
				}
			}
		}
	}

	public void updateCurrentAndBestSolution() {
		PastSolutionsClass Sol = new PastSolutionsClass();

		if ((solutionTabu.tourProfit > solutionNonTabu.tourProfit)
				&& (solutionTabu.tourProfit > currentBestSolution.tourProfit)) {
			currentBestSolution = solutionTabu.copySolution();
			bestSolution.add(solutionTabu.copySolution());
			Sol = solutionTabu;
			qtabu = 0;

		} else {
			Sol = solutionNonTabu;
			if (Sol.vehiclesTour == null) {

				Sol=lastNonTabuSolution;
			} else {
				lastNonTabuSolution = solutionNonTabu.copySolution();
			}
		}
		if (solutionNonTabu.tourProfit > currentBestSolution.tourProfit) {
			currentBestSolution = solutionNonTabu.copySolution();
			bestSolution.add(solutionNonTabu.copySolution());
			qtabu = 0;
		}

		currentSolution = Sol.copySolution();
		pastSolutionStepC.add(Sol.copySolution());
	}

	public void adjustTabuParamtersToNeighbourhoodSize(int maxNonImprovIterations, int NeighbourhoodSolutions,
			int improvementFrequency, int DELTA, double tourDurationUpdate, int numbRemoveAndReinsert,
			int TABU_TENURE_MIN, int TABU_TENURE_MAX) {
		this.maxNonImprovIterations = maxNonImprovIterations; // alpha
		this.noNeighbourhoodSolutions = NeighbourhoodSolutions; // beta //T
		this.improvementFrequency = improvementFrequency;
		this.DELTA = DELTA;
		this.tourDurationUpdate = tourDurationUpdate;
		this.numbRemoveAndReinsert = numbRemoveAndReinsert;
		this.TABU_TENURE_MIN = TABU_TENURE_MIN;
		this.TABU_TENURE_RANGE = TABU_TENURE_MAX - TABU_TENURE_MIN;
	}

	public void selectTwoRandomTours(Vehicle[] initialSolution) {
		boolean stop = false;
		while (!stop) { // Selecting two random tours Step 1. Page 1391
			selectedTour1 = (int) (Math.random() * (initialSolution.length - 1));
			selectedTour2 = (int) (Math.random() * (initialSolution.length - 1));
			if (selectedTour1 != selectedTour2) {
				stop = true;
			}
		}
		vehicleTabu[0] = new Vehicle(initialSolution[selectedTour1]);
		vehicleTabu[1] = new Vehicle(initialSolution[selectedTour2]);
		vehicleTabu[2] = new Vehicle(initialSolution[(initialSolution.length - 1)]);
	}

	public void updateVehicles(Vehicle[] initialSolution) {
		initialSolution[selectedTour1] = new Vehicle(vehicleTabu[0]);
		initialSolution[selectedTour2] = new Vehicle(vehicleTabu[1]);
		initialSolution[initialSolution.length - 1] = new Vehicle(vehicleTabu[2]);
	}

	public boolean addExtraVertices() {

		boolean verticeEntered = false;

		boolean terminateAddVertices = false;
		while (!terminateAddVertices) {
			int SwapRouteTo = -1;
			if (vehicleTabu[2].Route.size() <= 2) { 					// if all Nodes are in Solution stop
				terminateAddVertices = true;
			}
			else {
				PositionNodeInRoute testEnterNode = new PositionNodeInRoute();
				PositionNodeInRoute enterNode = new PositionNodeInRoute();
	
				for (int i = 1; i < vehicleTabu[2].Route.size() - 1; i++) {
					for (int VehIndexTo = 0; VehIndexTo <= 1; VehIndexTo++) {
						testEnterNode = vehicleTabu[VehIndexTo].bestPositionToEnter(vehicleTabu[2].Route.get(i), instance);
						if (testEnterNode.enteringCost < enterNode.enteringCost) {
							enterNode = new PositionNodeInRoute(testEnterNode);
							SwapRouteTo = VehIndexTo;
						}
					}
				}
	
				if (enterNode.node == null) { // if no additional customer was found, that fulfills the requirements
					terminateAddVertices = true;
				}
				else {		
					vehicleTabu[2].removeNode(enterNode.node, instance);
					vehicleTabu[SwapRouteTo].enterNodeToRoute(instance, enterNode);
					verticeEntered = true;
					

				}	

			}
		}

		return verticeEntered;
	}

	public void exchangeUsedAndUnusedVertices() {
		// System.out.println("exchanged Nodes with unused Nodes");
		boolean[] loadLimitReached = new boolean[vehicleTabu.length - 1];
		
		for (int i = 0; i < vehicleTabu.length-2; i++) {
			//check how many nodes can be removed
			ArrayList <Node> numberOfNodesRemoveable = new ArrayList<>();
			int correspondingColumn;
			if (i==0) {
				correspondingColumn = selectedTour1;
			} else {
				correspondingColumn = selectedTour2;
			}
			for ( int j = 1; j < vehicleTabu[i].Route.size()-1; j++) {
				for (int z = 1; z < Tabu_Matrix[correspondingColumn].length -1; z++)	{
					if (Tabu_Matrix[correspondingColumn] [z] == 0) {
						if (vehicleTabu[i].Route.get(j).NodeId == z) {
							numberOfNodesRemoveable.add(vehicleTabu[i].Route.get(j));
						}
					}

				}
			}
			//Now we have the amount of nodes which can be removed
			int randomNumberOfNodesToBeRemoved = (int) (Math.random() * (numberOfNodesRemoveable.size()+1)); //+1 such that it can be the max
			Vehicle removedNodes = new Vehicle(instance.NUMBER_OF_VEHICLES + 1, Double.MAX_VALUE);
			removedNodes = vehicleTabu[i].removeNumberOfNodesNotTabu(randomNumberOfNodesToBeRemoved, numberOfNodesRemoveable,instance);
			//randomReinsertion
			boolean terminateInsertion = false;
			while (!terminateInsertion) {

				PositionNodeInRoute enterNode = new PositionNodeInRoute();

				if (vehicleTabu[i].load >= ((vehicleTabu[i].capacity) * (1 + tourDurationUpdate))) {
						loadLimitReached[i] = true;
					} else {
						if (vehicleTabu[2].Route.size() <= 2) {
							terminateInsertion = true;
							//System.out.println("No nodes available");					
						} else {
							int randomStep3Three = 1 + (int) (Math.random() * (vehicleTabu[2].Route.size() - 2));
							enterNode = vehicleTabu[i].bestPositionToEnterWithoutCapacityLimit(vehicleTabu[2].Route.get(randomStep3Three), instance);
							vehicleTabu[2].removeNode(enterNode.node, instance);
							vehicleTabu[i].enterNodeToRoute(instance, enterNode);
						}
					}

				if (loadLimitReached[i]) {
					terminateInsertion = true;
				}



			}
			for (int j = 0; j < removedNodes.Route.size(); j++) { 			// reinsert Removed Nodes To Set of unused Customers
				vehicleTabu[2].Route.add(1, removedNodes.Route.get(j));
			}
			

		}

	}

	public boolean balanceSelectedTours() {
		boolean balancingExecuted;

		if (vehicleTabu[0].load > vehicleTabu[1].load) {
			balancingExecuted = exchangeFromLongerToShorterRoute(vehicleTabu[0], vehicleTabu[1]);					//select longer tour as the route which duration should be reduced (route from)
		} else {
			balancingExecuted = exchangeFromLongerToShorterRoute(vehicleTabu[1], vehicleTabu[0]);
		}

		vehicleTabu[0].UpdateLoad(instance.distanceMatrix);
		vehicleTabu[1].UpdateLoad(instance.distanceMatrix);
		return balancingExecuted;
	}

	public boolean exchangeFromLongerToShorterRoute(Vehicle routeFrom, Vehicle routeTo) {
		boolean balancingExecuted = false;
		double addedValueStep4;
		int swapIndexRouteFrom = -1;
		int swapIndexRouteTo = -1;
		double minValueStep4 = 0;

		for (int i = 1; i < routeFrom.Route.size() - 1; i++) {
			for (int j = 1; j < routeTo.Route.size(); j++) {
				// Subtract the following cost
				// Cost to get from the predecessor of node i to node i
				double MinusCost1 = instance.distanceMatrix[routeFrom.Route.get(i - 1).NodeId][routeFrom.Route.get(i).NodeId]; 
				double MinusCost2 = instance.distanceMatrix[routeFrom.Route.get(i).NodeId][routeFrom.Route.get(i + 1).NodeId]; // Cost to get from node i to its successor
				double MinusCost3 = instance.distanceMatrix[routeTo.Route.get(j-1).NodeId][routeTo.Route.get(j).NodeId]; // Cost to get from node j to its successor

				// Add the following cost
				double AddedCost1 = instance.distanceMatrix[routeFrom.Route.get(i - 1).NodeId][routeFrom.Route.get(i + 1).NodeId]; // Cost to get from the predecessor of node i to the successor of node i
				double AddedCost2 = instance.distanceMatrix[routeTo.Route.get(j-1).NodeId][routeFrom.Route.get(i).NodeId]; // Cost to get from node j to node i
				double AddedCost3 = instance.distanceMatrix[routeFrom.Route.get(i).NodeId][routeTo.Route.get(j).NodeId]; // Cost to get from node i to the successor of node j

				addedValueStep4 = AddedCost1 + AddedCost2 + AddedCost3 - MinusCost1 - MinusCost2 - MinusCost3;

				if (addedValueStep4 < minValueStep4) { // total Duration has to be decreased
					swapIndexRouteFrom = i;
					swapIndexRouteTo = j;
					minValueStep4 = addedValueStep4;
				}
			}
		} // We now know the best position;

		if (swapIndexRouteFrom > -1) {
			Node SwapNode = routeFrom.Route.get(swapIndexRouteFrom);
			routeFrom.Route.remove(swapIndexRouteFrom);
			routeTo.Route.add(swapIndexRouteTo, SwapNode);
			balancingExecuted = true;
			// System.out.println("Step 4: balanced Tours");
		}
		return balancingExecuted;
	}

	public void improveToursByExchange() {

		// System.out.println("Improve by exchange");
		boolean terminateCustomerExchange = false;
		int customerVertex = 1;
		if (vehicleTabu[0].Route.size() == 2) {
			terminateCustomerExchange = true;
		}

		int sizeOfVertex1 = vehicleTabu[0].Route.size() - 2;

		while (!terminateCustomerExchange) {
			for (int j = 1; j < vehicleTabu[1].Route.size() - 1; j++) {

				double effectOnDuration;
				//Cost to get from predecessor of node 
				double MinusCost1 = instance.distanceMatrix[vehicleTabu[0].Route.get(customerVertex - 1).NodeId][vehicleTabu[0].Route.get(customerVertex).NodeId]; // Cost  get from
				double MinusCost2 = instance.distanceMatrix[vehicleTabu[0].Route.get(customerVertex).NodeId][vehicleTabu[0].Route.get(customerVertex + 1).NodeId]; // Cost to
				double MinusCost3 = instance.distanceMatrix[vehicleTabu[1].Route.get(j - 1).NodeId][vehicleTabu[1].Route.get(j).NodeId]; // Cost to get from node j to its successor
				double MinusCost4 = instance.distanceMatrix[vehicleTabu[1].Route.get(j).NodeId][vehicleTabu[1].Route.get(j + 1).NodeId];

				// Add the following cost
				double AddedCost1 = instance.distanceMatrix[vehicleTabu[0].Route.get(customerVertex - 1).NodeId][vehicleTabu[1].Route.get(j).NodeId]; // Cost to get from the
																								// predecessor of node i
																								// to the successor of
																								// node i
				double AddedCost2 = instance.distanceMatrix[vehicleTabu[1].Route.get(j).NodeId][vehicleTabu[0].Route.get(customerVertex + 1).NodeId]; // Cost to get from node j to node i
				double AddedCost3 = instance.distanceMatrix[vehicleTabu[1].Route.get(j - 1).NodeId][vehicleTabu[0].Route.get(customerVertex).NodeId]; // Cost to get from node i to the successor of node j
				double AddedCost4 = instance.distanceMatrix[vehicleTabu[0].Route.get(customerVertex).NodeId][vehicleTabu[1].Route.get(j + 1).NodeId];

				effectOnDuration = AddedCost1 + AddedCost2 + AddedCost3 + AddedCost4 - MinusCost1 - MinusCost2
						- MinusCost3 - MinusCost4;

				if (effectOnDuration < 0) {
					Node swapNodeFrom0To1 = vehicleTabu[0].Route.get(customerVertex);
					Node swapNodeFrom1To0 = vehicleTabu[1].Route.get(j);
					vehicleTabu[0].Route.remove(customerVertex);
					vehicleTabu[1].Route.remove(j);
					vehicleTabu[0].Route.add(customerVertex, swapNodeFrom1To0); // inserting Node from Tour 2 in Tour 1
					vehicleTabu[1].Route.add(j, swapNodeFrom0To1); // isnerting Node form Tour 1 in Tour 2
					break;
				}
			}

			if (customerVertex == sizeOfVertex1) {
				terminateCustomerExchange = true;
			}
			customerVertex++;
		}
		vehicleTabu[0].UpdateLoad(instance.distanceMatrix);
		vehicleTabu[1].UpdateLoad(instance.distanceMatrix);
	}

	public Vehicle[] improveAllToursByRVI(Vehicle[] currentSolution) { // muss ich als Return ausführen!!!!!
		// System.out.println("improve by rvi method");
		//Vehicle[] vehiclesAfterRVI = Arrays.copyOf(currentSolution, currentSolution.length);	// Copy for later comparison
		Vehicle[] vehiclesAfterRVI = new Vehicle[currentSolution.length];
		for (int i = 0 ; i < currentSolution.length; i++) {
			vehiclesAfterRVI[i] = new Vehicle (currentSolution[i]);
		}
		
		for (int i = 0; i < vehiclesAfterRVI.length - 1; i++) {

			
			Vehicle testVehicle = new Vehicle(currentSolution[i]); // stores all Nodes in original Tour
			currentSolution[i].Route.clear();
			currentSolution[i].AddNode(instance.NODES[0], instance.NODES, instance.distanceMatrix); // add Start and end

			boolean terminateResequence = false;
			while (!terminateResequence) { // largest insertion procedure
				if (testVehicle.Route.size() == 2) {
					terminateResequence = true;
					break;
				}
				testVehicle = currentSolution[i].executeFarthestInsertion(testVehicle, instance);


			}

			if (vehiclesAfterRVI[i].load > currentSolution[i].load) { // if new Tour Route is better, let return Tour be the new tour
				vehiclesAfterRVI[i] = new Vehicle(currentSolution[i]);
			}
			
			//Continue Improvment

			int qRVI = 0;
			int improvementCounter = 0;
			
			boolean terminateRVIStep2 = false;
			while (!terminateRVIStep2) {
				if (qRVI >= numbRemoveAndReinsert) {
					terminateRVIStep2 = true;
					break;
				}
				if (currentSolution[i].Route.size() <= 2) {
					terminateRVIStep2=true;
					break;
				}
				qRVI++;

				// Remove and reinsert
				int randomNumberStep6 = 1 + (int) (Math.random() * (currentSolution[i].Route.size() - 3));
	
				Vehicle storageForRemovedNodes = new Vehicle(Integer.MAX_VALUE, Double.MAX_VALUE);
				storageForRemovedNodes = currentSolution[i].removeNumberOfNodes(randomNumberStep6, instance);
				
				//adjust storageForRemovedNodes such that it works with executeFarthestInsertion
				storageForRemovedNodes.Route.add(0,instance.NODES[0]);
				storageForRemovedNodes.Route.add(instance.NODES[instance.NODES.length-1]);

				boolean terminateReinsertion = false;

				while (!terminateReinsertion) {
					if (storageForRemovedNodes.Route.size() <= 2) {
						terminateReinsertion = true;
						break;
					}
					
					storageForRemovedNodes = currentSolution[i].executeFarthestInsertion(storageForRemovedNodes, instance);
					

				}

				if (currentSolution[i].load < vehiclesAfterRVI[i].load) {
					vehiclesAfterRVI[i] = new Vehicle(currentSolution[i]);
					improvementCounter = 0;
				} else {
					improvementCounter++;
				}
				if (improvementCounter >= (numbRemoveAndReinsert / 2)) {
					currentSolution[i] = new Vehicle(vehiclesAfterRVI[i]);
				}
				// End of Remove Process
			}

			boolean terminationAdditionalVertices = false;
			while (!terminationAdditionalVertices) {

				if (vehiclesAfterRVI[vehiclesAfterRVI.length - 1].Route.size() <= 2) {
					terminationAdditionalVertices = true;
					break;
				}

				PositionNodeInRoute testEnterNode = new PositionNodeInRoute();
				PositionNodeInRoute enterNode = new PositionNodeInRoute();

				for (int j = 1; j < vehiclesAfterRVI[vehiclesAfterRVI.length - 1].Route.size() - 1; j++) {
					testEnterNode = vehiclesAfterRVI[i].bestPositionToEnter(vehiclesAfterRVI[vehiclesAfterRVI.length - 1].Route.get(j), instance);
					if (testEnterNode.enteringCost < enterNode.enteringCost) {
						enterNode = new PositionNodeInRoute(testEnterNode);
					}
				}

				if (enterNode.node == null) {
					terminationAdditionalVertices = true;
					break;
				}

				vehiclesAfterRVI[vehiclesAfterRVI.length - 1].removeNode(enterNode.node, instance);
				vehiclesAfterRVI[i].enterNodeToRoute(instance, enterNode);
			}
		}

		return vehiclesAfterRVI;
	}

	public void updateNODESinSolution(Vehicle[] currentSolution) {
		NODESInSolution.clear();
		for (int i = 0; i < currentSolution.length - 1; i++) { // without last tour
			for (int j = 1; j < currentSolution[i].Route.size() - 1; j++) { // without start and depot
				NODESInSolution.add(currentSolution[i].Route.get(j));
			}
		}
	}

	public void updateTabuList() {
		boolean nodeIsNew = true;
		for (int i = 0; i < currentSolution.vehiclesTour.length-1; i++)	{		//check for all m tours
			for (int j = 1; j < currentSolution.vehiclesTour[i].Route.size()-1; j++)	{
				nodeIsNew = true;
				for (int z = 1; z < lastSelectedSolution.vehiclesTour[i].Route.size()-1; z++)	{
					if (currentSolution.vehiclesTour[i].Route.get(j).NodeId == lastSelectedSolution.vehiclesTour[i].Route.get(z).NodeId)	{
						nodeIsNew=false;
					}
				}
				if (nodeIsNew) {				//if node was not found in old solution
					Random TabuRan = new Random();
					int RandomDelay = TabuRan.nextInt(TABU_TENURE_RANGE) + TABU_TENURE_MIN;
					Tabu_Matrix[i] [currentSolution.vehiclesTour[i].Route.get(j).NodeId] = RandomDelay;
				}
			}
		}
		
		for (int i = 0; i < Tabu_Matrix.length; i++)	{
			for (int j = 0; j < Tabu_Matrix[i].length; j++)	{
				if (Tabu_Matrix[i] [j]>0)	{
					Tabu_Matrix[i][j]--;
				}
			}
		}
		
		lastSelectedSolution = currentSolution.copySolution();

	}

	public void overrideTabuStatus() {
		boolean resetTabuStatus;
		for ( int  i = 0; i < currentSolution.vehiclesTour.length-1; i++) {
				for ( int z = 1; z < Tabu_Matrix[i].length -1; z++)	 {
					resetTabuStatus = false;
					if (Tabu_Matrix[i][z] > 0)	{
						resetTabuStatus = true;									//Reset Tabu status except node is new solution
						for (int j = 1; j < currentSolution.vehiclesTour[i].Route.size()-1; j++) {
							if (currentSolution.vehiclesTour[i].Route.get(j).NodeId == z)	{
								resetTabuStatus = false;
							}
						}
					}
					if (resetTabuStatus) {
						Tabu_Matrix[i] [z] = 0; 
					}
			}
		}

	}

	public void updatePenaltyCoefficient() {
		boolean feasibleSolutionFound = false;
		for (int i = (pastSolutionStepC.size() - DELTA); 0 <= i && i <= pastSolutionStepC.size() - 1; i++) {
			if (!pastSolutionStepC.get(i).infeasible) {
				feasibleSolutionFound = true;
				break;
			}
		}

		if (feasibleSolutionFound) {
			penaltyCoefficient = penaltyCoefficient / 2;

		} else {
			penaltyCoefficient = penaltyCoefficient * 2;

		}
	}

	public boolean updateNeighborhoodSizeIndicator() {
		boolean terminationStepC = false;
		if (neigborhoodSizeIndicator == 0) {
			if (qtabu > (maxNonImprovIterations / 2)) {
				// goBackToStepB
				neigborhoodSizeIndicator = 1;
				setTabuParameters();

			} else {
				// goBackToStepB
			}
		} else {
			if (qtabu == 0) {
				// GoToStepB
				neigborhoodSizeIndicator = 0;
				setTabuParameters();
			} else {
				if (qtabu <= maxNonImprovIterations) {

					// goBackToStepB
				} else {
					terminationStepC = true;
					// System.out.println("goBackTo AMP");
				}
			}
		}
		return terminationStepC;
	}

	public void executeNeighbourhoodSearch(Vehicle[] initialSolution) {


		
		qtabu++;
		int neigbourhoodCounter = 0;
		boolean terminateNeigbourhoodSearch = false;
		while (!terminateNeigbourhoodSearch) {
			
			Vehicle [] vehicleNeighbourhoodSearch = new Vehicle[initialSolution.length];
			for (int i = 0; i < initialSolution.length; i++)	{
				vehicleNeighbourhoodSearch[i] = new Vehicle(initialSolution[i]);
			}
			
			selectTwoRandomTours(vehicleNeighbourhoodSearch); 				// Step 1
			
			
			if (!addExtraVertices()) { 							// Step 2
				exchangeUsedAndUnusedVertices(); 				// Step 3, executed if Step 2 fails
			}

			if (!balanceSelectedTours()) { 						// Step 4
				improveToursByExchange(); 						// Step 5, executed if Step 4 fails
			}

			updateVehicles(vehicleNeighbourhoodSearch); // Adding Vehicles Back To Solution

			if ((qtabu % improvementFrequency == 0)|| qtabu == 0) {
				vehicleNeighbourhoodSearch = improveAllToursByRVI(vehicleNeighbourhoodSearch);
			}

			neigbourhoodCounter++; // Step 7

			safeSolutionOfStepB(vehicleNeighbourhoodSearch);

			if (neigbourhoodCounter >= noNeighbourhoodSolutions) {
				terminateNeigbourhoodSearch = true;
			}

		} // EndTabuSearchStepB
	}

	public void safeSolutionOfStepB(Vehicle[] neighborhoodSolution) {
		// SaveNeighbourSolution and if feasible or not
		boolean tabuStatusOfSolution = false;
		boolean nodeIsIndeedInSolution = false;
		for (int i = 0; i < neighborhoodSolution.length - 1; i++) {
			for (int z = 1; z < Tabu_Matrix [i].length-1; z++) {
				nodeIsIndeedInSolution = true;
				if (Tabu_Matrix[i][z] > 0) { // Check alle Kudnen die in der Lösung sein müssen
					nodeIsIndeedInSolution = false;
					for (int j = 1; j < neighborhoodSolution[i].Route.size() - 1; j++) {
						if (z == neighborhoodSolution[i].Route.get(j).NodeId) {
							nodeIsIndeedInSolution = true;
							//break;
						}
					}
				}
				if (!nodeIsIndeedInSolution) {
					tabuStatusOfSolution = true;
					break;
				}
			}
		}

		PastSolutionsClass neighborSolution = new PastSolutionsClass(neighborhoodSolution, penaltyCoefficient);
		neighborSolution.tabu = tabuStatusOfSolution;
		pastSolutionsTabuStepB.add(neighborSolution.copySolution());

	}

	public void evaluateGeneratedNeighbourhoods() {
		determineBestNeighbourSolution(pastSolutionsTabuStepB); // Step 1 - Quasi Rewards are saved with tour solution

		updateCurrentAndBestSolution(); // C.2
		
		updateCurrentBestFeasibleSolution();
		
		updateTabuList();
		
	}
	
	public void updateCurrentBestFeasibleSolution()	{
		if(!currentSolution.infeasible) {
			if(currentSolution.tourProfit > currentBestFeasibleSolution.tourProfit)	{
				currentBestFeasibleSolution = currentSolution.copySolution();
				qtabu = 0;
			}
		}
	}

}
