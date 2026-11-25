package dispatch;

import warehouse.Position;
import routing.Order;
import routing.OrderStatus;
import routing.RouteOptimizer;
import routing.RouteResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderManager {
    private final PickerDispatcher dispatcher;
    private final RouteOptimizer routeOptimizer;
    private final Map<String, Order> activeOrders;
    private final List<Order> completedOrders;

    public OrderManager(PickerDispatcher dispatcher, RouteOptimizer routeOptimizer) {
        this.dispatcher = dispatcher;
        this.routeOptimizer = routeOptimizer;
        this.activeOrders = new LinkedHashMap<>();
        this.completedOrders = new ArrayList<>();
    }

    public boolean submitOrder(Order order) {
        if (order == null || order.getItems().isEmpty()) {
            return false;
        }
        if (activeOrders.containsKey(order.getOrderId())) {
            System.out.println("Order " + order.getOrderId() + ": Order ID is already active.");
            return false;
        }

        Position firstTarget = order.getItems().get(0).getPosition();
        Picker selected = dispatcher.findNearestIdlePicker(firstTarget);

        if (selected == null) {
            System.out.println("Order " + order.getOrderId() + ": No IDLE picker available.");
            return false;
        }

        RouteResult route = routeOptimizer.optimizeRoute(selected.getCurrentPosition(), order.getItems());
        if (!route.isComplete()) {
            System.out.println("Order " + order.getOrderId() + ": One or more items are unreachable.");
            return false;
        }
        order.setPlannedRoute(route);
        order.setAssignedPicker(selected);
        order.setStatus(OrderStatus.ASSIGNED);

        selected.setAssignedOrder(order);
        activeOrders.put(order.getOrderId(), order);
        return true;
    }

    public void startPicking(String orderId) {
        Order order = activeOrders.get(orderId);
        if (order == null) {
            System.out.println("Order " + orderId + " not found in active orders.");
            return;
        }
        if (order.getStatus() != OrderStatus.ASSIGNED) {
            System.out.println("Order " + orderId + " cannot start picking from state " + order.getStatus() + ".");
            return;
        }
        order.setStatus(OrderStatus.PICKING);
        if (order.getAssignedPicker() != null) {
            order.getAssignedPicker().setStatus(PickerStatus.PICKING);
        }
    }

    public void startPacking(String orderId) {
        Order order = activeOrders.get(orderId);
        if (order == null) {
            System.out.println("Order " + orderId + " not found in active orders.");
            return;
        }
        if (order.getStatus() != OrderStatus.PICKING) {
            System.out.println("Order " + orderId + " cannot start packing from state " + order.getStatus() + ".");
            return;
        }
        order.setStatus(OrderStatus.PACKING);
        if (order.getAssignedPicker() != null) {
            order.getAssignedPicker().setStatus(PickerStatus.PACKING);
            if (order.getPlannedRoute() != null && !order.getPlannedRoute().getFullRoute().isEmpty()) {
                List<Position> full = order.getPlannedRoute().getFullRoute();
                order.getAssignedPicker().setCurrentPosition(full.get(full.size() - 1));
            }
        }
    }

    public void completeOrder(String orderId) {
        Order order = activeOrders.get(orderId);
        if (order == null) {
            System.out.println("Order " + orderId + " not found in active orders.");
            return;
        }
        if (order.getStatus() != OrderStatus.PACKING) {
            System.out.println("Order " + orderId + " cannot complete from state " + order.getStatus() + " (must be PACKING).");
            return;
        }
        activeOrders.remove(orderId);
        order.setStatus(OrderStatus.COMPLETED);
        completedOrders.add(order);

        Picker picker = order.getAssignedPicker();
        if (picker != null) {
            picker.setStatus(PickerStatus.IDLE);
            picker.setAssignedOrder(null);
        }
    }

    public List<Order> getCompletedOrders() {
        return Collections.unmodifiableList(completedOrders);
    }
}
