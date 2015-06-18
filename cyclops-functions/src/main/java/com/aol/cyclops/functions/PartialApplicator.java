package com.aol.cyclops.functions;

import com.aol.cyclops.lambda.utils.Lambda;

import java.util.function.BiFunction;
import java.util.function.Function;


public class PartialApplicator extends Lambda{

    public static<T1,T2,T3,R> Function<T3,R> partial3(T1 t1, T2 t2, TriFunction<T1,T2,T3,R> triFunc){
        return (t3) -> triFunc.apply(t1,t2,t3);
    }

    public static <T1,T2,T3,T4,R> BiFunction<T3,T4,R>  partial4(T1 t1, T2 t2, QuadFunction<T1,T2,T3,T4,R> quadFunc){
        return (t3, t4) -> quadFunc.apply(t1,t2,t3,t4);
    }

    public static <T1,T2,T3,T4,T5,R> TriFunction<T3,T4,T5,R>  partial5(T1 t1, T2 t2, QuintFunction<T1,T2,T3,T4,T5,R> quintFunc){
        return (t3,t4,t5) -> quintFunc.apply(t1,t2,t3,t4,t5);
    }

    public static <T1,T2,T3,T4,T5,T6,R> QuadFunction<T3,T4,T5,T6,R>  partial6(T1 t1, T2 t2, HexFunction<T1,T2,T3,T4,T5,T6,R> hexFunc){
        return (t3,t4,t5,t6) -> hexFunc.apply(t1,t2,t3,t4,t5,t6);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,R> QuintFunction<T3,T4,T5,T6,T7,R>  partial7(T1 t1, T2 t2, HeptFunction<T1,T2,T3,T4,T5,T6,T7,R> heptFunc){
        return (t3,t4,t5,t6,t7) -> heptFunc.apply(t1,t2,t3,t4,t5,t6,t7);
    }

    public static <T1,T2,T3,T4,T5,T6,T7,T8,R> HexFunction<T3,T4,T5,T6,T7,T8,R>  partial8(T1 t1, T2 t2, OctFunction<T1,T2,T3,T4,T5,T6,T7,T8,R> octFunc){
        return (t3,t4,t5,t6,t7,t8) -> octFunc.apply(t1,t2,t3,t4,t5,t6,t7,t8);
    }

}