package com.aol.cyclops.react.base;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.types.futurestream.LazyFutureStream;
public abstract class BaseLazyNumberOperationsTest {
	abstract protected <U> LazyFutureStream<U> of(U... array);
	abstract protected <U> LazyFutureStream<U> ofThread(U... array);
	abstract protected <U> LazyFutureStream<U> react(Supplier<U>... array);
	LazyFutureStream<Integer> empty;
	LazyFutureStream<Integer> nonEmpty;
	private static final Executor exec = Executors.newFixedThreadPool(1);

	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);
		
	}
	
	@Test
	public void sumInt(){
		assertThat(of(1,2,3,4).lazyOperations().sumInt(i->i).get(),
				equalTo(10));
	}
	@Test
	public void sumDouble(){
		assertThat(of(1.0,2.0,3.0,4.0).lazyOperations().sumDouble(i->i).get(),
				equalTo(10.0));
	}
	@Test
	public void sumLong(){
		assertThat(of(1l,2l,3l,4l).lazyOperations().sumLong(i->i).get(),
				equalTo(10l));
	}
	@Test
	public void maxInt(){
		assertThat(of(1,2,3,4).lazyOperations().maxInt(i->i).get().getAsInt(),
				equalTo(4));
	}
	@Test
	public void maxDouble(){
		assertThat(of(1.0,2.0,3.0,4.0).lazyOperations().maxDouble(i->i).get().getAsDouble(),
				equalTo(4.0));
	}
	@Test
	public void maxLong(){
		assertThat(of(1l,2l,3l,4l).lazyOperations().maxLong(i->i).get().getAsLong(),
				equalTo(4l));
	}
	@Test
	public void minInt(){
		assertThat(of(1,2,3,4).lazyOperations().minInt(i->i).get().getAsInt(),
				equalTo(1));
	}
	@Test
	public void minDouble(){
		assertThat(of(1.0,2.0,3.0,4.0).lazyOperations().minDouble(i->i).get().getAsDouble(),
				equalTo(1.0));
	}
	@Test
	public void minLong(){
		assertThat(of(1l,2l,3l,4l).lazyOperations().minLong(i->i).get().getAsLong(),
				equalTo(1l));
	}
	@Test
	public void averageInt(){
		assertThat(of(1,2,3,4).lazyOperations().averageInt(i->i).get().getAsDouble(),
				equalTo(2.5));
	}
	@Test
	public void averageDouble(){
		assertThat(of(1.0,2.0,3.0,4.0).lazyOperations().averageDouble(i->i).get().getAsDouble(),
				equalTo(2.5));
	}
	@Test
	public void averageLong(){
		assertThat(of(1l,2l,3l,4l).lazyOperations().averageLong(i->i).get().getAsDouble(),
				equalTo(2.5));
	}
	@Test
	public void summaryStatsInt(){
		assertThat(of(1,2,3,4).lazyOperations().summaryStatisticsInt(i->i).get().getSum(),
				equalTo(10L));
	}
	@Test
	public void summaryStatsDouble(){
		assertThat(of(1.0,2.0,3.0,4.0).lazyOperations()
				.summaryStatisticsDouble(i->i).get().getSum(),
				equalTo(10.0));
	}
	@Test
	public void summaryStatsLong(){
		assertThat(of(1l,2l,3l,4l).lazyOperations().summaryStatisticsLong(i->i).get().getSum(),
				equalTo(10l));
	}
	
}
