package com.aol.cyclops.control.anym;

import cyclops.async.Future;
import cyclops.function.Monoid;
import cyclops.Semigroups;
import cyclops.monads.Witness;
import org.junit.Before;

import cyclops.monads.AnyM;
import org.junit.Test;

import java.util.NoSuchElementException;

public class FutureAnyMValueTest extends BaseAnyMValueTest<Witness.future> {
    @Before
    public void setUp() throws Exception {
        just = AnyM.fromFutureW(Future.ofResult(10));
        none = AnyM.fromFutureW(Future.ofError(new NoSuchElementException()));

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
    @Test(expected = NoSuchElementException.class)
    public void printNone(){
        System.out.println(just.get());
        System.err.println(none.get());

    }
}
