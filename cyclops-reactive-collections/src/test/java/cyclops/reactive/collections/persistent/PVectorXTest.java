package cyclops.reactive.collections.persistent;

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

import com.oath.cyclops.types.foldable.Evaluation;
import cyclops.reactive.collections.immutable.VectorX;
import cyclops.control.Option;
import cyclops.reactive.Spouts;
import cyclops.data.tuple.Tuple2;
import org.junit.Before;
import org.junit.Test;

import com.oath.cyclops.data.collections.extensions.FluentCollectionX;
import cyclops.reactive.collections.CollectionXTestsWithNulls;

public class PVectorXTest extends CollectionXTestsWithNulls{

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
				.vectorX(Evaluation.LAZY)
				.peek(i->counter.incrementAndGet())
				.materialize();

		long current = counter.get();
		Thread.sleep(400);
		assertTrue(current<counter.get());
	}

	@Override
	public <T> FluentCollectionX<T> of(T... values) {
		return VectorX.of(values);
	}
	@Test
    public void coflatMap(){
       assertThat(VectorX.of(1,2,3)
                   .coflatMap(s->s.sumInt(i->i))
                   .singleOrElse(null),equalTo(6));

    }
	@Test
    public void onEmptySwitch(){
            assertThat(VectorX.empty().onEmptySwitch(()-> VectorX.of(1,2,3)),equalTo(VectorX.of(1,2,3)));
    }
	/* (non-Javadoc)
	 * @see com.oath.cyclops.function.collections.extensions.AbstractCollectionXTest#zero()
	 */
	@Override
	public <T> FluentCollectionX<T> empty() {
		return VectorX.empty();
	}
	 @Override
	    public FluentCollectionX<Integer> range(int start, int end) {
	        return VectorX.range(start, end);
	    }
	    @Override
	    public FluentCollectionX<Long> rangeLong(long start, long end) {
	        return VectorX.rangeLong(start, end);
	    }
	    @Override
	    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
	       return VectorX.iterate(times, seed, fn);
	    }
	    @Override
	    public <T> FluentCollectionX<T> generate(int times,  Supplier<T> fn) {
	       return VectorX.generate(times, fn);
	    }
	    @Override
	    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
	       return VectorX.unfold(seed, unfolder);
	    }
}
