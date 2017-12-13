package cyclops.collections;

import com.oath.cyclops.data.collections.extensions.CollectionX;
import com.oath.cyclops.types.traversable.IterableX;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.ListX;
import cyclops.collections.mutable.SetX;
import cyclops.companion.Monoids;
import cyclops.companion.Reducers;
import cyclops.companion.Semigroups;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cyclops.data.tuple.Tuple.tuple;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class AbstractSetTest extends AbstractCollectionXTest {
    protected abstract <T> CollectionX<T> fromStream(Stream<T> s);

    @Test
    public void testMapA(){
        assertThat(of(1,2,3).map(i->i*2),equalTo(of(2,4,6)));
        assertThat(this.<Integer>empty().map(i->i*2),equalTo(empty()));
    }


    @Test
    public void sortedComparatorNoOrd() {
        assertThat(of(1,5,3,4,2).sorted((t1,t2) -> t2-t1).collect(Collectors.toList()),hasItems(5,4,3,2,1));
    }
    @Test
    public void whenGreaterThan2NoOrd() {
        String res = of(5, 2, 3).visit((x, xs) -> xs.join(x > 2 ? "hello" : "hello"), () -> "boo!");
        assertThat(res, containsString("hello"));
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

        assertThat(((IterableX<Integer>)of(1,2,3).intersperse(0)).toListX(),hasItem(0));




    }
    @Test
    public void cycleMonoidNoOrder(){
        assertThat(of(1,2,3)
                        .cycle(Reducers.toCountInt(),3)
                        .toSetX(),
                equalTo(SetX.of(3)));
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
    public void forEach2() {

        assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), (a , b) -> a + b).toList().size(),
                equalTo(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).size()));
    }

    @Test
    public void forEach2Filter() {

        assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), (a , b) -> a > 2 && b < 8,
                (a ,b) -> a + b).toList().size(), equalTo(Arrays.asList(3, 4, 5, 6, 7, 8, 9, 10).size()));
    }
    /**
    @Test
    public void testCycleNoOrd() {
        assertEquals(asList(1, 2 ),of(1, 2).cycle(3).toListX());
        assertEquals(asList(1, 2, 3), of(1, 2, 3).cycle(2).toListX());
    }

    @Test
    public void testCycleTimesNoOrd() {
        System.out.println(of(1, 2).cycle(3).materialize());
        assertEquals(asList(1, 2),of(1, 2).cycle(3).materialize().toListX());
    }
     **/

    @Test
    public void slidingIncrementNoOrd() {
        List<VectorX<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(3, 2).collect(Collectors.toList());

        System.out.println(list);
        assertThat(list.get(0).size(), greaterThan(1));
        assertThat(list.get(1).size(), greaterThan(1));
    }
    /**
    int count =0;
    @Test
    public void testCycleWhileNoOrd() {
        count =0;
        assertEquals(asList(1, 2,3),of(1, 2, 3).cycleWhile(next->count++<6).toListX());

    }
    @Test
    public void testCycleUntilNoOrd() {
        count =0;
        assertEquals(asList(1, 2,3),of(1, 2, 3).cycleUntil(next->count++==6).toListX());
    }
    @Test
    public void testCycleUntil() {
        count =0;
        System.out.println("List " + of(1, 2, 3).peek(System.out::println).cycleUntil(next->count++==6).toListX());
        count =0;
        assertEquals(3,of(1, 2, 3).cycleUntil(next->count++==6).toListX().size());

    }
    @Test
    public void testCycleWhile() {
        count =0;
        assertEquals(3,of(1, 2, 3).cycleWhile(next->count++<6).toListX().size());

    }
     **/
    @Test
    public void testScanLeftStringConcatMonoid() {
        assertThat(of("a", "b", "c").scanLeft(Reducers.toString("")).toList(), hasItems( "a", "ab", "abc"));
    }

    @Test
    public void allCombinations3NoOrd() {
        SetX<SetX<Integer>> x = of(1, 2, 3).combinations().map(s -> s.toSetX()).toSetX();
        System.out.println(x);
        assertTrue(x.containsValue(SetX.empty()));
        assertTrue(x.containsValue(SetX.of(1)));
        assertTrue(x.containsValue(SetX.of(2)));
        assertTrue(x.containsValue(SetX.of(3)));
        assertTrue(x.containsValue(SetX.of(1,2)));
        assertTrue(x.containsValue(SetX.of(1,3)));
        assertTrue(x.containsValue(SetX.of(2,3)));
        assertTrue(x.containsValue(SetX.of(1,2,3)));

    }
    @Test
    public void combinations2NoOrd() {
        SetX<SetX<Integer>> x = of(1, 2, 3).combinations(2).map(s -> s.toSetX()).toSetX();
        assertTrue(x.containsValue(SetX.of(1,2)));
        assertTrue(x.containsValue(SetX.of(1,3)));
        assertTrue(x.containsValue(SetX.of(2,3)));
    }
    /**
    @Test
    public void testCycleTimesNoOrder() {
        assertEquals(2,of(1, 2).cycle(3).toListX().size());
    }
     **/
    @Test
    public void combineNoOrd(){
        assertThat(of(1,1,2,3)
                .combine((a, b)->a.equals(b), Semigroups.intSum)
                .toListX().size(),greaterThan(1));
    }
    @Test
    public void slidingNoOrd() {
        SetX<VectorX<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(2).toSetX();

        System.out.println(list);
        assertTrue(list.containsValue(VectorX.of(1,2)));
    }
    @Test
    public void duplicates(){

        assertThat(of(1,2,1,2,1,2).size(),equalTo(2));
    }



    @Test
    public void testFlatMapA(){
        assertThat(of(1,2,3).concatMap(i-> of(i*2)),equalTo(of(2,4,6)));
        assertThat(this.<Integer>empty().concatMap(i-> of(i*2)),equalTo(empty()));
    }



    @Test
    public void testFoldRightA(){
        assertThat(fromStream(ReactiveSeq.range(0,100_000)).materialize().foldRight(Monoids.intSum),equalTo(704982704));
    }

    /**
     @Test
    public void testCycleNoOrd() {
        assertEquals(asList(1, 2 ),of(1, 2).cycle(3).toListX());
        assertEquals(asList(1, 2, 3), of(1, 2, 3).cycle(2).toListX());
    }

    @Test
    public void testCycleTimesNoOrd() {
        assertEquals(asList(1, 2),of(1, 2).cycle(3).toListX());
    }
    **/


    int count =0;
    /**
    @Test
    public void testCycleWhileNoOrd() {
        count =0;
        assertEquals(asList(1, 2,3),of(1, 2, 3).cycleWhile(next->count++<6).toListX());

    }
    @Test
    public void testCycleUntilNoOrd() {
        count =0;
        assertEquals(asList(1, 2,3),of(1, 2, 3).cycleUntil(next->count++==6).toListX());
    }
    @Test
    public void testCycleUntil() {
        count =0;
        System.out.println("List " + of(1, 2, 3).peek(System.out::println).cycleUntil(next->count++==6).toListX());
        count =0;
        assertEquals(3,of(1, 2, 3).cycleUntil(next->count++==6).toListX().size());

    }
    @Test
    public void testCycleWhile() {
        count =0;
        assertEquals(3,of(1, 2, 3).cycleWhile(next->count++<6).toListX().size());

    }

    @Test
    public void testCycleTimesNoOrder() {
        assertEquals(2,of(1, 2).cycle(3).toListX().size());
    }

    @Test
    public void testCycleNoOrder() {
        assertEquals(2,of(1, 2).cycle(3).toListX().size());
        assertEquals(3, of(1, 2, 3).cycle(2).toListX().size());
    }
     **/
    @Test
    public void take2Reversed(){
        range(0,10).reverse().limit(2).printOut();
        assertThat(range(0,10).materialize().reverse(),equalTo(range(0,10)));
    }
    @Test
    public void allCombinations3() {
        assertThat(of(1, 2, 3).combinations().map(s->s.toList()).toListX(),hasItems(ListX.of(), ListX.of(1), ListX.of(2),
                ListX.of(3), ListX.of(1, 2), ListX.of(1, 3), ListX.of(2, 3), ListX.of(1, 2, 3)));
    }
    @Test
    public void rangeLongReversedSkip(){
        System.out.println(rangeLong(0,5).reverse()
                .skip(3));
        assertThat(rangeLong(0,5).materialize().reverse(),equalTo(rangeLong(0,5)));
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
                .skip(3).toListX().size(),equalTo(2));
    }
    @Test
    public void rangeIntReversedSkip(){

        assertThat(range(0,20).reverse()
                .limit(10).skip(8).size(),equalTo(2));
    }
    @Test
    public void combinations2() {
        assertThat(of(1, 2, 3).combinations(2).map(s->s.toList()).toList(),
                hasItems(ListX.of(1, 2), ListX.of(1, 3), ListX.of(2, 3)));
    }
    @Test
    public void rangeInt(){
        System.out.println(range(0,150));
        assertThat(range(0,150)
                .limit(2).count(),equalTo(2l));
    }
    @Test
    public void rangeIntReversed(){
        assertThat(range(0,150).reverse()
                .limit(2).size(),equalTo(2));
    }
    @Test
    public void removeFirst(){
        IterableX<Integer> vec = this.of(1,2,2,2,3);

        assertThat(vec.removeFirst(i->i==2),equalTo(of(1,2,3)));
    }
    @Test
    public void permuations3() {
        System.out.println(of(1, 2, 3).permutations().map(s->s.toList()).toList());
        CollectionX<List<Integer>> x = of(1, 2, 3).permutations().map(s -> s.toList());

        assertTrue(x.containsValue(ListX.of(1,2,3)));
        assertTrue(x.containsValue(ListX.of(3,2,1)));
        assertTrue(x.containsValue(ListX.of(2,1,3)));
        assertTrue(x.containsValue(ListX.of(2,3,1)));
        assertTrue(x.containsValue(ListX.of(3,1,2)));
        assertTrue(x.containsValue(ListX.of(1,3,2)));
    }
    @Test
    public void batchWhileCollection(){
        assertThat(of(1,2,3,4,5,6)
                .groupedWhile(i->i%3!=0,()->new ArrayList<>())
                .toList().size(),equalTo(2));
        CollectionX<List<Integer>> x = of(1, 2, 3, 4, 5, 6)
                .groupedWhile(i -> i % 3 != 0, () -> new ArrayList<>());

        assertTrue(x.containsValue(ListX.of(1,2,3)));
        assertTrue(x.containsValue(ListX.of(4,5,6)));

    }
    @Test
    public void batchUntilCollection(){
        assertThat(of(1,2,3,4,5,6)
                .groupedUntil(i->i%3==0,()->new ArrayList<>())
                .toList().size(),equalTo(2));
        assertTrue(of(1,2,3,4,5,6)
                .groupedUntil(i->i%3==0,()->new ArrayList<>())
                .toList().contains(ListX.of(1,2,3)));
    }
    @Test
    public void batchBySizeSet(){
        System.out.println("List = " + of(1,1,1,1,1,1).grouped(3,()->new TreeSet<>()).toList());
        assertThat(of(1,1,1,1,1,1).grouped(3,()->new TreeSet<>()).toList().get(0).size(),is(1));
        assertThat(of(1,1,1,1,1,1).grouped(3,()->new TreeSet<>()).toList().size(),is(1));
    }
    @Test
    public void combine(){
        assertThat(of(1,1,2,3).materialize()
                .combine((a, b)->a.equals(b),Semigroups.intSum).materialize()
                .toSetX(),equalTo(SetX.of(1,2,3)));

    }
    @Test
    public void intStreamCompareReversed(){


        assertThat(0,
                equalTo(range(-5,6).materialize().reverse().sumInt(i->i)));

    }
    @Test
    public void longStreamCompareReversed(){
        assertThat(0L,
                equalTo(rangeLong(-5,6).materialize().reverse().sumLong(i->i)));
    }
    @Test
    public void reduceWithMonoidJoin(){
        String s = of("hello","2","world","4").join(",");
        Arrays.asList("hello","2","world","4").forEach(c->{
            assertTrue(s.contains(c));
        });

        String s2 =of("hello","2","world","4").reduce(Reducers.toString(","));
        Arrays.asList("hello","2","world","4").forEach(c->{
            assertTrue(s2.contains(c));
        });
    }

}
