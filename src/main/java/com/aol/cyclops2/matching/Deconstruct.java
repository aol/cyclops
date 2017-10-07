package com.aol.cyclops2.matching;

import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Function5;
import cyclops.collections.tuple.Tuple1;
import cyclops.collections.tuple.Tuple2;
import cyclops.collections.tuple.Tuple3;
import cyclops.collections.tuple.Tuple4;
import cyclops.collections.tuple.Tuple5;

import java.util.function.BiFunction;
import java.util.function.Function;

@FunctionalInterface
public interface Deconstruct<T> {

  T unapply();


  interface Deconstruct1<T1> extends Deconstruct<Tuple1<T1>> {
    default <R> R fold(Function<? super T1, ? extends R> match){

      return match.apply(unapply()._1());
    }
  }

  interface Deconstruct2<T1, T2> extends Deconstruct<Tuple2<T1, T2>> {
    default <R> R fold(BiFunction<? super T1, ? super T2, ? extends R> match){
      Tuple2<T1,T2> t2 = unapply();
      return match.apply(t2._1(),t2._2());
    }
  }

  interface Deconstruct3<T1, T2, T3> extends Deconstruct<Tuple3<T1, T2, T3>> {
    default <R> R fold(Function3<? super T1, ? super T2, ? super T3, ? extends R> match){
      Tuple3<T1,T2,T3> t = unapply();
      return match.apply(t._1(),t._2(),t._3());
    }
  }

  interface Deconstruct4<T1, T2, T3, T4> extends Deconstruct<Tuple4<T1, T2, T3, T4>> {
    default <R> R fold(Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> match){
      Tuple4<T1,T2,T3,T4> t = unapply();
      return match.apply(t._1(),t._2(),t._3(),t._4());
    }

  }

  interface Deconstruct5<T1, T2, T3, T4, T5> extends Deconstruct<Tuple5<T1, T2, T3, T4, T5>> {
    default <R> R fold(Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> match){
      Tuple5<T1,T2,T3,T4,T5> t = unapply();
      return match.apply(t._1(),t._2(),t._3(),t._4(),t._5());
    }
  }

}
