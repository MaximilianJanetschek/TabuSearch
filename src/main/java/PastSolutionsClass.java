
public class PastSolutionsClass {
	public double tourProfit;
	double quasiReward;
	Vehicle[] vehiclesTour;
	boolean infeasible =false;
	boolean tabu = false;
	double penaltyCoefficient;
	int iterationFound;
	
	public PastSolutionsClass (Vehicle [] Vehicle, double penaltyCoefficient) {
		vehiclesTour= new Vehicle[Vehicle.length];
		this.penaltyCoefficient = penaltyCoefficient;
		for(int i = 0; i<Vehicle.length; i++) {
			vehiclesTour[i] = new Vehicle (Vehicle[i]);
		}
		
		tourProfit=0;
		quasiReward=0;
		
		for (int i = 0; i < (vehiclesTour.length-1); i++) {									//calculating profit and tabu for all tours except for last one
			for ( int j = 0; j < vehiclesTour[i].Route.size()-1; j++) {
				tourProfit += vehiclesTour[i].Route.get(j).profit;
				quasiReward += vehiclesTour[i].Route.get(j).profit;
			}

			if (vehiclesTour[i].load > vehiclesTour[i].capacity) {
				double violation;
				violation  = vehiclesTour[i].load - vehiclesTour[i].capacity;
				infeasible = true;
				quasiReward = quasiReward -  (penaltyCoefficient * violation);
			}				
		}
	}
	
	public PastSolutionsClass copySolution ()	{
		PastSolutionsClass copyOfSolution = new PastSolutionsClass();
		copyOfSolution.tourProfit = tourProfit;
		copyOfSolution.quasiReward = this.quasiReward;
		copyOfSolution.penaltyCoefficient = this.penaltyCoefficient;
		copyOfSolution.infeasible = this.infeasible;
		copyOfSolution.tabu = this.tabu;
		copyOfSolution.iterationFound = this.iterationFound;
		copyOfSolution.vehiclesTour= new Vehicle[this.vehiclesTour.length];
		for(int i = 0; i<this.vehiclesTour.length; i++) {
			copyOfSolution.vehiclesTour[i] = new Vehicle (this.vehiclesTour[i]);
		}
		return copyOfSolution;
	}
	
	public PastSolutionsClass () {
		tourProfit = -1 *Double.MAX_VALUE;
		quasiReward = -1 * Double.MAX_VALUE;
	}
}