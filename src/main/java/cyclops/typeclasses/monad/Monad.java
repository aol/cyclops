package cyclops.typeclasses.monad;

import com.aol.cyclops.hkt.Higher;
import cyclops.typeclasses.Unit;
import cyclops.typeclasses.functor.Functor;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;


public interface Monad<CRE> extends Applicative<CRE>,Functor<CRE>, Unit<CRE> {
    
    public <T,R> Higher<CRE,R> flatMap(Function<? super T, ? extends Higher<CRE, R>> fn, Higher<CRE, T> ds);
    
    default <T>  Higher<CRE,T> flatten(Higher<CRE, Higher<CRE, T>> nested){
        return flatMap(Function.identity(), nested);
    }
    
    default <T> Higher<CRE,Stream<T>> replicate(long times, Higher<CRE, T> m){
        return sequence(Stream.generate(()->m).limit(times));
    }
    
    default <T, R> Function<Stream<T>, Higher<CRE, Stream<R>>> mapM(final Function<T, Higher<CRE, R>> fn) {
        return stream -> sequence(stream.map(fn));
    }
    
    default <T> Higher<CRE,Stream<T>> sequence(Stream<Higher<CRE, T>> stream) {
        Higher<CRE,Stream<T>> identity = unit(Stream.empty());
        
        BiFunction<Higher<CRE,Stream<T>>,Higher<CRE,T>,Higher<CRE,Stream<T>>> combineToStream = (acc,next) -> ap2(unit(a->b->Stream.concat(a,Stream.of(b))),acc,next);

        BinaryOperator<Higher<CRE,Stream<T>>> combineStreams = (a,b)->a.apply(b, (s1,s2)->s1);  

        return stream.reduce(identity,combineToStream,combineStreams);
    }
    default <T,R> Higher<CRE,Stream<R>> traverse(Function<T, R> fn, Stream<Higher<CRE, T>> stream) {
       return sequence(stream.map(h->map(fn,h)));
    }
    
}
