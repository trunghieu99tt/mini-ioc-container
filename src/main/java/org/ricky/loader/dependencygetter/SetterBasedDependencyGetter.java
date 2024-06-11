package org.ricky.loader.dependencygetter;

import org.ricky.annotation.Autowired;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SetterBasedDependencyGetter implements DependencyGetter {
  @Override
  public List<String> get(Class<?> clazz, List<String> beanNames) {
    final Method[] methods = clazz.getDeclaredMethods();
    return Arrays.stream(methods)
        .filter(
            method -> method.getName().startsWith("set")
                && method.getParameterTypes().length == 1
                && Arrays.stream(method.getDeclaredAnnotations())
                .anyMatch(a -> a.annotationType() == Autowired.class)
        )
        .map(
            method -> {
              var parameters = method.getParameters();
              return Arrays.stream(parameters)
                  .map(Parameter::getName)
                  .filter(beanNames::contains)
                  .toList();
            }
        )
        .flatMap(Collection::stream)
        .toList();
  }
}
