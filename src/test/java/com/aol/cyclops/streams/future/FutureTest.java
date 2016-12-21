package com.aol.cyclops.streams.future;
import static com.aol.cyclops.control.ReactiveSeq.of;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.jooq.lambda.tuple.Tuple.collectors;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.jooq.lambda.tuple.Tuple2;
import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.control.ReactiveSeq;



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
		assertThat(of(1,2,3,4,5).map(it -> it*100).foldFuture(s->s
					.reduce( (acc,next) -> acc+next),exec)
					.get(),is(1500));
	}
	@Test
	public void testMapReduceSeed(){
		assertThat(of(1,2,3,4,5).map(it -> it*100)
				.foldFuture(s->s.reduce( 50,(acc,next) -> acc+next),exec)
				,is(1550));
	}
	
	

	
}