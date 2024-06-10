package org.ricky.service;


import lombok.extern.slf4j.Slf4j;
import org.ricky.annotation.Component;

import java.time.Instant;

@Slf4j
@Component
public class RestaurantService {

  public void doSomething() {
    System.out.println("Restaurant service does something");
  }

  public void logToday() {
    System.out.println("Today = " + Instant.now());
  }
}