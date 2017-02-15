package cyclops.monads;


import com.aol.cyclops2.types.Transformable;
import cyclops.control.Xor;
import cyclops.function.Fn1;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jooq.lambda.tuple.Tuple2;

import java.util.function.Function;

@AllArgsConstructor(access= AccessLevel.PRIVATE)
public class Cokleisli<T,R,W extends WitnessType<W>> implements Fn1<AnyM<W,T>,R>,
                                                                Transformable<R>{

    public final Fn1<AnyM<W, T>,R> fn;


    @Override
    public R apply(AnyM<W, T> a) {
        return fn.apply(a);
    }
    public <R1> Cokleisli<T,R1,W> map(Function<? super R, ? extends R1> mapper){
        return cokleisli(fn.andThen(mapper));
    }

    public <R2> Cokleisli<T, Tuple2<R, R2>,W> fanout(Cokleisli<T, R2, W> f2) {
        return product(f2);

    }

    public <R2> Cokleisli<T, Tuple2<R, R2>,W> product(Cokleisli<T, R2, W> f2) {
        return cokleisli(fn.product(f2));
    }



    public static <T,R,W extends WitnessType<W>> Cokleisli<T,R,W> cokleisli(Function<? super AnyM<W,T>,? extends R> fn){
        return new Cokleisli<T, R, W>(Fn1.narrow(fn));
    }




}
