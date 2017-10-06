package cyclops.function;


import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public interface P3<T1,T2,T3> {

    boolean test(T1 t1, T2 t2,T3 t3);


    default P3<T1, T2,T3> and(P3<? super T1, ? super T2,? super T3> other) {
        Objects.requireNonNull(other);
        return (T1 t1, T2 t2,T3 t3) -> test(t1, t2, t3) && other.test(t1, t2,t3);
    }


    default P3<T1, T2,T3> negate() {
        return (T1 t1, T2 t2, T3 t3) -> !test(t1, t2, t3);
    }


    default P3<T1, T2, T3> or(P3<? super T1, ? super T2, ? super T3> other) {
        Objects.requireNonNull(other);
        return (T1 t1, T2 t2, T3 t3) -> test(t1, t2, t3) || other.test(t1, t2, t3);
    }

    public static <T1,T2,T3> P3<T1,T2,T3> self(P3<T1,T2,T3> pred){
        return pred;
    }
    public static <T1,T2,T3> P3<T1,T2,T3> always(){
        return (a,b,c)->true;
    }
    public static <T1,T2,T3> P3<T1,T2,T3> never(){
        return (a,b,c)->false;
    }
    public static <T1,T2,T3> P3<T1,T2,T3> and(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3){
        return (a,b,c)->p1.test(a) && p2.test(b) && p3.test(c);
    }
    public static <T1,T2,T3> P3<T1,T2,T3> _1(Predicate<? super T1> p1){
        return (a,b,c)->p1.test(a);
    }

    public static <T1,T2,T3> P3<T1,T2,T3> first(Predicate<? super T1> p1, Predicate<? super T2> p2,Predicate<? super T3> p3){
        return (a,b,c)->p1.test(a) && !p2.test(b) && !p3.test(c);
    }
    public static <T1,T2,T3> P3<T1,T2,T3> second(Predicate<? super T1> p1, Predicate<? super T2> p2,Predicate<? super T3> p3){
        return (a,b,c)->!p1.test(a) && p2.test(b)  && !p3.test(c);
    }
    public static <T1,T2,T3> P3<T1,T2,T3> third(Predicate<? super T1> p1, Predicate<? super T2> p2,Predicate<? super T3> p3){
        return (a,b,c)->!p1.test(a) && !p2.test(b)  && p3.test(c);
    }
    public static <T1,T2,T3> P3<T1,T2,T3> _2(Predicate<? super T2> p2){
        return (a,b,c)->p2.test(b);
    }
    public static <T1,T2,T3,T4> P3<T1,T2,T3> _3(Predicate<? super T3> p3){
        return (a,b,c)->p3.test(c);
    }
    public static <T1,T2,T3> P3<T1,T2,T3> or(Predicate<? super T1> p1, Predicate<? super T2> p2,Predicate<? super T3> p3){
        return (a,b,c)->p1.test(a) || p2.test(b) || p3.test(c);
    }
    public static <T1,T2,T3> P3<T1,T2,T3> xor(Predicate<? super T1> p1, Predicate<? super T2> p2,Predicate<? super T3> p3){
        return (a,b,c)->p1.test(a) ^ p2.test(b) ^ p3.test(c);
    }
}
