package org.ricky.service;

import lombok.extern.slf4j.Slf4j;
import org.ricky.annotation.Autowired;
import org.ricky.annotation.Component;
import org.ricky.annotation.PostConstruct;

import java.util.List;

@Slf4j
@Component
public class OrderService {

  private OrderRepository orderRepository;

  private PaymentService paymentService;

  @Autowired
  private RestaurantService restaurantService;

  @Autowired
  public OrderService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Autowired
  public void setPaymentService(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @PostConstruct
  void postInitiate() {
    System.out.println("Do something after creating orderService instance");
  }

  public void makeOrder() {
    paymentService.doSomething();
    restaurantService.doSomething();
    final List<String> orderIds = orderRepository.getOrderIds();
    System.out.println("orderIds = " + orderIds);
  }
}