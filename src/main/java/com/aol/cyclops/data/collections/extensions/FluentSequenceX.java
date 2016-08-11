package com.aol.cyclops.data.collections.extensions;

import java.util.Collection;

public interface FluentSequenceX<T> extends FluentCollectionX<T> {

    public FluentSequenceX<T> plus(T e);

    public FluentSequenceX<T> plusAll(Collection<? extends T> list);

    public FluentSequenceX<T> with(int i, T e);

    public FluentSequenceX<T> plus(int i, T e);

    public FluentSequenceX<T> plusAll(int i, Collection<? extends T> list);

    public FluentSequenceX<T> minus(Object e);

    public FluentSequenceX<T> minusAll(Collection<?> list);

    public FluentSequenceX<T> minus(int i);

    public FluentSequenceX<T> subList(int start, int end);

}
