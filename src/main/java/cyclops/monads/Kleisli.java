package cyclops.monads;


import com.aol.cyclops2.types.Transformable;
import cyclops.control.Xor;
import cyclops.function.Fn1;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.function.Function;

@AllArgsConstructor(access= AccessLevel.PRIVATE)
public class Kleisli<T,R,W extends WitnessType<W>> implements Fn1<T,AnyM<W,R>>,
                                                                Transformable<R>{

    public final Fn1<T, AnyM<W, R>> fn;
    private final W type = null;

    @Override
    public AnyM<W,R> apply(T a) {

        return fn.apply(a);
    }

    public <R1> Kleisli<T,R1,W> map(Function<? super R, ? extends R1> mapper){
        return kleisli(fn.andThen(am->am.map(mapper)));
    }

    public <R1> Kleisli<T,R1,W> flatMap(Function<? super R, ? extends AnyM<W,? extends R1>> mapper){
        return kleisli(fn.andThen(am->am.flatMapA(mapper)));
    }

    public <A> Kleisli<A,R,W> compose(Kleisli<A,T,W> kleisli) {
        return new Kleisli<A,R,W>(a -> kleisli.apply(a).flatMapA(this));
    }
    public <R2> Kleisli<T,R2,W> then(Kleisli<R,R2,W> kleisli) {

        return new Kleisli<T,R2,W>(t->this.apply(t)
                                    .flatMapA(kleisli));

    }

    public <__> Kleisli<Xor<T, __>, Xor<R, __>,W> leftK() {
         return kleisli(xr -> xr.visit(l -> fn.apply(l).map(Xor::secondary), r -> type.adapter().unit(r).map(Xor::primary)));
    }
    public <__> Kleisli<Xor<__,T>, Xor<__,R>,W> rightK() {
        return kleisli(xr -> xr.visit(l -> type.adapter().unit(l).map(Xor::secondary), r -> fn.apply(r).map(Xor::primary)));
    }
    public <__> Kleisli<Tuple2<T, __>, Tuple2<R, __>,W> firstK() {
        return kleisli(xr -> xr.map((v1,v2) -> fn.apply(v1).map(r1-> Tuple.tuple(r1,v2))));
    }
    public <__> Kleisli<Tuple2<__,T>, Tuple2<__,R>,W> secondK() {
        return kleisli(xr -> xr.map((v1,v2) -> fn.apply(v2).map(r2-> Tuple.tuple(v1,r2))));
    }


    public <T2,R2> Kleisli<Xor<T, T2>, Xor<R, R2>,W> merge(Kleisli<T2,R2,W> merge, W type) {
        Kleisli<T, Xor<R, R2>, W> first = then(lift(Xor::secondary, type));
        Kleisli<T2, Xor<R, R2>, W> second = merge.then(lift(Xor::primary, type));
        return first.fanIn(second);

    }

    public <T2> Kleisli<Xor<T, T2>, R,W> fanIn(Kleisli<T2,R,W> fanIn) {
        return new Kleisli<>(e -> e.visit(this, fanIn));
    }



    public static <T,R,W extends WitnessType<W>> Kleisli<T,R,W> kleisli(Function<? super T,? extends AnyM<W,? extends R>> fn){
        return new Kleisli<T, R, W>(narrow(fn));
    }
    public static <T,R,W extends WitnessType<W>> Kleisli<T,R,W> lift(Function<? super T,? extends R> fn, W type){
        return  kleisli(fn.andThen(r->type.adapter().unit(r)));
    }

    private static <T, W extends WitnessType<W>, R> Fn1<T,AnyM<W,R>> narrow(Function<? super T, ? extends AnyM<W, ? extends R>> fn) {
        if(fn instanceof Fn1){
            return (Fn1)fn;
        }
        return in -> (AnyM<W,R>)fn.apply(in);
    }
}
