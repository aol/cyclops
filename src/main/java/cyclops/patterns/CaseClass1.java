package cyclops.patterns;

import lombok.Value;
import org.jooq.lambda.tuple.Tuple1;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;


public interface CaseClass1<T1>{

    Tuple1<T1> unapply();

    default T1 _1(){
        return unapply().v1;
    }


    default <R> Optional<R> match(_CASE_1<T1, R>... cases){
        return Stream.of(cases)
                .filter(c -> c.getPredicate().test(_1()))
                .findFirst().map(c -> c.getMatch().apply(_1()));

    }

    default <R> R matchWhen(Function<T1,? extends R> match, _CASE_1<T1, R>... cases){

        Optional< R> opt = Stream.of(cases)
                                 .filter(c -> c.getPredicate().test(_1()))
                                 .findFirst().map(c -> c.getMatch().apply(_1()));

      return opt.orElse(match.apply(_1()));
    }
    default <R> R match(Function<? super T1, ? extends R> match){

        return match.apply(_1());
    }


    public static <T1,R> _CASE_1<T1,R> _CASE_(Predicate<? super T1> predicate,
                                                    Function<? super T1, ? extends R> match){
        return new _CASE_1<T1,R>(predicate,match);
    }

    @Value
    static class _CASE_1<T1,R>{
        private final Predicate<? super T1> predicate;
        private final Function<? super T1, ? extends R> match;
    }
}