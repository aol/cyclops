package com.aol.cyclops.functions.collections.extensions.standard;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.QueueX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest;

public class SetXTest extends AbstractCollectionXTest{

	@Override
	public <T> FluentCollectionX<T> of(T... values) {
		return SetX.of(values);
	}

	@Test
	public void onEmptySwitch(){
	        assertThat(SetX.empty().onEmptySwitch(()->SetX.of(1,2,3)),equalTo(SetX.of(1,2,3)));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.functions.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> FluentCollectionX<T> empty() {
		return SetX.empty();
	}
	
	 @Override
	    public FluentCollectionX<Integer> range(int start, int end) {
	        return SetX.range(start, end);
	    }
	    @Override
	    public FluentCollectionX<Long> rangeLong(long start, long end) {
	        return SetX.rangeLong(start, end);
	    }
	    @Override
	    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
	       return SetX.iterate(times, seed, fn);
	    }
	    @Override
	    public <T> FluentCollectionX<T> generate(int times,  Supplier<T> fn) {
	       return SetX.generate(times, fn);
	    }
	    @Override
	    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
	       return SetX.unfold(seed, unfolder);
	    }
}
