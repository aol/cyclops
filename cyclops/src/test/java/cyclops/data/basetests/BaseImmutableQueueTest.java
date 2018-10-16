package cyclops.data.basetests;

import cyclops.data.basetests.AbstractIterableXTest;
import cyclops.companion.Monoids;
import cyclops.data.*;
import cyclops.data.tuple.Tuple;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import java.util.Arrays;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public abstract class BaseImmutableQueueTest extends AbstractIterableXTest {

    protected abstract <T> ImmutableQueue<T> fromStream(Stream<T> s);

    @Override
    public abstract <T> ImmutableQueue<T> empty();
    @Override
    public abstract <T> ImmutableQueue<T> of(T... values);
    @Test
    public void appendAllMultiple(){
        assertThat(of(1,2,3).appendAll(of()),equalTo(of(1,2,3)));
        assertThat(of(1,2,3).appendAll(of(4,5)),equalTo(of(1,2,3,4,5)));
    }
    @Test
    public void takeValues(){
        assertThat(of(1,2,3).take(-1),equalTo(of()));
        assertThat(of(1,2,3).take(0),equalTo(of()));
        assertThat(of(1,2,3).take(1),equalTo(of(1)));
        assertThat(of(1,2,3).take(2),equalTo(of(1,2)));
        assertThat(of(1,2,3).take(3),equalTo(of(1,2,3)));
        assertThat(of(1,2,3).take(4),equalTo(of(1,2,3)));
    }
    @Test
    public void takeRightValues(){
        assertThat(of(1,2,3).takeRight(-1),equalTo(of()));
        assertThat(of(1,2,3).takeRight(0),equalTo(of()));
        assertThat(of(1,2,3).takeRight(1),equalTo(of(3)));
        assertThat(of(1,2,3).takeRight(2),equalTo(of(2,3)));
        assertThat(of(1,2,3).takeRight(3),equalTo(of(1,2,3)));
        assertThat(of(1,2,3).takeRight(4),equalTo(of(1,2,3)));
    }
    @Test
    public void dropValues(){
        assertThat(of(1,2,3).drop(-1),equalTo(of(1,2,3)));
        assertThat(of(1,2,3).drop(0),equalTo(of(1,2,3)));
        assertThat(of(1,2,3).drop(1),equalTo(of(2,3)));
        assertThat(of(1,2,3).drop(2),equalTo(of(3)));
        assertThat(of(1,2,3).drop(3),equalTo(of()));
        assertThat(of(1,2,3).drop(4),equalTo(of()));
    }
    @Test
    public void dropRightValues(){
        assertThat(of(1,2,3).dropRight(-1),equalTo(of(1,2,3)));
        assertThat(of(1,2,3).dropRight(0),equalTo(of(1,2,3)));
        assertThat(of(1,2,3).dropRight(1),equalTo(of(1,2)));
        assertThat(of(1,2,3).dropRight(2),equalTo(of(1)));
        assertThat(of(1,2,3).dropRight(3),equalTo(of()));
        assertThat(of(1,2,3).dropRight(4),equalTo(of()));
    }
    @Test
    public void span(){

        assertThat(of(1,2,3,4,1,2,3,4).span(i->i<3),equalTo(Tuple.tuple(of(1,2),of(3,4,1,2,3,4))));
        assertThat(of(1,2,3).span(i->i<9),equalTo(Tuple.tuple(of(1,2,3),of())));
        assertThat(of(1,2,3).span(i->i<0),equalTo(Tuple.tuple(of(),of(1,2,3))));
    }

    @Test
    public void splitBy(){

        assertThat(of(1,2,3,4,1,2,3,4).splitBy(i->i>3),equalTo(Tuple.tuple(of(1,2,3),of(4,1,2,3,4))));
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
        assertEquals(asList(1, 2, 3, 4, 5, 6), of(1, 2, 3, 4, 5, 6).span(i -> false)._2().toList());
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
    public void testMapA(){
        assertThat(of(1,2,3).map(i->i*2),equalTo(BankersQueue.of(2,4,6)));
        assertThat(this.<Integer>empty().map(i->i*2),equalTo(BankersQueue.empty()));
    }
    @Test
    public void testFlatMapA(){
        assertThat(of(1,2,3).flatMap(i-> of(i*2)),equalTo(BankersQueue.of(2,4,6)));
        assertThat(this.<Integer>empty().concatMap(i-> of(i*2)),equalTo(BankersQueue.empty()));
    }

    @Test
    public void testFoldRightA(){
        assertThat(fromStream(ReactiveSeq.range(0,100_000)).foldRight(Monoids.intSum),equalTo(704982704));
    }
    @Test
    public void forEach2() {

        assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), (a , b) -> a + b).toList().size(),
                equalTo(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 3, 4, 5, 6, 7, 8,
                        9, 10, 11, 12).size()));
    }

    @Test
    public void forEach2Filter() {

        assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), (a , b) -> a > 2 && b < 8,
                (a ,b) -> a + b).toList().size(), equalTo(Arrays.asList(3, 4, 5, 6, 7, 8, 9, 10).size()));
    }
    @Test
    public void onEmptyThrow(){

        assertTrue(empty().onEmptyTry(()->new RuntimeException("hello")).isFailure());
        assertFalse(of(1,2).onEmptyTry(()->new RuntimeException("hello")).isFailure());
    }
  @Test
  public void queueViewTest() {
      Queue<Integer> list = of(1, 2, 3).queueView();
      assertThat(list.size(), equalTo(3));
      assertThat(list.toArray(), equalTo(BankersQueue.of(1, 2, 3).toArray()));

      assertThat(list.contains(2), equalTo(true));
      assertThat(list.containsAll(Arrays.asList(2, 3)), equalTo(true));
      assertThat(list.containsAll(Arrays.asList(2, 3, 4)), equalTo(false));
  }
  @Test
  public void viewTest(){
    Queue<Integer> list = of(1,2,3).queueView();
    assertThat(list.size(),equalTo(3));




    assertThat(list.contains(2),equalTo(true));
    assertThat(list.containsAll(Arrays.asList(2,3)),equalTo(true));
    assertThat(list.containsAll(Arrays.asList(2,3,4)),equalTo(false));

  }

    @Test(expected = UnsupportedOperationException.class)
    public void addView(){
        Queue<Integer> list = of(1,2,3).queueView();


        assertThat(list.add(1),equalTo(false));

    }
    @Test(expected = UnsupportedOperationException.class)
    public void addAllView(){
        Queue<Integer> list = of(1,2,3).queueView();
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
        Queue<Integer> list = of(1,2,3).queueView();

        assertThat(list.remove(1),equalTo(false));
        assertThat(list.remove((Object)1),equalTo(false));
        assertThat(list.removeAll(Arrays.asList(1)),equalTo(false));
    }
    @Test(expected = UnsupportedOperationException.class)
    public void removeObjectView(){
        Queue<Integer> list = of(1,2,3).queueView();


        assertThat(list.remove((Object)1),equalTo(false));

    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeAllView(){
        Queue<Integer> list = of(1,2,3).queueView();


        assertThat(list.removeAll(Arrays.asList(1)),equalTo(false));
    }

}
