package com.oath.cyclops.data.collections.extensions.lazy.immutable;

import com.oath.cyclops.types.persistent.PersistentList;

import java.util.Iterator;


public interface FoldToList<T> {
    public PersistentList<T> from(final Iterator<T> i, int depth);
}
