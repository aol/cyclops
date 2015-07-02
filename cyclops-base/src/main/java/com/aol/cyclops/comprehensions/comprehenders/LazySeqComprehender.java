package com.aol.cyclops.comprehensions.comprehenders;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.aol.cyclops.lambda.api.Comprehender;
import com.aol.cyclops.lambda.monads.ComprehenderSelector;
import com.nurkiewicz.lazyseq.LazySeq;

public class LazySeqComprehender implements Comprehender<LazySeq> {

	@Override
	public Object map(LazySeq t, Function fn) {
		return t.map(fn);
	}
	/**
	 * Wrapper around flatMap
	 * 
	 * @param t Monadic type being wrapped
	 * @param fn JDK Function to wrap
	 * @return Result of call to <pre>{@code t.flatMap( i -> fn.apply(i)); }</pre>
	 */
	@Override
	public LazySeq executeflatMap(LazySeq t, Function fn){
		return flatMap(t,input -> unwrapOtherMonadTypes(this,fn.apply(input)));
	}
	@Override
	public LazySeq flatMap(LazySeq t, Function fn) {
		return t.flatMap(fn);
	}

	@Override
	public LazySeq of(Object o) {
		return LazySeq.of(o);
	}

	@Override
	public LazySeq empty() {
		return LazySeq.of();
	}

	@Override
	public Class getTargetClass() {
		return LazySeq.class;
	}
	static LazySeq unwrapOtherMonadTypes(Comprehender<LazySeq> comp,Object apply){

		if(apply instanceof LazySeq)
			return (LazySeq)apply;
		

		if (apply instanceof Optional) {
			if (((Optional) apply).isPresent())
				return comp.of(((Optional) apply).get());
			return comp.empty();
		}
		
		if (apply instanceof Stream) {
			return LazySeq.of( ((Stream)apply).iterator());
		}
		if (apply instanceof IntStream) {
			return comp.of(((IntStream) apply).boxed().collect(Collectors.toList()));
		}
		if (apply instanceof DoubleStream) {
			return comp.of(((DoubleStream) apply).boxed().collect(Collectors.toList()));
		}
		if (apply instanceof LongStream) {
			return comp.of(((DoubleStream) apply).boxed().collect(Collectors.toList()));
		}
		if (apply instanceof CompletableFuture) {
			return comp.of(((CompletableFuture) apply).join());
		}

		return (LazySeq) new ComprehenderSelector().selectComprehender(apply)
				.resolveForCrossTypeFlatMap(comp,apply);

	}
}
