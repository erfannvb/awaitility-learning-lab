package nvb.dev.service;

import nvb.dev.model.Order;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public interface OrderService {

    Order createOrder();
    Order getOrder(UUID id);
    void processOrder(UUID id, CountDownLatch latch);
    void failOrder(UUID id);

}
