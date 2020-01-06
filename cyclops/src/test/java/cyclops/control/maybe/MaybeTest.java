package cyclops.control.maybe;

import com.oath.cyclops.types.MonadicValue;
import com.oath.cyclops.types.persistent.PersistentSet;
import com.oath.cyclops.util.box.Mutable;


import com.oath.cyclops.types.Zippable;
import com.oath.cyclops.types.mixins.Printable;
import cyclops.companion.Monoids;
import cyclops.companion.Reducers;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.control.AbstractOptionTest;
import cyclops.control.AbstractValueTest;
import cyclops.control.Either;
import cyclops.control.Eval;
import cyclops.control.Future;
import cyclops.control.LazyEither5;
import cyclops.control.Maybe;
import cyclops.control.Maybe.CompletableMaybe;
import cyclops.control.Option;
import cyclops.control.Trampoline;
import cyclops.control.Try;
import cyclops.data.HashSet;
import cyclops.data.Seq;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.data.tuple.Tuple3;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static cyclops.control.Maybe.just;
import static org.hamcrest.Matchers.equalTo;
import static cyclops.data.tuple.Tuple.tuple;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;


public class MaybeTest extends AbstractOptionTest implements Printable {

    Maybe<Integer> just;
    Maybe<Integer> none;

    @Before
    public void setUp() throws Exception {
        lazy = true;
        just = Maybe.just(10);
        none = Maybe.nothing();
        cap =0;

    }

    int cap =0;

    @Test
    public void filterLazy(){
        Maybe.just(10)
            .filter(i->{
                lazy = false;
                return i>10;
            });

        assertTrue(lazy);
    }

    boolean lazy = true;


    @Test
    public void nullPublisher(){

        assertThat(Maybe.fromPublisher(Seq.of(null,190)),not(equalTo(Maybe.just(null))));
    }
    @Test
    public void fromNull(){
        System.out.println(Maybe.fromIterable(Seq.of().plus(null)));
        assertThat(Maybe.just(null), not(equalTo(Maybe.nothing())));
        assertThat(Maybe.nothing(), not(equalTo(Maybe.just(null))));
        assertThat(Maybe.fromIterable(Seq.of()),equalTo(Maybe.nothing()));
        assertThat(Maybe.fromIterable(Seq.of().plus(null)),equalTo(Maybe.just(null)));
        System.out.println(Maybe.fromPublisher(Seq.of(null,190)));
        assertThat(Maybe.fromPublisher(Seq.of(null,190)),not(equalTo(Maybe.just(null))));
        assertThat(Maybe.fromFuture(Future.ofResult(null)),equalTo(Maybe.just(null)));
        assertThat(Maybe.fromOption(Option.some(null)),equalTo(Maybe.just(null)));
    }
    @Test
    public void fromNullOption(){
        System.out.println(Maybe.fromIterable(Seq.of().plus(null)));
        assertThat(Maybe.just(null), not(equalTo(Option.none())));
        assertThat(Maybe.nothing(), not(equalTo(Option.some(null))));
        assertThat(Maybe.fromIterable(Seq.of()),equalTo(Option.none()));
        assertThat(Maybe.fromIterable(Seq.of().plus(null)),equalTo(Option.some(null)));
        System.out.println(Maybe.fromPublisher(Seq.of(null,190)));
        assertThat(Maybe.fromPublisher(Seq.of(null,190)),not(equalTo(Option.some(null))));
        assertThat(Maybe.fromFuture(Future.ofResult(null)),equalTo(Option.some(null)));
        assertThat(Maybe.fromOption(Option.some(null)),equalTo(Option.some(null)));
    }
  @Test
  public void testMaybeWithNull() {
      assertThat(Maybe.fromEval(Eval.later(()->null)),equalTo(Maybe.just(null)));
      assertThat(Maybe.fromEvalNullable(Eval.later(()->null)),equalTo(Maybe.nothing()));
      assertThat(Maybe.fromLazyOption(Eval.later(()->Option.none())),equalTo(Maybe.nothing()));
    System.out.println(Maybe.of(null).toString());
    System.out.println(Maybe.just(null).toString());
    System.out.println(Either.left(null).toString());
    System.out.println(Either.right(null).toString());

    System.out.println(Eval.defer(()-> Eval.now(null)).toString());
    System.out.println(Eval.later(()->null).toString());
    System.out.println(LazyEither5.left1(null).toString());
    System.out.println(LazyEither5.left2(null).toString());
    System.out.println(LazyEither5.left3(null).toString());
    System.out.println(LazyEither5.left4(null).toString());
    System.out.println(LazyEither5.right(null).toString());

    System.out.println(Option.some(null).toString());
    System.out.println(Option.of(null).toString());
  }
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

