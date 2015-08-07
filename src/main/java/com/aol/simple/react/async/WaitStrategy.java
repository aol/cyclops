package com.aol.simple.react.async;

import java.util.function.Predicate;
import java.util.function.Supplier;

public interface WaitStrategy <T>{

	public static interface Takeable<T>{
		public  T take()  throws InterruptedException;
	}
	public static interface Offerable{
		public boolean offer() throws InterruptedException;
	}
	
	public  T take(Takeable<T> t) throws InterruptedException;
	public boolean offer(Offerable o) throws InterruptedException;
}
