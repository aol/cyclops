package cyclops.control;

import cyclops.companion.Monoids;
import cyclops.collections.tuple.Tuple;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;


public class ReaderWriterStateTest {

    @Test
    public void map(){
        ReaderWriterState<String,Integer,String,Integer> rws = ReaderWriterState.rws((r,s)->Tuple.tuple(10,s,1), Monoids.intMax);
        ReaderWriterState<String, Integer, String, Integer> mapped = rws.map(i -> i * 2);

        assertThat(mapped.run("hello","world"),equalTo(Tuple.tuple(10, "world", 2)));
    }

}