package com.aol.cyclops.control;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

import com.aol.cyclops.Semigroups;
import com.aol.cyclops.closures.immutable.LazyImmutable;
import com.aol.cyclops.closures.mutable.Mutable;
import com.aol.cyclops.collections.extensions.persistent.PBagX;
import com.aol.cyclops.collections.extensions.persistent.POrderedSetX;
import com.aol.cyclops.collections.extensions.persistent.PQueueX;
import com.aol.cyclops.collections.extensions.persistent.PSetX;
import com.aol.cyclops.collections.extensions.persistent.PStackX;
import com.aol.cyclops.collections.extensions.persistent.PVectorX;
import com.aol.cyclops.collections.extensions.standard.DequeX;
import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.collections.extensions.standard.QueueX;
import com.aol.cyclops.collections.extensions.standard.SetX;
import com.aol.cyclops.collections.extensions.standard.SortedSetX;
import com.aol.cyclops.lambda.applicative.Applicativable.Applicatives;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.Reducers;
import com.aol.cyclops.streams.StreamUtils;
import com.aol.cyclops.trampoline.Trampoline;
import com.aol.cyclops.trycatch.Try;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.simple.SimpleReact;



public class MaybeTest {

	Maybe<Integer> just;
	Maybe<Integer> none;
	@Before
	public void setUp() throws Exception {
		just = Maybe.of(10);
		none = Maybe.none();
	}

	@Test
	public void testToMaybe() {
		assertThat(just.toMaybe(),equalTo(just));
		assertThat(none.toMaybe(),equalTo(none));
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
		assertThat(Maybe.fromOptional(Optional.of(10)),equalTo(just));
	}

	@Test
	public void testFromEvalSome() {
		assertThat(Maybe.fromEvalOf(Eval.now(10)),equalTo(just));
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
		Maybe<ListX<Integer>> maybes =Maybe.sequence(ListX.of(just,none,Maybe.of(1)));
		assertThat(maybes,equalTo(Maybe.of(ListX.of(10,1))));
	}

	@Test
	public void testAccumulateJustCollectionXOfMaybeOfTReducerOfR() {
		Maybe<PSetX<Integer>> maybes =Maybe.accumulateJust(ListX.of(just,none,Maybe.of(1)),Reducers.toPSetX());
		assertThat(maybes,equalTo(Maybe.of(PSetX.of(10,1))));
	}

	@Test
	public void testAccumulateJustCollectionXOfMaybeOfTFunctionOfQsuperTRSemigroupOfR() {
		Maybe<String> maybes =Maybe.accumulateJust(ListX.of(just,none,Maybe.of(1)),i->""+i,Semigroups.stringConcat);
		assertThat(maybes,equalTo(Maybe.of("101")));
	}
	@Test
	public void testAccumulateJust() {
		Maybe<Integer> maybes =Maybe.accumulateJust(ListX.of(just,none,Maybe.of(1)),Semigroups.intSum);
		assertThat(maybes,equalTo(Maybe.of(11)));
	}

	@Test
	public void testUnitT() {
		assertThat(just.unit(20),equalTo(Maybe.of(20)));
	}

	

	@Test
	public void testIsPresent() {
		assertTrue(just.isPresent());
		assertFalse(none.isPresent());
	}

	@Test
	public void testRecoverSupplierOfT() {
		assertThat(just.recover(20),equalTo(Maybe.of(10)));
		assertThat(none.recover(10),equalTo(Maybe.of(10)));
	}

	@Test
	public void testRecoverT() {
		assertThat(just.recover(()->20),equalTo(Maybe.of(10)));
		assertThat(none.recover(()->10),equalTo(Maybe.of(10)));
	}

	@Test
	public void testMapFunctionOfQsuperTQextendsR() {
		assertThat(just.map(i->i+5),equalTo(Maybe.of(15)));
		assertThat(none.map(i->i+5),equalTo(Maybe.none()));
	}

