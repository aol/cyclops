package cyclops.data;


import java.util.Comparator;

public class Comparators {

    private final static Comparator IDENTITY_COMPARATOR_INSTANCE = (a, b)-> Comparator.<Integer>naturalOrder().compare(System.identityHashCode(a),System.identityHashCode(b));

    public static <T> Comparator<T> identityComparator(){
        return IDENTITY_COMPARATOR_INSTANCE;
    }
    public static <T> Comparator<T> naturalOrderIdentityComparator(){
        return (a,b)-> {
            if (a instanceof Comparable) {
                return Comparator.<Comparable>naturalOrder().compare((Comparable)a,(Comparable)b);
            }
            return identityComparator().compare(a,b);
        };
    }
}
