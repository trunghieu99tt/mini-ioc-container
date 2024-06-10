package org.ricky.service;

import lombok.extern.slf4j.Slf4j;
import org.ricky.annotation.Autowired;
import org.ricky.annotation.Component;
import org.ricky.annotation.PostConstruct;

@Slf4j
@Component
public class OrderService {

  @Autowired
  private PaymentService paymentService;

  @Autowired
  private RestaurantService restaurantService;

  @PostConstruct
  void postInitiate() {
    System.out.println("Do something after creating orderService instance");
  }

  public void makeOrder() {
    paymentService.doSomething();
    restaurantService.doSomething();
  }
}