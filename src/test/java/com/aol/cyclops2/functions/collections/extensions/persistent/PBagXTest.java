package com.aol.cyclops2.functions.collections.extensions.persistent;

import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;
import cyclops.collections.immutable.PBagX;
import com.aol.cyclops2.functions.collections.extensions.AbstractCollectionXTest;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class PBagXTest extends AbstractCollectionXTest{

	@Override
	public <T> FluentCollectionX<T> of(T... values) {
		return PBagX.of(values);
	}
	@Test
    public void onEmptySwitch(){
            assertThat(PBagX.empty().onEmptySwitch(()->PBagX.of(1,2,3)),equalTo(PBagX.of(1,2,3)));
    }
	@Test
    public void coflatMap(){
       assertThat(PBagX.of(1,2,3)
                   .coflatMap(s->s.sumInt(i->i))
                   .single(),equalTo(6));
        
    }
	/* (non-Javadoc)
	 * @see com.aol.cyclops2.function.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> FluentCollectionX<T> empty() {
		return PBagX.empty();
	}
    @Override
    public FluentCollectionX<Integer> range(int start, int end) {
        return PBagX.range(start, end);
    }
    @Override
    public FluentCollectionX<Long> rangeLong(long start, long end) {
        return PBagX.rangeLong(start, end);
    }
    @Override
    public <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn) {
       return PBagX.iterate(times, seed, fn);
    }
    @Override
    public <T> FluentCollectionX<T> generate(int times,  Supplier<T> fn) {
       return PBagX.generate(times, fn);
    }
    @Override
    public <U, T> FluentCollectionX<T> unfold(U seed, Function<? super U, Optional<Tuple2<T, U>>> unfolder) {
       return PBagX.unfold(seed, unfolder);
    }

}
