package com.aol.cyclops.javaslang;
import javaslang.Function1;
import javaslang.Function2;
import javaslang.Function3;
import javaslang.Function4;
import javaslang.Function5;
import javaslang.Function6;
import javaslang.Function7;
import javaslang.Function8;
import javaslang.Tuple0;
import javaslang.Tuple1;
import javaslang.Tuple2;
import javaslang.Tuple3;
import javaslang.Tuple4;
import javaslang.Tuple5;
import javaslang.Tuple6;
import javaslang.Tuple7;
import javaslang.Tuple8;

public class Fromjooλ{
	public static <T,R>  Function1<T,R> f1(org.jooq.lambda.function.Function1<T,R> fn){
		return (t) -> fn.apply(t);
	}
	public static <T,X,R>  Function2<T,X,R> f2(org.jooq.lambda.function.Function2<T,X,R> fn){
		return (t,x) -> fn.apply(t,x);
	}
	public static <T1,T2,T3,R>  Function3<T1,T2,T3,R> f3(org.jooq.lambda.function.Function3<T1,T2,T3,R> fn){
		return (t1,t2,t3) -> fn.apply(t1,t2,t3);
	}
	public static <T1,T2,T3,T4,R>  Function4<T1,T2,T3,T4,R> f4(org.jooq.lambda.function.Function4<T1,T2,T3,T4,R> fn){
		return (t1,t2,t3,t4) -> fn.apply(t1,t2,t3,t4);
	}
	public static <T1,T2,T3,T4,T5,R>  Function5<T1,T2,T3,T4,T5,R> f5(org.jooq.lambda.function.Function5<T1,T2,T3,T4,T5,R> fn){
		return (t1,t2,t3,t4,t5) -> fn.apply(t1,t2,t3,t4,t5);
	}
	public static <T1,T2,T3,T4,T5,T6,R>  Function6<T1,T2,T3,T4,T5,T6,R> f6(org.jooq.lambda.function.Function6<T1,T2,T3,T4,T5,T6,R> fn){
		return (t1,t2,t3,t4,t5,t6) -> fn.apply(t1,t2,t3,t4,t5,t6);
	}
	public static <T1,T2,T3,T4,T5,T6,T7,R>  Function7<T1,T2,T3,T4,T5,T6,T7,R> f7(org.jooq.lambda.function.Function7<T1,T2,T3,T4,T5,T6,T7,R> fn){
		return (t1,t2,t3,t4,t5,t6,t7) -> fn.apply(t1,t2,t3,t4,t5,t6,t7);
	}
	public static <T1,T2,T3,T4,T5,T6,T7,T8,R>  Function8<T1,T2,T3,T4,T5,T6,T7,T8,R> f8(org.jooq.lambda.function.Function8<T1,T2,T3,T4,T5,T6,T7,T8,R> fn){
		return (t1,t2,t3,t4,t5,t6,t7,t8) -> fn.apply(t1,t2,t3,t4,t5,t6,t7,t8);
	}
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