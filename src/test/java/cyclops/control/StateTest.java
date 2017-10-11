package cyclops.control;

import cyclops.data.tuple.Tuple;
import cyclops.control.lazy.State;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 04/02/2017.
 */
public class StateTest {

    @Test
    public void eval(){
        State<String,Integer> state = State.state(s-> Tuple.tuple(s,10));
        assertThat(state.eval("hello"),equalTo(10));
    }
    @Test
    public void run(){
        State<String,Integer> state = State.state(s-> Tuple.tuple(s,10));
        assertThat(state.run("hello"),equalTo(Tuple.tuple("hello",10)));
    }

    @Test
    public void map(){
        State<String,Integer> state = State.<String,Integer>state(s-> Tuple.tuple(s,10))
                                            .map(i->i*2);
        assertThat(state.run("hello"),equalTo(Tuple.tuple("hello",20)));
    }
    @Test
    public void flatMap(){
        State<String,Integer> state = State.<String,Integer>state(s-> Tuple.tuple(s,10))
                                            .flatMap(i->State.state(s->Tuple.tuple(s,i*2)));
        assertThat(state.run("hello"),equalTo(Tuple.tuple("hello",20)));
    }



}