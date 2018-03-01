package cyclops.monads.transformers;
import com.oath.cyclops.ReactiveConvertableSequence;
import cyclops.ReactiveReducers;
import cyclops.companion.Semigroups;
import cyclops.data.Seq;
import cyclops.monads.AnyMs;
import cyclops.monads.Witness.*;

import com.oath.cyclops.types.mixins.Printable;
import com.oath.cyclops.util.box.Mutable;
import cyclops.reactive.collections.immutable.LinkedListX;
import cyclops.reactive.collections.mutable.ListX;
import cyclops.companion.Reducers;
import cyclops.companion.Streams;
import cyclops.control.*;
import cyclops.control.LazyEither;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;


public class XorTTest implements Printable {

	EitherT<optional,Throwable,Integer> just;
	EitherT<optional,Throwable,Integer> none;
	EitherT<optional,Throwable,Integer> one;
	@Before
	public void setUp() throws Exception {


		just = AnyMs.liftM(Either.<Throwable,Integer>right(10),optional.INSTANCE);
		none = EitherT.of(AnyM.ofNullable(null));
		one = EitherT.of(AnyM.ofNullable(LazyEither.right(1)));

	}

	@Test
	public void optionalVMaybe(){


	    Optional.of(10)
	            .map(i->print("optional " + (i+10)));

	    Either.right(10)
	         .map(i->print("maybe " + (i+10)));

	}





	private int add1(int i){
		return i+1;
	}

	@Test
	public void testOfT() {
		assertThat(Either.right(1),equalTo(Either.right(1)));
	}





	@Test
	public void testUnitT() {
		assertThat(just.unit(20).get(),equalTo(Maybe.just(20)));
	}





	@Test
	public void testMapFunctionOfQsuperTQextendsR() {
		assertThat(just.map(i->i+5).get(),equalTo(Option.some(15)));
		assertThat(none.map(i->i+5).orElse(1000),equalTo(1000));
	}

	@Test
	public void testFlatMap() {

		assertThat(just.flatMap(i-> Either.right(i+5)).get(),equalTo(Option.some(15)));
		assertThat(none.flatMap(i-> Either.right(i+5)).orElse(-1),equalTo(-1));
	}

	@Test
	public void testWhenFunctionOfQsuperTQextendsRSupplierOfQextendsR() {

		assertThat(just.visit(i->i+1,()->20),equalTo(AnyM.ofNullable(11)));
		assertThat(none.visit(i->i+1,()->20),equalTo(AnyM.ofNullable(null)));
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
        AnyM<optional,Stream<Integer>> toStream = just.visit(m->Stream.of(m),()->Stream.of());

        assertThat(toStream.stream().flatMap(i->i).collect(Collectors.toList()),equalTo(ListX.of(10)));
    }




	@Test
	public void testIterate() {
		assertThat(just.iterate(i->i+1,-1000).stream().limit(10).sumInt(i->(int)i),equalTo(145));
	}

	@Test
	public void testGenerate() {
		assertThat(just.generate(-10000).stream().limit(10).sumInt(i->i),equalTo(100));
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
		assertThat(just.mkString(),equalTo("XorT[Optional[Either.right[10]]]"));
		assertThat(none.mkString(),equalTo("XorT[Optional.empty]"));
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
		assertThat(just.mapReduce(ReactiveReducers.toLinkedListX()),equalTo(LinkedListX.of(10)));
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
		Seq<Integer> countAndTotal = just.reduce(ListX.of(Reducers.toCountInt(),Reducers.toTotalInt()));
		assertThat(countAndTotal,equalTo(Seq.of(1,10)));
	}

	@Test
	public void testReduceIterableOfReducerOfT() {
		Seq<Integer> countAndTotal = just.reduce(ListX.of(Reducers.toCountInt(),Reducers.toTotalInt()));
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
		assertThat(just.foldRightMapToType(ReactiveReducers.toLinkedListX()),equalTo(LinkedListX.of(10)));
	}



	@Test
	public void testWhenFunctionOfQsuperMaybeOfTQextendsR() {


	    String match = Either.right("data is present")
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
	public void testMapFunctionOfQsuperTQextendsR1() {
		assertThat(just.map(i->i+5).get(),equalTo(Option.some(15)));
	}

	@Test
	public void testPeek() {
		Mutable<Integer> capture = Mutable.of(null);
		just = just.peek(c->capture.set(c)).map(i->i+2);



		just.get();
		assertThat(capture.get(),equalTo(10));
	}

	private Trampoline<Integer> sum(int times, int sum){
		return times ==0 ?  Trampoline.done(sum) : Trampoline.more(()->sum(times-1,sum+times));
	}
	@Test
	public void testTrampoline() {
		assertThat(just.trampoline(n ->sum(10,n)).get(),equalTo(Maybe.just(65)));
	}




}
