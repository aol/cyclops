package com.oath.cyclops.types.persistent.views;

import com.oath.cyclops.types.persistent.PersistentSet;
import com.oath.cyclops.types.persistent.PersistentSortedSet;
import cyclops.reactive.ReactiveSeq;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface SortedSetView<T> extends SortedSet<T>
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
  public static class Impl<T> extends AbstractSet<T> implements SortedSetView<T> {
    private final PersistentSortedSet<T> host;

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


    @Override
    public Comparator<? super T> comparator() {
      return host.comparator();
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
      ReactiveSeq<T> x = host.stream()
                             .dropWhile(e -> !Objects.equals(e, fromElement))
                              .takeWhile(e -> !Objects.equals(e, toElement));
      return x.collect(Collectors.toCollection(()->new TreeSet<>(comparator())));
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
      ReactiveSeq<T> x = host.stream()
        .takeWhile(e -> !Objects.equals(e, toElement));
      return x.collect(Collectors.toCollection(()->new TreeSet<>(comparator())));
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
      ReactiveSeq<T> x = host.stream()
        .dropWhile(e -> !Objects.equals(e, fromElement));
      return x.collect(Collectors.toCollection(()->new TreeSet<>(comparator())));
    }

    @Override
    public T first() {
      return host.get(0).orElse(null);
    }

    @Override
    public T last() {
      return host.get(host.size()-1).orElse(null);
    }
  }
}