	@Test
	public void testFlatMap() {
		assertThat(just.flatMap(i->Maybe.of(i+5)),equalTo(Maybe.of(15)));
		assertThat(none.flatMap(i->Maybe.of(i+5)),equalTo(Maybe.none()));
	}

	@Test
	public void testWhenFunctionOfQsuperTQextendsRSupplierOfQextendsR() {
		assertThat(just.when(i->i+1,()->20),equalTo(11));
		assertThat(none.when(i->i+1,()->20),equalTo(20));
	}

	@Test
	public void testUnapply() {
		assertThat(just.unapply(),equalTo(ListX.of(10)));
		assertThat(none.unapply(),equalTo(ListX.of()));
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
		Stream<Integer> toStream = just.convertTo(m->Stream.of((int)m.get()));
		assertThat(toStream.collect(Collectors.toList()),equalTo(ListX.of(10)));
	}

	@Test
	public void testConvertToAsync() {
		FutureW<Stream<Integer>> async = just.convertToAsync(f->f.thenApply(i->Stream.of((int)i)));
		
		assertThat(async.get().collect(Collectors.toList()),equalTo(ListX.of(10)));
	}

	@Test
	public void testGetMatchable() {
		assertThat(just.getMatchable(),equalTo(10));
	}

	@Test
	public void testIterate() {
		assertThat(just.iterate(i->i+1).limit(10).sum(),equalTo(50));
	}

	@Test
	public void testGenerate() {
		assertThat(just.generate().limit(10).sum(),equalTo(50));
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
		none.toLazyImmutable();
		fail("exception expected");
		
	}

	@Test
	public void testToMutable() {
		assertThat(just.toMutable(),equalTo(Mutable.of(10)));
		
		
	}
	@Test
	public void testToMutableNone(){
		none.toMutable();
		fail("exception expected");
		
	}

	@Test
	public void testToXor() {
		assertThat(just.toXor(),equalTo(Xor.primary(10)));
		
	}
	@Test
	public void testToXorNone(){
		none.toXor();
		fail("exception expected");
		
	}


	@Test
	public void testToXorSecondary() {
		assertThat(just.toXorSecondary(),equalTo(Xor.secondary(10)));
	}

	@Test
	public void testToXorSecondaryNone(){
		none.toXorSecondary();
		fail("exception expected");
		
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
		assertThat(just.toIor(),equalTo(Xor.primary(10)));
		
	}
	@Test(expected=NoSuchElementException.class)
	public void testToIorNone(){
		none.toIor();
		fail("exception expected");
		
	}


	@Test
	public void testToIorSecondary() {
		assertThat(just.toIorSecondary(),equalTo(Xor.secondary(10)));
	}

	@Test(expected=NoSuchElementException.class)
	public void testToIorSecondaryNone(){
		none.toIorSecondary();
		fail("exception expected");
		
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
		none.toEvalLater();
		fail("exception expected");
		
	}

	@Test
	public void testToEvalAlways() {
		assertThat(just.toEvalAlways(),equalTo(Eval.always(()->10)));
	}
	@Test(expected=NoSuchElementException.class)
	public void testToEvalAlwaysNone() {
		none.toEvalAlways();
		fail("exception expected");
		
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
		assertThat(just.toQueueX(),equalTo(QueueX.singleton(10)));
		assertThat(none.toQueueX(),equalTo(QueueX.empty()));
	}

	@Test
	public void testToDequeX() {
		assertThat(just.toDequeX(),equalTo(DequeX.singleton(10)));
		assertThat(none.toDequeX(),equalTo(DequeX.empty()));
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
		assertThat(just.toPQueueX(),equalTo(PQueueX.singleton(10)));
		assertThat(none.toPQueueX(),equalTo(PQueueX.empty()));
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
		assertThat(just.mkString(),equalTo("10"));
		assertThat(none.mkString(),equalTo(""));
	}
	LazyReact react = new LazyReact();
	@Test
	public void testToFutureStreamLazyReact() {
		assertThat(just.toFutureStream(react).toList(),equalTo(Arrays.asList(10)));
		assertThat(none.toFutureStream(react).toList(),equalTo(Arrays.asList()));
	}

