package com.aol.cyclops.control.anym;

import cyclops.Monoid;
import cyclops.Semigroups;
import com.aol.cyclops.types.anyM.Witness;
import org.junit.Before;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.FutureW;
import org.junit.Test;

import java.util.NoSuchElementException;

public class FutureWAnyMValueTest extends BaseAnyMValueTest<Witness.future> {
    @Before
    public void setUp() throws Exception {
        just = AnyM.fromFutureW(FutureW.ofResult(10));
        none = AnyM.fromFutureW(FutureW.ofError(new NoSuchElementException()));

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
