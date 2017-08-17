package cyclops.patterns;

import cyclops.patterns.matchers.SealedMatcher2;
import cyclops.patterns.matchers.SealedMatcher3;

import java.util.function.Function;

public interface Sealed3<T1,T2,T3> {

    public <R> R match(Function<? super T1, ? extends R> fn1, Function<? super T2, ? extends R> fn2, Function<? super T3, ? extends R> fn3);

    default <R> R  visit(Function<? super T1, ? extends R> fn1, Function<? super T2, ? extends R> fn2, Function<? super T3, ? extends R> fn3){
        return match(fn1,fn2,fn3);
    }
    public static <X,T1 extends X,T2 extends X,T3 extends X> SealedMatcher3<X,T1,T2,T3> matcher(X x, Class<T1> t1, Class<T2> t2,Class<T3> t3){
        return new SealedMatcher3<>(x, t1, t2,t3);
    }
}
