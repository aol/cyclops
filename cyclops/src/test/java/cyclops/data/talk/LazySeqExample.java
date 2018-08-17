package cyclops.data.talk;

import cyclops.data.LazySeq;
import org.junit.Test;

public class LazySeqExample {

    String b = "!";
    @Test
    public void lazy(){

        LazySeq.of("hello","world")
               .map(a->b=a);

        System.out.println(b);
    }
}
