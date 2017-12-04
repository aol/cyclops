package com.oath.cyclops.react.base;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import cyclops.reactive.FutureStream;
import org.junit.Before;
import org.junit.Test;
public abstract class BaseLazyNumberOperationsTest {
	abstract protected <U> FutureStream<U> of(U... array);
	abstract protected <U> FutureStream<U> ofThread(U... array);
	abstract protected <U> FutureStream<U> react(Supplier<U>... array);
	FutureStream<Integer> empty;
	FutureStream<Integer> nonEmpty;
	private static final Executor exec = Executors.newFixedThreadPool(1);

	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);

	}

	@Test
	public void sumInt(){
		assertThat(of(1,2,3,4).foldLazy(s->s.sumInt(i->i)).get(),
				equalTo(10));
	}

	@Test
	public void summaryStatsInt(){
		assertThat( of(1,2,3,4).foldLazy(s->s.mapToInt(i->i)
											.summaryStatistics().getSum()).get(),
				equalTo(10L));
	}
	@Test
	public void summaryStatsDouble(){
		assertThat(of(1.0,2.0,3.0,4.0).foldLazy(s->s.mapToDouble(i->i)
													.summaryStatistics().getSum()).get(),
				equalTo(10.0));
	}
	@Test
	public void summaryStatsLong(){
		assertThat(of(1l,2l,3l,4l).foldLazy(s->s.mapToLong(i->i).summaryStatistics().getSum()).get(),
				equalTo(10l));
	}

}
