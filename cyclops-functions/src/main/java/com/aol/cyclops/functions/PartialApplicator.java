package com.aol.cyclops.functions;

import com.aol.cyclops.lambda.utils.Lambda;

import java.util.function.BiFunction;
import java.util.function.Function;


public class PartialApplicator extends Lambda{

    public static<T1,T2,T3,R> Function<T3,R> partial3(T1 t1, T2 t2, TriFunction<T1,T2,T3,R> triFunc){
        return (t3) -> triFunc.apply(t1,t2,t3);
    }
    
    public static<T1,T2,T3,R> BiFunction<T2,T3,R> partial3(T1 t1, TriFunction<T1,T2,T3,R> triFunc){
        return (t2,t3) -> triFunc.apply(t1,t2,t3);
    }
    
    public static <T1,T2,T3,T4,R> Function<T4,R>  partial4(T1 t1, T2 t2, T3 t3, QuadFunction<T1,T2,T3,T4,R> quadFunc){
        return (t4) -> quadFunc.apply(t1,t2,t3,t4);
    }

    public static <T1,T2,T3,T4,R> BiFunction<T3,T4,R>  partial4(T1 t1, T2 t2, QuadFunction<T1,T2,T3,T4,R> quadFunc){
        return (t3, t4) -> quadFunc.apply(t1,t2,t3,t4);
    }
    
    public static <T1,T2,T3,T4,R> TriFunction<T2,T3,T4,R>  partial4(T1 t1, QuadFunction<T1,T2,T3,T4,R> quadFunc){
        return (t2, t3, t4) -> quadFunc.apply(t1,t2,t3,t4);
    }
    
    public static <T1,T2,T3,T4,T5,R> Function<T5,R> partial5(T1 t1, T2 t2, T3 t3, T4 t4, QuintFunction<T1, T2, T3, T4, T5, R> quintFunc) {
    	return (t5) -> quintFunc.apply(t1,t2,t3,t4,t5);
    }

    public static <T1,T2,T3,T4,T5,R> BiFunction<T4,T5,R> partial5(T1 t1, T2 t2, T3 t3, QuintFunction<T1, T2, T3, T4, T5, R> quintFunc) {
    	return (t4, t5) -> quintFunc.apply(t1,t2,t3,t4,t5);
    }
    
    public static <T1,T2,T3,T4,T5,R> TriFunction<T3,T4,T5,R>  partial5(T1 t1, T2 t2, QuintFunction<T1,T2,T3,T4,T5,R> quintFunc){
        return (t3,t4,t5) -> quintFunc.apply(t1,t2,t3,t4,t5);
    }
    
    public static <T1,T2,T3,T4,T5,R> QuadFunction<T2,T3,T4,T5,R>  partial5(T1 t1, QuintFunction<T1,T2,T3,T4,T5,R> quintFunc){
        return (t2,t3,t4,t5) -> quintFunc.apply(t1,t2,t3,t4,t5);
    }

    public static <T1,T2,T3,T4,T5,T6,R> Function<T6,R>  partial6(T1 t1, T2 t2,T3 t3, T4 t4, T5 t5,  HexFunction<T1,T2,T3,T4,T5,T6,R> hexFunc){
        return (t6) -> hexFunc.apply(t1,t2,t3,t4,t5,t6);
    }
    
    public static <T1,T2,T3,T4,T5,T6,R> BiFunction<T5,T6,R>  partial6(T1 t1, T2 t2,T3 t3, T4 t4,  HexFunction<T1,T2,T3,T4,T5,T6,R> hexFunc){
        return (t5,t6) -> hexFunc.apply(t1,t2,t3,t4,t5,t6);
    }
    
    public static <T1,T2,T3,T4,T5,T6,R> TriFunction<T4,T5,T6,R> partial6(T1 t1, T2 t2,T3 t3,  HexFunction<T1,T2,T3,T4,T5,T6,R> hexFunc){
    	return (t4,t5,t6) -> hexFunc.apply(t1,t2,t3,t4,t5,t6);
    }
            
    public static <T1,T2,T3,T4,T5,T6,R> QuadFunction<T3,T4,T5,T6,R>  partial6(T1 t1, T2 t2, HexFunction<T1,T2,T3,T4,T5,T6,R> hexFunc){
        return (t3,t4,t5,t6) -> hexFunc.apply(t1,t2,t3,t4,t5,t6);
    }
    
    public static <T1,T2,T3,T4,T5,T6,R> QuintFunction<T2,T3,T4,T5,T6,R>  partial6(T1 t1, HexFunction<T1,T2,T3,T4,T5,T6,R> hexFunc){
        return (t2,t3,t4,t5,t6) -> hexFunc.apply(t1,t2,t3,t4,t5,t6);
    }
    
