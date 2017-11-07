package cyclops.control;

import cyclops.collections.immutable.PersistentSetX;
import cyclops.companion.Monoids;
import cyclops.companion.Reducers;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.async.Future;
import cyclops.async.LazyReact;
import com.oath.cyclops.util.box.Mutable;
import cyclops.collections.mutable.ListX;
import cyclops.control.Eval.CompletableEval;
import cyclops.control.Eval.Module.Later;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;


public class CompletableEvalTest {

	public static CompletableEval<Integer,Integer> now(Integer v){
	    CompletableEval<Integer,Integer> completable = Eval.eval();
	    completable.complete(v);
	    return completable;
    }
	Eval<Integer> just;
	Eval<Integer> none;
	@Before
	public void setUp() throws Exception {
		just = now(10);
		none = now(null);
	}



    @Test
    public void coFlatMap(){
        assertThat(just.coflatMap(m-> m.isPresent()? m.toOptional().get() : 50),equalTo(Eval.now(10)));
        assertThat(none.coflatMap(m-> m.isPresent()? m.toOptional().get() : 50),equalTo(Eval.now(50)));

    }
    @Test
    public void combine(){


        Monoid<Integer> add = Monoid.of(0,Semigroups.intSum);
        assertThat(just.combineEager(add,Eval.now(10)),equalTo(Eval.now(20)));



        Monoid<Integer> firstNonNull = Monoid.of(null , Semigroups.firstNonNull());
        assertThat(just.combineEager(firstNonNull,none),equalTo(just));

    }
	@Test
	public void testToMaybe() {
		assertThat(just.toMaybe(),equalTo(Maybe.of(10)));
		assertThat(none.toMaybe(),equalTo(Maybe.nothing()));
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
		assertThat(Maybe.fromEval(CompletableEvalTest.now(10)),equalTo(just.toMaybe()));
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
		Eval<ReactiveSeq<Integer>> maybes =Eval.sequence(ListX.of(just,CompletableEvalTest.now(1)));
		assertThat(maybes,equalTo(Eval.now(ListX.of(10,1))));
	}


	@Test
	public void testAccumulateJustCollectionXOfMaybeOfTReducerOfR() {


		Eval<PersistentSetX<Integer>> maybes =Eval.accumulate(ListX.of(just,CompletableEvalTest.now(1)),Reducers.toPersistentSetX());
		assertThat(maybes,equalTo(Eval.now(PersistentSetX.of(10,1))));
	}

	@Test
	public void testAccumulateJustCollectionXOfMaybeOfTFunctionOfQsuperTRSemigroupOfR() {
		Eval<String> maybes =Eval.accumulate(ListX.of(just,Eval.later(()->1)),i->""+i,Monoids.stringConcat);
		assertThat(maybes,equalTo(Eval.now("101")));
	}
	@Test
	public void testAccumulateJust() {
		Eval<Integer> maybes =Eval.accumulate(Monoids.intSum,ListX.of(just,CompletableEvalTest.now(1)));
		assertThat(maybes,equalTo(Eval.now(11)));
	}

	@Test
	public void testUnitT() {
		assertThat(just.unit(20),equalTo(CompletableEvalTest.now(20)));
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
		assertThat(just.map(i->i+5),equalTo(CompletableEvalTest.now(15)));
		assertThat(none.toMaybe().map(i->i+5),equalTo(Maybe.nothing()));
	}

	@Test
	public void testFlatMap() {
		assertThat(just.flatMap(i->CompletableEvalTest.now(i+5)),equalTo(Eval.later(()->15)));
		assertThat(none.toMaybe().flatMap(i->Maybe.of(i+5)),equalTo(Maybe.nothing()));
	}

	@Test
	public void testWhenFunctionOfQsuperTQextendsRSupplierOfQextendsR() {
		assertThat(just.visit(i->i+1,()->20),equalTo(11));
		assertThat(none.visit(i->i+1,()->20),equalTo(20));
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
        Future<Stream<Integer>> async = Future.of(()->just.visit(f->Stream.of((int)f),()->Stream.of()));

        assertThat(async.orElse(Stream.empty()).collect(Collectors.toList()),equalTo(ListX.of(10)));
    }


