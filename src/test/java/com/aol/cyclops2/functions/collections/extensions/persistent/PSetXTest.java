package com.aol.cyclops2.functions.collections.extensions.persistent;

import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;
import cyclops.collections.immutable.PersistentSetX;
import cyclops.collections.mutable.ListX;
import com.aol.cyclops2.functions.collections.extensions.AbstractCollectionXTest;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class PSetXTest extends AbstractCollectionXTest {

    @Override
    public <T> FluentCollectionX<T> of(T... values) {
        return PersistentSetX.of(values);
    }

    @Test
    public void cycle(){
        ListX.of(1).stream().cycle(2).forEach(System.out::println);
        Iterator<Integer> it = ListX.of(1).stream().cycle(2).iterator();
        while(it.hasNext()){
            System.out.println("Next " + it.next());
        }
      //  System.out.println(of(1).cycle(2));
    //    assertThat(of(1,2,3).cycle(2).listX(),equalTo(ListX.of(3,2,1,3,2,1)));
    }
    @Test
    public void onEmptySwitch() {
        assertThat(PersistentSetX.empty()
                        .onEmptySwitch(() -> PersistentSetX.of(1, 2, 3)),
                   equalTo(PersistentSetX.of(1, 2, 3)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.function.collections.extensions.AbstractCollectionXTest#
     * empty()
     */
    @Override
    public <T> FluentCollectionX<T> empty() {
        return PersistentSetX.empty();
    }

    @Test
    @Override
    public void forEach2() {

        assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), (a, b) -> a + b)
                              .toList()
                              .size(),
                   equalTo(12));
    }
    @Test
    public void coflatMap(){
       assertThat(PersistentSetX.of(1,2,3)
                   .coflatMap(s->s.sumInt(i->i))
                   .single(),equalTo(6));
        
    }
    @Override
    public FluentCollectionX<Integer> range(int start, int end) {
        return PersistentSetX.range(start, end);
    }

    @Override
    public FluentCollectionX<Long> rangeLong(long start, long end) {
        return PersistentSetX.rangeLong(start, end);
    }

    @Override
    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return PersistentSetX.iterate(times, seed, fn);
    }

    @Override
    public <T> FluentCollectionX<T> generate(int times, Supplier<T> fn) {
        return PersistentSetX.generate(times, fn);
    }

    @Override
    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return PersistentSetX.unfold(seed, unfolder);
    }

}
