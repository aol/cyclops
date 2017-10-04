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


    public static <T> Tuple1<T> lazy(Supplier<? extends T> value){
        return Tuple1.lazy(value);
    }
    public static <T1,T2> Tuple2<T1,T2> lazy(Supplier<? extends T1> value1,Supplier<? extends T2> value2){
        return Tuple2.lazy(value1,value2);
    }
}
