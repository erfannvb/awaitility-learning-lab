package nvb.dev.service.impl;

import nvb.dev.exception.OrderNotFoundException;
import nvb.dev.model.Order;
import nvb.dev.model.OrderStatus;
import nvb.dev.service.OrderService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderServiceImpl implements OrderService, AutoCloseable {

    private final Map<UUID, Order> orderMap = new HashMap<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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
        executorService.submit(() -> {
            order.updateStatus(OrderStatus.PROCESSING);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            if (order.getOrderStatus() != OrderStatus.FAILED)
                order.updateStatus(OrderStatus.COMPLETED);
        });
    }

    @Override
    public void failOrder(UUID id) {
        Order order = getOrder(id);
        order.updateStatus(OrderStatus.FAILED);
    }

    @Override
    public void close() {
        executorService.shutdown();
    }
}
