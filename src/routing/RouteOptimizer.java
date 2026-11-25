package routing;

import warehouse.Item;
import warehouse.Position;
import pathfinding.PathFinder;
import pathfinding.PathResult;
import java.util.ArrayList;
import java.util.List;

public class RouteOptimizer {
    private final PathFinder pathFinder;

    public RouteOptimizer(PathFinder pathFinder) {
        this.pathFinder = pathFinder;
    }

    public RouteResult optimizeRoute(Position startPosition, List<Item> items) {
        if (items == null || items.isEmpty()) {
            return new RouteResult(
                List.of(),
                List.of(),
                startPosition != null ? List.of(startPosition) : List.of(),
                0,
                startPosition != null
            );
        }

        List<Item> remaining = new ArrayList<>(items);
        List<Item> visitingOrder = new ArrayList<>();
        List<RouteResult.Segment> segments = new ArrayList<>();
        List<Position> fullRoute = new ArrayList<>();

        Position currentPos = startPosition;
        String currentLabel = "Start " + startPosition;
        int totalDistance = 0;

        if (startPosition != null) {
            fullRoute.add(startPosition);
        }

        while (!remaining.isEmpty()) {
            Item bestItem = null;
            PathResult bestPath = null;

            for (Item candidate : remaining) {
                PathResult res = pathFinder.findShortestPath(currentPos, candidate.getPosition());
                if (res.isReachable()) {
                    if (bestPath == null || res.getDistance() < bestPath.getDistance()) {
                        bestPath = res;
                        bestItem = candidate;
                    }
                }
            }

            if (bestItem == null || bestPath == null) {
                return new RouteResult(visitingOrder, segments, fullRoute, totalDistance, false);
            }

            String nextLabel = bestItem.getName() + " " + bestItem.getPosition();
            segments.add(new RouteResult.Segment(
                currentLabel, nextLabel, bestPath.getDistance(), bestPath.getPath()
            ));

            List<Position> segPath = bestPath.getPath();
            for (int i = 1; i < segPath.size(); i++) {
                fullRoute.add(segPath.get(i));
            }

            totalDistance += bestPath.getDistance();
            visitingOrder.add(bestItem);
            remaining.remove(bestItem);

            currentPos = bestItem.getPosition();
            currentLabel = nextLabel;
        }

        return new RouteResult(visitingOrder, segments, fullRoute, totalDistance, true);
    }
}
