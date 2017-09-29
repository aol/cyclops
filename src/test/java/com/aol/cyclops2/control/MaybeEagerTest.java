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
import cyclops.control.Maybe.CompletableMaybe;
import cyclops.function.Monoid;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple3;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static cyclops.control.Maybe.eager;
import static org.hamcrest.Matchers.equalTo;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.*;

public class MaybeEagerTest implements Printable {

    Maybe<Integer> eager;
    Maybe<Integer> none;

    @Before
    public void setUp() throws Exception {
        eager = Maybe.eager(10);
        none = Maybe.eagerNone();

    }


    
    @Test
    public void recoverWith(){
        assertThat(none.recoverWith(()->Maybe.eager(10)).get(),equalTo(10));
        assertThat(none.recoverWith(()->Maybe.eagerNone()).isPresent(),equalTo(false));
        assertThat(eager.recoverWith(()->Maybe.eager(5)).get(),equalTo(10));
    }
    
    boolean lazy = true;

    @Test
    public void lazyTest() {
        Maybe.eager(10)
             .flatMap(i -> { lazy=false; return Maybe.eager(15);})
             .map(i -> { lazy=false; return   Maybe.eager(15);})
             .map(i -> Maybe.eager(20));
             
        
        assertFalse(lazy);
            
    }
    @Test
    public void testZipMonoid(){
        BinaryOperator<Zippable<Integer>> sumMaybes = Semigroups.combineScalarFunctors(Semigroups.intSum);
        assertThat(Maybe.eager(1).zip(sumMaybes, Maybe.eager(5)),equalTo(Maybe.eager(6)));
        
    }



    @Test
    public void testZip() {
        assertThat(Maybe.eager(10).zip(Eval.now(20), (a, b) -> a + b).get(), equalTo(30));
        assertThat(Maybe.eager(10).zipP(Eval.now(20),(a, b) -> a + b).get(), equalTo(30));
        assertThat(Maybe.eager(10).zipS(Stream.of(20), (a, b) -> a + b).get(), equalTo(30));
        assertThat(Maybe.eager(10).zip(Seq.of(20), (a, b) -> a + b).get(), equalTo(30));
        assertThat(Maybe.eager(10).zip(Seq.of(20)).get(), equalTo(Tuple.tuple(10, 20)));
        assertThat(Maybe.eager(10).zipS(Stream.of(20)).get(), equalTo(Tuple.tuple(10, 20)));
        assertThat(Maybe.eager(10).zip(Eval.now(20)).get(), equalTo(Tuple.tuple(10, 20)));
    }


    @Test
    public void fib() {
        System.out.println(fibonacci(eager(tuple(100_000, 1l, 0l))));
    }

    public Maybe<Long> fibonacci(Maybe<Tuple3<Integer, Long, Long>> fib) {
        return fib.flatMap(t -> t.v1 == 0 ? eager(t.v3) : fibonacci(eager(tuple(t.v1 - 1, t.v2 + t.v3, t.v2))));
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
        assertThat(eager.nest().map(m -> m.get()), equalTo(eager));
        assertThat(none.nest().map(m -> m.get()), equalTo(none));
    }

    @Test
    public void coFlatMap() {

        Maybe.eagerNone().coflatMap(m -> m.isPresent() ? m.get() : 10);

        // Maybe[10]

        assertThat(eager.coflatMap(m -> m.isPresent() ? m.get() : 50), equalTo(eager));
        assertThat(none.coflatMap(m -> m.isPresent() ? m.get() : 50), equalTo(Maybe.eager(50)));
    }

    @Test
    public void combine() {
        Monoid<Integer> add = Monoid.of(0, Semigroups.intSum);
        assertThat(eager.combineEager(add, none), equalTo(eager));
        assertThat(none.combineEager(add, eager), equalTo(Maybe.eager(0)));
        assertThat(none.combineEager(add, none), equalTo(Maybe.eager(0)));
        assertThat(eager.combineEager(add, Maybe.eager(10)), equalTo(Maybe.eager(20)));
        Monoid<Integer> firstNonNull = Monoid.of(null, Semigroups.firstNonNull());
        assertThat(eager.combineEager(firstNonNull, none), equalTo(eager));

    }

    @Test
    public void optionalVMaybe() {

        Optional.of(10).map(i -> print("optional " + (i + 10)));

        Maybe.eager(10).map(i -> print("maybe " + (i + 10)));

    }

