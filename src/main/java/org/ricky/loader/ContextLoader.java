package org.ricky.loader;


import lombok.extern.log4j.Log4j2;
import org.reflections.Reflections;
import org.ricky.annotation.Autowired;
import org.ricky.annotation.Component;
import org.ricky.annotation.PostConstruct;
import org.ricky.loader.dependencygetter.ConstructorBasedDependencyGetter;
import org.ricky.loader.dependencygetter.DependencyGetter;
import org.ricky.loader.dependencygetter.FieldBasedDependencyGetter;
import org.ricky.loader.dependencygetter.SetterBasedDependencyGetter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Log4j2
public class ContextLoader {

  private static final ContextLoader INSTANCE = new ContextLoader();
  private final Map<Class<?>, Object> beans = new HashMap<>();
  private final List<DependencyGetter> dependencyGetters = List.of(
      new ConstructorBasedDependencyGetter(),
      new FieldBasedDependencyGetter(),
      new SetterBasedDependencyGetter()
  );
  private List<String> beanNames = new ArrayList<>();

  private ContextLoader() {
  }

  public static ContextLoader getInstance() {
    return INSTANCE;
  }


  @SuppressWarnings("unchecked")
  public <T> T getBean(Class<T> interfaceType) {
    if (beans.containsKey(interfaceType)) {
      return (T) beans.get(interfaceType);
    }
    throw new RuntimeException("No bean registered for type: " + interfaceType);
  }

  public synchronized void load(String scanPackage) {
    final Reflections reflections = new Reflections(scanPackage);
    final Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Component.class);
    this.beanNames = classes.stream().map(Class::getName).toList();
    final Map<String, List<String>> objWithDependencies = new HashMap<>();
    classes.forEach(clazz -> {
      List<String> beanDependencies = getDependencies(clazz);
      objWithDependencies.put(clazz.getName(), beanDependencies);
    });
    final BeansOrderResolver beansOrderResolver = new BeansOrderResolver(objWithDependencies);
    final Pair<List<String>, List<List<String>>> resolverResponse = beansOrderResolver.resolve();
    final List<String> serviceInitializationOrder = resolverResponse.getFirst();
    final List<List<String>> circulars = resolverResponse.getSecond();

    if (!circulars.isEmpty()) {
      throw new RuntimeException(
          "Circular dependency detected: " + circulars
      );
    }

    serviceInitializationOrder.forEach(
        beanName -> {
          try {
            final Optional<Class<?>> nullableClazz =
                classes.stream().filter(clazz -> clazz.getName().equals(beanName)).findFirst();
            if (nullableClazz.isEmpty()) {
              return;
            }
            final Class<?> clazz = nullableClazz.get();
            final Object instance = initBeanInstance(clazz);

            injectFieldBasedDependency(instance);
            injectSetterBasedDependencies(instance);
            invokePostInitiate(instance);
            beans.put(clazz, instance);
          } catch (Exception ex) {
            throw new RuntimeException(ex);
          }
        }
    );
  }

  private Object initBeanInstance(Class<?> clazz) throws InstantiationException, IllegalAccessException,
      InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
    Optional<Constructor<?>> nullableAutowiredConstructor = getConstructorForInjection(clazz);
    final Object instance;
    if (nullableAutowiredConstructor.isEmpty()) {
      instance = Class.forName(clazz.getName()).getDeclaredConstructor().newInstance();
    } else {
      Constructor<?> constructor = nullableAutowiredConstructor.get();
      var parameterTypes = constructor.getParameterTypes();
      Object[] dependencies = new Object[parameterTypes.length];
      for (int i = 0; i < parameterTypes.length; i++) {
        dependencies[i] = getBean(parameterTypes[i]);
      }
      instance = constructor.newInstance(dependencies);
    }
    return instance;
  }

  private Optional<Constructor<?>> getConstructorForInjection(Class<?> clazz) {
    for (Constructor<?> constructor : clazz.getConstructors()) {
      if (constructor.isAnnotationPresent(Autowired.class)) {
        return Optional.of(constructor);
      }
    }

    return Optional.empty();
  }

  private List<String> getDependencies(final Class<?> clazz) {
    final List<String> dependencies = new ArrayList<>();
    dependencyGetters.forEach(dependencyGetter -> dependencies.addAll(dependencyGetter.get(clazz, beanNames)));
    return dependencies;
  }


  private void injectFieldBasedDependency(final Object instance) {
    var fields = instance.getClass().getDeclaredFields();
    Arrays.stream(fields)
        .filter(
            field -> Arrays.stream(field.getDeclaredAnnotations())
                .anyMatch(a -> a.annotationType() == Autowired.class)
        )
        .forEach(
            field -> {
              final Object bean = getBean(field.getType());
              field.setAccessible(true);
              try {
                field.set(instance, bean);
              } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot inject dependency " + field.getClass().getName()
                    + " to " + instance.getClass().getName());
              }
            }
        );
  }

  private void injectSetterBasedDependencies(final Object instance) throws InvocationTargetException,
      IllegalAccessException {
    final Method[] methods = instance.getClass().getDeclaredMethods();
    for (final Method method : methods) {
      if (isAutowiredSetterMethod(method)) {
        Class<?> parameterType = method.getParameterTypes()[0];
        Object dependency = getBean(parameterType);
        if (dependency != null) {
          method.invoke(instance, dependency);
        }
      }
    }
  }

  private boolean isAutowiredSetterMethod(Method method) {
    return method.getName().startsWith("set")
        && method.getParameterTypes().length == 1
        && Arrays.stream(method.getDeclaredAnnotations())
        .anyMatch(a -> a.annotationType() == Autowired.class);
  }

  private void invokePostInitiate(Object instance) {
    var postMethods = Arrays.stream(instance.getClass().getDeclaredMethods())
        .filter(
            method -> Arrays.stream(method.getDeclaredAnnotations())
                .anyMatch(a -> a.annotationType() == PostConstruct.class)
        )
        .toList();
    if (postMethods.isEmpty()) {
      return;
    }
    if (postMethods.size() > 1) {
      throw new RuntimeException("Cannot have more than 1 post initiate method");
    }
    try {
      var method = postMethods.get(0);
      method.setAccessible(true);
      method.invoke(instance);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}