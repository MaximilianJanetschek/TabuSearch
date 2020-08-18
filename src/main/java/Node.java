
public class Node {
	// Class variables
	public int NodeId;
	public double NODE_X, NODE_Y; // Node Coordinates
	public double profit; // Node Demand if Customer
	public double serviceTime;
	public boolean isRouted; // True if part of a route

	public Node(int depot_x, int depot_y) // Cunstructor for depot
	{
		this.NodeId = 0;
		this.NODE_X = depot_x;
		this.NODE_Y = depot_y;
	}

	public Node(int id, double x, double y, double profit, double serviceTime) // Cunstructor for Customers
	{
		this.NodeId = id;
		this.NODE_X = x;
		this.NODE_Y = y;
		this.profit = profit;
		this.serviceTime = serviceTime;
		this.isRouted = false;
	}

}