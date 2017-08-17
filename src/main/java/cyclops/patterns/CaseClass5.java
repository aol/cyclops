package cyclops.patterns;

import cyclops.function.*;
import lombok.Value;
import org.jooq.lambda.tuple.*;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;


public interface CaseClass5<T1, T2,T3, T4,T5>{

    Tuple5<T1,T2,T3,T4,T5> unapply();

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
    default T5 _5(){
        return unapply().v5;
    }

    default <R> CaseClass1<R> one(Function<Tuple5<T1,T2,T3,T4,T5>,Tuple1<R>> fn){
        return ()->fn.apply(unapply());
    }
    default <R1,R2> CaseClass2<R1,R2> two(Function<Tuple5<T1,T2,T3,T4,T5>,Tuple2<R1,R2>> fn){
        return ()->fn.apply(unapply());
    }
    default <R1,R2,R3> CaseClass3<R1,R2,R3> three(Function<Tuple5<T1,T2,T3,T4,T5>,Tuple3<R1,R2,R3>> fn){
        return ()->fn.apply(unapply());
    }
    default <R1,R2,R3,R4> CaseClass4<R1,R2,R3,R4> four(Function<Tuple5<T1,T2,T3,T4,T5>,Tuple4<R1,R2,R3,R4>> fn){
        return ()->fn.apply(unapply());
    }

    default <R> R match(Fn5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> match){
        return match.apply(_1(),_2(),_3(),_4(),_5());
    }

    default <R> Optional<R> match(_CASE_5<T1, T2, T3, T4, T5, R>... cases){
        return Stream.of(cases)
                .filter(c -> c.getPredicate().test(_1(), _2(),_3(),_4(),_5()))
                .findFirst().map(c -> c.getMatch().apply(_1(), _2(),_3(),_4(),_5()));

    }

    default <R> R matchWhen(Fn5<? super T1, ? super T2, ? super T3,? super T4, ? super T5,? extends R> match, _CASE_5<T1, T2, T3, T4, T5, R>... cases){

        Optional< R> opt = Stream.of(cases)
                                 .filter(c -> c.getPredicate().test(_1(), _2(),_3(),_4(),_5()))
                                 .findFirst().map(c -> c.getMatch().apply(_1(), _2(),_3(),_4(),_5()));

      return opt.orElse(match.apply(_1(),_2(),_3(),_4(),_5()));
    }


    public static <T1,T2,T3,T4,T5,R> _CASE_5<T1,T2,T3,T4,T5,R> _CASE_(P5<? super T1, ? super T2, ? super T3, ? super T4,? super T5> predicate,
                                                                Fn5<? super T1, ? super T2, ? super T3, ? super T4,? super T5, ? extends R> match){
        return new _CASE_5<>(predicate,match);
    }

    @Value
    static class _CASE_5<T1,T2,T3,T4,T5,R>{
        private final P5<? super T1,? super T2, ? super T3, ? super T4,? super T5> predicate;
        private final Fn5<? super T1, ? super T2,? super T3,? super T4, ? super T5,? extends R> match;
    }
}