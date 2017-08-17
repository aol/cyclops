package cyclops.patterns;

import cyclops.patterns.matchers.SealedMatcher1Or;
import cyclops.patterns.matchers.SealedMatcher2;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Sealed1Or<T1> {

    public <R> R match(Function<? super T1, ? extends R> fn1, Supplier<? extends R> s);

    default <R> R visit(Function<? super T1, ? extends R> fn1, Supplier<? extends R> s){
        return match(fn1,s);
    }

    public static <X,T1 extends X,T2 extends X> SealedMatcher1Or<X,T1> matcher(X x, Class<T1> t1){
        return new SealedMatcher1Or<>(x, t1);
    }
}
