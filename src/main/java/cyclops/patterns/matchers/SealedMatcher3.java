package cyclops.patterns.matchers;


import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor
public class SealedMatcher3<X,T1 extends X,T2 extends X, T3 extends X> {


    private final X inst;
    private final Class<T1> classA;
    private final Class<T2> classB;
    private final Class<T3> classC;


    public <R> R match(Function<? super T1,? extends R> fn1, Function<? super T2,? extends R> fn2, Function<? super T3,? extends R> fn3){
        if(inst.getClass().isAssignableFrom(classA)){
            return fn1.apply((T1)inst);
        }else  if(inst.getClass().isAssignableFrom(classB)){
         return    fn2.apply((T2)inst);
        }
        return   fn3.apply((T3)inst);
    }
}
