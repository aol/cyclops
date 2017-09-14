package com.aol.cyclops2.matching.types;

import org.jooq.lambda.tuple.Tuple1;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.jooq.lambda.tuple.Tuple5;

@FunctionalInterface
public interface Deconstruct<T> {

  T deconstruct();

  interface Deconstruct1<T1> extends Deconstruct<Tuple1<T1>> {

  }

  interface Deconstruct2<T1, T2> extends Deconstruct<Tuple2<T1, T2>> {

  }

  interface Deconstruct3<T1, T2, T3> extends Deconstruct<Tuple3<T1, T2, T3>> {

  }

  interface Deconstruct4<T1, T2, T3, T4> extends Deconstruct<Tuple4<T1, T2, T3, T4>> {


  }

  interface Deconstruct5<T1, T2, T3, T4, T5> extends Deconstruct<Tuple5<T1, T2, T3, T4, T5>> {

  }

}
