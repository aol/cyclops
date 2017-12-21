package cyclops.data;

import com.oath.cyclops.types.traversable.IterableX;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.reactive.collections.mutable.SetX;
import cyclops.companion.Semigroups;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.data.basetests.BaseImmutableSetTest;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

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
        assertEquals(asList(1, 2, 1, 2, 1, 2).size(),of(1, 2).cycle(3).toListX().size());
        assertEquals(asList(1, 2, 3, 1, 2, 3).size(), of(1, 2, 3).cycle(2).toListX().size());
    }
    @Test
    public void testCycleTimesNoOrd() {
        assertEquals(asList(1, 2, 1, 2, 1, 2).size(),of(1, 2).cycle(3).toListX().size());
    }

    int count =0;
    @Test
    public void testCycleWhileNoOrd() {
        count =0;
        assertEquals(asList(1, 2,3, 1, 2,3).size(),of(1, 2, 3).cycleWhile(next->count++<6).toListX().size());

    }
    @Test
    public void testCycleUntilNoOrd() {
        count =0;
        assertEquals(asList(1, 2,3, 1, 2,3).size(),of(1, 2, 3).cycleUntil(next->count++==6).toListX().size());

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
                .toSetX(),equalTo(SetX.of(3,4)));

    }
    @Test
    public void testCycleUntil() {
        count =0;
        System.out.println("List " + of(1, 2, 3).peek(System.out::println).cycleUntil(next->count++==6).toListX());
        count =0;
        assertEquals(6,of(1, 2, 3).cycleUntil(next->count++==6).toListX().size());

    }
    @Test
    public void testCycleWhile() {
        count =0;
        assertEquals(6,of(1, 2, 3).cycleWhile(next->count++<6).toListX().size());

    }
    @Test
    public void testCycleTimesNoOrder() {
        assertEquals(6,of(1, 2).cycle(3).toListX().size());
    }
    @Test
    public void combineNoOrd(){
        assertThat(of(1,1,2,3)
                .combine((a, b)->a.equals(b), Semigroups.intSum)
                .toListX(),equalTo(ListX.of(3,4)));
    }
    @Test
    public void batchBySizeSet(){
        System.out.println("List = " + of(1,1,1,1,1,1).grouped(3,()->new java.util.TreeSet<>()).toList());
        assertThat(of(1,1,1,1,1,1).grouped(3,()->new java.util.TreeSet<>()).toList().get(0).size(),is(1));
        assertThat(of(1,1,1,1,1,1).grouped(3,()->new TreeSet<>()).toList().size(),is(2));
    }
    @Test
    public void testCycleNoOrder() {
        assertEquals(6,of(1, 2).cycle(3).toListX().size());
        assertEquals(6, of(1, 2, 3).cycle(2).toListX().size());
    }
    @Test
    public void lastIndexOf(){

      assertThat(empty().lastIndexOf(e->true),equalTo(Maybe.nothing()));
      assertThat(of(1).lastIndexOf(e->true),equalTo(Maybe.just(0l)));
      assertThat(of(1).lastIndexOf(e->false),equalTo(Maybe.nothing()));
      assertThat(of(1,2,3).lastIndexOf(e-> Objects.equals(2,e)),equalTo(Maybe.just(1l)));
      assertThat(of(1,2,3,2).lastIndexOf(e->Objects.equals(2,e)),equalTo(Maybe.just(2l)));
    }
}
