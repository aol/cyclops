package com.aol.cyclops.types.futurestream;

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
