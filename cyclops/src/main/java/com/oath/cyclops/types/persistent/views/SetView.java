package com.oath.cyclops.types.persistent.views;

import com.oath.cyclops.types.persistent.PersistentIndexed;
import com.oath.cyclops.types.persistent.PersistentSet;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public interface SetView<T> extends Set<T>
{
  @Override
  @Deprecated
  default boolean removeIf(Predicate<? super T> filter) {
    return false;
  }

  @Override
  @Deprecated
  boolean add(T t);

  @Override
  @Deprecated
  boolean remove(Object o);

  @Override
  @Deprecated
  boolean addAll(Collection<? extends T> c);



  @Override
  @Deprecated
  boolean removeAll(Collection<?> c);

  @Override
  @Deprecated
  boolean retainAll(Collection<?> c);



  @Override
  void clear();

  @AllArgsConstructor
  public static class Impl<T> extends AbstractSet<T> implements SetView<T> {
    private final PersistentSet<T> host;

    @Override
    public int size() {
      return host.size();
    }

    @Override
    public boolean isEmpty() {
      return host.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
      return host.containsValue((T)o);
    }

    @Override
    public Iterator<T> iterator() {
      return host.iterator();
    }

    @Override
    public Object[] toArray() {
      return host.stream().toArray();
    }



    @Override
    public boolean add(T t) {
      return false;
    }

    @Override
    public boolean remove(Object o) {
      return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {

      for(Object n : c){
        if(!contains(n))
          return false;
      }
      return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
      return false;
    }



    @Override
    public boolean removeAll(Collection<?> c) {
      return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      return false;
    }

    @Override
    public void clear() {

    }


  }
}
