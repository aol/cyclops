package com.aol.cyclops2.control.transformers;


import com.aol.cyclops2.types.mixins.Printable;
import cyclops.companion.Reducers;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import com.aol.cyclops2.util.box.Mutable;
import cyclops.collectionx.mutable.ListX;
import cyclops.collectionx.immutable.LinkedListX;
import cyclops.control.*;
import cyclops.control.lazy.Eval;
import cyclops.control.lazy.Trampoline;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.transformers.EvalT;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;


public class EvalTTest implements Printable {

	EvalT<Witness.optional,Integer> just;
	EvalT<Witness.optional,Integer> none;
	EvalT<Witness.optional,Integer> one;
	@Before
	public void setUp() throws Exception {


		just = Eval.now(10).liftM(Witness.optional.INSTANCE);
		none = EvalT.of(AnyM.ofNullable(null));
		one = EvalT.of(AnyM.ofNullable(Eval.now(1)));
	}
	
	@Test
	public void optionalVEval(){


	    Optional.of(10)
	            .map(i->print("optional " + (i+10)));
	            
	    Eval.now(10)
	         .map(i->print("Eval " + (i+10)));
	    
	}
	


	private int add1(int i){
		return i+1;
	}

	@Test
	public void testOfT() {
		assertThat(Eval.now(1),equalTo(Eval.now(1)));
	}

	

	@Test
	public void testOfNullable() {
		assertFalse(Eval.now(null).isPresent());
		assertThat(Eval.now(1),equalTo(Eval.now(1)));
		
	}

	@Test
	public void testNarrow() {
		assertThat(Eval.now(1),equalTo(Eval.narrow(Eval.now(1))));
	}

	

	@Test
	public void testUnitT() {
		assertThat(just.unit(20).orElse(-1),equalTo(20));
	}

	



	@Test
	public void testMapFunctionOfQsuperTQextendsR() {
		assertThat(just.map(i->i+5).orElse(-1),equalTo(15));
		assertThat(none.map(i->i+5).orElse(1000),equalTo(1000));
	}

	@Test
	public void testFlatMap() {
	    
		assertThat(just.flatMap(i->Eval.now(i+5)).orElse(-1),equalTo(15));
		assertThat(none.flatMap(i->Eval.now(i+5)).orElse(-1),equalTo(-1));
	}
	
	@Test
	public void testWhenFunctionOfQsuperTQextendsRSupplierOfQextendsR() {

		assertThat(just.visit(i->i+1,()->20),equalTo(AnyM.ofNullable(11)));
		assertThat(none.visit(i->i+1,()->20),equalTo(AnyM.ofNullable(null)));
	}



	@Test
	public void testStream() {
		assertThat(just.stream().toListX(),equalTo(ListX.of(10)));
		assertThat(none.stream().toListX(),equalTo(ListX.of()));
	}

	@Test
	public void testOfSupplierOfT() {
		
	}

	@Test
    public void testConvertTo() {
        AnyM<Witness.optional,Stream<Integer>> toStream = just.visit(m->Stream.of(m),()->Stream.of());

        assertThat(toStream.stream().flatMap(i->i).collect(Collectors.toList()),equalTo(ListX.of(10)));
    }




	@Test
	public void testIterate() {
		assertThat(just.iterate(i->i+1,-1000).to(Witness::optional).get().limit(10).sumInt(i->(int)i),equalTo(145));
	}

	@Test
	public void testGenerate() {
		assertThat(just.generate(-100).to(Witness::optional).get().limit(10).sumInt(i->i),equalTo(100));
	}

	@Test
	public void testMapReduceReducerOfE() {
		assertThat(just.mapReduce(Reducers.toCountInt()),equalTo(1));
	}

	@Test
	public void testFoldMonoidOfT() {

		assertThat(just.foldLeft(Reducers.toTotalInt()),equalTo(10));
	}

	@Test
	public void testFoldTBinaryOperatorOfT() {
		assertThat(just.foldLeft(1, (a,b)->a*b),equalTo(10));
	}





	@Test
	public void testMkString() {
		assertThat(just.mkString(),equalTo("EvalT[Optional[Always[10]]]"));
		assertThat(none.mkString(),equalTo("EvalT[Optional.empty]"));
	}


	@Test
	public void testGet() {
		assertThat(just.get(),equalTo(Option.some(10)));
	}

	@Test
	public void testFilter() {

		assertFalse(just.filter(i->i<5).isPresent());
		assertTrue(just.filter(i->i>5).isPresent());
		assertFalse(none.filter(i->i<5).isPresent());
		assertFalse(none.filter(i->i>5).isPresent());
		
	}

	@Test
	public void testOfType() {
		assertFalse(just.ofType(String.class).isPresent());
		assertTrue(just.ofType(Integer.class).isPresent());
		assertFalse(none.ofType(String.class).isPresent());
		assertFalse(none.ofType(Integer.class).isPresent());
	}

	@Test
	public void testFilterNot() {

		assertTrue(just.filterNot(i->i<5).isPresent());
		assertFalse(just.filterNot(i->i>5).isPresent());
		assertFalse(none.filterNot(i->i<5).isPresent());
		assertFalse(none.filterNot(i->i>5).isPresent());
	}