    @Test
    public void odd() {
        System.out.println(even(Maybe.eager(200000)).get());
    }

    public Maybe<String> odd(Maybe<Integer> n) {

        return n.flatMap(x -> even(Maybe.eager(x - 1)));
    }

    public Maybe<String> even(Maybe<Integer> n) {
        return n.flatMap(x -> {
            return x <= 0 ? Maybe.eager("done") : odd(Maybe.eager(x - 1));
        });
    }

    @Test
    public void testFiltering() {
        assertThat(ReactiveSeq.of(Maybe.eager(1), Try.success(1)).filter(Xor.primary(1)).toListX(),
                equalTo(ListX.of(Maybe.eager(1), Try.success(1))));
    }

    @Test
    public void testFilteringNoValue() {
        assertThat(ReactiveSeq.of(1, 1).filter(Xor.primary(1)).toListX(), equalTo(ListX.of(1, 1)));
    }

    @Test
    public void testToMaybe() {
        assertThat(eager.toMaybe(), equalTo(eager));
        assertThat(none.toMaybe(), equalTo(none));
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
        assertThat(Maybe.eager(1), equalTo(Maybe.eager(1)));
    }

    @Test
    public void testOfNullable() {
        assertFalse(Maybe.ofNullable(null).isPresent());
        assertThat(Maybe.ofNullable(1), equalTo(Maybe.eager(1)));

    }

    @Test
    public void testNarrow() {
        assertThat(Maybe.ofNullable(1), equalTo(Maybe.narrow(Maybe.eager(1))));
    }

    @Test
    public void testSequenceLazy() {
        Maybe<ListX<Integer>> maybes = Maybe.sequence(ListX.of(eager, none, Maybe.eager(1)));

        assertThat(maybes, equalTo(Maybe.eager(1).flatMap(i -> Maybe.eagerNone())));
    }

    @Test
    public void testSequence() {
        Maybe<ListX<Integer>> maybes = Maybe.sequence(ListX.of(eager, none, Maybe.eager(1)));

        assertThat(maybes, equalTo(Maybe.eagerNone()));
    }

    @Test
    public void testSequenceJust() {
        Maybe<ListX<Integer>> maybes = Maybe.sequenceJust(ListX.of(eager, none, Maybe.eager(1)));
        assertThat(maybes, equalTo(Maybe.eager(ListX.of(10, 1))));
    }

    @Test
    public void testAccumulateJustCollectionXOfMaybeOfTReducerOfR() {
        Maybe<PersistentSetX<Integer>> maybes = Maybe.accumulateJust(ListX.of(eager, none, Maybe.eager(1)), Reducers.toPersistentSetX());
        assertThat(maybes, equalTo(Maybe.eager(PersistentSetX.of(10, 1))));
    }

    @Test
    public void testAccumulateJustCollectionXOfMaybeOfTFunctionOfQsuperTRSemigroupOfR() {
        Maybe<String> maybes = Maybe.accumulateJust(ListX.of(eager, none, Maybe.eager(1)), i -> "" + i,
                Monoids.stringConcat);
        assertThat(maybes, equalTo(Maybe.eager("101")));
    }

    @Test
    public void testAccumulateJust() {
        Maybe<Integer> maybes = Maybe.accumulateJust(Monoids.intSum,ListX.of(eager, none, Maybe.eager(1)));
        assertThat(maybes, equalTo(Maybe.eager(11)));
    }

    @Test
    public void testUnitT() {
        assertThat(eager.unit(20), equalTo(Maybe.eager(20)));
    }

    @Test
    public void testIsPresent() {
        assertTrue(eager.isPresent());
        assertFalse(none.isPresent());
    }

    @Test
    public void testRecoverSupplierOfT() {
        assertThat(eager.recover(20), equalTo(Maybe.eager(10)));
        assertThat(none.recover(10), equalTo(Maybe.eager(10)));
    }

    @Test
    public void testRecoverT() {
        assertThat(eager.recover(() -> 20), equalTo(Maybe.eager(10)));
        assertThat(none.recover(() -> 10), equalTo(Maybe.eager(10)));
    }


    @Test
    public void testMapFunctionOfQsuperTQextendsR() {
        assertThat(eager.map(i -> i + 5), equalTo(Maybe.eager(15)));
        assertThat(none.map(i -> i + 5), equalTo(Maybe.eagerNone()));
    }

