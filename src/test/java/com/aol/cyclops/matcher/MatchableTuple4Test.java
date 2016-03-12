package com.aol.cyclops.matcher;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple1;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Matchable.MTuple1;
import com.aol.cyclops.control.Matchable.MTuple2;
import com.aol.cyclops.control.Matchable.MTuple3;

public class MatchableTuple4Test {

    Matchable.MTuple4<String,Integer,Character,Long> t;
    @Before
    public void setup(){
        t= Matchable.from( ()->"hello",()->10,()->'c',()->-2l);
    }

    @Test
    public void on$1___(){
        assertThat(t.on$1___().getMatchable(),equalTo(Tuple.tuple("hello")));
    }
    @Test
    public void on$_2__(){
        assertThat(t.on$_2__().getMatchable(),equalTo(Tuple.tuple(10)));
    }
    @Test
    public void on$__3_(){
        assertThat(t.on$__3_().getMatchable(),equalTo(Tuple.tuple('c')));
    }
    @Test
    public void on$___4(){
        assertThat(t.on$___4().getMatchable(),equalTo(Tuple.tuple(-2l)));
    }
    @Test
    public void on$12__(){
        assertThat(t.on$12__().getMatchable(),equalTo(Tuple.tuple("hello",10)));
    }
    @Test
    public void on$1_3_(){
        assertThat(t.on$1_3_().getMatchable(),equalTo(Tuple.tuple("hello",'c')));
    }
    @Test
    public void on$1__4(){
        assertThat(t.on$1__4().getMatchable(),equalTo(Tuple.tuple("hello",-2l)));
    }
    @Test
    public void on$_23_(){
        assertThat(t.on$_23_().getMatchable(),equalTo(Tuple.tuple(10,'c')));
    }
    @Test
    public void on$_2_4(){
        assertThat(t.on$_2_4().getMatchable(),equalTo(Tuple.tuple(10,-2l)));
    }
    @Test
    public void on$__34(){
        assertThat(t.on$__34().getMatchable(),equalTo(Tuple.tuple('c',-2l)));
    }
    
    @Test
    public void on$123_(){
        assertThat(t.on$123_().getMatchable(),equalTo(Tuple.tuple("hello",10,'c')));
    }
    @Test
    public void on$12_4(){
        assertThat(t.on$12_4().getMatchable(),equalTo(Tuple.tuple("hello",10,-2l)));
    }
    @Test
    public void on$1_34(){
        assertThat(t.on$1_34().getMatchable(),equalTo(Tuple.tuple("hello",'c',-2l)));
    }

    @Test
    public void on$_234(){
        assertThat(t.on$_234().getMatchable(),equalTo(Tuple.tuple(10,'c',-2l)));
    }
}
