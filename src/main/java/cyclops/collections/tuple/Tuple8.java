package cyclops.collections.tuple;


import com.aol.cyclops2.types.foldable.To;
import cyclops.function.Fn7;
import cyclops.function.Fn8;
import cyclops.function.Memoize;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Supplier;

/*
  A Tuple implementation that can be either eager / strict or lazy


 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> implements To<Tuple8<T1,T2,T3,T4,T5,T6,T7,T8>>,
                                                Serializable{

    private static final long serialVersionUID = 1L;

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> of(T1 value1, T2 value2, T3 value3, T4 value4, T5 value5, T6 value6, T7 value7, T8 value8) {
        return new Tuple8<>(value1,value2,value3,value4,value5,value6,value7,value8);
    }
    public static <T1,T2,T3,T4,T5, T6, T7,T8> Tuple8<T1,T2,T3,T4,T5,T6, T7, T8> lazy(Supplier<? extends T1> supplier1,
                                                                              Supplier<? extends T2> supplier2,
                                                                              Supplier<? extends T3> supplier3,
                                                                              Supplier<? extends T4> supplier4,
                                                                              Supplier<? extends T5> supplier5,
                                                                              Supplier<? extends T6> supplier6,
                                                                              Supplier<? extends T7> supplier7,
                                                                              Supplier<? extends T8> supplier8) {
        return new Tuple8<T1,T2,T3,T4,T5,T6,T7,T8>(null,null,null,null,null, null,null,null) {
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

            @Override
            public T6 _6() {
                return supplier6.get();
            }

            @Override
            public T7 _7() {
                return supplier7.get();
            }
            @Override
            public T8 _8() {
                return supplier8.get();
            }
        };
    }
    private final T1 _1;
    private final T2 _2;
    private final T3 _3;
    private final T4 _4;
    private final T5 _5;
    private final T6 _6;
    private final T7 _7;
    private final T8 _8;

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
    public  T6 _6(){
        return _6;
    }
    public  T7 _7(){
        return _7;
    }
    public  T8 _8(){
        return _8;
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

    public Tuple1<T6> sixth(){
        return Tuple.tuple(_6());
    }

    public Tuple1<T7> seventh(){
        return Tuple.tuple(_7());
    }
    public Tuple1<T8> eighth(){
        return Tuple.tuple(_8());
    }

    public Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> eager(){
        return of(_1(),_2(),_3(),_4(),_5(),_6(),_7(),_8());
    }

    public Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> memo(){
        Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> host = this;
        return new Tuple8<T1,T2,T3,T4,T5,T6,T7,T8>(null,null, null,null, null,null,null,null){
            final Supplier<T1> memo1 = Memoize.memoizeSupplier(host::_1);
            final Supplier<T2> memo2 = Memoize.memoizeSupplier(host::_2);
            final Supplier<T3> memo3 = Memoize.memoizeSupplier(host::_3);
            final Supplier<T4> memo4 = Memoize.memoizeSupplier(host::_4);
            final Supplier<T5> memo5 = Memoize.memoizeSupplier(host::_5);
            final Supplier<T6> memo6 = Memoize.memoizeSupplier(host::_6);
            final Supplier<T7> memo7 = Memoize.memoizeSupplier(host::_7);
            final Supplier<T8> memo8 = Memoize.memoizeSupplier(host::_8);
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
            @Override
            public T6 _6() {
                return memo6.get();
            }
            @Override
            public T7 _7() {
                return memo7.get();
            }
            @Override
            public T8 _8() {
                return memo8.get();
            }
        };
    }
    public <R1,R2,R3,R4,R5,R6,R7,R8> Tuple8<R1,R2,R3,R4,R5,R6,R7,R8> mapAll(Function<? super T1, ? extends R1> fn1, Function<? super T2,? extends R2> fn2,
                                                                      Function<? super T3,? extends R3> fn3,
                                                                      Function<? super T4,? extends R4> fn4,
                                                                      Function<? super T5,? extends R5> fn5,
                                                                      Function<? super T6,? extends R6> fn6,
                                                                      Function<? super T7,? extends R7> fn7,
                                                                            Function<? super T8,? extends R8> fn8){
        return of( fn1.apply(_1()),
                    fn2.apply(_2()),
                    fn3.apply(_3()),
                    fn4.apply(_4()),
                    fn5.apply(_5()),
                    fn6.apply(_6()),
                    fn7.apply(_7()),
                fn8.apply(_8()));
    }

    public <R1,R2,R3,R4,R5,R6,R7,R8> Tuple8<R1,R2,R3,R4,R5,R6,R7,R8> lazyMapAll(Function<? super T1, ? extends R1> fn1, Function<? super T2,? extends R2> fn2,
                                                                          Function<? super T3,? extends R3> fn3,
                                                                          Function<? super T4,? extends R4> fn4,
                                                                          Function<? super T5,? extends R5> fn5,
                                                                          Function<? super T6,? extends R6> fn6,
                                                                          Function<? super T7,? extends R7> fn7,
                                                                                Function<? super T8,? extends R8> fn8){
        return lazy(()->(fn1.apply(_1())),()->fn2.apply(_2()),()->fn3.apply(_3()),()->fn4.apply(_4()),
                ()->fn5.apply(_5()),()->fn6.apply(_6()),()->fn7.apply(_7()),()->fn8.apply(_8()));
    }
    public <R> Tuple8<R, T2,T3,T4,T5,T6,T7,T8> map1(Function<? super T1, ? extends R> fn) {
        return of(fn.apply(_1()), _2(),_3(),_4(),_5(),_6(),_7(),_8());
    }
    public <R> Tuple8<R, T2,T3,T4,T5,T6,T7,T8> lazyMap1(Function<? super T1, ? extends R> fn) {
        return lazy(()->fn.apply(_1()),()-> _2(),()->_3(),()->_4(),()->_5(),()->_6(),()->_7(),()->_8());
    }
    public <R> Tuple8<T1, R, T3, T4,T5,T6,T7,T8> map2(Function<? super T2, ? extends R> fn) {
        return of(_1(), fn.apply(_2()),_3(),_4(),_5(),_6(),_7(),_8());
    }
    public <R> Tuple8<T1, R, T3,T4,T5,T6,T7,T8> lazyMap2(Function<? super T2, ? extends R> fn) {
        return lazy(() -> _1(), () -> fn.apply(_2()),()->_3(),()->_4(),()->_5(),()->_6(),()->_7(),()->_8());
    }
    public <R> Tuple8<T1, T2, R,T4,T5,T6,T7,T8> map3(Function<? super T3, ? extends R> fn) {
        return of(_1(), _2(),fn.apply(_3()),_4(),_5(),_6(),_7(),_8());
    }
    public <R> Tuple8<T1, T2, R,T4,T5,T6,T7,T8> lazyMap3(Function<? super T3, ? extends R> fn) {
        return lazy(() -> _1(), () -> _2(),()->fn.apply(_3()),()->_4(),()->_5(),()->_6(),()->_7(),()->_8());
    }
    public <R> Tuple8<T1, T2, T3, R , T5, T6, T7,T8> map4(Function<? super T4, ? extends R> fn) {
        return of(_1(), _2(),_3(),fn.apply(_4()),_5(),_6(),_7(),_8());
    }
    public <R> Tuple8<T1, T2, T3, R, T5,T6,T7,T8> lazyMap4(Function<? super T4, ? extends R> fn) {
        return lazy(() -> _1(), () -> _2(),()->_3(),()->fn.apply(_4()),()->_5(),()->_6(),()->_7(),()->_8());
    }
    public <R> Tuple8<T1, T2, T3, T4 , R,T6,T7,T8> map5(Function<? super T5, ? extends R> fn) {
        return of(_1(), _2(),_3(),_4(),fn.apply(_5()),_6(),_7(),_8());
    }
    public <R> Tuple8<T1, T2, T3, T4, R,T6,T7,T8> lazyMap5(Function<? super T5, ? extends R> fn) {
        return lazy(() -> _1(), () -> _2(),()->_3(),()->_4(),()->fn.apply(_5()),()->_6(),()->_7(),()->_8());
    }
    public <R> Tuple8<T1, T2, T3, T4 , T5,R, T7,T8> map6(Function<? super T6, ? extends R> fn) {
        return of(_1(), _2(),_3(),_4(),_5(),fn.apply(_6()),_7(),_8());
    }
    public <R> Tuple8<T1, T2, T3, T4, T5,R,T7,T8> lazyMap6(Function<? super T6, ? extends R> fn) {
        return lazy(() -> _1(), () -> _2(),()->_3(),()->_4(),()->_5(),()->fn.apply(_6()),()->_7(),()->_8());
    }

    public <R> R visit(Fn8<? super T1, ? super T2, ? super T3,? super T4, ? super T5,? super T6,? super T7,? super T8,? extends R> fn){
        return fn.apply(_1(),_2(),_3(),_4(),_5(),_6(),_7(),_8());
    }

    @Override
    public String toString() {
        return String.format("[%s,%s,%s,%s,%s,%s,%s,%s]", _1(),_2(),_3(),_4(),_5(),_6(),_7(),_8());
    }




}
