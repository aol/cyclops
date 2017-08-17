package cyclops.patterns.matchers;


import lombok.AllArgsConstructor;

import java.util.function.Function;
import java.util.function.Supplier;

@AllArgsConstructor
public class SealedMatcher1Or<X,T1 extends X> {


    private final X inst;
    private final Class<T1> classA;



    public <R> R match(Function<? super T1,? extends R> fn1, Supplier<? extends R> fn2){
        if(inst.getClass().isAssignableFrom(classA)){
            return fn1.apply((T1)inst);
        }

        return   fn2.get();
    }
}
