package com.aol.cyclops.matcher;

import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple1;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.jooq.lambda.tuple.Tuple5;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Matchable.MTuple1;
import com.aol.cyclops.control.Matchable.MTuple2;
import com.aol.cyclops.control.Matchable.MTuple3;
import com.aol.cyclops.control.Matchable.MTuple4;

public class MatchableTuple5Test {

    Matchable.MTuple5<String,Integer,Character,Long,String> t;
    @Before
    public void setup(){
        t= Matchable.from( ()->"hello",()->10,()->'c',()->-2l,()->"world");
    }

    @Test
    public void on$1____(){
        assertThat(t.on$1____().getMatchable(),equalTo(Tuple.tuple("hello")));
    }
    @Test
    public void on$_2___(){
        assertThat(t.on$_2___().getMatchable(),equalTo(Tuple.tuple(10)));
    }
    @Test
    public void on$__3__(){
        assertThat(t.on$__3__().getMatchable(),equalTo(Tuple.tuple('c')));
    }
    @Test
    public void on$___4_(){
        assertThat(t.on$___4_().getMatchable(),equalTo(Tuple.tuple(-2l)));
    }
    @Test
    public void on$12___(){
        assertThat(t.on$12___().getMatchable(),equalTo(Tuple.tuple("hello",10)));
    }
    @Test
    public void on$1_3__(){
        assertThat(t.on$1_3__().getMatchable(),equalTo(Tuple.tuple("hello",'c')));
    }
    @Test
    public void on$1__4_(){
        assertThat(t.on$1__4_().getMatchable(),equalTo(Tuple.tuple("hello",-2l)));
    }
    @Test
    public void on$_23__(){
        assertThat(t.on$_23__().getMatchable(),equalTo(Tuple.tuple(10,'c')));
    }
    @Test
    public void on$_2_4_(){
        assertThat(t.on$_2_4_().getMatchable(),equalTo(Tuple.tuple(10,-2l)));
    }
    @Test
    public void on$__34_(){
        assertThat(t.on$__34_().getMatchable(),equalTo(Tuple.tuple('c',-2l)));
    }
    
    @Test
    public void on$123__(){
        assertThat(t.on$123__().getMatchable(),equalTo(Tuple.tuple("hello",10,'c')));
    }
    @Test
    public void on$12_4_(){
        assertThat(t.on$12_4_().getMatchable(),equalTo(Tuple.tuple("hello",10,-2l)));
    }
    @Test
    public void on$1_34_(){
        assertThat(t.on$1_34_().getMatchable(),equalTo(Tuple.tuple("hello",'c',-2l)));
    }

    @Test
    public void on$_234_(){
        assertThat(t.on$_234_().getMatchable(),equalTo(Tuple.tuple(10,'c',-2l)));
    }
    
     @Test
    public void on$____5(){
         assertThat(t.on$____5().getMatchable(),equalTo(Tuple.tuple("world")));
    }
    
     
     @Test
    public void on$1___5(){
         assertThat(t.on$1___5().getMatchable(),equalTo(Tuple.tuple("hello","world")));
    }
    
     @Test
    public void on$_2__5(){
         assertThat(t.on$_2__5().getMatchable(),equalTo(Tuple.tuple(10,"world")));
    }
    
     @Test
    public void on$__3_5(){
         assertThat(t.on$__3_5().getMatchable(),equalTo(Tuple.tuple('c',"world")));
    }
     @Test
    public void on$___45(){
         assertThat(t.on$___45().getMatchable(),equalTo(Tuple.tuple(-2l,"world")));
    }
    
     
    
     @Test
    public void on$12__5(){
         assertThat(t.on$12__5().getMatchable(),equalTo(Tuple.tuple("hello",10,"world")));
    }
    
     @Test
    public void on$1_3_5(){
         assertThat(t.on$1_3_5().getMatchable(),equalTo(Tuple.tuple("hello",'c',"world")));
    }
     @Test
    public void on$1__45(){
         assertThat(t.on$1__45().getMatchable(),equalTo(Tuple.tuple("hello",-2l,"world")));
    }

    
     @Test
    public void on$_23_5(){
         assertThat(t.on$_23_5().getMatchable(),equalTo(Tuple.tuple(10,'c',"world")));
    }
     @Test
    public void on$__345(){
         assertThat(t.on$__345().getMatchable(),equalTo(Tuple.tuple('c',-2l,"world")));
    }
     @Test
    public void on$1234_(){
         assertThat(t.on$1234_().getMatchable(),equalTo(Tuple.tuple("hello",10,'c',-2l)));
    }
     @Test
    public void on$123_5(){
         assertThat(t.on$123_5().getMatchable(),equalTo(Tuple.tuple("hello",10,'c',"world")));
    }
     @Test
    public void on$12_45(){
         assertThat(t.on$123_5().getMatchable(),equalTo(Tuple.tuple("hello",10,'c',"world")));
    }
     @Test
    public void on$1_345(){
         assertThat(t.on$1_345().getMatchable(),equalTo(Tuple.tuple("hello",'c',-2l,"world")));
    }
     @Test
    public void on$_2345(){
         assertThat(t.on$_2345().getMatchable(),equalTo(Tuple.tuple(10,'c',-2l,"world")));
    }
}
