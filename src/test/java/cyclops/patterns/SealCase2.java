package cyclops.patterns;

import java.util.function.Function;

public interface SealCase2<T1,T2> {

    public <R> R match(Function<T1,R> fn1, Function<T2,R> fn2);

}
