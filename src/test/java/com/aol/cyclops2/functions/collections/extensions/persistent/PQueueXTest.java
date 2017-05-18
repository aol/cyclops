package com.aol.cyclops2.functions.collections.extensions.persistent;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import cyclops.collections.immutable.PersistentQueueX;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops2.functions.collections.extensions.AbstractCollectionXTest;

public class PQueueXTest extends AbstractCollectionXTest{

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
                   .single(),equalTo(6));
        
    }
	/* (non-Javadoc)
	 * @see com.aol.cyclops2.function.collections.extensions.AbstractCollectionXTest#empty()
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
	    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
	       return PersistentQueueX.unfold(seed, unfolder);
	    }
}
