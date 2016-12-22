package com.aol.cyclops.functions.collections.extensions.standard;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.FluentCollectionX;
import cyclops.collections.ListX;
import cyclops.collections.QueueX;
import com.aol.cyclops.functions.collections.extensions.CollectionXTestsWithNulls;

public class QueueXTest extends CollectionXTestsWithNulls {

    @Override
    public <T> FluentCollectionX<T> of(T... values) {
        return QueueX.of(values);
    }
    @Test
    public void onEmptySwitch(){
        assertThat(QueueX.empty().onEmptySwitch(()->QueueX.of(1,2,3)).toList(),equalTo(ListX.of(1,2,3)));
    }
    @Test
    public void coflatMap(){
       assertThat(QueueX.of(1,2,3)
                   .coflatMap(s->s.sumInt(i->i))
                   .single(),equalTo(6));
        
    }
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops.function.collections.extensions.AbstractCollectionXTest#
     * empty()
     */
    @Override
    public <T> FluentCollectionX<T> empty() {
        return QueueX.empty();
    }

    @Override
    public FluentCollectionX<Integer> range(int start, int end) {
        return QueueX.range(start, end);
    }

    @Override
    public FluentCollectionX<Long> rangeLong(long start, long end) {
        return QueueX.rangeLong(start, end);
    }

    @Override
    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return QueueX.iterate(times, seed, fn);
    }

    @Override
    public <T> FluentCollectionX<T> generate(int times, Supplier<T> fn) {
        return QueueX.generate(times, fn);
    }

    @Override
    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return QueueX.unfold(seed, unfolder);
    }

}
