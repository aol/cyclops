package com.aol.cyclops.control.transformers.seq;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.cyclops.control.monads.transformers.StreamableT;
import com.aol.cyclops.control.monads.transformers.values.StreamableTValue;
import com.aol.cyclops.types.AbstractTraversableTest;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.util.stream.Streamable;


public class StreamableTSeqTraversableTest extends AbstractTraversableTest {

    @Test
    public void streamable(){
        Streamable.of(1,2,3).zip(Stream.of(1,2,3)).printOut();
    }
    @Override
    public <T> Traversable<T> of(T... elements) {
        return StreamableT.fromIterable(Arrays.asList(Streamable.of(elements)));
    }

    @Override
    public <T> Traversable<T> empty() {
        return StreamableT.emptyStreamable();
    }

}
