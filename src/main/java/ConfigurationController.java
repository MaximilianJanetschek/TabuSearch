import java.util.ArrayList;

public class ConfigurationController {
	static ArrayList <String> nameOfInstance = new ArrayList<>(); 
	static int NUMBER_OF_RUNS = 10;
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ExcelInterface.initializeResultSearch();
		nameOfInstance = ExcelInterface.readExcel();
		double [] resultsOfRuns = new double[NUMBER_OF_RUNS];
		
		for ( int instanceIndex = 1 ; instanceIndex < nameOfInstance.size(); instanceIndex++) {
			System.out.println("Solutions for run " + nameOfInstance.get(instanceIndex) + " are generated");
			for (int counterOfRuns = 1; counterOfRuns <= NUMBER_OF_RUNS; counterOfRuns++) {
				//ExcecuteTabu Search
				try {
					TeamOrienteeringProblem_TabuSearch.executeSearch(nameOfInstance.get(instanceIndex));
				} catch (Exception e) {
					e.printStackTrace();
				}
				// Safe Solution for later
				resultsOfRuns[counterOfRuns-1] = Documentation.bestSolution.tourProfit;
				System.out.println("Solution for run " + counterOfRuns +" genertated!");
			}

			ExcelInterface.writeSolutionToExcel(resultsOfRuns, instanceIndex);
		}
		
	}
}
