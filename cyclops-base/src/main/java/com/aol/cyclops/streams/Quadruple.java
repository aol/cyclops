package com.aol.cyclops.streams;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value @AllArgsConstructor
public class Quadruple<T1,T2,T3,T4> {
	public final T1 v1;
	public final T2 v2;
	public final T3 v3;
	public final T4 v4;
	public Quadruple(List list){
		v1 = (T1)list.get(0);
		v2 = (T2)list.get(1);
		v3 = (T3)list.get(2);
		v4 = (T4)list.get(3);
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
	public T4 _4(){
		return v4;
	}
}