    public static <T1,T2,T3,T4,T5,T6,T7,R> Function<T7,R>  partial7(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, HeptFunction<T1,T2,T3,T4,T5,T6,T7,R> heptFunc){
    	return (t7) -> heptFunc.apply(t1,t2,t3,t4,t5,t6,t7);
    }
    
    public static <T1,T2,T3,T4,T5,T6,T7,R> BiFunction<T6,T7,R>  partial7(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, HeptFunction<T1,T2,T3,T4,T5,T6,T7,R> heptFunc){
    	return (t6,t7) -> heptFunc.apply(t1,t2,t3,t4,t5,t6,t7);
    }
    
    public static <T1,T2,T3,T4,T5,T6,T7,R> TriFunction<T5,T6,T7,R>  partial7(T1 t1, T2 t2, T3 t3, T4 t4, HeptFunction<T1,T2,T3,T4,T5,T6,T7,R> heptFunc){
    	return (t5,t6,t7) -> heptFunc.apply(t1,t2,t3,t4,t5,t6,t7);
    }
    
    public static <T1,T2,T3,T4,T5,T6,T7,R> QuadFunction<T4,T5,T6,T7,R>  partial7(T1 t1, T2 t2, T3 t3, HeptFunction<T1,T2,T3,T4,T5,T6,T7,R> heptFunc){
    	return (t4,t5,t6,t7) -> heptFunc.apply(t1,t2,t3,t4,t5,t6,t7);
    }
 
    public static <T1,T2,T3,T4,T5,T6,T7,R> QuintFunction<T3,T4,T5,T6,T7,R>  partial7(T1 t1, T2 t2, HeptFunction<T1,T2,T3,T4,T5,T6,T7,R> heptFunc){
        return (t3,t4,t5,t6,t7) -> heptFunc.apply(t1,t2,t3,t4,t5,t6,t7);
    }
    
    public static <T1,T2,T3,T4,T5,T6,T7,R> HexFunction<T2,T3,T4,T5,T6,T7,R>  partial7(T1 t1, HeptFunction<T1,T2,T3,T4,T5,T6,T7,R> heptFunc){
        return (t2,t3,t4,t5,t6,t7) -> heptFunc.apply(t1,t2,t3,t4,t5,t6,t7);
    }
    
    public static <T1,T2,T3,T4,T5,T6,T7,T8,R> Function<T8,R>  partial8(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, OctFunction<T1,T2,T3,T4,T5,T6,T7,T8,R> octFunc){
    	return (t8) -> octFunc.apply(t1,t2,t3,t4,t5,t6,t7,t8);
    }
    
/*    public static <T1,T2,T3,T4,T5,T6,T7,T8,R> BiFunction<T6,T7,R>  partial8(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, OctFunction<T1,T2,T3,T4,T5,T6,T7,T8,R> octFunc){
    	return (t7,t8) -> octFunc.apply(t1,t2,t3,t4,t5,t6,t7,t8);
    }
    
    public static <T1,T2,T3,T4,T5,T6,T7,T8,R> TriFunction<T5,T6,T7,R>  partial8(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, OctFunction<T1,T2,T3,T4,T5,T6,T7,T8,R> octFunc){
    	return (t6,t7,t8) -> octFunc.apply(t1,t2,t3,t4,t5,t6,t7,t8);
    }
    
    public static <T1,T2,T3,T4,T5,T6,T7,T8,R> QuadFunction<T4,T5,T6,T7,R>  partial8(T1 t1, T2 t2, T3 t3, OctFunction<T1,T2,T3,T4,T5,T6,T7,T8,R> octFunc){
    	return (t5,t6,t7,t8) -> octFunc.apply(t1,t2,t3,t4,t5,t6,t7,t8);
    }
 
    public static <T1,T2,T3,T4,T5,T6,T7,T8,R> QuintFunction<T3,T4,T5,T6,T7,R>  partial8(T1 t1, T2 t2, OctFunction<T1,T2,T3,T4,T5,T6,T7,T8,R> octFunc){
        return (t4,t5,t6,t7,t8) -> octFunc.apply(t1,t2,t3,t4,t5,t6,t7,t8);
    }
    
    public static <T1,T2,T3,T4,T5,T6,T7,T8,R> HexFunction<T2,T3,T4,T5,T6,T7,R>  partial8(T1 t1, OctFunction<T1,T2,T3,T4,T5,T6,T7,T8,R> octFunc){
        return (t2,t3,t4,t5,t6,t7) -> octFunc.apply(t1,t2,t3,t4,t5,t6,t7,t8);
    }*/

}