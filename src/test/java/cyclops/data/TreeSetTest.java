package cyclops.data;

import com.aol.cyclops2.types.stream.HeadAndTail;
import com.aol.cyclops2.types.traversable.IterableX;
import cyclops.companion.Monoids;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple2;
import cyclops.data.basetests.BaseImmutableSortedSetTest;
import cyclops.reactive.ReactiveSeq;
import org.junit.Ignore;
import org.junit.Test;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
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

    public <T> ImmutableSortedSet<T> of(Comparator<T> comp, T... values) {
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


        ImmutableSortedSet<Number> set = this.<Number>of(Comparators.identityComparator(),1, 10l, 2, 20l, 3);
        ImmutableSortedSet<Integer> setA = set.ofType(Integer.class);
        assertThat(setA.toListX(),containsInAnyOrder(1, 2, 3));

    }
    @Test
    public void headTailReplayNoOrd() {

        IterableX<String> helloWorld = of("hello", "world", "last");
        HeadAndTail<String> headAndTail = helloWorld.headAndTail();
        String head = headAndTail.head();
        assertThat(head, isOneOf("hello","world","last"));

      

    }

}
