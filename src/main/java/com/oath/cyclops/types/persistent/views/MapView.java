package com.oath.cyclops.types.persistent.views;

import com.oath.cyclops.types.persistent.PersistentIndexed;
import com.oath.cyclops.types.persistent.PersistentMap;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public interface MapView<K,V> extends Map<K,V>
{

  @Override
  @Deprecated
   default V putIfAbsent(K key, V value){
    return value;
  }

  @Override
  @Deprecated
  boolean remove(Object key, Object value) ;

  @Override
  @Deprecated
  boolean replace(K key, V oldValue, V newValue);

  @Override
  @Deprecated
  default V replace(K key, V value){
    return value;
  }

  @Override
  @Deprecated
  default V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction){
    return mappingFunction.apply(key);
  }

  @Override
  @Deprecated
  default  V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction){
    return remappingFunction.apply(key,get(key));
  }

  @Override
  @Deprecated
  default V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction){
    return remappingFunction.apply(key,get(key));
  }

  @Override
  @Deprecated
   default V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction){
    return remappingFunction.apply(get(key),value);
  }

  @Override
  @Deprecated
  V put(K key, V value);

  @Override
  @Deprecated
  V remove(Object key);

  @Override
  @Deprecated
  void clear();

  @Override
  @Deprecated
  void putAll(Map<? extends K, ? extends V> m);

  @Override
  @Deprecated
  void replaceAll(BiFunction<? super K, ? super V, ? extends V> function);

  @AllArgsConstructor
  public static class Impl<K,V> extends AbstractMap<K,V> implements MapView<K,V> {
    private final PersistentMap<K,V> host;

    @Override
    public int size() {
      return host.size();
    }

    @Override
    public boolean isEmpty() {
      return host.isEmpty();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
      return host.stream()
                 .map(t2->new AbstractMap.SimpleEntry<>(t2._1(),t2._2()))
                 .collect(Collectors.toSet());
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
      return host.getOrElse((K)key,defaultValue);
    }


    @Override
    public boolean remove(Object key, Object value) {
      return false;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
      return false;
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {

    }
  }
}
