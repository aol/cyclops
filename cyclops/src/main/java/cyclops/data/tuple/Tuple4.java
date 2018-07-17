package cyclops.data.tuple;


import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher4;
import com.oath.cyclops.types.foldable.EqualTo;
import com.oath.cyclops.types.foldable.OrderedBy;
import com.oath.cyclops.types.foldable.To;
import cyclops.companion.Comparators;
import cyclops.function.Function4;
import cyclops.function.Memoize;
import cyclops.function.Monoid;
import com.oath.cyclops.hkt.DataWitness.tuple4;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/*
  A Tuple implementation that can be either eager / strict or lazy


 */
@AllArgsConstructor
public class Tuple4<T1,T2,T3,T4> implements To<Tuple4<T1,T2,T3,T4>>,
                                        Serializable,
                                        Higher4<tuple4,T1,T2,T3,T4>,
                                        EqualTo<Higher<Higher<Higher<tuple4,T1>,T2>,T3>,T4,Tuple4<T1,T2,T3,T4>>,
                                        OrderedBy<Higher<Higher<Higher<tuple4,T1>,T2>,T3>,T4,Tuple4<T1,T2,T3,T4>>,
                                        Comparable<Tuple4<T1,T2,T3,T4>>{

    private static final long serialVersionUID = 1L;

    public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> of(T1 value1, T2 value2, T3 value3, T4 value4) {
        return new Tuple4<>(value1,value2,value3,value4);
    }
    public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> lazy(Supplier<? extends T1> supplier1,
                                                         Supplier<? extends T2> supplier2,
                                                         Supplier<? extends T3> supplier3,
                                                         Supplier<? extends T4> supplier4) {
        return new Tuple4<T1,T2,T3,T4>(null,null,null,null) {
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

            @Override
            public T4 _4() {
                return supplier4.get();
            }
        };
    }
    private final T1 _1;
    private final T2 _2;
    private final T3 _3;
    private final T4 _4;

    public T1 _1(){
        return _1;
    }

    public  T2 _2(){
        return _2;
    }
    public  T3 _3(){
        return _3;
    }
    public  T4 _4(){
        return _4;
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

    public Tuple1<T4> fourth(){
        return Tuple.tuple(_4());
    }

    public Tuple4<T1,T2,T3,T4> eager(){
        return of(_1(),_2(),_3(),_4());
    }

    public <R1> R1 transform(Function4<? super T1, ? super T2, ? super T3,? super T4, ? extends R1> fn){
      return fn.apply(_1(),_2(),_3(),_4());
    }

    public Tuple4<T1,T2,T3,T4> memo(){
        Tuple4<T1,T2,T3,T4> host = this;
        return new Tuple4<T1,T2,T3,T4>(null,null, null,null){
            final Supplier<T1> memo1 = Memoize.memoizeSupplier(host::_1);
            final Supplier<T2> memo2 = Memoize.memoizeSupplier(host::_2);
            final Supplier<T3> memo3 = Memoize.memoizeSupplier(host::_3);
            final Supplier<T4> memo4 = Memoize.memoizeSupplier(host::_4);
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
            @Override
            public T4 _4() {
                return memo4.get();
            }
        };
    }

    public <R> Tuple4<T1,T2,T3,R> flatMap(Monoid<T1> m1, Monoid<T2> m2,Monoid<T3> m3, Function<? super T4, ? extends Tuple4<T1,T2,T3,R>> fn){
        return fn.apply(_4())
                .map1(t1->m1.apply(t1,_1()))
                .map2(t2->m2.apply(t2,_2()))
                .map3(t3->m3.apply(t3,_3()));
    }
    public <R1,R2,R3,R4> Tuple4<R1,R2,R3,R4> mapAll(Function<? super T1, ? extends R1> fn1, Function<? super T2,? extends R2> fn2,
                                              Function<? super T3,? extends R3> fn3,
                                                     Function<? super T4,? extends R4> fn4){
        return of( fn1.apply(_1()),
                    fn2.apply(_2()),
                    fn3.apply(_3()),
                    fn4.apply(_4()));
    }

    public <R1,R2,R3,R4> Tuple4<R1,R2,R3,R4> lazyMapAll(Function<? super T1, ? extends R1> fn1, Function<? super T2,? extends R2> fn2,
                                                  Function<? super T3,? extends R3> fn3,
                                                        Function<? super T4,? extends R4> fn4){
        return lazy(()->(fn1.apply(_1())),()->fn2.apply(_2()),()->fn3.apply(_3()),()->fn4.apply(_4()));
    }
    public <R> Tuple4<R, T2,T3,T4> map1(Function<? super T1, ? extends R> fn) {
        return of(fn.apply(_1()), _2(),_3(),_4());
    }
    public <R> Tuple4<R, T2,T3,T4> lazyMap1(Function<? super T1, ? extends R> fn) {
        return lazy(()->fn.apply(_1()),()-> _2(),()->_3(),()->_4());
    }
    public <R> Tuple4<T1, R, T3, T4> map2(Function<? super T2, ? extends R> fn) {
        return of(_1(), fn.apply(_2()),_3(),_4());
    }
    public <R> Tuple4<T1, R, T3,T4> lazyMap2(Function<? super T2, ? extends R> fn) {
        return lazy(() -> _1(), () -> fn.apply(_2()),()->_3(),()->_4());
    }
    public <R> Tuple4<T1, T2, R,T4> map3(Function<? super T3, ? extends R> fn) {
        return of(_1(), _2(),fn.apply(_3()),_4());
    }
    public <R> Tuple4<T1, T2, R,T4> lazyMap3(Function<? super T3, ? extends R> fn) {
        return lazy(() -> _1(), () -> _2(),()->fn.apply(_3()),()->_4());
    }
    public <R> Tuple4<T1, T2, T3, R> map4(Function<? super T4, ? extends R> fn) {
        return of(_1(), _2(),_3(),fn.apply(_4()));
    }
    public <R> Tuple4<T1, T2, T3, R> lazyMap4(Function<? super T4, ? extends R> fn) {
        return lazy(() -> _1(), () -> _2(),()->_3(),()->fn.apply(_4()));
    }

    public <R> R fold(Function4<? super T1, ? super T2, ? super T3,? super T4, ? extends R> fn){
        return fn.apply(_1(),_2(),_3(),_4());
    }

    @Override
    public String toString() {
        return String.format("[%s,%s,%s,%s]", _1(),_2(),_3(),_4());
    }


    public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> narrowK3(Higher4<tuple4,T1,T2,T3,T4> ds){
        return (Tuple4<T1,T2,T3,T4>)ds;
    }
    public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> narrowK(Higher<Higher<Higher<Higher<tuple4,T1>,T2>,T3>,T4> ds){
        return (Tuple4<T1,T2,T3,T4>)ds;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Tuple4)) return false;
        Tuple4<?, ?, ?, ?> tuple4 = (Tuple4<?, ?, ?, ?>) o;
        return Objects.equals(_1(), tuple4._1()) &&
                Objects.equals(_2(), tuple4._2()) &&
                Objects.equals(_3(), tuple4._3()) &&
                Objects.equals(_4(), tuple4._4());
    }

    @Override
    public int hashCode() {
        return Objects.hash(_1(), _2(), _3(), _4());
    }

    @Override
    public int compareTo(Tuple4<T1, T2, T3, T4> o) {
        int result = Comparators.naturalOrderIdentityComparator().compare(_1(),o._1());
        if(result==0){
            result = Comparators.naturalOrderIdentityComparator().compare(_2(),o._2());
            if(result==0){
                result = Comparators.naturalOrderIdentityComparator().compare(_3(),o._3());
                if(result==0){
                    result = Comparators.naturalOrderIdentityComparator().compare(_4(),o._4());
                }
            }

        }
        return result;
    }

    public final Object[] toArray() {
        return new Object[] { _1(),_2(),_3(),_4() };
    }

    public  <  T5> Tuple5<T1, T2, T3, T4, T5>  concat(Tuple1<T5> tuple) {
        return Tuple.tuple(_1(),_2(),_3(),_4(),tuple._1());
    }
    public  <T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> concat(Tuple2<T5,T6> tuple) {
        return  Tuple.tuple(_1(),_2(),_3(),_4(),tuple._1(),tuple._2());
    }


    public  <T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> concat(Tuple3<T5, T6, T7> tuple) {
        return  Tuple.tuple(_1(),_2(),_3(),_4(),tuple._1(), tuple._2(), tuple._3());
    }


    public  <T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8>  concat(Tuple4<T5, T6, T7, T8> tuple) {
        return  Tuple.tuple(_1(),_2(),_3(),_4(), tuple._1(), tuple._2(), tuple._3(), tuple._4());
    }

    public  <T5> Tuple5<T1, T2, T3, T4, T5>  lazyConcat(Tuple1<T5> tuple) {
        return Tuple.lazy(()->_1(),()->_2,()->_3,()->_4,()->tuple._1());
    }
    public  <T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> lazyConcat(Tuple2<T5,T6> tuple) {
        return Tuple.lazy(()->_1(),()->_2,()->_3,()->_4,()->tuple._1(),()->tuple._2());
    }


    public  <T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> lazyConcat(Tuple3<T5, T6, T7> tuple) {
        return Tuple.lazy(()->_1(),()->_2,()->_3,()->_4, ()->tuple._1(), ()->tuple._2(), ()->tuple._3());
    }


    public  <T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> lazyConcat(Tuple4<T5, T6, T7, T8> tuple) {
        return Tuple.lazy(()->_1(),()->_2,()->_3,()->_4, ()->tuple._1(), ()->tuple._2(), ()->tuple._3(), ()->tuple._4());
    }




}
