package cyclops.monads;


import com.aol.cyclops2.types.Transformable;
import cyclops.control.Xor;
import cyclops.function.*;
import cyclops.monads.transformers.ReaderT;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;


/**
 * Compose functions that return monads
 *
 * @param <W> Monad kind
 * @param <T> Function input type
 * @param <R> Function return type
 *              (inside monad e.g. Kleisli[stream,String,Integer] represents a function that takes a String and returns a Stream of Integers)
 */
@AllArgsConstructor(access= AccessLevel.PRIVATE)
public class Kleisli<W extends WitnessType<W>,T,R> implements Fn1<T,AnyM<W,R>>,
                                                                Transformable<R>{

    public final Fn1<T, AnyM<W, R>> fn;
    private final W type;

    @Override
    public AnyM<W,R> apply(T a) {

        return fn.apply(a);
    }

    public Kleisli<W,T,R> local(Function<? super R, ? extends R> local){
        return kleisli(t->fn.apply(t).map(r->local.apply(r)),type);
    }
    public <R1> Kleisli<W,T,R1> map(Function<? super R, ? extends R1> mapper){
        return kleisli(fn.andThen(am->am.map(mapper)),type);
    }
    public <R1> Kleisli<W,T,R1> flatMapA(Function<? super R, ? extends AnyM<W,? extends R1>> mapper){
        return kleisli(fn.andThen(am->am.flatMapA(mapper)),type);
    }
    public <R1> Kleisli<W,T,R1> flatMap(Function<? super R, ? extends Kleisli<W,T, R1>> mapper){
        return kleisli(t->fn.apply(t).flatMapA(r ->  mapper.apply(r).fn.apply(t)),type);
    }

    public <A> Kleisli<W,A,R> compose(Kleisli<W,A,T> kleisli) {
        return new Kleisli<W,A,R>(a -> kleisli.apply(a).flatMapA(this),type);
    }
    public <R2> Kleisli<W,T,R2> then(Kleisli<W,R,R2> kleisli) {

        return new Kleisli<W,T,R2>(t->this.apply(t)
                                    .flatMapA(kleisli),type);

    }

    public <__> Kleisli<W,Xor<T, __>, Xor<R, __>> leftK() {
         return kleisli(xr -> xr.visit(l -> fn.apply(l).map(Xor::secondary), r -> type.adapter().unit(r).map(Xor::primary)),type);
    }
    public <__> Kleisli<W,Xor<__,T>, Xor<__,R>> rightK() {
        return kleisli(xr -> xr.visit(l -> type.adapter().unit(l).map(Xor::secondary), r -> fn.apply(r).map(Xor::primary)),type);
    }
    public <__> Kleisli<W,Tuple2<T, __>, Tuple2<R, __>> firstK() {
        return kleisli(xr -> xr.map((v1,v2) -> fn.apply(v1).map(r1-> Tuple.tuple(r1,v2))),type);
    }
    public <__> Kleisli<W,Tuple2<__,T>, Tuple2<__,R>> secondK() {
        return kleisli(xr -> xr.map((v1,v2) -> fn.apply(v2).map(r2-> Tuple.tuple(v1,r2))),type);
    }


    public <T2,R2> Kleisli<W,Xor<T, T2>, Xor<R, R2>> merge(Kleisli<W,T2,R2> merge, W type) {
        Kleisli<W,T, Xor<R, R2>> first = then(lift(Xor::secondary, type));
        Kleisli<W,T2, Xor<R, R2>> second = merge.then(lift(Xor::primary, type));
        return first.fanIn(second);

    }

    public <T2> Kleisli<W,Xor<T, T2>, R> fanIn(Kleisli<W,T2,R> fanIn) {
        return new Kleisli<>(e -> e.visit(this, fanIn),type);
    }



    public <R1, R2, R3, R4> Kleisli<W,T,R4> forEach4(Function<? super R, Function<? super T,? extends AnyM<W,? extends R1>>> value2,
                                                     BiFunction<? super R, ? super R1, Function<? super T,? extends AnyM<W,? extends R2>>> value3,
                                                     Fn3<? super R, ? super R1, ? super R2, Function<? super T,? extends AnyM<W,? extends R3>>> value4,
                                                    Fn4<? super R, ? super R1, ? super R2, ? super R3, ? extends R4> yieldingFunction) {



        Kleisli<W, T, R4> d2 = kleisli(t -> type.adapter().unit(yieldingFunction.apply(null, null, null, null)), type);

        return this.flatMap(in -> {

            Kleisli<W,T,R1> a = kleisli(value2.apply(in),type);
            return a.flatMap(ina -> {
                Kleisli<W,T,R2> b = kleisli(value3.apply(in,ina),type);
                return b.flatMap(inb -> {

                    Kleisli<W,T,R3> c = kleisli(value4.apply(in,ina,inb),type);
                    return c.map(inc->yieldingFunction.apply(in, ina, inb, inc));

                });


            });


        });

    }




    public <R1, R2, R4> Kleisli<W,T,R4> forEach3(Function<? super R, Function<? super T,? extends AnyM<W,? extends R1>>> value2,
                                               BiFunction<? super R, ? super R1, Function<? super T,? extends AnyM<W,? extends R2>>> value3,
                                               Fn3<? super R, ? super R1, ? super R2, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            Kleisli<W,T,R1> a = kleisli(value2.apply(in),type);
            return a.flatMap(ina -> {
                Kleisli<W,T,R2> b = kleisli(value3.apply(in,ina),type);
                return b.map(in2 -> {
                    return yieldingFunction.apply(in, ina, in2);

                });



            });

        });

    }



    public <R1, R4> Kleisli<W,T,R4> forEach2(Function<? super R, Function<? super T,? extends AnyM<W,? extends R1>>> value2,
                                           BiFunction<? super R, ? super R1, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            Kleisli<W,T,R1> a = kleisli(value2.apply(in),type);
            return a.map(in2 -> {
                return yieldingFunction.apply(in, in2);

            });




        });


    }


    public static <T,R,W extends WitnessType<W>> Kleisli<W,T,R> kleisli(Function<? super T,? extends AnyM<W,? extends R>> fn, W type){
        return new Kleisli<W,T, R>(narrow(fn),type);
    }
    public static <T,R,W extends WitnessType<W>> Kleisli<W,T,R> lift(Function<? super T,? extends R> fn, W type){
        return  kleisli(fn.andThen(r->type.adapter().unit(r)),type);
    }

    private static <T, W extends WitnessType<W>, R> Fn1<T,AnyM<W,R>> narrow(Function<? super T, ? extends AnyM<W, ? extends R>> fn) {
        if(fn instanceof Fn1){
            return (Fn1)fn;
        }
        return in -> (AnyM<W,R>)fn.apply(in);
    }
}
