package com.aol.cyclops.control.transformers;

import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.monads.transformers.values.StreamableTValue;
import com.aol.cyclops.types.AbstractTraversableTest;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.util.stream.Streamable;


public class StreamableTValueTraversableTest extends AbstractTraversableTest {

    @Test
    public void streamable(){
        Streamable.of(1,2,3).zipStream(Stream.of(1,2,3)).printOut();
    }
    @Override
    public <T> Traversable<T> of(T... elements) {
        return StreamableTValue.fromValue(Maybe.just(Streamable.of(elements)));
    }

    @Override
    public <T> Traversable<T> empty() {
        return StreamableTValue.emptyOptional();
    }

}
