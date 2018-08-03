package cyclops.monads.collections.mutable;

import com.oath.cyclops.anym.AnyMSeq;
import com.oath.cyclops.ReactiveConvertableSequence;
import com.oath.cyclops.async.QueueFactories;
import com.oath.cyclops.async.adapters.Queue;
import com.oath.cyclops.async.adapters.Topic;
import com.oath.cyclops.types.reactive.ReactiveSubscriber;
import cyclops.companion.Monoids;
import cyclops.control.Future;
import cyclops.data.Seq;
import cyclops.monads.Witness.*;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.oath.cyclops.react.ThreadPools;
import cyclops.futurestream.LazyReact;


import cyclops.monads.collections.AbstractAnyMSeqOrderedDependentTest;
import cyclops.futurestream.FutureStream;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import cyclops.monads.AnyM;
import cyclops.reactive.collections.mutable.ListX;


public class FutureStreamTest extends AbstractAnyMSeqOrderedDependentTest<futureStream> {

    int count = 0;




    public <T> FutureStream<T> ft(T... values) {
        return new LazyReact(ThreadPools.getCommonFreeThread()).async().of(values);
    }



    @Override
    public void combineNoOrderMonoid() {
        assertThat(of(1,2,3)
            .combine(Monoids.intSum,(a, b)->a.equals(b))
            .to(ReactiveConvertableSequence::converter).listX(),hasItem(2));
    }

    @Test
	public void materialize(){
		ListX<Integer> d= of(1, 2, 3).cycleUntil(next->count++==6).to(ReactiveConvertableSequence::converter).listX();
		System.out.println("D " + d);
		count =0;
		assertEquals(asList(1, 2,3, 1, 2,3),of(1, 2, 3).cycleUntil(next->count++==6).to(ReactiveConvertableSequence::converter).listX());
	}
	@Override
	public <T> AnyMSeq<futureStream,T> of(T... values) {
		return AnyM.fromFutureStream(new LazyReact(ThreadPools.getCommonFreeThread()).async().of(values));
	}

	@Override
	public <T> AnyMSeq<futureStream,T> empty() {
		return AnyM.fromFutureStream(new LazyReact().of());
	}

    @Test
    public void testParallelFlatMap() {
        assertThat(new LazyReact(ThreadPools.getCommonFreeThread()).fromStream(Stream.generate(() -> 1).limit(1000)).parallel()
                .map(a -> Thread.currentThread().getName()).toSet().size(), greaterThan(1));
    }

}

