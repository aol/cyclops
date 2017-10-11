package com.aol.cyclops2.control;

import com.aol.cyclops2.util.box.Mutable;
import cyclops.collections.immutable.PersistentSetX;
import cyclops.async.LazyReact;
import cyclops.collections.mutable.ListX;
import cyclops.async.Future;
import cyclops.companion.Monoids;
import cyclops.companion.Reducers;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.control.*;
import cyclops.control.lazy.Maybe;
import cyclops.control.lazy.Trampoline;
import cyclops.function.Monoid;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;



public class Xor2Test {

	Either<String,Integer> just;
	Either<String,Integer> none;
	@Before
	public void setUp() throws Exception {
		just = Either.right(10);
		none = Either.left("none");
	}


   
	@Test
    public void testSequenceSecondary() {
        Either<ListX<Integer>,ListX<String>> xors = Either.sequenceLeft(ListX.of(just,none, Either.right(1)));
        assertThat(xors,equalTo(Either.right(ListX.of("none"))));
    }

    @Test
    public void testAccumulateSecondary2() {
        Either<?,PersistentSetX<String>> xors = Either.accumulateLeft(ListX.of(just,none, Either.right(1)),Reducers.<String>toPersistentSetX());
        assertThat(xors,equalTo(Either.right(PersistentSetX.of("none"))));
    }

    @Test
    public void testAccumulateSecondarySemigroup() {
        Either<?,String> xors = Either.accumulateLeft(ListX.of(just,none, Either.left("1")), i->""+i, Monoids.stringConcat);
        assertThat(xors,equalTo(Either.right("none1")));
    }
    @Test
    public void testAccumulateSecondarySemigroupIntSum() {
        Ior<?,Integer> iors = Ior.accumulateSecondary(Monoids.intSum,ListX.of(Ior.both(2, "boo!"),Ior.secondary(1)));
        assertThat(iors,equalTo(Ior.primary(3)));
    }

	@Test
    public void nest(){
       assertThat(just.nest().map(m->m.toOptional().get()),equalTo(just));
       assertThat(none.nest().map(m->m.get()),equalTo(none));
    }

	@Test
	public void visit(){
	    
	    assertThat(just.visit(secondary->"no", primary->"yes"),equalTo("yes"));
	    assertThat(none.visit(secondary->"no", primary->"yes"),equalTo("no"));
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
		assertThat(Ior.primary(1),equalTo(Ior.primary(1)));
	}

	

	@Test
	public void testSequence() {
		Either.sequenceRight(ListX.of(just, Either.right(1))).printOut();
		Either<ListX<String>,ListX<Integer>> maybes = Either.sequenceRight(ListX.of(just,none, Either.right(1)));
		assertThat(maybes,equalTo(Either.right(ListX.of(10,1))));
	}

	@Test
	public void testAccumulateJustCollectionXOfMaybeOfTReducerOfR() {
		Either<?,PersistentSetX<Integer>> maybes = Either.accumulateRight(ListX.of(just,none, Either.right(1)),Reducers.toPersistentSetX());
		assertThat(maybes,equalTo(Either.right(PersistentSetX.of(10,1))));
	}

	@Test
	public void testAccumulateJustCollectionXOfMaybeOfTFunctionOfQsuperTRSemigroupOfR() {
		Either<?,String> maybes = Either.accumulateRight(ListX.of(just,none, Either.right(1)), i->""+i,Monoids.stringConcat);
		assertThat(maybes,equalTo(Either.right("101")));
	}
	@Test
	public void testAccumulateJust() {
		Either<?,Integer> maybes = Either.accumulateRight(Monoids.intSum,ListX.of(just,none, Either.right(1)));
		assertThat(maybes,equalTo(Either.right(11)));
	}
	@Test
    public void testAccumulateSecondary() {
        Either<?,String> maybes = Either.accumulateLeft(Monoids.stringConcat,ListX.of(just,none, Either.left("hello")));
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
		assertThat(just.visit(i->i+1,()->20),equalTo(11));
		assertThat(none.visit(i->i+1,()->20),equalTo(20));
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
		assertThat(just.toIor(),equalTo(Ior.primary(10)));
		
	}
	@Test
	public void testToIorNone(){
		Ior<String,Integer> ior = none.toIor();
		assertTrue(ior.isSecondary());
        assertThat(ior,equalTo(Ior.secondary("none")));
		
	}


	@Test
	public void testToIorSecondary() {
		assertThat(just.toIor().swap(),equalTo(Ior.secondary(10)));
	}
	

	@Test
	public void testToIorSecondaryNone(){
	    Ior<Integer,String> ior = none.toIor().swap();
        assertTrue(ior.isPrimary());
        assertThat(ior,equalTo(Ior.primary("none")));
		
	}



	@Test
	public void testMkString() {
		assertThat(just.mkString(),equalTo("Xor.lazyRight[10]"));
		assertThat(none.mkString(),equalTo("Xor.lazyLeft[none]"));
	}
	LazyReact react = new LazyReact();


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
	public void testCast() {
		Either<?,Number> num = just.cast(Number.class);
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
	public void testTrampoline() {
		assertThat(just.trampoline(n ->sum(10,n)),equalTo(Either.right(65)));
	}

	

	@Test
	public void testUnitT1() {
		assertThat(none.unit(10),equalTo(just));
	}

}
