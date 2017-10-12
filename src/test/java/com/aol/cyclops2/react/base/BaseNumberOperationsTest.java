package com.aol.cyclops2.react.base;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import cyclops.reactive.FutureStream;
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
		assertThat(of(1,2,3,4).foldFuture(exec,s->s.sumInt(i->i)).orElse(-1),
				equalTo(10));
	}
	@Test
	public void sumDouble(){
		assertThat(of(1.0,2.0,3.0,4.0).foldFuture(exec,s->s.sumDouble(i->i)).orElse(-1d),
				equalTo(10.0));
	}
	@Test
	public void sumLong(){
		assertThat(of(1l,2l,3l,4l).foldFuture(exec,s->s.sumLong(i->i)).orElse(-1l),
				equalTo(10l));
	}
	@Test
	public void maxInt(){
		assertThat(of(1,2,3,4).foldFuture(exec,s->s.mapToInt(i->i).max()).toOptional().get().getAsInt(),
				equalTo(4));
	}

}
