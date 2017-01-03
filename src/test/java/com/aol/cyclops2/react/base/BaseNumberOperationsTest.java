package com.aol.cyclops2.react.base;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import cyclops.stream.FutureStream;
import org.junit.Before;
import org.junit.Test;

public abstract class BaseNumberOperationsTest {
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
		assertThat(of(1,2,3,4).foldFuture(s->s.sumInt(i->i),exec).get(),
				equalTo(10));
	}
	@Test
	public void sumDouble(){
		assertThat(of(1.0,2.0,3.0,4.0).foldFuture(s->s.sumDouble(i->i),exec).get(),
				equalTo(10.0));
	}
	@Test
	public void sumLong(){
		assertThat(of(1l,2l,3l,4l).foldFuture(s->s.sumLong(i->i),exec).get(),
				equalTo(10l));
	}
	@Test
	public void maxInt(){
		assertThat(of(1,2,3,4).foldFuture(s->s.mapToInt(i->i).max(),exec).get().getAsInt(),
				equalTo(4));
	}

}
