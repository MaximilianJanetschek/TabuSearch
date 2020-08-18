
class PositionNodeInRoute {
	Node node;
	int position;
	double enteringCost;

	public PositionNodeInRoute(Node node) {
		this.node = node;
		this.position = -1;
		this.enteringCost = Double.MAX_VALUE;
	}

	public PositionNodeInRoute(PositionNodeInRoute newExample) {
		this.node = newExample.node;
		this.position = newExample.position;
		this.enteringCost = newExample.enteringCost;
	}

	public PositionNodeInRoute() {
		this.node = null;
		this.position = -1;
		this.enteringCost = Double.MAX_VALUE;
	}
}