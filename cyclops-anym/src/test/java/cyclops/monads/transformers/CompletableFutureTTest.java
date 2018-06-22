package cyclops.monads.transformers;

import com.oath.cyclops.anym.AnyMValue;
import com.oath.cyclops.ReactiveConvertableSequence;
import cyclops.ReactiveReducers;
import cyclops.companion.*;
import cyclops.data.Seq;
import cyclops.monads.AnyMs;
import cyclops.monads.Witness;
import cyclops.monads.Witness.*;

import com.oath.cyclops.types.mixins.Printable;
import cyclops.monads.transformers.jdk.CompletableFutureT;
import cyclops.reactive.collections.immutable.LinkedListX;
import com.oath.cyclops.util.box.Mutable;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.control.*;
import cyclops.control.Maybe;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;


public class CompletableFutureTTest implements Printable {

	CompletableFutureT<optional,Integer> just;
	CompletableFutureT<optional,Integer> none;
	CompletableFutureT<optional,Integer> one;
	@Before
	public void setUp() throws Exception {


		just = AnyMs.liftM(CompletableFutures.ofResult(10), optional.INSTANCE);
		none = CompletableFutureT.of(AnyM.ofNullable(null));
        AnyMValue<optional, CompletableFuture<Integer>> x = AnyM.ofNullable(CompletableFuture.completedFuture(1));
		one = CompletableFutureT.of(x);
	}

	@Test
	public void optionalVMaybe(){


	    Optional.of(10)
	            .map(i->print("optional " + (i+10)));

	    Maybe.just(10)
	         .map(i->print("maybe " + (i+10)));

	}




	private int add1(int i){
		return i+1;
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

		assertThat(just.flatMap(i->CompletableFuture.completedFuture(i+5)).orElse(-1),equalTo(15));
		assertThat(none.flatMap(i->CompletableFuture.completedFuture(i+5)).orElse(-1),equalTo(-1));
	}

	@Test
	public void testWhenFunctionOfQsuperTQextendsRSupplierOfQextendsR() {

		assertThat(just.fold(i->i+1,()->20),equalTo(AnyM.ofNullable(11)));
		assertThat(none.fold(i->i+1,()->20),equalTo(AnyM.ofNullable(null)));
	}



	@Test
	public void testStream() {
		assertThat(just.stream().to(ReactiveConvertableSequence::converter).listX(),equalTo(ListX.of(10)));
		assertThat(none.stream().to(ReactiveConvertableSequence::converter).listX(),equalTo(ListX.of()));
	}

	@Test
	public void testOfSupplierOfT() {

	}

	@Test
    public void testConvertTo() {
        AnyM<optional,Stream<Integer>> toStream = just.fold(m->Stream.of(m),()->Stream.of());

        assertThat(toStream.stream().flatMap(i->i).collect(Collectors.toList()),equalTo(ListX.of(10)));
    }




	@Test
	public void testIterate() {
		assertThat(just.iterate(i->i+1,-1000).to(Witness::optional).get().limit(10).sumInt(i->(int)i),equalTo(145));
	}

	@Test
	public void testGenerate() {
		assertThat(just.generate(-10000).to(Witness::optional).get().limit(10).sumInt(i->i),equalTo(100));
	}

	@Test
	public void testMapReduceReducerOfE() {
		assertThat(just.foldMap(Reducers.toCountInt()),equalTo(1));
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
			assertThat(none.mkString(),equalTo("CompletableFutureT[Optional.empty]"));
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
		assertThat(just.foldMap(ReactiveReducers.toLinkedListX()),equalTo(LinkedListX.of(10)));
	}

	@Test
	public void testMapReduceFunctionOfQsuperTQextendsRMonoidOfR() {
		assertThat(just.foldMap(s->s.toString(), Monoid.of("", Semigroups.stringJoin(","))),equalTo(",10"));
	}

	@Test
	public void testReduceMonoidOfT() {
		assertThat(just.foldLeft(Monoid.of(1, Semigroups.intMult)),equalTo(10));
	}

	@Test
	public void testReduceBinaryOperatorOfT() {
		assertThat(just.foldLeft((a,b)->a+b),equalTo(Option.of(10)));
	}

	@Test
	public void testReduceTBinaryOperatorOfT() {
		assertThat(just.foldLeft(10,(a,b)->a+b),equalTo(20));
	}

	@Test
	public void testReduceUBiFunctionOfUQsuperTUBinaryOperatorOfU() {
		assertThat(just.foldLeft(11,(a,b)->a+b,(a,b)->a*b),equalTo(21));
	}

	@Test
	public void testReduceStreamOfQextendsMonoidOfT() {
		Seq<Integer> countAndTotal = just.foldLeft(ListX.of(Reducers.toCountInt(),Reducers.toTotalInt()));
		assertThat(countAndTotal,equalTo(Seq.of(1,10)));
	}

	@Test
	public void testReduceIterableOfReducerOfT() {
		Seq<Integer> countAndTotal = just.foldLeft(ListX.of(Reducers.toCountInt(),Reducers.toTotalInt()));
		assertThat(countAndTotal,equalTo(Seq.of(1,10)));
	}



	@Test
	public void testFoldRightMonoidOfT() {
		assertThat(just.foldRight(Monoid.of(1, Semigroups.intMult)),equalTo(10));
	}

	@Test
	public void testFoldRightTBinaryOperatorOfT() {
		assertThat(just.foldRight(10,(a,b)->a+b),equalTo(20));
	}

	@Test
	public void testFoldRightMapToType() {
		assertThat(just.foldMapRight(ReactiveReducers.toLinkedListX()),equalTo(LinkedListX.of(10)));
	}



	@Test
	public void testWhenFunctionOfQsuperMaybeOfTQextendsR() {


	    String match = Maybe.just("data is present")
	                        .fold(present->"hello", ()->"missing");



		assertThat(just.fold(s->"hello", ()->"world"),equalTo(AnyM.ofNullable("hello")));
		//none remains none as visit is on the Future not the Optional
		assertThat(none.fold(s->"hello", ()->"world"),equalTo(AnyM.ofNullable(null)));
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



		just.get();
		assertThat(capture.get(),equalTo(10));
	}



}
