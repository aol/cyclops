package cyclops.monads;


import com.oath.cyclops.types.functor.Transformable;
import cyclops.function.Function1;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple2;

import java.util.function.Function;

@AllArgsConstructor(access= AccessLevel.PRIVATE)
public class CokleisliM<T,R,W extends WitnessType<W>> implements Function1<AnyM<W,T>,R>,
                                                                Transformable<R>{

    public final Function1<AnyM<W, T>,R> fn;


    @Override
    public R apply(AnyM<W, T> a) {
        return fn.apply(a);
    }
    public <R1> CokleisliM<T,R1,W> mapFn(Function<? super R, ? extends R1> mapper){
        return cokleisli(fn.andThen(mapper));
    }

    public <R2> CokleisliM<T, Tuple2<R, R2>,W> fanout(CokleisliM<T, R2, W> f2) {
        return product(f2);

    }

    public <R2> CokleisliM<T, Tuple2<R, R2>,W> product(CokleisliM<T, R2, W> f2) {
        return cokleisli(fn.product(f2));
    }



    public static <T,R,W extends WitnessType<W>> CokleisliM<T,R,W> cokleisli(Function<? super AnyM<W,T>,? extends R> fn){
        return new CokleisliM<T, R, W>(Function1.narrow(fn));
    }




}
