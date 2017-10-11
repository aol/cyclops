package com.aol.cyclops2.control;

import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.mixins.Printable;
import cyclops.async.Future;
import cyclops.async.LazyReact;
import cyclops.collections.box.Mutable;
import cyclops.collections.immutable.PersistentSetX;
import cyclops.collections.mutable.ListX;
import cyclops.companion.Monoids;
import cyclops.companion.Reducers;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.control.*;
import cyclops.control.lazy.Eval;
import cyclops.control.lazy.Maybe;
import cyclops.control.lazy.Trampoline;
import cyclops.function.Monoid;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.equalTo;
import static cyclops.collections.tuple.Tuple.tuple;
import static org.junit.Assert.*;

public class OptionTest implements Printable {

    Option<Integer> eager;
    Option<Integer> none;

    @Before
    public void setUp() throws Exception {
        eager = Option.some(10);
        none = Option.none();

    }


    
    @Test
    public void recoverWith(){
        assertThat(none.recoverWith(()->Option.some(10)).toOptional().get(),equalTo(10));
        assertThat(none.recoverWith(()->Option.none()).isPresent(),equalTo(false));
        assertThat(eager.recoverWith(()->Option.some(5)).toOptional().get(),equalTo(10));
    }
    
    boolean lazy = true;

    @Test
    public void lazyTest() {
        Option.some(10)
             .flatMap(i -> { lazy=false; return Option.some(15);})
             .map(i -> { lazy=false; return   Option.some(15);})
             .map(i -> Option.some(20));
             
        
        assertFalse(lazy);
            
    }
    @Test
    public void testZipMonoid(){
        BinaryOperator<Zippable<Integer>> sumMaybes = Semigroups.combineScalarFunctors(Semigroups.intSum);
        assertThat(Option.some(1).zip(sumMaybes, Option.some(5)),equalTo(Option.some(6)));
        
    }



    @Test
    public void fib2() {
        System.out.println(fib(10, 1l, 0l));
    }

    public static long fib(int n, long a, long b) {
        return n == 0 ? b : fib(n - 1, a + b, a);
    }

    @Test
    public void nest() {
        assertThat(eager.nest().map(m -> m.toOptional().get()), equalTo(eager));
        assertThat(none.nest().map(m -> m.toOptional().get()), equalTo(none));
    }

    @Test
    public void coFlatMap() {

        Option.none().coflatMap(m -> m.isPresent() ? m.toOptional().get() : 10);

        // Maybe[10]

        assertThat(eager.coflatMap(m -> m.isPresent() ? m.toOptional().get() : 50), equalTo(eager));
        assertThat(none.coflatMap(m -> m.isPresent() ? m.toOptional().get() : 50), equalTo(Option.some(50)));
    }

    @Test
    public void combine() {
        Monoid<Integer> add = Monoid.of(0, Semigroups.intSum);
        assertThat(eager.combineEager(add, none), equalTo(eager));
        assertThat(none.combineEager(add, eager), equalTo(Option.some(0)));
        assertThat(none.combineEager(add, none), equalTo(Option.some(0)));
        assertThat(eager.combineEager(add, Option.some(10)), equalTo(Option.some(20)));
        Monoid<Integer> firstNonNull = Monoid.of(null, Semigroups.firstNonNull());
        assertThat(eager.combineEager(firstNonNull, none), equalTo(eager));

    }

    @Test
    public void optionalVMaybe() {

        Optional.of(10).map(i -> print("optional " + (i + 10)));

        Option.some(10).map(i -> print("maybe " + (i + 10)));

    }



    @Test
    public void noneEquals(){
        assertThat(none.toMaybe(), equalTo(none));
    }

    @Test
    public void testToMaybe() {
        assertThat(eager.toMaybe(), equalTo(eager));
        assertThat(none.toMaybe(), equalTo(none));
        assertThat(eager, equalTo(eager.toMaybe()));
        assertThat(none, equalTo(none.toMaybe()));
        assertThat(eager, equalTo(eager.toMaybe().map(i->i)));
    }

