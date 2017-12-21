package cyclops.data.basetests;

import cyclops.collections.AbstractIterableXTest;
import cyclops.companion.Monoids;
import cyclops.data.*;
import cyclops.reactive.ReactiveSeq;
import org.junit.Test;

import java.util.Arrays;
import java.util.Queue;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class BaseImmutableQueueTest extends AbstractIterableXTest {

    protected abstract <T> ImmutableQueue<T> fromStream(Stream<T> s);

    @Override
    public abstract <T> ImmutableQueue<T> empty();
    @Override
    public abstract <T> ImmutableQueue<T> of(T... values);

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
  public void queueViewTest(){
    Queue<Integer> list = of(1,2,3).queueView();
    assertThat(list.size(),equalTo(3));
    assertThat(list.toArray(),equalTo(BankersQueue.of(1,2,3).toArray()));
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
  public void viewTest(){
    Queue<Integer> list = of(1,2,3).queueView();
    assertThat(list.size(),equalTo(3));


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
