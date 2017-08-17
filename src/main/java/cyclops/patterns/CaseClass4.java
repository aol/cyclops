package cyclops.patterns;

import cyclops.function.Fn3;
import cyclops.function.Fn4;
import cyclops.function.P3;
import cyclops.function.P4;
import lombok.Value;
import org.jooq.lambda.tuple.Tuple1;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;


public interface CaseClass4<T1, T2,T3, T4>{

    Tuple4<T1,T2,T3,T4> unapply();

    default T1 _1(){
        return unapply().v1;
    }
    default T2 _2(){
        return unapply().v2;
    }
    default T3 _3(){
        return unapply().v3;
    }
    default T4 _4(){
        return unapply().v4;
    }

    default <R> CaseClass1<R> one(Function<Tuple4<T1,T2,T3,T4>,Tuple1<R>> fn){
        return ()->fn.apply(unapply());
    }
    default <R1,R2> CaseClass2<R1,R2> two(Function<Tuple4<T1,T2,T3,T4>,Tuple2<R1,R2>> fn){
        return ()->fn.apply(unapply());
    }
    default <R1,R2,R3> CaseClass3<R1,R2,R3> three(Function<Tuple4<T1,T2,T3,T4>,Tuple3<R1,R2,R3>> fn){
        return ()->fn.apply(unapply());
    }
    default <R> R match(Fn4<? super T1, ? super T2, ? super T3,? super T4, ? extends R> match){
        return match.apply(_1(),_2(),_3(),_4());
    }

    default <R> Optional<R> match(_CASE_4<T1, T2, T3,T4, R>... cases){
        return Stream.of(cases)
                .filter(c -> c.getPredicate().test(_1(), _2(),_3(),_4()))
                .findFirst().map(c -> c.getMatch().apply(_1(), _2(),_3(),_4()));

    }

    default <R> R matchWhen(Fn4<? super T1, ? super T2, ? super T3,? super T4, ? extends R> match, _CASE_4<T1, T2, T3, T4, R>... cases){

        Optional< R> opt = Stream.of(cases)
                                 .filter(c -> c.getPredicate().test(_1(), _2(),_3(),_4()))
                                 .findFirst().map(c -> c.getMatch().apply(_1(), _2(),_3(),_4()));

      return opt.orElse(match.apply(_1(),_2(),_3(),_4()));
    }

    public static <T1,T2,T3,T4,R> _CASE_4<T1,T2,T3,T4,R> _CASE_(P4<? super T1, ? super T2, ? super T3, ? super T4> predicate,
                                                          Fn4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> match){
        return new _CASE_4<>(predicate,match);
    }

    @Value
    static class _CASE_4<T1,T2,T3,T4,R>{
        P4<? super T1,? super T2, ? super T3, ? super T4> predicate;
        Fn4<? super T1, ? super T2,? super T3,? super T4, ? extends R> match;
    }
}