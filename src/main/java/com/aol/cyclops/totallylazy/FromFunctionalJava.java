package com.aol.cyclops.totallylazy;

import fj.*;
import javaslang.Functions.λ1;
import javaslang.Functions.λ2;
import javaslang.Tuple.*;
import javaslang.monad.Either;
import javaslang.monad.Either.Left;
import javaslang.monad.Either.Right;
import javaslang.monad.Option;


public class FromFunctionalJava {
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