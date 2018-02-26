package cyclops.arrow;

import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher3;
import com.oath.cyclops.types.functor.Transformable;
import cyclops.control.Either;
import cyclops.function.Function1;
import cyclops.function.Function3;
import cyclops.function.Function4;


import cyclops.function.Monoid;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;

import java.util.function.BiFunction;
import java.util.function.Function;


import com.oath.cyclops.hkt.DataWitness.kleisli;


/**
 * Compose arrow that return monads
 *
 * @param <W> Monad kind
 * @param <T> Function input type
 * @param <R> Function return type
 *              (inside monad e.g. Kleisli[stream,String,Integer] represents a function that takes a String and returns a Stream of Integers)
 */

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Kleisli<W,T,R> implements Function1<T,Higher<W,R>>,
                                        Transformable<R>,
                                        Higher3<kleisli,W,T,R> {

    private final Monad<W> monad;

    private final Function<? super T, ? extends Higher<W,? extends R>> fn;

    public static <W,T,R> Kleisli<W,T,R> of(Monad<W> monad, Function<? super T, ? extends Higher<W,? extends R>> fn){
        return new Kleisli<W,T,R>(monad,fn);
    }

    public static <W,T,R> Kleisli<W,T,R> arrow(Monad<W> monad, Function<? super T, ? extends R> fn){
       return of(monad,a -> monad.unit(fn.apply(a)));
    }
    public Kleisli<W,T,R> local(Function<? super R, ? extends R> local){
        return kleisliK(monad, t->monad.map(r->local.apply(r),apply(t)));
    }
    public <R1> Kleisli<W,T,R1> map(Function<? super R, ? extends R1> mapper){
        return kleisliK(monad,andThen(am->monad.map(mapper,am)));
    }
    public <R1> Kleisli<W,T,R1> flatMap(Function<? super R, ? extends Higher<W,? extends R1>> mapper){
      Function<R,Higher<W,R1>> fn = (Function<R,Higher<W,R1>>)mapper;
      Kleisli<W, T, R1> x = kleisliK(monad, andThen(am -> monad.flatMap(fn, am)));
      return x;
    }
    public  <R2> Kleisli<W, T, Tuple2<R,R2>> zip(Kleisli<W, T, R2> o){
        return zip(o, Tuple::tuple);
    }
    public  <R2,B> Kleisli<W, T, B> zip(Kleisli<W, T, R2> o, BiFunction<? super R,? super R2,? extends B> fn){
        return flatMapK(a -> o.map(b -> fn.apply(a,b)));
    }

    public <R1> Kleisli<W,T,R1> flatMapK(Function<? super R, ? extends Kleisli<W,T, R1>> mapper){
        return kleisliK(monad, t->monad.flatMap(r ->  mapper.apply(r).apply(t),apply(t)));
    }

    public <A> Kleisli<W,A,R> compose(Kleisli<W,A,T> kleisli) {
        return of(monad,a -> monad.flatMap(this,kleisli.apply(a)));
    }
    public <R2> Kleisli<W,T,R2> then(Kleisli<W,R,R2> kleisli) {
        return of(monad,t-> monad.flatMap(kleisli,apply(t)));

    }

    public <__> Kleisli<W,Either<T, __>, Either<R, __>> leftK(W type) {
        return kleisliK(monad, xr -> xr.visit(l -> monad.map(Either::left,apply(l)), r -> monad.map(Either::right,monad.unit(r))));
    }
    public <__> Kleisli<W,Either<__,T>, Either<__,R>> rightK(W type) {
        return kleisliK(monad, xr -> xr.visit(l -> monad.map(Either::left,monad.unit(l)), r -> monad.map(Either::right,apply(r))));
    }
    public <__> Kleisli<W,Tuple2<T, __>, Tuple2<R, __>> firstK() {
        return kleisliK(monad, xr -> xr.transform((v1, v2) -> monad.map(r1-> Tuple.tuple(r1,v2),apply(v1))));
    }
    public <__> Kleisli<W,Tuple2<__,T>, Tuple2<__,R>> secondK() {
        return kleisliK(monad, xr -> xr.transform((v1, v2) -> monad.map(r2-> Tuple.tuple(v1,r2),apply(v2))));
    }


    public <T2,R2> Kleisli<W,Either<T, T2>, Either<R, R2>> merge(Kleisli<W,T2,R2> merge, W type) {
        Kleisli<W,T, Either<R, R2>> first = then(lift(monad, Either::left, type));
        Kleisli<W,T2, Either<R, R2>> second = merge.then(lift(monad, Either::right, type));
        return first.fanIn(second);

    }


    public <T2> Kleisli<W,Either<T, T2>, R> fanIn(Kleisli<W,T2,R> fanIn) {
        return of(monad,e -> e.visit(this, fanIn));
    }



    public <R1, R2, R3, R4> Kleisli<W,T,R4> forEach4(Function<? super R, Function<? super T,? extends Higher<W,? extends R1>>> value2,
                                                     BiFunction<? super R, ? super R1, Function<? super T,? extends Higher<W,? extends R2>>> value3,
                                                     Function3<? super R, ? super R1, ? super R2, Function<? super T,? extends Higher<W,? extends R3>>> value4,
                                                     Function4<? super R, ? super R1, ? super R2, ? super R3, ? extends R4> yieldingFunction) {




        return this.flatMapK(in -> {

            Kleisli<W,T,R1> a = kleisliK(monad,value2.apply(in));
            return a.flatMapK(ina -> {
                Kleisli<W,T,R2> b = kleisliK(monad,value3.apply(in,ina));
                return b.flatMapK(inb -> {

                    Kleisli<W,T,R3> c = kleisliK(monad,value4.apply(in,ina,inb));
                    return c.map(inc->yieldingFunction.apply(in, ina, inb, inc));

                });


            });


        });

    }




    public <R1, R2, R4> Kleisli<W,T,R4> forEach3(Function<? super R, Function<? super T,? extends Higher<W,? extends R1>>> value2,
                                                 BiFunction<? super R, ? super R1, Function<? super T,? extends Higher<W,? extends R2>>> value3,
                                                 Function3<? super R, ? super R1, ? super R2, ? extends R4> yieldingFunction) {

        return this.flatMapK(in -> {

            Kleisli<W,T,R1> a = kleisliK(monad,value2.apply(in));
            return a.flatMapK(ina -> {
                Kleisli<W,T,R2> b = kleisliK(monad,value3.apply(in,ina));
                return b.map(in2 -> {
                    return yieldingFunction.apply(in, ina, in2);

                });



            });

        });

    }

    public <R1, R4> Kleisli<W,T,R4> forEach2(Function<? super R, Function<? super T,? extends Higher<W,? extends R1>>> value2,
                                             BiFunction<? super R, ? super R1, ? extends R4> yieldingFunction) {

        return this.flatMapK(in -> {

            Kleisli<W,T,R1> a = kleisliK(monad,value2.apply(in));
            return a.map(in2 -> {
                return yieldingFunction.apply(in, in2);

            });




        });


    }
    public <R1, R2, R3, R4> Kleisli<W,T,R4> forEachK4(Function<? super R, ? extends Kleisli<W,T,? extends R1>> value2,
                                                     BiFunction<? super R, ? super R1, ? extends Kleisli<W,T,? extends R2>> value3,
                                                     Function3<? super R, ? super R1, ? super R2, ? extends Kleisli<W,T,? extends R3>> value4,
                                                     Function4<? super R, ? super R1, ? super R2, ? super R3, ? extends R4> yieldingFunction) {




        return this.flatMapK(in -> {

            Kleisli<W,T,? extends R1> a = value2.apply(in);
            return a.flatMapK(ina -> {
                Kleisli<W,T,? extends R2> b = value3.apply(in,ina);
                return b.flatMapK(inb -> {

                    Kleisli<W,T,? extends R3> c = value4.apply(in,ina,inb);
                    return c.map(inc->yieldingFunction.apply(in, ina, inb, inc));

                });


            });


        });

    }




    public <R1, R2, R4> Kleisli<W,T,R4> forEachK3(Function<? super R, ? extends Kleisli<W,T,? extends R1>> value2,
                                                 BiFunction<? super R, ? super R1, ? extends Kleisli<W,T,? extends R2>> value3,
                                                 Function3<? super R, ? super R1, ? super R2, ? extends R4> yieldingFunction) {

        return this.flatMapK(in -> {

            Kleisli<W,T,? extends R1> a = value2.apply(in);
            return a.flatMapK(ina -> {
                Kleisli<W,T,? extends R2> b = value3.apply(in,ina);
                return b.map(in2 -> {
                    return yieldingFunction.apply(in, ina, in2);

                });



            });

        });

    }

    public <R1, R4> Kleisli<W,T,R4> forEachK2(Function<? super R, ? extends Kleisli<W,T,? extends R1>> value2,
                                             BiFunction<? super R, ? super R1, ? extends R4> yieldingFunction) {

        return this.flatMapK(in -> {

            Kleisli<W,T,? extends R1> a = value2.apply(in);
            return a.map(in2 -> {
                return yieldingFunction.apply(in, in2);

            });

        });


    }


    public static <T,R,W> Kleisli<W,T,R> kleisliK(Monad<W> monad, Function<? super T,? extends Higher<W,? extends R>> fn){
        return of(monad,fn);
    }
    public static <T,R,W> Kleisli<W,T,R> lift(Monad<W> monad, Function<? super T,? extends R> fn, W type){
        return  kleisliK(monad,fn.andThen(r->monad.unit(r)));
    }

    public static <T, W, R> Function1<T,Higher<W,R>> narrow(Function<? super T, ? extends Higher<W, ? extends R>> fn) {
        if(fn instanceof Function1){
            return (Function1)fn;
        }
        return in -> (Higher<W,R>)fn.apply(in);
    }

    public static <T, W, R> Kleisli<W,T,R> narrowK(Higher<Higher<Higher<kleisli, W>, T>, R> k) {

        return (Kleisli)k;
    }

    public static <T, W, R> Kleisli<W,T,R> narrowK3(Higher3<kleisli,W,T,R> kleisliHigher3) {

        return (Kleisli<W,T,R>)kleisliHigher3;
    }

    @Override
    public Higher<W, R> apply(T a) {
        return (Higher<W,R>)fn.apply(a);
    }


}