    private int add1(int i) {
        return i + 1;
    }



    @Test
    public void testFromOptional() {
        assertThat(Maybe.fromOptional(Optional.of(10)), equalTo(eager));
    }

    @Test
    public void testFromEvalSome() {
        assertThat(Maybe.fromEval(Eval.now(10)), equalTo(eager));
    }

    @Test
    public void testOfT() {
        assertThat(Option.some(1), equalTo(Option.some(1)));
    }

    @Test
    public void testOfNullable() {
        assertFalse(Option.ofNullable(null).isPresent());
        assertThat(Option.ofNullable(1), equalTo(Option.some(1)));

    }

    @Test
    public void testNarrow() {
        assertThat(Option.ofNullable(1), equalTo(Option.narrow(Option.some(1))));
    }

    @Test
    public void testSequenceLazy() {
        Option<ListX<Integer>> maybes = Option.sequence(ListX.of(eager, none, Option.some(1)));

        assertThat(maybes, equalTo(Option.some(1).flatMap(i -> Option.none())));
    }

    @Test
    public void testSequence() {
        Option<ListX<Integer>> maybes = Option.sequence(ListX.of(eager, none, Option.some(1)));

        assertThat(maybes, equalTo(Option.none()));
    }

    @Test
    public void testSequenceJust() {
        Option<ListX<Integer>> maybes = Option.sequenceJust(ListX.of(eager, none, Option.some(1)));
        assertThat(maybes, equalTo(Option.some(ListX.of(10, 1))));
    }

    @Test
    public void testAccumulateJustCollectionXOfMaybeOfTReducerOfR() {
        Option<PersistentSetX<Integer>> maybes = Option.accumulateJust(ListX.of(eager, none, Option.some(1)), Reducers.toPersistentSetX());
        assertThat(maybes, equalTo(Option.some(PersistentSetX.of(10, 1))));
    }

    @Test
    public void testAccumulateJustCollectionXOfMaybeOfTFunctionOfQsuperTRSemigroupOfR() {
        Option<String> maybes = Option.accumulateJust(ListX.of(eager, none, Option.some(1)), i -> "" + i,
                Monoids.stringConcat);
        assertThat(maybes, equalTo(Option.some("101")));
    }

    @Test
    public void testAccumulateJust() {
        Option<Integer> maybes = Option.accumulateJust(Monoids.intSum,ListX.of(eager, none, Option.some(1)));
        assertThat(maybes, equalTo(Option.some(11)));
    }

    @Test
    public void testUnitT() {
        assertThat(eager.unit(20), equalTo(Option.some(20)));
    }

    @Test
    public void testIsPresent() {
        assertTrue(eager.isPresent());
        assertFalse(none.isPresent());
    }

    @Test
    public void testRecoverSupplierOfT() {
        assertThat(eager.recover(20), equalTo(Option.some(10)));
        assertThat(none.recover(10), equalTo(Option.some(10)));
    }

    @Test
    public void testRecoverT() {
        assertThat(eager.recover(() -> 20), equalTo(Option.some(10)));
        assertThat(none.recover(() -> 10), equalTo(Option.some(10)));
    }


    @Test
    public void testMapFunctionOfQsuperTQextendsR() {
        assertThat(eager.map(i -> i + 5), equalTo(Option.some(15)));
        assertThat(none.map(i -> i + 5), equalTo(Option.none()));
    }

    @Test
    public void testFlatMap() {

        assertThat(eager.flatMap(i -> Option.some(i + 5)), equalTo(Option.some(15)));
        assertThat(none.flatMap(i -> Option.some(i + 5)), equalTo(Option.none()));
    }

