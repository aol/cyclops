package com.aol.cyclops;


import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import javaslang.Functions.λ1;
import javaslang.Functions.λ2;
import javaslang.Tuple.Tuple0;
import javaslang.Tuple.Tuple1;
import javaslang.Tuple.Tuple2;
import javaslang.Tuple.Tuple3;
import javaslang.Tuple.Tuple4;
import javaslang.Tuple.Tuple5;
import javaslang.Tuple.Tuple6;
import javaslang.Tuple.Tuple7;
import javaslang.Tuple.Tuple8;
import javaslang.collection.List;
import javaslang.monad.Either;
import javaslang.monad.Either.Left;
import javaslang.monad.Either.Right;
import javaslang.monad.Option;

import org.jooq.lambda.Seq;

import com.aol.simple.react.stream.lazy.LazyFutureStream;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Quadruple;
import com.googlecode.totallylazy.Quintuple;
import com.googlecode.totallylazy.Triple;
import com.nurkiewicz.lazyseq.LazySeq;

import fj.F;
import fj.F2;
import fj.P1;
import fj.P2;
import fj.P3;
import fj.P4;
import fj.P5;
import fj.P6;
import fj.P7;
import fj.P8;


public class JavasLangConverter {
	
	
	public static class FromJDK<T,R> {
		
		public static <T,R>  λ1<T,R> λ(Function<T,R> fn){
			return (t) -> fn.apply(t);
		}
		public static <T,X,R>  λ2<T,X,R> λ2(BiFunction<T,X,R> fn){
			return (t,x) -> fn.apply(t,x);
		}
		public static<T> Option<T> option(java.util.Optional<T> o){
			return Option.of(o.orElse(null));
		}
		
	}
	
	public static class FromGuava {
		public static <T,R>  λ1<T,R> λ(com.google.common.base.Function<T,R> fn){
			return (t) -> fn.apply(t);
		}
		public static<T> Option<T> option(Optional<T> o){
			return Option.of(o.orNull());
		}
	}
	public static class FromTotallyLazy {
		public static <T,R>  λ1<T,R> λ(com.googlecode.totallylazy.Function<T,R> fn){
			return (t) -> fn.apply(t);
		}
		public static <T,X,R>  λ2<T,X,R>  λ2(com.googlecode.totallylazy.Function2<T,X,R> fn){
			return (t,x) -> fn.apply(t,x);
		}
		
		public static<T> Option<T> option(com.googlecode.totallylazy.Option<T> o){
			return Option.of(o.getOrNull());
		}
		public static<L,R> Either<L,R> either(com.googlecode.totallylazy.Either<L,R> e){
			if(e.isLeft())
				return new Left(e.value()); 
			else
				return new Right(e.value());
		}
		
		public static <T1,T2> Tuple2<T1,T2> tuple(Pair<T1,T2> t){
			return new Tuple2(t._1(),t._2());
		}
		public static <T1,T2,T3> Tuple3<T1,T2,T3> tuple(Triple<T1,T2,T3> t){
			return new Tuple3(t.first(),t.second(),t.third());
		}
		public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> tuple(Quadruple<T1,T2,T3,T4> t){
			return new Tuple4(t.first(),t.second(),t.third(),t.fourth());
		}
		public static <T1,T2,T3,T4,T5> Tuple5<T1,T2,T3,T4,T5> tuple(Quintuple<T1,T2,T3,T4,T5> t){
			return new Tuple5(t.first(),t.second(),t.third(),t.fourth(),t.fifth());
		}
	}
	public static class FromFunctionalJava {
		public static <T,R>  λ1<T,R> λ(F<T,R> fn){
			return (t) -> fn.f(t);
		}
		public static <T,X,R>  λ2<T,X,R> λ2(F2<T,X,R> fn){
			return (t,x) -> fn.f(t,x);
		}
		public static<T> Option<T> option(fj.data.Option<T> o){
			if(o.isNone())
				return Option.of(null);
			return Option.of(o.valueE("failed"));
		}
		public static<L,R> Either<L,R> either(fj.data.Either<L,R> e){
			if(e.isLeft())
				return new Left(e.left().value()); 
			else
				return new Right(e.right().value());
		}
		
