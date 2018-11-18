package com.oath.cyclops.types.persistent;

import com.oath.cyclops.types.persistent.views.ListView;

public interface PersistentList<T> extends PersistentIndexed<T> {
    PersistentList<T> plus(T e);


    PersistentList<T> plusAll(Iterable<? extends T> list);


    PersistentList<T> updateAt(int i, T e);


    PersistentList<T> insertAt(int i, T e);


    PersistentList<T> insertAt(int i, Iterable<? extends T> list);


    PersistentList<T> removeValue(T e);


    PersistentList<T> removeAll(Iterable<? extends T> list);


    PersistentList<T> removeAt(long i);

    default ListView<T> listView(){
    return new ListView.Impl<>(this);
  }
}
