package com.aol.simple.react.async.future;

import java.util.concurrent.Executor;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class FinalPipeline {
	public final Function[] functions;
	public final Executor[] executors;
	public static FinalPipeline empty() {
		return new FinalPipeline(new Function[0],new Executor[0]);
	}
}
