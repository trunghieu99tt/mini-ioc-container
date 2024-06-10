package org.ricky;

import lombok.extern.log4j.Log4j2;
import org.ricky.annotation.Component;
import org.ricky.loader.ContextLoader;
import org.ricky.service.OrderService;
import org.ricky.service.RestaurantService;

@Log4j2
@Component
public class Application {
  public static void main(String[] args) {
    ContextLoader.getInstance().load("org.ricky");
    final OrderService orderService = ContextLoader.getInstance().getBean(OrderService.class);
    orderService.makeOrder();
    final RestaurantService restaurantService = ContextLoader.getInstance().getBean(RestaurantService.class);
    restaurantService.logToday();
  }
}