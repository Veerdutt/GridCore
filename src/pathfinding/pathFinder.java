package pathfinding;

import warehouse.Position;
import warehouse.Warehouse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PathFinder {
    private static final int[] D_ROW = {-1, 1, 0, 0}; // Up, Down, Left, Right
    private static final int[] D_COL = {0, 0, -1, 1};

    private final Warehouse warehouse;

    public PathFinder(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public PathResult findShortestPath(Position start, Position destination) {
        if (!warehouse.isValidPosition(start) || !warehouse.isValidPosition(destination)) {
            return new PathResult(false, -1, Collections.emptyList());
        }

        // Edge case: start or destination is blocked by a rack
        if (warehouse.isObstacle(start) || warehouse.isObstacle(destination)) {
            return new PathResult(false, -1, Collections.emptyList());
        }

        // Edge case: start and destination are identical
        if (start.equals(destination)) {
            return new PathResult(true, 0, List.of(start));
        }

        boolean[][] visited = new boolean[Warehouse.ROWS][Warehouse.COLS];
        Queue<Node> queue = new LinkedList<>();

        visited[start.row][start.col] = true;
        queue.offer(new Node(start, null, 0));

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            Position curPos = current.position;

            if (curPos.equals(destination)) {
                List<Position> reconstructed = reconstructPath(current);
                return new PathResult(true, current.distance, reconstructed);
            }

            for (int i = 0; i < 4; i++) {
                int nextRow = curPos.row + D_ROW[i];
                int nextCol = curPos.col + D_COL[i];
                Position nextPos = new Position(nextRow, nextCol);

                if (warehouse.isValidPosition(nextPos)
                    && !visited[nextRow][nextCol]
                    && !warehouse.isObstacle(nextPos)) {
                    visited[nextRow][nextCol] = true;
                    queue.offer(new Node(nextPos, current, current.distance + 1));
                }
            }
        }

        // Destination unreachable
        return new PathResult(false, -1, Collections.emptyList());
    }

    private List<Position> reconstructPath(Node goalNode) {
        List<Position> path = new ArrayList<>();
        Node curr = goalNode;
        while (curr != null) {
            path.add(curr.position);
            curr = curr.parent;
        }
        Collections.reverse(path);
        return path;
    }
}
