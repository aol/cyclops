package cyclops.monads.anym.value;


import com.oath.cyclops.anym.AnyMValue;
import com.oath.cyclops.anym.AnyMValue2;
import com.oath.cyclops.ReactiveConvertableSequence;
import com.oath.cyclops.util.box.Mutable;
import cyclops.companion.Semigroups;
import cyclops.control.Future;
import cyclops.data.Seq;
import cyclops.futurestream.LazyReact;
import cyclops.control.*;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;
import cyclops.function.Monoid;
import cyclops.companion.Reducers;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.collections.mutable.ListX;

import cyclops.companion.Streams;
import cyclops.monads.AnyM;
import cyclops.monads.Matchers;
import cyclops.monads.WitnessType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;



public abstract class BaseAnyMValueTest<W extends WitnessType<W>> {

	protected AnyMValue<W,Integer> just;
	protected AnyMValue<W,Integer> none;



	@Test
	public void testToMaybe() {
		assertThat(just.toMaybe(),equalTo(Maybe.of(10)));
		assertThat(none.toMaybe(),equalTo(Maybe.nothing()));
	}

	@Test
    public void nest(){
       assertThat(just.nest().map(m->m.orElse(-2000)), Matchers.equivalent(just));

    }
    @Test
    public void coFlatMap(){

        assertThat(just.coflatMap(m-> m.isPresent()? m.orElse(-4000) : 50), Matchers.equivalent(just));
        assertThat(none.coflatMap(m-> m.isPresent()? m.orElse(-5000) : just.orElse(-4000)), Matchers.equivalent(just));
    }
    @Test
    public void combine(){

        Monoid<Integer> add = Monoid.of(0, Semigroups.intSum);
/**
        assertThat(none.combineEager(add,just).toTry(),equalTo(Try.success(0)));
        assertThat(none.combineEager(add,none).toTry(),equalTo(Try.success(0)));
        assertThat(just.combineEager(add,just).toTry(),equalTo(Try.success(20)));
 **/
          //  just.combineEager(add,none).printOut();
        System.out.println("None " + none);
	    assertThat(just.combineEager(add,none), Matchers.equivalent(just));


    }
	private int add1(int i){
		return i+1;
	}


	@Test
	public void testFromOptional() {
		assertThat(Maybe.fromOptional(Optional.of(10)),equalTo(just.toMaybe()));
	}

	@Test
	public void testFromEvalSome() {
		assertThat(Maybe.fromEval(Eval.now(10)),equalTo(just.toMaybe()));
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
		assertThat(just.unit(20).toMaybe(),equalTo(Maybe.of(20)));
	}



	@Test
	public void testIsPresent() {
		assertTrue(just.toMaybe().isPresent());
		assertFalse(none.toOptional().isPresent());
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
		assertThat(just.map(i->i+5).orElse(-400),equalTo(Maybe.of(15).orElse(-40000)));
		assertThat(none.map(i->i+5).toMaybe(),equalTo(Maybe.nothing()));
	}

	@Test
	public void testFlatMap() {
		assertThat(just.flatMap(i-> AnyM.ofNullable(i+5)).toMaybe(),equalTo(Maybe.of(15)));
		assertThat(none.flatMap(i->AnyM.ofNullable(i+5)).toMaybe(),equalTo(Maybe.nothing()));
	}

	@Test
	public void testWhenFunctionOfQsuperTQextendsRSupplierOfQextendsR() {
		assertThat(just.visit(i->i+1,()->20),equalTo(11));
		assertThat(none.visit(i->i+1,()->20),equalTo(20));
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
		Stream<Integer> toStream = just.visit(m->Stream.of(m),()->Stream.of());
		assertThat(toStream.collect(Collectors.toList()),equalTo(ListX.of(10)));
	}

	@Test
	public void testConvertToAsync() {
		Future<Stream<Integer>> async = Future.of(()->just.visit(f->Stream.of((int)f),()->Stream.of()));

		assertThat(async.orElse(Stream.empty()).collect(Collectors.toList()),equalTo(ListX.of(10)));
	}


	@Test
	public void testIterate() {
		assertThat(just.asSupplier(1000).iterate(i->i+1).limit(10).sumInt(i->i),equalTo(145));
	}

	@Test
	public void testGenerate() {
		assertThat(just.asSupplier(400000).generate().limit(10).sumInt(i->i),equalTo(100));
	}

	@Test
	public void testMapReduceReducerOfE() {
		assertThat(just.mapReduce(Reducers.toCountInt()),equalTo(1));
	}


