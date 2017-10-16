package cyclops.data;

import com.aol.cyclops2.types.traversable.IterableX;
import cyclops.companion.Monoids;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.data.basetests.BaseImmutableSortedSetTest;
import cyclops.reactive.ReactiveSeq;
import org.junit.Ignore;
import org.junit.Test;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

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
    public <U, T> ImmutableSortedSet<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return TreeSet.unfold(seed,unfolder);
    }
    @Test @Ignore
    public void printTree(){
       System.out.println(TreeSet.range(0,10_000).printTree());
    }

    @Test
    public void foldX(){
            System.out.println(TreeSet.range(0,10).seq());
          System.out.println(TreeSet.of(Comparators.naturalComparator(),0,1,2,3,4,5,6).seq());
          System.out.println(HashSet.range(0,10).seq());
                  System.out.println(TreeSet.range(0,10).seq().foldRight(Monoids.intSum));

         System.out.println(TreeSet.range(0,10).foldRight(Monoids.intSum));
    }

    public void testOfTypeNoOrd() {



        assertThat((((IterableX<Integer>)of(1, 10l, 2, 20l, 3).ofType(Integer.class))).toListX(),containsInAnyOrder(1, 2, 3));



    }

}
