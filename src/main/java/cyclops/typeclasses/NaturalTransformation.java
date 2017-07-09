package cyclops.typeclasses;

import com.aol.cyclops2.hkt.Higher;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.function.Fn1;
import cyclops.typeclasses.functions.FunctionK;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface NaturalTransformation<W1,W2>{

    <T> Higher<W2, T> apply(Higher<W1, T> a) ;


    default <T> FunctionK<W1,W2,T> asFunctionK(){
        return in->apply(in);
    }
    default <W3> NaturalTransformation<W1, W3> andThen(NaturalTransformation<W2, W3> after) {
        return new NaturalTransformation<W1, W3>(){
            @Override
            public <T> Higher<W3, T> apply(Higher<W1, T> a) {
                return after.apply(NaturalTransformation.this.apply(a));
            }
        };
    }
    default <W3> NaturalTransformation<W3, W2> compose(NaturalTransformation<W3, W1> before) {
        return new NaturalTransformation<W3, W2>(){
            @Override
            public <T> Higher<W2, T> apply(Higher<W3, T> a) {
                return NaturalTransformation.this.apply(before.apply(a));
            }
        };
    }
    static <T> NaturalTransformation<T, T> identity() {
        return new NaturalTransformation<T, T>() {
            @Override
            public <T1> Higher<T, T1> apply(Higher<T, T1> a) {
                return a;
            }
        };
    }
}