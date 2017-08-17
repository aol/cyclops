package cyclops.patterns;

import cyclops.patterns.matchers.SealedMatcher4;
import cyclops.patterns.matchers.SealedMatcher5;

import java.util.function.Function;

public interface Sealed5<T1,T2,T3,T4,T5> {

    public <R> R match(Function<? super T1, ? extends R> fn1, Function<? super T2, ? extends R> fn2,
                       Function<? super T3, ? extends R> fn3, Function<? super T4, ? extends R> fn4,
                       Function<? super T4, ? extends R> fn5);

    default <R> R visit(Function<? super T1, ? extends R> fn1, Function<? super T2, ? extends R> fn2,
            Function<? super T3, ? extends R> fn3, Function<? super T4, ? extends R> fn4,
            Function<? super T4, ? extends R> fn5){
        return match(fn1,fn2,fn3,fn4,fn5);
    }

    public static <X,T1 extends X,T2 extends X,T3 extends X,T4 extends X,T5 extends X> SealedMatcher5<X,T1,T2,T3,T4,T5> matcher(X x, Class<T1> t1,
                                                                                                                                Class<T2> t2, Class<T3> t3,
                                                                                                                                Class<T4> t4, Class<T5> t5){
        return new SealedMatcher5<>(x, t1, t2,t3, t4, t5);
    }
}
