package cyclops.control;

import com.sun.tools.javah.Gen;
import cyclops.function.FluentFunctions;
import cyclops.function.Fn1;
import cyclops.stream.ReactiveSeq;
import org.junit.Before;
import org.junit.Test;
import org.testng.annotations.TestInstance;

import static cyclops.control.Generator.*;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 31/05/2017.
 */
public class GeneratorTest {

    @Before
    public void setup(){
        i=0;
    }
    @Test
    public void streamFn(){
        ReactiveSeq.generate(suspend((Integer i)->i<4,s2->s2.yield(i++)))
                .printOut();

    }
    @Test
    public void stream(){
        ReactiveSeq.generate(suspend(times(2),s2->s2.yield(i++)))
                    .printOut();

    }
    @Test
    public void streamGen(){
        Generator<Integer> gen = suspend(times(2),s2->s2.yield(i++));
        gen.stream().printOut();
    }
    int i = 0;
    @Test
    public void nested(){
        i = 100;
        Fn1 f;
        FluentFunctions.of(i->i);
        ReactiveSeq.generate(suspend((Integer i)->i!=4, s-> {
                    System.out.println("Top level - repeat 2 times after sequence!");
                    Generator<Integer> gen = suspend(times(2)
                                                      .before(()->i=i*2),
                                                      s2->s2.yield(i++));
                    return s.yieldAll(1,
                                        gen.stream());
                }
        )).take(12)
                .printOut();
    }
    int k = 9999;
    @Test
    public void nestedMulti(){
        i = 100;
        k=9999;

        ReactiveSeq.generate(suspend((Integer i)->i!=4, s-> {


                    Generator<Integer> gen1 = suspend(times(2),
                            s2->s2.yield(i++));
                    Generator<Integer> gen2 = suspend(times(2),
                                s2->s2.yield(k--));

                    return s.yieldAll(gen1.stream(),
                            gen2.stream());
                }
        )).take(12)
                .printOut();
    }


}