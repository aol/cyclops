package cyclops.control;

import com.oath.cyclops.types.Zippable;
import com.oath.cyclops.types.mixins.Printable;
import com.oath.cyclops.types.persistent.PersistentSet;
import cyclops.companion.Monoids;
import cyclops.companion.Reducers;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import com.oath.cyclops.util.box.Mutable;

import cyclops.control.Maybe.CompletableMaybe;
import cyclops.data.HashSet;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple3;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.equalTo;
import static cyclops.data.tuple.Tuple.tuple;
import static org.junit.Assert.*;

public class CompletableMaybeTest implements Printable {

    Maybe<Integer> just;
    Maybe<Integer> none;
    public static <T> Maybe.CompletableMaybe<T,T> just(T value){
        Maybe.CompletableMaybe<T,T> completable = Maybe.maybe();
        completable.complete(value);
        return completable;
    }
    public static <T> Maybe.CompletableMaybe<T,T> none(){
        Maybe.CompletableMaybe<T,T> completable = Maybe.maybe();
        completable.complete(null);
        return completable;
    }
    @Before
    public void setUp() throws Exception {
        just = just(10);
        none = none();

    }
    @Test
    public void completableTest(){
        CompletableMaybe<Integer,Integer> completable = Maybe.maybe();
        Maybe<Integer> mapped = completable.map(i->i*2)
                                          .flatMap(i-> Eval.later(()->i+1));

        completable.complete(5);
        System.out.println(mapped.getClass());
        mapped.printOut();
        assertThat(mapped.orElse(-5),equalTo(11));


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
        assertThat(result.get(),equalTo(Try.success(2000)));

    }

    @Test
    public void recoverWith(){
        assertThat(none.recoverWith(()->CompletableMaybeTest.just(10)).orElse(null),equalTo(10));
        assertThat(none.recoverWith(()->Maybe.nothing()).isPresent(),equalTo(false));
        assertThat(just.recoverWith(()->CompletableMaybeTest.just(5)).orElse(null),equalTo(10));
    }

    boolean lazy = true;

    @Test
    public void lazyTest() {
        CompletableMaybeTest.just(10)
             .flatMap(i -> { lazy=false; return CompletableMaybeTest.just(15);})
             .map(i -> { lazy=false; return   CompletableMaybeTest.just(15);})
             .map(i -> Maybe.of(20));


        assertTrue(lazy);

    }
    @Test
    public void testZipMonoid(){
        BinaryOperator<Zippable<Integer>> sumMaybes = Semigroups.combineScalarFunctors(Semigroups.intSum);
        assertThat(sumMaybes.apply(Maybe.just(5),Maybe.just(1)),equalTo(Maybe.just(6)));

    }



    @Test
    public void testZip() {
        assertThat(CompletableMaybeTest.just(10).zip(Eval.now(20), (a, b) -> a + b).orElse(null), equalTo(30));
        assertThat(CompletableMaybeTest.just(10).zip((a, b) -> a + b, Eval.now(20)).orElse(null), equalTo(30));
              assertThat(CompletableMaybeTest.just(10).zip(ReactiveSeq.of(20), (a, b) -> a + b).orElse(null), equalTo(30));
        assertThat(CompletableMaybeTest.just(10).zip(ReactiveSeq.of(20)).orElse(null), equalTo(Tuple.tuple(10, 20)));
        assertThat(CompletableMaybeTest.just(10).zip(Eval.now(20)).orElse(null), equalTo(Tuple.tuple(10, 20)));
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
        assertThat(just.nest().map(m -> m.toOptional().get()), equalTo(Maybe.just(10)));
        assertThat(none.nest().map(m -> m.toOptional().get()), equalTo(Maybe.nothing()));
    }


    @Test
    public void combine() {
        Monoid<Integer> add = Monoid.of(0, Semigroups.intSum);
        assertThat(just.zip(add, none), equalTo(Maybe.nothing()));
        assertThat(none.zip(add, just), equalTo(Maybe.nothing()));
        assertThat(none.zip(add, none), equalTo(Maybe.nothing()));
        assertThat(just.zip(add, CompletableMaybeTest.just(10)), equalTo(Maybe.just(20)));


    }

    @Test
    public void optionalVMaybe() {

        Optional.of(10).map(i -> print("optional " + (i + 10)));

        CompletableMaybeTest.just(10).map(i -> print("maybe " + (i + 10)));

    }

    @Test
    public void odd() {
        System.out.println(even(CompletableMaybeTest.just(200000)).orElse(null));
    }

    public Maybe<String> odd(Maybe<Integer> n) {

        return n.flatMap(x -> even(CompletableMaybeTest.just(x - 1)));
    }

    public Maybe<String> even(Maybe<Integer> n) {
        return n.flatMap(x -> {
            return x <= 0 ? CompletableMaybeTest.just("done") : odd(CompletableMaybeTest.just(x - 1));
        });
    }


    @Test
    public void testFilteringNoValue() {
        assertThat(ReactiveSeq.of(1, 1).filter(i->i==1).toList(), equalTo(Arrays.asList(1, 1)));
    }

    @Test
    public void testToMaybe() {
        assertThat(just.toMaybe(), equalTo(Maybe.just(10)));
        assertThat(none.toMaybe(), equalTo(Maybe.nothing()));
    }

    private int add1(int i) {
        return i + 1;
    }





