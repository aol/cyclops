package com.aol.cyclops.internal.matcher2;

import org.jooq.lambda.tuple.Tuple1;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.jooq.lambda.tuple.Tuple5;

import com.aol.cyclops.control.Matchable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

@AllArgsConstructor
public class MatchableCase<X> extends CaseBeingBuilt {
    @Getter //(AccessLevel.PACKAGE)
    @Wither(AccessLevel.PACKAGE)
    private final PatternMatcher patternMatcher;

    public <T> Matchable.CheckValues<X, T> withType(final Class<T> type) {
        return new Matchable.CheckValues(
                                         type, this);

    }

    public <T1, T2, T3, T4, T5> Matchable.CheckValue5<X, T1, T2, T3, T4, T5> withType5(final Class<Tuple5<T1, T2, T3, T4, T5>> type) {
        return new Matchable.CheckValue5(
                                         type, this);
    }

    public <T1, T2, T3, T4> Matchable.CheckValue4<X, T1, T2, T3, T4> withType4(final Class<Tuple4<T1, T2, T3, T4>> type) {
        return new Matchable.CheckValue4(
                                         type, this);
    }

    public <T1, T2, T3> Matchable.CheckValue3<X, T1, T2, T3> withType3(final Class<Tuple3<T1, T2, T3>> type) {
        return new Matchable.CheckValue3(
                                         type, this);
    }

    public <T1, T2> Matchable.CheckValue2<X, T1, T2> withType2(final Class<Tuple2<T1, T2>> type) {
        return new Matchable.CheckValue2(
                                         type, this);
    }

    public <T1> Matchable.CheckValue1<X, T1> withType1(final Class<Tuple1<T1>> type) {
        return new Matchable.CheckValue1(
                                         type, this);
    }

    public <T1> Matchable.CheckValueOpt<X, T1> withTypeOpt(final Class<Tuple1<T1>> type) {
        return new Matchable.CheckValueOpt(
                                           type, this);
    }

}
