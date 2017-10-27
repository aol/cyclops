package com.oath.cyclops.streams.future;
import static cyclops.reactive.ReactiveSeq.of;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import static cyclops.data.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import cyclops.async.Future;
import org.junit.Before;
import org.junit.Test;

import cyclops.reactive.ReactiveSeq;



//see BaseSequentialSeqTest for in order tests
public  class FutureTest {

	ReactiveSeq<Integer> empty;
	ReactiveSeq<Integer> nonEmpty;
	static final Executor exec = Executors.newFixedThreadPool(1);
	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);

	}

	@Test
	public void testMapReduce(){
		assertThat(of(1,2,3,4,5).map(it -> it*100).foldFuture(exec,s->s
					.reduce( (acc,next) -> acc+next))
					.orElse(null),is(Optional.of(1500)));
	}
	@Test
	public void testMapReduceSeed(){
		assertThat(of(1,2,3,4,5).map(it -> it*100)
				.foldFuture(exec,s->s.reduce( 50,(acc,next) -> acc+next)).get()
				,is(Future.ofResult(1550).get()));
	}




}
