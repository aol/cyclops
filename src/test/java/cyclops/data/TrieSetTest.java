package cyclops.data;

import cyclops.collections.tuple.Tuple2;
import cyclops.data.basetests.BaseImmutableSetTest;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class TrieSetTest extends BaseImmutableSetTest{
    @Override
    protected <T> ImmutableSet<T> fromStream(Stream<T> s) {
        return TrieSet.fromStream(s);
    }

    @Override
    public <T> ImmutableSet<T> empty() {
        return TrieSet.empty();
    }

    @Override
    public <T> ImmutableSet<T> of(T... values) {
        return TrieSet.of(values);
    }

    @Override
    public ImmutableSet<Integer> range(int start, int end) {
        return TrieSet.range(start,end);
    }

    @Override
    public ImmutableSet<Long> rangeLong(long start, long end) {
        return TrieSet.rangeLong(start,end);
    }

    @Override
    public <T> ImmutableSet<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return TrieSet.iterate(seed,fn,times);
    }

    @Override
    public <T> ImmutableSet<T> generate(int times, Supplier<T> fn) {
        return TrieSet.generate(fn,times);
    }

    @Override
    public <U, T> ImmutableSet<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return TrieSet.unfold(seed,unfolder);
    }
}
