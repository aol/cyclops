package cyclops.data.tuple;


import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.types.Filters;
import com.aol.cyclops2.types.foldable.EqualTo;
import com.aol.cyclops2.types.foldable.OrderedBy;
import com.aol.cyclops2.types.foldable.To;
import com.aol.cyclops2.types.functor.Transformable;
import cyclops.control.Identity;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;
import cyclops.control.Either;
import cyclops.data.Comparators;
import cyclops.function.Memoize;
import cyclops.function.Monoid;
import cyclops.control.anym.Witness.tuple1;
import cyclops.typeclasses.*;
import cyclops.typeclasses.comonad.Comonad;
import cyclops.typeclasses.comonad.ComonadByPure;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.*;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

/*
  A Tuple1 implementation that can be lazyEither eager / strict or lazy
  Roughly analogous to the Identity monad

 */
@AllArgsConstructor
public class Tuple1<T> implements To<Tuple1<T>>,
                                  Serializable,
                                  Transformable<T>,
                                  Filters<T>,
                                  EqualTo<tuple1,T,Tuple1<T>>,
                                  OrderedBy<tuple1,T,Tuple1<T>>,
                                  Comparable<Tuple1<T>>,
                                  Higher<tuple1,T>{

    private static final long serialVersionUID = 1L;

    public static <T> Tuple1<T> of(T value) {
        return new Tuple1<T>(value);
    }
    public static <T> Tuple1<T> lazy(Supplier<? extends T> supplier) {
        return new Tuple1<T>(null) {
            @Override
            public T _1() {
                return supplier.get();
            }
        };
    }


    public Tuple1<T> memo(){
        Tuple1<T> host = this;
        return new Tuple1<T>(null){
            final Supplier<T> memo = Memoize.memoizeSupplier(host::_1);
            @Override
            public T _1() {

                return memo.get();
            }
        };
    }

    private final T _1;

    public T _1(){
        return _1;
    }

    public Identity<T> toIdentity(){
        return Identity.of(_1());
    }


    public <R> Tuple1<R> map(Function<? super T, ? extends R> fn){
        return of((fn.apply(_1())));
    }

    @Override
    public Tuple1<T> peek(Consumer<? super T> c) {
        return (Tuple1<T>)Transformable.super.peek(c);
    }

    @Override
    public <R> Tuple1<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
        return (Tuple1<R>)Transformable.super.trampoline(mapper);
    }

    @Override
    public <R> Tuple1<R> retry(Function<? super T, ? extends R> fn) {
        return (Tuple1<R>)Transformable.super.retry(fn);
    }

    public Tuple1<T> eager(){
        return of(_1());
    }
    @Override
    public <R> Tuple1<R> retry(Function<? super T, ? extends R> fn, int retries, long delay, TimeUnit timeUnit) {
        return (Tuple1<R>)Transformable.super.retry(fn,retries,delay,timeUnit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Tuple1)) return false;
        Tuple1<?> tuple1 = (Tuple1<?>) o;
        return Objects.equals(_1(), tuple1._1());
    }
    @Override
    public int compareTo(Tuple1<T> o) {
        return Comparators.naturalOrderIdentityComparator().compare(_1(),o._1());
    }
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), _1());
    }

    public <R> Tuple1<R> lazyMap(Function<? super T, ? extends R> fn){
        return lazy(()->fn.apply(_1()));
    }
    public <T2,R> Tuple1<R> zip(Tuple1<T2> t2,BiFunction<? super T,? super T2, ? extends R > zipper) {
        return of(zipper.apply(_1(),t2._1()));
    }
    public <T2,R> Tuple1<R> lazyZip(Tuple1<T2> t2,BiFunction<? super T,? super T2, ? extends R > zipper) {
        return lazy(()->zipper.apply(_1(),t2._1()));
    }
    public <R> Tuple1<R> flatMap(Function<? super T, ? extends Tuple1<R>> fn){
        return fn.apply(_1());
    }
    public <R> Tuple1<R> lazyFlatMap(Function<? super T, ? extends Tuple1<R>> fn){
        return lazy(()->fn.apply(_1())._1());
    }
    public <R> R visit(Function<? super T, ? extends R> fn){
        return fn.apply(_1());
    }

    @Override
    public String toString() {
        return String.format("[%s]", _1());
    }

    public static <T> Tuple1<T> narrowK(Higher<tuple1,T> ds){
        return (Tuple1<T>)ds;
    }

    public static  <T,R> Tuple1<R> tailRec(T initial, Function<? super T, ? extends Tuple1<? extends Either<T, R>>> fn){
        Tuple1<? extends Either<T, R>> next[] = new Tuple1[1];
        next[0] = Tuple1.of(Either.left(initial));
        boolean cont = true;
        do {

            cont = next[0].visit(p -> p.visit(s -> {
                next[0] = narrowK(fn.apply(s));
                return true;
            }, __ -> false));
        } while (cont);
        return next[0].map(x->x.orElse(null));
    }

    @Override
    public Maybe<T> filter(Predicate<? super T> predicate) {
        return predicate.test(_1()) ? Maybe.just(_1()) :  Maybe.nothing();
    }

    @Override
    public <U> Maybe<U> ofType(Class<? extends U> type) {
        return (Maybe<U>)Filters.super.ofType(type);
    }

    @Override
    public Maybe<T> filterNot(Predicate<? super T> predicate) {
        return (Maybe<T>)Filters.super.filterNot(predicate);
    }

    @Override
    public Maybe<T> notNull() {
        return (Maybe<T>)Filters.super.notNull();
    }

    public static class Instances {


        public static InstanceDefinitions<tuple1> definitions(){
            return new InstanceDefinitions<tuple1>() {
                @Override
                public <T, R> Functor<tuple1> functor() {
                    return Tuple1.Instances.functor();
                }

                @Override
                public <T> Pure<tuple1> unit() {
                    return Tuple1.Instances.unit();
                }

                @Override
                public <T, R> Applicative<tuple1> applicative() {
                    return Tuple1.Instances.applicative();
                }

                @Override
                public <T, R> Monad<tuple1> monad() {
                    return Tuple1.Instances.monad();
                }

                @Override
                public <T, R> Maybe<MonadZero<tuple1>> monadZero() {
                    return Maybe.nothing();
                }

                @Override
                public <T> Maybe<MonadPlus<tuple1>> monadPlus() {
                    return Maybe.nothing();
                }

                @Override
                public <T> MonadRec<tuple1> monadRec() {
                    return Tuple1.Instances.monadRec();
                }

                @Override
                public <T> Maybe<MonadPlus<tuple1>> monadPlus(Monoid<Higher<tuple1, T>> m) {
                    return Maybe.nothing();
                }

                @Override
                public <C2, T> Traverse<tuple1> traverse() {
                    return Tuple1.Instances.traverse();
                }

                @Override
                public <T> Foldable<tuple1> foldable() {
                    return Tuple1.Instances.foldable();
                }

                @Override
                public <T> Maybe<Comonad<tuple1>> comonad() {
                    return Maybe.just(Tuple1.Instances.comonad());
                }

                @Override
                public <T> Maybe<Unfoldable<tuple1>> unfoldable() {
                    return Maybe.nothing();
                }
            };
        }

        public static Functor<tuple1> functor(){
            return new Functor<tuple1>(){
                @Override
                public <T, R> Higher<tuple1, R> map(Function<? super T, ? extends R> fn, Higher<tuple1, T> ds) {
                    return narrowK(ds).map(fn);
                }
            };
        }

        public static Pure<tuple1> unit(){
            return new Pure<tuple1>(){
                @Override
                public <T> Higher<tuple1, T> unit(T value) {
                    return of(value);
                }
            };
        }
        public static Applicative<tuple1> applicative(){
            return new Applicative<tuple1>(){


                @Override
                public <T, R> Higher<tuple1, R> ap(Higher<tuple1, ? extends Function<T, R>> fn, Higher<tuple1, T> apply) {
                    Tuple1<? extends Function<T, R>> f = narrowK(fn);
                    Tuple1<T> ap = narrowK(apply);
                    return f.flatMap(x -> ap.map(x));
                }

                @Override
                public <T, R> Higher<tuple1, R> map(Function<? super T, ? extends R> fn, Higher<tuple1, T> ds) {
                    return functor().map(fn,ds);
                }

                @Override
                public <T> Higher<tuple1, T> unit(T value) {
                    return Tuple1.Instances.unit().unit(value);
                }
            };
        }
        public static Monad<tuple1> monad(){
            return new Monad<tuple1>(){


                @Override
                public <T, R> Higher<tuple1, R> ap(Higher<tuple1, ? extends Function<T, R>> fn, Higher<tuple1, T> apply) {
                    return applicative().ap(fn,apply);
                }

                @Override
                public <T, R> Higher<tuple1, R> map(Function<? super T, ? extends R> fn, Higher<tuple1, T> ds) {
                    return functor().map(fn,ds);
                }

                @Override
                public <T> Higher<tuple1, T> unit(T value) {
                    return Tuple1.Instances.unit().unit(value);
                }

                @Override
                public <T, R> Higher<tuple1, R> flatMap(Function<? super T, ? extends Higher<tuple1, R>> fn, Higher<tuple1, T> ds) {
                    return narrowK(ds).flatMap(fn.andThen(i->narrowK(i)));
                }
            };
        }
        public static MonadRec<tuple1> monadRec() {

            return new MonadRec<tuple1>(){
                @Override
                public <T, R> Higher<tuple1, R> tailRec(T initial, Function<? super T, ? extends Higher<tuple1, ? extends Either<T, R>>> fn) {
                    return Tuple1.tailRec(initial,fn.andThen(Tuple1::narrowK));
                }

            };


        }
        public static Traverse<tuple1> traverse(){
            return new Traverse<tuple1>(){

                @Override
                public <T, R> Higher<tuple1, R> ap(Higher<tuple1, ? extends Function<T, R>> fn, Higher<tuple1, T> apply) {
                    return applicative().ap(fn,apply);
                }

                @Override
                public <T, R> Higher<tuple1, R> map(Function<? super T, ? extends R> fn, Higher<tuple1, T> ds) {
                    return functor().map(fn,ds);
                }

                @Override
                public <T> Higher<tuple1, T> unit(T value) {
                    return Tuple1.Instances.unit().unit(value);
                }

                @Override
                public <C2, T, R> Higher<C2, Higher<tuple1, R>> traverseA(Applicative<C2> applicative, Function<? super T, ? extends Higher<C2, R>> fn, Higher<tuple1, T> ds) {
                    Tuple1<T> id = narrowK(ds);
                    Function<R, Tuple1<R>> rightFn = r -> of(r);
                    return applicative.map(rightFn, fn.apply(id._1()));
                }

                @Override
                public <C2, T> Higher<C2, Higher<tuple1, T>> sequenceA(Applicative<C2> applicative, Higher<tuple1, Higher<C2, T>> ds) {
                    return traverseA(applicative,Function.identity(),ds);
                }
            };
        }
        public static Foldable<tuple1> foldable(){
            return new Foldable<tuple1>(){


                @Override
                public <T> T foldRight(Monoid<T> monoid, Higher<tuple1, T> ds) {
                    return monoid.apply(narrowK(ds)._1(),monoid.zero());
                }

                @Override
                public <T> T foldLeft(Monoid<T> monoid, Higher<tuple1, T> ds) {
                    return monoid.apply(monoid.zero(),narrowK(ds)._1());
                }

                @Override
                public <T, R> R foldMap(Monoid<R> mb, Function<? super T, ? extends R> fn, Higher<tuple1, T> nestedA) {
                    return foldLeft(mb,narrowK(nestedA).<R>map(fn));
                }
            };
        }
        public static Comonad<tuple1> comonad(){
            return new ComonadByPure<tuple1>(){


                @Override
                public <T> T extract(Higher<tuple1, T> ds) {
                    return narrowK(ds)._1();
                }

                @Override
                public <T, R> Higher<tuple1, R> map(Function<? super T, ? extends R> fn, Higher<tuple1, T> ds) {
                    return functor().map(fn,ds);
                }

                @Override
                public <T> Higher<tuple1, T> unit(T value) {
                    return Tuple1.Instances.unit().unit(value);
                }
            };
        }
    }
    public static  <T> Kleisli<tuple1,Tuple1<T>,T> kindKleisli(){
        return Kleisli.of(Tuple1.Instances.monad(), Tuple1::widen);
    }
    public static <T> Higher<tuple1, T> widen(Tuple1<T> narrow) {
        return narrow;
    }
    public static  <T> Cokleisli<tuple1,T,Tuple1<T>> kindCokleisli(){
        return Cokleisli.of(Tuple1::narrowK);
    }
}
