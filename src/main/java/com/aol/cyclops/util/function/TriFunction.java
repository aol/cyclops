package com.aol.cyclops.util.function;

import java.util.function.Function;

import org.jooq.lambda.function.Function3;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.Maybe;

@FunctionalInterface
public interface TriFunction<S1, S2, S3, R> extends Function3<S1,S2,S3,R> {

    public R apply(S1 a, S2 b, S3 c);

    default Function3<S1, S2, S3, R> function3() {
        return this;
    }
    default TriFunction<S1, S2, S3, Maybe<R>> lift(){
        TriFunction<S1, S2, S3, R> host = this;
       return (s1,s2,s3)-> Maybe.fromEval(Eval.later(()->host.apply(s1,s2,s3)));
    }
    
    default Function<S2, Function<S3, R>> apply(final S1 s) {
        return Curry.curry3(this)
                    .apply(s);
    }

    default Function<S3, R> apply(final S1 s, final S2 s2) {
        return Curry.curry3(this)
                    .apply(s)
                    .apply(s2);
    }
}
