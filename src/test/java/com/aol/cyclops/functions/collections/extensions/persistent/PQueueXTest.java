package com.aol.cyclops.functions.collections.extensions.persistent;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops.data.collections.extensions.persistent.PQueueX;
import com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest;

public class PQueueXTest extends AbstractCollectionXTest{

	@Override
	public <T> FluentCollectionX<T> of(T... values) {
		return PQueueX.of(values);
	}
	@Test
    public void onEmptySwitch(){
            assertThat(PQueueX.empty().onEmptySwitch(()->PQueueX.of(1,2,3)).toList(), equalTo(PQueueX.of(1,2,3).toList()));
    }
	
	@Test
    public void coflatMap(){
       assertThat(PQueueX.of(1,2,3)
                   .coflatMap(s->s.sum().get())
                   .single(),equalTo(6));
        
    }
	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> FluentCollectionX<T> empty() {
		return PQueueX.empty();
	}
	 @Override
	    public FluentCollectionX<Integer> range(int start, int end) {
	        return PQueueX.range(start, end);
	    }
	    @Override
	    public FluentCollectionX<Long> rangeLong(long start, long end) {
	        return PQueueX.rangeLong(start, end);
	    }
	    @Override
	    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
	       return PQueueX.iterate(times, seed, fn);
	    }
	    @Override
	    public <T> FluentCollectionX<T> generate(int times,  Supplier<T> fn) {
	       return PQueueX.generate(times, fn);
	    }
	    @Override
	    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
	       return PQueueX.unfold(seed, unfolder);
	    }
}
