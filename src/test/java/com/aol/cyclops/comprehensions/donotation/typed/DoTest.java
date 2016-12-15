package com.aol.cyclops.comprehensions.donotation.typed;

import static com.aol.cyclops.control.ReactiveSeq.range;
import static org.hamcrest.Matchers.equalTo;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.junit.Test;

import com.aol.cyclops.control.For;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.anyM.AnyMSeq;

import lombok.val;
public class DoTest {
	
    @Test
    public void doGen2(){
       
        ReactiveSeq.range(1,10)
                   .forEach2(i->range(0, i), (i,j)->tuple(i,j));
                
        //  .forEach(System.out::println);
        
        
    }

}
