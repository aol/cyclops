package com.aol.cyclops.control;

import static com.aol.cyclops.control.Matchable.otherwise;
import static com.aol.cyclops.control.Matchable.then;
import static com.aol.cyclops.control.Matchable.when;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducers;
import com.aol.cyclops.Semigroups;
import com.aol.cyclops.data.LazyImmutable;
import com.aol.cyclops.data.Mutable;
import com.aol.cyclops.data.collections.extensions.persistent.PBagX;
import com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX;
import com.aol.cyclops.data.collections.extensions.persistent.PQueueX;
import com.aol.cyclops.data.collections.extensions.persistent.PSetX;
import com.aol.cyclops.data.collections.extensions.persistent.PStackX;
import com.aol.cyclops.data.collections.extensions.persistent.PVectorX;
import com.aol.cyclops.data.collections.extensions.standard.DequeX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.QueueX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.data.collections.extensions.standard.SortedSetX;
import com.aol.cyclops.types.applicative.Applicativable.Applicatives;
import com.aol.cyclops.util.stream.StreamUtils;



public class Eval2Test {

	Eval<Integer> just;
	Eval<Integer> none;
	@Before
	public void setUp() throws Exception {
		just = Eval.now(10);
		none = Eval.now(null);
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
	public void testApplicativeBuilder() {
		assertThat(Applicatives.<Integer,Integer>applicatives(just, just)
					.applicative(this::add1).ap(Optional.of(20)).get(),equalTo(21));
	}

	

	@Test
	public void testFromOptional() {
		assertThat(Maybe.fromOptional(Optional.of(10)),equalTo(just.toMaybe()));
	}

	@Test
	public void testFromEvalSome() {
		assertThat(Maybe.fromEvalOf(Eval.now(10)),equalTo(just.toMaybe()));
	}

	@Test
	public void testOfT() {
		assertThat(Maybe.of(1),equalTo(Maybe.of(1)));
	}

	

	@Test
	public void testOfNullable() {
		assertFalse(Maybe.ofNullable(null).isPresent());
		assertThat(Maybe.ofNullable(1),equalTo(Maybe.of(1)));
		
	}

	@Test
	public void testNarrow() {
		assertThat(Maybe.ofNullable(1),equalTo(Maybe.narrow(Maybe.of(1))));
	}

	@Test
	public void testSequence() {
		Eval<ListX<Integer>> maybes =Eval.sequence(ListX.of(just,Eval.now(1)));
		assertThat(maybes,equalTo(Eval.now(ListX.of(10,1))));
	}

	@Test
	public void testAccumulateJustCollectionXOfMaybeOfTReducerOfR() {
		Eval<PSetX<Integer>> maybes =Eval.accumulate(ListX.of(just,none,Eval.now(1)),Reducers.toPSetX());
		assertThat(maybes,equalTo(Eval.now(PSetX.of(10,1))));
	}

	@Test
	public void testAccumulateJustCollectionXOfMaybeOfTFunctionOfQsuperTRSemigroupOfR() {
		Eval<String> maybes =Eval.accumulate(ListX.of(just,none,Eval.later(()->1)),i->""+i,Semigroups.stringConcat);
		assertThat(maybes,equalTo(Eval.now("101")));
	}
	@Test
	public void testAccumulateJust() {
		Eval<Integer> maybes =Eval.accumulate(ListX.of(just,none,Eval.now(1)),Semigroups.intSum);
		assertThat(maybes,equalTo(Eval.now(11)));
	}

	@Test
	public void testUnitT() {
		assertThat(just.unit(20),equalTo(Eval.now(20)));
	}

	

	@Test
	public void testIsPresent() {
		assertTrue(just.toMaybe().isPresent());
		assertFalse(none.toMaybe().isPresent());
	}

	@Test
	public void testRecoverSupplierOfT() {
		assertThat(just.toMaybe().recover(20),equalTo(Maybe.of(10)));
		assertThat(none.toMaybe().recover(10),equalTo(Maybe.of(10)));
	}

	@Test
	public void testRecoverT() {
		assertThat(just.toMaybe().recover(()->20),equalTo(Maybe.of(10)));
		assertThat(none.toMaybe().recover(()->10),equalTo(Maybe.of(10)));
	}

	@Test
	public void testMapFunctionOfQsuperTQextendsR() {
		assertThat(just.map(i->i+5),equalTo(Eval.now(15)));
		assertThat(none.toMaybe().map(i->i+5),equalTo(Maybe.none()));
	}

	@Test
	public void testFlatMap() {
		assertThat(just.flatMap(i->Eval.now(i+5)),equalTo(Eval.later(()->15)));
		assertThat(none.toMaybe().flatMap(i->Maybe.of(i+5)),equalTo(Maybe.none()));
	}

	@Test
	public void testWhenFunctionOfQsuperTQextendsRSupplierOfQextendsR() {
		assertThat(just.visit(i->i+1,()->20),equalTo(11));
		assertThat(none.visit(i->i+1,()->20),equalTo(20));
	}

	@Test
	public void testUnapply() {
		assertThat(just.unapply(),equalTo(ListX.of(10)));
		assertThat(none.unapply(),equalTo(ListX.of()));
	}

	@Test
	public void testStream() {
		assertThat(just.stream().toListX(),equalTo(ListX.of(10)));
		assertThat(none.stream().filter(i->i!=null).toListX(),equalTo(ListX.of()));
	}

	@Test
	public void testOfSupplierOfT() {
		
	}


    public void testConvertTo() {
        Stream<Integer> toStream = just.visit(m->Stream.of(m),()->Stream.of());
        assertThat(toStream.collect(Collectors.toList()),equalTo(ListX.of(10)));
    }

    @Test
    public void testConvertToAsync() {
        FutureW<Stream<Integer>> async = FutureW.ofSupplier(()->just.visit(f->Stream.of((int)f),()->Stream.of()));
        
        assertThat(async.get().collect(Collectors.toList()),equalTo(ListX.of(10)));
    }
	

	@Test
	public void testIterate() {
		assertThat(just.iterate(i->i+1).limit(10).sum(),equalTo(Optional.of(145)));
	}

	@Test
	public void testGenerate() {
		assertThat(just.generate().limit(10).sum(),equalTo(Optional.of(100)));
	}

	@Test
	public void testMapReduceReducerOfE() {
		assertThat(just.mapReduce(Reducers.toCountInt()),equalTo(1));
	}

	@Test
	public void testFoldMonoidOfT() {
		assertThat(just.fold(Reducers.toTotalInt()),equalTo(10));
	}

	@Test
	public void testFoldTBinaryOperatorOfT() {
		assertThat(just.fold(1, (a,b)->a*b),equalTo(10));
	}

	@Test
	public void testToLazyImmutable() {
		assertThat(just.toLazyImmutable(),equalTo(LazyImmutable.of(10)));
	}
	@Test
	public void testToLazyImmutableNone(){
		assertThat(none.toLazyImmutable().get(),nullValue());
		
		
	}

	@Test
	public void testToMutable() {
		assertThat(just.toMutable(),equalTo(Mutable.of(10)));
		
		
	}
	@Test
	public void testToMutableNone(){
		assertThat(none.toMutable().get(),nullValue());
		
		
	}

	@Test
	public void testToXor() {
		assertThat(just.toXor(),equalTo(Xor.primary(10)));
		
	}
	@Test
	public void testToXorNone(){
	    Xor<?,Integer> empty = none.toXor();
	    
	    
        assertTrue(empty.swap().map(__->10).get()==10);
		
	}


	@Test
	public void testToXorSecondary() {
		assertThat(just.toXor().swap(),equalTo(Xor.secondary(10)));
	}

	@Test
	public void testToXorSecondaryNone(){
		Xor<Integer,?> empty = none.toXor().swap();
		assertTrue(empty.isPrimary());
		assertThat(empty.map(__->10),equalTo(Xor.primary(10)));
		
		
	}
	@Test
	public void testToTry() {
		assertTrue(none.toTry().isFailure());
		assertThat(just.toTry(),equalTo(Try.success(10)));
	}

	@Test
	public void testToTryClassOfXArray() {
		assertTrue(none.toTry(Throwable.class).isSuccess());
	}

	@Test
	public void testToIor() {
		assertThat(just.toIor(),equalTo(Ior.primary(10)));
		
	}
	@Test
	public void testToIorNone(){
	    Xor<Integer,?> empty = none.toXor().swap();
        assertTrue(empty.isPrimary());
        assertThat(empty.map(__->10),equalTo(Xor.primary(10)));
		
	}


	@Test
	public void testToIorSecondary() {
		assertThat(just.toIor().swap(),equalTo(Ior.secondary(10)));
	}

	@Test
	public void testToIorSecondaryNone(){
		Ior<Integer,?> ior = none.toIor().swap().map(__->10);
		assertThat(ior.get(),equalTo(10));
		
	}
	@Test
	public void testToEvalNow() {
		assertThat(just.toEvalNow(),equalTo(Eval.now(10)));
	}
	@Test
	public void testToEvalNowNone() {
		assertThat(none.toEvalNow(),equalTo(Eval.now(null)));
		
		
	}

	@Test
	public void testToEvalLater() {
		assertThat(just.toEvalLater(),equalTo(Eval.later(()->10)));
	}
	@Test
	public void testToEvalLaterNone() {
	    assertThat(none.toEvalLater().get(),nullValue());
	
		
	}

	@Test
	public void testToEvalAlways() {
		assertThat(just.toEvalAlways(),equalTo(Eval.always(()->10)));
	}
	@Test
	public void testToEvalAlwaysNone() {
		assertThat(none.toEvalAlways().get(),nullValue());
		
		
	}

	@Test
	public void testToListX() {
		
		assertThat(just.toListX(),equalTo(ListX.singleton(10)));
		assertThat(none.toListX(),equalTo(ListX.empty()));
	}

	@Test
	public void testToSetX() {
		assertThat(just.toSetX(),equalTo(SetX.singleton(10)));
		assertThat(none.toSetX(),equalTo(SetX.empty()));
	}

	@Test
	public void testToSortedSetX() {
		assertThat(just.toSortedSetX(),equalTo(SortedSetX.singleton(10)));
		assertThat(none.toSortedSetX(),equalTo(SortedSetX.empty()));
	}

	@Test
	public void testToQueueX() {
		assertThat(just.toQueueX().toList(),equalTo(QueueX.singleton(10).toList()));
		assertThat(none.toQueueX().toList(),equalTo(QueueX.empty().toList()));
	}

	@Test
	public void testToDequeX() {
		assertThat(just.toDequeX().toList(),equalTo(Arrays.asList(10)));
		assertThat(none.toDequeX().toList(),equalTo(DequeX.empty().toList()));
	}

	@Test
	public void testToPStackX() {
		assertThat(just.toPStackX(),equalTo(PStackX.singleton(10)));
		assertThat(none.toPStackX(),equalTo(PStackX.empty()));
	}

	@Test
	public void testToPVectorX() {
		assertThat(just.toPVectorX(),equalTo(PVectorX.singleton(10)));
		assertThat(none.toPVectorX(),equalTo(PVectorX.empty()));
	}

	@Test
	public void testToPQueueX() {
		assertThat(just.toPQueueX().toList(),equalTo(PQueueX.singleton(10).toList()));
		assertThat(none.toPQueueX().toList(),equalTo(PQueueX.empty().toList()));
	}

	@Test
	public void testToPSetX() {
		assertThat(just.toPSetX(),equalTo(PSetX.singleton(10)));
		assertThat(none.toPSetX(),equalTo(PSetX.empty()));
	}

	@Test
	public void testToPOrderedSetX() {
		assertThat(just.toPOrderedSetX(),equalTo(POrderedSetX.singleton(10)));
		assertThat(none.toPOrderedSetX(),equalTo(POrderedSetX.empty()));
	}

	@Test
	public void testToPBagX() {
		assertThat(just.toPBagX(),equalTo(PBagX.singleton(10)));
		assertThat(none.toPBagX(),equalTo(PBagX.empty()));
	}

	@Test
	public void testMkString() {
		assertThat(just.mkString(),equalTo("Always[10]"));
		assertThat(none.mkString(),equalTo("Always[]"));
	}
	LazyReact react = new LazyReact();
	@Test
	public void testToFutureStreamLazyReact() {
		assertThat(just.toFutureStream(react).toList(),equalTo(Arrays.asList(10)));
		assertThat(none.toFutureStream(react).filter(i->i!=null).toList(),equalTo(Arrays.asList()));
	}

	@Test
	public void testToFutureStream() {
		assertThat(just.toFutureStream().toList(),equalTo(Arrays.asList(10)));
		assertThat(none.toFutureStream().filter(i->i!=null).toList(),equalTo(Arrays.asList()));
	}
	SimpleReact react2 = new SimpleReact();
	@Test
	public void testToSimpleReactSimpleReact() {
		assertThat(just.toSimpleReact(react2).block(),equalTo(Arrays.asList(10)));
		assertThat(none.toSimpleReact(react2).filter(i->i!=null).block(),equalTo(Arrays.asList()));
	}

	@Test
	public void testToSimpleReact() {
		assertThat(just.toSimpleReact().block(),equalTo(Arrays.asList(10)));
		assertThat(none.toSimpleReact().filter(i->i!=null).block(),equalTo(Arrays.asList()));
	}

	@Test
	public void testGet() {
		assertThat(just.get(),equalTo(10));
	}
	@Test
	public void testGetNone() {
		assertThat(none.get(),nullValue());
		
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

	


	@Test
	public void testAp1() {
		assertThat(Maybe.of(1).ap1(this::add1).toMaybe(),equalTo(Maybe.of(2)));
	}
	
	private int add(int a, int b){
		return a+b;
	}

	@Test
	public void testAp2() {
		assertThat(Maybe.of(1).ap2(this::add).ap(Optional.of(3)).toMaybe(),equalTo(Maybe.of(4)));
	}
	private int add3(int a, int b, int c){
		return a+b+c;
	}
	@Test
	public void testAp3() {
		assertThat(Maybe.of(1).ap3(this::add3).ap(Optional.of(3)).ap(Maybe.of(4)).toMaybe(),equalTo(Maybe.of(8)));
	}
	private int add4(int a, int b, int c,int d){
		return a+b+c+d;
	}
	@Test
	public void testAp4() {
		assertThat(Maybe.of(1).ap4(this::add4)
						.ap(Optional.of(3))
						.ap(Maybe.of(4))
						.ap(Maybe.of(6)).toMaybe(),equalTo(Maybe.of(14)));
	}
	private int add5(int a, int b, int c,int d,int e){
		return a+b+c+d+e;
	}
	@Test
	public void testAp5() {
		assertThat(Maybe.of(1).ap5(this::add5)
				.ap(Optional.of(3))
				.ap(Maybe.of(4))
				.ap(Maybe.of(6))
				.ap(Maybe.of(10)).toMaybe(),equalTo(Maybe.of(24)));
	}

	

	@Test
	public void testMapReduceReducerOfR() {
		assertThat(just.mapReduce(Reducers.toPStackX()),equalTo(just.toPStackX()));
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
		assertThat(just.foldRightMapToType(Reducers.toPStackX()),equalTo(just.toPStackX()));
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
	public void testToAtomicReference() {
		assertThat(just.toAtomicReference().get(),equalTo(10));
	}
	@Test
	public void testToAtomicReferenceNone() {
		assertThat(none.toAtomicReference().get(),nullValue());
	}

	@Test
	public void testToOptionalAtomicReference() {
		assertFalse(none.toOptionalAtomicReference().isPresent());
		assertTrue(just.toOptionalAtomicReference().isPresent());
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
	public void testToList() {
		assertThat(just.toList(),equalTo(Arrays.asList(10)));
		assertThat(none.toListX(),equalTo(new ArrayList<>()));
	}

	
	@Test
	public void testToFutureW() {
		FutureW<Integer> cf = just.toFutureW();
		assertThat(cf.get(),equalTo(10));
	}

	@Test
	public void testToCompletableFuture() {
		CompletableFuture<Integer> cf = just.toCompletableFuture();
		assertThat(cf.join(),equalTo(10));
	}

	@Test
	public void testToCompletableFutureAsync() {
		CompletableFuture<Integer> cf = just.toCompletableFutureAsync();
		assertThat(cf.join(),equalTo(10));
	}
	Executor exec = Executors.newFixedThreadPool(1);

	@Test
	public void testToCompletableFutureAsyncExecutor() {
		CompletableFuture<Integer> cf = just.toCompletableFutureAsync(exec);
		assertThat(cf.join(),equalTo(10));
	}

	

	@Test
	public void testMatches() {
		assertThat(just.matches(c->c.is(when(10),then("hello")),otherwise("miss")).toMaybe(),equalTo(Maybe.of("hello")));
		assertThat(just.matches(c->c.is(when(10),then("hello")).is(when(2),then("hello")),otherwise("miss")).toMaybe(),equalTo(Maybe.of("hello")));
		assertThat(just.matches(c->c.is(when(1),then("hello"))
									 .is(when(2),then(()->"hello"))
									 .is(when(3),then(()->"hello")),otherwise("miss")).toMaybe(),equalTo(Maybe.just("miss")));
		
	}

	
	

	

	@Test
	public void testIterator1() {
		assertThat(StreamUtils.stream(just.iterator()).collect(Collectors.toList()),
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
		Eval<Number> num = just.cast(Number.class);
	}

	@Test
	public void testMapFunctionOfQsuperTQextendsR1() {
		assertThat(just.map(i->i+5),equalTo(Eval.now(15)));
	}
	
	@Test
	public void testPeek() {
		Mutable<Integer> capture = Mutable.of(null);
		just = just.peek(c->capture.set(c));
		
		
		just.get();
		assertThat(capture.get(),equalTo(10));
	}

	private Trampoline<Integer> sum(int times,int sum){
		return times ==0 ?  Trampoline.done(sum) : Trampoline.more(()->sum(times-1,sum+times));
	}
	@Test
	public void testTrampoline() {
		assertThat(just.trampoline(n ->sum(10,n)),equalTo(Eval.now(65)));
	}

	

	@Test
	public void testUnitT1() {
		assertThat(none.unit(10),equalTo(just));
	}

}
