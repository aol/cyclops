package com.aol.cyclops2.control;

import cyclops.collections.box.Mutable;
import cyclops.collections.immutable.PersistentSetX;
import cyclops.async.LazyReact;
import cyclops.collections.mutable.ListX;
import cyclops.async.Future;
import cyclops.companion.Monoids;
import cyclops.companion.Reducers;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.control.*;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.function.Monoid;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;



public class Xor2Test {

	Xor<String,Integer> just;
	Xor<String,Integer> none;
	@Before
	public void setUp() throws Exception {
		just = Xor.primary(10);
		none = Xor.secondary("none");
	}

	@Test
    public void testZip(){
        assertThat(Xor.primary(10).zip(Eval.now(20),(a, b)->a+b).get(),equalTo(30));
        assertThat(Xor.primary(10).zipP(Eval.now(20),(a,b)->a+b).get(),equalTo(30));
        assertThat(Xor.primary(10).zipS(Stream.of(20),(a,b)->a+b).get(),equalTo(30));
        assertThat(Xor.primary(10).zip(Seq.of(20),(a,b)->a+b).get(),equalTo(30));
        assertThat(Xor.primary(10).zip(Seq.of(20)).get(),equalTo(Tuple.tuple(10,20)));
        assertThat(Xor.primary(10).zipS(Stream.of(20)).get(),equalTo(Tuple.tuple(10,20)));
        assertThat(Xor.primary(10).zip(Eval.now(20)).get(),equalTo(Tuple.tuple(10,20)));
    }
   
	@Test
    public void testSequenceSecondary() {
        Xor<ListX<Integer>,ListX<String>> xors =Xor.sequenceSecondary(ListX.of(just,none,Xor.primary(1)));
        assertThat(xors,equalTo(Xor.primary(ListX.of("none"))));
    }

    @Test
    public void testAccumulateSecondary2() {
        Xor<?,PersistentSetX<String>> xors = Xor.accumulateSecondary(ListX.of(just,none,Xor.primary(1)),Reducers.<String>toPersistentSetX());
        assertThat(xors,equalTo(Xor.primary(PersistentSetX.of("none"))));
    }

    @Test
    public void testAccumulateSecondarySemigroup() {
        Xor<?,String> xors = Xor.accumulateSecondary(ListX.of(just,none,Xor.secondary("1")),i->""+i, Monoids.stringConcat);
        assertThat(xors,equalTo(Xor.primary("none1")));
    }
    @Test
    public void testAccumulateSecondarySemigroupIntSum() {
        Ior<?,Integer> iors = Ior.accumulateSecondary(Monoids.intSum,ListX.of(Ior.both(2, "boo!"),Ior.secondary(1)));
        assertThat(iors,equalTo(Ior.primary(3)));
    }

	@Test
    public void nest(){
       assertThat(just.nest().map(m->m.get()),equalTo(just));
       assertThat(none.nest().map(m->m.get()),equalTo(none));
    }
    @Test
    public void coFlatMap(){
        assertThat(just.coflatMap(m-> m.isPresent()? m.get() : 50),equalTo(just));
        assertThat(none.coflatMap(m-> m.isPresent()? m.get() : 50),equalTo(Xor.primary(50)));
    }
    @Test
    public void combine(){
        Monoid<Integer> add = Monoid.of(0,Semigroups.intSum);
        assertThat(just.combineEager(add,none),equalTo(Xor.primary(10)));
        assertThat(none.combineEager(add,just),equalTo(Xor.primary(0))); 
        assertThat(none.combineEager(add,none),equalTo(Xor.primary(0))); 
        assertThat(just.combineEager(add,Xor.primary(10)),equalTo(Xor.primary(20)));
        Monoid<Integer> firstNonNull = Monoid.of(null , Semigroups.firstNonNull());
        assertThat(just.combineEager(firstNonNull,Xor.primary(null)),equalTo(just));
         
    }
	@Test
	public void visit(){
	    
	    assertThat(just.visit(secondary->"no", primary->"yes"),equalTo("yes"));
	    assertThat(none.visit(secondary->"no", primary->"yes"),equalTo("no"));
	}
	@Test
    public void visitXor(){
        assertThat(just.bimap(secondary->"no", primary->"yes"),equalTo(Xor.primary("yes")));
        assertThat(none.bimap(secondary->"no", primary->"yes"),equalTo(Xor.secondary("no")));
    }
	@Test
	public void testToMaybe() {
		assertThat(just.toMaybe(),equalTo(Maybe.of(10)));
		assertThat(none.toMaybe(),equalTo(Maybe.none()));
	}

	private int add1(int i){
		return i+1;
	}

	@Test
	public void testOfT() {
		assertThat(Ior.primary(1),equalTo(Ior.primary(1)));
	}

	

	@Test
	public void testSequence() {
		Xor.sequencePrimary(ListX.of(just,Xor.primary(1))).printOut();
		Xor<ListX<String>,ListX<Integer>> maybes =Xor.sequencePrimary(ListX.of(just,none,Xor.primary(1)));
		assertThat(maybes,equalTo(Xor.primary(ListX.of(10,1))));
	}