    @Test
    public void testFromEvalSome() {
        assertThat(Maybe.fromEval(Eval.now(10)), equalTo(Maybe.just(10)));
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
        Maybe<ReactiveSeq<Integer>> maybes = Maybe.sequence(Arrays.asList(just, none, Maybe.of(1)));

        assertThat(maybes, equalTo(CompletableMaybeTest.just(1).flatMap(i -> Maybe.nothing())));
    }

    @Test
    public void testSequence() {
        Maybe<ReactiveSeq<Integer>> maybes = Maybe.sequence(Arrays.asList(just, none, Maybe.of(1)));

        assertThat(maybes, equalTo(Maybe.nothing()));
    }

    @Test
    public void testSequenceJust() {
        Maybe<ReactiveSeq<Integer>> maybes = Maybe.sequenceJust(Arrays.asList(just, none, Maybe.of(1)));
        assertThat(maybes.map(s->s.toList()), equalTo(Maybe.of(Arrays.asList(10, 1))));
    }

    @Test
    public void testAccumulateJustCollectionXOfMaybeOfTReducerOfR() {
        Maybe<PersistentSet<Integer>> maybes = Maybe.accumulateJust(Arrays.asList(just, none, Maybe.of(1)), Reducers.toPersistentSet());
        assertThat(maybes, equalTo(Maybe.of(HashSet.of(10, 1))));
    }

    @Test
    public void testAccumulateJustCollectionXOfMaybeOfTFunctionOfQsuperTRSemigroupOfR() {
        Maybe<String> maybes = Maybe.accumulateJust(Arrays.asList(just, none, Maybe.of(1)), i -> "" + i,
                Monoids.stringConcat);
        assertThat(maybes, equalTo(Maybe.of("101")));
    }

    @Test
    public void testAccumulateJust() {
        Maybe<Integer> maybes = Maybe.accumulateJust(Monoids.intSum,Arrays.asList(just, none, Maybe.of(1)));
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
        assertThat(just.stream().toList(), equalTo(Arrays.asList(10)));
        assertThat(none.stream().toList(), equalTo(Arrays.asList()));
    }

    @Test
    public void testOfSupplierOfT() {

    }

    @Test
    public void testConvertTo() {
        Stream<Integer> toStream = just.visit(m -> Stream.of(m), () -> Stream.of());
        assertThat(toStream.collect(Collectors.toList()), equalTo(Arrays.asList(10)));
    }

    @Test
    public void testConvertToAsync() {
        Future<Stream<Integer>> async = Future
                .of(() -> just.visit(f -> Stream.of((int) f), () -> Stream.of()));

        assertThat(async.orElse(Stream.empty()).collect(Collectors.toList()), equalTo(Arrays.asList(10)));
    }

    @Test
    public void testIterate() {

        assertThat(just.asSupplier(-100).iterate(i -> i + 1).limit(10).sumInt(i->i), equalTo(145));
    }

    @Test
    public void testGenerate() {
        assertThat(just.asSupplier(-100).generate().limit(10).sumInt(i->i), equalTo(100));
    }



    @Test
    public void testToXor() {
        assertThat(just.toEither(-50), equalTo(Either.right(10)));

    }

    @Test
    public void testToXorNone() {
        Either<?, Integer> empty = none.toEither(-10);

        assertTrue(empty.swap().map(__ -> 10).orElse(6000) == 10);

    }

    @Test
    public void testToXorSecondary() {
        assertThat(just.toEither(-400).swap(), equalTo(Either.left(10)));
    }

    @Test
    public void testToXorSecondaryNone() {
        Either<Integer, ?> empty = none.toEither(-100).swap();
        assertTrue(empty.isRight());
        assertThat(empty.map(__ -> 10), equalTo(Either.right(10)));

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
    public void testToIorNone() {
        Either<Integer, ?> empty = none.toEither(-400).swap();
        assertTrue(empty.isRight());
        assertThat(empty.map(__ -> 10), equalTo(Either.right(10)));

    }


    @Test
    public void testMkString() {
        assertThat(just.mkString(), equalTo("CompletableMaybe[10]"));
        assertThat(none.mkString(), equalTo("CompletableMaybe[]"));
    }


    @Test
    public void testGet() {
        assertThat(just.orElse(-100), equalTo(10));
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
    public void testFoldRightMonoidOfT() {
        assertThat(just.fold(Monoid.of(1, Semigroups.intMult)), equalTo(10));
    }




    @Test
    public void testWhenFunctionOfQsuperMaybeOfTQextendsR() {

        String match = CompletableMaybeTest.just("data is present").visit(present -> "hello", () -> "missing");

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








    static Executor exec = Executors.newFixedThreadPool(1);


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
    public void testMapFunctionOfQsuperTQextendsR1() {
        assertThat(just.map(i -> i + 5), equalTo(Maybe.of(15)));
    }

    @Test
    public void testPeek() {
        Mutable<Integer> capture = Mutable.of(null);
        just = just.peek(c -> capture.set(c));
        assertNull(capture.get());

        just.orElse(20);
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
        assertThat(none.unit(10), equalTo(Maybe.just(10)));
    }

	@Test
	public void testFlatMapIterable() {
		Maybe<Integer> maybe = just.concatMap(i -> Arrays.asList(i, 20, 30));
		assertThat(maybe.orElse(50), equalTo(10));
	}

	@Test
	public void testFlatMapPublisher() {
		Maybe<Integer> maybe = Maybe.of(100).mergeMap(i -> Flux.just(10, i));
		assertThat(maybe.orElse(500), equalTo(10));
	}
}
