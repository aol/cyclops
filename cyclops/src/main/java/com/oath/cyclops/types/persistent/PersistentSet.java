package com.oath.cyclops.types.persistent;

import com.oath.cyclops.types.persistent.views.SetView;
import cyclops.reactive.ReactiveSeq;

public interface PersistentSet<T>  extends PersistentCollection<T>{

    PersistentSet<T> plus(T e);
    PersistentSet<T> plusAll(Iterable<? extends T> list);

    PersistentSet<T> removeValue(T e);

    PersistentSet<T> removeAll(Iterable<? extends T> list);

    default ReactiveSeq<T> stream(){
        return ReactiveSeq.fromIterable(this);
    }

  default SetView<T> setView(){
    return new SetView.Impl<>(this);
  }
}
