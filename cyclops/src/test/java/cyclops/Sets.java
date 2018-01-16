package cyclops;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface Sets {

    public static <T> Set<T> empty(){
        return new HashSet<>(Arrays.asList());
    }
    public static <T> Set<T> set(T... values){
        return new HashSet<>(Arrays.asList(values));
    }
}
