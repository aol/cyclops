package com.aol.cyclops.comprehensions.donotation.typed;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.jooq.lambda.tuple.Tuple;
import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.For;
import com.aol.cyclops.control.Maybe;
public class ValueIssueTest {

    @Test
    public void valueBug(){
        assertThat(For.iterable((Iterable<Integer>)AnyM.fromOptional(Optional.of(10)))
                      .anyM(i->AnyM.fromOptional(Optional.of(i+5)))
                      .yield2(Tuple::tuple).toMaybe().get().get(0),equalTo(Tuple.tuple(10,15)));
    }
    @Test
    public void valueBug2(){
        assertThat(For.Values.each2(Maybe.of(10),i->Maybe.<Integer>of(i+5),
                      Tuple::tuple).toMaybe().get(),equalTo(Tuple.tuple(10,15)));
    }
    @Test
    public void forGenFilter2(){
        
        
        For.anyM((AnyM<Integer>)Eval.now(10).anyM())
          .anyM(i->AnyM.fromEval(Eval.now(i+5)))
          .yield2(Tuple::tuple).toMaybe().printOut();;
    }
}
