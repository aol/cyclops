package cyclops.monads;


import com.aol.cyclops2.types.Transformable;
import cyclops.control.either.Either;
import cyclops.function.Fn1;
import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor
public class Kleisli<T,R,W extends WitnessType<W>> implements Fn1<T,AnyM<W,R>>,
                                                                Transformable<R>{

    public final Fn1<T, AnyM<W, R>> fn;

    @Override
    public AnyM<W,R> apply(T a) {

        return fn.apply(a);
    }

    public <R1> Kleisli<T,R1,W> map(Function<? super R, ? extends R1> mapper){
        return kliesli(fn.andThen(am->am.map(mapper)));
    }

    public <R1> Kleisli<T,R1,W> flatMap(Function<? super R, ? extends AnyM<W,? extends R1>> mapper){
        return kliesli(fn.andThen(am->am.flatMapA(mapper)));
    }

    public <A> Kleisli<A,R,W> compose(Kleisli<A,T,W> kleisli) {
        return new Kleisli<A,R,W>(a -> kleisli.apply(a).flatMapA(this));
    }
    public <R2> Kleisli<T,R2,W> then(Kleisli<R,R2,W> kleisli) {

        return new Kleisli<T,R2,W>(t->this.apply(t)
                                    .flatMapA(kleisli));

    }

    public <T2,R2> Kleisli<Either<T, T2>, Either<R, R2>,W> merge(Kleisli<T2,R2,W> merge, W type) {
        Kleisli<T, Either<R, R2>, W> first = then(lift(Either::left, type));
        Kleisli<T2, Either<R, R2>, W> second = merge.then(lift(Either::right, type));
        return first.fanIn(second);

    }

    public <T2> Kleisli<Either<T, T2>, R,W> fanIn(Kleisli<T2,R,W> fanIn) {
        return new Kleisli<>(e -> e.visit(this, fanIn));
    }



    private static <T,R,W extends WitnessType<W>> Kleisli<T,R,W> kliesli(Function<? super T,? extends AnyM<W,? extends R>> fn){
        return new Kleisli<T, R, W>(narrow(fn));
    }
    private static <T,R,W extends WitnessType<W>> Kleisli<T,R,W> lift(Function<? super T,? extends R> fn, W type){
        return  kliesli(fn.andThen(r->type.adapter().unit(r)));
    }

    private static <T, W extends WitnessType<W>, R> Fn1<T,AnyM<W,R>> narrow(Function<? super T, ? extends AnyM<W, ? extends R>> fn) {
        if(fn instanceof Fn1){
            return (Fn1)fn;
        }
        return in -> (AnyM<W,R>)fn.apply(in);
    }
}
