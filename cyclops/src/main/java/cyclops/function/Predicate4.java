package cyclops.function;


import java.util.Objects;
import java.util.function.Predicate;

public interface Predicate4<T1,T2,T3,T4> {

    boolean test(T1 t1, T2 t2, T3 t3, T4 t4);


    default Predicate4<T1, T2,T3,T4> and(Predicate4<? super T1, ? super T2, ? super T3, ? super T4> other) {
        Objects.requireNonNull(other);
        return (T1 t1, T2 t2,T3 t3, T4 t4) -> test(t1, t2, t3,t4) && other.test(t1, t2,t3,t4);
    }


    default Predicate4<T1, T2,T3,T4> negate() {
        return (T1 t1, T2 t2, T3 t3, T4 t4) -> !test(t1, t2, t3,t4);
    }


    default Predicate4<T1, T2, T3, T4> or(Predicate4<? super T1, ? super T2, ? super T3, ? super T4> other) {
        Objects.requireNonNull(other);
        return (T1 t1, T2 t2, T3 t3, T4 t4) -> test(t1, t2, t3, t4) || other.test(t1, t2, t3,t4);
    }
    public static <T1,T2,T3,T4> Predicate4<T1,T2,T3,T4> self(Predicate4<T1, T2, T3, T4> pred){
        return pred;
    }
    public static <T1,T2,T3,T4> Predicate4<T1,T2,T3,T4> always(){
        return (a,b,c,d)->true;
    }
    public static <T1,T2,T3,T4> Predicate4<T1,T2,T3,T4> never(){
        return (a,b,c,d)->false;
    }
    public static <T1,T2,T3,T4> Predicate4<T1,T2,T3,T4> and(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                            Predicate<? super T4> p4){
        return (a,b,c,d)->p1.test(a) && p2.test(b) && p3.test(c) && p4.test(d);
    }
    public static <T1,T2,T3,T4> Predicate4<T1,T2,T3,T4> _1(Predicate<? super T1> p1){
        return (a,b,c,d)->p1.test(a);
    }

    public static <T1,T2,T3,T4> Predicate4<T1,T2,T3,T4> first(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                              Predicate<? super T4> p4){
        return (a,b,c,d)->p1.test(a) && !p2.test(b) && !p3.test(c) && !p4.test(d);
    }
    public static <T1,T2,T3,T4> Predicate4<T1,T2,T3,T4> second(Predicate<? super T1> p1, Predicate<? super T2> p2,
                                                               Predicate<? super T3> p3, Predicate<? super T4> p4){
        return (a,b,c,d)->!p1.test(a) && p2.test(b)  && !p3.test(c) && p4.test(d);
    }
    public static <T1,T2,T3,T4> Predicate4<T1,T2,T3,T4> third(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                              Predicate<? super T4> p4){
        return (a,b,c,d)->!p1.test(a) && !p2.test(b)  && p3.test(c) && !p4.test(d);
    }
    public static <T1,T2,T3,T4> Predicate4<T1,T2,T3,T4> fourth(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                               Predicate<? super T4> p4){
        return (a,b,c,d)->!p1.test(a) && !p2.test(b)  && !p3.test(c) && p4.test(d);
    }

    public static <T1,T2,T3,T4> Predicate4<T1,T2,T3,T4> _2(Predicate<? super T2> p2){
        return (a,b,c,d)->p2.test(b);
    }

    public static <T1,T2,T3,T4> Predicate4<T1,T2,T3,T4> _3(Predicate<? super T3> p3){
        return (a,b,c,d)->p3.test(c);
    }

    public static <T1,T2,T3,T4> Predicate4<T1,T2,T3,T4> _4(Predicate<? super T4> p3){
        return (a,b,c,d)->p3.test(d);
    }

    public static <T1,T2,T3,T4> Predicate4<T1,T2,T3,T4> or(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                           Predicate<? super T4> p4){
        return (a,b,c,d)->p1.test(a) || p2.test(b) || p3.test(c) || p4.test(d);
    }
    public static <T1,T2,T3,T4> Predicate4<T1,T2,T3,T4> xor(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                            Predicate<? super T4> p4){
        return (a,b,c,d)->p1.test(a) ^ p2.test(b) ^ p3.test(c) ^ p4.test(d);
    }
}
