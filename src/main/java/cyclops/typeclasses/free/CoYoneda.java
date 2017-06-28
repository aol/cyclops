package cyclops.typeclasses.free;

import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.hkt.Higher2;
import com.aol.cyclops2.hkt.Higher3;
import cyclops.monads.Witness;
import cyclops.monads.Witness.coYoneda;
import cyclops.typeclasses.functor.Functor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.function.Function;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CoYoneda<F, T, R> implements Higher3<coYoneda, F, T, R> {



    private final Function<? super T,? extends R> function;
    private final Higher<F, T> higher;

    public static <F,T,R> CoYoneda<F,T,R> of(Function<? super T,? extends R> fn, Higher<F,T> higher){
        return new CoYoneda<>(fn,higher);
    }
    public Yoneda<F, R> toYoneda(Functor<F> functor) {
        return new Yoneda<F, R>() {
            public <R2> Higher<F, R2> apply(Function<? super R, ? extends R2> f){
                Higher<F, ? extends R2> local = CoYoneda.this.map(f).run(functor);
                return (Higher<F,R2>)local;
            }
        };
    }

    public static <F,T,R> CoYoneda<F,T,R> narrowK3(Higher3<coYoneda, F, T, R> higher){
        return (CoYoneda<F,T,R>)higher;
    }
    public static <F,T,R> CoYoneda<F,T,R> narrowK( Higher<Higher2<coYoneda, F, T>, R>  higher){
        return (CoYoneda<F,T,R>)higher;
    }

    public Higher<F, R> run(Functor<F> functor) {
        return functor.map(function, higher);
    }

    public <R2> CoYoneda<F, T, R2> map(Function<? super R, ? extends R2> f){
        return new CoYoneda<F, T, R2>(i->f.apply(function.apply(i)), higher);
    }
    
    public static class Instances{
        
        public <F, T1, R> Functor<Higher2<coYoneda, F, T1>> functor(){
            return new Functor<Higher2<coYoneda, F, T1>>() {
                @Override
                public <T, R> Higher<Higher2<coYoneda, F, T1>, R> map(Function<? super T, ? extends R> fn, Higher<Higher2<coYoneda, F, T1>, T> ds) {
                    return ds.convert(CoYoneda::narrowK).map(fn);
                }
            };
        }
    }


}

