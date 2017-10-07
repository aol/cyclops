package cyclops.typeclasses;


import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.function.Function1;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.collections.tuple.Tuple2;

import java.util.function.Function;

@AllArgsConstructor(access= AccessLevel.PRIVATE)
public class Cokleisli<W,T,R> implements Function1<Higher<W,T>,R>,
                                            Transformable<R>{

    public final Function1<Higher<W, T>,R> fn;


    @Override
    public R apply(Higher<W, T> a) {
        return fn.apply(a);
    }
    public <R1> Cokleisli<W,T,R1> map(Function<? super R, ? extends R1> mapper){
        return cokleisli(fn.andThen(mapper));
    }

    public <R2> Cokleisli<W,T, Tuple2<R, R2>> fanout(Cokleisli<W,T, R2> f2) {
        return product(f2);

    }

    public <R2> Cokleisli<W,T, Tuple2<R, R2>> product(Cokleisli<W,T, R2> f2) {
        return cokleisli(fn.product(f2));
    }



    public static <W,T,R> Cokleisli<W,T,R> cokleisli(Function<? super Higher<W,T>,? extends R> fn){
        return new Cokleisli<W,T, R>(Function1.narrow(fn));
    }
    public static <W,T,R> Cokleisli<W,T,R> of(Function<? super Higher<W,T>,? extends R> fn){
        return new Cokleisli<W,T, R>(Function1.narrow(fn));
    }




}
