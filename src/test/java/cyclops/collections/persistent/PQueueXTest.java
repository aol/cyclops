package cyclops.collections.persistent;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collections.AbstractCollectionXTest;
import cyclops.collections.immutable.PersistentQueueX;
import cyclops.control.Option;
import cyclops.reactive.Spouts;
import cyclops.data.tuple.Tuple2;
import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;

public class PQueueXTest extends AbstractCollectionXTest {

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
				.persistentQueueX(Evaluation.LAZY)
				.peek(i->counter.incrementAndGet())
				.materialize();

		long current = counter.get();
		Thread.sleep(400);
		assertTrue(current<counter.get());
	}
	@Override
	public <T> FluentCollectionX<T> of(T... values) {
		return PersistentQueueX.of(values);
	}
	@Test
    public void onEmptySwitch(){
            assertThat(PersistentQueueX.empty().onEmptySwitch(()-> PersistentQueueX.of(1,2,3)).toList(), equalTo(PersistentQueueX.of(1,2,3).toList()));
    }
	
	@Test
    public void coflatMap(){
       assertThat(PersistentQueueX.of(1,2,3)
                   .coflatMap(s->s.sumInt(i->i))
                   .singleOrElse(null),equalTo(6));
        
    }
	/* (non-Javadoc)
	 * @see com.aol.cyclops2.function.collections.extensions.AbstractCollectionXTest#zero()
	 */
	@Override
	public <T> FluentCollectionX<T> empty() {
		return PersistentQueueX.empty();
	}
	 @Override
	    public FluentCollectionX<Integer> range(int start, int end) {
	        return PersistentQueueX.range(start, end);
	    }
	    @Override
	    public FluentCollectionX<Long> rangeLong(long start, long end) {
	        return PersistentQueueX.rangeLong(start, end);
	    }
	    @Override
	    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
	       return PersistentQueueX.iterate(times, seed, fn);
	    }
	    @Override
	    public <T> FluentCollectionX<T> generate(int times,  Supplier<T> fn) {
	       return PersistentQueueX.generate(times, fn);
	    }
	    @Override
	    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
	       return PersistentQueueX.unfold(seed, unfolder);
	    }
}
