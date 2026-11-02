package nvb.dev.model;

import java.util.UUID;

public class Order {

    private final UUID id;
    private OrderStatus orderStatus;

    public Order() {
        this.id = UUID.randomUUID();
        this.orderStatus = OrderStatus.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void updateStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
