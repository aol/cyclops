package cyclops.collections.tuple;


import com.aol.cyclops2.types.foldable.To;
import cyclops.function.Fn6;
import cyclops.function.Fn7;
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
public class Tuple7<T1,T2,T3,T4,T5,T6,T7> implements To<Tuple7<T1,T2,T3,T4,T5,T6,T7>>,
                                                Serializable{

    private static final long serialVersionUID = 1L;

    public static <T1,T2,T3,T4,T5,T6,T7> Tuple7<T1,T2,T3,T4,T5,T6,T7> of(T1 value1, T2 value2, T3 value3, T4 value4, T5 value5, T6 value6,T7 value7) {
        return new Tuple7<>(value1,value2,value3,value4,value5,value6,value7);
    }
    public static <T1,T2,T3,T4,T5, T6, T7> Tuple7<T1,T2,T3,T4,T5,T6, T7> lazy(Supplier<? extends T1> supplier1,
                                                                      Supplier<? extends T2> supplier2,
                                                                      Supplier<? extends T3> supplier3,
                                                                      Supplier<? extends T4> supplier4,
                                                                      Supplier<? extends T5> supplier5,
                                                                      Supplier<? extends T6> supplier6,
                                                                      Supplier<? extends T7> supplier7) {
        return new Tuple7<T1,T2,T3,T4,T5,T6,T7>(null,null,null,null,null, null,null) {
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
        };
    }
    private final T1 _1;
    private final T2 _2;
    private final T3 _3;
    private final T4 _4;
    private final T5 _5;
    private final T6 _6;
    private final T7 _7;

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

    public Tuple7<T1,T2,T3,T4,T5,T6,T7> eager(){
        return of(_1(),_2(),_3(),_4(),_5(),_6(),_7());
    }


    public <R1,R2,R3,R4,R5,R6,R7> Tuple7<R1,R2,R3,R4,R5,R6,R7> mapAll(Function<? super T1, ? extends R1> fn1, Function<? super T2,? extends R2> fn2,
                                                                Function<? super T3,? extends R3> fn3,
                                                                Function<? super T4,? extends R4> fn4,
                                                                Function<? super T5,? extends R5> fn5,
                                                                Function<? super T6,? extends R6> fn6,
                                                                      Function<? super T7,? extends R7> fn7){
        return of( fn1.apply(_1()),
                    fn2.apply(_2()),
                    fn3.apply(_3()),
                    fn4.apply(_4()),
                    fn5.apply(_5()),
                    fn6.apply(_6()),
                    fn7.apply(_7()));
    }

    public <R1,R2,R3,R4,R5,R6,R7> Tuple7<R1,R2,R3,R4,R5,R6,R7> lazyMapAll(Function<? super T1, ? extends R1> fn1, Function<? super T2,? extends R2> fn2,
                                                                    Function<? super T3,? extends R3> fn3,
                                                                    Function<? super T4,? extends R4> fn4,
                                                                    Function<? super T5,? extends R5> fn5,
                                                                    Function<? super T6,? extends R6> fn6,
                                                                          Function<? super T7,? extends R7> fn7){
        return lazy(()->(fn1.apply(_1())),()->fn2.apply(_2()),()->fn3.apply(_3()),()->fn4.apply(_4()),
                ()->fn5.apply(_5()),()->fn6.apply(_6()),()->fn7.apply(_7()));
    }
    public <R> Tuple7<R, T2,T3,T4,T5,T6,T7> map1(Function<? super T1, ? extends R> fn) {
        return of(fn.apply(_1()), _2(),_3(),_4(),_5(),_6(),_7());
    }
    public <R> Tuple7<R, T2,T3,T4,T5,T6,T7> lazyMap1(Function<? super T1, ? extends R> fn) {
        return lazy(()->fn.apply(_1()),()-> _2(),()->_3(),()->_4(),()->_5(),()->_6(),()->_7());
    }
    public <R> Tuple7<T1, R, T3, T4,T5,T6,T7> map2(Function<? super T2, ? extends R> fn) {
        return of(_1(), fn.apply(_2()),_3(),_4(),_5(),_6(),_7());
    }
    public <R> Tuple7<T1, R, T3,T4,T5,T6,T7> lazyMap2(Function<? super T2, ? extends R> fn) {
        return lazy(() -> _1(), () -> fn.apply(_2()),()->_3(),()->_4(),()->_5(),()->_6(),()->_7());
    }
    public <R> Tuple7<T1, T2, R,T4,T5,T6,T7> map3(Function<? super T3, ? extends R> fn) {
        return of(_1(), _2(),fn.apply(_3()),_4(),_5(),_6(),_7());
    }
    public <R> Tuple7<T1, T2, R,T4,T5,T6,T7> lazyMap3(Function<? super T3, ? extends R> fn) {
        return lazy(() -> _1(), () -> _2(),()->fn.apply(_3()),()->_4(),()->_5(),()->_6(),()->_7());
    }
    public <R> Tuple7<T1, T2, T3, R , T5, T6, T7> map4(Function<? super T4, ? extends R> fn) {
        return of(_1(), _2(),_3(),fn.apply(_4()),_5(),_6(),_7());
    }
    public <R> Tuple7<T1, T2, T3, R, T5,T6,T7> lazyMap4(Function<? super T4, ? extends R> fn) {
        return lazy(() -> _1(), () -> _2(),()->_3(),()->fn.apply(_4()),()->_5(),()->_6(),()->_7());
    }
    public <R> Tuple7<T1, T2, T3, T4 , R,T6,T7> map5(Function<? super T5, ? extends R> fn) {
        return of(_1(), _2(),_3(),_4(),fn.apply(_5()),_6(),_7());
    }
    public <R> Tuple7<T1, T2, T3, T4, R,T6,T7> lazyMap5(Function<? super T5, ? extends R> fn) {
        return lazy(() -> _1(), () -> _2(),()->_3(),()->_4(),()->fn.apply(_5()),()->_6(),()->_7());
    }
    public <R> Tuple7<T1, T2, T3, T4 , T5,R, T7> map6(Function<? super T6, ? extends R> fn) {
        return of(_1(), _2(),_3(),_4(),_5(),fn.apply(_6()),_7());
    }
    public <R> Tuple7<T1, T2, T3, T4, T5,R,T7> lazyMap6(Function<? super T6, ? extends R> fn) {
        return lazy(() -> _1(), () -> _2(),()->_3(),()->_4(),()->_5(),()->fn.apply(_6()),()->_7());
    }

    public <R> R visit(Fn7<? super T1, ? super T2, ? super T3,? super T4, ? super T5,? super T6,? super T7,? extends R> fn){
        return fn.apply(_1(),_2(),_3(),_4(),_5(),_6(),_7());
    }

    @Override
    public String toString() {
        return String.format("[%s,%s,%s,%s,%s,%s,%s]", _1(),_2(),_3(),_4(),_5(),_6(),_7());
    }




}
