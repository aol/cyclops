package cyclops.data;

import cyclops.control.Option;
import cyclops.data.basetests.BaseImmutableListTest;
import cyclops.data.tuple.Tuple2;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class NonEmptyListTest extends BaseImmutableListTest {
    @Override
    protected <T> Seq<T> fromStream(Stream<T> s) {
        return Seq.fromStream(s);
    }

    @Override
    public <T> Seq<T> empty() {
        return Seq.empty();
    }

    @Override
    public <T> ImmutableList<T> of(T... values) {
        if(values.length==0)
            return empty();
        if(values.length==1)
            return NonEmptyList.of(values[0]);
        return NonEmptyList.of(values[0], Arrays.copyOf(values,1));
    }

    @Override
    public Seq<Integer> range(int start, int end) {
        return Seq.range(start,end);
    }

    @Override
    public Seq<Long> rangeLong(long start, long end) {
        return Seq.rangeLong(start,end);
    }

    @Override
    public <T> ImmutableList<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return Seq.iterate(seed,fn,times);
    }

    @Override
    public <T> Seq<T> generate(int times, Supplier<T> fn) {
        return Seq.generate(fn,times);
    }

    @Override
    public <U, T> Seq<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return Seq.unfold(seed,unfolder);
    }
}
