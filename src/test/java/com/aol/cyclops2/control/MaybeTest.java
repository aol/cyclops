package com.aol.cyclops2.control;

import cyclops.*;
import cyclops.collections.box.Mutable;
import cyclops.collections.immutable.PSetX;
import cyclops.async.LazyReact;
import cyclops.collections.ListX;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.mixins.Printable;
import cyclops.async.Future;
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

import static cyclops.control.Maybe.just;
import static org.hamcrest.Matchers.equalTo;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.*;

public class MaybeTest implements Printable {

    Maybe<Integer> just;
    Maybe<Integer> none;

    @Before
    public void setUp() throws Exception {
        just = Maybe.just(10);
        none = Maybe.none();

    }
    @Test
    public void completableTest(){
        CompletableMaybe<Integer,Integer> completable = Maybe.maybe();
        Maybe<Integer> mapped = completable.map(i->i*2)
                                          .flatMap(i->Eval.later(()->i+1));

        completable.complete(5);
        System.out.println(mapped.getClass());
        mapped.printOut();
        assertThat(mapped.get(),equalTo(11));


    }
    @Test
    public void completableNoneTest(){
        CompletableMaybe<Integer,Integer> completable = Maybe.maybe();
        Maybe<Integer> mapped = completable.map(i->i*2)
                                           .flatMap(i->Eval.later(()->i+1));

        completable.complete(null);

        mapped.printOut();
        assertThat(mapped.isPresent(),equalTo(false));


    }
    @Test
    public void completableErrorTest(){
        CompletableMaybe<Integer,Integer> completable = Maybe.maybe();
        Maybe<Integer> mapped = completable.map(i->i*2)
                .flatMap(i->Eval.later(()->i+1));

        completable.completeExceptionally(new IllegalStateException());

        mapped.printOut();
        assertThat(mapped.isPresent(),equalTo(false));


    }
    @Test
    public void reactive(){

        Future<Integer> result = Future.future();
        Future<Integer> future = Future.future();

        Thread t=  new Thread(()->{
            try {
                Thread.sleep(1500l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            future.complete(10);
        });
        t.start();

        Spouts.from(Maybe.fromFuture(future)
              .map(i->i*2))
                .peek(System.out::println)
                .map(i->i*100)
                .subscribeAll(e->result.complete(e));


        assertFalse(result.isDone());
        System.out.println("Blocking?");
        assertThat(result.get(),equalTo(2000));

    }
    
    @Test
    public void recoverWith(){
        assertThat(none.recoverWith(()->Maybe.just(10)).get(),equalTo(10));
        assertThat(none.recoverWith(()->Maybe.none()).isPresent(),equalTo(false));
        assertThat(just.recoverWith(()->Maybe.just(5)).get(),equalTo(10));
    }
    
    boolean lazy = true;

    @Test
    public void lazyTest() {
        Maybe.just(10)
             .flatMap(i -> { lazy=false; return Maybe.just(15);})
             .map(i -> { lazy=false; return   Maybe.just(15);})
             .map(i -> Maybe.of(20));
             
        
        assertTrue(lazy);
            
    }
    @Test
    public void testZipMonoid(){
        BinaryOperator<Zippable<Integer>> sumMaybes = Semigroups.combineScalarFunctors(Semigroups.intSum);
        assertThat(Maybe.just(1).zip(sumMaybes, Maybe.just(5)),equalTo(Maybe.just(6)));
        
    }



    @Test
    public void testZip() {
        assertThat(Maybe.just(10).zip(Eval.now(20), (a, b) -> a + b).get(), equalTo(30));
        assertThat(Maybe.just(10).zipP(Eval.now(20),(a, b) -> a + b).get(), equalTo(30));
        assertThat(Maybe.just(10).zipS(Stream.of(20), (a, b) -> a + b).get(), equalTo(30));
        assertThat(Maybe.just(10).zip(Seq.of(20), (a, b) -> a + b).get(), equalTo(30));
        assertThat(Maybe.just(10).zip(Seq.of(20)).get(), equalTo(Tuple.tuple(10, 20)));
        assertThat(Maybe.just(10).zipS(Stream.of(20)).get(), equalTo(Tuple.tuple(10, 20)));
        assertThat(Maybe.just(10).zip(Eval.now(20)).get(), equalTo(Tuple.tuple(10, 20)));
    }


    @Test
    public void fib() {
        System.out.println(fibonacci(just(tuple(100_000, 1l, 0l))));
    }

    public Maybe<Long> fibonacci(Maybe<Tuple3<Integer, Long, Long>> fib) {
        return fib.flatMap(t -> t.v1 == 0 ? just(t.v3) : fibonacci(just(tuple(t.v1 - 1, t.v2 + t.v3, t.v2))));
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
        assertThat(just.nest().map(m -> m.get()), equalTo(just));
        assertThat(none.nest().map(m -> m.get()), equalTo(none));
    }

    @Test
    public void coFlatMap() {

        Maybe.none().coflatMap(m -> m.isPresent() ? m.get() : 10);

        // Maybe[10]

        assertThat(just.coflatMap(m -> m.isPresent() ? m.get() : 50), equalTo(just));
        assertThat(none.coflatMap(m -> m.isPresent() ? m.get() : 50), equalTo(Maybe.of(50)));
    }

    @Test
    public void combine() {
        Monoid<Integer> add = Monoid.of(0, Semigroups.intSum);
        assertThat(just.combineEager(add, none), equalTo(just));
        assertThat(none.combineEager(add, just), equalTo(Maybe.of(0)));
        assertThat(none.combineEager(add, none), equalTo(Maybe.of(0)));
        assertThat(just.combineEager(add, Maybe.just(10)), equalTo(Maybe.just(20)));
        Monoid<Integer> firstNonNull = Monoid.of(null, Semigroups.firstNonNull());
        assertThat(just.combineEager(firstNonNull, none), equalTo(just));

    }

    @Test
    public void optionalVMaybe() {

        Optional.of(10).map(i -> print("optional " + (i + 10)));

        Maybe.just(10).map(i -> print("maybe " + (i + 10)));

    }

    @Test
    public void odd() {
        System.out.println(even(Maybe.just(200000)).get());
    }

    public Maybe<String> odd(Maybe<Integer> n) {

        return n.flatMap(x -> even(Maybe.just(x - 1)));
    }

    public Maybe<String> even(Maybe<Integer> n) {
        return n.flatMap(x -> {
            return x <= 0 ? Maybe.just("done") : odd(Maybe.just(x - 1));
        });
    }

    @Test
    public void testFiltering() {
        assertThat(ReactiveSeq.of(Maybe.just(1), Try.success(1)).filter(Xor.primary(1)).toListX(),
                equalTo(ListX.of(Maybe.just(1), Try.success(1))));
    }

    @Test
    public void testFilteringNoValue() {
        assertThat(ReactiveSeq.of(1, 1).filter(Xor.primary(1)).toListX(), equalTo(ListX.of(1, 1)));
    }

    @Test
    public void testToMaybe() {
        assertThat(just.toMaybe(), equalTo(just));
        assertThat(none.toMaybe(), equalTo(none));
    }

    private int add1(int i) {
        return i + 1;
    }



    @Test
    public void testFromOptional() {
        assertThat(Maybe.fromOptional(Optional.of(10)), equalTo(just));
    }

    @Test
    public void testFromEvalSome() {
        assertThat(Maybe.fromEval(Eval.now(10)), equalTo(just));
    }

    @Test
    public void testOfT() {
        assertThat(Maybe.of(1), equalTo(Maybe.of(1)));
    }

    @Test
    public void testOfNullable() {
        assertFalse(Maybe.ofNullable(null).isPresent());
        assertThat(Maybe.ofNullable(1), equalTo(Maybe.of(1)));

    }

    @Test
    public void testNarrow() {
        assertThat(Maybe.ofNullable(1), equalTo(Maybe.narrow(Maybe.of(1))));
    }

    @Test
    public void testSequenceLazy() {
        Maybe<ListX<Integer>> maybes = Maybe.sequence(ListX.of(just, none, Maybe.of(1)));

        assertThat(maybes, equalTo(Maybe.just(1).flatMap(i -> Maybe.none())));
    }

    @Test
    public void testSequence() {
        Maybe<ListX<Integer>> maybes = Maybe.sequence(ListX.of(just, none, Maybe.of(1)));

        assertThat(maybes, equalTo(Maybe.none()));
    }

    @Test
    public void testSequenceJust() {
        Maybe<ListX<Integer>> maybes = Maybe.sequenceJust(ListX.of(just, none, Maybe.of(1)));
        assertThat(maybes, equalTo(Maybe.of(ListX.of(10, 1))));
    }

    @Test
    public void testAccumulateJustCollectionXOfMaybeOfTReducerOfR() {
        Maybe<PSetX<Integer>> maybes = Maybe.accumulateJust(ListX.of(just, none, Maybe.of(1)), Reducers.toPSetX());
        assertThat(maybes, equalTo(Maybe.of(PSetX.of(10, 1))));
    }

    @Test
    public void testAccumulateJustCollectionXOfMaybeOfTFunctionOfQsuperTRSemigroupOfR() {
        Maybe<String> maybes = Maybe.accumulateJust(ListX.of(just, none, Maybe.of(1)), i -> "" + i,
                Monoids.stringConcat);
        assertThat(maybes, equalTo(Maybe.of("101")));
    }

    @Test
    public void testAccumulateJust() {
        Maybe<Integer> maybes = Maybe.accumulateJust(Monoids.intSum,ListX.of(just, none, Maybe.of(1)));
        assertThat(maybes, equalTo(Maybe.of(11)));
    }

    @Test
    public void testUnitT() {
        assertThat(just.unit(20), equalTo(Maybe.of(20)));
    }

    @Test
    public void testIsPresent() {
        assertTrue(just.isPresent());
        assertFalse(none.isPresent());
    }

    @Test
    public void testRecoverSupplierOfT() {
        assertThat(just.recover(20), equalTo(Maybe.of(10)));
        assertThat(none.recover(10), equalTo(Maybe.of(10)));
    }

    @Test
    public void testRecoverT() {
        assertThat(just.recover(() -> 20), equalTo(Maybe.of(10)));
        assertThat(none.recover(() -> 10), equalTo(Maybe.of(10)));
    }
    @Test
    public void testRecoverFn() {
        assertThat(just.recover(__ -> 20), equalTo(Maybe.of(10)));
        assertThat(none.recover(__ -> 10), equalTo(Maybe.of(10)));
    }

    @Test
    public void testMapFunctionOfQsuperTQextendsR() {
        assertThat(just.map(i -> i + 5), equalTo(Maybe.of(15)));
        assertThat(none.map(i -> i + 5), equalTo(Maybe.none()));
    }

    @Test
    public void testFlatMap() {

        assertThat(just.flatMap(i -> Maybe.of(i + 5)), equalTo(Maybe.of(15)));
        assertThat(none.flatMap(i -> Maybe.of(i + 5)), equalTo(Maybe.none()));
    }

    @Test
    public void testFlatMapAndRecover() {
        assertThat(just.flatMap(i -> Maybe.none()).recover(15), equalTo(Maybe.of(15)));
        assertThat(just.flatMap(i -> Maybe.none()).recover(() -> 15), equalTo(Maybe.of(15)));
        assertThat(none.flatMap(i -> Maybe.of(i + 5)).recover(15), equalTo(Maybe.of(15)));
    }

    @Test
    public void testWhenFunctionOfQsuperTQextendsRSupplierOfQextendsR() {
        assertThat(just.visit(i -> i + 1, () -> 20), equalTo(11));
        assertThat(none.visit(i -> i + 1, () -> 20), equalTo(20));
    }

    @Test
    public void testStream() {
        assertThat(just.stream().toListX(), equalTo(ListX.of(10)));
        assertThat(none.stream().toListX(), equalTo(ListX.of()));
    }

    @Test
    public void testOfSupplierOfT() {

    }

    @Test
    public void testConvertTo() {
        Stream<Integer> toStream = just.visit(m -> Stream.of(m), () -> Stream.of());
        assertThat(toStream.collect(Collectors.toList()), equalTo(ListX.of(10)));
    }

    @Test
    public void testConvertToAsync() {
        Future<Stream<Integer>> async = Future
                .ofSupplier(() -> just.visit(f -> Stream.of((int) f), () -> Stream.of()));

        assertThat(async.get().collect(Collectors.toList()), equalTo(ListX.of(10)));
    }

    @Test
    public void testIterate() {
        assertThat(just.iterate(i -> i + 1).limit(10).sumInt(i->i), equalTo(145));
    }

    @Test
    public void testGenerate() {
        assertThat(just.generate().limit(10).sumInt(i->i), equalTo(100));
    }

    @Test
    public void testMapReduceReducerOfE() {
        assertThat(just.mapReduce(Reducers.toCountInt()), equalTo(1));
    }


    @Test
    public void testToXor() {
        assertThat(just.toXor(), equalTo(Xor.primary(10)));

    }

    @Test
    public void testToXorNone() {
        Xor<?, Integer> empty = none.toXor();

        assertTrue(empty.swap().map(__ -> 10).get() == 10);

    }

    @Test
    public void testToXorSecondary() {
        assertThat(just.toXor().swap(), equalTo(Xor.secondary(10)));
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
        assertThat(just.toTry(), equalTo(Try.success(10)));
    }

    @Test
    public void testToTryClassOfXArray() {
        assertTrue(none.toTry(Throwable.class).isFailure());
    }

    @Test
    public void testToIor() {
        assertThat(just.toIor(), equalTo(Ior.primary(10)));
        assertThat(Ior.fromPublisher(just), equalTo(Ior.primary(10)));

    }

    @Test
    public void testToIorNone() {
        Xor<Integer, ?> empty = none.toXor().swap();
        assertTrue(empty.isPrimary());
        assertThat(empty.map(__ -> 10), equalTo(Xor.primary(10)));

    }

    @Test
    public void testToIorSecondary() {
        assertThat(just.toIor().swap(), equalTo(Ior.secondary(10)));
    }

    @Test
    public void testToIorSecondaryNone() {
        Ior<Integer, ?> ior = none.toIor().swap().map(__ -> 10);
        assertThat(ior.get(), equalTo(10));

    }

    @Test
    public void testToEvalNow() {
        assertThat(just.toEvalNow(), equalTo(Eval.now(10)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testToEvalNowNone() {
        none.toEvalNow();
        fail("exception expected");

    }

    @Test
    public void testToEvalLater() {
        assertThat(just.toEvalLater(), equalTo(Eval.later(() -> 10)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testToEvalLaterNone() {
        none.toEvalLater().get();
        fail("exception expected");

    }

    @Test
    public void testToEvalAlways() {
        assertThat(just.toEvalAlways(), equalTo(Eval.always(() -> 10)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testToEvalAlwaysNone() {
        none.toEvalAlways().get();
        fail("exception expected");

    }



    @Test
    public void testMkString() {
        assertThat(just.mkString(), equalTo("Just[10]"));
        assertThat(none.mkString(), equalTo("Nothing[]"));
    }

    LazyReact react = new LazyReact();


    @Test
    public void testGet() {
        assertThat(just.get(), equalTo(10));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetNone() {
        none.get();

    }

    @Test
    public void testFilter() {
        assertFalse(just.filter(i -> i < 5).isPresent());
        assertTrue(just.filter(i -> i > 5).isPresent());
        assertFalse(none.filter(i -> i < 5).isPresent());
        assertFalse(none.filter(i -> i > 5).isPresent());

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
        assertTrue(just.filterNot(i -> i < 5).isPresent());
        assertFalse(just.filterNot(i -> i > 5).isPresent());
        assertFalse(none.filterNot(i -> i < 5).isPresent());
        assertFalse(none.filterNot(i -> i > 5).isPresent());
    }

    @Test
    public void testNotNull() {
        assertTrue(just.notNull().isPresent());
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
        assertThat(just.mapReduce(s -> s.toString(), Monoid.of("", Semigroups.stringJoin(","))), equalTo(",10"));
    }

    @Test
    public void testReduceMonoidOfT() {
        assertThat(just.reduce(Monoid.of(1, Semigroups.intMult)), equalTo(10));
    }

    @Test
    public void testReduceBinaryOperatorOfT() {
        assertThat(just.reduce((a, b) -> a + b), equalTo(Optional.of(10)));
    }

    @Test
    public void testReduceTBinaryOperatorOfT() {
        assertThat(just.reduce(10, (a, b) -> a + b), equalTo(20));
    }

    @Test
    public void testReduceUBiFunctionOfUQsuperTUBinaryOperatorOfU() {
        assertThat(just.reduce(11, (a, b) -> a + b, (a, b) -> a * b), equalTo(21));
    }

    @Test
    public void testReduceStreamOfQextendsMonoidOfT() {
        ListX<Integer> countAndTotal = just.reduce(Stream.of(Reducers.toCountInt(), Reducers.toTotalInt()));
        assertThat(countAndTotal, equalTo(ListX.of(1, 10)));
    }

    @Test
    public void testReduceIterableOfReducerOfT() {
        ListX<Integer> countAndTotal = just.reduce(Arrays.asList(Reducers.toCountInt(), Reducers.toTotalInt()));
        assertThat(countAndTotal, equalTo(ListX.of(1, 10)));
    }

    @Test
    public void testFoldRightMonoidOfT() {
        assertThat(just.foldRight(Monoid.of(1, Semigroups.intMult)), equalTo(10));
    }

    @Test
    public void testFoldRightTBinaryOperatorOfT() {
        assertThat(just.foldRight(10, (a, b) -> a + b), equalTo(20));
    }


    @Test
    public void testWhenFunctionOfQsuperMaybeOfTQextendsR() {

        String match = Maybe.just("data is present").visit(present -> "hello", () -> "missing");

        assertThat(just.visit(s -> "hello", () -> "world"), equalTo("hello"));
        assertThat(none.visit(s -> "hello", () -> "world"), equalTo("world"));
    }

    @Test
    public void testOrElseGet() {
        assertThat(none.orElseGet(() -> 2), equalTo(2));
        assertThat(just.orElseGet(() -> 2), equalTo(10));
    }

    @Test
    public void testToOptional() {
        assertFalse(none.toOptional().isPresent());
        assertTrue(just.toOptional().isPresent());
        assertThat(just.toOptional(), equalTo(Optional.of(10)));
    }

    @Test
    public void testToStream() {
        assertThat(none.toStream().collect(Collectors.toList()).size(), equalTo(0));
        assertThat(just.toStream().collect(Collectors.toList()).size(), equalTo(1));

    }


    @Test
    public void testOrElse() {
        assertThat(none.orElse(20), equalTo(20));
        assertThat(just.orElse(20), equalTo(10));
    }

    @Test(expected = RuntimeException.class)
    public void testOrElseThrow() {
        none.orElseThrow(() -> new RuntimeException());
    }

    @Test
    public void testOrElseThrowSome() {

        assertThat(just.orElseThrow(() -> new RuntimeException()), equalTo(10));
    }



    @Test
    public void testToFutureW() {
        Future<Integer> cf = just.toFuture();
        assertThat(cf.get(), equalTo(10));
    }

    @Test
    public void testToCompletableFuture() {
        CompletableFuture<Integer> cf = just.toCompletableFuture();
        assertThat(cf.join(), equalTo(10));
    }

    @Test
    public void testToCompletableFutureAsync() {
        CompletableFuture<Integer> cf = just.toCompletableFutureAsync();
        assertThat(cf.join(), equalTo(10));
    }

    static Executor exec = Executors.newFixedThreadPool(1);

    @Test
    public void testToCompletableFutureAsyncExecutor() {
        CompletableFuture<Integer> cf = just.toCompletableFutureAsync(exec);
        assertThat(cf.join(), equalTo(10));
    }


    @Test
    public void testIterator1() {
        assertThat(Streams.stream(just.iterator()).collect(Collectors.toList()), equalTo(Arrays.asList(10)));
    }

    @Test
    public void testForEach() {
        Mutable<Integer> capture = Mutable.of(null);
        none.forEach(c -> capture.set(c));
        assertNull(capture.get());
        just.forEach(c -> capture.set(c));
        assertThat(capture.get(), equalTo(10));
    }

    @Test
    public void testSpliterator() {
        assertThat(StreamSupport.stream(just.spliterator(), false).collect(Collectors.toList()),
                equalTo(Arrays.asList(10)));
    }

    @Test
    public void testCast() {
        Maybe<Number> num = just.cast(Number.class);
    }

    @Test
    public void testMapFunctionOfQsuperTQextendsR1() {
        assertThat(just.map(i -> i + 5), equalTo(Maybe.of(15)));
    }

    @Test
    public void testPeek() {
        Mutable<Integer> capture = Mutable.of(null);
        just = just.peek(c -> capture.set(c));
        assertNull(capture.get());

        just.get();
        assertThat(capture.get(), equalTo(10));
    }

    private Trampoline<Integer> sum(int times, int sum) {
        return times == 0 ? Trampoline.done(sum) : Trampoline.more(() -> sum(times - 1, sum + times));
    }

    @Test
    public void testTrampoline() {
        assertThat(just.trampoline(n -> sum(10, n)), equalTo(Maybe.of(65)));
    }

    @Test
    public void testUnitT1() {
        assertThat(none.unit(10), equalTo(just));
    }

	@Test
	public void testFlatMapIterable() {
		Maybe<Integer> maybe = just.flatMapI(i -> Arrays.asList(i, 20, 30));
		assertThat(maybe.get(), equalTo(10));
	}

	@Test
	public void testFlatMapPublisher() {
		Maybe<Integer> maybe = Maybe.of(100).flatMapP(i -> Flux.just(10, i));
		assertThat(maybe.get(), equalTo(10));
	}
}
