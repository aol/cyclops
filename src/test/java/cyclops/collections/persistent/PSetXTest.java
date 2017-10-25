package cyclops.collections.persistent;

import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collections.AbstractSetTest;
import cyclops.collections.immutable.PersistentSetX;
import cyclops.collections.mutable.ListX;
import cyclops.control.Option;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.data.tuple.Tuple2;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static org.hamcrest.Matchers.equalTo;
import static cyclops.data.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PSetXTest extends AbstractSetTest {

    AtomicLong counter = new AtomicLong(0);
    @Before
    public void setup(){

        counter = new AtomicLong(0);
        super.setup();
    }
    @Test
    public void asyncTest() throws InterruptedException {
        Spouts.async(Stream.generate(()->"next"), Executors.newFixedThreadPool(1))
                .onePer(1, TimeUnit.MILLISECONDS)
                .take(1000)
                .to()
                .persistentSetX(Evaluation.LAZY)
                .peek(i->counter.incrementAndGet())
                .materialize();

        long current = counter.get();
        Thread.sleep(400);
        assertTrue(current<counter.get());
    }

    @Override
    protected <T> CollectionX<T> fromStream(Stream<T> s) {
        return PersistentSetX.persistentSetX(ReactiveSeq.fromStream(s));
    }

    @Test
    public void testSorted() {

        CollectionX<Tuple2<Integer, String>> t1 = of(tuple(2, "two"), tuple(1, "replaceWith"));

        List<Tuple2<Integer, String>> s1 = t1.sorted().toListX().sorted();
        //System.out.println(s1);
        assertEquals(tuple(1, "replaceWith"), s1.get(0));
        assertEquals(tuple(2, "two"), s1.get(1));

        CollectionX<Tuple2<Integer, String>> t2 = of(tuple(2, "two"), tuple(1, "replaceWith"));
        List<Tuple2<Integer, String>> s2 = t2.sorted(comparing(t -> t._1())).toListX().sorted();
        assertEquals(tuple(1, "replaceWith"), s2.get(0));
        assertEquals(tuple(2, "two"), s2.get(1));

        CollectionX<Tuple2<Integer, String>> t3 = of(tuple(2, "two"), tuple(1, "replaceWith"));
        List<Tuple2<Integer, String>> s3 = t3.sorted(t -> t._1()).toListX().sorted();
        assertEquals(tuple(1, "replaceWith"), s3.get(0));
        assertEquals(tuple(2, "two"), s3.get(1));

    }
    @Override
    public <T> FluentCollectionX<T> of(T... values) {
        return PersistentSetX.of(values);
    }

    @Test
    public void cycle(){
        ListX.of(1).stream().cycle(2).forEach(System.out::println);
        Iterator<Integer> it = ListX.of(1).stream().cycle(2).iterator();
        while(it.hasNext()){
            System.out.println("Next " + it.next());
        }
      //  System.out.println(of(1).cycle(2));
    //    assertThat(of(1,2,3).cycle(2).listX(),equalTo(ListX.of(3,2,1,3,2,1)));
    }
    @Test
    public void onEmptySwitch() {
        assertThat(PersistentSetX.empty()
                        .onEmptySwitch(() -> PersistentSetX.of(1, 2, 3)),
                   equalTo(PersistentSetX.of(1, 2, 3)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aol.cyclops2.function.collections.extensions.AbstractCollectionXTest#
     * zero()
     */
    @Override
    public <T> FluentCollectionX<T> empty() {
        return PersistentSetX.empty();
    }

    @Test
    @Override
    public void forEach2() {

        assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), (a, b) -> a + b)
                              .size(),
                   equalTo(12));
    }
    @Test
    public void coflatMap(){
       assertThat(PersistentSetX.of(1,2,3)
                   .coflatMap(s->s.sumInt(i->i))
                   .singleOrElse(null),equalTo(6));
        
    }
    @Override
    public FluentCollectionX<Integer> range(int start, int end) {
        return PersistentSetX.range(start, end);
    }

    @Override
    public FluentCollectionX<Long> rangeLong(long start, long end) {
        return PersistentSetX.rangeLong(start, end);
    }

    @Override
    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
        return PersistentSetX.iterate(times, seed, fn);
    }

    @Override
    public <T> FluentCollectionX<T> generate(int times, Supplier<T> fn) {
        return PersistentSetX.generate(times, fn);
    }

    @Override
    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
        return PersistentSetX.unfold(seed, unfolder);
    }

}
