package com.oath.cyclops.types.persistent;


import com.oath.cyclops.types.persistent.views.SortedSetView;
import cyclops.control.Option;

import java.util.Comparator;

public interface PersistentSortedSet<T> extends PersistentSet<T> {
    public PersistentSortedSet<T> plus(T e);

    public PersistentSortedSet<T> plusAll(Iterable<? extends T> list);

    public PersistentSortedSet<T> removeValue(T e);

    public PersistentSortedSet<T> removeAll(Iterable<? extends T> list);

    Option<T> get(int index);

  Comparator<? super T> comparator();
  default SortedSetView<T> sortedSetView(){
    return new SortedSetView.Impl<>(this);
  }
}
