package cyclops.data;

import cyclops.companion.Comparators;
import cyclops.companion.Monoids;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.data.basetests.BaseImmutableSortedSetTest;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class TreeSetTest extends BaseImmutableSortedSetTest{
    @Override
    protected <T> ImmutableSortedSet<T> fromStream(Stream<T> s) {
        return TreeSet.fromStream(s, Comparators.naturalOrderIdentityComparator());
    }

    @Override
    public <T> ImmutableSortedSet<T> empty() {
        return TreeSet.empty(Comparators.naturalOrderIdentityComparator());
    }

    @Override
    public <T> ImmutableSortedSet<T> of(T... values) {
        return TreeSet.of(Comparators.naturalOrderIdentityComparator(),values);
    }
    @Test
    public void minusOneLarge(){
        assertThat(range(0,10_000).removeValue(1).size(), equalTo(9999));
        assertThat(range(0,10_000).append(1).removeValue(1).size(), equalTo(9999));
    }

    @Test
    @Override
    public void insertAtIterable(){
        List<String> result = 	of(1,2,3).insertAt(1,of(100,200,300))
            .map(it ->it+"!!").collect(Collectors.toList());

        MatcherAssert.assertThat(result, hasItems("1!!","100!!","200!!","300!!","2!!","3!!"));
    }
    @Test
    public void takeWhileDropWhile(){
        System.out.println(of(1,2,3,4,1,2,3,4).takeWhile(i->i<3));
        System.out.println(of(1,2,3,4,1,2,3,4).dropWhile(i->i<3));
    }
    @Test
    public void takeMultiple(){
        assertThat(of(1,2,3,4).take(2),equalTo(of(1,2)));
    }
    public <T> ImmutableSortedSet<T> of(Comparator<T> comp, T... values) {
        return TreeSet.of(comp,values);
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

    @Test
    public void testOfTypeNoOrd() {


        ImmutableSortedSet<Number> set = this.<Number>of(Comparators.identityComparator(),1, 10l, 2, 20l, 3);
        ImmutableSortedSet<Integer> setA = set.ofType(Integer.class);
        assertThat(setA.toList(),containsInAnyOrder(1, 2, 3));

    }

  @Test
  public void lastIndexOf(){

    MatcherAssert.assertThat(empty().lastIndexOf(e->true),equalTo(Maybe.nothing()));
    MatcherAssert.assertThat(of(1).lastIndexOf(e->true),equalTo(Maybe.just(0l)));
    MatcherAssert.assertThat(of(1).lastIndexOf(e->false),equalTo(Maybe.nothing()));
    MatcherAssert.assertThat(of(1,2,3).lastIndexOf(e-> Objects.equals(2,e)),equalTo(Maybe.just(1l)));
    MatcherAssert.assertThat(of(1,2,3,2).lastIndexOf(e->Objects.equals(2,e)),
      equalTo(of(1,2,3,2).indexOf(e->Objects.equals(2,e))));
  }
    @Test
    public void lastIndexOfSlize(){
        MatcherAssert.assertThat(empty().lastIndexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.nothing()));
        MatcherAssert.assertThat(of(1,2,3).lastIndexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.just(0l)));
        MatcherAssert.assertThat(of(1).lastIndexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.nothing()));
        MatcherAssert.assertThat(of(0,1,2,3,4,5,6,1,2,3).lastIndexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.just(1l)));
    }
}
