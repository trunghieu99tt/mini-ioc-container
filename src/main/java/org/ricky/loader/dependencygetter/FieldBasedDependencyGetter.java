package org.ricky.loader.dependencygetter;

import org.ricky.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class FieldBasedDependencyGetter implements DependencyGetter {
  @Override
  public List<String> get(Class<?> clazz, List<String> beanNames) {
    final Field[] fields = clazz.getDeclaredFields();
    return Arrays.stream(fields)
        .filter(
            field -> Arrays.stream(field.getDeclaredAnnotations())
                .anyMatch(a -> a.annotationType() == Autowired.class)
        )
        .map(field -> field.getType().getName())
        .filter(beanNames::contains)
        .toList();
  }
}
