package cyclops.control;

import com.oath.cyclops.types.persistent.PersistentSet;
import com.oath.cyclops.util.box.Mutable;


import cyclops.companion.Monoids;
import cyclops.companion.Reducers;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.data.HashSet;
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



public class Ior2Test {

	Ior<String,Integer> just;
	Ior<String,Integer> none;
	@Before
	public void setUp() throws Exception {
		just = Ior.right(10);
		none = Ior.left("none");
	}
	static class Base{ }
	static class One extends EitherTest.Base { }
	static class Two extends EitherTest.Base {}
	@Test
	public void visitAny(){

		Ior<One,Two> test = Ior.right(new Two());
		test.to(Ior::applyAny).apply(b->b.toString());
		Ior.right(10).to(Ior::consumeAny).accept(System.out::println);
		Ior.right(10).to(e->Ior.visitAny(System.out::println,e));
		Object value = Ior.right(10).to(e->Ior.visitAny(e, x->x));
		assertThat(value,equalTo(10));
	}


	@Test
    public void nest(){
       assertThat(just.nest().map(m->m.toOptional().get()),equalTo(just));
       assertThat(none.nest().map(m->m.get()),equalTo(none));
    }
    @Test
    public void coFlatMap(){
        assertThat(just.coflatMap(m-> m.isPresent()? m.toOptional().get() : 50),equalTo(just));
        assertThat(none.coflatMap(m-> m.isPresent()? m.toOptional().get() : 50),equalTo(Ior.right(50)));
    }

	@Test
    public void visit(){
        assertThat(just.visit(secondary->"no", primary->"yes",(sec,pri)->"oops!"),equalTo("yes"));
        assertThat(none.visit(secondary->"no", primary->"yes",(sec,pri)->"oops!"),equalTo("no"));
        assertThat(Ior.both(10, "eek").visit(secondary->"no", primary->"yes",(sec,pri)->"oops!"),equalTo("oops!"));
    }
    @Test
    public void visitIor(){
        assertThat(just.bimap(secondary->"no", primary->"yes"),equalTo(Ior.right("yes")));
        assertThat(none.bimap(secondary->"no", primary->"yes"),equalTo(Ior.left("no")));
        assertThat(Ior.both(10, "eek").bimap(secondary->"no", primary->"yes"),equalTo(Ior.both("no","yes")));
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
    public void testSequenceSecondary() {
    Ior<Integer, ReactiveSeq<String>> iors = Ior.sequenceLeft(Arrays.asList(just, none, Ior.right(1)));
        assertThat(iors.map(s->s.toList()),equalTo(Ior.right(Arrays.asList("none"))));
    }

	@Test
    public void testAccumulateSecondary() {
        Ior<?,PersistentSet<String>> iors = Ior.accumulateLeft(Arrays.asList(just,none,Ior.right(1)), Reducers.<String>toPersistentSet());
        assertThat(iors,equalTo(Ior.right(HashSet.of("none"))));
    }

	@Test
    public void testAccumulateSecondarySemigroup() {
        Ior<?,String> iors = Ior.accumulateLeft(Arrays.asList(just,none,Ior.left("1")), i->""+i,Monoids.stringConcat);
        assertThat(iors,equalTo(Ior.right("none1")));
    }
	@Test
    public void testAccumulateSecondarySemigroupIntSum() {
        Ior<?,Integer> iors = Ior.accumulateLeft(Monoids.intSum,Arrays.asList(Ior.both(2, "boo!"),Ior.left(1)));
        assertThat(iors,equalTo(Ior.right(3)));
    }
	@Test
	public void testSequence() {
    Ior<String, ReactiveSeq<Integer>> maybes = Ior.sequenceRight(Arrays.asList(just, none, Ior.right(1)));
		assertThat(maybes.map(s->s.toList()),equalTo(Ior.right(Arrays.asList(10,1))));
	}

	@Test
	public void testAccumulateJustCollectionXOfMaybeOfTReducerOfR() {
		Ior<?,PersistentSet<Integer>> maybes =Ior.accumulateRight(Arrays.asList(just,none,Ior.right(1)),Reducers.toPersistentSet());
		assertThat(maybes,equalTo(Ior.right(HashSet.of(10,1))));
	}

	@Test
	public void testAccumulateJustCollectionXOfMaybeOfTFunctionOfQsuperTRSemigroupOfR() {
		Ior<?,String> maybes = Ior.accumulateRight(Arrays.asList(just,none,Ior.right(1)), i->""+i,Semigroups.stringConcat);
		assertThat(maybes,equalTo(Ior.right("101")));
	}
	@Test
	public void testAccumulateJust() {
		Ior<?,Integer> maybes =Ior.accumulateRight(Arrays.asList(just,none,Ior.right(1)),Semigroups.intSum);
		assertThat(maybes,equalTo(Ior.right(11)));
	}

	@Test
	public void testUnitT() {
		assertThat(just.unit(20),equalTo(Ior.right(20)));
	}



	@Test
	public void testisPrimary() {
		assertTrue(just.isRight());
		assertFalse(none.isRight());
	}


	@Test
	public void testMapFunctionOfQsuperTQextendsR() {
		assertThat(just.map(i->i+5),equalTo(Ior.right(15)));
		assertThat(none.map(i->i+5),equalTo(Ior.left("none")));
	}

	@Test
	public void testFlatMap() {
		assertThat(just.flatMap(i->Ior.right(i+5)),equalTo(Ior.right(15)));
		assertThat(none.flatMap(i->Ior.right(i+5)),equalTo(Ior.left("none")));
	}

	@Test
	public void testWhenFunctionOfQsuperTQextendsRSupplierOfQextendsR() {
		assertThat(just.visit(i->i+1,()->20),equalTo(11));
		assertThat(none.visit(i->i+1,()->20),equalTo(20));
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
        Stream<Integer> toStream = just.visit(m->Stream.of(m),()->Stream.of());
        assertThat(toStream.collect(Collectors.toList()),equalTo(Arrays.asList(10)));
    }


    @Test
    public void testConvertToAsync() {
        Future<Stream<Integer>> async = Future.of(()->just.visit(f->Stream.of((int)f),()->Stream.of()));

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
		Either<String,Integer> xor = none.toEither();
		assertTrue(xor.isLeft());
		assertThat(xor,equalTo(Either.left("none")));

	}



	@Test
	public void testToXorSecondary() {
		assertThat(just.toEither(-5000).swap(),equalTo(Either.left(10)));
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
		assertThat(just.mkString(),equalTo("Ior.right[10]"));
		assertThat(none.mkString(),equalTo("Ior.left[none]"));
	}


	@Test
	public void testGet() {
		assertThat(just.orElse(-50),equalTo(10));
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
		assertThat(just.fold(Monoid.of(1, Semigroups.intMult)),equalTo(10));
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
		assertThat(just.map(i->i+5),equalTo(Ior.right(15)));
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
		assertThat(just.trampoline(n ->sum(10,n)),equalTo(Ior.right(65)));
	}



	@Test
	public void testUnitT1() {
		assertThat(none.unit(10),equalTo(just));
	}

}
