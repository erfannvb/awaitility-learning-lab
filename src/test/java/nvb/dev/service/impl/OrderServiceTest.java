package nvb.dev.service.impl;

import nvb.dev.model.Order;
import nvb.dev.model.OrderStatus;
import org.awaitility.core.TerminalFailureException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    @Test
    void shouldCompleteOrderEventually() {
        try (OrderServiceImpl orderService = new OrderServiceImpl()) {
            Order order = orderService.createOrder();

            CountDownLatch latch = new CountDownLatch(1);
            orderService.processOrder(order.getId(), latch);
            boolean started = latch.await(3, TimeUnit.SECONDS);
            assertTrue(started);

            await()
                    .pollDelay(Duration.ofSeconds(1))
                    .pollInterval(Duration.ofMillis(200))
                    .failFast(() -> order.getOrderStatus() == OrderStatus.FAILED)
                    .untilAsserted(() -> assertEquals(OrderStatus.COMPLETED, order.getOrderStatus()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldReturnImmediatelyWhenProcessingOrder() {
        try (OrderServiceImpl orderService = new OrderServiceImpl()) {
            Order order = orderService.createOrder();

            long startTime = System.nanoTime();

            CountDownLatch latch = new CountDownLatch(1);
            orderService.processOrder(order.getId(), latch);
            long endTime = System.nanoTime();
            boolean started = latch.await(3, TimeUnit.SECONDS);
            assertTrue(started);

            long elapsed = (endTime - startTime) / 1_000_000;

            assertTrue(elapsed < 500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldRemainProcessingForAtLeast500Milliseconds() {
        try (OrderServiceImpl orderService = new OrderServiceImpl()) {
            Order order = orderService.createOrder();

            CountDownLatch latch = new CountDownLatch(1);
            orderService.processOrder(order.getId(), latch);
            boolean started = latch.await(3, TimeUnit.SECONDS);
            assertTrue(started);

            await()
                    .during(Duration.ofMillis(500))
                    .until(() -> order.getOrderStatus() == OrderStatus.PROCESSING);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldFailImmediatelyWhenOrderProcessingFails() {
        try (OrderServiceImpl orderService = new OrderServiceImpl()) {
            Order order = orderService.createOrder();

            CountDownLatch latch = new CountDownLatch(1);
            orderService.processOrder(order.getId(), latch);
            boolean started = latch.await(3, TimeUnit.SECONDS);
            assertTrue(started);

            orderService.failOrder(order.getId());

            assertThrows(TerminalFailureException.class, () -> {
                await()
                        .atMost(Duration.ofSeconds(3))
                        .failFast(() -> order.getOrderStatus() == OrderStatus.FAILED)
                        .untilAsserted(() -> assertEquals(OrderStatus.COMPLETED, order.getOrderStatus()));
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}