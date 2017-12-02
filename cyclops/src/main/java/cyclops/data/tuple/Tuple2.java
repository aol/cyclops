package cyclops.data.tuple;


import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher2;
import com.oath.cyclops.types.foldable.EqualTo;
import com.oath.cyclops.types.foldable.OrderedBy;
import com.oath.cyclops.types.foldable.To;
import cyclops.control.Maybe;
import cyclops.control.Either;
import cyclops.data.Comparators;
import cyclops.function.Memoize;
import cyclops.function.Monoid;
import com.oath.cyclops.hkt.DataWitness.tuple2;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.comonad.ComonadByPure;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functions.MonoidK;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.*;

/*
  A Tuple implementation that can be either eager / strict or lazy


 */
@AllArgsConstructor
public class Tuple2<T1,T2> implements To<Tuple2<T1,T2>>,
                                        Serializable,
                                        EqualTo<Higher<tuple2,T1>,T2,Tuple2<T1,T2>>,
                                        OrderedBy<Higher<tuple2,T1>,T2,Tuple2<T1,T2>>,
                                        Comparable<Tuple2<T1,T2>>,
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

    public Tuple2<T1,T2> memo(){
        Tuple2<T1,T2> host = this;
        return new Tuple2<T1,T2>(null,null){
            final Supplier<T1> memo1 = Memoize.memoizeSupplier(host::_1);
            final Supplier<T2> memo2 = Memoize.memoizeSupplier(host::_2);
            @Override
            public T1 _1() {

                return memo1.get();
            }

            @Override
            public T2 _2() {
                return memo2.get();
            }
        };
    }

    public <R> Tuple2<T1,R> flatMap(Monoid<T1> m,Function<? super T2, ? extends Tuple2<T1,R>> fn){
        return fn.apply(_2()).map1(t1->m.apply(t1,_1()));
    }

    public <R1> R1 transform(BiFunction<? super T1, ? super T2, ? extends R1> fn1){
        return fn1.apply(_1(),_2());
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



    public static <T1,T2> Tuple2<T1,T2> narrowK2(Higher2<tuple2,T1,T2> ds){
        return (Tuple2<T1,T2>)ds;
    }
    public static <T1,T2> Tuple2<T1,T2> narrowK(Higher<Higher<tuple2, T1>, T2> ds){
        return (Tuple2<T1,T2>)ds;
    }

    public static  <T1,T2,R> Tuple2<T1,R> tailRec(Monoid<T1> op,T2 initial, Function<? super T2, ? extends Tuple2<T1,? extends Either<T2, R>>> fn){
        Tuple2<T1,? extends Either<T2, R>> next[] = new Tuple2[1];
        next[0] = Tuple2.of(op.zero(), Either.left(initial));
        boolean cont = true;
        do {

            cont = next[0].visit((a,p) -> p.visit(s -> {
                next[0] = narrowK(fn.apply(s)).map1(t1->op.apply(next[0]._1(),t1));
                return true;
            }, __ -> false));
        } while (cont);
        return next[0].map2(x->x.orElse(null));
    }
    public Active<Higher<tuple2,T1>,T2> allTypeclasses(Monoid<T1> m){
        return Active.of(this,Instances.definitions(m));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Tuple2)) return false;
        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;
        return Objects.equals(_1(), tuple2._1()) &&
                Objects.equals(_2(), tuple2._2());
    }

    @Override
    public int hashCode() {
        return Objects.hash(_1(), _2());
    }

    @Override
    public int compareTo(Tuple2<T1, T2> o) {
        int result = Comparators.naturalOrderIdentityComparator().compare(_1(),o._1());
        if(result==0){
            result = Comparators.naturalOrderIdentityComparator().compare(_2(),o._2());
        }
        return result;
    }

    public static <K, V> Tuple2< K, V> narrow(Tuple2<? extends K, ? extends V> t) {
        return (Tuple2<K,V>)t;
    }

    public static class Instances {


        public static <T1> InstanceDefinitions<Higher<tuple2, T1>> definitions(Monoid<T1> m){
            return new InstanceDefinitions<Higher<tuple2, T1>>() {
                @Override
                public <T, R> Functor<Higher<tuple2, T1>> functor() {
                    return Tuple2.Instances.functor();
                }

                @Override
                public <T> Pure<Higher<tuple2, T1>> unit() {
                    return Tuple2.Instances.unit(m);
                }

                @Override
                public <T, R> Applicative<Higher<tuple2, T1>> applicative() {
                    return Tuple2.Instances.applicative(m);
                }

                @Override
                public <T, R> Monad<Higher<tuple2, T1>> monad() {
                    return Tuple2.Instances.monad(m);
                }

                @Override
                public <T, R> Maybe<MonadZero<Higher<tuple2, T1>>> monadZero() {
                    return Maybe.nothing();
                }

                @Override
                public <T> Maybe<MonadPlus<Higher<tuple2, T1>>> monadPlus() {
                    return Maybe.nothing();
                }

                @Override
                public <T> MonadRec<Higher<tuple2, T1>> monadRec() {
                    return Tuple2.Instances.monadRec(m);
                }


                @Override
                public <C2, T> Traverse<Higher<tuple2, T1>> traverse() {
                    return Tuple2.Instances.traverse(m);
                }

                @Override
                public <T> Maybe<MonadPlus<Higher<tuple2, T1>>> monadPlus(MonoidK<Higher<tuple2, T1>> m) {
                    return Maybe.nothing();
                }

                @Override
                public <T> Foldable<Higher<tuple2, T1>> foldable() {
                    return Tuple2.Instances.foldable();
                }

                @Override
                public <T> Maybe<Comonad<Higher<tuple2, T1>>> comonad() {
                    return Maybe.just(Tuple2.Instances.comonad(m));
                }

                @Override
                public <T> Maybe<Unfoldable<Higher<tuple2, T1>>> unfoldable() {
                    return Maybe.nothing();
                }
            };
        }

        public static <T1> Functor<Higher<tuple2, T1>> functor(){
            return new Functor<Higher<tuple2, T1>>(){

                @Override
                public <T, R> Higher<Higher<tuple2, T1>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<tuple2, T1>, T> ds) {
                    return narrowK(ds).map2(fn);
                }


            };
        }

        public static <T1> Pure<Higher<tuple2, T1>> unit(Monoid<T1> m){
            return new Pure<Higher<tuple2, T1>>(){
                @Override
                public <T> Higher<Higher<tuple2, T1>, T> unit(T value) {
                    return of(m.zero(),value);
                }
            };
        }
        public static <T1> Applicative<Higher<tuple2, T1>> applicative(Monoid<T1> m){
            return new Applicative<Higher<tuple2, T1>>(){


                @Override
                public <T, R> Higher<Higher<tuple2, T1>, R> ap(Higher<Higher<tuple2, T1>, ? extends Function<T, R>> fn, Higher<Higher<tuple2, T1>, T> apply) {
                    Tuple2<T1,? extends Function<T, R>> f = narrowK(fn);
                    Tuple2<T1,T> ap = narrowK(apply);
                    return f.flatMap(m,x -> ap.map2(x));
                }

                @Override
                public <T, R> Higher<Higher<tuple2, T1>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<tuple2, T1>, T> ds) {
                    return Instances.<T1>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<tuple2, T1>, T> unit(T value) {
                    return Tuple2.Instances.<T1>unit(m).unit(value);
                }
            };
        }
        public static <T1> Monad<Higher<tuple2, T1>> monad(Monoid<T1> m){
            return new Monad<Higher<tuple2, T1>>(){
                @Override
                public <T, R> Higher<Higher<tuple2, T1>, R> ap(Higher<Higher<tuple2, T1>, ? extends Function<T, R>> fn, Higher<Higher<tuple2, T1>, T> apply) {
                    return Instances.<T1>applicative(m).ap(fn,apply);
                }

                @Override
                public <T, R> Higher<Higher<tuple2, T1>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<tuple2, T1>, T> ds) {
                    return Instances.<T1>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<tuple2, T1>, T> unit(T value) {
                    return Tuple2.Instances.<T1>unit(m).unit(value);
                }

                @Override
                public <T, R> Higher<Higher<tuple2, T1>, R> flatMap(Function<? super T, ? extends Higher<Higher<tuple2, T1>, R>> fn, Higher<Higher<tuple2, T1>, T> ds) {
                    return narrowK(ds).flatMap(m,fn.andThen(Tuple2::narrowK));
                }

            };
        }
        public static <T1> MonadRec<Higher<tuple2, T1>> monadRec(Monoid<T1> m) {
            return new MonadRec<Higher<tuple2, T1>>(){
                @Override
                public <T, R> Higher<Higher<tuple2, T1>, R> tailRec(T initial, Function<? super T, ? extends Higher<Higher<tuple2, T1>, ? extends Either<T, R>>> fn) {
                    return Tuple2.tailRec(m,initial,fn.andThen(Tuple2::narrowK));
                }

            };


        }
        public static <T1> Traverse<Higher<tuple2, T1>> traverse(Monoid<T1> m){
            return new Traverse<Higher<tuple2, T1>>(){
                @Override
                public <T, R> Higher<Higher<tuple2, T1>, R> ap(Higher<Higher<tuple2, T1>, ? extends Function<T, R>> fn, Higher<Higher<tuple2, T1>, T> apply) {
                    return Instances.<T1>applicative(m).ap(fn,apply);
                }

                @Override
                public <C2, T, R> Higher<C2, Higher<Higher<tuple2, T1>, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<Higher<tuple2, T1>, T> ds) {
                    Tuple2<T1, T> id = narrowK(ds);
                    Function<R, Tuple2<T1,R>> rightFn = r -> of(id._1(),r);
                    return applicative.map(rightFn, fn.apply(id._2()));
                }

                @Override
                public <C2, T> Higher<C2, Higher<Higher<tuple2, T1>, T>> sequenceA(Applicative<C2> applicative, Higher<Higher<tuple2, T1>, Higher<C2, T>> ds) {
                    return traverseA(applicative,Function.identity(),ds);
                }

                @Override
                public <T, R> Higher<Higher<tuple2, T1>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<tuple2, T1>, T> ds) {
                    return Instances.<T1>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<tuple2, T1>, T> unit(T value) {
                    return Instances.unit(m).unit(value);
                }
            };
        }
        public static <T1> Foldable<Higher<tuple2, T1>> foldable(){
            return new Foldable<Higher<tuple2, T1>>(){
                @Override
                public <T> T foldRight(Monoid<T> monoid, Higher<Higher<tuple2, T1>, T> ds) {
                    return monoid.apply(narrowK(ds)._2(),monoid.zero());
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<Higher<tuple2, T1>, T> ds) {
                    return monoid.apply(monoid.zero(),narrowK(ds)._2());
                }

                @Override
                public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<Higher<tuple2, T1>, T> nestedA) {
                    return foldLeft(mb,narrowK(nestedA).<R>map2(fn));
                }

            };
        }
        public static <T1> Comonad<Higher<tuple2, T1>> comonad(Monoid<T1> m){
            return new ComonadByPure<Higher<tuple2, T1>>(){
                @Override
                public <T> T extract(Higher<Higher<tuple2, T1>, T> ds) {
                    return narrowK(ds)._2();
                }

                @Override
                public <T, R> Higher<Higher<tuple2, T1>, R> map(Function<? super T, ? extends R> fn, Higher<Higher<tuple2, T1>, T> ds) {
                    return Instances.<T1>functor().map(fn,ds);
                }

                @Override
                public <T> Higher<Higher<tuple2, T1>, T> unit(T value) {
                    return Instances.unit(m).unit(value);
                }

            };
        }
    }
    public static  <T1,T2> Kleisli<Higher<tuple2,T1>,Tuple2<T1,T2>,T2> kindKleisli(Monoid<T1> m){
        return Kleisli.of(Instances.monad(m), Tuple2::widen);
    }
    public static <T1,T2> Higher2<tuple2,T1, T2> widen(Tuple2<T1,T2> narrow) {
        return narrow;
    }
    public static  <T1,T2> Cokleisli<Higher<tuple2,T1>,T2,Tuple2<T1,T2>> kindCokleisli(){
        return Cokleisli.of(Tuple2::narrowK);
    }
}
