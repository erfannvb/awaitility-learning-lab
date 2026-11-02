package nvb.dev.service.impl;

import nvb.dev.exception.OrderNotFoundException;
import nvb.dev.model.Order;
import nvb.dev.model.OrderStatus;
import nvb.dev.service.OrderService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OrderServiceImpl implements OrderService {

    private final Map<UUID, Order> orderMap = new HashMap<>();

    @Override
    public Order createOrder() {
        Order order = new Order();
        orderMap.put(order.getId(), order);
        return order;
    }

    @Override
    public Order getOrder(UUID id) {
        Order order = orderMap.get(id);
        if (order == null)
            throw new OrderNotFoundException("Order does not exist!");
        return order;
    }

    @Override
    public void processOrder(UUID id) {
        Order order = getOrder(id);
        order.updateStatus(OrderStatus.PROCESSING);
        order.updateStatus(OrderStatus.COMPLETED);
    }
}