	@Test
	public void testAccumulateJustCollectionXOfMaybeOfTReducerOfR() {
		Xor<?,PersistentSetX<Integer>> maybes =Xor.accumulatePrimary(ListX.of(just,none,Xor.primary(1)),Reducers.toPersistentSetX());
		assertThat(maybes,equalTo(Xor.primary(PersistentSetX.of(10,1))));
	}

	@Test
	public void testAccumulateJustCollectionXOfMaybeOfTFunctionOfQsuperTRSemigroupOfR() {
		Xor<?,String> maybes = Xor.accumulatePrimary(ListX.of(just,none,Xor.primary(1)),i->""+i,Monoids.stringConcat);
		assertThat(maybes,equalTo(Xor.primary("101")));
	}
	@Test
	public void testAccumulateJust() {
		Xor<?,Integer> maybes =Xor.accumulatePrimary(Monoids.intSum,ListX.of(just,none,Xor.primary(1)));
		assertThat(maybes,equalTo(Xor.primary(11)));
	}
	@Test
    public void testAccumulateSecondary() {
        Xor<?,String> maybes =Xor.accumulateSecondary(Monoids.stringConcat,ListX.of(just,none,Xor.secondary("hello")));
        assertThat(maybes,equalTo(Xor.primary("nonehello")));
    }

	@Test
	public void testUnitT() {
		assertThat(just.unit(20),equalTo(Xor.primary(20)));
	}

	

	@Test
	public void testisPrimary() {
		assertTrue(just.isPrimary());
		assertFalse(none.isPrimary());
	}

	
	@Test
	public void testMapFunctionOfQsuperTQextendsR() {
		assertThat(just.map(i->i+5),equalTo(Xor.primary(15)));
		assertThat(none.map(i->i+5),equalTo(Xor.secondary("none")));
	}

	@Test
	public void testFlatMap() {
		assertThat(just.flatMap(i->Xor.primary(i+5)),equalTo(Xor.primary(15)));
		assertThat(none.flatMap(i->Xor.primary(i+5)),equalTo(Xor.secondary("none")));
	}

	@Test
	public void testWhenFunctionOfQsuperTQextendsRSupplierOfQextendsR() {
		assertThat(just.visit(i->i+1,()->20),equalTo(11));
		assertThat(none.visit(i->i+1,()->20),equalTo(20));
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
	   
        Stream<Integer> toStream = just.visit(m->Stream.of(m),()->Stream.of());
        assertThat(toStream.collect(Collectors.toList()),equalTo(ListX.of(10)));
    }


    @Test
    public void testConvertToAsync() {
        Future<Stream<Integer>> async = Future.ofSupplier(()->just.visit(f->Stream.of((int)f),()->Stream.of()));
        
        assertThat(async.get().collect(Collectors.toList()),equalTo(ListX.of(10)));
    }
	
	@Test
	public void testIterate() {
		assertThat(just.iterate(i->i+1).limit(10).sumInt(i->i),equalTo(145));
	}

	@Test
	public void testGenerate() {
		assertThat(just.generate().limit(10).sumInt(i->i),equalTo(100));
	}

	@Test
	public void testMapReduceReducerOfE() {
		assertThat(just.mapReduce(Reducers.toCountInt()),equalTo(1));
	}



	@Test
	public void testToXor() {
		assertThat(just.toXor(),equalTo(Xor.primary(10)));
		
	}
	@Test
	public void testToXorNone(){
		Xor<String,Integer> xor = none.toXor();
		assertTrue(xor.isSecondary());
		assertThat(xor,equalTo(Xor.secondary("none")));
		
	}


	@Test
	public void testToXorSecondary() {
		assertThat(just.toXor().swap(),equalTo(Xor.secondary(10)));
	}

	@Test
	public void testToXorSecondaryNone(){
		Xor<Integer,String> xorNone = none.toXor().swap();
		assertThat(xorNone,equalTo(Xor.primary("none")));
		
	}
	@Test
	public void testToTry() {
		assertTrue(none.toTry().isFailure());
		assertThat(just.toTry(),equalTo(Try.success(10)));
	}

	@Test
	public void testToTryClassOfXArray() {
		assertTrue(none.toTry(Throwable.class).isFailure());
	}

	@Test
	public void testToIor() {
		assertThat(just.toIor(),equalTo(Ior.primary(10)));
		
	}
	@Test
	public void testToIorNone(){
		Ior<String,Integer> ior = none.toIor();
		assertTrue(ior.isSecondary());
        assertThat(ior,equalTo(Ior.secondary("none")));
		
	}


	@Test
	public void testToIorSecondary() {
		assertThat(just.toIor().swap(),equalTo(Ior.secondary(10)));
	}
	

	@Test
	public void testToIorSecondaryNone(){
	    Ior<Integer,String> ior = none.toIor().swap();
        assertTrue(ior.isPrimary());
        assertThat(ior,equalTo(Ior.primary("none")));
		
	}
	@Test
	public void testToEvalNow() {
		assertThat(just.toEvalNow(),equalTo(Eval.now(10)));
	}
	@Test(expected=NoSuchElementException.class)
	public void testToEvalNowNone() {
		none.toEvalNow();
		fail("exception expected");
		
	}