    @Test
    public void testFlatMap() {

        assertThat(eager.flatMap(i -> Maybe.eager(i + 5)), equalTo(Maybe.eager(15)));
        assertThat(none.flatMap(i -> Maybe.eager(i + 5)), equalTo(Maybe.eagerNone()));
    }

    @Test
    public void testFlatMapAndRecover() {
        assertThat(eager.flatMap(i -> Maybe.eagerNone()).recover(15), equalTo(Maybe.eager(15)));
        assertThat(eager.flatMap(i -> Maybe.eagerNone()).recover(() -> 15), equalTo(Maybe.eager(15)));
        assertThat(none.flatMap(i -> Maybe.eager(i + 5)).recover(15), equalTo(Maybe.eager(15)));
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

        assertThat(async.get().collect(Collectors.toList()), equalTo(ListX.of(10)));
    }

    @Test
    public void testIterate() {
        assertThat(eager.iterate(i -> i + 1).limit(10).sumInt(i->i), equalTo(145));
    }

    @Test
    public void testGenerate() {
        assertThat(eager.generate().limit(10).sumInt(i->i), equalTo(100));
    }

    @Test
    public void testMapReduceReducerOfE() {
        assertThat(eager.mapReduce(Reducers.toCountInt()), equalTo(1));
    }


    @Test
    public void testToXor() {
        assertThat(eager.toXor(), equalTo(Xor.primary(10)));

    }

    @Test
    public void testToXorNone() {
        Xor<?, Integer> empty = none.toXor();

        assertTrue(empty.swap().map(__ -> 10).get() == 10);

    }

    @Test
    public void testToXorSecondary() {
        assertThat(eager.toXor().swap(), equalTo(Xor.secondary(10)));
    }

