package com.aol.cyclops.data.collections.extensions.standard;

import java.util.Collection;

import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.FluentSequenceX;

public interface MutableSequenceX<T> extends CollectionX<T>, FluentSequenceX<T> {
    public MutableSequenceX<T> plus(T e);

    public MutableSequenceX<T> plusAll(Collection<? extends T> list);

    public MutableSequenceX<T> with(int i, T e);

    public MutableSequenceX<T> plus(int i, T e);

    public MutableSequenceX<T> plusAll(int i, Collection<? extends T> list);

    public MutableSequenceX<T> minus(Object e);

    public MutableSequenceX<T> minusAll(Collection<?> list);

    public MutableSequenceX<T> minus(int i);

    public MutableSequenceX<T> subList(int start, int end);
}
