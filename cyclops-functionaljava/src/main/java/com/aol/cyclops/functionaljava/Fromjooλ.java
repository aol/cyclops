package com.aol.cyclops.functionaljava;
import fj.P;
import fj.P1;
import fj.P2;
import fj.P3;
import fj.P4;
import fj.P5;
import fj.P6;
import fj.P7;
import fj.P8;

public class Fromjooλ{
	
	public static <T1> P1<T1> tuple(org.jooq.lambda.tuple.Tuple1<T1> t){
		return  P.lazy(()->t.v1);
	}
	public static <T1,T2> P2<T1,T2> tuple(org.jooq.lambda.tuple.Tuple2<T1,T2> t){
		return P.lazy(P.lazy(()->t.v1),P.lazy(()->t.v2));
	}
	public static <T1,T2,T3> P3<T1,T2,T3> tuple(org.jooq.lambda.tuple.Tuple3<T1,T2,T3> t){
		return P.p(t.v1,t.v2,t.v3);
	}
	public static <T1,T2,T3,T4> P4<T1,T2,T3,T4> tuple(org.jooq.lambda.tuple.Tuple4<T1,T2,T3,T4> t){
		return P.p(t.v1,t.v2,t.v3,t.v4);
	}
	public static <T1,T2,T3,T4,T5> P5<T1,T2,T3,T4,T5> tuple(org.jooq.lambda.tuple.Tuple5<T1,T2,T3,T4,T5> t){
		return P.p(t.v1,t.v2,t.v3,t.v4,t.v5);
	}
	public static <T1,T2,T3,T4,T5,T6> P6<T1,T2,T3,T4,T5,T6> tuple(org.jooq.lambda.tuple.Tuple6<T1,T2,T3,T4,T5,T6> t){
		return P.p(t.v1,t.v2,t.v3,t.v4,t.v5,t.v6);
	}
	public static <T1,T2,T3,T4,T5,T6,T7> P7<T1,T2,T3,T4,T5,T6,T7> tuple(org.jooq.lambda.tuple.Tuple7<T1,T2,T3,T4,T5,T6,T7> t){
		return P.p(t.v1,t.v2,t.v3,t.v4,t.v5,t.v6,t.v7);
	}
	public static <T1,T2,T3,T4,T5,T6,T7,T8> P8<T1,T2,T3,T4,T5,T6,T7,T8> tuple(org.jooq.lambda.tuple.Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> t){
		return P.p(t.v1,t.v2,t.v3,t.v4,t.v5,t.v6,t.v7,t.v8);
	}
	
	
}