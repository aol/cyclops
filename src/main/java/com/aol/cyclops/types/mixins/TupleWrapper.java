package com.aol.cyclops.types.mixins;

import java.util.List;
import java.util.stream.Collectors;

import com.aol.cyclops.internal.invokedynamic.ReflectionCache;
import com.aol.cyclops.util.ExceptionSoftener;
@Deprecated //internal interface - move in 2.0.0
public interface TupleWrapper {

    public Object getInstance();

    @SuppressWarnings("unchecked")
    default List<Object> values() {

        try {
            return ReflectionCache.getFields(getInstance().getClass())
                                  .stream()
                                  .map(f -> {
                                      try {

                                          return f.get(getInstance());
                                      } catch (final Exception e) {
                                          throw ExceptionSoftener.throwSoftenedException(e);
                                      }
                                  })
                                  .collect(Collectors.toList());
        } catch (final Exception e) {
            throw ExceptionSoftener.throwSoftenedException(e);

        }
    }
}