	@Test
	public void testToFutureStream() {
		assertThat(just.toFutureStream().toList(),equalTo(Arrays.asList(10)));
		assertThat(none.toFutureStream().toList(),equalTo(Arrays.asList()));
	}
	SimpleReact react2 = new SimpleReact();
	@Test
	public void testToSimpleReactSimpleReact() {
		assertThat(just.toSimpleReact(react2).block(),equalTo(Arrays.asList(10)));
		assertThat(none.toSimpleReact(react2).block(),equalTo(Arrays.asList()));
	}

	@Test
	public void testToSimpleReact() {
		assertThat(just.toSimpleReact().block(),equalTo(Arrays.asList(10)));
		assertThat(none.toSimpleReact().block(),equalTo(Arrays.asList()));
	}

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
		assertThat(just.mapReduce(s->s.toString(), Monoid.of("",Semigroups.stringJoin(","))),equalTo("10"));
	}

	@Test
	public void testReduceMonoidOfT() {
		assertThat(just.reduce(Monoid.of(1,Semigroups.intMult)),equalTo(10));
	}

	@Test
	public void testReduceBinaryOperatorOfT() {
		assertThat(just.reduce((a,b)->a+b),equalTo(10));
	}

	@Test
	public void testReduceTBinaryOperatorOfT() {
		assertThat(just.reduce(10,(a,b)->a+b),equalTo(20));
	}

	@Test
	public void testReduceUBiFunctionOfUQsuperTUBinaryOperatorOfU() {
		assertThat(just.reduce(11,(a,b)->a+b,(a,b)->a*b),equalTo(11));
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
		assertThat(just.when(s->"hello", ()->"world"),equalTo("hello"));
		assertThat(none.when(s->"hello", ()->"world"),equalTo("world"));
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
	@Test(expected=NoSuchElementException.class)
	public void testToAtomicReferenceNone() {
		none.toAtomicReference().get();
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
	public void testGetMatchable1() {
		assertThat(just.getMatchable(),equalTo(10));
	}

	@Test
	public void testMatches() {
		assertThat(just.mayMatch(c->c.values(i->"hello",10)),equalTo(Maybe.of("hello")));
		assertThat(just.mayMatch(c->c.values(i->"hello",1).values(i->"hello",2)),equalTo(Maybe.of("hello")));
		assertThat(just.mayMatch(c->c.just(i->"hello",1)
									 .values(i->"hello",2)
									 .values(i->"hello",3)),equalTo(Maybe.none()));
		
	}

	
	

	@Test
	public void testUnwrap() {
		assertThat(just.unwrap(),equalTo(Maybe.of(10)));
		assertThat(none.unwrap(),equalTo(Maybe.none()));
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
		assertThat(capture.get(),equalTo(nullValue()));
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
		Maybe<Number> num = just.cast(Number.class);
	}

	@Test
	public void testMapFunctionOfQsuperTQextendsR1() {
		assertThat(just.map(i->i+5),equalTo(Maybe.of(15)));
	}
	
	@Test
	public void testPeek() {
		Mutable<Integer> capture = Mutable.of(null);
		just.peek(c->capture.set(c));
		assertThat(capture.get(),equalTo(nullValue()));
		just.get();
		assertThat(capture.get(),equalTo(10));
	}

	private Trampoline<Integer> sum(int times){
		return times ==0 ?  Trampoline.done(times) : Trampoline.more(()->sum(times-1));
	}
	@Test
	public void testTrampoline() {
		assertThat(just.trampoline(this::sum),equalTo(40));
	}

	

	@Test
	public void testUnitT1() {
		assertThat(none.unit(10),equalTo(just));
	}

}
