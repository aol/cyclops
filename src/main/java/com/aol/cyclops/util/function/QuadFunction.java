package com.aol.cyclops.util.function;

import java.util.function.Function;

import org.jooq.lambda.function.Function3;
import org.jooq.lambda.function.Function4;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Maybe;

public interface QuadFunction<T1, T2, T3, T4, R> extends Function4<T1,T2,T3,T4,R> {

    public R apply(T1 a, T2 b, T3 c, T4 d);
    
    default QuadFunction<T1, T2, T3, T4, Maybe<R>> lift(){
       return (s1,s2,s3,s4)-> Maybe.fromEval(Eval.later(()->apply(s1,s2,s3,s4)));
    }
    default Function<T2, Function<T3, Function<T4, R>>> apply(final T1 s) {
        return Curry.curry4(this)
                    .apply(s);
    }

    default Function<T3, Function<T4, R>> apply(final T1 s, final T2 s2) {
        return Curry.curry4(this)
                    .apply(s)
                    .apply(s2);
    }

    default Function<T4, R> apply(final T1 s, final T2 s2, final T3 s3) {
        return Curry.curry4(this)
                    .apply(s)
                    .apply(s2)
                    .apply(s3);
    }
}
