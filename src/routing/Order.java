package routing;

import warehouse.Item;
import dispatch.Picker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    private final String orderId;
    private final List<Item> items;
    private Picker assignedPicker;
    private OrderStatus status;
    private RouteResult plannedRoute;

    public Order(String orderId, List<Item> items) {
        this.orderId = orderId;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.status = OrderStatus.NEW;
    }

    public String getOrderId() {
        return orderId;
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Picker getAssignedPicker() {
        return assignedPicker;
    }

    public void setAssignedPicker(Picker assignedPicker) {
        this.assignedPicker = assignedPicker;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public RouteResult getPlannedRoute() {
        return plannedRoute;
    }

    public void setPlannedRoute(RouteResult plannedRoute) {
        this.plannedRoute = plannedRoute;
    }
}
