package cyclops.reactive.collections.standard;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
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
import cyclops.control.Option;
import cyclops.reactive.Spouts;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.collections.AbstractCollectionXTest;
import org.junit.Before;
import org.junit.Test;

import com.oath.cyclops.data.collections.extensions.FluentCollectionX;
import cyclops.reactive.collections.mutable.DequeX;
import cyclops.reactive.collections.mutable.ListX;

public class DequeXTest extends AbstractCollectionXTest {

	@Override
	public <T> FluentCollectionX<T> of(T... values) {
		return DequeX.of(values);
	}
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
				.dequeX(Evaluation.LAZY)
				.peek(i->counter.incrementAndGet())
				.materialize();

		long current = counter.get();
		Thread.sleep(400);
		assertTrue(current<counter.get());
	}

	@Test
    public void coflatMap(){
       assertThat(DequeX.of(1,2,3)
                   .coflatMap(s->s.sumInt(i->i))
                   .singleOrElse(null),equalTo(6));

    }
    @Test
    public void onEmptySwitch() {
        assertThat(DequeX.empty().onEmptySwitch(() -> DequeX.of(1, 2, 3)).toList(), equalTo(ListX.of(1, 2, 3)));
    }
	/* (non-Javadoc)
	 * @see com.oath.cyclops.function.collections.extensions.AbstractCollectionXTest#zero()
	 */
	@Override
	public <T> FluentCollectionX<T> empty() {
		return DequeX.empty();
	}
	 @Override
	    public FluentCollectionX<Integer> range(int start, int end) {
	        return DequeX.range(start, end);
	    }
	    @Override
	    public FluentCollectionX<Long> rangeLong(long start, long end) {
	        return DequeX.rangeLong(start, end);
	    }
	    @Override
	    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
	       return DequeX.iterate(times, seed, fn);
	    }
	    @Override
	    public <T> FluentCollectionX<T> generate(int times,  Supplier<T> fn) {
	       return DequeX.generate(times, fn);
	    }
	    @Override
	    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Option<Tuple2<T, U>>> unfolder) {
	       return DequeX.unfold(seed, unfolder);
	    }

    @Test
    public void compareDifferentSizes(){
        assertThat(empty().size(),not(equalTo(of(1).size())));
        assertThat(of(1).size(),not(equalTo(empty().size())));
        assertThat(of(1).size(),not(equalTo(of(1,2,3).size())));
    }

}
