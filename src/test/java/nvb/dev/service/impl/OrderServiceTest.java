package nvb.dev.service.impl;

import nvb.dev.model.Order;
import nvb.dev.model.OrderStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderServiceTest {

    @Test
    void shouldCompleteOrderEventually() {
        try (OrderServiceImpl orderService = new OrderServiceImpl()) {
            Order order = orderService.createOrder();
            orderService.processOrder(order.getId());

            await()
                    .pollDelay(Duration.ofSeconds(1))
                    .pollInterval(Duration.ofMillis(200))
                    .atMost(Duration.ofSeconds(3))
                    .failFast(() -> order.getOrderStatus() == OrderStatus.FAILED)
                    .untilAsserted(() -> assertEquals(OrderStatus.COMPLETED, order.getOrderStatus()));
        }
    }

    @Test
    void shouldReturnImmediatelyWhenProcessingOrder() {
        try (OrderServiceImpl orderService = new OrderServiceImpl()) {
            Order order = orderService.createOrder();

            long startTime = System.nanoTime();

            orderService.processOrder(order.getId());

            long endTime = System.nanoTime();
            long elapsed = (endTime - startTime) / 1_000_000;

            assertTrue(elapsed < 500);
        }
    }

    @Test
    void shouldRemainProcessingForAtLeast500Milliseconds() {
        try (OrderServiceImpl orderService = new OrderServiceImpl()) {
            Order order = orderService.createOrder();
            orderService.processOrder(order.getId());
            await()
                    .atMost(Duration.ofSeconds(1))
                    .during(Duration.ofMillis(500))
                    .until(() -> order.getOrderStatus() == OrderStatus.PROCESSING);
        }
    }
}