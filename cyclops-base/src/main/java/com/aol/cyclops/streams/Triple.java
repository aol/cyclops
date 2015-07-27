package com.aol.cyclops.streams;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class Triple<T1,T2,T3> {
	public final T1 v1;
	public final T2 v2;
	public final T3 v3;
	public Triple(List list){
		v1 = (T1)list.get(0);
		v2 = (T2)list.get(1);
		v3 = (T3)list.get(2);
	}
	public T1 _1(){
		return v1;
	}
	public T2 _2(){
		return v2;
	}
	public T3 _3(){
		return v3;
	}
}
