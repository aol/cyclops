package cyclops.data.basetests;

import com.oath.cyclops.types.traversable.IterableX;
import cyclops.Sets;
import cyclops.data.Seq;
import cyclops.data.TreeSet;
import cyclops.data.Vector;
import cyclops.data.basetests.AbstractIterableXTest;
import cyclops.data.*;


import cyclops.companion.Monoids;
import cyclops.companion.Reducers;
import cyclops.companion.Semigroups;
import cyclops.data.HashSet;
import cyclops.data.ImmutableSet;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cyclops.data.tuple.Tuple.tuple;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public abstract class BaseImmutableSetTest extends AbstractIterableXTest {

    protected abstract <T> ImmutableSet<T> fromStream(Stream<T> s);

    @Override
    public abstract <T> ImmutableSet<T> of(T... values);
    @Override
    public abstract <T> ImmutableSet<T> empty();

    @Test
    public void duplicates(){
        assertThat(of(1,2,1,2,1,2).size(),equalTo(2));
    }


    @Test
    public void testMapA(){
        assertThat(of(1,2,3).map(i->i*2),equalTo(HashSet.of(2,4,6)));
        assertThat(this.<Integer>empty().map(i->i*2),equalTo(HashSet.empty()));
    }
    @Test
    public void testFlatMapA(){
        assertThat(of(1,2,3).flatMap(i-> of(i*2)),equalTo(HashSet.of(2,4,6)));
        assertThat(this.<Integer>empty().concatMap(i-> of(i*2)),equalTo(HashSet.empty()));
    }

    @Test
    public void sortedComparatorNoOrd() {
        assertThat(of(1,5,3,4,2).sorted((t1,t2) -> t2-t1).collect(Collectors.toList()),hasItems(5,4,3,2,1));
    }

    @Test
    public void testSorted() {

        IterableX<Tuple2<Integer, String>> t1 = of(tuple(2, "two"), tuple(1, "replaceWith"));

        List<Tuple2<Integer, String>> s1 = t1.sorted().toList();
        System.out.println(s1);
        assertTrue(s1.contains(tuple(1, "replaceWith")));
        assertTrue(s1.contains(tuple(2, "two")));

    }
    @Test
    public void testIntersperseNoOrd() {

        assertThat(((IterableX<Integer>)of(1,2,3).intersperse(0)).toList(),hasItem(0));




    }
    @Test
    public void cycleMonoidNoOrder(){
        assertThat(of(1,2,3)
                        .cycle(Reducers.toCountInt(),3)
                        .toSet(),
                equalTo(new java.util.HashSet<>(Arrays.asList(3))));
    }
    @Test
    public void permuations3NoOrd() {
        /**
         * Hello [[1, 2, 3]]
         peek - [1,2,3]

         */
        System.out.println("Hello " +of(1, 2, 3).permutations().map(s->s.toSet()).toSet());
        assertThat(of(1, 2, 3).permutations().map(s->s.toSet()).toSet(),
                equalTo(of(of(1, 2, 3),
                        of(1, 3, 2), of(2, 1, 3), of(2, 3, 1), of(3, 1, 2), of(3, 2, 1))
                            .peek(i->System.out.println("peek - " + i)).map(s->s.toSet()).toSet()));
    }

    @Test
    public void testFoldRightA(){
        assertThat(fromStream(ReactiveSeq.range(0,100_000)).foldRight(Monoids.intSum),equalTo(704982704));
    }
    @Test
    public void forEach2() {

        assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), (a , b) -> a + b).toList().size(),
                equalTo(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).size()));
    }

    @Test
    public void forEach2Filter() {

        assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), (a , b) -> a > 2 && b < 8,
                (a ,b) -> a + b).toList().size(), equalTo(Arrays.asList(3, 4, 5, 6, 7, 8, 9, 10).size()));
    }
    @Test
    public void testCycleNoOrd() {
        assertEquals(asList(1, 2 ),of(1, 2).cycle(3).toList());
        assertEquals(asList(1, 2, 3), of(1, 2, 3).cycle(2).toList());
    }
    @Test
    public void testCycleTimesNoOrd() {
        assertEquals(asList(1, 2),of(1, 2).cycle(3).toList());
    }

    @Test
    public void slidingIncrementNoOrd() {
        List<Seq<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(3, 2).collect(Collectors.toList());

        System.out.println(list);
        assertThat(list.get(0).size(), greaterThan(1));
        assertThat(list.get(1).size(), greaterThan(1));
    }
    int count =0;
    @Test
    public void testCycleWhileNoOrd() {
        count =0;
        assertEquals(asList(1, 2,3),of(1, 2, 3).cycleWhile(next->count++<6).toList());

    }
    @Test
    public void testCycleUntilNoOrd() {
        count =0;
        assertEquals(asList(1, 2,3),of(1, 2, 3).cycleUntil(next->count++==6).toList());
    }
    @Test
    public void testCycleUntil() {
        count =0;
        System.out.println("List " + of(1, 2, 3).peek(System.out::println).cycleUntil(next->count++==6).toList());
        count =0;
        assertEquals(3,of(1, 2, 3).cycleUntil(next->count++==6).toList().size());

    }
    @Test
    public void testCycleWhile() {
        count =0;
        assertEquals(3,of(1, 2, 3).cycleWhile(next->count++<6).toList().size());

    }
    @Test
    public void testScanLeftStringConcatMonoid() {
        assertThat(of("a", "b", "c").scanLeft(Reducers.toString("")).toList(), hasItems( "a", "ab", "abc"));
    }

    @Test
    public void allCombinations3NoOrd() {
        Set<Set<Integer>> x = of(1, 2, 3).combinations().map(s -> s.toSet()).toSet();
        System.out.println(x);
        assertTrue(x.contains(Sets.empty()));
        assertTrue(x.contains(Sets.set(1)));
        assertTrue(x.contains(Sets.set(2)));
        assertTrue(x.contains(Sets.set(3)));
        assertTrue(x.contains(Sets.set(1,2)));
        assertTrue(x.contains(Sets.set(1,3)));
        assertTrue(x.contains(Sets.set(2,3)));
        assertTrue(x.contains(Sets.set(1,2,3)));

    }
    @Test
    public void combinations2NoOrd() {
        Set<Set<Integer>> x = of(1, 2, 3).combinations(2).map(s -> s.toSet()).toSet();
        assertTrue(x.contains(Sets.set(1,2)));
        assertTrue(x.contains(Sets.set(1,3)));
        assertTrue(x.contains(Sets.set(2,3)));
    }
    @Test
    public void testCycleTimesNoOrder() {
        assertEquals(2,of(1, 2).cycle(3).toList().size());
    }
    @Test
    public void combineNoOrd(){
        assertThat(of(1,1,2,3)
                .combine((a, b)->a.equals(b), Semigroups.intSum)
                .toList(),equalTo(Arrays.asList(1,2,3)));
    }
    @Test
    public void slidingNoOrd() {
        Set<Seq<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(2).toSet();

        System.out.println(list);
        assertTrue(list.contains(Vector.of(1,2)));
    }
    @Test
    public void testCycleNoOrder() {
        assertEquals(2,of(1, 2).cycle(3).toList().size());
        assertEquals(3, of(1, 2, 3).cycle(2).toList().size());
    }
    @Test
    public void take2Reversed(){
        range(0,10).reverse().take(2).printOut();
        assertThat(range(0,10).reverse(),equalTo(range(0,10)));
    }
    @Test
    public void allCombinations3() {
        assertThat(of(1, 2, 3).combinations().map(s->s.toList()).toList(),hasItems(Arrays.asList(), Arrays.asList(1), Arrays.asList(2),
                Arrays.asList(3), Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3), Arrays.asList(1, 2, 3)));
    }
    @Test
    public void rangeLongReversedSkip(){
        System.out.println(rangeLong(0,5).reverse()
                .drop(3));
        assertThat(rangeLong(0,5).reverse(),equalTo(rangeLong(0,5)));
    }
    @Test @Ignore
    public void longStreamCompare(){

    }
    @Test
    public void negativeLong(){
        assertThat(rangeLong(-1000L,200)
                .count(),equalTo(1200L));
    }
    @Test
    public void rangeIntReversedSkip2(){
        assertThat(range(0,5).reverse()
                .drop(3).toList().size(),equalTo(2));
    }
    @Test
    public void rangeIntReversedSkip(){

        assertThat(range(0,20).reverse()
                .take(10).drop(8).size(),equalTo(2));
    }
    @Test
    public void combinations2() {
        assertThat(of(1, 2, 3).combinations(2).map(s->s.toList()).toList(),
                hasItems(Arrays.asList(1, 2), Arrays.asList(1, 3), Arrays.asList(2, 3)));
    }
    @Test
    public void rangeInt(){
        System.out.println(range(0,150));
        assertThat(range(0,150)
                .take(2).count(),equalTo(2l));
    }
    @Test
    public void rangeIntReversed(){
        assertThat(range(0,150).reverse()
                .take(2).size(),equalTo(2));
    }
    @Test
    public void removeFirst(){
        IterableX<Integer> vec = this.of(1,2,2,2,3);

        assertThat(vec.removeFirst(i->i==2),equalTo(of(1,3)));
    }
    @Test
    public void permuations3() {
        System.out.println(of(1, 2, 3).permutations().map(s->s.toList()).toList());
        ImmutableSet<List<Integer>> x = of(1, 2, 3).permutations().map(s -> s.toList());

        assertTrue(x.containsValue(Arrays.asList(1,2,3)));
        assertTrue(x.containsValue(Arrays.asList(3,2,1)));
        assertTrue(x.containsValue(Arrays.asList(2,1,3)));
        assertTrue(x.containsValue(Arrays.asList(2,3,1)));
        assertTrue(x.containsValue(Arrays.asList(3,1,2)));
        assertTrue(x.containsValue(Arrays.asList(1,3,2)));
    }
    @Test
    public void batchWhileCollection(){
        assertThat(of(1,2,3,4,5,6)
                .groupedWhile(i->i%3!=0,()-> Vector.empty())
                .toList().size(),equalTo(2));
        ImmutableSet<Vector<Integer>> x = of(1, 2, 3, 4, 5, 6)
                .groupedWhile(i -> i % 3 != 0, () ->  Vector.empty());

        assertTrue(x.containsValue(Vector.of(1,2,3)));
        assertTrue(x.containsValue(Vector.of(4,5,6)));

    }
    @Test
    public void batchUntilCollection(){
        assertThat(of(1,2,3,4,5,6)
                .groupedUntil(i->i%3==0,()->Vector.empty())
                .toList().size(),equalTo(2));
        assertTrue(of(1,2,3,4,5,6)
                .groupedUntil(i->i%3==0,()->Vector.empty())
                .toList().contains(Vector.of(1,2,3)));
    }
    @Test
    public void batchBySizeSet(){
        System.out.println("List = " + of(1,1,1,1,1,1).grouped(3,()-> TreeSet.empty()).toList());
        assertThat(of(1,1,1,1,1,1).grouped(3,()->TreeSet.empty()).toList().get(0).size(),is(1));
        assertThat(of(1,1,1,1,1,1).grouped(3,()->TreeSet.empty()).toList().size(),is(1));
    }
    @Test
    public void combine(){
        assertThat(of(1,1,2,3)
                .combine((a, b)->a.equals(b),Semigroups.intSum)
                .toList(),equalTo(Arrays.asList(1,2,3)));

    }
    @Test
    public void reduceWithMonoidJoin(){
        String s = of("hello","2","world","4").join(",");
        Arrays.asList("hello","2","world","4").forEach(c->{
            assertTrue(s.contains(c));
        });

        String s2 =of("hello","2","world","4").foldLeft(Reducers.toString(","));
        Arrays.asList("hello","2","world","4").forEach(c->{
            assertTrue(s2.contains(c));
        });
    }
    @Test
    public void onEmptyThrow(){

        assertTrue(empty().onEmptyTry(()->new RuntimeException("hello")).isFailure());
        assertFalse(of(1,2).onEmptyTry(()->new RuntimeException("hello")).isFailure());
    }
  @Test
  public void viewTest(){
    Set<Integer> list = of(1,2,3).setView();
    assertThat(list.size(),equalTo(3));
    assertThat(list,equalTo(new java.util.HashSet<>(Arrays.asList(1,2,3))));



    assertThat(list.contains(2),equalTo(true));
    assertThat(list.containsAll(Arrays.asList(2,3)),equalTo(true));
    assertThat(list.containsAll(Arrays.asList(2,3,4)),equalTo(false));

  }

  @Test(expected = UnsupportedOperationException.class)
  public void addView(){
        Set<Integer> list = of(1,2,3).setView();


        assertThat(list.add(1),equalTo(false));

    }
    @Test(expected = UnsupportedOperationException.class)
    public void addAllView(){
        Set<Integer> list = of(1,2,3).setView();
          assertThat(list.addAll(Arrays.asList(1)),equalTo(false));

        assertThat(list.contains(2),equalTo(true));
        assertThat(list.containsAll(Arrays.asList(2,3)),equalTo(true));
        assertThat(list.containsAll(Arrays.asList(2,3,4)),equalTo(false));
        assertThat(list.remove(1),equalTo(false));
        assertThat(list.remove((Object)1),equalTo(false));
        assertThat(list.removeAll(Arrays.asList(1)),equalTo(false));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeView(){
        Set<Integer> list = of(1,2,3).setView();

        assertThat(list.remove(1),equalTo(false));
        assertThat(list.remove((Object)1),equalTo(false));
        assertThat(list.removeAll(Arrays.asList(1)),equalTo(false));
    }
    @Test(expected = UnsupportedOperationException.class)
    public void removeObjectView(){
        Set<Integer> list = of(1,2,3).setView();


        assertThat(list.remove((Object)1),equalTo(false));

    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeAllView(){
        Set<Integer> list = of(1,2,3).setView();


        assertThat(list.removeAll(Arrays.asList(1)),equalTo(false));
    }
    @Test
    public void drop2Reversed(){
        System.out.println(Spouts.range(0,10).vector());
        System.out.println(ReactiveSeq.range(0,10).vector());
        System.out.println(range(0,10).reverse().vector());
        System.out.println("RS"  + ReactiveSeq.range(0,10).reverse().vector());
        assertThat(range(0,10).reverse().drop(2).toList().size(),equalTo(8));
        assertThat(range(0,10).reverse().drop(2).toList(),not(contains(10)));
    }
    @Test
    public void drop2ReversedLong(){
        System.out.println(Spouts.rangeLong(0,10).vector());
        System.out.println(ReactiveSeq.range(0,10).vector());
        System.out.println(range(0,10).reverse().vector());
        System.out.println(ReactiveSeq.range(0,10).reverse().vector());
        assertThat(rangeLong(0,10).reverse().drop(2).toList().size(),equalTo(8));
        assertThat(rangeLong(0,10).reverse().drop(2).toList(),not(contains(10)));
    }
    @Test
    public void take2ReversedLong(){
        System.out.println(Spouts.rangeLong(0,10).vector());
        System.out.println(ReactiveSeq.range(0,10).vector());
        System.out.println(range(0,10).reverse().vector());
        System.out.println(ReactiveSeq.range(0,10).reverse().vector());
        System.out.println(ReactiveSeq.rangeLong(0,10).reverse().vector());
        assertThat(rangeLong(0,10).reverse().take(2).toList().size(),equalTo(2));
        assertThat(rangeLong(0,10).reverse().take(2).toList(),not(contains(10)));
    }
    @Test
    public void pushFlatMap() {

        IterableX<Integer> odds =of(1, 3, 5, 7, 9);
        IterableX<Integer> even = of(2, 4, 6);

        IterableX<Vector<Tuple2<Integer,Integer>>> zipped = Spouts.from(odds.zip(  (t1, t2) -> Tuple.tuple(t1, t2),even)).reduceAll(Vector.empty(),(a, b)->a.plus(b));


        Vector<Tuple2<Integer, Integer>> x = zipped.elementAt(0l).orElse(null);
        System.out.println(x);
        assertThat(x,containsInAnyOrder(Tuple.tuple(1, 2),
            Tuple.tuple(3, 4),
            Tuple.tuple(5, 6)));

        IterableX<Vector<Tuple2<Integer,Integer>>> zipped2 = Spouts.from(odds.concatMap(it -> of(it)
            .zip( (t1, t2) -> Tuple.tuple(t1, t2),even)
        )).reduceAll(Vector.empty(),(a, b)->a.plus(b));

        Vector<Tuple2<Integer, Integer>> x2 = zipped2.elementAt(0l).orElse(null);
        System.out.println("X2 is  " +x2);
        assertThat(x2,containsInAnyOrder(Tuple.tuple(1, 2),
            Tuple.tuple(3, 2),
            Tuple.tuple(5, 2),
            Tuple.tuple(7, 2),
            Tuple.tuple(9, 2)));
    }

    @Override
    public void prependAppend3() {
        Assert.assertThat(of(1)
            .prependStream(Stream.of(2)).toList(), containsInAnyOrder(ReactiveSeq.fromIterable(asList(1))
            .prependStream(Stream.of(2)).toList()));
    }

    @Override
    public void statelessRemoveFirst() {
        IterableX<Integer> stream = of(5,2,1).removeFirst(e -> Objects.equals(e, 2));

        assertThat(stream.toList(),containsInAnyOrder(Arrays.asList(5,1)));
        assertThat(stream.toList(),containsInAnyOrder(Arrays.asList(5,1)));
    }
}
