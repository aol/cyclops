package cyclops.patterns;

import cyclops.patterns.matchers.SealedMatcher2;

import java.util.function.Function;

public interface Sealed2<T1,T2> {

    public <R> R match(Function<? super T1,? extends R> fn1, Function<? super T2,? extends R> fn2);

    public static <X,T1 extends X,T2 extends X> SealedMatcher2<X,T1,T2> matcher(X x, Class<T1> t1, Class<T2> t2){
        return new SealedMatcher2<>(x, t1, t2);
    }
}
