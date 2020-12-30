package com.oath.cyclops.types.persistent.views;

import com.oath.cyclops.types.persistent.PersistentMap;
import lombok.AllArgsConstructor;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface MapView<K,V> extends Map<K,V>
{

  @Override
  @Deprecated
   default V putIfAbsent(K key, V value){
      throw new UnsupportedOperationException();
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
      throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  default V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction){
      throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  default  V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction){
      throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  default V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction){
      throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
   default V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction){
      throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  default V put(K key, V value){
      throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  default V remove(Object key){
      throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  default void clear(){
      throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  default void putAll(Map<? extends K, ? extends V> m){
      throw new UnsupportedOperationException();
  }

  @Override
  @Deprecated
  default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function){
      throw new UnsupportedOperationException();
  }

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
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        throw new UnsupportedOperationException();
    }
    @Override
    @Deprecated
    public V put(K key, V value){
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public V remove(Object key){
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void clear(){
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void putAll(Map<? extends K, ? extends V> m){
        throw new UnsupportedOperationException();
    }


  }
}