	@Test
	public void testNotNull() {
		assertTrue(just.notNull().isPresent());
		assertFalse(none.notNull().isPresent());
		
	}

	



	private int add(int a, int b){
		return a+b;
	}


	private int add3(int a, int b, int c){
		return a+b+c;
	}

	private int add4(int a, int b, int c,int d){
		return a+b+c+d;
	}

	private int add5(int a, int b, int c,int d,int e){
		return a+b+c+d+e;
	}

	

	@Test
	public void testMapReduceReducerOfR() {
		assertThat(just.mapReduce(Reducers.toLinkedListX()),equalTo(LinkedListX.of(10)));
	}

	@Test
	public void testMapReduceFunctionOfQsuperTQextendsRMonoidOfR() {
		assertThat(just.mapReduce(s->s.toString(), Monoid.of("",Semigroups.stringJoin(","))),equalTo(",10"));
	}

	@Test
	public void testReduceMonoidOfT() {
		assertThat(just.reduce(Monoid.of(1,Semigroups.intMult)),equalTo(10));
	}

	@Test
	public void testReduceBinaryOperatorOfT() {
		assertThat(just.reduce((a,b)->a+b),equalTo(Optional.of(10)));
	}

	@Test
	public void testReduceTBinaryOperatorOfT() {
		assertThat(just.reduce(10,(a,b)->a+b),equalTo(20));
	}

	@Test
	public void testReduceUBiFunctionOfUQsuperTUBinaryOperatorOfU() {
		assertThat(just.reduce(11,(a,b)->a+b,(a,b)->a*b),equalTo(21));
	}

	@Test
	public void testReduceStreamOfQextendsMonoidOfT() {
		ListX<Integer> countAndTotal = just.reduce(Stream.of(Reducers.toCountInt(),Reducers.toTotalInt()));
		assertThat(countAndTotal,equalTo(ListX.of(1,10)));
	}

	@Test
	public void testReduceIterableOfReducerOfT() {
		ListX<Integer> countAndTotal = just.reduce(Arrays.asList(Reducers.toCountInt(),Reducers.toTotalInt()));
		assertThat(countAndTotal,equalTo(ListX.of(1,10)));
	}

	

	@Test
	public void testFoldRightMonoidOfT() {
		assertThat(just.foldRight(Monoid.of(1,Semigroups.intMult)),equalTo(10));
	}

	@Test
	public void testFoldRightTBinaryOperatorOfT() {
		assertThat(just.foldRight(10,(a,b)->a+b),equalTo(20));
	}

	@Test
	public void testFoldRightMapToType() {
		assertThat(just.foldRightMapToType(Reducers.toLinkedListX()),equalTo(LinkedListX.of(10)));
	}

	
	
	@Test
	public void testWhenFunctionOfQsuperEvalOfTQextendsR() {
	    
	    
	    String match = Eval.now("data is present")
	                        .visit(present->"hello", ()->"missing");
	    
	    
	    
		assertThat(just.visit(s->"hello", ()->"world"),equalTo(AnyM.ofNullable("hello")));
		//none remains none as visit is on the Future not the Optional
		assertThat(none.visit(s->"hello", ()->"world"),equalTo(AnyM.ofNullable(null)));
	}

	
	@Test
	public void testOrElseGet() {
		assertThat(none.orElseGet(()->2),equalTo(2));
		assertThat(just.orElseGet(()->2),equalTo(10));
	}



	@Test
	public void testOrElse() {
		assertThat(none.orElse(20),equalTo(20));
		assertThat(just.orElse(20),equalTo(10));
	}

	@Test(expected=RuntimeException.class)
	public void testOrElseThrow() {
		none.orElseThrow(()->new RuntimeException());
	}
	@Test
	public void testOrElseThrowSome() {
		
		assertThat(just.orElseThrow(()->new RuntimeException()),equalTo(10));
	}



	



	@Test
	public void testIterator1() {
		assertThat(Streams.stream(just.iterator()).collect(Collectors.toList()),
				equalTo(Arrays.asList(10)));
	}

	@Test
	public void testForEach() {
		Mutable<Integer> capture = Mutable.of(null);
		 none.forEach(c->capture.set(c));
		assertNull(capture.get());
		just.forEach(c->capture.set(c));
		assertThat(capture.get(),equalTo(10));
	}

	@Test
	public void testSpliterator() {
		assertThat(StreamSupport.stream(just.spliterator(),false).collect(Collectors.toList()),
				equalTo(Arrays.asList(10)));
	}

	

	@Test
	public void testMapFunctionOfQsuperTQextendsR1() {
		assertThat(just.map(i->i+5).orElse(-1),equalTo(15));
	}
	
	@Test
	public void testPeek() {
		Mutable<Integer> capture = Mutable.of(null);
		just = just.peek(c->capture.set(c)).map(i->i+2);
		
		
		
		just.orElse(-1);
		assertThat(capture.get(),equalTo(10));
	}

	private Trampoline<Integer> sum(int times, int sum){
		return times ==0 ?  Trampoline.done(sum) : Trampoline.more(()->sum(times-1,sum+times));
	}
	@Test
	public void testTrampoline() {
		assertThat(just.trampoline(n ->sum(10,n)).orElse(-1),equalTo(65));
	}

	


}
