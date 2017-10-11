package com.aol.cyclops2.control;

import com.aol.cyclops2.util.box.Mutable;
import cyclops.collections.immutable.PersistentSetX;
import cyclops.async.LazyReact;
import cyclops.collections.mutable.ListX;
import com.aol.cyclops2.types.Zippable;
import com.aol.cyclops2.types.mixins.Printable;
import cyclops.async.Future;
import cyclops.companion.Monoids;
import cyclops.companion.Reducers;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.control.*;
import cyclops.control.lazy.Eval;
import cyclops.control.lazy.Maybe;
import cyclops.control.lazy.Maybe.CompletableMaybe;
import cyclops.control.lazy.Trampoline;
import cyclops.function.Monoid;
import cyclops.stream.Spouts;
import cyclops.collections.tuple.Tuple3;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static cyclops.control.lazy.Maybe.just;
import static org.hamcrest.Matchers.equalTo;
import static cyclops.collections.tuple.Tuple.tuple;
import static org.junit.Assert.*;

public class MaybeTest implements Printable {

    Maybe<Integer> just;
    Maybe<Integer> none;

    @Before
    public void setUp() throws Exception {
        just = Maybe.just(10);
        none = Maybe.nothing();
        cap =0;

    }

    int cap =0;


    @Test
    public void lazy(){

        Maybe.just(10)
                .peek(i->cap=i);

        assertThat(cap,equalTo(0));

    }
    @Test
    public void completableTest(){
        CompletableMaybe<Integer,Integer> completable = Maybe.maybe();
        Maybe<Integer> mapped = completable.map(i->i*2)
                                          .flatMap(i-> Eval.later(()->i+1));

        completable.complete(5);
        System.out.println(mapped.getClass());
        mapped.printOut();
        assertThat(mapped.toOptional().get(),equalTo(11));


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
                .forEachAsync(e->result.complete(e));


        assertFalse(result.isDone());
        System.out.println("Blocking?");
        assertThat(result.toOptional().get(),equalTo(2000));

    }
    
    @Test
    public void recoverWith(){
        assertThat(none.recoverWith(()->Maybe.just(10)).toOptional().get(),equalTo(10));
        assertThat(none.recoverWith(()->Maybe.nothing()).toOptional().isPresent(),equalTo(false));
        assertThat(just.recoverWith(()->Maybe.just(5)).toOptional().get(),equalTo(10));
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
    public void fib() {
        System.out.println(fibonacci(just(tuple(100_000, 1l, 0l))));
    }

    public Maybe<Long> fibonacci(Maybe<Tuple3<Integer, Long, Long>> fib) {
        return fib.flatMap(t -> t._1() == 0 ? just(t._3()) : fibonacci(just(tuple(t._1() - 1, t._2() + t._3(), t._2()))));
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
        assertThat(just.nest().map(m -> m.toOptional().get()), equalTo(just));
        assertThat(none.nest().map(m -> m.toOptional().get()), equalTo(none));
    }

    @Test
    public void coFlatMap() {

        Maybe.nothing().coflatMap(m -> m.isPresent() ? m.toOptional().get() : 10);

        // Maybe[10]

        assertThat(just.coflatMap(m -> m.isPresent() ? m.toOptional().get() : 50), equalTo(just));
        assertThat(none.coflatMap(m -> m.isPresent() ? m.toOptional().get() : 50), equalTo(Maybe.of(50)));
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
        System.out.println(even(Maybe.just(200000)));

    }
    @Test
    public void oddBug() {

        even(Maybe.just(200000)).visit(i->{
            System.out.println(i);
            return null;
        },()->null);
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

        assertThat(maybes, equalTo(Maybe.just(1).flatMap(i -> Maybe.nothing())));
    }

    @Test
    public void testSequence() {
        Maybe<ListX<Integer>> maybes = Maybe.sequence(ListX.of(just, none, Maybe.of(1)));

        assertThat(maybes, equalTo(Maybe.nothing()));
    }

    @Test
    public void testSequenceJust() {
        Maybe<ListX<Integer>> maybes = Maybe.sequenceJust(ListX.of(just, none, Maybe.of(1)));
        assertThat(maybes, equalTo(Maybe.of(ListX.of(10, 1))));
    }

    @Test
    public void testAccumulateJustCollectionXOfMaybeOfTReducerOfR() {
        Maybe<PersistentSetX<Integer>> maybes = Maybe.accumulateJust(ListX.of(just, none, Maybe.of(1)), Reducers.toPersistentSetX());
        assertThat(maybes, equalTo(Maybe.of(PersistentSetX.of(10, 1))));
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
    public void testMapFunctionOfQsuperTQextendsR() {
        assertThat(just.map(i -> i + 5), equalTo(Maybe.of(15)));
        assertThat(none.map(i -> i + 5), equalTo(Maybe.nothing()));
    }

    @Test
    public void testFlatMap() {

        assertThat(just.flatMap(i -> Maybe.of(i + 5)), equalTo(Maybe.of(15)));
        assertThat(none.flatMap(i -> Maybe.of(i + 5)), equalTo(Maybe.nothing()));
    }

    @Test
    public void testFlatMapAndRecover() {
        assertThat(just.flatMap(i -> Maybe.nothing()).recover(15), equalTo(Maybe.of(15)));
        assertThat(just.flatMap(i -> Maybe.nothing()).recover(() -> 15), equalTo(Maybe.of(15)));
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
                .of(() -> just.visit(f -> Stream.of((int) f), () -> Stream.of()));

        assertThat(async.toOptional().get().collect(Collectors.toList()), equalTo(ListX.of(10)));
    }

    @Test
    public void testIterate() {
        assertThat(just.iterate(i -> i + 1,-1000).limit(10).sumInt(i->i), equalTo(145));
    }

    @Test
    public void testGenerate() {
        assertThat(just.generate(-10000).limit(10).sumInt(i->i), equalTo(100));
    }


    @Test
    public void testToXor() {
        assertThat(just.toEither(-5000), equalTo(Either.right(10)));

    }

    @Test
    public void testToXorNone() {
        Either<?, Integer> empty = none.toEither(-50000);

        assertTrue(empty.swap().map(__ -> 10).toOptional().get() == 10);

    }

    @Test
    public void testToXorSecondary() {
        assertThat(just.toEither(-5000).swap(), equalTo(Either.left(10)));
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
        assertThat(just.toTry(),equalTo(Try.success(10)));
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
        assertThat(just.mkString(), equalTo("Just[10]"));
        assertThat(none.mkString(), equalTo("Nothing[]"));
    }

    LazyReact react = new LazyReact();


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
    public void testFoldRightMonoidOfT() {
        assertThat(just.fold(Monoid.of(1, Semigroups.intMult)), equalTo(10));
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
        assertThat(none.stream().collect(Collectors.toList()).size(), equalTo(0));
        assertThat(just.stream().collect(Collectors.toList()).size(), equalTo(1));

    }


    @Test
    public void testOrElse() {
        assertThat(none.orElse(20), equalTo(20));
        assertThat(just.orElse(20), equalTo(10));
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

        just.toOptional().get();
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
		assertThat(maybe.toOptional().get(), equalTo(10));
	}

	@Test
	public void testFlatMapPublisher() {
		Maybe<Integer> maybe = Maybe.of(100).flatMapP(i -> Flux.just(10, i));
		assertThat(maybe.toOptional().get(), equalTo(10));
	}
}
