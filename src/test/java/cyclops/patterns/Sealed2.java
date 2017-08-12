package cyclops.patterns;


import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor
public class Sealed2<X,T1 extends X,T2 extends X> {


    X inst;
    Class<T1> classA;
    Class<T2> classB;


    public <R> R match(Function<T1,R> fn1, Function<T2,R> fn2){
        if(inst.getClass().isAssignableFrom(classA)){
            return fn1.apply((T1)this);
        }

        return    fn2.apply((T2)this);
    }
}
