package com.aol.simple.react.stream;

import java.util.concurrent.ExecutorService;

import lombok.AllArgsConstructor;
import lombok.experimental.Builder;

import com.nurkiewicz.asyncretry.RetryExecutor;

@AllArgsConstructor
@Builder
public class FutureStreamBuilder {

	private final ExecutorService executor;
	private final RetryExecutor retrier;
	private final Boolean eager;
	
}
