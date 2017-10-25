package cyclops.typeclasses.free;

import com.aol.cyclops2.hkt.Higher;
import cyclops.monads.Witness.yoneda;
import cyclops.typeclasses.functor.Functor;

import java.util.function.Function;

public interface Yoneda<U,T> extends Higher<Higher<yoneda, U>, T> {



    public static <U,T> Yoneda<U,T> of(Higher<U,T> hkt, Functor<U> functor){
        return new Yoneda<U,T>(){
            @Override
            public <R> Higher<U, R> apply(Function<? super T, ? extends R> inner) {
               return functor.map(inner,hkt);
            }
        };


    }

    <R> Higher<U, R> apply(Function<? super T, ? extends R> fn);

    default Higher<U,T> run(){
        return apply(a->a);
    }

    default <R> Yoneda<U,R> map(Function<? super T, ? extends R> outer, Functor<U> functor){
        return new Yoneda<U,R>(){
            @Override
            public <R1> Higher<U, R1> apply(Function<? super R, ? extends R1> inner) {
                return Yoneda.this.apply(i->inner.apply(outer.apply(i)));
            }
        };
    }
}