package com.aol.cyclops2.functions.collections.extensions.persistent;

import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;
import cyclops.collections.immutable.BagX;
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
