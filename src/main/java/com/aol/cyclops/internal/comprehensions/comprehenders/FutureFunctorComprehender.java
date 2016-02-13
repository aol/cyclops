package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.types.extensability.Comprehender;

public class FutureFunctorComprehender implements Comprehender<FutureW>{
	public Class getTargetClass(){
		return FutureW.class;
	}

	@Override
	public Object map(FutureW t, Function fn) {
		return t.map(fn);
	}

	@Override
	public FutureW flatMap(FutureW t, Function fn) {
		return t.flatMap(fn);
	}

	@Override
	public boolean instanceOfT(Object apply) {
		return apply instanceof FutureW;
	}

	@Override
	public FutureW of(Object o) {
		return FutureW.of(CompletableFuture.completedFuture(o));
	}

	@Override
	public FutureW empty() {
		return FutureW.of(new CompletableFuture());
	}
	

}