    @Test
    public void testFlatMapAndRecover() {
        assertThat(eager.flatMap(i -> Option.none()).recover(15), equalTo(Option.some(15)));
        assertThat(eager.flatMap(i -> Option.none()).recover(() -> 15), equalTo(Option.some(15)));
        assertThat(none.flatMap(i -> Option.some(i + 5)).recover(15), equalTo(Option.some(15)));
    }

    @Test
    public void testWhenFunctionOfQsuperTQextendsRSupplierOfQextendsR() {
        assertThat(eager.visit(i -> i + 1, () -> 20), equalTo(11));
        assertThat(none.visit(i -> i + 1, () -> 20), equalTo(20));
    }

    @Test
    public void testStream() {
        assertThat(eager.stream().toListX(), equalTo(ListX.of(10)));
        assertThat(none.stream().toListX(), equalTo(ListX.of()));
    }

    @Test
    public void testOfSupplierOfT() {

    }

    @Test
    public void testConvertTo() {
        Stream<Integer> toStream = eager.visit(m -> Stream.of(m), () -> Stream.of());
        assertThat(toStream.collect(Collectors.toList()), equalTo(ListX.of(10)));
    }

    @Test
    public void testConvertToAsync() {
        Future<Stream<Integer>> async = Future
                .of(() -> eager.visit(f -> Stream.of((int) f), () -> Stream.of()));

        assertThat(async.toOptional().get().collect(Collectors.toList()), equalTo(ListX.of(10)));
    }

    @Test
    public void testIterate() {
        assertThat(eager.iterate(i -> i + 1,-1000).limit(10).sumInt(i->i), equalTo(145));
    }

    @Test
    public void testGenerate() {
        assertThat(eager.generate(-1000).limit(10).sumInt(i->i), equalTo(100));
    }



    @Test
    public void testToXor() {
        assertThat(eager.toEither(-1000), equalTo(Either.right(10)));

    }

    @Test
    public void testToXorNone() {
        Either<?, Integer> empty = none.toEither(-50000);

        assertTrue(empty.swap().map(__ -> 10).toOptional().get() == 10);

    }

    @Test
    public void testToXorSecondary() {
        assertThat(eager.toEither(-1000).swap(), equalTo(Either.left(10)));
    }

    @Test
    public void testToXorSecondaryNone() {
        Either<Integer, ?> empty = none.toEither(-50000).swap();
        assertTrue(empty.isRight());
        assertThat(empty.map(__ -> 10), equalTo(Either.right(10)));

    }

    @Test
    public void testToTry() {
        assertTrue(none.toTry().isFailure());
        assertThat(eager.toTry(), equalTo(Try.success(10)));
    }

    @Test
    public void testToTryClassOfXArray() {
        assertTrue(none.toTry(Throwable.class).isFailure());
    }


    @Test
    public void testToIorNone() {
        Either<Integer, ?> empty = none.toEither(-50000).swap();
        assertTrue(empty.isRight());
        assertThat(empty.map(__ -> 10), equalTo(Either.right(10)));

    }





    @Test
    public void testMkString() {
        assertThat(eager.mkString(), equalTo("Some[10]"));
        assertThat(none.mkString(), equalTo("None[]"));
    }

    LazyReact react = new LazyReact();




    @Test
    public void testFilter() {
        assertFalse(eager.filter(i -> i < 5).isPresent());
        assertTrue(eager.filter(i -> i > 5).isPresent());
        assertFalse(none.filter(i -> i < 5).isPresent());
        assertFalse(none.filter(i -> i > 5).isPresent());

    }

    @Test
    public void testOfType() {
        assertFalse(eager.ofType(String.class).isPresent());
        assertTrue(eager.ofType(Integer.class).isPresent());
        assertFalse(none.ofType(String.class).isPresent());
        assertFalse(none.ofType(Integer.class).isPresent());
    }

    @Test
    public void testFilterNot() {
        assertTrue(eager.filterNot(i -> i < 5).isPresent());
        assertFalse(eager.filterNot(i -> i > 5).isPresent());
        assertFalse(none.filterNot(i -> i < 5).isPresent());
        assertFalse(none.filterNot(i -> i > 5).isPresent());
    }

