package cyclops.data;


import cyclops.data.tuple.Tuple2;
import cyclops.data.basetests.BaseImmutableListTest;
import org.junit.Test;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class LazySeqTest extends BaseImmutableListTest {

    @Override
    protected <T> LazySeq<T> fromStream(Stream<T> s) {
        return LazySeq.fromStream(s);
    }

    @Override
    public <T> LazySeq<T> empty() {
        return LazySeq.empty();
    }

    @Override
    public <T> LazySeq<T> of(T... values) {
        return LazySeq.of(values);
    }

    @Override
    public LazySeq<Integer> range(int start, int end) {
        return LazySeq.range(start,end);
    }

    @Override
    public LazySeq<Long> rangeLong(long start, long end) {
        return LazySeq.rangeLong(start,end);
    }

    @Override
    public <T> ImmutableList<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return LazySeq.iterate(seed,fn,times);
    }

    @Override
    public <T> LazySeq<T> generate(int times, Supplier<T> fn) {
        return LazySeq.generate(fn,times);
    }

    @Override
    public <U, T> Seq<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return Seq.unfold(seed,unfolder);
    }
    @Test
    public void prependAllTest(){
        assertThat(LazySeq.of(1,2,3).prependAll(LazySeq.of(4,5,6)),equalTo(LazySeq.of(4,5,6,1,2,3)));
    }

    @Test
    public void scanRight(){
        LazySeq.of(1,2,3).scanRight(0,(a, b)->a+b).printOut();
    }





}
