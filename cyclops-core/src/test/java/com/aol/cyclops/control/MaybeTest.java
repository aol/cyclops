package com.aol.cyclops.control;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.aol.cyclops.sequence.Reducers;
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
		assertThat(just.applicatives().applicative(this::add1).ap(Optional.of(20)).get(),equalTo(21));
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
	public void testAp1ApplicativeOfTRQ() {
		assertThat(just.ap1(just.applicatives().applicative(this::add1)),equalTo(Maybe.of(11)));
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
	public void testRemoveAllStreamOfT() {
		assertFalse(just.removeAll(Stream.of(10,11,12)).isPresent());
		assertTrue(just.removeAll(Stream.of(11,12)).isPresent());
	}

	@Test
	public void testRemoveAllIterableOfT() {
		assertFalse(just.removeAll(ListX.of(10,11,12)).isPresent());
		assertTrue(just.removeAll(ListX.of(11,12)).isPresent());
	}

	@Test
	public void testRemoveAllTArray() {
		assertFalse(just.removeAll(10,11,12).isPresent());
		assertTrue(just.removeAll(11,12).isPresent());
	}

	@Test
	public void testRetainAllIterableOfT() {
		assertTrue(just.retainAll(ListX.of(10,11,12)).isPresent());
		assertFalse(just.retainAll(ListX.of(11,12)).isPresent());
	}

	@Test
	public void testRetainAllStreamOfT() {
		assertTrue(just.retainAll(Stream.of(10,11,12)).isPresent());
		assertFalse(just.retainAll(Stream.of(11,12)).isPresent());
	}

	@Test
	public void testRetainAllTArray() {
		assertTrue(just.retainAll(10,11,12).isPresent());
		assertFalse(just.retainAll(11,12).isPresent());
	}

	@Test
	public void testRetainMatches() {
		assertTrue(just.retainMatches(equalTo(10)).isPresent());
		assertFalse(just.retainMatches(equalTo(11)).isPresent());
	}

	@Test
	public void testRemoveMatches() {
		assertFalse(just.removeMatches(equalTo(10)).isPresent());
		assertTrue(just.removeMatches(equalTo(11)).isPresent());
	}

	@Test
	public void testApplicatives() {
		fail("Not yet implemented");
	}

	@Test
	public void testAp1FunctionOfQsuperTQextendsR() {
		fail("Not yet implemented");
	}

	@Test
	public void testAp2() {
		fail("Not yet implemented");
	}

	@Test
	public void testAp3() {
		fail("Not yet implemented");
	}

	@Test
	public void testAp4() {
		fail("Not yet implemented");
	}

	@Test
	public void testAp5() {
		fail("Not yet implemented");
	}

	@Test
	public void testAp1ApplicativeOfTRQ1() {
		fail("Not yet implemented");
	}

	@Test
	public void testMapReduceReducerOfR() {
		fail("Not yet implemented");
	}

	@Test
	public void testMapReduceFunctionOfQsuperTQextendsRMonoidOfR() {
		fail("Not yet implemented");
	}

	@Test
	public void testReduceMonoidOfT() {
		fail("Not yet implemented");
	}

	@Test
	public void testReduceBinaryOperatorOfT() {
		fail("Not yet implemented");
	}

	@Test
	public void testReduceTBinaryOperatorOfT() {
		fail("Not yet implemented");
	}

	@Test
	public void testReduceUBiFunctionOfUQsuperTUBinaryOperatorOfU() {
		fail("Not yet implemented");
	}

	@Test
	public void testReduceStreamOfQextendsMonoidOfT() {
		fail("Not yet implemented");
	}

	@Test
	public void testReduceIterableOfReducerOfT() {
		fail("Not yet implemented");
	}

	@Test
	public void testFoldLeftMonoidOfT() {
		fail("Not yet implemented");
	}

	@Test
	public void testFoldLeftTBinaryOperatorOfT() {
		fail("Not yet implemented");
	}

	@Test
	public void testFoldLeftMapToType() {
		fail("Not yet implemented");
	}

	@Test
	public void testFoldRightMonoidOfT() {
		fail("Not yet implemented");
	}

	@Test
	public void testFoldRightTBinaryOperatorOfT() {
		fail("Not yet implemented");
	}

	@Test
	public void testFoldRightMapToType() {
		fail("Not yet implemented");
	}

	@Test
	public void testFromSupplier() {
		fail("Not yet implemented");
	}

	@Test
	public void testGet1() {
		fail("Not yet implemented");
	}

	@Test
	public void testFilterWhenPredicateOfQsuperT() {
		fail("Not yet implemented");
	}

	@Test
	public void testFilterWhenPredicateOfQsuperTFunctionOfQsuperTQextendsR() {
		fail("Not yet implemented");
	}

	@Test
	public void testWhenFunctionOfQsuperMaybeOfTQextendsR() {
		fail("Not yet implemented");
	}

	@Test
	public void testFilterWhenOrElse() {
		fail("Not yet implemented");
	}

	@Test
	public void testOrElseGet() {
		fail("Not yet implemented");
	}

	@Test
	public void testToOptional() {
		fail("Not yet implemented");
	}

	@Test
	public void testToStream() {
		fail("Not yet implemented");
	}

	@Test
	public void testToAtomicReference() {
		fail("Not yet implemented");
	}

	@Test
	public void testToOptionalAtomicReference() {
		fail("Not yet implemented");
	}

	@Test
	public void testOrElse() {
		fail("Not yet implemented");
	}

	@Test
	public void testOrElseThrow() {
		fail("Not yet implemented");
	}

	@Test
	public void testToList() {
		fail("Not yet implemented");
	}

	@Test
	public void testIterator() {
		fail("Not yet implemented");
	}

	@Test
	public void testToFutureW() {
		fail("Not yet implemented");
	}

	@Test
	public void testToCompletableFuture() {
		fail("Not yet implemented");
	}

	@Test
	public void testToCompletableFutureAsync() {
		fail("Not yet implemented");
	}

	@Test
	public void testToCompletableFutureAsyncExecutor() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMatchable1() {
		fail("Not yet implemented");
	}

	@Test
	public void testMatchesFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPER() {
		fail("Not yet implemented");
	}

	@Test
	public void testMatchesFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPER() {
		fail("Not yet implemented");
	}

	@Test
	public void testMatchesFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPER() {
		fail("Not yet implemented");
	}

	@Test
	public void testMatchesFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPER() {
		fail("Not yet implemented");
	}

	@Test
	public void testMatchesFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPER() {
		fail("Not yet implemented");
	}

	@Test
	public void testMayMatchFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPER() {
		fail("Not yet implemented");
	}

	@Test
	public void testMayMatchFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPER() {
		fail("Not yet implemented");
	}

	@Test
	public void testMayMatchFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPER() {
		fail("Not yet implemented");
	}

	@Test
	public void testMayMatchFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPER() {
		fail("Not yet implemented");
	}

	@Test
	public void testMayMatchFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPERFunctionOfCheckValuesOfQsuperTYPERCheckValuesOfQsuperTYPER() {
		fail("Not yet implemented");
	}

	@Test
	public void testOfT1() {
		fail("Not yet implemented");
	}

	@Test
	public void testOfStream() {
		fail("Not yet implemented");
	}

	@Test
	public void testOfDecomposable() {
		fail("Not yet implemented");
	}

	@Test
	public void testListOfValues() {
		fail("Not yet implemented");
	}

	@Test
	public void testUnwrap() {
		fail("Not yet implemented");
	}

	@Test
	public void testIterator1() {
		fail("Not yet implemented");
	}

	@Test
	public void testForEach() {
		fail("Not yet implemented");
	}

	@Test
	public void testSpliterator() {
		fail("Not yet implemented");
	}

	@Test
	public void testCast() {
		fail("Not yet implemented");
	}

	@Test
	public void testMapFunctionOfQsuperTQextendsR1() {
		fail("Not yet implemented");
	}

	@Test
	public void testPeek() {
		fail("Not yet implemented");
	}

	@Test
	public void testTrampoline() {
		fail("Not yet implemented");
	}

	@Test
	public void testMatchesCases() {
		fail("Not yet implemented");
	}

	@Test
	public void testPatternMatchRFunctionOfCheckValuesOfQsuperTRCheckValuesOfQsuperTR() {
		fail("Not yet implemented");
	}

	@Test
	public void testPatternMatchRFunctionOfCheckValuesOfQsuperTRCheckValuesOfQsuperTRFunctionOfCheckValuesOfQsuperTRCheckValuesOfQsuperTR() {
		fail("Not yet implemented");
	}

	@Test
	public void testPatternMatchRFunctionOfCheckValuesOfQsuperTRCheckValuesOfQsuperTRFunctionOfCheckValuesOfQsuperTRCheckValuesOfQsuperTRFunctionOfCheckValuesOfQsuperTRCheckValuesOfQsuperTR() {
		fail("Not yet implemented");
	}

	@Test
	public void testPatternMatchRFunctionOfCheckValuesOfQsuperTRCheckValuesOfQsuperTRFunctionOfCheckValuesOfQsuperTRCheckValuesOfQsuperTRFunctionOfCheckValuesOfQsuperTRCheckValuesOfQsuperTRFunctionOfCheckValuesOfQsuperTRCheckValuesOfQsuperTR() {
		fail("Not yet implemented");
	}

	@Test
	public void testPatternMatchRFunctionOfCheckValuesOfQsuperTRCheckValuesOfQsuperTRFunctionOfCheckValuesOfQsuperTRCheckValuesOfQsuperTRFunctionOfCheckValuesOfQsuperTRCheckValuesOfQsuperTRFunctionOfCheckValuesOfQsuperTRCheckValuesOfQsuperTRFunctionOfCheckValuesOfQsuperTRCheckValuesOfQsuperTR() {
		fail("Not yet implemented");
	}

	@Test
	public void testUnitT1() {
		fail("Not yet implemented");
	}

}
