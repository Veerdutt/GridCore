package pathfinding;

import warehouse.Position;
import java.util.Collections;
import java.util.List;

public class PathResult {
    private final boolean reachable;
    private final int distance;
    private final List<Position> path;

    public PathResult(boolean reachable, int distance, List<Position> path) {
        this.reachable = reachable;
        this.distance = distance;
        this.path = path != null ? Collections.unmodifiableList(path) : Collections.emptyList();
    }

    public boolean isReachable() {
        return reachable;
    }

    public int getDistance() {
        return distance;
    }

    public List<Position> getPath() {
        return path;
    }
}
