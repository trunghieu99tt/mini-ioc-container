package org.ricky.service;

import org.ricky.annotation.Autowired;
import org.ricky.annotation.Component;

import java.util.List;

@Component
public class OrderRepository {
  @Autowired
  private OrderService orderService;

  public List<String> getOrderIds() {
    return List.of("1", "2", "3");
  }
}
