package com.aol.cyclops.javaslang;


import java.util.stream.Collector;
import java.util.stream.Stream;

import javaslang.collection.List;

import org.jooq.lambda.Seq;

import com.aol.simple.react.stream.lazy.LazyFutureStream;
import com.google.common.collect.FluentIterable;
import com.nurkiewicz.lazyseq.LazySeq;


public class JavasLangConverter {
	
	
	
	public static class ToStream { 
		
		public static <T> FluentIterable<T> toFluentIterable(javaslang.collection.Stream<T> s){
			return FluentIterable.from(s);
		}
		public static <T> Stream<T> toStream(javaslang.collection.Stream<T> s){
			return Seq.seq(s.iterator());
		}
		public static <T> Seq<T> toJooλ(javaslang.collection.Stream<T> s){
			return Seq.seq(s.iterator());
		}
		public static <T> LazyFutureStream<T> toFutureStream(javaslang.collection.Stream<T> s){
			return LazyFutureStream.futureStream(s.iterator());
		}
		
		public static <T> javaslang.collection.Stream<T> toJavasLang(javaslang.collection.Stream<T> s) {
			return javaslang.collection.Stream.of(s.iterator());
		}
		public static <T> LazySeq<T> toLazySeq(javaslang.collection.Stream<T> s){
			return LazySeq.of(s.iterator());
		}
		
		public static <T> fj.data.Stream<T> toFunctionalJavaStream(javaslang.collection.Stream<T> s){
			return fj.data.Stream.iterableStream(s);
		}
	}
	
	public static class Collectors{
		
		
		 public static <T> Collector<T, ?, javaslang.collection.List<T>> toList() {
			
			return  Collector.<T,javaslang.collection.List<T>>of(
					List.Nil::instance,
					(list,val)->list.append(val),
                      (left, right) -> left.appendAll(right) 
				    );
		        
		    }
	}
	
}
