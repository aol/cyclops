package com.aol.cyclops.matcher;

import java.util.Arrays;
import java.util.Iterator;

import lombok.Value;

@Value
public class Two<T1,T2> implements Iterable {
	public final T1 v1;
	public final T2 v2;
	
	public static <T1,T2> Two<T1,T2> tuple(T1 t1, T2 t2){
		return new Two<>(t1,t2);
	}

	@Override
	public Iterator iterator() {
		return Arrays.asList(v1,v2).iterator();
	}
}
