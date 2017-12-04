package com.oath.cyclops.internal.stream.spliterators;

import org.junit.Test;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 31/12/2016.
 */
public class StreamFlatMappingSpliteratorTest {
    @Test
    public void tryAdvance() throws Exception {
        StreamFlatMappingSpliterator<String, String> split = new StreamFlatMappingSpliterator<>(new DummySpliterator<String>(-1l, 1), s -> Stream.of(s));

        boolean called[] = {false};
        split.tryAdvance(e->called[0]=true);
        assertTrue(called[0]);
        called[0] = false;
        split.tryAdvance(e->called[0]=true);
        assertTrue(called[0]);
        called[0] = false;
        split.tryAdvance(e->called[0]=true);
        assertTrue(called[0]);
        called[0] = false;
        split.tryAdvance(e->called[0]=true);
        assertTrue(called[0]);


    }

    static class DummySpliterator<T> extends Spliterators.AbstractSpliterator<T>{

        public DummySpliterator(long est, int additionalCharacteristics) {
            super(est, additionalCharacteristics);
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            action.accept((T)"hello");
            return true;
        }
    }
}