    @Test
    public void testNotNull() {
        assertTrue(eager.notNull().isPresent());
        assertFalse(none.notNull().isPresent());

    }

     private int add(int a, int b) {
        return a + b;
    }

    private int add3(int a, int b, int c) {
        return a + b + c;
    }



    private int add4(int a, int b, int c, int d) {
        return a + b + c + d;
    }



    private int add5(int a, int b, int c, int d, int e) {
        return a + b + c + d + e;
    }





    @Test
    public void testFoldRightMonoidOfT() {
        assertThat(eager.fold(Monoid.of(1, Semigroups.intMult)), equalTo(10));
    }


    @Test
    public void testWhenFunctionOfQsuperMaybeOfTQextendsR() {

        String match = Option.some("data is present").visit(present -> "hello", () -> "missing");

        assertThat(eager.visit(s -> "hello", () -> "world"), equalTo("hello"));
        assertThat(none.visit(s -> "hello", () -> "world"), equalTo("world"));
    }

    @Test
    public void testOrElseGet() {
        assertThat(none.orElseGet(() -> 2), equalTo(2));
        assertThat(eager.orElseGet(() -> 2), equalTo(10));
    }

    @Test
    public void testToOptional() {
        assertFalse(none.toOptional().isPresent());
        assertTrue(eager.toOptional().isPresent());
        assertThat(eager.toOptional(), equalTo(Optional.of(10)));
    }

    @Test
    public void testToStream() {
        assertThat(none.stream().collect(Collectors.toList()).size(), equalTo(0));
        assertThat(eager.stream().collect(Collectors.toList()).size(), equalTo(1));

    }


    @Test
    public void testOrElse() {
        assertThat(none.orElse(20), equalTo(20));
        assertThat(eager.orElse(20), equalTo(10));
    }




    @Test
    public void testIterator1() {
        assertThat(Streams.stream(eager.iterator()).collect(Collectors.toList()), equalTo(Arrays.asList(10)));
    }

    @Test
    public void testForEach() {
        Mutable<Integer> capture = Mutable.of(null);
        none.forEach(c -> capture.set(c));
        assertNull(capture.get());
        eager.forEach(c -> capture.set(c));
        assertThat(capture.get(), equalTo(10));
    }

    @Test
    public void testSpliterator() {
        assertThat(StreamSupport.stream(eager.spliterator(), false).collect(Collectors.toList()),
                equalTo(Arrays.asList(10)));
    }

    @Test
    public void testCast() {
        Option<Number> num = eager.cast(Number.class);
    }

    @Test
    public void testMapFunctionOfQsuperTQextendsR1() {
        assertThat(eager.map(i -> i + 5), equalTo(Option.some(15)));
    }

    @Test
    public void testPeek() {
        Mutable<Integer> capture = Mutable.of(null);
        eager = eager.peek(c -> capture.set(c));

        eager.toOptional().get();
        assertThat(capture.get(), equalTo(10));
    }

    private Trampoline<Integer> sum(int times, int sum) {
        return times == 0 ? Trampoline.done(sum) : Trampoline.more(() -> sum(times - 1, sum + times));
    }

    @Test
    public void testTrampoline() {
        assertThat(eager.trampoline(n -> sum(10, n)), equalTo(Option.some(65)));
    }

    @Test
    public void testUnitT1() {
        assertThat(none.unit(10), equalTo(eager));
    }

	@Test
	public void testFlatMapIterable() {
        Option<Integer> maybe = eager.flatMapI(i -> Arrays.asList(i, 20, 30));
		assertThat(maybe.toOptional().get(), equalTo(10));
	}

	@Test
	public void testFlatMapPublisher() {
        Option<Integer> maybe = Option.some(100).flatMapP(i -> Flux.just(10, i));
		assertThat(maybe.toOptional().get(), equalTo(10));
	}
}
