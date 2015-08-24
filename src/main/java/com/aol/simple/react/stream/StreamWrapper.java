package com.aol.simple.react.stream;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import com.aol.simple.react.async.future.FastFuture;

public interface StreamWrapper {
	public StreamWrapper stream(Function<Stream<FastFuture>,Stream<FastFuture>> action);
	public StreamWrapper withNewStream(Stream<FastFuture> stream, BaseSimpleReact simple);
	public List<FastFuture> list();
	public Stream<FastFuture> stream();
	
	public StreamWrapper  withList(List<FastFuture> list);
	public StreamWrapper withStream(Stream<FastFuture> noType);
	
}
