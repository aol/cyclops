package com.aol.cyclops.guava;

import org.jooq.lambda.function.Function1;
import org.jooq.lambda.function.Function2;
import org.jooq.lambda.function.Function3;
import org.jooq.lambda.function.Function4;
import org.jooq.lambda.function.Function5;
import org.jooq.lambda.function.Function6;
import org.jooq.lambda.function.Function7;
import org.jooq.lambda.function.Function8;

import com.google.common.base.Function;

import fj.F;
import fj.F2;
import fj.F3;
import fj.F4;
import fj.F5;
import fj.F6;
import fj.F7;
import fj.F8;
import fj.P;
import fj.P1;
import fj.P2;
import fj.P3;
import fj.P4;
import fj.P5;
import fj.P6;
import fj.P7;
import fj.P8;

public class FromJooqLambda{
	public static <T,R>  Function<T,R> f1(Function1<T,R> fn){
		return (t) -> fn.apply(t);
	}
	
	
	
}