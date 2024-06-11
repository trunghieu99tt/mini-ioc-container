package org.ricky.loader.dependencygetter;

import org.ricky.annotation.Autowired;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConstructorBasedDependencyGetter implements DependencyGetter {
  @Override
  public List<String> get(Class<?> clazz, List<String> beanNames) {
    for (Constructor<?> constructor : clazz.getConstructors()) {
      if (constructor.isAnnotationPresent(Autowired.class)) {
        return Arrays.stream(constructor.getParameters())
            .map(parameter -> parameter.getType().getName())
            .filter(beanNames::contains)
            .toList();
      }
    }

    return Collections.emptyList();
  }
}
