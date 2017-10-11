package cyclops.collections.tuple;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Empty tuple type
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Tuple0 {
    static Tuple0 INSTANCE = new Tuple0();

    public static Tuple0 empty(){
        return INSTANCE;
    }

    public <T> Tuple1<T> add(T t){
        return Tuple.tuple(t);
    }
}
