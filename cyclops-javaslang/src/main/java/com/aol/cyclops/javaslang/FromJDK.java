package com.aol.cyclops.javaslang;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;

import javaslang.Function1;
import javaslang.Function2;
import javaslang.collection.List;
import javaslang.collection.LazyStream;
import javaslang.collection.Stream;
import javaslang.control.Option;

import com.aol.cyclops.invokedynamic.ExceptionSoftener;
import com.aol.simple.react.async.subscription.Continueable;

public class FromJDK<T,R> {
	
	public static <T,R>  Function1<T,R> f1(Function<T,R> fn){
		return (t) -> fn.apply(t);
	}
	public static <T,X,R>  Function2<T,X,R> f2(BiFunction<T,X,R> fn){
		return (t,x) -> fn.apply(t,x);
	}
	public static<T> Option<T> option(java.util.Optional<T> o){
		return Option.of(o.orElse(null));
	}
	public static<T> Stream<T> stream(java.util.stream.Stream<T> stream){
		return Stream.ofAll(()->stream.iterator());
	}
	
	public static<T> Stream<T> stream(java.util.stream.Stream<T> stream, Continueable  sub){
		
		Iterator i = stream.iterator();
		return Stream.ofAll(()-> {return new Iterator(){

			@Override
			public boolean hasNext() {
				return !sub.closed() 	
						&& i.hasNext();
			}

			@Override
			public Object next() {
				try{
					return i.next();
				}catch(Throwable t){
				
					throw ExceptionSoftener.throwSoftenedException(t);
				}
			}};});
	}
	
}
