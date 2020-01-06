package cyclops.control.either;

import com.oath.cyclops.types.persistent.PersistentSet;
import com.oath.cyclops.util.box.Mutable;
import cyclops.control.Either;
import cyclops.control.Future;
import cyclops.control.Ior;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.control.Trampoline;
import cyclops.control.Try;
import cyclops.data.HashSet;

import cyclops.companion.Monoids;
import cyclops.companion.Reducers;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
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
import static org.junit.Assert.*;



public class Either2Test {

	Either<String,Integer> just;
	Either<String,Integer> none;
	@Before
	public void setUp() throws Exception {
		just = Either.right(10);
		none = Either.left("none");
	}

  @Test
  public void filterAlt(){
    assertThat(Either.right(10).filter(i->i>100,r->"hello"),equalTo(Either.left("hello")));
    assertThat(Either.right(101).filter(i->i>100,r->"hello"),equalTo(Either.right(101)));
    assertThat(Either.<Integer,Integer>left(101).filter(i->i>100,r->-1),equalTo(Either.left(101)));
  }

	@Test
    public void testSequenceSecondary() {
    Either<Integer, ReactiveSeq<String>> xors = Either.sequenceLeft(Arrays.asList(just, none, Either.right(1)));
        assertThat(xors.map(s->s.toList()),equalTo(Either.right(Arrays.asList("none"))));
    }

    @Test
    public void testAccumulateSecondary2() {
        Either<?,PersistentSet<String>> xors = Either.accumulateLeft(Arrays.asList(just,none, Either.right(1)),Reducers.<String>toPersistentSet());
        assertThat(xors,equalTo(Either.right(HashSet.of("none"))));
    }

    @Test
    public void testAccumulateSecondarySemigroup() {
        Either<?,String> xors = Either.accumulateLeft(Arrays.asList(just,none, Either.left("1")), i->""+i, Monoids.stringConcat);
        assertThat(xors,equalTo(Either.right("none1")));
    }
    @Test
    public void testAccumulateSecondarySemigroupIntSum() {
        Ior<?,Integer> iors = Ior.accumulateLeft(Monoids.intSum,Arrays.asList(Ior.both(2, "boo!"),Ior.left(1)));
        assertThat(iors,equalTo(Ior.right(3)));
    }

	@Test
    public void nest(){
       assertThat(just.nest().map(m->m.toOptional().get()),equalTo(just));
       assertThat(none.nest().map(m->m.get()),equalTo(none));
    }

