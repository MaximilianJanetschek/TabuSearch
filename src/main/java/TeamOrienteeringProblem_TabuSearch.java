import java.io.IOException;


public class TeamOrienteeringProblem_TabuSearch {

	public static void executeSearch(String nameOfInstance) throws IOException {
		
		Documentation.clearEverything();
		// -----------------------------------------Parameters and Random
		// Data----------------------------------------//

		String instanceSource = nameOfInstance; 						// say "random" if you want to create an instance
		Instance testInstance = new Instance(instanceSource);

		testInstance.calculateDistanceMatrix();
		//testInstance.printInstanceInformation();

		Solution s = new Solution(testInstance); // New instance of the solution class

		// --------------------------------------------Calling the Solution
		// Heuristics----------------------------------------------//

		//System.out.println("\nAttempting to resolve Vehicle Routing Problem (VRP) for " + testInstance.NUMBER_OF_CUSTOMERS + " Customers and " + testInstance.NUMBER_OF_VEHICLES + " vehicles"+ " with " + testInstance.VEHICLE_CAPACITY + " units of capacity\n");

		// ---Greedy Heuristic---//
		s.greedyHeuristic(); // Call the greedy heuristic

		//s.SolutionPrint("Greedy Solution"); // Print the greedy solution

		//draw.drawRoutes(s, "Greedy_Solution"); // Draw the greedy solution

		// ---Tabu Search Heuristic---//

		s.resetEverything();

		s.adaptiveMemoryProcessTabuSearch();

		//s.SolutionPrint("Solution After Tabu Search");

		//draw.drawRoutes(s, "TABU_Solution");
		
		Documentation.setBestSolutionOfRun();
		
		//System.out.println(Documentation.bestSolution.tourProfit);
		
		//Application.launch(LineChartOutput.class);
	}
}