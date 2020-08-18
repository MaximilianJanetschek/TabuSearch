import java.util.ArrayList;
import java.util.Arrays;

public class Solution {
	// Class variables:
	Vehicle[] vehicles; 			// An array of the class Vehicle
	Instance instance;

	// AMP variables
	AdaptiveMemoryProcess amp;
	int MAX_FOR_AMP = 3;

	// Tabu Variables
	double bestSolutionValueFeasbile = -1 * Double.MAX_VALUE;
	double bestSolutionValueFeasibleTabu = -1 * Double.MAX_VALUE;


	public ArrayList<PastSolutionsClass> bestSolutionFeasible;
	public ArrayList<PastSolutionsClass> pastSolutionsTabuFeasible;
	public ArrayList<PastSolutionsClass> pastSolutionsTabu;

	Solution(Instance instance) {
		this.instance = instance;

		// Instantiation:
		vehicles = new Vehicle[instance.NUMBER_OF_VEHICLES]; // Instantiate the vehicle array
		bestSolutionFeasible = new ArrayList<>();
		pastSolutionsTabuFeasible = new ArrayList<>();
		pastSolutionsTabu = new ArrayList<>();
		amp = new AdaptiveMemoryProcess(instance);

		// Construct the new vehicles
		for (int i = 0; i < instance.NUMBER_OF_VEHICLES; i++) {
			if (i == (instance.NUMBER_OF_VEHICLES - 1)) {
				vehicles[i] = new Vehicle(i + 1, Integer.MAX_VALUE);
			} else {
				vehicles[i] = new Vehicle(i + 1, instance.VEHICLE_CAPACITY); // Construction requires the id and the capacity
			}
		}
	}

	public void resetEverything() {
		bestSolutionValueFeasbile = -1 * Double.MAX_VALUE;
		bestSolutionFeasible.clear();
	}

	public void addSolution(Vehicle[] vehicle, double penaltyCoefficient) {
		PastSolutionsClass safeSolution = new PastSolutionsClass(vehicle, penaltyCoefficient);
		pastSolutionsTabu.add(safeSolution.copySolution());
		addBestFeasibleSolution(safeSolution);

	}

	public void addBestFeasibleSolution(PastSolutionsClass feasibleSolution) {

		if (feasibleSolution.tourProfit > bestSolutionValueFeasbile) {
			bestSolutionValueFeasbile = feasibleSolution.tourProfit;
			bestSolutionFeasible.add(feasibleSolution.copySolution());
			Documentation.addNewBestFeasibleSolution(feasibleSolution.copySolution());
		}
		pastSolutionsTabuFeasible.add(feasibleSolution.copySolution());

	}

	// ---------------------------------------Methods of the Solution
	// Class---------------------------------------//

	public void greedyHeuristic() {
		vehicles = amp.greedyHeuristic();
		addBestFeasibleSolution(new PastSolutionsClass(vehicles, 0));
	}

	public void adaptiveMemoryProcessTabuSearch() {
		// AMP step 1
		amp.generateSetN();
		int qAMP = 1;
		// AMP step 2 and 3
		boolean terminateAMP = false;
		while (!terminateAMP) {


			// Step 2
			Vehicle[] vehicleAMP = amp.constructAMPSolution();

			// Adding Solution to feasible Solutions
			addSolution(vehicleAMP, 0);
			tabuSearch(vehicleAMP);
			//System.out.println("Counter AMP: " + qAMP);
			amp.updateSetN(pastSolutionsTabuFeasible);

			if (qAMP >= MAX_FOR_AMP) {
				terminateAMP = true;
			}
			qAMP++;
		}

	}

	public void updateBestFeasibleSolution(TabuSearch tabuSearch, int iterationCounter) {
		if (tabuSearch.currentSolution.tourProfit > this.bestSolutionValueFeasbile) {
			PastSolutionsClass solutionToBeAdded = tabuSearch.currentSolution.copySolution();
			bestSolutionFeasible.add(solutionToBeAdded);
			this.bestSolutionValueFeasbile = tabuSearch.currentSolution.tourProfit;
			//System.out.println("Found improved Solution" + tabuSearch.currentSolution.tourProfit + "Number of Step " + iterationCounter);
			tabuSearch.qtabu = 0;
			Documentation.addNewBestFeasibleSolution(solutionToBeAdded);
		}
		if (tabuSearch.currentSolution.tourProfit > this.bestSolutionValueFeasibleTabu) {
			this.bestSolutionValueFeasibleTabu = tabuSearch.currentSolution.tourProfit;
			pastSolutionsTabuFeasible.add(tabuSearch.currentSolution.copySolution());
		}
	}

	// -------------------------------AMP-Step1------------------------------------------//

