package com.aol.cyclops2.react.base;

import static java.util.stream.Collectors.joining;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static cyclops.data.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.function.Supplier;

import cyclops.stream.FutureStream;
import org.junit.Before;
import org.junit.Test;


//see BaseSequentialSeqTest for in order tests
public abstract class BaseSeqLazyTest {
	abstract protected <U> FutureStream<U> of(U... array);
	abstract protected <U> FutureStream<U> ofThread(U... array);
	abstract protected <U> FutureStream<U> react(Supplier<U>... array);
	FutureStream<Integer> empty;
	FutureStream<Integer> nonEmpty;

	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);
		
	}
	@Test
	public void testMax(){
		assertThat(of(1,2,3,4,5).foldLazy(s->s.max((t1,t2) -> t1-t2))
				.get(),is(Optional.of(5)));
	}

	protected Object value() {
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "jello";
	}
	protected int value2() {
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 200;
	}
	
	
	   

	    
	
	protected Object sleep(int i) {
		try {
			Thread.currentThread().sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
	}
	
}
