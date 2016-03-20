package com.aol.cyclops.comprehensions.donotation.typed;
import static com.aol.cyclops.control.For.Values.each2;
import static com.aol.cyclops.control.For.Values.each3;
import static com.aol.cyclops.control.For.Values.each4;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.jooq.lambda.tuple.Tuple;
import org.junit.Test;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

import lombok.val;
import reactor.core.publisher.Flux;
public class ForValueTest {
    @Test
    public void forGen2(){
        
       
        
        
        val list = each2(Maybe.just(10), 
                          i-> Eval.<Integer>now(i+5),
                          Tuple::tuple).toMaybe();
        
        val list2 = Maybe.just(10).flatMap(i-> Maybe.<Integer>just(i+5)
                                                            .map(a-> Tuple.<Integer,Integer>tuple(i,a))).toMaybe();
                                    
        assertThat(list,equalTo(list2));
                    
    }
    @Test
    public void valueBug(){
        Do.add((Iterable<Integer>)AnyM.fromOptional(Optional.of(10)))
          .withAnyM(i->AnyM.fromOptional(Optional.of(i+5)))
          .yield(Tuple::tuple).toMaybe().printOut();
    }
    @Test
    public void forGenFilter2(){
        
        
       
         
       val list = each2(Maybe.just(10), 
                        a-> Eval.<Integer>now(a+5),
                        (a,b)->a+b<10,
                        Tuple::tuple).toListX();

        val list2 = Maybe.just(10).flatMap(a-> Maybe.<Integer>just(a+5).filter(b->(a+b)<10)
                                                    .map(b-> Tuple.<Integer,Integer>tuple(a,b))).toListX();
                            

                                    
        assertThat(list,equalTo(list2));
                    
    }
    @Test
    public void forGen3(){
        val list = each3(Maybe.just(10), 
                        i-> Eval.<Integer>now(i+5),
                        (a,b) -> FutureW.<Integer>ofResult(a+b+10),
                        Tuple::tuple).toListX();

        val list2 = Maybe.just(10).flatMap(a-> Maybe.<Integer>just(a+5).flatMap(b-> Maybe.just(a+b+10)
                                                    .map(c-> Tuple.<Integer,Integer,Integer>tuple(a,b,c)))).toListX();
                            
        assertThat(list,equalTo(list2));
                    
    }
    @Test
    public void forGenFilter3(){
        
        val list = each3(Maybe.<Integer>just(10), 
                        i-> Eval.<Integer>now(i+5),
                        (a,b) -> FutureW.<Integer>ofResult(a+b+10),
                        (a,b,c)->a+b+c<10,
                        Tuple::tuple).toListX();

        val list2 = Maybe.just(10).flatMap(a-> Maybe.<Integer>just(a+5).flatMap(b-> Maybe.just(a+b+10)
                                            .filter(c->a+b+c<10)
                                            .map(c-> Tuple.<Integer,Integer,Integer>tuple(a,b,c)))).toListX();
                            

                                    
        assertThat(list,equalTo(list2));
                    
    }
    @Test
    public void forGen4(){
        val list = each4(Maybe.<Integer>just(10), 
                i-> Eval.<Integer>now(i+5),
                (a,b) -> FutureW.<Integer>ofResult(a+b+10),
                (a,b,c) -> Maybe.<Integer>just(a+b+c+10),
                
                Tuple::tuple).toListX();

        val list2 = Maybe.just(10).flatMap(a-> Maybe.<Integer>just(a+5).flatMap(b-> Maybe.just(a+b+10)
                            .flatMap(c->Maybe.<Integer>just(a+b+c+10)
                            
                            .map(d-> Tuple.<Integer,Integer,Integer>tuple(a,b,c))))).toListX();
                            
        assertThat(list,equalTo(list2));
                    
    }
    @Test
    public void forGenFilter4(){
        
        
        val list = each4(Maybe.<Integer>just(10), 
                        i-> Eval.<Integer>now(i+5),
                        (a,b) -> FutureW.<Integer>ofResult(a+b+10),
                        (a,b,c) -> Maybe.<Integer>just(a+b+c+10),
                        (a,b,c,d)->a+b+c+d<10,
                        Tuple::tuple).toListX();

        val list2 = Maybe.just(10).flatMap(a-> Maybe.<Integer>just(a+5).flatMap(b-> Maybe.just(a+b+10)
                                    .flatMap(c->Maybe.<Integer>just(a+b+c+10)
                                    .filter(d->a+b+c+d<10)
                                    .map(d-> Tuple.<Integer,Integer,Integer>tuple(a,b,c))))).toListX();
                    

                            
assertThat(list,equalTo(list2));
                    
    }

}
