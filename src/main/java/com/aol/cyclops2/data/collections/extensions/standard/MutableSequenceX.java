package com.aol.cyclops2.data.collections.extensions.standard;

import java.util.Collection;

import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.data.collections.extensions.FluentSequenceX;

/**
 * @author johnmcclean
 *
 * @param <T> type of data held in this Collection
 */
public interface MutableSequenceX<T> extends CollectionX<T>, FluentSequenceX<T> {
    @Override
    public MutableSequenceX<T> plus(T e);

    @Override
    public MutableSequenceX<T> plusAll(Collection<? extends T> list);

    @Override
    public MutableSequenceX<T> with(int i, T e);

    @Override
    public MutableSequenceX<T> plus(int i, T e);

    @Override
    public MutableSequenceX<T> plusAll(int i, Collection<? extends T> list);

    @Override
    public MutableSequenceX<T> minus(Object e);

    @Override
    public MutableSequenceX<T> minusAll(Collection<?> list);

    @Override
    public MutableSequenceX<T> minus(int i);

    @Override
    public MutableSequenceX<T> subList(int start, int end);
}
