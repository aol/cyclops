package com.oath.cyclops.control;

import cyclops.control.*;
import cyclops.monads.AnyM;
import org.junit.Test;

public class MaybeTest {

  @Test
  public void testMaybeWithNull() {
    System.out.println(Maybe.of(null).toString());
    System.out.println(Maybe.just(null).toString());
    System.out.println(Either.left(null).toString());
    System.out.println(Either.right(null).toString());
    System.out.println(AnyM.always(null));
    System.out.println(Eval.defer(null).toString());
    System.out.println(Eval.later(null).toString());
    System.out.println(LazyEither5.left1(null).toString());
    System.out.println(LazyEither5.left2(null).toString());
    System.out.println(LazyEither5.left3(null).toString());
    System.out.println(LazyEither5.left4(null).toString());
    System.out.println(LazyEither5.right(null).toString());
    System.out.println(Option.just(null).toString());
    System.out.println(Option.some(null).toString());
    System.out.println(Option.of(null).toString());
  }
}
