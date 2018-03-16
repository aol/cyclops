package cyclops.function;


import cyclops.data.Ord;

import java.util.Comparator;

public interface Ordering<T> extends Comparator<T> {

    default Ord.Ordering compareOrder(T left, T right){
        return Ord.Ordering.values()[Math.max(-1,Math.min(1,compare(left,right)))+1];
    }
    default boolean isLessThan(T left, T right){
        return compare(left,right)<0;
    }
    default boolean isLessThanOrEqual(T left, T right){
        return compare(left,right)<=0;
    }
    default boolean isGreaterThan(T left, T right){
        return compare(left,right)>0;
    }
    default boolean isGreaterThanOrEqual(T left, T right){
        return compare(left,right)>=0;
    }

    default T max(T left, T right){
        if(isGreaterThanOrEqual(left,right))
            return left;
        return right;
    }

    default T min(T left, T right){
        if(isLessThanOrEqual(left,right))
            return left;
        return right;
    }
    static boolean isEqual(int compareResult){
        return compareResult == 0;
    }
    static boolean isFirstParameterLessThan(int compareResult){
        return compareResult < 0;
    }
    static boolean isFirstParameterGreaterThan(int compareResult){
        return compareResult > 0;
    }
    static boolean isFirstParameterLessThanOrEqual(int compareResult){
        return compareResult <= 0;
    }
    static boolean isFirstParameterGreaterThanOrEqual(int compareResult){
        return compareResult >= 0;
    }

    public static <T> Ordering<T> of(Comparator<? super T> comp){
        if(comp instanceof  Ordering)
            return (Ordering)comp;
        return (left,right)->comp.compare(left,right);
    }
}
