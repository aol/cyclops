package cyclops.function;

import java.util.Objects;
import java.util.function.Predicate;

public interface Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> {
    boolean test(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8);

    default Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> and(Predicate8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8> other) {
        Objects.requireNonNull(other);
        return (T1 t1, T2 t2,T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8) -> test(t1,t2,t3,t4,t5, t6, t7, t8) && other.test(t1,t2,t3,t4,t5,t6, t7, t8);
    }

    default Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> negate() {
        return (T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8) -> !test(t1, t2, t3,t4,t5, t6, t7, t8);
    }

    default Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> or(Predicate8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6,? super T7, ? super T8> other) {
        Objects.requireNonNull(other);
        return (T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8) -> test(t1, t2, t3, t4,t5, t6, t7, t8) || other.test(t1,t2,t3,t4,t5,t6, t7, t8);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> self(Predicate8<T1, T2, T3, T4, T5, T6, T7, T8> pred){ return pred; }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> always(){
        return (a,b,c,d,e,f,g,h)->true;
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> never(){
        return (a,b,c,d,e,f,g,h)->false;
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> and(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                              Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7, Predicate<? super T8> p8){
        return (a,b,c,d,e,f,g,h)->p1.test(a) && p2.test(b) && p3.test(c) && p4.test(d) && p5.test(e) && p6.test(f) && p7.test(g) && p8.test(h);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> first(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                                Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7, Predicate<? super T8> p8){
        return (a,b,c,d,e,f,g,h)->p1.test(a) && !p2.test(b) && !p3.test(c) && !p4.test(d) && !p5.test(e) && !p6.test(f) && !p7.test(g) && !p8.test(h);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> second(Predicate<? super T1> p1, Predicate<? super T2> p2,
                                                                                 Predicate<? super T3> p3, Predicate<? super T4> p4,
                                                                                 Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7, Predicate<? super T8> p8){
        return (a,b,c,d,e,f,g,h)->!p1.test(a) && p2.test(b)  && !p3.test(c) && p4.test(d) && !p5.test(e) && !p6.test(f) && !p7.test(g) && !p8.test(h);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> third(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                                Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7, Predicate<? super T8> p8){
        return (a,b,c,d,e,f,g,h)->!p1.test(a) && !p2.test(b)  && p3.test(c) && !p4.test(d)  && !p5.test(e) && !p6.test(f) && !p7.test(g) && !p8.test(h);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> fourth(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                                 Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7, Predicate<? super T8> p8){
        return (a,b,c,d,e,f,g,h)->!p1.test(a) && !p2.test(b)  && !p3.test(c) && p4.test(d) && !p5.test(e) && !p6.test(f) && !p7.test(g) && !p8.test(h);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> fifth(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                                Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7, Predicate<? super T8> p8){
        return (a,b,c,d,e,f,g,h)->!p1.test(a) && !p2.test(b)  && !p3.test(c) && !p4.test(d) && p5.test(e) && !p6.test(f) && !p7.test(g) && !p8.test(h);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> sixt(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                               Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7, Predicate<? super T8> p8){
        return (a,b,c,d,e,f,g,h)->!p1.test(a) && !p2.test(b)  && !p3.test(c) && !p4.test(d) && !p5.test(e) && p6.test(f) && !p7.test(g) && !p8.test(h);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> seventh(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                                  Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7, Predicate<? super T8> p8){
        return (a,b,c,d,e,f,g,h)->!p1.test(a) && !p2.test(b)  && !p3.test(c) && !p4.test(d) && !p5.test(e) && !p6.test(f) && p7.test(g) && !p8.test(h);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> eight(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                                  Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7, Predicate<? super T8> p8){
        return (a,b,c,d,e,f,g,h)->!p1.test(a) && !p2.test(b)  && !p3.test(c) && !p4.test(d) && !p5.test(e) && !p6.test(f) && !p7.test(g) && p8.test(h);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> _1(Predicate<? super T1> p1){
        return (a,b,c,d,e,f,g,h)->p1.test(a);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> _2(Predicate<? super T2> p2){
        return (a,b,c,d,e,f,g,h)->p2.test(b);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> _3(Predicate<? super T3> p3){
        return (a,b,c,d,e,f,g,h)->p3.test(c);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> _4(Predicate<? super T4> p4){
        return (a,b,c,d,e,f,g,h)->p4.test(d);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> _5(Predicate<? super T5> p5){
        return (a,b,c,d,e,f,g,h)->p5.test(e);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> _6(Predicate<? super T6> p6){
        return (a,b,c,d,e,f,g,h)->p6.test(f);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> _7(Predicate<? super T7> p7){
        return (a,b,c,d,e,f,g,h)->p7.test(g);
    }
    
    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> _8(Predicate<? super T8> p8){
        return (a,b,c,d,e,f,g,h)->p8.test(h);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> or(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                             Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7, Predicate<? super T8> p8){
        return (a,b,c,d,e,f,g,h)->p1.test(a) || p2.test(b) || p3.test(c) || p4.test(d) || p5.test(e) || p6.test(f) || p7.test(g) && p8.test(h) || p8.test(h);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8> Predicate8<T1,T2,T3,T4,T5,T6,T7,T8> xor(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                              Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6, Predicate<? super T7> p7, Predicate<? super T8> p8){
        return (a,b,c,d,e,f,g,h)->p1.test(a) ^ p2.test(b) ^ p3.test(c) ^ p4.test(d) ^ p5.test(e) ^ p6.test(f) ^ p7.test(g) && p8.test(h) ^ p8.test(h);
    }
}
