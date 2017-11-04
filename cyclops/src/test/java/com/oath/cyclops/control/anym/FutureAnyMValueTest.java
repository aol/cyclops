package com.oath.cyclops.control.anym;

import cyclops.async.Future;
import cyclops.function.Monoid;
import cyclops.companion.Semigroups;
import cyclops.monads.DataWitness.future;
import org.junit.Before;

import cyclops.monads.AnyM;
import org.junit.Test;

import java.util.NoSuchElementException;

public class FutureAnyMValueTest extends BaseAnyMValueTest<future> {
    @Before
    public void setUp() throws Exception {
        just = AnyM.fromFuture(Future.ofResult(10));
        none = AnyM.fromFuture(Future.ofError(new NoSuchElementException()));

    }
    @Test
    public void combineEager(){


        Monoid<Integer> add = Monoid.of(0, Semigroups.intSum);

        just.flatMap(t1 -> none.map(t2 -> add.apply(t1, t2))).printOut();
        just.flatMapA(t1 -> none.map(t2 -> add.apply(t1, t2))).printOut();

    }
    @Test
    public void ofType(){
        just.ofType(String.class).printOut();
    }

    public void printNone(){
        System.out.println(just.get());
        System.err.println(none.get());

    }
}
