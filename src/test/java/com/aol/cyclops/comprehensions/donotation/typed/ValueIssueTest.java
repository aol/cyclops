package com.aol.cyclops.comprehensions.donotation.typed;

import java.util.Optional;

import org.jooq.lambda.tuple.Tuple;
import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.For;
import com.aol.cyclops.control.Eval;

public class ValueIssueTest {

    @Test
    public void valueBug(){
        For.iterable((Iterable<Integer>)AnyM.fromOptional(Optional.of(10)))
          .anyM(i->AnyM.fromOptional(Optional.of(i+5)))
          .yield(Tuple::tuple).toMaybe().printOut();
    }
    @Test
    public void forGenFilter2(){
        
        
        For.anyM((AnyM<Integer>)Eval.now(10).anyM())
          .anyM(i->AnyM.fromEval(Eval.now(i+5)))
          .yield(Tuple::tuple).toMaybe().printOut();;
    }
}
