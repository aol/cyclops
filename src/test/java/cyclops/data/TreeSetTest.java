package cyclops.data;

import cyclops.collections.tuple.Tuple2;
import cyclops.data.basetests.BaseImmutableSortedSetTest;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class TreeSetTest extends BaseImmutableSortedSetTest{
    @Override
    protected <T> ImmutableSortedSet<T> fromStream(Stream<T> s) {
        return TreeSet.fromStream(s,Comparators.naturalOrderIdentityComparator());
    }

    @Override
    public <T> ImmutableSortedSet<T> empty() {
        return TreeSet.empty(Comparators.naturalOrderIdentityComparator());
    }

    @Override
    public <T> ImmutableSortedSet<T> of(T... values) {
        return TreeSet.of(Comparators.naturalOrderIdentityComparator(),values);
    }

    @Override
    public ImmutableSortedSet<Integer> range(int start, int end) {
        return TreeSet.range(start,end);
    }

    @Override
    public ImmutableSortedSet<Long> rangeLong(long start, long end) {
        return TreeSet.rangeLong(start,end);
    }

    @Override
    public <T> ImmutableSortedSet<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return TreeSet.iterate(seed,fn,times);
    }

    @Override
    public <T> ImmutableSortedSet<T> generate(int times, Supplier<T> fn) {
        return TreeSet.generate(fn,times);
    }

    @Override
    public <U, T> ImmutableSortedSet<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
        return TreeSet.unfold(seed,unfolder);
    }
}
