package routing;

import warehouse.Item;
import warehouse.Position;
import java.util.Collections;
import java.util.List;

public class RouteResult {
    public static class Segment {
        public final String fromLabel;
        public final String toLabel;
        public final int distance;
        public final List<Position> path;

        public Segment(String fromLabel, String toLabel, int distance, List<Position> path) {
            this.fromLabel = fromLabel;
            this.toLabel = toLabel;
            this.distance = distance;
            this.path = path;
        }

        @Override
        public String toString() {
            return fromLabel + " -> " + toLabel + " (" + distance + ")";
        }
    }

    private final List<Item> visitingOrder;
    private final List<Segment> segments;
    private final List<Position> fullRoute;
    private final int totalDistance;
    private final boolean complete;

    public RouteResult(
        List<Item> visitingOrder,
        List<Segment> segments,
        List<Position> fullRoute,
        int totalDistance,
        boolean complete
    ) {
        this.visitingOrder = Collections.unmodifiableList(visitingOrder);
        this.segments = Collections.unmodifiableList(segments);
        this.fullRoute = Collections.unmodifiableList(fullRoute);
        this.totalDistance = totalDistance;
        this.complete = complete;
    }

    public List<Item> getVisitingOrder() {
        return visitingOrder;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public List<Position> getFullRoute() {
        return fullRoute;
    }

    public int getTotalDistance() {
        return totalDistance;
    }

    public boolean isComplete() {
        return complete;
    }
}
