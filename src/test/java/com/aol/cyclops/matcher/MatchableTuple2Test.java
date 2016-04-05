package com.aol.cyclops.matcher;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.jooq.lambda.tuple.Tuple;
import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.control.Matchable;

public class MatchableTuple2Test {

    Matchable.MTuple2<String,Integer> t2;
    @Before
    public void setup(){
        t2= Matchables.supplier2( ()->"hello",()->10);
    }
    @Test
    public void on1_(){
        assertThat(t2.on$1_().getMatchable(),equalTo(Tuple.tuple("hello")));
    }
    @Test
    public void on_2(){
        assertThat(t2.on$_2().getMatchable(),equalTo(Tuple.tuple(10)));
    }
}