	// -------------------------------Tabu
	// Search------------------------------------------//
	public void tabuSearch(Vehicle[] vehicleAMP) { // Where CostMatrix :=: distanceMatrix

		pastSolutionsTabu.clear();
		pastSolutionsTabuFeasible.clear();
		TabuSearch tabuSearch = new TabuSearch(instance);

		// Tabu Search Parameters
		int counterStep3C = 0;
		int NUMBER_EXECUTION_STEP_C = 1000;

		//printTourWithAdditionalInformation(vehicleAMP);

		boolean terminateTabuSearch = false;
		while (!terminateTabuSearch) {

			// Step A initialize Tabu Search (neighbourhood size and currentSolution is AMP) and reset Tabu Matrix
			tabuSearch.initializeTabuSearch(vehicleAMP);

			boolean terminateTabuSearchStepC = false;
			while (!terminateTabuSearchStepC) {

				// get the Tours of current AMP solution Solution
				for(int i = 0; i< tabuSearch.currentSolution.vehiclesTour.length;i++) {
					vehicles [i] = new Vehicle (tabuSearch.currentSolution.vehiclesTour[i]);
				}
				
				// clear collected old Neighborhood Solutions
				tabuSearch.pastSolutionsTabuStepB.clear();
				


				tabuSearch.executeNeighbourhoodSearch(vehicles);

				// Step C of the tabu search procedure
				tabuSearch.evaluateGeneratedNeighbourhoods();


				
				//Adjust Tabu Matrix if tabu solution is selected
				if (tabuSearch.currentSolution.tabu) {
					tabuSearch.currentSolution.tabu=false;
					tabuSearch.overrideTabuStatus();
				}

				
				//Add Solution for documentation
				pastSolutionsTabu.add(tabuSearch.currentSolution.copySolution());
				
				//Add Solution to best feasbile Solutions if applicable
				if (!tabuSearch.currentSolution.infeasible) {
					updateBestFeasibleSolution(tabuSearch, counterStep3C);
				}

				tabuSearch.updatePenaltyCoefficient();
				

				terminateTabuSearchStepC = tabuSearch.updateNeighborhoodSizeIndicator();

				counterStep3C++;

				if (counterStep3C >= NUMBER_EXECUTION_STEP_C) {
					terminateTabuSearch = true;
					terminateTabuSearchStepC = true;
				}
			}
		} // EndTabuSearch
	}

	public void SolutionPrint(String Solution_Label) { // Print Solution In console
		System.out.println("=========================================================");
		System.out.println(Solution_Label + "\n");
		double highestProfit = -1 * Double.MIN_VALUE;
		Vehicle[] vehiclesPrint = new Vehicle[instance.NUMBER_OF_VEHICLES]; // Updaten!!

		System.out.println("Number of Solutions " + bestSolutionFeasible.size() + "\n");
		for (int z = 0; z < bestSolutionFeasible.size(); z++) {
			if (bestSolutionFeasible.get(z).tourProfit > highestProfit) {
				highestProfit = bestSolutionFeasible.get(z).tourProfit;
				vehiclesPrint = bestSolutionFeasible.get(z).vehiclesTour;
			}
		}

		printTourWithAdditionalInformation(vehiclesPrint);

		if (!(Solution_Label == "Greedy Solution")) {
			// System.out.println(vehiclesPrint[50]);
		}
	}

	public void printTourWithAdditionalInformation(Vehicle[] vehiclesPrint) {
		double tourProfit = 0;
		double totalProfit = 0;
		for (int j = 0; j < vehiclesPrint.length; j++) {
			tourProfit = 0;
			if (!vehiclesPrint[j].Route.isEmpty()) {
				System.out.print("Vehicle " + (j + 1) + ": ");
				int RoutSize = vehiclesPrint[j].Route.size();
				int actLoad = 0;

				for (int k = 0; k < RoutSize; k++) {
					if (k == RoutSize - 1) {
						System.out.print(
								vehiclesPrint[j].Route.get(k).NodeId + "(" + vehiclesPrint[j].Route.get(k).serviceTime
										+ " - " + vehiclesPrint[j].Route.get(k).profit + ")\n");
					} else {
						System.out.print(
								vehiclesPrint[j].Route.get(k).NodeId + "(" + vehiclesPrint[j].Route.get(k).serviceTime
										+ " - " + vehiclesPrint[j].Route.get(k).profit + ")" + " -> ");
					} // + " (" + NODES[vehicles[j].Route.get(k).NodeId].demand + " )" +
					actLoad += vehiclesPrint[j].Route.get(k).serviceTime;
					tourProfit += vehiclesPrint[j].Route.get(k).profit;
				}
				System.out.println(" Load of Vehicle: " + vehiclesPrint[j].load + " Time Of Service: " + actLoad);
				System.out.println("Traveled Distance: " + (vehiclesPrint[j].load - actLoad)
						+ " Total Profit collected:" + tourProfit + "\n");
			}
			if (j < vehiclesPrint.length - 1) {
				totalProfit += tourProfit;
			}
		}
		System.out.println("\nSolution Value " + totalProfit + "\n");
	}
}