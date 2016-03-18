package com.aol.cyclops.comprehensions.donotation.typed;

import java.util.Optional;

import org.jooq.lambda.tuple.Tuple;
import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Do;
import com.aol.cyclops.control.Eval;

public class ValueIssueTest {

    @Test
    public void valueBug(){
        Do.add((Iterable<Integer>)AnyM.fromOptional(Optional.of(10)))
          .withAnyM(i->AnyM.fromOptional(Optional.of(i+5)))
          .yield(Tuple::tuple).toMaybe().printOut();
    }
    @Test
    public void forGenFilter2(){
        
        
        Do.add((AnyM<Integer>)Eval.now(10).anyM())
          .withAnyM(i->AnyM.fromEval(Eval.now(i+5)))
          .yield(Tuple::tuple).toMaybe().printOut();;
    }
}
