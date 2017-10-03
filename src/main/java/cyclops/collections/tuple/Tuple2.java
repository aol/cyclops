package cyclops.collections.tuple;


import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.hkt.Higher2;
import com.aol.cyclops2.types.Filters;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.BiTransformable;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.control.Identity;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;
import cyclops.control.Xor;
import cyclops.function.Fn3;
import cyclops.function.Monoid;
import cyclops.monads.Witness;
import cyclops.monads.Witness.tuple2;
import cyclops.typeclasses.Cokleisli;
import cyclops.typeclasses.InstanceDefinitions;
import cyclops.typeclasses.Kleisli;
import cyclops.typeclasses.Pure;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.comonad.ComonadByPure;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

/*
  A Tuple1 implementation that can be either eager / strict or lazy
  Roughly analogous to the Identity monad

 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Tuple2<T1,T2> implements To<Tuple2<T1,T2>>,
                                        Serializable,
                                        Higher2<tuple2,T1,T2> {

    private static final long serialVersionUID = 1L;

    public static <T1,T2> Tuple2<T1,T2> of(T1 value1, T2 value2) {
        return new Tuple2<T1,T2>(value1,value2);
    }
    public static <T1,T2> Tuple2<T1,T2> lazy(Supplier<? extends T1> supplier1,Supplier<? extends T2> supplier2) {
        return new Tuple2<T1,T2>(null,null) {
            @Override
            public T1 _1() {
                return supplier1.get();
            }
            @Override
            public T2 _2() {
                return supplier2.get();
            }
        };
    }
    private final T1 _1;
    private final T2 _2;

    public T1 _1(){
        return _1;
    }

    public  T2 _2(){
        return _2;
    }

    public Tuple1<T1> first(){
        return Tuple.tuple(_1());
    }

    public Tuple1<T2> second(){
        return Tuple.tuple(_2());
    }

    public Tuple2<T1,T2> eager(){
        return of(_1(),_2());
    }

    public <R1,R2> Tuple2<R1,R2> bimap(Function<? super T1, ? extends R1> fn1,Function<? super T2,? extends R2> fn2){
        return of((fn1.apply(_1())),fn2.apply(_2()));
    }

    public <R1,R2> Tuple2<R1,R2> lazyBimap(Function<? super T1, ? extends R1> fn1,Function<? super T2,? extends R2> fn2){
        return lazy(()->(fn1.apply(_1())),()->fn2.apply(_2()));
    }
    public <R> Tuple2<R, T2> map1(Function<? super T1, ? extends R> fn) {
        return of(fn.apply(_1()), _2());
    }
    public <R> Tuple2<R, T2> lazyMap1(Function<? super T1, ? extends R> fn) {
        return lazy(()->fn.apply(_1()),()-> _2());
    }
    public <R> Tuple2<T1, R> map2(Function<? super T2, ? extends R> fn) {
        return of(_1(), fn.apply(_2()));
    }
    public <R> Tuple2<T1, R> lazyMap2(Function<? super T2, ? extends R> fn) {
        return lazy(() -> _1(), () -> fn.apply(_2()));
    }

    public Tuple2<T2,T1> swap(){
        return of(_2(),_1());
    }

    public Tuple2<T2,T1> lazySwap(){
        return lazy(()->_2(),()->_1());
    }

    public <R> R visit(BiFunction<? super T1, ? super T2, ? extends R> fn){
        return fn.apply(_1(),_2());
    }

    @Override
    public String toString() {
        return String.format("[%s,%s]", _1(),_2());
    }

    public static <T1,T2> Tuple2<T1,T2> narrowK(Higher2<tuple2,T1,T2> ds){
        return (Tuple2<T1,T2>)ds;
    }

    public static  <T1,T2,R> Tuple2<T1,R> tailRec(Monoid<T1> op,T2 initial, Function<? super T2, ? extends Tuple2<T1,? extends Xor<T2, R>>> fn){
        Tuple2<T1,? extends Xor<T2, R>> next[] = new Tuple2[1];
        next[0] = Tuple2.of(op.zero(),Xor.secondary(initial));
        boolean cont = true;
        do {

            cont = next[0].visit((a,p) -> p.visit(s -> {
                next[0] = narrowK(fn.apply(s)).map1(t1->op.apply(next[0]._1(),t1));
                return true;
            }, __ -> false));
        } while (cont);
        return next[0].map2(Xor::get);
    }


    public static class Instances {


        public static InstanceDefinitions<T1> definitions(){
            return new InstanceDefinitions<T1>() {
                @Override
                public <T, R> Functor<T1> functor() {
                    return Tuple2.Instances.functor();
                }

                @Override
                public <T> Pure<T1> unit() {
                    return Tuple2.Instances.unit();
                }

                @Override
                public <T, R> Applicative<T1> applicative() {
                    return Tuple2.Instances.applicative();
                }

                @Override
                public <T, R> Monad<T1> monad() {
                    return Tuple2.Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<T1>> monadZero() {
                    return Maybe.none();
                }

                @Override
                public <T> Maybe<MonadPlus<T1>> monadPlus() {
                    return Maybe.none();
                }

                @Override
                public <T> MonadRec<T1> monadRec() {
                    return Tuple2.Instances.monadRec();
                }

                @Override
                public <T> Maybe<MonadPlus<T1>> monadPlus(Monoid<Higher<T1, T>> m) {
                    return Maybe.none();
                }

                @Override
                public <C2, T> Traverse<T1> traverse() {
                    return Tuple2.Instances.traverse();
                }

                @Override
                public <T> Foldable<T1> foldable() {
                    return Tuple2.Instances.foldable();
                }

                @Override
                public <T> Maybe<Comonad<T1>> comonad() {
                    return Maybe.just(Tuple2.Instances.comonad());
                }

                @Override
                public <T> Maybe<Unfoldable<T1>> unfoldable() {
                    return Maybe.none();
                }
            };
        }

        public static Functor<T1> functor(){
            return new Functor<T1>(){
                @Override
                public <T, R> Higher<T1, R> map(Function<? super T, ? extends R> fn, Higher<T1, T> ds) {
                    return narrowK(ds).map(fn);
                }
            };
        }

        public static Pure<T1> unit(){
            return new Pure<T1>(){
                @Override
                public <T> Higher<T1, T> unit(T value) {
                    return of(value);
                }
            };
        }
        public static Applicative<T1> applicative(){
            return new Applicative<T1>(){


                @Override
                public <T, R> Higher<T1, R> ap(Higher<T1, ? extends Function<T, R>> fn, Higher<T1, T> apply) {
                    Tuple2<? extends Function<T, R>> f = narrowK(fn);
                    Tuple2<T> ap = narrowK(apply);
                    return f.flatMap(x -> ap.map(x));
                }

                @Override
                public <T, R> Higher<T1, R> map(Function<? super T, ? extends R> fn, Higher<T1, T> ds) {
                    return functor().map(fn,ds);
                }

                @Override
                public <T> Higher<T1, T> unit(T value) {
                    return Tuple2.Instances.unit().unit(value);
                }
            };
        }
        public static Monad<T1> monad(){
            return new Monad<T1>(){


                @Override
                public <T, R> Higher<T1, R> ap(Higher<T1, ? extends Function<T, R>> fn, Higher<T1, T> apply) {
                    return applicative().ap(fn,apply);
                }

                @Override
                public <T, R> Higher<T1, R> map(Function<? super T, ? extends R> fn, Higher<T1, T> ds) {
                    return functor().map(fn,ds);
                }

                @Override
                public <T> Higher<T1, T> unit(T value) {
                    return Tuple2.Instances.unit().unit(value);
                }

                @Override
                public <T, R> Higher<T1, R> flatMap(Function<? super T, ? extends Higher<T1, R>> fn, Higher<T1, T> ds) {
                    return narrowK(ds).flatMap(fn.andThen(i->narrowK(i)));
                }
            };
        }
        public static MonadRec<T1> monadRec() {

            return new MonadRec<T1>(){
                @Override
                public <T, R> Higher<T1, R> tailRec(T initial, Function<? super T, ? extends Higher<T1, ? extends Xor<T, R>>> fn) {
                    return Tuple2.tailRec(initial,fn.andThen(Tuple2::narrowK));
                }

            };


        }
        public static Traverse<T1> traverse(){
            return new Traverse<T1>(){

                @Override
                public <T, R> Higher<T1, R> ap(Higher<T1, ? extends Function<T, R>> fn, Higher<T1, T> apply) {
                    return applicative().ap(fn,apply);
                }

                @Override
                public <T, R> Higher<T1, R> map(Function<? super T, ? extends R> fn, Higher<T1, T> ds) {
                    return functor().map(fn,ds);
                }

                @Override
                public <T> Higher<T1, T> unit(T value) {
                    return Tuple2.Instances.unit().unit(value);
                }

                @Override
                public <C2, T, R> Higher<C2, Higher<T1, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<T1, T> ds) {
                    Tuple2<T> id = narrowK(ds);
                    Function<R, Tuple2<R>> rightFn = r -> of(r);
                    return applicative.map(rightFn, fn.apply(id._1()));
                }

                @Override
                public <C2, T> Higher<C2, Higher<T1, T>> sequenceA(Applicative<C2> applicative, Higher<T1, Higher<C2, T>> ds) {
                    return traverseA(applicative,Function.identity(),ds);
                }
            };
        }
        public static Foldable<T1> foldable(){
            return new Foldable<T1>(){


                @Override
                public <T> T foldRight(Monoid<T> monoid, Higher<T1, T> ds) {
                    return monoid.apply(narrowK(ds)._1(),monoid.zero());
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<T1, T> ds) {
                    return monoid.apply(monoid.zero(),narrowK(ds)._1());
                }

                @Override
                public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<T1, T> nestedA) {
                    return foldLeft(mb,narrowK(nestedA).<R>map(fn));
                }
            };
        }
        public static Comonad<T1> comonad(){
            return new ComonadByPure<T1>(){


                @Override
                public <T> T extract(Higher<T1, T> ds) {
                    return narrowK(ds)._1();
                }

                @Override
                public <T, R> Higher<T1, R> map(Function<? super T, ? extends R> fn, Higher<T1, T> ds) {
                    return functor().map(fn,ds);
                }

                @Override
                public <T> Higher<T1, T> unit(T value) {
                    return Tuple2.Instances.unit().unit(value);
                }
            };
        }
    }
    public static  <T> Kleisli<T1,Tuple2<T>,T> kindKleisli(){
        return Kleisli.of(Tuple2.Instances.monad(), Tuple2::widen);
    }
    public static <T> Higher<T1, T> widen(Tuple2<T> narrow) {
        return narrow;
    }
    public static  <T> Cokleisli<T1,T,Tuple2<T>> kindCokleisli(){
        return Cokleisli.of(Tuple2::narrowK);
    }
}
