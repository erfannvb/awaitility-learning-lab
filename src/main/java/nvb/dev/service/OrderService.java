package nvb.dev.service;

import nvb.dev.model.Order;

import java.util.UUID;

public interface OrderService {

    Order createOrder();
    Order getOrder(UUID id);
    void processOrder(UUID id);
    void failOrder(UUID id);

}