	@Test
	public void testToXor() {
		assertThat(just.toEither(1000000),equalTo(Either.right(10)));

	}
	@Test
	public void testToXorNone(){
	    Either<?,Integer> empty = none.toEither(1000000);


        assertTrue(empty.swap().map(__->10).orElse(-10000)==10);

	}


	@Test
	public void testToXorSecondary() {
		assertThat(just.toEither(1000000).swap(),equalTo(Either.left(10)));
	}

	@Test
	public void testToXorSecondaryNone(){
		Either<Integer,?> empty = none.toEither(100000).swap();
		assertTrue(empty.isRight());
		assertThat(empty.map(__->10),equalTo(Either.right(10)));


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
	public void testMkString() {
		if(just instanceof AnyMValue2)
		    assertThat(just.mkString(),equalTo("AnyMValue2[10]"));
		else
            assertThat(just.mkString(),equalTo("AnyMValue[10]"));
        if(none instanceof AnyMValue2)
		    assertThat(none.mkString(),equalTo("AnyMValue2[]"));
        else
            assertThat(none.mkString(),equalTo("AnyMValue[]"));
	}
	@Test
    public void testToString() {
        if(just instanceof AnyMValue2)
            assertThat(just.toString(),equalTo("AnyMValue2[10]"));
        else
            assertThat(just.mkString(),equalTo("AnyMValue[10]"));
        if(none instanceof AnyMValue2)
            assertThat(none.toString(),equalTo("AnyMValue2[]"));
        else
            assertThat(none.mkString(),equalTo("AnyMValue[]"));
    }
	LazyReact react = new LazyReact();

	@Test
	public void testGet() {
		assertThat(just.get(),equalTo(Option.some(10)));
	}


	@Test
	public void testFilter() {
		if(!just.adapter().isFilterable())
			return;
		assertFalse(just.filter(i->i<5).toMaybe().isPresent());
		assertTrue(just.filter(i->i>5).toMaybe().isPresent());
		assertFalse(none.filter(i->i<5).toMaybe().isPresent());
		assertFalse(none.filter(i->i>5).toMaybe().isPresent());

	}

	@Test
	public void testOfType() {
		if(!just.adapter().isFilterable())
			return;
		assertFalse(just.ofType(String.class).toMaybe().isPresent());
		assertTrue(just.ofType(Integer.class).toMaybe().isPresent());
		assertFalse(none.ofType(String.class).toMaybe().isPresent());
		assertFalse(none.ofType(Integer.class).toMaybe().isPresent());
	}

	@Test
	public void testFilterNot() {
		if(!just.adapter().isFilterable())
			return;
		assertTrue(just.filterNot(i->i<5).toMaybe().isPresent());
		assertFalse(just.filterNot(i->i>5).toMaybe().isPresent());
		assertFalse(none.filterNot(i->i<5).toMaybe().isPresent());
		assertFalse(none.filterNot(i->i>5).toMaybe().isPresent());
	}

	@Test
	public void testNotNull() {
		if(!just.adapter().isFilterable())
			return;
		assertTrue(just.notNull().toMaybe().isPresent());
		assertFalse(none.notNull().toMaybe().isPresent());

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
		Seq<Integer> countAndTotal = just.reduce(ReactiveSeq.of(Reducers.toCountInt(),Reducers.toTotalInt()));
		assertThat(countAndTotal,equalTo(Seq.of(1,10)));
	}

	@Test
	public void testReduceIterableOfReducerOfT() {
        Seq<Integer> countAndTotal = just.reduce(Arrays.asList(Reducers.toCountInt(),Reducers.toTotalInt()));
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
		assertThat(none.stream().collect(Collectors.toList()).size(),equalTo(0));
		assertThat(just.stream().collect(Collectors.toList()).size(),equalTo(1));

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
		assertThat(just.map(i->i+5).toMaybe(),equalTo(Maybe.of(15)));
	}

	@Test
	public void testPeek() {
		Mutable<Integer> capture = Mutable.of(null);
		just = just.peek(c->capture.set(c));


		just.get();
		assertThat(capture.get(),equalTo(10));
	}

	private Trampoline<Integer> sum(int times, int sum){
		return times ==0 ?  Trampoline.done(sum) : Trampoline.more(()->sum(times-1,sum+times));
	}
	@Test
	public void testTrampoline() {
		assertThat(just.trampoline(n ->sum(10,n)).toMaybe(),equalTo(Maybe.of(65)));
	}



	@Test
	public void testUnitT1() {
		assertThat(none.unit(10),Matchers.equivalent(just));
	}

}
