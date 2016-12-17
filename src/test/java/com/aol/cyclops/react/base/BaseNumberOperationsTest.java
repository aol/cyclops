package com.aol.cyclops.react.base;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.types.futurestream.LazyFutureStream;
public abstract class BaseNumberOperationsTest {
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
		assertThat(of(1,2,3,4).foldFuture(s->s.sumInt(i->i),exec).get(),
				equalTo(10));
	}
	@Test
	public void sumDouble(){
		assertThat(of(1.0,2.0,3.0,4.0).futureOperations(exec).sumDouble(i->i).join(),
				equalTo(10.0));
	}
	@Test
	public void sumLong(){
		assertThat(of(1l,2l,3l,4l).futureOperations(exec).sumLong(i->i).join(),
				equalTo(10l));
	}
	@Test
	public void maxInt(){
		assertThat(of(1,2,3,4).futureOperations(exec).maxInt(i->i).join().getAsInt(),
				equalTo(4));
	}
	@Test
	public void maxDouble(){
		assertThat(of(1.0,2.0,3.0,4.0).futureOperations(exec).maxDouble(i->i).join().getAsDouble(),
				equalTo(4.0));
	}
	@Test
	public void maxLong(){
		assertThat(of(1l,2l,3l,4l).futureOperations(exec).maxLong(i->i).join().getAsLong(),
				equalTo(4l));
	}
	@Test
	public void minInt(){
		assertThat(of(1,2,3,4).futureOperations(exec).minInt(i->i).join().getAsInt(),
				equalTo(1));
	}
	@Test
	public void minDouble(){
		assertThat(of(1.0,2.0,3.0,4.0).futureOperations(exec).minDouble(i->i).join().getAsDouble(),
				equalTo(1.0));
	}
	@Test
	public void minLong(){
		assertThat(of(1l,2l,3l,4l).futureOperations(exec).minLong(i->i).join().getAsLong(),
				equalTo(1l));
	}
	@Test
	public void averageInt(){
		assertThat(of(1,2,3,4).futureOperations(exec).averageInt(i->i).join().getAsDouble(),
				equalTo(2.5));
	}
	@Test
	public void averageDouble(){
		assertThat(of(1.0,2.0,3.0,4.0).futureOperations(exec).averageDouble(i->i).join().getAsDouble(),
				equalTo(2.5));
	}
	@Test
	public void averageLong(){
		assertThat(of(1l,2l,3l,4l).futureOperations(exec).averageLong(i->i).join().getAsDouble(),
				equalTo(2.5));
	}
	@Test
	public void summaryStatsInt(){
		assertThat(of(1,2,3,4).futureOperations(exec).summaryStatisticsInt(i->i).join().getSum(),
				equalTo(10L));
	}
	@Test
	public void summaryStatsDouble(){
		assertThat(of(1.0,2.0,3.0,4.0).foldFuture(s->s.mapToDouble(e->e)
						                              .summaryStatistics(),exec).get().getSum(),
				equalTo(10.0));
		assertThat(of(1.0,2.0,3.0,4.0).foldDouble(exec,i->i,s->s.summaryStatistics()).get().getSum(),
				equalTo(10.0));
	}
	@Test
	public void summaryStatsLong(){
		assertThat(of(1l,2l,3l,4l).futureOperations(exec).summaryStatisticsLong(i->i).join().getSum(),
				equalTo(10l));
	}
	
}
