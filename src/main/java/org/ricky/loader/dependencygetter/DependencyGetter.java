package org.ricky.loader.dependencygetter;

import java.util.List;

public interface DependencyGetter {
  List<String> get(final Class<?> clazz, List<String> beanNames);
}
