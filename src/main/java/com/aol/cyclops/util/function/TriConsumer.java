package com.aol.cyclops.util.function;

import java.util.function.Consumer;
import java.util.function.Function;

public interface TriConsumer<S1, S2, S3> {

	public void accept(S1 a,S2 b,S3 c);
	
	default Function<S2,Consumer<S3>> apply(S1 s){
		return CurryConsumer.curryC3(this).apply(s);
	}
	default Consumer<S3> apply(S1 s, S2 s2){
		return CurryConsumer.curryC3(this).apply(s).apply(s2);
	}
}
