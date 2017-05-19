package com.aol.cyclops2.functions.collections.extensions.standard.anyM;

import com.aol.cyclops2.functions.collections.extensions.AbstractAnyMSeqOrderedDependentTest;
import com.aol.cyclops2.types.anyM.AnyMSeq;
import cyclops.collections.mutable.ListX;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ReactiveStreamXTest extends AbstractAnyMSeqOrderedDependentTest<Witness.reactiveSeq>{
    int count =0;

    @Test
    public void materialize(){
        ListX<Integer> d= of(1, 2, 3).cycleUntil(next->count++==6).toListX();
        System.out.println("D " + d);
        count =0;
        assertEquals(asList(1, 2,3, 1, 2,3),of(1, 2, 3).cycleUntil(next->count++==6).toListX());
    }
    @Test
    public void testCycleUntil() {
        count =0;
        ReactiveSeq<Integer> stream1 = Spouts.of(1,2,3);
        ReactiveSeq<Integer> stream2 = AnyM.fromStream(stream1).unwrap();
        assertTrue(stream1== stream2);
        System.out.println("Stream2 cycling " + stream2.cycleUntil(next->count++==6).toListX().materialize());
        count=0;
        System.out.println("Cycle until!");
        ListX<Integer> a =Spouts.of(1,2,3).cycleUntil(next->count++==6).toListX().materialize();
        count=0;
        ListX<Integer> b= Witness.reactiveSeq(of(1, 2, 3)).cycleUntil(next->count++==6).toListX();
        count=0;
        ListX<Integer> c= Witness.reactiveSeq(of(1, 2, 3).cycleUntil(next->count++==6)).toListX();
        count=0;
        ListX<Integer> d= of(1, 2, 3).cycleUntil(next->count++==6).toListX();
        System.out.println("A " + a);
        count=0;
        System.out.println("B " + b);
        count=0;
        System.out.println("C " + c);
        count=0;
        System.out.println("D " + d);
        count=0;

        System.out.println("Cycle"  +Spouts.of(1,2,3).cycleUntil(next->count++==6).toListX());
        System.out.println("Print!");
        //  of(1, 2, 3).cycleUntil(next->count++==6).printOut();
        count=0;
        assertEquals(asList(1, 2,3, 1, 2,3),of(1, 2, 3).cycleUntil(next->count++==6).toListX());

    }
	@Override
	public <T> AnyMSeq<Witness.reactiveSeq,T> of(T... values) {
		return AnyM.fromStream(Spouts.of(values));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops2.function.collections.extensions.AbstractCollectionXTest#empty()
	 */
	@Override
	public <T> AnyMSeq<Witness.reactiveSeq,T> empty() {
		return AnyM.fromStream(Spouts.empty());
	}
	@Test
    public void when(){
        
        String res= AnyM.fromStream(ReactiveSeq.of(1,2,3)).visit((x,xs)->
                                xs.join(x>2? "hello" : "world"),()->"boo!");
                    
        assertThat(res,equalTo("2world3"));
    }
	@Test
    public void whenGreaterThan2(){
        System.out.println("Value = "+Spouts.of(5,2,3).visit((x,xs)->{
            System.out.println("X is " + x);
            System.out.println("XS " + xs.toList());
                  return   xs.join(x>2? "hello" : "world");
        },()->"boo!") );
        String res= of(5,2,3).visit((x,xs)->
                                xs.join(x>2? "hello" : "world"),()->"boo!");
                
        assertThat(res,equalTo("2hello3"));
    }
    @Test
    public void when2(){
        
        Integer res =   of(1,2,3).visit((x,xs)->x,()->10);
        System.out.println(res);
    }
    @Test
    public void whenNilOrNot(){
        String res1=    of(1,2,3).visit((x,xs)-> x>2? "hello" : "world",()->"EMPTY");
    }
    @Test
    public void whenNilOrNotJoinWithFirstElement(){
        
        
        String res= of(1,2,3).visit((x,xs)-> xs.join(x>2? "hello" : "world"),()->"EMPTY");
        assertThat(res,equalTo("2world3"));
    }
	
	

}

