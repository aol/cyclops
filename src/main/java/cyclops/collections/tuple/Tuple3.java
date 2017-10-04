package cyclops.collections.tuple;


import com.aol.cyclops2.hkt.Higher;
import com.aol.cyclops2.hkt.Higher2;
import com.aol.cyclops2.hkt.Higher3;
import com.aol.cyclops2.types.foldable.EqualTo;
import com.aol.cyclops2.types.foldable.OrderedBy;
import com.aol.cyclops2.types.foldable.To;
import cyclops.control.Maybe;
import cyclops.control.Xor;
import cyclops.function.Fn3;
import cyclops.function.Memoize;
import cyclops.function.Monoid;
import cyclops.monads.Witness;
import cyclops.monads.Witness.tuple2;
import cyclops.monads.Witness.tuple3;
import cyclops.typeclasses.*;
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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/*
  A Tuple implementation that can be either eager / strict or lazy


 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Tuple3<T1,T2,T3> implements To<Tuple3<T1,T2,T3>>,
                                        Serializable,
                                        Higher3<tuple3,T1,T2,T3> ,
                                        EqualTo<Higher<Higher<tuple3,T1>,T2>,T3,Tuple3<T1,T2,T3>>,
                                        OrderedBy<Higher<Higher<tuple3,T1>,T2>,T3,Tuple3<T1,T2,T3>>{

    private static final long serialVersionUID = 1L;

    public static <T1,T2,T3> Tuple3<T1,T2,T3> of(T1 value1, T2 value2, T3 value3) {
        return new Tuple3<>(value1,value2,value3);
    }
    public static <T1,T2,T3> Tuple3<T1,T2,T3> lazy(Supplier<? extends T1> supplier1, Supplier<? extends T2> supplier2,Supplier<? extends T3> supplier3) {
        return new Tuple3<T1,T2,T3>(null,null,null) {
            @Override
            public T1 _1() {
                return supplier1.get();
            }
            @Override
            public T2 _2() {
                return supplier2.get();
            }

            @Override
            public T3 _3() {
                return supplier3.get();
            }
        };
    }
    private final T1 _1;
    private final T2 _2;
    private final T3 _3;

    public T1 _1(){
        return _1;
    }

    public  T2 _2(){
        return _2;
    }
    public  T3 _3(){
        return _3;
    }

    public Tuple1<T1> first(){
        return Tuple.tuple(_1());
    }

    public Tuple1<T2> second(){
        return Tuple.tuple(_2());
    }

    public Tuple1<T3> third(){
        return Tuple.tuple(_3());
    }

    public Tuple3<T1,T2,T3> eager(){
        return of(_1(),_2(),_3());
    }
    public Tuple3<T1,T2,T3> memo(){
        Tuple3<T1,T2,T3> host = this;
        return new Tuple3<T1,T2,T3>(null,null, null){
            final Supplier<T1> memo1 = Memoize.memoizeSupplier(host::_1);
            final Supplier<T2> memo2 = Memoize.memoizeSupplier(host::_2);
            final Supplier<T3> memo3 = Memoize.memoizeSupplier(host::_3);
            @Override
            public T1 _1() {

                return memo1.get();
            }

            @Override
            public T2 _2() {
                return memo2.get();
            }
            @Override
            public T3 _3() {
                return memo3.get();
            }
        };
    }

    public <R> Tuple3<T1,T2,R> flatMap(Monoid<T1> m1, Monoid<T2> m2,Function<? super T3, ? extends Tuple3<T1,T2,R>> fn){
        return fn.apply(_3()).map1(t1->m1.apply(t1,_1())).map2(t2->m2.apply(t2,_2()));
    }
    public <R1,R2,R3> Tuple3<R1,R2,R3> mapAll(Function<? super T1, ? extends R1> fn1, Function<? super T2,? extends R2> fn2,
                                                Function<? super T3,? extends R3> fn3){
        return of( fn1.apply(_1()),
                    fn2.apply(_2()),
                    fn3.apply(_3()));
    }

    public <R1,R2,R3> Tuple3<R1,R2,R3> lazyMapAll(Function<? super T1, ? extends R1> fn1, Function<? super T2,? extends R2> fn2,
                                                  Function<? super T3,? extends R3> fn3){
        return lazy(()->(fn1.apply(_1())),()->fn2.apply(_2()),()->fn3.apply(_3()));
    }
    public <R> Tuple3<R, T2,T3> map1(Function<? super T1, ? extends R> fn) {
        return of(fn.apply(_1()), _2(),_3());
    }
    public <R> Tuple3<R, T2,T3> lazyMap1(Function<? super T1, ? extends R> fn) {
        return lazy(()->fn.apply(_1()),()-> _2(),()->_3());
    }
    public <R> Tuple3<T1, R, T3> map2(Function<? super T2, ? extends R> fn) {
        return of(_1(), fn.apply(_2()),_3());
    }
    public <R> Tuple3<T1, R, T3> lazyMap2(Function<? super T2, ? extends R> fn) {
        return lazy(() -> _1(), () -> fn.apply(_2()),()->_3());
    }
    public <R> Tuple3<T1, T2, R> map3(Function<? super T3, ? extends R> fn) {
        return of(_1(), _2(),fn.apply(_3()));
    }
    public <R> Tuple3<T1, T2, R> lazyMap3(Function<? super T3, ? extends R> fn) {
        return lazy(() -> _1(), () -> _2(),()->fn.apply(_3()));
    }


    public <R> R visit(Fn3<? super T1, ? super T2, ? super T3, ? extends R> fn){
        return fn.apply(_1(),_2(),_3());
    }

    @Override
    public String toString() {
        return String.format("[%s,%s,%s]", _1(),_2(),_3());
    }


    public static <T1,T2,T3> Tuple3<T1,T2,T3> narrowK3(Higher3<tuple3,T1,T2,T3> ds){
        return (Tuple3<T1,T2,T3>)ds;
    }
    public static <T1,T2,T3> Tuple3<T1,T2,T3> narrowK(Higher<Higher<Higher<tuple3,T1>,T2>,T3> ds){
        return (Tuple3<T1,T2,T3>)ds;
    }


}
