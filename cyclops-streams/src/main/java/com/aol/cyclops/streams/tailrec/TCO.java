package com.aol.cyclops.streams.tailrec;

import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

@Wither
@AllArgsConstructor
public class TCO<T> {
	@Getter
	private final Stream<T> stream;
	
	public TailRec<T> prepend(T value){
		return  new TailRec<>(stream(new SpliteratorModule.TCOPrependOperator<T>(stream.spliterator(), value)));
	}
	private <U> Stream<U> stream(Spliterator<U> spliterator){
		return StreamSupport.stream(spliterator, false);
	}
}