	@Test
	public void testToEvalLater() {
		assertThat(just.toEvalLater(),equalTo(Eval.later(()->10)));
	}
	@Test(expected=NoSuchElementException.class)
	public void testToEvalLaterNone() {
		none.toEvalLater().get();
		fail("exception expected");
		
	}

	@Test
	public void testToEvalAlways() {
		assertThat(just.toEvalAlways(),equalTo(Eval.always(()->10)));
	}
	@Test(expected=NoSuchElementException.class)
	public void testToEvalAlwaysNone() {
		none.toEvalAlways().get();
		fail("exception expected");
		
	}


	@Test
	public void testMkString() {
		assertThat(just.mkString(),equalTo("Xor.primary[10]"));
		assertThat(none.mkString(),equalTo("Xor.secondary[none]"));
	}
	LazyReact react = new LazyReact();


	@Test
	public void testGet() {
		assertThat(just.get(),equalTo(10));
	}
	@Test(expected=NoSuchElementException.class)
	public void testGetNone() {
		none.get();
		
	}

	@Test
	public void testFilter() {
		assertFalse(just.filter(i->i<5).isPrimary());
		assertTrue(just.filter(i->i>5).isPrimary());
		assertFalse(none.filter(i->i<5).isPrimary());
		assertFalse(none.filter(i->i>5).isPrimary());
		
	}

	@Test
	public void testOfType() {
		assertFalse(just.ofType(String.class).isPrimary());
		assertTrue(just.ofType(Integer.class).isPrimary());
		assertFalse(none.ofType(String.class).isPrimary());
		assertFalse(none.ofType(Integer.class).isPrimary());
	}

	@Test
	public void testFilterNot() {
		assertTrue(just.filterNot(i->i<5).isPrimary());
		assertFalse(just.filterNot(i->i>5).isPrimary());
		assertFalse(none.filterNot(i->i<5).isPrimary());
		assertFalse(none.filterNot(i->i>5).isPrimary());
	}

	@Test
	public void testNotNull() {
		assertTrue(just.notNull().isPrimary());
		assertFalse(none.notNull().isPrimary());
		
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
	public void testMapReduceFunctionOfQsuperTQextendsRMonoidOfR() {
		assertThat(just.mapReduce(s->s.toString(), Monoid.of("",Semigroups.stringJoin(","))),equalTo(",10"));
	}

	@Test
	public void testReduceMonoidOfT() {
		assertThat(just.reduce(Monoid.of(1, Semigroups.intMult)),equalTo(10));
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
	public void testWhenFunctionOfQsuperMaybeOfTQextendsR() {
		assertThat(just.visit(s->"hello", ()->"world"),equalTo("hello"));
		assertThat(none.visit(s->"hello", ()->"world"),equalTo("world"));
	}

	
	@Test
	public void testOrElseGet() {
		assertThat(none.orElseGet(()->2),equalTo(2));
		assertThat(just.orElseGet(()->2),equalTo(10));
	}

	@Test
	public void testToOptional() {
		assertFalse(none.toOptional().isPresent());
		assertTrue(just.toOptional().isPresent());
		assertThat(just.toOptional(),equalTo(Optional.of(10)));
	}

	@Test
	public void testToStream() {
		assertThat(none.toStream().collect(Collectors.toList()).size(),equalTo(0));
		assertThat(just.toStream().collect(Collectors.toList()).size(),equalTo(1));
		
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
	public void testToFutureW() {
		Future<Integer> cf = just.toFuture();
		assertThat(cf.get(),equalTo(10));
	}

	@Test
	public void testToCompletableFuture() {
		CompletableFuture<Integer> cf = just.toCompletableFuture();
		assertThat(cf.join(),equalTo(10));
	}


	Executor exec = Executors.newFixedThreadPool(1);

	@Test
	public void testToCompletableFutureAsyncExecutor() {
		CompletableFuture<Integer> cf = just.toCompletableFutureAsync(exec);
		assertThat(cf.join(),equalTo(10));
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
	public void testCast() {
		Xor<?,Number> num = just.cast(Number.class);
	}

	@Test
	public void testMapFunctionOfQsuperTQextendsR1() {
		assertThat(just.map(i->i+5),equalTo(Xor.primary(15)));
	}
	
	@Test
	public void testPeek() {
		Mutable<Integer> capture = Mutable.of(null);
		just = just.peek(c->capture.set(c));
		
		assertThat(capture.get(),equalTo(10));
	}

	private Trampoline<Integer> sum(int times, int sum){
		return times ==0 ?  Trampoline.done(sum) : Trampoline.more(()->sum(times-1,sum+times));
	}
	@Test
	public void testTrampoline() {
		assertThat(just.trampoline(n ->sum(10,n)),equalTo(Xor.primary(65)));
	}

	

	@Test
	public void testUnitT1() {
		assertThat(none.unit(10),equalTo(just));
	}

}
