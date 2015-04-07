package com.aol.cyclops;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;

import com.aol.simple.react.stream.lazy.LazyFutureStream;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.collections.PersistentList;
import com.nurkiewicz.lazyseq.LazySeq;

public class TotallyLazyConverter {

	static class Streams { 
		
		public static <T> Stream<T> toStream(Sequence<T> s){
			return Seq.seq(s);
		}
		
		public static <T> FluentIterable<T> toFluentIterable(Sequence<T> s){
			return FluentIterable.from(s);
		}
		public static <T> Seq<T> toJooλ(Sequence<T> s){
			return Seq.seq(s);
		}
		public static <T> LazyFutureStream<T> toFutureStream(Sequence<T> s){
			return LazyFutureStream.futureStream(s);
		}
		
		public static <T> javaslang.collection.Stream<T> toJavasLang(Sequence<T> s){
			return javaslang.collection.Stream.of(s);
		}
		
		public static <T> LazySeq<T> toLazySeq(Sequence<T> s){
			return LazySeq.of(s);
		}
		
		public static <T> fj.data.Stream<T> toFunctionalJavaStream(Sequence<T> s){
			return fj.data.Stream.iterableStream(s);
		}
	}
	
	static class Collectors{
		
		
		 public static <T> Collector<T, ?, PersistentList<T>> toList() {
			
			return  Collector.<T,PersistentList<T>>of(
					 com.googlecode.totallylazy.collections.PersistentList.constructors::empty,
				      List::add,
                      (left, right) -> PersistentList.constructors.list(Iterables.concat(left,right)) 
				    );
		        
		    }
	}
	
}
