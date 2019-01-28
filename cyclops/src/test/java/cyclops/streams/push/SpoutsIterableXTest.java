package cyclops.streams.push;

import com.oath.cyclops.types.traversable.IterableX;
import cyclops.control.Option;
import cyclops.data.basetests.AbstractIterableXTest;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class SpoutsIterableXTest extends AbstractIterableXTest {
    @Override
    public <T> IterableX<T> empty() {
        return Spouts.empty();
    }

    @Override
    public <T> IterableX<T> of(T... values) {
        return Spouts.of(values);
    }

    @Override
    public IterableX<Integer> range(int start, int end) {
        return Spouts.range(start,end);
    }

    @Override
    public IterableX<Long> rangeLong(long start, long end) {
        return Spouts.rangeLong(start,end);
    }

    @Override
    public <T> IterableX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return Spouts.iterate(seed,fn).take(times);
    }

    @Override
    public <T> IterableX<T> generate(int times, Supplier<T> fn) {
        return Spouts.generate(fn).take(times);
    }

    @Override
    public <U, T> IterableX<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return Spouts.unfold(seed,unfolder);
    }
}
