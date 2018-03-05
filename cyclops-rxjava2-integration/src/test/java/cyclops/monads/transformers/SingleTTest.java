package cyclops.monads.transformers;


import com.oath.cyclops.types.mixins.Printable;
import com.oath.cyclops.util.box.Mutable;
import cyclops.collections.immutable.LinkedListX;
import cyclops.collections.mutable.ListX;
import cyclops.companion.Reducers;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;
import cyclops.control.Try;
import cyclops.control.Either;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;

import cyclops.monads.transformers.rx2.SingleT;
import cyclops.reactive.ReactiveSeq;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;


import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static cyclops.control.Maybe.just;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;


public class SingleTTest implements Printable {

	SingleT<Witness.optional,Integer> just;
	SingleT<Witness.optional,Integer> none;
	SingleT<Witness.optional,Integer> one;
	@Before
	public void setUp() throws Exception {


		just = SingleT.of(AnyM.ofNullable(Single.just(10)));
		none = SingleT.of(AnyM.ofNullable(null));
		one = SingleT.of(AnyM.ofNullable(Single.just(1)));
	}

	@Test
	public void optionalVMaybe(){


	    Optional.of(10)
	            .map(i->print("optional " + (i+10)));

	    just(10)
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
		assertFalse(Maybe.ofNullable(null).stream().size()>0);
		assertThat(Maybe.ofNullable(1),equalTo(Maybe.of(1)));

	}

	@Test
	public void testNarrow() {
		assertThat(Maybe.ofNullable(1),equalTo(Maybe.narrow(Maybe.of(1))));
	}






	@Test
	public void testMapFunctionOfQsuperTQextendsR() {
		assertThat(just.map(i->i+5).get(),equalTo(just(15)));
		assertThat(none.map(i->i+5).orElse(1000),equalTo(1000));
	}

	@Test
	public void testFlatMap() {

		assertThat(just.flatMap(i-> Maybe.of(i+5)).get(),equalTo(just(15)));
		assertThat(none.flatMap(i-> Maybe.of(i+5)).orElse(-1),equalTo(-1));
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
		assertThat(just.mkString(),containsString("SingleT[Optional[io.reactivex.internal.operators.single.SingleJust"));
		assertThat(none.mkString(),equalTo("SingleT[Optional.empty]"));
	}


	@Test
	public void testGet() {
		assertThat(just.get(),equalTo(just(10)));
	}
	@Test
	public void testGetNone() {
		assertFalse(none.get().isPresent());

	}

	@Test
	public void testFilter() {

		assertFalse(just.filter(i->i<5).stream().size()>0);
		assertTrue(just.filter(i->i>5).stream().size()>0);
		assertFalse(none.filter(i->i<5).stream().size()>0);
		assertFalse(none.filter(i->i>5).stream().size()>0);

	}

	@Test
	public void testOfType() {
		assertFalse(just.ofType(String.class).stream().size()>0);
		assertTrue(just.ofType(Integer.class).stream().size()>0);
		assertFalse(none.ofType(String.class).stream().size()>0);
		assertFalse(none.ofType(Integer.class).stream().size()>0);
	}

	@Test
	public void testFilterNot() {

		assertTrue(just.filterNot(i->i<5).stream().size()>0);
		assertFalse(just.filterNot(i->i>5).stream().size()>0);
		assertFalse(none.filterNot(i->i<5).stream().size()>0);
		assertFalse(none.filterNot(i->i>5).stream().size()>0);
	}

	@Test
	public void testNotNull() {
		assertTrue(just.notNull().stream().size()>0);
		assertFalse(none.notNull().stream().size()>0);

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
		assertThat(just.mapReduce(s->s.toString(), Monoid.of("", Semigroups.stringJoin(","))),equalTo(",10"));
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
		ListX<Integer> countAndTotal = just.reduce(Stream.of(Reducers.toCountInt(), Reducers.toTotalInt()));
		assertThat(countAndTotal,equalTo(ListX.of(1,10)));
	}

	@Test
	public void testReduceIterableOfReducerOfT() {
		ListX<Integer> countAndTotal = just.reduce(Arrays.asList(Reducers.toCountInt(), Reducers.toTotalInt()));
		assertThat(countAndTotal,equalTo(ListX.of(1,10)));
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
		assertThat(just.foldRightMapToType(Reducers.toLinkedListX()),equalTo(LinkedListX.of(10)));
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
		assertThat(just.map(i->i+5).get(),equalTo(just(15)));
	}

	@Test
	public void testPeek() {
		Mutable<Integer> capture = Mutable.of(null);
		just = just.peek(c->capture.set(c)).map(i->i+2);



		just.get().orElse(-1);
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
