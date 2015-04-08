package com.aol.cyclops.totallylazy;

import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Quadruple;
import com.googlecode.totallylazy.Quintuple;
import com.googlecode.totallylazy.Triple;
import javaslang.Functions.λ1;
import javaslang.Functions.λ2;
import javaslang.Tuple.Tuple2;
import javaslang.Tuple.Tuple3;
import javaslang.Tuple.Tuple4;
import javaslang.Tuple.Tuple5;
import javaslang.monad.Either;
import javaslang.monad.Either.Left;
import javaslang.monad.Either.Right;
import javaslang.monad.Option;

public class FromJavasLang {
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
