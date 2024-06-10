package org.ricky.service;

import lombok.extern.slf4j.Slf4j;
import org.ricky.annotation.Component;

@Slf4j
@Component
public class PaymentService {

  public void doSomething() {
    System.out.println("Payment service does something");
  }
}