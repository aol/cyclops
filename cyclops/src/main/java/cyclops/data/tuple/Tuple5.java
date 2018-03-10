package cyclops.data.tuple;


import com.oath.cyclops.types.foldable.To;
import cyclops.data.Comparators;
import cyclops.function.*;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/*
  A Tuple implementation that can be lazyEither eager / strict or lazy


 */
@AllArgsConstructor
public class Tuple5<T1,T2,T3,T4,T5> implements To<Tuple5<T1,T2,T3,T4,T5>>,
                                                Serializable,
                                                Comparable<Tuple5<T1,T2,T3,T4,T5>>{

    private static final long serialVersionUID = 1L;

    public static <T1,T2,T3,T4,T5> Tuple5<T1,T2,T3,T4,T5> of(T1 value1, T2 value2, T3 value3, T4 value4, T5 value5) {
        return new Tuple5<>(value1,value2,value3,value4,value5);
    }
    public static <T1,T2,T3,T4,T5> Tuple5<T1,T2,T3,T4,T5> lazy(Supplier<? extends T1> supplier1,
                                                         Supplier<? extends T2> supplier2,
                                                         Supplier<? extends T3> supplier3,
                                                         Supplier<? extends T4> supplier4,
                                                               Supplier<? extends T5> supplier5) {
        return new Tuple5<T1,T2,T3,T4,T5>(null,null,null,null,null) {
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

            @Override
            public T5 _5() {
                return supplier5.get();
            }
        };
    }
    private final T1 _1;
    private final T2 _2;
    private final T3 _3;
    private final T4 _4;
    private final T5 _5;

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
    public  T5 _5(){
        return _5;
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

    public Tuple1<T5> fifth(){
        return Tuple.tuple(_5());
    }

    public Tuple5<T1,T2,T3,T4,T5> eager(){
        return of(_1(),_2(),_3(),_4(),_5());
    }

    public <R1> R1 transform(Function5<? super T1, ? super T2, ? super T3,? super T4, ? super T5, ? extends R1> fn){
      return fn.apply(_1(),_2(),_3(),_4(),_5());
    }

    public Tuple5<T1,T2,T3,T4,T5> memo(){
        Tuple5<T1,T2,T3,T4,T5> host = this;
        return new Tuple5<T1,T2,T3,T4,T5>(null,null, null,null, null){
            final Supplier<T1> memo1 = Memoize.memoizeSupplier(host::_1);
            final Supplier<T2> memo2 = Memoize.memoizeSupplier(host::_2);
            final Supplier<T3> memo3 = Memoize.memoizeSupplier(host::_3);
            final Supplier<T4> memo4 = Memoize.memoizeSupplier(host::_4);
            final Supplier<T5> memo5 = Memoize.memoizeSupplier(host::_5);
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
            @Override
            public T5 _5() {
                return memo5.get();
            }
        };
    }

    public <R1,R2,R3,R4,R5> Tuple5<R1,R2,R3,R4,R5> mapAll(Function<? super T1, ? extends R1> fn1, Function<? super T2,? extends R2> fn2,
                                                            Function<? super T3,? extends R3> fn3,
                                                            Function<? super T4,? extends R4> fn4,
                                                            Function<? super T5,? extends R5> fn5){
        return of( fn1.apply(_1()),
                    fn2.apply(_2()),
                    fn3.apply(_3()),
                    fn4.apply(_4()),
                    fn5.apply(_5()));
    }

    public <R1,R2,R3,R4,R5> Tuple5<R1,R2,R3,R4,R5> lazyMapAll(Function<? super T1, ? extends R1> fn1, Function<? super T2,? extends R2> fn2,
                                                        Function<? super T3,? extends R3> fn3,
                                                        Function<? super T4,? extends R4> fn4,
                                                              Function<? super T5,? extends R5> fn5){
        return lazy(()->(fn1.apply(_1())),()->fn2.apply(_2()),()->fn3.apply(_3()),()->fn4.apply(_4()),
                ()->fn5.apply(_5()));
    }
    public <R> Tuple5<R, T2,T3,T4,T5> map1(Function<? super T1, ? extends R> fn) {
        return of(fn.apply(_1()), _2(),_3(),_4(),_5());
    }
    public <R> Tuple5<R, T2,T3,T4,T5> lazyMap1(Function<? super T1, ? extends R> fn) {
        return lazy(()->fn.apply(_1()),()-> _2(),()->_3(),()->_4(),()->_5());
    }
    public <R> Tuple5<T1, R, T3, T4,T5> map2(Function<? super T2, ? extends R> fn) {
        return of(_1(), fn.apply(_2()),_3(),_4(),_5());
    }
    public <R> Tuple5<T1, R, T3,T4,T5> lazyMap2(Function<? super T2, ? extends R> fn) {
        return lazy(() -> _1(), () -> fn.apply(_2()),()->_3(),()->_4(),()->_5());
    }
    public <R> Tuple5<T1, T2, R,T4,T5> map3(Function<? super T3, ? extends R> fn) {
        return of(_1(), _2(),fn.apply(_3()),_4(),_5());
    }
    public <R> Tuple5<T1, T2, R,T4,T5> lazyMap3(Function<? super T3, ? extends R> fn) {
        return lazy(() -> _1(), () -> _2(),()->fn.apply(_3()),()->_4(),()->_5());
    }
    public <R> Tuple5<T1, T2, T3, R , T5> map4(Function<? super T4, ? extends R> fn) {
        return of(_1(), _2(),_3(),fn.apply(_4()),_5());
    }
    public <R> Tuple5<T1, T2, T3, R, T5> lazyMap4(Function<? super T4, ? extends R> fn) {
        return lazy(() -> _1(), () -> _2(),()->_3(),()->fn.apply(_4()),()->_5());
    }
    public <R> Tuple5<T1, T2, T3, T4 , R> map5(Function<? super T5, ? extends R> fn) {
        return of(_1(), _2(),_3(),_4(),fn.apply(_5()));
    }
    public <R> Tuple5<T1, T2, T3, T4, R> lazyMap5(Function<? super T5, ? extends R> fn) {
        return lazy(() -> _1(), () -> _2(),()->_3(),()->_4(),()->fn.apply(_5()));
    }

    public <R> R visit(Function5<? super T1, ? super T2, ? super T3,? super T4, ? super T5,? extends R> fn){
        return fn.apply(_1(),_2(),_3(),_4(),_5());
    }
    @Override
    public int compareTo(Tuple5<T1, T2, T3, T4,T5> o) {
        int result = Comparators.naturalOrderIdentityComparator().compare(_1(),o._1());
        if(result==0){
            result = Comparators.naturalOrderIdentityComparator().compare(_2(),o._2());
            if(result==0){
                result = Comparators.naturalOrderIdentityComparator().compare(_3(),o._3());
                if(result==0){
                    result = Comparators.naturalOrderIdentityComparator().compare(_4(),o._4());
                    if(result==0){
                        result = Comparators.naturalOrderIdentityComparator().compare(_5(),o._5());
                    }
                }
            }

        }
        return result;
    }
    @Override
    public String toString() {
        return String.format("[%s,%s,%s,%s,%s]", _1(),_2(),_3(),_4(),_5());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Tuple5)) return false;
        Tuple5<?, ?, ?, ?, ?> tuple5 = (Tuple5<?, ?, ?, ?, ?>) o;
        return Objects.equals(_1(), tuple5._1()) &&
                Objects.equals(_2(), tuple5._2()) &&
                Objects.equals(_3(), tuple5._3()) &&
                Objects.equals(_4(), tuple5._4()) &&
                Objects.equals(_5(), tuple5._5());
    }

    @Override
    public int hashCode() {
        return Objects.hash(_1(), _2(), _3(), _4(), _5());
    }

    public final Object[] toArray() {
        return new Object[] { _1(),_2(),_3(),_4(),_5() };
    }
}
