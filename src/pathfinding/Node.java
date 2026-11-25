package pathfinding;

import warehouse.Position;

public class Node {
    public final Position position;
    public final Node parent;
    public final int distance;

    public Node(Position position, Node parent, int distance) {
        this.position = position;
        this.parent = parent;
        this.distance = distance;
    }
}
