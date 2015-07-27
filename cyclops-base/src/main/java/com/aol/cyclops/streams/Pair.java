package com.aol.cyclops.streams;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Value;


@Value @AllArgsConstructor
public class Pair<T1,T2> implements Iterable{
	public final T1 v1;
	public final T2 v2;
	public Pair(List list){
		v1 = (T1)list.get(0);
		v2 = (T2)list.get(1);
	}
	public T1 _1(){
		return v1;
	}
	public T2 _2(){
		return v2;
	}
	@Override
	public Iterator iterator() {
		return Arrays.asList(v1,v2).iterator();
	}
}