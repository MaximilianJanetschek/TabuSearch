import java.util.ArrayList;

public class Documentation {
	static int iterationCounter = 0;
	static int ampCounter = 1;
	static PastSolutionsClass bestSolution = new PastSolutionsClass();
	static ArrayList<ArrayList<PastSolutionsClass>> tabuSearchOverallDocumentation = new ArrayList<ArrayList<PastSolutionsClass>>();
	static ArrayList<PastSolutionsClass> tabuSearchBestFeasibleSolution = new ArrayList<PastSolutionsClass>();
	static ArrayList<Boolean> feasiblityOverview = new ArrayList<>();
	
	public static ArrayList<PastSolutionsClass> getSafedFeasibleSolutions () 	{
		return tabuSearchBestFeasibleSolution;
	}
	
	public static void addSolutionToDocumentation (PastSolutionsClass newSolution)	{
		tabuSearchOverallDocumentation.get(0).add(newSolution);
	}
	
	public static void addNewBestFeasibleSolution(PastSolutionsClass newSolution)	{
		tabuSearchBestFeasibleSolution.add(newSolution);
	}
	
	public static void setBestSolutionOfRun ()	{
		int arraySize = tabuSearchBestFeasibleSolution.size();
		bestSolution = tabuSearchBestFeasibleSolution.get(arraySize-1);
	}
	
	public static void clearEverything() {
		iterationCounter = 0;
		ampCounter = 1;
		bestSolution = new PastSolutionsClass();
		tabuSearchOverallDocumentation = new ArrayList<ArrayList<PastSolutionsClass>>();
		tabuSearchBestFeasibleSolution = new ArrayList<PastSolutionsClass>();
		feasiblityOverview = new ArrayList<>();
	}
	
}