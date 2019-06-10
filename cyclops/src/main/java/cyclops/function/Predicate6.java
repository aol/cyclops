package cyclops.function;

import java.util.Objects;
import java.util.function.Predicate;

public interface Predicate6<T1,T2,T3,T4,T5,T6> {
    boolean test(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6);

    default Predicate6<T1,T2,T3,T4,T5,T6> and(Predicate6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6> other) {
        Objects.requireNonNull(other);
        return (T1 t1, T2 t2,T3 t3, T4 t4, T5 t5, T6 t6) -> test(t1,t2,t3,t4,t5, t6) && other.test(t1,t2,t3,t4,t5,t6);
    }

    default Predicate6<T1,T2,T3,T4,T5,T6> negate() {
        return (T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) -> !test(t1, t2, t3,t4,t5, t6);
    }

    default Predicate6<T1,T2,T3,T4,T5,T6> or(Predicate6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6> other) {
        Objects.requireNonNull(other);
        return (T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) -> test(t1, t2, t3, t4,t5, t6) || other.test(t1,t2,t3,t4,t5,t6);
    }

    public static <T1,T2,T3,T4,T5,T6> Predicate6<T1,T2,T3,T4,T5,T6> self(Predicate6<T1, T2, T3, T4, T5,T6> pred){ return pred; }

    public static <T1,T2,T3,T4,T5,T6> Predicate6<T1,T2,T3,T4,T5,T6> always(){
        return (a,b,c,d,e,f)->true;
    }

    public static <T1,T2,T3,T4,T5,T6> Predicate6<T1,T2,T3,T4,T5,T6> never(){
        return (a,b,c,d,e,f)->false;
    }

    public static <T1,T2,T3,T4,T5,T6> Predicate6<T1,T2,T3,T4,T5,T6> and(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                  Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6){
        return (a,b,c,d,e,f)->p1.test(a) && p2.test(b) && p3.test(c) && p4.test(d) && p5.test(e) && p6.test(f);
    }

    public static <T1,T2,T3,T4,T5,T6> Predicate6<T1,T2,T3,T4,T5,T6> first(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                    Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6){
        return (a,b,c,d,e,f)->p1.test(a) && !p2.test(b) && !p3.test(c) && !p4.test(d) && !p5.test(e) && !p6.test(f);
    }

    public static <T1,T2,T3,T4,T5,T6> Predicate6<T1,T2,T3,T4,T5,T6> second(Predicate<? super T1> p1, Predicate<? super T2> p2,
                                                                     Predicate<? super T3> p3, Predicate<? super T4> p4,
                                                                     Predicate<? super T5> p5, Predicate<? super T6> p6){
        return (a,b,c,d,e,f)->!p1.test(a) && p2.test(b)  && !p3.test(c) && p4.test(d) && p5.test(e);
    }

    public static <T1,T2,T3,T4,T5,T6> Predicate6<T1,T2,T3,T4,T5,T6> third(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                    Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6){
        return (a,b,c,d,e,f)->!p1.test(a) && !p2.test(b)  && p3.test(c) && !p4.test(d)  && !p5.test(e);
    }

    public static <T1,T2,T3,T4,T5,T6> Predicate6<T1,T2,T3,T4,T5,T6> fourth(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                     Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6){
        return (a,b,c,d,e,f)->!p1.test(a) && !p2.test(b)  && !p3.test(c) && p4.test(d) && !p5.test(e);
    }

    public static <T1,T2,T3,T4,T5,T6> Predicate6<T1,T2,T3,T4,T5,T6> fifth(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                    Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6){
        return (a,b,c,d,e,f)->!p1.test(a) && !p2.test(b)  && !p3.test(c) && !p4.test(d) && p5.test(e) && !p6.test(f);
    }

    public static <T1,T2,T3,T4,T5,T6> Predicate6<T1,T2,T3,T4,T5,T6> sixt(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                    Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6){
        return (a,b,c,d,e,f)->!p1.test(a) && !p2.test(b)  && !p3.test(c) && !p4.test(d) && !p5.test(e) && p6.test(f);
    }

    public static <T1,T2,T3,T4,T5,T6> Predicate6<T1,T2,T3,T4,T5,T6> _1(Predicate<? super T1> p1){
        return (a,b,c,d,e,f)->p1.test(a);
    }

    public static <T1,T2,T3,T4,T5,T6> Predicate6<T1,T2,T3,T4,T5,T6> _2(Predicate<? super T2> p2){
        return (a,b,c,d,e,f)->p2.test(b);
    }

    public static <T1,T2,T3,T4,T5,T6> Predicate6<T1,T2,T3,T4,T5,T6> _3(Predicate<? super T3> p3){
        return (a,b,c,d,e,f)->p3.test(c);
    }

    public static <T1,T2,T3,T4,T5,T6> Predicate6<T1,T2,T3,T4,T5,T6> _4(Predicate<? super T4> p4){
        return (a,b,c,d,e,f)->p4.test(d);
    }

    public static <T1,T2,T3,T4,T5,T6> Predicate6<T1,T2,T3,T4,T5,T6> _5(Predicate<? super T5> p5){
        return (a,b,c,d,e,f)->p5.test(e);
    }

    public static <T1,T2,T3,T4,T5,T6> Predicate6<T1,T2,T3,T4,T5,T6> _6(Predicate<? super T6> p6){
        return (a,b,c,d,e,f)->p6.test(f);
    }

    public static <T1,T2,T3,T4,T5,T6> Predicate6<T1,T2,T3,T4,T5,T6> or(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                 Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6){
        return (a,b,c,d,e,f)->p1.test(a) || p2.test(b) || p3.test(c) || p4.test(d) || p5.test(e) || p6.test(f);
    }

    public static <T1,T2,T3,T4,T5,T6> Predicate6<T1,T2,T3,T4,T5,T6> xor(Predicate<? super T1> p1, Predicate<? super T2> p2, Predicate<? super T3> p3,
                                                                  Predicate<? super T4> p4, Predicate<? super T5> p5, Predicate<? super T6> p6){
        return (a,b,c,d,e,f)->p1.test(a) ^ p2.test(b) ^ p3.test(c) ^ p4.test(d) ^ p5.test(e) ^ p6.test(f);
    }
}
