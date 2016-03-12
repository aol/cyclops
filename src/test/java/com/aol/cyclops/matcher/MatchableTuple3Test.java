package com.aol.cyclops.matcher;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple1;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Matchable.MTuple1;
import com.aol.cyclops.control.Matchable.MTuple2;

public class MatchableTuple3Test {

    Matchable.MTuple3<String,Integer,Character> t;
    @Before
    public void setup(){
        t= Matchable.from( ()->"hello",()->10,()->'c');
    }
    
    @Test
    public void on$1__(){
        assertThat(t.on$1__().getMatchable(),equalTo(Tuple.tuple("hello")));
    }
    @Test
    public void on$_2_(){
        assertThat(t.on$_2_().getMatchable(),equalTo(Tuple.tuple(10)));
    }
    @Test
    public void on$__3(){
        assertThat(t.on$__3().getMatchable(),equalTo(Tuple.tuple('c')));
    }
    @Test
    public void on$12_(){
        assertThat(t.on$12_().getMatchable(),equalTo(Tuple.tuple("hello",10)));
    }
    @Test
    public void on$1_3(){
        assertThat(t.on$1_3().getMatchable(),equalTo(Tuple.tuple("hello",'c')));
    }
    @Test
    public void on$_23(){
        assertThat(t.on$_23().getMatchable(),equalTo(Tuple.tuple(10,'c')));
    }
}
