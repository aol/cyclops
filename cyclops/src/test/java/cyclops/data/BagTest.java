package cyclops.data;

import com.oath.cyclops.types.traversable.IterableX;

import cyclops.data.*;
import cyclops.companion.Semigroups;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.basetests.BaseImmutableSetTest;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

public class BagTest extends BaseImmutableSetTest {

    @Test
    public void testBag(){
        Bag<Integer> bag1 = Bag.of(1,2,3,4,10,1,1,2);
        Bag<Integer> bag2 = Bag.fromStream(Stream.of(1,2,3,4,10,1,1,2));

        assertThat(bag1,equalTo(bag2));
    }
    @Test
    public void minus(){
        Bag<Integer> bag1 = Bag.of(1,2,3,4,10,1,1,2);
        assertThat(bag1.removeValue(1).instances(1),equalTo(2));

    }
    @Test
    @Override
    public void insertAtIterable(){
        List<String> result = 	of(1,2,3).insertAt(1,of(100,200,300))
            .map(it ->it+"!!").collect(Collectors.toList());

       assertThat(result, hasItems("1!!","100!!","200!!","300!!","2!!","3!!"));
    }

    @Test
    public void stream(){
        List<Integer> l = Bag.of(1, 2, 3, 4, 10, 1, 1, 2).stream().toList();
        assertThat(l.size(),equalTo(8));
    }

    @Override
    protected <T> ImmutableSet<T> fromStream(Stream<T> s) {
        return Bag.fromStream(s);
    }

    @Override
    public <T> Bag<T> empty() {
        return Bag.empty();
    }

    @Override
    public <T> ImmutableSet<T> of(T... values) {
        return Bag.of(values);
    }

    @Override
    public IterableX<Integer> range(int start, int end) {
        return Bag.fromStream(ReactiveSeq.range(start,end));
    }

    @Override
    public IterableX<Long> rangeLong(long start, long end) {
        return Bag.fromStream(ReactiveSeq.rangeLong(start,end));
    }

    @Override
    public <T> IterableX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return Bag.fromStream(ReactiveSeq.iterate(seed,fn).take(times));
    }

    @Override
    public <T> IterableX<T> generate(int times, Supplier<T> fn) {
        return Bag.fromStream(ReactiveSeq.generate(fn).take(times));
    }

    @Override
    public <U, T> IterableX<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return Bag.fromStream(ReactiveSeq.unfold(seed,unfolder));
    }

    @Test
    public void duplicates(){

        assertThat(of(1,2,1,2,1,2).size(),equalTo(6));
    }
    @Test
    public void forEach2() {

        assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), (a , b) -> a + b).toList().size(),
                equalTo(30));
    }

    @Test
    public void testCycleNoOrd() {
        assertEquals(asList(1, 2, 1, 2, 1, 2).size(),of(1, 2).cycle(3).toList().size());
        assertEquals(asList(1, 2, 3, 1, 2, 3).size(), of(1, 2, 3).cycle(2).toList().size());
    }
    @Test
    public void testCycleTimesNoOrd() {
        assertEquals(asList(1, 2, 1, 2, 1, 2).size(),of(1, 2).cycle(3).toList().size());
    }

    int count =0;
    @Test
    public void testCycleWhileNoOrd() {
        count =0;
        assertEquals(asList(1, 2,3, 1, 2,3).size(),of(1, 2, 3).cycleWhile(next->count++<6).toList().size());

    }
    @Test
    public void testCycleUntilNoOrd() {
        count =0;
        assertEquals(asList(1, 2,3, 1, 2,3).size(),of(1, 2, 3).cycleUntil(next->count++==6).toList().size());

    }

    @Test
    public void removeFirst(){
        IterableX<Integer> vec = this.of(1,2,2,2,3);

        assertThat(vec.removeFirst(i->i==2),equalTo(of(1,2,2,3)));
    }
    @Test
    public void combine(){
        assertThat(of(1,1,2,3)
                .combine((a, b)->a.equals(b), Semigroups.intSum)
                .toSet(),equalTo(new java.util.HashSet<>(Arrays.asList(3,4))));

    }
    @Test
    public void testCycleUntil() {
        count =0;
        System.out.println("List " + of(1, 2, 3).peek(System.out::println).cycleUntil(next->count++==6).toList());
        count =0;
        assertEquals(6,of(1, 2, 3).cycleUntil(next->count++==6).toList().size());

    }
    @Test
    public void testCycleWhile() {
        count =0;
        assertEquals(6,of(1, 2, 3).cycleWhile(next->count++<6).toList().size());

    }
    @Test
    public void testCycleTimesNoOrder() {
        assertEquals(6,of(1, 2).cycle(3).toList().size());
    }
    @Test
    public void combineNoOrd(){
        assertThat(of(1,1,2,3)
                .combine((a, b)->a.equals(b), Semigroups.intSum)
                .toList(),equalTo(Arrays.asList(3,4)));
    }
    @Test
    public void batchBySizeSet(){
        System.out.println("List = " + of(1,1,1,1,1,1).grouped(3,()->TreeSet.empty()).toList());
        assertThat(of(1,1,1,1,1,1).grouped(3,()->TreeSet.empty()).toList().get(0).size(),is(1));
        assertThat(of(1,1,1,1,1,1).grouped(3,()->TreeSet.empty()).toList().size(),is(2));
    }
    @Test
    public void testCycleNoOrder() {
        assertEquals(6,of(1, 2).cycle(3).toList().size());
        assertEquals(6, of(1, 2, 3).cycle(2).toList().size());
    }
    @Test
    public void lastIndexOf(){

      assertThat(empty().lastIndexOf(e->true),equalTo(Maybe.nothing()));
      assertThat(of(1).lastIndexOf(e->true),equalTo(Maybe.just(0l)));
      assertThat(of(1).lastIndexOf(e->false),equalTo(Maybe.nothing()));
      assertThat(of(1,2,3).lastIndexOf(e-> Objects.equals(2,e)),equalTo(Maybe.just(1l)));
      assertThat(of(1,2,3,2).lastIndexOf(e->Objects.equals(2,e)),equalTo(Maybe.just(2l)));
    }
    @Test
    public void lastIndexOfSlize(){
        assertThat(empty().lastIndexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.nothing()));
        assertThat(of(1,2,3).lastIndexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.just(0l)));
        assertThat(of(1).lastIndexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.nothing()));
        assertThat(of(0,1,2,3,4,5,6,1,2,3).lastIndexOfSlice(Seq.of(1,2,3)),equalTo(Maybe.nothing()));
    }
}
