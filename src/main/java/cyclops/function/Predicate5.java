package cyclops.function;


import java.util.Objects;
import java.util.function.Predicate;

public interface Predicate5<T1,T2,T3,T4,T5> {

    boolean test(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5);


    default Predicate5<T1, T2,T3,T4,T5> and(Predicate5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5> other) {
        Objects.requireNonNull(other);
        return (T1 t1, T2 t2,T3 t3, T4 t4, T5 t5) -> test(t1, t2, t3,t4,t5) && other.test(t1, t2,t3,t4,t5);
    }


    default Predicate5<T1, T2,T3,T4,T5> negate() {
        return (T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) -> !test(t1, t2, t3,t4,t5);
    }


    default Predicate5<T1, T2, T3, T4,T5> or(Predicate5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5> other) {
        Objects.requireNonNull(other);
        return (T1 t1, T2 t2, T3 t3, T4 t4,T5 t5) -> test(t1, t2, t3, t4,t5) || other.test(t1, t2, t3,t4,t5);
    }
    public static <T1,T2,T3,T4,T5> Predicate5<T1,T2,T3,T4,T5> self(Predicate5<T1, T2, T3, T4, T5> pred){
        return pred;
    }
    public static <T1,T2,T3,T4,T5> Predicate5<T1,T2,T3,T4,T5> always(){
        return (a,b,c,d,e)->true;
    }
    public static <T1,T2,T3,T4,T5> Predicate5<T1,T2,T3,T4,T5> never(){
        return (a,b,c,d,e)->false;
    }
    public static <T1,T2,T3,T4,T5> Predicate5<T1,T2,T3,T4,T5> and(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                  Predicate<? super T4> p4, Predicate<? super T5> p5){
        return (a,b,c,d,e)->p1.test(a) && p2.test(b) && p3.test(c) && p4.test(d) && p5.test(e);
    }
    public static <T1,T2,T3,T4,T5> Predicate5<T1,T2,T3,T4,T5> _1(Predicate<? super T1> p1){
        return (a,b,c,d,e)->p1.test(a);
    }

    public static <T1,T2,T3,T4,T5> Predicate5<T1,T2,T3,T4,T5> first(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                    Predicate<? super T4> p4, Predicate<? super T5> p5){
        return (a,b,c,d,e)->p1.test(a) && !p2.test(b) && !p3.test(c) && !p4.test(d) && !p5.test(e);
    }
    public static <T1,T2,T3,T4,T5> Predicate5<T1,T2,T3,T4,T5> second(Predicate<? super T1> p1, Predicate<? super T2> p2,
                                                                     Predicate<? super T3> p3, Predicate<? super T4> p4,
                                                                     Predicate<? super T5> p5){
        return (a,b,c,d,e)->!p1.test(a) && p2.test(b)  && !p3.test(c) && p4.test(d) && p5.test(e);
    }
    public static <T1,T2,T3,T4,T5> Predicate5<T1,T2,T3,T4,T5> third(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                    Predicate<? super T4> p4, Predicate<? super T5> p5){
        return (a,b,c,d,e)->!p1.test(a) && !p2.test(b)  && p3.test(c) && !p4.test(d)  && !p5.test(e);
    }
    public static <T1,T2,T3,T4,T5> Predicate5<T1,T2,T3,T4,T5> fourth(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                     Predicate<? super T4> p4, Predicate<? super T5> p5){
        return (a,b,c,d,e)->!p1.test(a) && !p2.test(b)  && !p3.test(c) && p4.test(d) && !p5.test(e);
    }
    public static <T1,T2,T3,T4,T5> Predicate5<T1,T2,T3,T4,T5> fifth(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                    Predicate<? super T4> p4, Predicate<? super T5> p5){
        return (a,b,c,d,e)->!p1.test(a) && !p2.test(b)  && !p3.test(c) && !p4.test(d) && p5.test(e);
    }

    public static <T1,T2,T3,T4,T5> Predicate5<T1,T2,T3,T4,T5> _2(Predicate<? super T2> p2){
        return (a,b,c,d,e)->p2.test(b);
    }

    public static <T1,T2,T3,T4,T5> Predicate5<T1,T2,T3,T4,T5> _3(Predicate<? super T3> p3){
        return (a,b,c,d,e)->p3.test(c);
    }

    public static <T1,T2,T3,T4,T5> Predicate5<T1,T2,T3,T4,T5> _4(Predicate<? super T4> p3){
        return (a,b,c,d,e)->p3.test(d);
    }

    public static <T1,T2,T3,T4,T5> Predicate5<T1,T2,T3,T4,T5> _5(Predicate<? super T5> p3){
        return (a,b,c,d,e)->p3.test(e);
    }

    public static <T1,T2,T3,T4,T5> Predicate5<T1,T2,T3,T4,T5> or(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                 Predicate<? super T4> p4, Predicate<? super T5> p5){
        return (a,b,c,d,e)->p1.test(a) || p2.test(b) || p3.test(c) || p4.test(d) || p5.test(e);
    }
    public static <T1,T2,T3,T4,T5> Predicate5<T1,T2,T3,T4,T5> xor(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                  Predicate<? super T4> p4, Predicate<? super T5> p5){
        return (a,b,c,d,e)->p1.test(a) ^ p2.test(b) ^ p3.test(c) ^ p4.test(d) ^ p5.test(e);
    }
}