	@Test
	public void visit(){

	    assertThat(just.fold(secondary->"no", primary->"yes"),equalTo("yes"));
	    assertThat(none.fold(secondary->"no", primary->"yes"),equalTo("no"));
	}
	@Test
    public void visitXor(){
        assertThat(just.bimap(secondary->"no", primary->"yes"),equalTo(Either.right("yes")));
        assertThat(none.bimap(secondary->"no", primary->"yes"),equalTo(Either.left("no")));
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
	public void testOfT() {
		assertThat(Ior.right(1),equalTo(Ior.right(1)));
	}



	@Test
	public void testSequence() {
		Either.sequenceRight(Arrays.asList(just, Either.right(1))).printOut();
    Either<String, ReactiveSeq<Integer>> maybes = Either.sequenceRight(Arrays.asList(just, none, Either.right(1)));
		assertThat(maybes.map(s->s.toList()),equalTo(Either.right(Arrays.asList(10,1))));
	}

	@Test
	public void testAccumulateJustCollectionXOfMaybeOfTReducerOfR() {
		Either<?,PersistentSet<Integer>> maybes = Either.accumulateRight(Arrays.asList(just,none, Either.right(1)),Reducers.toPersistentSet());
		assertThat(maybes,equalTo(Either.right(HashSet.of(10,1))));
	}

	@Test
	public void testAccumulateJustCollectionXOfMaybeOfTFunctionOfQsuperTRSemigroupOfR() {
		Either<?,String> maybes = Either.accumulateRight(Arrays.asList(just,none, Either.right(1)), i->""+i,Monoids.stringConcat);
		assertThat(maybes,equalTo(Either.right("101")));
	}
	@Test
	public void testAccumulateJust() {
		Either<?,Integer> maybes = Either.accumulateRight(Monoids.intSum,Arrays.asList(just,none, Either.right(1)));
		assertThat(maybes,equalTo(Either.right(11)));
	}
	@Test
    public void testAccumulateSecondary() {
        Either<?,String> maybes = Either.accumulateLeft(Monoids.stringConcat,Arrays.asList(just,none, Either.left("hello")));
        assertThat(maybes,equalTo(Either.right("nonehello")));
    }

	@Test
	public void testUnitT() {
		assertThat(just.unit(20),equalTo(Either.right(20)));
	}



	@Test
	public void testisPrimary() {
		assertTrue(just.isRight());
		assertFalse(none.isRight());
	}


	@Test
	public void testMapFunctionOfQsuperTQextendsR() {
		assertThat(just.map(i->i+5),equalTo(Either.right(15)));
		assertThat(none.map(i->i+5),equalTo(Either.left("none")));
	}

	@Test
	public void testFlatMap() {
		assertThat(just.flatMap(i-> Either.right(i+5)),equalTo(Either.right(15)));
		assertThat(none.flatMap(i-> Either.right(i+5)),equalTo(Either.left("none")));
	}

	@Test
	public void testWhenFunctionOfQsuperTQextendsRSupplierOfQextendsR() {
		assertThat(just.fold(i->i+1,()->20),equalTo(11));
		assertThat(none.fold(i->i+1,()->20),equalTo(20));
	}


	@Test
	public void testStream() {
		assertThat(just.stream().toList(),equalTo(Arrays.asList(10)));
		assertThat(none.stream().toList(),equalTo(Arrays.asList()));
	}

	@Test
	public void testOfSupplierOfT() {

	}

	@Test
    public void testConvertTo() {

        Stream<Integer> toStream = just.fold(m->Stream.of(m),()->Stream.of());
        assertThat(toStream.collect(Collectors.toList()),equalTo(Arrays.asList(10)));
    }


    @Test
    public void testConvertToAsync() {
        Future<Stream<Integer>> async = Future.of(()->just.fold(f->Stream.of((int)f),()->Stream.of()));

        assertThat(async.orElse(Stream.empty()).collect(Collectors.toList()),equalTo(Arrays.asList(10)));
    }

	@Test
	public void testIterate() {
		assertThat(just.asSupplier(-1000).iterate(i->i+1).limit(10).sumInt(i->i),equalTo(145));
	}

	@Test
	public void testGenerate() {
		assertThat(just.asSupplier(-1000).generate().limit(10).sumInt(i->i),equalTo(100));
	}




	@Test
	public void testToXor() {
		assertThat(just.toEither(-5000),equalTo(Either.right(10)));

	}
	@Test
	public void testToXorNone(){
		Either<String,Integer> xor = none;
		assertTrue(xor.isLeft());
		assertThat(xor,equalTo(Either.left("none")));

	}


	@Test
	public void testToXorSecondary() {
		assertThat(just.toEither(-5000).swap(),equalTo(Either.left(10)));
	}

	@Test
	public void testToXorSecondaryNone(){
		Either<Integer,String> xorNone = none.swap();
		assertThat(xorNone,equalTo(Either.right("none")));

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
		assertThat(just.toIor(),equalTo(Ior.right(10)));

	}
	@Test
	public void testToIorNone(){
		Ior<String,Integer> ior = none.toIor();
		assertTrue(ior.isLeft());
        assertThat(ior,equalTo(Ior.left("none")));

	}


	@Test
	public void testToIorSecondary() {
		assertThat(just.toIor().swap(),equalTo(Ior.left(10)));
	}


	@Test
	public void testToIorSecondaryNone(){
	    Ior<Integer,String> ior = none.toIor().swap();
        assertTrue(ior.isRight());
        assertThat(ior,equalTo(Ior.right("none")));

	}



	@Test
	public void testMkString() {
		assertThat(just.mkString(),equalTo("Either.right[10]"));
		assertThat(none.mkString(),equalTo("Either.left[none]"));
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
	public void testFoldRightMonoidOfT() {
		assertThat(just.fold(Monoid.of(1,Semigroups.intMult)),equalTo(10));
	}



	@Test
	public void testWhenFunctionOfQsuperMaybeOfTQextendsR() {
		assertThat(just.fold(s->"hello", ()->"world"),equalTo("hello"));
		assertThat(none.fold(s->"hello", ()->"world"),equalTo("world"));
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
		assertThat(just.map(i->i+5),equalTo(Either.right(15)));
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
	public void testUnitT1() {
		assertThat(none.unit(10),equalTo(just));
	}

}
