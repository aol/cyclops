package cyclops.control;

import com.oath.cyclops.types.persistent.PersistentSet;
import com.oath.cyclops.types.reactive.Completable;
import com.oath.cyclops.util.box.Mutable;


import cyclops.companion.*;
import cyclops.data.HashSet;
import cyclops.data.Seq;
import cyclops.function.Monoid;

import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import cyclops.data.tuple.Tuple;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;




public class FutureTest {

    static Executor ex = Executors.newFixedThreadPool(5);
    Future<Integer> just;
    Future<Integer> none;
    NoSuchElementException exception = new NoSuchElementException();
    @Before
    public void setUp() throws Exception {
        just = Future.of(CompletableFuture.completedFuture(10));
        none = Future.ofError(exception);
    }

    private void sleep(long time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Test
    public void nonblocking(){
        Mono<String> mono = Mono.never();
        Future<String> future = Future.fromPublisher(mono);
        Future.fromPublisher(Maybe.maybe());
        Future.fromPublisher(LazyEither.either());
        Future.fromPublisher(LazyEither3.either3());
        Future.fromPublisher(LazyEither4.either4());
        Future.fromPublisher(LazyEither5.either5());
        Future.fromPublisher(Eval.eval());


    }
    @Test
    public void setCorrectly(){

        Seq<Completable<String>> completables = Seq.of(Maybe.<String>maybe(),
                                                        LazyEither.either(),
                                                        LazyEither3.either3(),
                                                        LazyEither4.either4(),
                                                        LazyEither5.either5(),
                                                        Eval.eval());


        for(Completable<String> c : completables) {
            Future<String> f = Future.fromPublisher((Publisher<String>)c);
            c.complete("hello");
            assertThat(f.get(),equalTo(Try.success("hello")));
        }

    }

    
    @Test
    public void completableFuture(){
        System.out.println("Thread " + Thread.currentThread().getId());
        LazyEither.CompletableEither<String, String> completable = LazyEither.<String>either();
        Try<String,Throwable> async = Try.fromEither(completable);

        Future<String> future = Future.fromPublisher(completable)
                                        .peek(System.out::println)
                                        .peek(i->System.out.println("Thread " + Thread.currentThread().getId()));

        new Thread(()->{
            completable.complete("hello world");
        }).start();

        future.get();

    }
    @Test
    public void combine(){

        Monoid<Integer> add = Monoid.of(0,Semigroups.intSum);

        System.out.println("Just " + just.zip(add,none));
        assertThat(just.zip(add,none).orElse(-1),equalTo(none.orElse(-1)));


    }
    @Test
    public void sub(){
        Future<Integer> f = Future.future();

        Spouts.from(f).forEachSubscribe(System.out::println);

        f.complete(10);
    }

    @Test
    public void testVisitAsync(){

      int r =  Future.ofResult(10)
                      .visitAsync(i->i*2, e->-1).toOptional()
                      .get();
        assertThat(20,equalTo(r));
    }
    @Test
    public void testVisitFailAsync(){

      int r =  Future.<Integer>ofError(new RuntimeException())
                      .visitAsync(i->i*2, e->-1).toOptional()
                      .get();
        assertThat(-1,equalTo(r));
    }
    @Test
    public void testVisit(){

      int r =  Future.ofResult(10)
                      .fold(i->i*2, e->-1);
        assertThat(20,equalTo(r));
    }
    @Test
    public void testVisitFail(){

      int r =  Future.<Integer>ofError(new RuntimeException())
                      .fold(i->i*2, e->-1);
        assertThat(-1,equalTo(r));
    }

    @Test
    public void testFirstSuccess(){

        Future<Integer> ft = Future.future();
        Future<Integer> result = Future.firstSuccess(Future.of(()->1),ft);

        ft.complete(10);
        assertThat(result.get(), is(equalTo(Try.success(1))));
    }


    @Test
    public void testZip(){
        assertThat(Future.ofResult(10).zip(Eval.now(20),(a, b)->a+b).orElse(-100),equalTo(30));
        assertThat(Future.ofResult(10).zip((a, b)->a+b, Eval.now(20)).orElse(-100),equalTo(30));
        assertThat(Future.ofResult(10).zip(ReactiveSeq.of(20),(a, b)->a+b).orElse(-100),equalTo(30));
        assertThat(Future.ofResult(10).zip(ReactiveSeq.of(20)).orElse(null),equalTo(Tuple.tuple(10,20)));
        assertThat(Future.ofResult(10).zip(Eval.now(20)).orElse(null),equalTo(Tuple.tuple(10,20)));
    }



    @Test
    public void apNonBlocking(){

       Future<String> f =  Future.of(()->{ sleep(1000l); return "hello";},ex)
                                .zip(Future.of(()->" world",ex),String::concat);


      System.out.println("hello");
      long start=System.currentTimeMillis();
      System.out.println(f.get());
      assertThat(System.currentTimeMillis()-start,greaterThan(400l));
    }

    @Test
    public void FutureFromIterableTest() {

        ReactiveSeq<Integer> stream = ReactiveSeq.of(1, 2, 3);

        Future<Integer> maybe = Future.fromPublisher(stream);
        assertThat(maybe.toOptional().get(), equalTo(1));

    }

    @Test
    public void FutureAsyncFromIterableTest() {

        ReactiveSeq<Integer> stream = ReactiveSeq.of(1, 2, 3);

        Future<Integer> maybe = Future.fromIterable(stream, ex);
        assertThat(maybe.toOptional().get(), equalTo(1));

    }

    @Test
    public void scheduleDelay(){

        long start = System.currentTimeMillis();
        String res = Future.schedule(100, Executors.newScheduledThreadPool(1), ()->"hello").toOptional()
                            .get();

        assertThat(100l,lessThan(System.currentTimeMillis()-start));
        assertThat(res,equalTo("hello"));

    }
    @Test
    public void scheduleCron(){

        long start = System.currentTimeMillis();
        Future.schedule("* * * * * ?", Executors.newScheduledThreadPool(1), ()->"hello").toOptional()
                            .get();

        String res = Future.schedule("* * * * * ?", Executors.newScheduledThreadPool(1), ()->"hello").toOptional()
                .get();
        assertThat(1000l,lessThan(System.currentTimeMillis()-start));
        assertThat(res,equalTo("hello"));

    }


    @Test
    public void combine2(){
        Monoid<Integer> add = Monoid.of(0,Semigroups.intSum);

        assertThat(just.zip(add, Maybe.just(10)).toMaybe(),equalTo(Maybe.just(20)));
        Monoid<Integer> firstNonNull = Monoid.of(null , Semigroups.firstNonNull());
        assertThat(just.zip(firstNonNull,Future.ofResult(null)).get(),equalTo(just.get()));

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
        Future<ReactiveSeq<Integer>> maybes = Future.sequence(Arrays.asList(just, Future.ofResult(1)));
        assertThat(maybes.map(s->s.toList()).orElse(Arrays.asList()),equalTo(Arrays.asList(10,1)));
    }
    @Test
    public void testSequenceCF() {
        CompletableFuture<ReactiveSeq<Integer>> maybes = CompletableFutures.sequence(Arrays.asList(just.getFuture(),none.getFuture(), Future.ofResult(1).getFuture()));
        assertThat(maybes.isCompletedExceptionally(),equalTo(true));

    }
    @Test
    public void testAccumulateSuccessSemigroup() {
        Future<Integer> maybes = Future.accumulateSuccess(Monoid.of(0,(a,b)->a+1),Arrays.asList(just,none, Future.ofResult(1)));

        assertThat(maybes.get(),equalTo(Try.success(2)));
    }
    @Test
    public void testAccumulateSuccess() {
        Future<PersistentSet<Integer>> maybes = Future.accumulateSuccess(Arrays.asList(just,none, Future.ofResult(1)), Reducers.toPersistentSet());

        assertThat(maybes.get(),equalTo(Try.success(HashSet.of(10,1))));
    }
    @Test @Ignore
    public void testAccumulateJNonBlocking() {
        Future<PersistentSet<Integer>> maybes = Future.accumulateSuccess(Arrays.asList(just,none, Future.of(()->{while(true){System.out.println("hello");}},Executors.newFixedThreadPool(1)), Future.ofResult(1)),Reducers.toPersistentSet());
        System.out.println("not blocked");

    }
    @Test
    public void testAccumulateNoValue() {
        Future<String> maybes = Future.accumulate(Arrays.asList(), i->""+i,Monoids.stringConcat);
        assertThat(maybes.orElse("npo"),equalTo(""));
    }
    @Test
    public void testAccumulateOneValue() {
        Future<String> maybes = Future.accumulate(Arrays.asList(just), i->""+i,Monoids.stringConcat);
        assertThat(maybes.get(),equalTo(Try.success("10")));
    }
    @Test
    public void testAccumulateJustCollectionXOfMaybeOfTFunctionOfQsuperTRSemigroupOfR() {
        Future<String> maybes = Future.accumulate(Arrays.asList(just, Future.ofResult(1)), i->""+i,Monoids.stringConcat);
        assertThat(maybes.get(),equalTo(Try.success("101")));
    }
    @Test
    public void testAccumulateJust() {
        Future<Integer> maybes = Future.accumulate(Monoids.intSum,Arrays.asList(just, Future.ofResult(1)));
        assertThat(maybes.get(),equalTo(Try.success(11)));
    }
    @Test
    public void testAccumulateError() {
        Future<Integer> maybes = Future.accumulate(Monoids.intSum,Arrays.asList(none, Future.ofResult(1)));
        assertTrue(maybes.isFailed());
    }


    @Test
    public void testUnitT() {
        assertThat(just.unit(20).get(),equalTo(Future.ofResult(20).get()));
    }



    @Test
    public void testisPrimary() {
        assertTrue(just.isSuccess());
        assertTrue(none.isFailed());
    }


    @Test
    public void testMapFunctionOfQsuperTQextendsR() {
        assertThat(just.map(i->i+5).get(),equalTo(Try.success(15)));
        assertTrue(none.map(i->i+5).isFailed());
    }

    @Test
    public void testFlatMap() {
        assertThat(just.flatMap(i-> Future.ofResult(i+5) ).get(),equalTo(Future.ofResult(15).get() ));
        assertTrue(none.flatMap(i-> Future.ofResult(i+5)).isFailed());
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
        assertThat(just.asSupplier(-100).iterate(i->i+1).limit(10).sumInt(i->i),equalTo(145));
    }

    @Test
    public void testGenerate() {
        assertThat(just.asSupplier(-100).generate().limit(10).sumInt(i->i),equalTo(100));
    }



    @Test
    public void testToXor() {
        assertThat(just.toEither(-5000),equalTo(Either.right(10)));

    }
    @Test
    public void testToXorNone(){
        Either<Throwable,Integer> xor = none.toEither();
        assertTrue(xor.isLeft());
        assertThat(xor,equalTo(Either.left(exception)));

    }


    @Test
    public void testToXorSecondary() {
        assertThat(just.toEither(-5000).swap(),equalTo(Either.left(10)));
    }

    @Test
    public void testToXorSecondaryNone(){
        Either<Integer, Throwable> xorNone = none.toEither().swap();
        assertThat(xorNone,equalTo(Either.right(exception)));

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
        Ior<Integer, Throwable> ior = none.toIor().swap();
        assertTrue(ior.isRight());
        assertThat(ior,equalTo(Ior.right(exception)));

    }


    @Test
    public void testToIorSecondary() {
        assertThat(just.toIor().swap(),equalTo(Ior.left(10)));
    }


    @Test
    public void testToIorSecondaryNone(){
        Ior<Integer,Throwable> ior = none.toIor().swap();
        assertTrue(ior.isRight());
        assertThat(ior,equalTo(Ior.right(exception)));

    }



    @Test
    public void testMkString() {
        assertThat(just.mkString(),containsString("Future["));
        assertThat(none.mkString(),containsString("Future["));
    }


    @Test
    public void testGet() {
        assertThat(just.get(),equalTo(Try.success(10)));
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
    public void testToCompletableFuture() {
        CompletableFuture<Integer> cf = just.toCompletableFuture();
        assertThat(cf.join(),equalTo(10));
    }


    Executor exec = Executors.newFixedThreadPool(1);





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
        assertThat(just.map(i->i+5).get(),equalTo(Try.success(15)));
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
    public void mapBoth(){
        assertThat(Future.ofResult(1).map(i->i*2, e->-1).get(),equalTo(Try.success(2)));
    }
    @Test
    public void testUnitT1() {
        assertThat(none.unit(10).get(),equalTo(just.get()));
    }

    @Test
    public void testRecover() {
        assertThat(Future.ofError(new RuntimeException()).recover(__ -> true).get(), equalTo(Try.success(true)));
    }
    @Test
    public void testRecoverSupplier() {
        assertThat(Future.ofError(new RuntimeException()).recover(() -> true).get(), equalTo(Try.success(true)));
    }



    @Test
    public void testFlatMapPublisher() {
        Future<Integer> f = just.mergeMap(i -> Flux.just(100, i));
        assertThat(f.get(), equalTo(Try.success(100)));
    }

}
