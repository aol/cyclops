package cyclops.monads.collections.mutable;


import com.oath.cyclops.anym.AnyMSeq;
import com.oath.cyclops.ReactiveConvertableSequence;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.reactiveSeq;
import cyclops.monads.collections.AbstractAnyMSeqOrderedDependentTest;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.junit.Test;

import java.util.concurrent.ForkJoinPool;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class ReactiveStreamXTest extends AbstractAnyMSeqOrderedDependentTest<reactiveSeq> {
    int count =0;

    boolean complete = false;
    @Test
    public void asyncTest(){
        complete = false;
        System.out.println("Start");
        AnyM.fromStream(Spouts.async(ReactiveSeq.of(1,2,3,4,5),ForkJoinPool.commonPool()))
                      .forEach(System.out::println,System.err::println,()->complete=true);
        System.out.println("Set up");
        assertFalse(complete);
        while(!complete){
            Thread.yield();
        }
        assertTrue(complete);

    }

    @Test
    public void asyncReactiveTest(){
        complete = false;
        System.out.println("Start");
        Spouts.reactive(ReactiveSeq.of(1,2,3,4,5),ForkJoinPool.commonPool())
                .forEach(System.out::println,System.err::println,()->complete=true);

        System.out.println("Set up");
        assertFalse(complete);
        while(!complete){
            Thread.yield();
        }
        assertTrue(complete);

    }
    @Test
    public void asyncReactiveTestAnyM(){
        complete = false;
        System.out.println("Start");


         AnyM.fromStream(Spouts.reactive(ReactiveSeq.of(1,2,3,4,5),ForkJoinPool.commonPool()))
                 .map(i->i*2)
                 .sliding(1)
                 .forEach(System.out::println,System.err::println,()->complete=true);

        System.out.println("Set up");
        assertFalse(complete);
        while(!complete){
            Thread.yield();
        }
        assertTrue(complete);

    }
    @Test
    public void materialize(){
        ListX<Integer> d= of(1, 2, 3).cycleUntil(next->count++==6).to(ReactiveConvertableSequence::converter).listX();
        System.out.println("D " + d);
        count =0;
        assertEquals(asList(1, 2,3, 1, 2,3),of(1, 2, 3).cycleUntil(next->count++==6).to(ReactiveConvertableSequence::converter).listX());
    }
    @Test
    public void testCycleUntil() {
        count =0;
        ReactiveSeq<Integer> stream1 = Spouts.of(1,2,3);
        ReactiveSeq<Integer> stream2 = AnyM.fromStream(stream1).unwrap();
        assertTrue(stream1== stream2);
        System.out.println("Stream2 cycling " + stream2.cycleUntil(next->count++==6).to(ReactiveConvertableSequence::converter).listX().materialize());
        count=0;
        System.out.println("Cycle until!");
        ListX<Integer> a =Spouts.of(1,2,3).cycleUntil(next->count++==6).to(ReactiveConvertableSequence::converter).listX().materialize();
        count=0;
        ListX<Integer> b= Witness.reactiveSeq(of(1, 2, 3)).cycleUntil(next->count++==6).to(ReactiveConvertableSequence::converter).listX();
        count=0;
        ListX<Integer> c= Witness.reactiveSeq(of(1, 2, 3).cycleUntil(next->count++==6)).to(ReactiveConvertableSequence::converter).listX();
        count=0;
        ListX<Integer> d= of(1, 2, 3).cycleUntil(next->count++==6).to(ReactiveConvertableSequence::converter).listX();
        System.out.println("A " + a);
        count=0;
        System.out.println("B " + b);
        count=0;
        System.out.println("C " + c);
        count=0;
        System.out.println("D " + d);
        count=0;

        System.out.println("Cycle"  +Spouts.of(1,2,3).cycleUntil(next->count++==6).to(ReactiveConvertableSequence::converter).listX());
        System.out.println("Print!");
        //  of(1, 2, 3).cycleUntil(next->count++==6).printOut();
        count=0;
        assertEquals(asList(1, 2,3, 1, 2,3),of(1, 2, 3).cycleUntil(next->count++==6).to(ReactiveConvertableSequence::converter).listX());

    }
	@Override
	public <T> AnyMSeq<reactiveSeq,T> of(T... values) {
		return AnyM.fromStream(Spouts.of(values));
	}

	@Override
	public <T> AnyMSeq<reactiveSeq,T> empty() {
		return AnyM.fromStream(Spouts.empty());
	}

    public static class BooleanProxy {
        public boolean value;

        public BooleanProxy(boolean b) {
            value = b;
        }
    }

    @Test
    public void testOnComplete() {
        BooleanProxy completed = new BooleanProxy(false);

        ReactiveSeq.ofInts(1, 2, 3, 4).onComplete(() -> {
            completed.value = true;
        }).forEach(x -> {
            assertFalse(completed.value);
        } );

        assertTrue(completed.value);
    }
}