        completable.completeAsNone();

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

        assertThat(sumMaybes.apply(Maybe.just(1),Maybe.just(5)),equalTo(Maybe.just(6)));

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
    public void combine() {
        Monoid<Integer> add = Monoid.of(0, Semigroups.intSum);
        assertThat(just.zip(add, none), equalTo(none));
        assertThat(none.zip(add, just), equalTo(none));
        assertThat(none.zip(add, none), equalTo(none));
        assertThat(just.zip(add, Maybe.just(10)), equalTo(Maybe.just(20)));
        Monoid<Integer> firstNonNull = Monoid.of(null, Semigroups.firstNonNull());
        assertThat(just.zip(firstNonNull, none), equalTo(none));

    }

    @Test
    public void asyncZip(){
       CompletableMaybe<Integer,Integer> cm =  Maybe.maybe();
        System.out.println("Test ");
        Maybe<Tuple2<Integer, Integer>> m = cm.zip(Tuple::tuple, Maybe.just(10));
        System.out.println("Blocked ? ");
       assertThat(m.getClass(),equalTo(Maybe.Lazy.class));
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

        even(Maybe.just(200000)).fold(i->{
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
        Maybe<ReactiveSeq<Integer>> maybes = Maybe.sequence(Arrays.asList(just, none, Maybe.of(1)));

        assertThat(maybes, equalTo(Maybe.just(1).flatMap(i -> Maybe.nothing())));
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
        assertThat(just.fold(i -> i + 1, () -> 20), equalTo(11));
        assertThat(none.fold(i -> i + 1, () -> 20), equalTo(20));
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
        Stream<Integer> toStream = just.fold(m -> Stream.of(m), () -> Stream.of());
        assertThat(toStream.collect(Collectors.toList()), equalTo(Arrays.asList(10)));
    }

    @Test
    public void testConvertToAsync() {
        Future<Stream<Integer>> async = Future
                .of(() -> just.fold(f -> Stream.of((int) f), () -> Stream.of()));

        assertThat(async.toOptional().get().collect(Collectors.toList()), equalTo(Arrays.asList(10)));
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

        String match = Maybe.just("data is present").fold(present -> "hello", () -> "missing");

        assertThat(just.fold(s -> "hello", () -> "world"), equalTo("hello"));
        assertThat(none.fold(s -> "hello", () -> "world"), equalTo("world"));
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
    public void testUnitT1() {
        assertThat(none.unit(10), equalTo(just));
    }

	@Test
	public void testFlatMapIterable() {
		Maybe<Integer> maybe = just.concatMap(i -> Arrays.asList(i, 20, 30));
		assertThat(maybe.toOptional().get(), equalTo(10));
	}

	@Test
	public void testFlatMapPublisher() {
		Maybe<Integer> maybe = Maybe.of(100).mergeMap(i -> Flux.just(10, i));
		assertThat(maybe.toOptional().get(), equalTo(10));
	}

  @Override
  protected <T> Maybe<T> of(T value) {
    return Maybe.just(value);
  }

  @Override
  protected <T> Maybe<T> empty() {
    return Maybe.nothing();
  }

  @Override
  protected <T> Maybe<T> fromPublisher(Publisher<T> p) {
    return Maybe.fromPublisher(p);
  }
}
