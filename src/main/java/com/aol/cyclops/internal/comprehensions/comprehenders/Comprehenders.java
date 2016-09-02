package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.AbstractMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.pcollections.PStack;

import com.aol.cyclops.Reducers;
import com.aol.cyclops.types.extensability.Comprehender;

/**
 * Registered Comprehenders
 * 
 * @author johnmcclean
 *
 */
public class Comprehenders {

    private final static PStack<Map.Entry<Class, Comprehender>> comprehenders;

    static {
        ServiceLoader<Comprehender> loader = ServiceLoader.load(Comprehender.class);

        comprehenders = Reducers.<Map.Entry<Class, Comprehender>> toPStack()
                                .mapReduce((Stream) Seq.seq(loader.iterator())
                                                       .sorted((a, b) -> b.priority() - a.priority())
                                                       .map(comp -> new AbstractMap.SimpleEntry(
                                                                                                comp.getTargetClass(), comp)));

    }

    /**
     * @return Registered Comprehenders
     */
    public PStack<Map.Entry<Class, Comprehender>> getRegisteredComprehenders() {
        return comprehenders;
    }

}
