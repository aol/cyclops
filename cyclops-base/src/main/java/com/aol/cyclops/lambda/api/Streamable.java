package com.aol.cyclops.lambda.api;

import static com.aol.cyclops.lambda.api.AsDecomposable.asDecomposable;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Streamable<T> extends Iterable<T>{

	default Iterator<T> iterator(){
		return stream().iterator();
	}
	default  T getValue(){
		return (T)this;
	}
	
	default Stream<T> stream(){
		T streamable = getValue();
		if(streamable instanceof Stream)
			return (Stream)streamable;
		if(streamable instanceof Iterable)
			return StreamSupport.stream(((Iterable)streamable).spliterator(), false);
		return  new InvokeDynamic().stream(streamable).orElseGet( ()->
								(Stream)StreamSupport.stream(asDecomposable(streamable)
												.unapply()
												.spliterator(),
													false));
	}
}
