package cyclops.companion.rx2;

import com.oath.cyclops.util.ExceptionSoftener;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by johnmcclean on 13/06/2017.
 */
public class Functions {

    public static <T1,T2,R> BiFunction<T1,T2,R> bifunction(io.reactivex.functions.BiFunction<T1,T2,R> fn){
        return ExceptionSoftener.softenBiFunction((a,b)->fn.apply(a,b));
    }
    public static <T1,T2,R> io.reactivex.functions.BiFunction<T1,T2,R> rxBifunction(BiFunction<T1,T2,R> fn){
        return (a,b)->fn.apply(a,b);
    }
    public static <T1,R> Function<T1,R> function(io.reactivex.functions.Function<T1,R> fn){
        return ExceptionSoftener.softenFunction((a)->fn.apply(a));
    }
    public static <T1,R> io.reactivex.functions.Function<T1,R> rxFunction(Function<T1,R> fn){
        return (a)->fn.apply(a);
    }
    public static <T1> Predicate<T1> predicate(io.reactivex.functions.Predicate<T1> fn){
        return ExceptionSoftener.softenPredicate((a)->fn.test(a));
    }
    public static <T1> io.reactivex.functions.Predicate<T1> rxPredicate(Predicate<T1> fn){
        return (a)->fn.test(a);
    }
}
