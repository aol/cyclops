package com.aol.cyclops2.data.collections.extensions.lazy.immutable;

import com.aol.cyclops2.types.persistent.PersistentList;

import java.util.Iterator;


public interface FoldToList<T> {
    public PersistentList<T> from(final Iterator<T> i, int depth);
}
