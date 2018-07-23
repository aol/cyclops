package cyclops.data.basetests;


import cyclops.companion.Monoids;
import cyclops.data.*;
import cyclops.data.HashSet;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class BaseImmutableSortedSetTest extends BaseImmutableSetTest {

    protected abstract <T> ImmutableSortedSet<T> fromStream(Stream<T> s);

    @Override
    public abstract <T> ImmutableSortedSet<T> of(T... values);

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
    public void testFoldRightA(){
        assertThat(fromStream(ReactiveSeq.range(0,100_000)).foldRight(Monoids.intSum),equalTo(704982704));
    }

    @Test
    public void span(){

        assertThat(of(1,2,3,4,1,2,3,4).span(i->i<3),equalTo(Tuple.tuple(of(1,2),of(3,4))));
        assertThat(of(1,2,3).span(i->i<9),equalTo(Tuple.tuple(of(1,2,3),of())));
        assertThat(of(1,2,3).span(i->i<0),equalTo(Tuple.tuple(of(),of(1,2,3))));
    }

    @Test
    public void splitBy(){

        assertThat(of(1,2,3,4,1,2,3,4).splitBy(i->i>3),equalTo(Tuple.tuple(of(1,2,3),of(4))));
        assertThat(of(1,2,3).splitBy(i->i<9),equalTo(Tuple.tuple(of(),of(1,2,3))));
        assertThat(of(1,2,3).splitBy(i->i<0),equalTo(Tuple.tuple(of(1,2,3),of())));
    }
    @Test
    public void testPartition() {


        assertEquals(asList(1, 3, 5), of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0)._1().toList());
        assertEquals(asList(2, 4, 6), of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 != 0)._2().toList());

        assertEquals(asList(2, 4, 6), of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 == 0)._1().toList());
        assertEquals(asList(1, 3, 5), of(1, 2, 3, 4, 5, 6).partition(i -> i % 2 == 0)._2().toList());

        assertEquals(asList(1, 2, 3), of(1, 2, 3, 4, 5, 6).partition(i -> i <= 3)._1().toList());
        assertEquals(asList(4, 5, 6), of(1, 2, 3, 4, 5, 6).partition(i -> i <= 3)._2().toList());

        assertEquals(asList(1, 2, 3, 4, 5, 6), of(1, 2, 3, 4, 5, 6).partition(i -> true)._1().toList());
        assertEquals(asList(), of(1, 2, 3, 4, 5, 6).partition(i -> true)._2().toList());

        assertEquals(asList(), of(1, 2, 3, 4, 5, 6).partition(i -> false)._1().toList());
        assertEquals(asList(1, 2, 3, 4, 5, 6), of(1, 2, 3, 4, 5, 6).splitBy(i -> false)._1().toList());
    }
    @Test
    public void splitAtTest(){
        assertThat(of(1,2,3).splitAt(4) ,equalTo(Tuple.tuple(of(1,2,3),of())));
        assertThat(of(1,2,3).splitAt(3) ,equalTo(Tuple.tuple(of(1,2,3),of())));
        assertThat(of(1,2,3).splitAt(2) ,equalTo(Tuple.tuple(of(1,2),of(3))));
        assertThat(of(1,2,3).splitAt(1) ,equalTo(Tuple.tuple(of(1),of(2,3))));
        assertThat(of(1,2,3).splitAt(0) ,equalTo(Tuple.tuple(of(),of(1,2,3))));
        assertThat(of(1,2,3).splitAt(-1) ,equalTo(Tuple.tuple(of(),of(1,2,3))));
    }

    @Test
    public void forEach2Filter() {

        assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), (a , b) -> a > 2 && b < 8,
                (a ,b) -> a + b).toList().size(), equalTo(Arrays.asList(3, 4, 5, 6, 7, 8, 9, 10).size()));
    }
    @Test
    public void testCycleTimesNoOrder() {
        assertEquals(2,of(1, 2).cycle(3).toList().size());

    }

    @Test
    public void permuations3() {
        System.out.println(of(1, 2, 3).permutations().map(s->s.toList()).toList());
        Seq<List<Integer>> x = of(1, 2, 3).permutations().map(s -> s.toList()).seq();


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
                .groupedWhile(i -> i % 3 != 0, () -> Vector.empty());

        ImmutableList<Vector<Integer>> l = x.seq();

        assertTrue(l.containsValue(Vector.of(1,2,3)));
        assertTrue(l.containsValue(Vector.of(4,5,6)));

    }
  @Test
  public void setViewTest(){
    Set<Integer> list = of(1,2,3).setView();
    assertThat(list.size(),equalTo(3));
    assertThat(list,equalTo(new java.util.HashSet<>(Arrays.asList(1,2,3))));

    assertThat(list.add(1),equalTo(false));
    assertThat(list.addAll(Arrays.asList(1)),equalTo(false));

    assertThat(list.contains(2),equalTo(true));
    assertThat(list.containsAll(Arrays.asList(2,3)),equalTo(true));
    assertThat(list.containsAll(Arrays.asList(2,3,4)),equalTo(false));
    assertThat(list.remove(1),equalTo(false));
    assertThat(list.remove((Object)1),equalTo(false));
    assertThat(list.removeAll(Arrays.asList(1)),equalTo(false));
  }
  @Test
  public void sortedSetViewTest(){
    SortedSet<Integer> list = of(1,2,3).sortedSetView();
    assertThat(list.size(),equalTo(3));
    assertThat(list,equalTo(new java.util.TreeSet<>(Arrays.asList(1,2,3))));

    assertThat(list.add(1),equalTo(false));
    assertThat(list.addAll(Arrays.asList(1)),equalTo(false));

    assertThat(list.contains(2),equalTo(true));
    assertThat(list.containsAll(Arrays.asList(2,3)),equalTo(true));
    assertThat(list.containsAll(Arrays.asList(2,3,4)),equalTo(false));
    assertThat(list.remove(1),equalTo(false));
    assertThat(list.remove((Object)1),equalTo(false));
    assertThat(list.removeAll(Arrays.asList(1)),equalTo(false));
  }
}
