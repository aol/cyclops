package cyclops.collections.persistent;

import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collections.AbstractCollectionXTest;
import cyclops.collections.immutable.BagX;
import cyclops.stream.Spouts;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
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
    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
       return BagX.unfold(seed, unfolder);
    }

}