    @Test
    public void testToXorSecondaryNone() {
        Xor<Integer, ?> empty = none.toXor().swap();
        assertTrue(empty.isPrimary());
        assertThat(empty.map(__ -> 10), equalTo(Xor.primary(10)));

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
    public void testToIor() {
        assertThat(eager.toIor(), equalTo(Ior.primary(10)));
        assertThat(Ior.fromPublisher(eager), equalTo(Ior.primary(10)));

    }

    @Test
    public void testToIorNone() {
        Xor<Integer, ?> empty = none.toXor().swap();
        assertTrue(empty.isPrimary());
        assertThat(empty.map(__ -> 10), equalTo(Xor.primary(10)));

    }

    @Test
    public void testToIorSecondary() {
        assertThat(eager.toIor().swap(), equalTo(Ior.secondary(10)));
    }

    @Test
    public void testToIorSecondaryNone() {
        Ior<Integer, ?> ior = none.toIor().swap().map(__ -> 10);
        assertThat(ior.get(), equalTo(10));

    }

    @Test
    public void testToEvalNow() {
        assertThat(eager.toEvalNow(), equalTo(Eval.now(10)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testToEvalNowNone() {
        none.toEvalNow();
        fail("exception expected");

    }

    @Test
    public void testToEvalLater() {
        assertThat(eager.toEvalLater(), equalTo(Eval.later(() -> 10)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testToEvalLaterNone() {
        none.toEvalLater().get();
        fail("exception expected");

    }

    @Test
    public void testToEvalAlways() {
        assertThat(eager.toEvalAlways(), equalTo(Eval.always(() -> 10)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testToEvalAlwaysNone() {
        none.toEvalAlways().get();
        fail("exception expected");

    }



    @Test
    public void testMkString() {
        assertThat(eager.mkString(), equalTo("JustEager[10]"));
        assertThat(none.mkString(), equalTo("NothingEager[]"));
    }

    LazyReact react = new LazyReact();


    @Test
    public void testGet() {
        assertThat(eager.get(), equalTo(10));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetNone() {
        none.get();

    }

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
    public void testMapReduceFunctionOfQsuperTQextendsRMonoidOfR() {
        assertThat(eager.mapReduce(s -> s.toString(), Monoid.of("", Semigroups.stringJoin(","))), equalTo(",10"));
    }

    @Test
    public void testReduceMonoidOfT() {
        assertThat(eager.reduce(Monoid.of(1, Semigroups.intMult)), equalTo(10));
    }

    @Test
    public void testReduceBinaryOperatorOfT() {
        assertThat(eager.reduce((a, b) -> a + b), equalTo(Optional.of(10)));
    }

    @Test
    public void testReduceTBinaryOperatorOfT() {
        assertThat(eager.reduce(10, (a, b) -> a + b), equalTo(20));
    }

    @Test
    public void testReduceUBiFunctionOfUQsuperTUBinaryOperatorOfU() {
        assertThat(eager.reduce(11, (a, b) -> a + b, (a, b) -> a * b), equalTo(21));
    }

    @Test
    public void testReduceStreamOfQextendsMonoidOfT() {
        ListX<Integer> countAndTotal = eager.reduce(Stream.of(Reducers.toCountInt(), Reducers.toTotalInt()));
        assertThat(countAndTotal, equalTo(ListX.of(1, 10)));
    }

    @Test
    public void testReduceIterableOfReducerOfT() {
        ListX<Integer> countAndTotal = eager.reduce(Arrays.asList(Reducers.toCountInt(), Reducers.toTotalInt()));
        assertThat(countAndTotal, equalTo(ListX.of(1, 10)));
    }

    @Test
    public void testFoldRightMonoidOfT() {
        assertThat(eager.foldRight(Monoid.of(1, Semigroups.intMult)), equalTo(10));
    }

    @Test
    public void testFoldRightTBinaryOperatorOfT() {
        assertThat(eager.foldRight(10, (a, b) -> a + b), equalTo(20));
    }


    @Test
    public void testWhenFunctionOfQsuperMaybeOfTQextendsR() {

        String match = Maybe.eager("data is present").visit(present -> "hello", () -> "missing");

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
        assertThat(none.toStream().collect(Collectors.toList()).size(), equalTo(0));
        assertThat(eager.toStream().collect(Collectors.toList()).size(), equalTo(1));

    }


    @Test
    public void testOrElse() {
        assertThat(none.orElse(20), equalTo(20));
        assertThat(eager.orElse(20), equalTo(10));
    }

    @Test(expected = RuntimeException.class)
    public void testOrElseThrow() {
        none.orElseThrow(() -> new RuntimeException());
    }

    @Test
    public void testOrElseThrowSome() {

        assertThat(eager.orElseThrow(() -> new RuntimeException()), equalTo(10));
    }



    @Test
    public void testToFuture() {
        Future<Integer> cf = eager.toFuture();
        assertThat(cf.get(), equalTo(10));
    }

    @Test
    public void testToCompletableFuture() {
        CompletableFuture<Integer> cf = eager.toCompletableFuture();
        assertThat(cf.join(), equalTo(10));
    }


    static Executor exec = Executors.newFixedThreadPool(1);

    @Test
    public void testToCompletableFutureAsyncExecutor() {
        CompletableFuture<Integer> cf = eager.toCompletableFutureAsync(exec);
        assertThat(cf.join(), equalTo(10));
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
        Maybe<Number> num = eager.cast(Number.class);
    }

    @Test
    public void testMapFunctionOfQsuperTQextendsR1() {
        assertThat(eager.map(i -> i + 5), equalTo(Maybe.eager(15)));
    }

    @Test
    public void testPeek() {
        Mutable<Integer> capture = Mutable.of(null);
        eager = eager.peek(c -> capture.set(c));
        assertNull(capture.get());

        eager.get();
        assertThat(capture.get(), equalTo(10));
    }

    private Trampoline<Integer> sum(int times, int sum) {
        return times == 0 ? Trampoline.done(sum) : Trampoline.more(() -> sum(times - 1, sum + times));
    }

    @Test
    public void testTrampoline() {
        assertThat(eager.trampoline(n -> sum(10, n)), equalTo(Maybe.eager(65)));
    }

    @Test
    public void testUnitT1() {
        assertThat(none.unit(10), equalTo(eager));
    }

	@Test
	public void testFlatMapIterable() {
		Maybe<Integer> maybe = eager.flatMapI(i -> Arrays.asList(i, 20, 30));
		assertThat(maybe.get(), equalTo(10));
	}

	@Test
	public void testFlatMapPublisher() {
		Maybe<Integer> maybe = Maybe.eager(100).flatMapP(i -> Flux.just(10, i));
		assertThat(maybe.get(), equalTo(10));
	}
}
