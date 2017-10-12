package cyclops.data;

import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.data.basetests.BaseImmutableSetTest;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class HashSetTest extends BaseImmutableSetTest{
    @Override
    protected <T> ImmutableSet<T> fromStream(Stream<T> s) {
        return HashSet.fromStream(s);
    }

    @Override
    public <T> ImmutableSet<T> empty() {
        return HashSet.empty();
    }

    @Override
    public <T> ImmutableSet<T> of(T... values) {
        return HashSet.of(values);
    }

    @Override
    public ImmutableSet<Integer> range(int start, int end) {
        return HashSet.range(start,end);
    }

    @Override
    public ImmutableSet<Long> rangeLong(long start, long end) {
        return HashSet.rangeLong(start,end);
    }

    @Override
    public <T> ImmutableSet<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return HashSet.iterate(seed,fn,times);
    }

    @Override
    public <T> ImmutableSet<T> generate(int times, Supplier<T> fn) {
        return HashSet.generate(fn,times);
    }

    @Override
    public <U, T> ImmutableSet<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return HashSet.unfold(seed,unfolder);
    }
}
