package com.aol.cyclops.functions.collections.extensions.standard;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops.data.collections.extensions.standard.SortedSetX;
import com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest;

public class SortedSetXTest extends AbstractCollectionXTest {

    @Override
    public <T> FluentCollectionX<T> of(T... values) {
        return SortedSetX.of(values);
    }

    @Test
    public void onEmptySwitch() {
        assertThat(SortedSetX.empty()
                             .onEmptySwitch(() -> SortedSetX.of(1, 2, 3)),
                   equalTo(SortedSetX.of(1, 2, 3)));
    }

    public void coflatMap(){
       assertThat(SortedSetX.of(1,2,3)
                   .coflatMap(s->s.sumInt(i->i))
                   .single(),equalTo(6));
        
    }
   

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#
     * empty()
     */
    @Override
    public <T> FluentCollectionX<T> empty() {
        return SortedSetX.empty();
    }

    @Test
    @Override
    public void forEach2() {

        assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), (a, b) -> a + b)
                              .toList()
                              .size(),
                   equalTo(12));
    }

    @Override
    public FluentCollectionX<Integer> range(int start, int end) {
        return SortedSetX.range(start, end);
    }

    @Override
    public FluentCollectionX<Long> rangeLong(long start, long end) {
        return SortedSetX.rangeLong(start, end);
    }

    @Override
    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return SortedSetX.iterate(times, seed, fn);
    }

    @Override
    public <T> FluentCollectionX<T> generate(int times, Supplier<T> fn) {
        return SortedSetX.generate(times, fn);
    }

    @Override
    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return SortedSetX.unfold(seed, unfolder);
    }
}
