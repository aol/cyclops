package com.aol.cyclops.comprehensions.donotation.typed;

import com.aol.cyclops.control.ReactiveSeq;
import org.junit.Test;

import static com.aol.cyclops.control.ReactiveSeq.range;
import static org.jooq.lambda.tuple.Tuple.tuple;
public class DoTest {
	
    @Test
    public void doGen2(){
       
        ReactiveSeq.range(1,10)
                   .forEach2(i->range(0, i), (i,j)->tuple(i,j));
                
        //  .forEach(System.out::println);
        
        
    }

}
