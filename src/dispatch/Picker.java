package dispatch;

import warehouse.Position;
import routing.Order;

public class Picker {
    private final String pickerId;
    private Position currentPosition;
    private PickerStatus status;
    private Order assignedOrder;

    public Picker(String pickerId, Position initialPosition) {
        this.pickerId = pickerId;
        this.currentPosition = initialPosition;
        this.status = PickerStatus.IDLE;
        this.assignedOrder = null;
    }

    public String getPickerId() {
        return pickerId;
    }

    public Position getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Position currentPosition) {
        this.currentPosition = currentPosition;
    }

    public PickerStatus getStatus() {
        return status;
    }

    public void setStatus(PickerStatus status) {
        this.status = status;
    }

    public Order getAssignedOrder() {
        return assignedOrder;
    }

    public void setAssignedOrder(Order assignedOrder) {
        this.assignedOrder = assignedOrder;
    }

    @Override
    public String toString() {
        String orderId = (assignedOrder != null) ? assignedOrder.getOrderId() : "None";
        return pickerId + " | Pos: " + currentPosition + " | Status: " + status + " | Order: " + orderId;
    }
}
