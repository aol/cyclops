package cyclops.control;


import com.oath.cyclops.hkt.Higher;
import com.oath.cyclops.hkt.Higher2;
import com.oath.cyclops.matching.Deconstruct;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple1;
import cyclops.function.Monoid;
import cyclops.function.Semigroup;

import com.oath.cyclops.hkt.DataWitness.constant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @param <T> Value type
 * @param <P> Phantom type
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constant<T,P> implements Higher2<constant,T,P> , Supplier<T>, Deconstruct.Deconstruct1<T>, Serializable {
    private static final long serialVersionUID = 1L;

    public final T value;


    public <R> Constant<T,R> map(Function<? super P, ? extends R> fn){
        return of(value);
    }

    public static <T,P> Constant<T,P> of(T value){
        return new Constant<>(value);
    }

    public T get(){
        return value;
    }

    public static <T,P> Constant<T,P> narrowK2(Higher2<constant,T,P> constant){
        return (Constant<T,P>) constant;
    }
    public static <T,P> Constant<T,P> narrowK(Higher<Higher<constant,T>,P> constant){
        return (Constant<T,P>) constant;
    }

    @Override
    public Tuple1<T> unapply() {
        return Tuple.tuple(value);
    }



}
