package cyclops.control;

import cyclops.collectionx.mutable.ListX;
import cyclops.reactive.Generator;
import cyclops.reactive.ReactiveSeq;
import org.junit.Before;
import org.junit.Test;

import static cyclops.reactive.Generator.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * Created by johnmcclean on 31/05/2017.
 */
public class GeneratorTest {

    @Before
    public void setup(){
        i=0;
    }
    public Integer next(){
        return i++;
    }

    @Test
    public void suspendLoop(){
        assertThat(ReactiveSeq.fromIterable(suspend(times(100),s->i<5 ? s.yield(i++) : s.yieldAndStop(i)))
                .toListX(),equalTo(ListX.of(0,1,2,3,4,5)));
    }
    @Test
    public void methodRefYield(){
        assertThat(ReactiveSeq.fromIterable(suspend(s-> {
                    System.out.println("Top level - should see this only once!");
                    return s.yieldRef(1,
                            this::next,
                            this::next,
                            this::next,
                            this::next);
                }
        )).take(3).toListX(),equalTo(ListX.of(1,0,1)));
    }

    @Test
    public void sequenceWithFunction(){
        assertThat(ReactiveSeq.fromIterable(suspend((Integer i)->i!=4,s-> {
                    System.out.println("Top level - repeat 2 times after sequence!");
                    return s.yield(1,
                            () -> s.yield(2),
                            () -> s.yield(3),
                            () -> s.yield(4));
                }
        )).take(12).toListX(),equalTo(ListX.of(1,2,3,4)));
    }
    @Test
    public void sequenceWithFunctionWithCurrent(){
        assertThat(ReactiveSeq.fromIterable(suspend((Integer i)->i!=4,s-> {
                    System.out.println("Top level - repeat 2 times after sequence!");
                    return s.yield(1,
                            () -> s.yield(s.current()+2),
                            () -> s.yield(3),
                            () -> s.yield(4));
                }
        )).take(12).toListX(),equalTo(ListX.of(1,3,3,4)));
    }
    @Test
    public void sequenceWithMaybe(){
        assertThat(ReactiveSeq.generate(suspend((Integer i)->i!=4,s-> {
                    System.out.println("Top level - repeat 2 times after sequence!");
                    return s.yield(1,
                            () -> s.yield(s.maybe()
                                    .map(o->o+5)
                                    .orElse(10)),
                            () -> s.yield(3),
                            () -> s.yield(4));
                }
        )).take(120).toListX(),equalTo(ListX.of(1,6,3,4)));
    }
    @Test
    public void suspendRefTest(){
        Generator<Integer> generator = suspendRef(times(10),this::next);

        assertThat(generator.stream().toListX(),equalTo(ListX.of(0,1,2,3,4,5,6,7,8,9)));

    }
    @Test
    public void streamFn(){
        assertThat(ReactiveSeq.generate(suspend((Integer i)->i<4,s2->s2.yield(i++))).toListX(),equalTo(ListX.of(0,1,2,3,4)));

    }
    @Test
    public void stream(){
        assertThat( ReactiveSeq.generate(suspend(times(2),s2->s2.yield(i++)))
                .toListX(),equalTo(ListX.of(0,1)));

    }
    @Test
    public void streamGen(){
        Generator<Integer> gen = suspend(times(2),s2->s2.yield(i++));
        assertThat(gen.stream().toListX(),equalTo(ListX.of(0,1)));
    }
    int i = 0;
    @Test
    public void nested(){
        i = 100;

        assertThat(ReactiveSeq.generate(suspend((Integer i)->i!=4, s-> {
                    System.out.println("Top level - repeat 2 times after sequence!");
                    Generator<Integer> gen = suspend(times(2)
                                                      .before(()->i=i*2),
                                                      s2->s2.yield(i++));
                    return s.yieldAll(1,
                                        gen.stream());
                }
        )).take(6).toListX(),equalTo(ListX.of(1, 100, 101, 1, 204, 205)));

    }
    int k = 9999;
    @Test
    public void nestedMulti(){
        i = 100;
        k=9999;

        assertThat(ReactiveSeq.generate(suspend((Integer i)->i!=4, s-> {


                    Generator<Integer> gen1 = suspend(times(2),
                            s2->s2.yield(i++));
                    Generator<Integer> gen2 = suspend(times(2),
                                s2->s2.yield(k--));

                    return s.yieldAll(gen1.stream(),
                            gen2.stream());
                }
        )).take(5).toListX(),equalTo(ListX.of(100, 101, 9999, 9998, 102)));

    }

    @Test
    public void nestedInPeek(){
        assertThat(ReactiveSeq.fromIterable(suspend(times(10),s->s.yield(i++)))
                .peek(i-> {
                            ReactiveSeq.fromIterable(suspend(s-> {
                                        System.out.println("Top level - should see this only once!");
                                        return s.yield(1,
                                                () -> s.yield(2),
                                                () -> s.yield(3),
                                                () -> s.yield(4));
                                    }
                            )).take(6)
                                    .printOut();
                        }
                )
                .take(3)
                .toListX(),equalTo(ListX.of(0,1,2)));
    }

    @Test
    public void innerClass(){
        assertThat(ReactiveSeq.<Integer>fromIterable(suspend(new GeneratorFunction<Integer>() {
                                                      int runningTotal =0;

                                                      @Override
                                                      public Generator<Integer> apply(Suspended<Integer> s) {
                                                          System.out.println("Top level - should see this only once!");
                                                          return s.yield(1,
                                                                  () -> {
                                                                      runningTotal = runningTotal +5;
                                                                      return s.yield(runningTotal+2);
                                                                  },
                                                                  () -> s.yield(runningTotal+3),
                                                                  () -> s.yieldAndStop(runningTotal+6));

                                                      }
                                                  }

        )).take(6).toListX(),equalTo(ListX.of(1,7,8,11)));
    }

}