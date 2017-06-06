package com.aol.cyclops2.data.collections.extensions.lazy.immutable;

import org.pcollections.PStack;

import java.util.Iterator;


public interface FoldToList<T> {
    public PStack<T> from(final Iterator<T> i, int depth);
}
