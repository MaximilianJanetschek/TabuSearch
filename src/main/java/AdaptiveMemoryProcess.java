import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class AdaptiveMemoryProcess {
	ArrayList<Vehicle>  setN;
	ArrayList<Vehicle>	setNForConstruction;
    ArrayList<PastSolutionsClass> pastSolutionsTabuAMP;
	
	int NUMBER_VEHICLE_TOURS;
	int NUMBER_OF_CUSTOMERS;
	Instance instance;
	int numbPotentVehicleTours = 30;									//tau
	Vehicle [] vehicles;
	int NUMBER_OF_TOURS_IN_SET_N = 1500;
	
	public AdaptiveMemoryProcess(Instance instance) {
		this.NUMBER_OF_CUSTOMERS = instance.NUMBER_OF_CUSTOMERS;
		this.NUMBER_VEHICLE_TOURS = instance.NUMBER_OF_VEHICLES;
		this.instance = instance;
		this.vehicles = new Vehicle [NUMBER_VEHICLE_TOURS];
        pastSolutionsTabuAMP = new ArrayList<>();
        setN = new ArrayList <>();
        setNForConstruction = new ArrayList<>();
        for (int i = 0 ; i < instance.NUMBER_OF_VEHICLES; i++)	{
            if( i == (instance.NUMBER_OF_VEHICLES -1) ){
            	vehicles[i] = new Vehicle(i+1, Integer.MAX_VALUE);
            }
            else {
            		vehicles[i] = new Vehicle(i+1,instance.VEHICLE_CAPACITY);									//Construction requires the id and the capacity
            }
        }
	}
	
	public Vehicle[] greedyHeuristic () {
		generatingPartialSolutions();
		return vehicles;
	}
	
	public void generateSetN () {
		for(int i = 0; i < (NUMBER_OF_TOURS_IN_SET_N/(NUMBER_VEHICLE_TOURS-1)); i++) {
			generatingPartialSolutions();
			for(int j = 0; j < vehicles.length-1; j++)  {													//ohne letze Tour
				this.setN.add(new Vehicle(vehicles[j]));
			}
		}
	}
	
    public void generatingPartialSolutions() {
        
        instance.resetStatusRouted();
        resetVehicleRoutes(vehicles);
    	vehicles = enterAdditionalCustomerToSolution(vehicles,0);

        addSolution(vehicles,0);  
   }
    
    public void resetVehicleRoutes(Vehicle[] vehicle) {
        for (int i = 0; i < vehicle.length; i++) {
        	if (vehicle[i] != null) {
        		vehicle[i].Route.clear();
        	}
        }
    }
    
	public Vehicle[] enterAdditionalCustomerToSolution (Vehicle[] currentTours, int startingTour) {
        
		int VehIndex = startingTour;
		Vehicle[] toursImprovement = Arrays.copyOf(currentTours, currentTours.length);
        while (UnassignedCustomerExists()) {											//As long as not all customers have been assigned to a tour
	    	PositionNodeInRoute enterNode;												//object containing all Information for entering a Node at best Position

	        if (toursImprovement[VehIndex].Route.isEmpty())	{								//If the vehicle has been unassigned so far
	        	toursImprovement[VehIndex].enterRandomNodeToRoute(instance);				
	        }              
	        else {            	
	        	enterNode = toursImprovement[VehIndex].addBestNodeToRoute(instance);
	        	if ( enterNode.node  == null)		{										//If not a single Customer Fits              
	        		if ( VehIndex+1 < toursImprovement.length ) 	{						//If we have more vehicles to assign                
	        			VehIndex = VehIndex+1; 												//Go to next Vehicle
	        		}
	        	}
	        	else	{		//If a fitting customer is found
	        		toursImprovement[VehIndex].enterNodeToRoute(instance, enterNode);
	        	}
	        }  
        }
        return toursImprovement;
	}
	
    public boolean UnassignedCustomerExists()							//Checks, if all customers (NODES) have been assigned to a tour
    {
        for (int i = 1; i < instance.NODES.length-1; i++)  {
            if (!instance.NODES[i].isRouted)	{
            	return true;
            }
        }
        return false;
    }
    
    public void addSolution (Vehicle[] vehicle, double penaltyCoefficient) {

        pastSolutionsTabuAMP.add(new PastSolutionsClass(vehicle, penaltyCoefficient));

    }
    
	public void updateSetN (ArrayList<PastSolutionsClass> pastSolutionsTabu) {
		
		if (pastSolutionsTabu.size() > 0) {
			for (int j = 0; j < instance.NUMBER_OF_VEHICLES-1; j++) {
				double worstValue = Double.MAX_VALUE;
				int removeIndex = -1;
				for (int i = 0; i < setN.size(); i++) {
					if (worstValue>setN.get(i).profitVehicle) {
						worstValue = setN.get(i).profitVehicle;
						removeIndex = i;						
					}
				}
				if (removeIndex == -1) {
					System.out.println("Sth is wrong!");
				}
				else {
					setN.remove(removeIndex);
				}
			}
			for (int i = 0; i < (pastSolutionsTabu.get(pastSolutionsTabu.size()-1).vehiclesTour.length-1); i++)	{
				setN.add(new Vehicle (pastSolutionsTabu.get(pastSolutionsTabu.size()-1).vehiclesTour[i]));
			}
		}		
	}
	
    public Vehicle[] constructAMPSolution () {

		int ampCounter = 0;

		Vehicle[] vehicleAMP = new Vehicle [NUMBER_VEHICLE_TOURS];
		setNForConstruction.clear();
		for ( int i = 0; i < setN.size(); i++) {
			setNForConstruction.add(new Vehicle(setN.get(i)));
		}
		
		boolean terminationAMP = false;
		while (!terminationAMP) {
	
			Vehicle[] candidateTourForInitialSolution = generateSetOfCandidateTours();
			
	   		//sort selected Tours according to tour profit
	   		Arrays.sort(candidateTourForInitialSolution, new SortByProfit());			//should be already sorted by selection
	   		
	   		//Assigning index Value
	   		assignIndexValue(candidateTourForInitialSolution); 		

			vehicleAMP[ampCounter] = selectCandiateTour(candidateTourForInitialSolution);
   		
			updateSetN(vehicleAMP[ampCounter]);
			
			if (setNForConstruction.isEmpty()) {
				terminationAMP = true;
				for (int i = ampCounter+1; i<instance.NUMBER_OF_VEHICLES; i++) {
					if(i == (instance.NUMBER_OF_VEHICLES-1)) {
						vehicleAMP[i] = new Vehicle(i, Double.MAX_VALUE);
					}
					else {
						vehicleAMP[i] = new Vehicle(i,instance.VEHICLE_CAPACITY);				//Add dynamische Vehicle Cap
					}
					
				}
			}

			if (ampCounter == (instance.NUMBER_OF_VEHICLES-2)) {				//all tours except the fallback are filled
				terminationAMP=true;
				vehicleAMP[instance.NUMBER_OF_VEHICLES-1] = new Vehicle (instance.NUMBER_OF_VEHICLES, Double.MAX_VALUE);
			}
			ampCounter++;	
		}	   //now we have a feasible Solution
		
		//Add all NODES not contained in the Tours to last Tour or Routes Before
		instance.resetStatusRouted();
		instance.updateStatusIsRouted(vehicleAMP);
		
		vehicleAMP = enterAdditionalCustomerToSolution(vehicleAMP, ampCounter);

		return vehicleAMP;		
   }
    
	public Vehicle[] generateSetOfCandidateTours () {
		
		int candidateNumberOfVehicles=-1;
		if (setNForConstruction.size() < numbPotentVehicleTours) {
			candidateNumberOfVehicles = setNForConstruction.size();
		}
		else {
			candidateNumberOfVehicles = numbPotentVehicleTours;
		}
		Vehicle[] candidateTourForInitialSolution= new Vehicle[candidateNumberOfVehicles];
	
    	boolean [] isSelectedAMP = new boolean [setNForConstruction.size()];
    	for(int i = 0; i < isSelectedAMP.length; i++) {
    		isSelectedAMP[i]=false;
    	}

		for (int i =0; i< candidateNumberOfVehicles; i++) {							//get tours with highest Profit
    		double highestProfitTour=-1* Double.MAX_VALUE;
    		int bestVehicle=-1;
			for (int j = 0; j < setNForConstruction.size(); j++) {
				if (!isSelectedAMP[j]) {
					if(highestProfitTour < setNForConstruction.get(j).profitVehicle) {
						highestProfitTour=setNForConstruction.get(j).profitVehicle;
						bestVehicle=j;
					}
				}
			}
			candidateTourForInitialSolution[i]=new Vehicle (setNForConstruction.get(bestVehicle));
			isSelectedAMP[bestVehicle]=true;
		}
		return candidateTourForInitialSolution;
	}
	
	public void assignIndexValue (Vehicle[] vehicle) {
		double allTourProfits = 0;
		for(int i = 0; i < vehicle.length; i++) {
			allTourProfits += vehicle[i].profitVehicle;
		}
		
		if (allTourProfits > 0)	{
			for (int i = 0; i < vehicle.length; i++) {
				double partialTourProfits = 0;
				for (int j = 0; j <= i; j++) {
					partialTourProfits += vehicle[j].profitVehicle;
				}
				vehicle[i].index = partialTourProfits / allTourProfits;
			}
		}	else {
			double partialTourProfits;
			allTourProfits = vehicle.length-1;
			for (int i = 0; i < vehicle.length; i++) {
				partialTourProfits = i;
				for (int j = 0; j <= i; j++) {
					partialTourProfits += vehicle[j].profitVehicle;
				}
				vehicle[i].index = partialTourProfits / allTourProfits;
			}
		}
	}
	
	public Vehicle selectCandiateTour (Vehicle[] candidateTours) {
		Vehicle selectedCandidateTour;
		double randomAMP = Math.random();
		int selectedTourID =-1;
		for (int i =0; i<candidateTours.length; i++ ) {
			if (randomAMP < candidateTours[i].index) {
				selectedTourID = i;
				break;
			}
		}
		selectedCandidateTour = new Vehicle (candidateTours[selectedTourID]);
		return selectedCandidateTour;
	}
    
	public void updateSetN (Vehicle vehicle) {
		boolean keepTour;
		ArrayList<Vehicle> tempSetN = new ArrayList<> ();
		for (int j=0; j<setNForConstruction.size(); j++) {							//check in all tours in N
	    	keepTour=true;
			for (int k=1; k < vehicle.Route.size()-1; k++) {						//check for all NODES selected
				instance.setStatusRoutedOfNode(vehicle.Route.get(k));   
		    	for (int z=1; z<setNForConstruction.get(j).Route.size()-1; z++) {		//without Depot
					if (vehicle.Route.get(k).NodeId == setNForConstruction.get(j).Route.get(z).NodeId) {
						keepTour=false;
						break;
					}
				}
			}
	    	if (keepTour) {
	    		tempSetN.add(setNForConstruction.get(j));
	    	}
		}
	    setNForConstruction = tempSetN;
	}

	
}

class SortByProfit implements Comparator<Vehicle> {  
	public int compare(Vehicle a, Vehicle b) 
{        
	if ( a.profitVehicle < b.profitVehicle ) 
		return 1;        
else if ( a.profitVehicle == b.profitVehicle ) 
	return 0;        
else return -1;    }
}