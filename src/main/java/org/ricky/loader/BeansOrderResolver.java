package org.ricky.loader;

import java.util.*;

public class BeansOrderResolver {

  private Map<String, List<String>> dependents = new HashMap<>();

  public BeansOrderResolver(Map<String, List<String>> dependents) {
    this.dependents = dependents;
  }

  public Pair<List<String>, List<List<String>>> resolve() {
    Map<String, List<String>> children = new HashMap<>();
    Set<String> services = new HashSet<>();

    for (Map.Entry<String, List<String>> entry : dependents.entrySet()) {
      String key = entry.getKey();
      List<String> value = entry.getValue();
      services.add(key);
      for (String dependent : value) {
        services.add(dependent);
        children.computeIfAbsent(dependent, k -> new ArrayList<>()).add(key);
      }
    }

    Queue<String> queue = new LinkedList<>();
    Map<String, Boolean> initialized = new HashMap<>();
    List<String> order = new ArrayList<>();

    for (String service : services) {
      if (!dependents.containsKey(service) || dependents.get(service).isEmpty()) {
        queue.add(service);
        initialized.put(service, true);
      }
    }

    while (!queue.isEmpty()) {
      String currentService = queue.poll();
      initialized.put(currentService, true);
      order.add(currentService);

      if (children.containsKey(currentService)) {
        for (String child : children.get(currentService)) {
          dependents.get(child).remove(currentService);
          if (dependents.get(child).isEmpty() && !initialized.getOrDefault(child, false)) {
            queue.add(child);
            initialized.put(child, true);
          }
        }
      }
    }

    List<List<String>> circulars = new ArrayList<>();

    for (String service : services) {
      if (!initialized.getOrDefault(service, false)) {
        List<List<String>> circularsFromService = findCirculars(service);
        circulars.addAll(circularsFromService);
      }
    }

    return new Pair<>(order, circulars);
  }

  private List<List<String>> findCirculars(String source) {
    List<List<String>> ans = new ArrayList<>();
    Queue<List<String>> queue = new LinkedList<>();
    List<String> pathSoFar = new ArrayList<>();
    pathSoFar.add(source);
    queue.add(pathSoFar);

    while (!queue.isEmpty()) {
      List<String> u = queue.poll();
      String last = u.get(u.size() - 1);
      if (dependents.containsKey(last)) {
        for (String dependent : dependents.get(last)) {
          List<String> v = new ArrayList<>(u);
          v.add(dependent);
          if (u.contains(dependent)) {
            ans.add(v);
          } else {
            queue.add(v);
          }
        }
      }
    }

    return ans;
  }

}

record Pair<K, V>(K first, V second) {
  public K getFirst() {
    return first;
  }

  public V getSecond() {
    return second;
  }
}