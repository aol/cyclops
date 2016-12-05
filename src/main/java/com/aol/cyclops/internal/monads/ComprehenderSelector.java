package com.aol.cyclops.internal.monads;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.aol.cyclops.internal.comprehensions.comprehenders.Comprehenders;
import com.aol.cyclops.internal.comprehensions.comprehenders.InvokeDynamicComprehender;
import com.aol.cyclops.types.extensability.Comprehender;

public class ComprehenderSelector {

    private final Comprehenders comprehenders = new Comprehenders();
    @SuppressWarnings("rawtypes")
    private final ConcurrentMap<Class, Comprehender> cache = new ConcurrentHashMap<>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Comprehender selectComprehender(final Class structure) {

        return cache.computeIfAbsent(structure, st -> comprehenders.getRegisteredComprehenders()
                                                                   .stream()
                                                                   .filter(e -> e.getKey()
                                                                                 .isAssignableFrom(structure))
                                                                   .map(e -> e.getValue())
                                                                   .findFirst()
                                                                   .orElse(new InvokeDynamicComprehender(
                                                                                                         Optional.of(structure))));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" }) 
    public Comprehender selectComprehender(final Object structure) {

        return cache.computeIfAbsent(structure.getClass(), st -> comprehenders.getRegisteredComprehenders()
                                                                              .stream()
                                                                              .filter(e -> e.getKey()
                                                                                            .isAssignableFrom(structure.getClass()))
                                                                              .map(e -> e.getValue())
                                                                              .findFirst()
                                                                              .orElse(new InvokeDynamicComprehender(
                                                                                                                    Optional.ofNullable(structure)
                                                                                                                            .map(Object::getClass))));

    }

}
