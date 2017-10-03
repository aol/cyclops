package cyclops.collections.tuple;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Tuple {

    public static <T> Tuple1<T> tuple(T value){
        return new Tuple1<>(value);
    }
}
