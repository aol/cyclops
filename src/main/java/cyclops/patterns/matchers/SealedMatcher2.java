package cyclops.patterns.matchers;


import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor
public class SealedMatcher2<X,T1 extends X,T2 extends X> {


    private final X inst;
    private final Class<T1> classA;
    private final Class<T2> classB;


    public <R> R match(Function<? super T1,? extends R> fn1, Function<? super T2,? extends R> fn2){
        if(inst.getClass().isAssignableFrom(classA)){
            return fn1.apply((T1)inst);
        }

        return    fn2.apply((T2)inst);
    }
}