	@Test
	public void testIterate() {
		assertThat(just.asSupplier(-10000).iterate(i->i+1).limit(10).sumInt(i->i),equalTo(
						Stream.iterate(just.get(),i->i+1).limit(10).mapToInt(i->i).sum()));
		assertThat(just.asSupplier(-10000).iterate(i->i+1).limit(10).sumInt(i->i),equalTo(145));
	}

	@Test
	public void testGenerate() {
		assertThat(just.generate().limit(10).sumInt(i->i),equalTo(100));
	}




	@Test
	public void testToXor() {
		assertThat(just.toEither(-5000),equalTo(Either.right(10)));

	}
	@Test
	public void testToXorNone(){
	    Either<?,Integer> empty = none.toEither(-50000);


        assertTrue(empty.swap().map(__->10).toOptional().get()==10);

	}


	@Test
	public void testToXorSecondary() {
		assertThat(just.toEither(-5000).swap(),equalTo(Either.left(10)));
	}

	@Test
	public void testToXorSecondaryNone(){
		Either<Integer,?> empty = none.toEither(-50000).swap();
		assertTrue(empty.isRight());
		assertThat(empty.map(__->10),equalTo(Either.right(10)));


	}
	@Test
	public void testToTry() {
		assertTrue(none.toTry().isFailure());
		assertThat(just.toTry(),equalTo(Try.success(10)));
		assertTrue(Try.fromPublisher(none).isFailure());
        assertThat(Try.fromPublisher(just),equalTo(Try.success(10)));
	}

	@Test
	public void testToTryClassOfXArray() {
		assertFalse(none.toTry(Throwable.class).isSuccess());
	}


	@Test
	public void testToIorNone(){
	    Either<Integer,?> empty = none.toEither(-50000).swap();
        assertTrue(empty.isRight());
        assertThat(empty.map(__->10),equalTo(Either.right(10)));

	}





	@Test
	public void testMkString() {
		assertThat(just.mkString(),equalTo("CompletableEval[10]"));
		assertThat(none.mkString(),equalTo("CompletableEval[]"));
	}
	LazyReact react = new LazyReact();




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





	private int add(int a, int b){
		return a+b;
	}


	@Test
    public void testZipEval() {
        assertThat(just.zip(Eval.later(()->20),this::add),equalTo(CompletableEvalTest.now(30)));
    }
    @Test
    public void testZipEvalLazy(){
        assertTrue(Eval.later(()->10).zip(Eval.later(()->20),this::add) instanceof Later);
    }
    @Test
    public void testZipPubEval() {
        assertThat(just.zip(Eval.later(()->20),this::add),equalTo(CompletableEvalTest.now(30)));
    }
    @Test
    public void testZipPubEvalLazy(){
        assertTrue(Eval.later(()->10).zip(this::add, Eval.later(()->20)) instanceof Later);
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
	public void testFoldRightMonoidOfT() {
		assertThat(just.fold(Monoid.of(1,Semigroups.intMult)),equalTo(10));
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
		assertThat(none.stream().collect(Collectors.toList()).size(),equalTo(1));
		assertThat(just.stream().collect(Collectors.toList()).size(),equalTo(1));

	}


	@Test
	public void testOrElse() {
		assertThat(none.orElse(20),equalTo(20));
		assertThat(just.orElse(20),equalTo(10));
	}



	@Test
	public void testToFuture() {
		Future<Integer> cf = just.toFuture();
		assertThat(cf.get(),equalTo(Try.success(10)));
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
		assertThat(just.map(i->i+5),equalTo(CompletableEvalTest.now(15)));
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
		assertThat(just.trampoline(n ->sum(10,n)),equalTo(CompletableEvalTest.now(65)));
	}



	@Test
	public void testUnitT1() {
		assertThat(none.unit(10),equalTo(just));
	}

}
