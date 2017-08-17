package cyclops.patterns;

import lombok.Value;
import org.jooq.lambda.tuple.Tuple1;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;


public interface CaseClass2<T1, T2> {

    Tuple2<T1,T2> unapply();

    default <R> CaseClass1<R> one(Function<Tuple2<T1,T2>,Tuple1<R>> fn){
        return ()->fn.apply(unapply());
    }

    default T1 _1(){
        return unapply().v1;
    }
    default T2 _2(){
        return unapply().v2;
    }

    default <R> R match(BiFunction<? super T1,? super T2, ? extends R> match){
        return match.apply(_1(),_2());
    }

    default <R> Optional<R> match(_CASE_2<T1, T2, R>... cases){
        return Stream.of(cases)
                .filter(c -> c.getPredicate().test(_1(), _2()))
                .findFirst().map(c -> c.getMatch().apply(_1(), _2()));

    }

    default <R> R matchWhen(BiFunction<? super T1, ? super T2, ? extends R> match, _CASE_2<T1, T2, R>... cases){

        Optional< R> opt = Stream.of(cases)
                                 .filter(c -> c.getPredicate().test(_1(), _2()))
                                 .findFirst().map(c -> c.getMatch().apply(_1(), _2()));

      return opt.orElse(match.apply(_1(),_2()));
    }

    public static <T1,T2,R> _CASE_2<T1,T2,R> _CASE_(BiPredicate<? super T1,? super T2> predicate,
                                                    BiFunction<? super T1, ? super T2, ? extends R> match){
        return new _CASE_2<>(predicate,match);
    }

    @Value
    static class _CASE_2<T1,T2,R>{
        private final BiPredicate<? super T1,? super T2> predicate;
        private final  BiFunction<? super T1, ? super T2, ? extends R> match;

    }
}