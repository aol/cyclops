package cyclops.data.tuple;


import com.oath.cyclops.hkt.DataWitness.tuple1;
import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.types.Filters;
import com.oath.cyclops.types.foldable.EqualTo;
import com.oath.cyclops.types.foldable.OrderedBy;
import com.oath.cyclops.types.foldable.To;
import com.oath.cyclops.types.functor.Transformable;
import cyclops.control.Either;
import cyclops.control.Option;
import cyclops.control.Trampoline;
import cyclops.data.Comparators;
import cyclops.function.Memoize;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.*;

/*
  A Tuple1 implementation that can be either eager / strict or lazy
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




    public <R> Tuple1<R> map(Function<? super T, ? extends R> fn){
        return of((fn.apply(_1())));
    }

    @Override
    public Tuple1<T> peek(Consumer<? super T> c) {
        return (Tuple1<T>)Transformable.super.peek(c);
    }



    public Tuple1<T> eager(){
        return of(_1());
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
    public Option<T> filter(Predicate<? super T> predicate) {
        return predicate.test(_1()) ? Option.some(_1()) :  Option.none();
    }

    @Override
    public <U> Option<U> ofType(Class<? extends U> type) {
        return (Option<U>)Filters.super.ofType(type);
    }

    @Override
    public Option<T> filterNot(Predicate<? super T> predicate) {
        return (Option<T>)Filters.super.filterNot(predicate);
    }

    @Override
    public Option<T> notNull() {
        return (Option<T>)Filters.super.notNull();
    }


    public final Object[] toArray() {
        return new Object[] { _1() };
    }

    public  <T2> Tuple2<T, T2> concat(Tuple1<T2> tuple) {
        return Tuple.tuple(_1(),tuple._1());
    }
    public  <T2,T3> Tuple3<T, T2,T3> concat(Tuple2<T2,T3> tuple) {
        return Tuple.tuple(_1(),tuple._1(),tuple._2());
    }


    public  <T2, T3, T4> Tuple4<T, T2, T3, T4> concat(Tuple3<T2, T3, T4> tuple) {
        return Tuple.tuple(_1(), tuple._1(), tuple._2(), tuple._3());
    }


    public  <T2, T3, T4, T5> Tuple5<T, T2, T3, T4, T5> concat(Tuple4<T2, T3, T4, T5> tuple) {
        return Tuple.tuple(_1(), tuple._1(), tuple._2(), tuple._3(), tuple._4());
    }


    public  <T2, T3, T4, T5, T6> Tuple6<T, T2, T3, T4, T5, T6> concat(Tuple5<T2, T3, T4, T5, T6> tuple) {
        return Tuple.tuple(_1(), tuple._1(), tuple._2(), tuple._3(), tuple._4(), tuple._5());
    }


    public  <T2, T3, T4, T5, T6, T7> Tuple7<T, T2, T3, T4, T5, T6, T7> concat(Tuple6<T2, T3, T4, T5, T6, T7> tuple) {
        return Tuple.tuple(_1(),tuple._1(), tuple._2(),tuple._3(), tuple._4(),tuple._5(), tuple._6());
    }


    public  <T2, T3, T4, T5, T6, T7, T8> Tuple8<T, T2, T3, T4, T5, T6, T7, T8> concat(Tuple7<T2, T3, T4, T5, T6, T7, T8> tuple) {
        return Tuple.tuple(_1(), tuple._1(),tuple._2(), tuple._3(), tuple._4(), tuple._5(),tuple._6(), tuple._7());
    }


    public  <T2> Tuple2<T, T2> lazyConcat(Tuple1<T2> tuple) {
        return Tuple2.lazy(()->_1(),()->tuple._1());
    }
    public  <T2,T3> Tuple3<T, T2,T3> lazyConcat(Tuple2<T2,T3> tuple) {
        return Tuple3.lazy(()->_1(),()->tuple._1(),()->tuple._2());
    }


    public  <T2, T3, T4> Tuple4<T, T2, T3, T4> lazyConcat(Tuple3<T2, T3, T4> tuple) {
        return Tuple4.lazy(()->_1(), ()->tuple._1(), ()->tuple._2(), ()->tuple._3());
    }


    public  <T2, T3, T4, T5> Tuple5<T, T2, T3, T4, T5> lazyConcat(Tuple4<T2, T3, T4, T5> tuple) {
        return Tuple5.lazy(()->_1(), ()->tuple._1(), ()->tuple._2(), ()->tuple._3(), ()->tuple._4());
    }


    public  <T2, T3, T4, T5, T6> Tuple6<T, T2, T3, T4, T5, T6> lazyConcat(Tuple5<T2, T3, T4, T5, T6> tuple) {
        return Tuple6.lazy(()->_1(), ()->tuple._1(), ()->tuple._2(), ()->tuple._3(), ()->tuple._4(), ()->tuple._5());
    }


    public  <T2, T3, T4, T5, T6, T7> Tuple7<T, T2, T3, T4, T5, T6, T7> lazyConcat(Tuple6<T2, T3, T4, T5, T6, T7> tuple) {
        return Tuple7.lazy(()->_1(),()->tuple._1(), ()->tuple._2(),()-> tuple._3(), ()->tuple._4(),()-> tuple._5(), ()->tuple._6());
    }


    public  <T2, T3, T4, T5, T6, T7, T8> Tuple8<T, T2, T3, T4, T5, T6, T7, T8> lazyConcat(Tuple7<T2, T3, T4, T5, T6, T7, T8> tuple) {
        return Tuple8.lazy(()->_1(), ()->tuple._1(),()-> tuple._2(), ()->tuple._3(), ()->tuple._4(), ()->tuple._5(), ()->tuple._6(), ()->tuple._7());
    }




}
