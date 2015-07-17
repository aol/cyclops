package com.aol.cyclops.functionaljava;

import javaslang.Function1;
import javaslang.Function2;
import javaslang.Tuple1;
import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.Tuple4;
import javaslang.Tuple5;
import javaslang.Tuple6;
import javaslang.Tuple7;
import javaslang.Tuple8;
import javaslang.control.Either;
import javaslang.control.Left;
import javaslang.control.Option;
import javaslang.control.Right;
import fj.F;
import fj.F2;
import fj.P;
import fj.P1;
import fj.P2;
import fj.P3;
import fj.P4;
import fj.P5;
import fj.P6;
import fj.P7;
import fj.P8;


public class FromJavaslang {
	public static <T,R>  F<T,R> λ(Function1<T,R> fn){
		return (t) -> fn.apply(t);
	}
	public static <T,X,R>  F2<T,X,R> λ2(Function2<T,X,R> fn){
		return (t,x) -> fn.apply(t,x);
	}
	public static<T> fj.data.Option<T> option(Option<T> o){
		if(o.isEmpty())
			return fj.data.Option.none();
		return fj.data.Option.some(o.get());
	}
	public static<L,R> fj.data.Either<L,R> either(Either<L,R> e){
		if(e.isLeft())
			return fj.data.Either.left(e.left().get()); 
		else
			return fj.data.Either.right(e.right().get());
	}
	
	public static <T1> P1<T1> tuple(Tuple1<T1> t){
		return P.p(t._1);
	}
	public static <T1,T2> P2<T1,T2> tuple(Tuple2<T1,T2> t){
		return P.p(t._1,t._2);
	}
	public static <T1,T2,T3> P3<T1,T2,T3> tuple(Tuple3<T1,T2,T3> t){
		return P.p(t._1,t._2,t._3);
	}
	public static <T1,T2,T3,T4> P4<T1,T2,T3,T4> tuple(Tuple4<T1,T2,T3,T4> t){
		return P.p(t._1,t._2,t._3,t._4);
	}
	public static <T1,T2,T3,T4,T5> P5<T1,T2,T3,T4,T5> tuple(Tuple5<T1,T2,T3,T4,T5> t){
		return P.p(t._1,t._2,t._3,t._4,t._5);
	}
	public static <T1,T2,T3,T4,T5,T6> P6<T1,T2,T3,T4,T5,T6> tuple(Tuple6<T1,T2,T3,T4,T5,T6> t){
		return P.p(t._1,t._2,t._3,t._4,t._5,t._6);
	}
	public static <T1,T2,T3,T4,T5,T6,T7> P7<T1,T2,T3,T4,T5,T6,T7> tuple(Tuple7<T1,T2,T3,T4,T5,T6,T7> t){
		return P.p(t._1,t._2,t._3,t._4,t._5,t._6,t._7);
	}
	public static <T1,T2,T3,T4,T5,T6,T7,T8> P8<T1,T2,T3,T4,T5,T6,T7,T8> tuple(Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> t){
		return P.p(t._1,t._2,t._3,t._4,t._5,t._6,t._7,t._8);
	}
	
}