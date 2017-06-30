package cyclops.monads;


import com.aol.cyclops2.types.functor.Transformable;
import cyclops.control.Xor;
import cyclops.function.*;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * Compose functions that return monads
 *
 * @param <W> Monad kind
 * @param <T> Function input type
 * @param <R> Function return type
 *              (inside monad e.g. Kleisli[stream,String,Integer] represents a function that takes a String and returns a Stream of Integers)
 */
@FunctionalInterface
public interface Kleisli<W extends WitnessType<W>,T,R> extends Fn1<T,AnyM<W,R>>,
                                                                Transformable<R>{

    default Kleisli<W,T,R> local(Function<? super R, ? extends R> local){
        return kleisli(t->apply(t).map(r->local.apply(r)));
    }
    default <R1> Kleisli<W,T,R1> map(Function<? super R, ? extends R1> mapper){
        return kleisli(andThen(am->am.map(mapper)));
    }
    default <R1> Kleisli<W,T,R1> flatMapA(Function<? super R, ? extends AnyM<W,? extends R1>> mapper){
        return kleisli(andThen(am->am.flatMapA(mapper)));
    }

    /**
     *
     * Compose functions that return monads.
     *
     * Example :-
     *
     * <pre>
     *  {@code
     *  import cyclops.monads.Witness.reactiveSeq;
        import static cyclops.monads.Kleisli.kleisli;


        Kleisli<reactiveSeq, Integer, Integer> k1 = kleisli(t -> ReactiveSeq.iterate(0,i->i<t, i->i+1)
                                                                            .anyM(), reactiveSeq.INSTANCE);

        k1.flatMap(i-> kleisli(t-> ReactiveSeq.of(t+i)
                                              .anyM(), reactiveSeq.INSTANCE))
          .applyHKT(10)
          .forEach(System.out::println);

        10
        11
        12
        13
        14
        15
        16
        17
        18
        19
     *
     * }</pre>
     *
     * @param mapper
     * @param <R1>
     * @return
     */
    default <R1> Kleisli<W,T,R1> flatMap(Function<? super R, ? extends Kleisli<W,T, R1>> mapper){
        return kleisli(t->apply(t).flatMapA(r ->  mapper.apply(r).apply(t)));
    }

    default <A> Kleisli<W,A,R> compose(Kleisli<W,A,T> kleisli) {
        return a -> kleisli.apply(a).flatMapA(this);
    }
    default <R2> Kleisli<W,T,R2> then(Kleisli<W,R,R2> kleisli) {

        return t-> apply(t).flatMapA(kleisli);

    }

    default <__> Kleisli<W,Xor<T, __>, Xor<R, __>> leftK(W type) {
         return kleisli(xr -> xr.visit(l -> apply(l).map(Xor::secondary), r -> type.adapter().unit(r).map(Xor::primary)));
    }
    default <__> Kleisli<W,Xor<__,T>, Xor<__,R>> rightK(W type) {
        return kleisli(xr -> xr.visit(l -> type.adapter().unit(l).map(Xor::secondary), r -> apply(r).map(Xor::primary)));
    }
    default <__> Kleisli<W,Tuple2<T, __>, Tuple2<R, __>> firstK() {
        return kleisli(xr -> xr.map((v1,v2) -> apply(v1).map(r1-> Tuple.tuple(r1,v2))));
    }
    default <__> Kleisli<W,Tuple2<__,T>, Tuple2<__,R>> secondK() {
        return kleisli(xr -> xr.map((v1,v2) -> apply(v2).map(r2-> Tuple.tuple(v1,r2))));
    }


    default <T2,R2> Kleisli<W,Xor<T, T2>, Xor<R, R2>> merge(Kleisli<W,T2,R2> merge, W type) {
        Kleisli<W,T, Xor<R, R2>> first = then(lift(Xor::secondary, type));
        Kleisli<W,T2, Xor<R, R2>> second = merge.then(lift(Xor::primary, type));
        return first.fanIn(second);

    }

    default <T2> Kleisli<W,Xor<T, T2>, R> fanIn(Kleisli<W,T2,R> fanIn) {
        return e -> e.visit(this, fanIn);
    }



    default <R1, R2, R3, R4> Kleisli<W,T,R4> forEach4(Function<? super R, Function<? super T,? extends AnyM<W,? extends R1>>> value2,
                                                     BiFunction<? super R, ? super R1, Function<? super T,? extends AnyM<W,? extends R2>>> value3,
                                                     Fn3<? super R, ? super R1, ? super R2, Function<? super T,? extends AnyM<W,? extends R3>>> value4,
                                                    Fn4<? super R, ? super R1, ? super R2, ? super R3, ? extends R4> yieldingFunction) {




        return this.flatMap(in -> {

            Kleisli<W,T,R1> a = kleisli(value2.apply(in));
            return a.flatMap(ina -> {
                Kleisli<W,T,R2> b = kleisli(value3.apply(in,ina));
                return b.flatMap(inb -> {

                    Kleisli<W,T,R3> c = kleisli(value4.apply(in,ina,inb));
                    return c.map(inc->yieldingFunction.apply(in, ina, inb, inc));

                });


            });


        });

    }




    default <R1, R2, R4> Kleisli<W,T,R4> forEach3(Function<? super R, Function<? super T,? extends AnyM<W,? extends R1>>> value2,
                                               BiFunction<? super R, ? super R1, Function<? super T,? extends AnyM<W,? extends R2>>> value3,
                                               Fn3<? super R, ? super R1, ? super R2, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            Kleisli<W,T,R1> a = kleisli(value2.apply(in));
            return a.flatMap(ina -> {
                Kleisli<W,T,R2> b = kleisli(value3.apply(in,ina));
                return b.map(in2 -> {
                    return yieldingFunction.apply(in, ina, in2);

                });



            });

        });

    }

    default <R1, R4> Kleisli<W,T,R4> forEach2(Function<? super R, Function<? super T,? extends AnyM<W,? extends R1>>> value2,
                                           BiFunction<? super R, ? super R1, ? extends R4> yieldingFunction) {

        return this.flatMap(in -> {

            Kleisli<W,T,R1> a = kleisli(value2.apply(in));
            return a.map(in2 -> {
                return yieldingFunction.apply(in, in2);

            });




        });


    }


    public static <T,R,W extends WitnessType<W>> Kleisli<W,T,R> kleisli(Function<? super T,? extends AnyM<W,? extends R>> fn){
        return in-> {
            Fn1<T,AnyM<W,R>> fn1 = narrow(fn);
            return fn1.apply(in);
        };
    }
    public static <T,R,W extends WitnessType<W>> Kleisli<W,T,R> lift(Function<? super T,? extends R> fn, W type){
        return  kleisli(fn.andThen(r->type.adapter().unit(r)));
    }

    static <T, W extends WitnessType<W>, R> Fn1<T,AnyM<W,R>> narrow(Function<? super T, ? extends AnyM<W, ? extends R>> fn) {
        if(fn instanceof Fn1){
            return (Fn1)fn;
        }
        return in -> (AnyM<W,R>)fn.apply(in);
    }
}
