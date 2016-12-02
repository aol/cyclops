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
import com.aol.cyclops.data.collections.extensions.persistent.PSetX;
import com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest;

public class PSetXTest extends AbstractCollectionXTest{

	@Override
	public <T> FluentCollectionX<T> of(T... values) {
		return PSetX.of(values);
	}
	@Test
    public void onEmptySwitch(){
            assertThat(PSetX.empty().onEmptySwitch(()->PSetX.of(1,2,3)),equalTo(PSetX.of(1,2,3)));
    }
	@Test
    public void coflatMap(){
       assertThat(PSetX.of(1,2,3)
                   .coflatMap(s->s.sum().get())
                   .single(),equalTo(6));
        
    }
	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> FluentCollectionX<T> empty() {
		return PSetX.empty();
	}
	 @Override
	    public FluentCollectionX<Integer> range(int start, int end) {
	        return PSetX.range(start, end);
	    }
	    @Override
	    public FluentCollectionX<Long> rangeLong(long start, long end) {
	        return PSetX.rangeLong(start, end);
	    }
	    @Override
	    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
	       return PSetX.iterate(times, seed, fn);
	    }
	    @Override
	    public <T> FluentCollectionX<T> generate(int times,  Supplier<T> fn) {
	       return PSetX.generate(times, fn);
	    }
	    @Override
	    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
	       return PSetX.unfold(seed, unfolder);
	    }
}
