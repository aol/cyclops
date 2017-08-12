package cyclops.patterns;

import lombok.Value;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;


public interface CaseClass2<T1, T2>{

    Tuple2<T1,T2> unapply();

    default T1 _1(){
        return unapply().v1;
    }
    default T2 _2(){
        return unapply().v2;
    }

    default <R> R match(BiFunction<T1, T2,? extends R> match){
        return match.apply(_1(),_2());
    }
    default <R> Optional<R> match(Case2<T1,T2,R>... cases){
        return Stream.of(cases)
                .filter(c -> c.getPredicate().test(_1(), _2()))
                .findFirst().map(c -> c.getMatch().apply(_1(), _2()));

    }

    default <R> R matchWhen(BiFunction<T1, T2, ? extends R> match, Case2<T1,T2,R>... cases){

        Optional< R> opt = Stream.of(cases)
                                 .filter(c -> c.getPredicate().test(_1(), _2()))
                                 .findFirst().map(c -> c.getMatch().apply(_1(), _2()));

      return opt.orElse(match.apply(_1(),_2()));
    }
    default <R> R match_1(Function<T1,? extends R> match){

        return match.apply(_1());
    }
    default <R> R match_2(Function<T2,? extends R> match){
        return match.apply(_2());
    }
    public static <T1,T2,R> Case2<T1,T2,R> _CASE_(BiPredicate<T1,T2> predicate,
                                                  BiFunction<T1, T2, ? extends R> match){
        return new Case2<>(predicate,match);
    }
    @Value
    static class Case2<T1,T2,R>{
        BiPredicate<T1,T2> predicate;
        BiFunction<T1, T2, ? extends R> match;


    }
}