		public static <T1> Tuple1<T1> tuple(P1<T1> t){
			return new Tuple1(t._1());
		}
		public static <T1,T2> Tuple2<T1,T2> tuple(P2<T1,T2> t){
			return new Tuple2(t._1(),t._2());
		}
		public static <T1,T2,T3> Tuple3<T1,T2,T3> tuple(P3<T1,T2,T3> t){
			return new Tuple3(t._1(),t._2(),t._3());
		}
		public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> tuple(P4<T1,T2,T3,T4> t){
			return new Tuple4(t._1(),t._2(),t._3(),t._4());
		}
		public static <T1,T2,T3,T4,T5> Tuple5<T1,T2,T3,T4,T5> tuple(P5<T1,T2,T3,T4,T5> t){
			return new Tuple5(t._1(),t._2(),t._3(),t._4(),t._5());
		}
		public static <T1,T2,T3,T4,T5,T6> Tuple6<T1,T2,T3,T4,T5,T6> tuple(P6<T1,T2,T3,T4,T5,T6> t){
			return new Tuple6(t._1(),t._2(),t._3(),t._4(),t._5(),t._6());
		}
		public static <T1,T2,T3,T4,T5,T6,T7> Tuple7<T1,T2,T3,T4,T5,T6,T7> tuple(P7<T1,T2,T3,T4,T5,T6,T7> t){
			return new Tuple7(t._1(),t._2(),t._3(),t._4(),t._5(),t._6(),t._7());
		}
		public static <T1,T2,T3,T4,T5,T6,T7,T8> Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> tuple(P8<T1,T2,T3,T4,T5,T6,T7,T8> t){
			return new Tuple8(t._1(),t._2(),t._3(),t._4(),t._5(),t._6(),t._7(),t._8());
		}
		
	}
	public static class Fromjooλ{
		public static  Tuple0 tuple(org.jooq.lambda.tuple.Tuple0 t){
			return Tuple0.instance();
		}
		public static <T1> Tuple1<T1> tuple(org.jooq.lambda.tuple.Tuple1<T1> t){
			return new Tuple1(t.v1);
		}
		public static <T1,T2> Tuple2<T1,T2> tuple(org.jooq.lambda.tuple.Tuple2<T1,T2> t){
			return new Tuple2(t.v1,t.v2);
		}
		public static <T1,T2,T3> Tuple3<T1,T2,T3> tuple(org.jooq.lambda.tuple.Tuple3<T1,T2,T3> t){
			return new Tuple3(t.v1,t.v2,t.v3);
		}
		public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> tuple(org.jooq.lambda.tuple.Tuple4<T1,T2,T3,T4> t){
			return new Tuple4(t.v1,t.v2,t.v3,t.v4);
		}
		public static <T1,T2,T3,T4,T5> Tuple5<T1,T2,T3,T4,T5> tuple(org.jooq.lambda.tuple.Tuple5<T1,T2,T3,T4,T5> t){
			return new Tuple5(t.v1,t.v2,t.v3,t.v4,t.v5);
		}
		public static <T1,T2,T3,T4,T5,T6> Tuple6<T1,T2,T3,T4,T5,T6> tuple(org.jooq.lambda.tuple.Tuple6<T1,T2,T3,T4,T5,T6> t){
			return new Tuple6(t.v1,t.v2,t.v3,t.v4,t.v5,t.v6);
		}
		public static <T1,T2,T3,T4,T5,T6,T7> Tuple7<T1,T2,T3,T4,T5,T6,T7> tuple(org.jooq.lambda.tuple.Tuple7<T1,T2,T3,T4,T5,T6,T7> t){
			return new Tuple7(t.v1,t.v2,t.v3,t.v4,t.v5,t.v6,t.v7);
		}
		public static <T1,T2,T3,T4,T5,T6,T7,T8> Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> tuple(org.jooq.lambda.tuple.Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> t){
			return new Tuple8(t.v1,t.v2,t.v3,t.v4,t.v5,t.v6,t.v7,t.v8);
		}
		
		
	}
	
	
	
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
