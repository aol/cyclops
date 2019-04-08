package cyclops.function;

import java.util.Objects;
import java.util.function.Predicate;

public interface Predicate7<T1,T2,T3,T4,T5,T6,T7> {
    boolean test(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7);

    default Predicate7<T1,T2,T3,T4,T5,T6,T7> and(Predicate7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6,T7> other) {
        Objects.requireNonNull(other);
        return (T1 t1, T2 t2,T3 t3, T4 t4, T5 t5, T6 t6, T7 t7) -> test(t1,t2,t3,t4,t5, t6, t7) && other.test(t1,t2,t3,t4,t5,t6, t7);
    }

    default Predicate7<T1,T2,T3,T4,T5,T6,T7> negate() {
        return (T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7) -> !test(t1, t2, t3,t4,t5, t6, t7);
    }

    default Predicate7<T1,T2,T3,T4,T5,T6,T7> or(Predicate7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6,T7> other) {
        Objects.requireNonNull(other);
        return (T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7) -> test(t1, t2, t3, t4,t5, t6, t7) || other.test(t1,t2,t3,t4,t5,t6, t7);
    }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> self(Predicate7<T1, T2, T3, T4, T5, T6,T7> pred){ return pred; }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> always(){
        return (a,b,c,d,e,f, g)->true;
    }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> never(){
        return (a,b,c,d,e,f, g)->false;
    }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> and(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                        Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7){
        return (a,b,c,d,e,f,g)->p1.test(a) && p2.test(b) && p3.test(c) && p4.test(d) && p5.test(e) && p6.test(f) && p7.test(g);
    }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> first(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                          Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7){
        return (a,b,c,d,e,f, g)->p1.test(a) && !p2.test(b) && !p3.test(c) && !p4.test(d) && !p5.test(e) && !p6.test(f) && !p7.test(g);
    }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> second(Predicate<? super T1> p1, Predicate<? super T2> p2,
                                                                           Predicate<? super T3> p3, Predicate<? super T4> p4,
                                                                           Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7){
        return (a,b,c,d,e,f,g)->!p1.test(a) && p2.test(b)  && !p3.test(c) && p4.test(d) && !p5.test(e) && !p6.test(f) && !p7.test(g);
    }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> third(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                          Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7){
        return (a,b,c,d,e,f, g)->!p1.test(a) && !p2.test(b)  && p3.test(c) && !p4.test(d)  && !p5.test(e) && !p6.test(f) && !p7.test(g);
    }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> fourth(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                           Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7){
        return (a,b,c,d,e,f, g)->!p1.test(a) && !p2.test(b)  && !p3.test(c) && p4.test(d) && !p5.test(e) && !p6.test(f) && !p7.test(g);
    }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> fifth(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                          Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7){
        return (a,b,c,d,e,f, g)->!p1.test(a) && !p2.test(b)  && !p3.test(c) && !p4.test(d) && p5.test(e) && !p6.test(f) && !p7.test(g);
    }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> sixt(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                         Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7){
        return (a,b,c,d,e,f, g)->!p1.test(a) && !p2.test(b)  && !p3.test(c) && !p4.test(d) && !p5.test(e) && p6.test(f) && !p7.test(g);
    }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> seventh(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                         Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7){
        return (a,b,c,d,e,f, g)->!p1.test(a) && !p2.test(b)  && !p3.test(c) && !p4.test(d) && !p5.test(e) && !p6.test(f) && p7.test(g);
    }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> _1(Predicate<? super T1> p1){
        return (a,b,c,d,e,f, g)->p1.test(a);
    }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> _2(Predicate<? super T2> p2){
        return (a,b,c,d,e,f, g)->p2.test(b);
    }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> _3(Predicate<? super T3> p3){
        return (a,b,c,d,e,f, g)->p3.test(c);
    }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> _4(Predicate<? super T4> p4){
        return (a,b,c,d,e,f, g)->p4.test(d);
    }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> _5(Predicate<? super T5> p5){
        return (a,b,c,d,e,f, g)->p5.test(e);
    }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> _6(Predicate<? super T6> p6){
        return (a,b,c,d,e,f, g)->p6.test(f);
    }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> _7(Predicate<? super T7> p7){
        return (a,b,c,d,e,f, g)->p7.test(g);
    }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> or(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                       Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7){
        return (a,b,c,d,e,f, g)->p1.test(a) || p2.test(b) || p3.test(c) || p4.test(d) || p5.test(e) || p6.test(f) || p7.test(g);
    }

    public static <T1,T2,T3,T4,T5,T6,T7> Predicate7<T1,T2,T3,T4,T5,T6,T7> xor(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                        Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7){
        return (a,b,c,d,e,f, g)->p1.test(a) ^ p2.test(b) ^ p3.test(c) ^ p4.test(d) ^ p5.test(e) ^ p6.test(f) ^ p7.test(g);
    }
}
