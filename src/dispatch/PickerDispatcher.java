package dispatch;

import warehouse.Position;
import pathfinding.PathFinder;
import pathfinding.PathResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class PickerDispatcher {
    private static class PickerCandidate {
        final Picker picker;
        final int distance;

        PickerCandidate(Picker picker, int distance) {
            this.picker = picker;
            this.distance = distance;
        }
    }

    private final List<Picker> pickers;
    private final PathFinder pathFinder;

    public PickerDispatcher(List<Picker> pickers, PathFinder pathFinder) {
        this.pickers = pickers;
        this.pathFinder = pathFinder;
    }

    /** Returns all pickers currently idle and unassigned. */
    public List<Picker> getIdlePickers() {
        List<Picker> idle = new ArrayList<>();
        for (Picker picker : pickers) {
            if (picker.getStatus() == PickerStatus.IDLE && picker.getAssignedOrder() == null) {
                idle.add(picker);
            }
        }
        return idle;
    }

    public Picker findNearestIdlePicker(Position targetStart) {
        if (targetStart == null) return null;

        PriorityQueue<PickerCandidate> pq = new PriorityQueue<>(
            Comparator.comparingInt(c -> c.distance)
        );

        for (Picker picker : getIdlePickers()) {
            PathResult res = pathFinder.findShortestPath(picker.getCurrentPosition(), targetStart);
            if (res.isReachable()) {
                pq.offer(new PickerCandidate(picker, res.getDistance()));
            }
        }

        PickerCandidate nearest = pq.poll();
        return nearest != null ? nearest.picker : null;
    }
}
