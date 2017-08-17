package cyclops.patterns;

import cyclops.function.Fn3;
import cyclops.function.P3;
import lombok.Value;
import org.jooq.lambda.tuple.Tuple1;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;


public interface CaseClass3<T1, T2,T3>{

    Tuple3<T1,T2,T3> unapply();

    default T1 _1(){
        return unapply().v1;
    }
    default T2 _2(){
        return unapply().v2;
    }
    default T3 _3(){
        return unapply().v3;
    }

    default <R> CaseClass1<R> one(Function<Tuple3<T1,T2,T3>,Tuple1<R>> fn){
        return ()->fn.apply(unapply());
    }
    default <R1,R2> CaseClass2<R1,R2> two(Function<Tuple3<T1,T2,T3>,Tuple2<R1,R2>> fn){
        return ()->fn.apply(unapply());
    }

    default <R> R match(Fn3<? super T1, ? super T2, ? super T3, ? extends R> match){
        return match.apply(_1(),_2(),_3());
    }

    default <R> Optional<R> match(_CASE_3<T1, T2, T3, R>... cases){
        return Stream.of(cases)
                .filter(c -> c.getPredicate().test(_1(), _2(),_3()))
                .findFirst().map(c -> c.getMatch().apply(_1(), _2(),_3()));

    }

    default <R> R matchWhen(Fn3<? super T1,? super T2, ? super T3, ? extends R> match, _CASE_3<T1, T2, T3, R>... cases){

        Optional< R> opt = Stream.of(cases)
                                 .filter(c -> c.getPredicate().test(_1(), _2(),_3()))
                                 .findFirst().map(c -> c.getMatch().apply(_1(), _2(),_3()));

      return opt.orElse(match.apply(_1(),_2(),_3()));
    }

    public static <T1,T2,T3,R> _CASE_3<T1,T2,T3,R> _CASE_(P3<? super T1, ? super T2, ? super T3> predicate,
                                                    Fn3<? super T1, ? super T2, ? super T3, ? extends R> match){
        return new _CASE_3<>(predicate,match);
    }

    @Value
    static class _CASE_3<T1,T2,T3,R>{
        private final P3<? super T1,? super T2, ? super T3> predicate;
        private final Fn3<? super T1, ? super T2,? super T3, ? extends R> match;
    }
}