package cyclops.collections.tuple;

import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class Tuple {

    public static <T> Tuple1<T> tuple(T value){
        return Tuple1.of(value);
    }
    public static <T1,T2> Tuple2<T1,T2> tuple(T1 value1, T2 value2){
        return Tuple2.of(value1,value2);
    }
    public static <T1,T2,T3> Tuple3<T1,T2,T3> tuple(T1 value1, T2 value2, T3 value3){
        return Tuple3.of(value1,value2,value3);
    }
    public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> tuple(T1 value1, T2 value2, T3 value3,T4 value4){
        return Tuple4.of(value1,value2,value3,value4);
    }
    public static <T1,T2,T3,T4,T5> Tuple5<T1,T2,T3,T4,T5> tuple(T1 value1, T2 value2, T3 value3,T4 value4,T5 value5){
        return Tuple5.of(value1,value2,value3,value4,value5);
    }
    public static <T1,T2,T3,T4,T5,T6> Tuple6<T1,T2,T3,T4,T5,T6> tuple(T1 value1, T2 value2, T3 value3,T4 value4,T5 value5,T6 value6){
        return Tuple6.of(value1,value2,value3,value4,value5,value6);
    }
    public static <T1,T2,T3,T4,T5,T6,T7> Tuple7<T1,T2,T3,T4,T5,T6,T7> tuple(T1 value1, T2 value2, T3 value3,T4 value4,T5 value5,T6 value6,T7 value7){
        return Tuple7.of(value1,value2,value3,value4,value5,value6,value7);
    }
    public static <T1,T2,T3,T4,T5,T6,T7,T8> Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> tuple(T1 value1, T2 value2, T3 value3,T4 value4,T5 value5,T6 value6,T7 value7,T8 value8){
        return Tuple8.of(value1,value2,value3,value4,value5,value6,value7,value8);
    }


    public static <T> Tuple1<T> lazy(Supplier<? extends T> value){
        return Tuple1.lazy(value);
    }
    public static <T1,T2> Tuple2<T1,T2> lazy(Supplier<? extends T1> value1,Supplier<? extends T2> value2){
        return Tuple2.lazy(value1,value2);
    }
    public static <T1,T2,T3> Tuple3<T1,T2,T3> lazy(Supplier<? extends T1> value1,Supplier<? extends T2> value2,Supplier<? extends T3> value3){
        return Tuple3.lazy(value1,value2,value3);
    }
    public static <T1,T2,T3,T4> Tuple4<T1,T2,T3,T4> lazy(Supplier<? extends T1> value1,
                                                            Supplier<? extends T2> value2,
                                                            Supplier<? extends T3> value3,
                                                            Supplier<? extends T4> value4){
        return Tuple4.lazy(value1,value2,value3,value4);
    }
    public static <T1,T2,T3,T4,T5> Tuple5<T1,T2,T3,T4,T5> lazy(Supplier<? extends T1> value1,
                                                          Supplier<? extends T2> value2,
                                                          Supplier<? extends T3> value3,
                                                          Supplier<? extends T4> value4,
                                                               Supplier<? extends T5> value5){
        return Tuple5.lazy(value1,value2,value3,value4,value5);
    }
    public static <T1,T2,T3,T4,T5,T6> Tuple6<T1,T2,T3,T4,T5,T6> lazy(Supplier<? extends T1> value1,
                                                               Supplier<? extends T2> value2,
                                                               Supplier<? extends T3> value3,
                                                               Supplier<? extends T4> value4,
                                                               Supplier<? extends T5> value5,
                                                                     Supplier<? extends T6> value6){
        return Tuple6.lazy(value1,value2,value3,value4,value5,value6);
    }
    public static <T1,T2,T3,T4,T5,T6,T7> Tuple7<T1,T2,T3,T4,T5,T6,T7> lazy(Supplier<? extends T1> value1,
                                                                     Supplier<? extends T2> value2,
                                                                     Supplier<? extends T3> value3,
                                                                     Supplier<? extends T4> value4,
                                                                     Supplier<? extends T5> value5,
                                                                     Supplier<? extends T6> value6,
                                                                           Supplier<? extends T7> value7){
        return Tuple7.lazy(value1,value2,value3,value4,value5,value6,value7);
    }
    public static <T1,T2,T3,T4,T5,T6,T7,T8> Tuple8<T1,T2,T3,T4,T5,T6,T7,T8> lazy(Supplier<? extends T1> value1,
                                                                           Supplier<? extends T2> value2,
                                                                           Supplier<? extends T3> value3,
                                                                           Supplier<? extends T4> value4,
                                                                           Supplier<? extends T5> value5,
                                                                           Supplier<? extends T6> value6,
                                                                           Supplier<? extends T7> value7,
                                                                                 Supplier<? extends T8> value8){
        return Tuple8.lazy(value1,value2,value3,value4,value5,value6,value7,value8);
    }
}
