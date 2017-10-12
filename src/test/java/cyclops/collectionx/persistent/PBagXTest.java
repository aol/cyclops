package cyclops.collectionx.persistent;

import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collectionx.AbstractCollectionXTest;
import cyclops.collectionx.immutable.BagX;
import cyclops.control.Option;
import cyclops.reactive.Spouts;
import cyclops.data.tuple.Tuple2;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
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

public class PBagXTest extends AbstractCollectionXTest {

    AtomicLong counter = new AtomicLong(0);
    @Before
    public void setup(){

        counter = new AtomicLong(0);
    }
    @Test
    public void asyncTest() throws InterruptedException {
        Spouts.async(Stream.generate(()->"next"), Executors.newFixedThreadPool(1))
                .onePer(1, TimeUnit.MILLISECONDS)
                .take(1000)
                .to()
                .bagX(Evaluation.LAZY)
                .peek(i->counter.incrementAndGet())
                .materialize();

        long current = counter.get();
        Thread.sleep(400);
        assertTrue(current<counter.get());
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
		return BagX.of(values);
	}
	@Test
    public void onEmptySwitch(){
            assertThat(BagX.empty().onEmptySwitch(()-> BagX.of(1,2,3)),equalTo(BagX.of(1,2,3)));
    }
	@Test
    public void coflatMap(){
       assertThat(BagX.of(1,2,3)
                   .coflatMap(s->s.sumInt(i->i))
                   .singleUnsafe(),equalTo(6));
        
    }
	/* (non-Javadoc)
	 * @see com.aol.cyclops2.function.collections.extensions.AbstractCollectionXTest#zero()
	 */
	@Override
	public <T> FluentCollectionX<T> empty() {
		return BagX.empty();
	}
    @Override
    public FluentCollectionX<Integer> range(int start, int end) {
        return BagX.range(start, end);
    }
    @Override
    public FluentCollectionX<Long> rangeLong(long start, long end) {
        return BagX.rangeLong(start, end);
    }
    @Override
    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
       return BagX.iterate(times, seed, fn);
    }
    @Override
    public <T> FluentCollectionX<T> generate(int times,  Supplier<T> fn) {
       return BagX.generate(times, fn);
    }
    @Override
    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
       return BagX.unfold(seed, unfolder);
    }

}
