package com.aol.simple.react.stream.traits;

import java.util.function.Supplier;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Continuation {

	private final Supplier<Continuation> remainderOfWorkToBeDone;
	
	public Continuation proceed(){
		return remainderOfWorkToBeDone.get();
	}

	public static Continuation empty() {
		
		return new Continuation( ()-> empty());
	}
}
