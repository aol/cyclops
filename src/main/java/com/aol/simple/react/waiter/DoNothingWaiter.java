package com.aol.simple.react.waiter;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DoNothingWaiter implements Consumer<CompletableFuture> {

	@Override
	public void accept(CompletableFuture t) {
		
		
	}

}
