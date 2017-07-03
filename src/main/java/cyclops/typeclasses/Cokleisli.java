package cyclops.typeclasses;


import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.function.Fn1;

import cyclops.monads.WitnessType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jooq.lambda.tuple.Tuple2;

import java.util.function.Function;

@AllArgsConstructor(access= AccessLevel.PRIVATE)
public class Cokleisli<T,R,W extends WitnessType<W>> implements Fn1<Higher<W,T>,R>,
                                                                Transformable<R>{

    public final Fn1<Higher<W, T>,R> fn;


    @Override
    public R apply(Higher<W, T> a) {
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



    public static <T,R,W extends WitnessType<W>> Cokleisli<T,R,W> cokleisli(Function<? super Higher<W,T>,? extends R> fn){
        return new Cokleisli<T, R, W>(Fn1.narrow(fn));
    }




}
