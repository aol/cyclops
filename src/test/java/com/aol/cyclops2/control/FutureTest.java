package com.aol.cyclops2.control;

import cyclops.*;
import cyclops.box.Mutable;
import cyclops.collections.immutable.PSetX;
import cyclops.async.LazyReact;
import cyclops.collections.ListX;
import cyclops.async.Future;
import cyclops.control.*;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Spouts;
import lombok.val;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.Flux;

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

    Executor ex = Executors.newFixedThreadPool(5);
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
    public void sub(){
        Future<Integer> f = Future.future();

        Spouts.from(f).subscribe(System.out::println);

        f.complete(10);
    }
    
    @Test
    public void testVisitAsync(){
       
      int r =  Future.ofResult(10)
                      .visitAsync(i->i*2, e->-1)
                      .get();
        assertThat(20,equalTo(r));
    }
    @Test
    public void testVisitFailAsync(){
        
      int r =  Future.<Integer>ofError(new RuntimeException())
                      .visitAsync(i->i*2, e->-1)
                      .get();
        assertThat(-1,equalTo(r));
    }
    @Test
    public void testVisit(){
       
      int r =  Future.ofResult(10)
                      .visit(i->i*2, e->-1);
        assertThat(20,equalTo(r));
    }
    @Test
    public void testVisitFail(){
        
      int r =  Future.<Integer>ofError(new RuntimeException())
                      .visit(i->i*2, e->-1);
        assertThat(-1,equalTo(r));
    }
    @Test
    public void testBreakout(){
        
        Future<ListX<Integer>> strings = Future.quorum(status -> status.getCompleted() > 1, Future.ofSupplier(()->1), Future.ofSupplier(()->1), Future.ofSupplier(()->1));
               

        assertThat(strings.get().size(), is(greaterThan(1)));
    }
    @Test
    public void testBreakoutAll(){
        
        Future<ListX<Integer>> strings = Future.quorum(status -> status.getCompleted() > 2, Future.ofSupplier(()->1), Future.ofSupplier(()->1), Future.ofSupplier(()->1));
               

        assertThat(strings.get().size(), is(equalTo(3)));
    }
    @Test
    public void testFirstSuccess(){
        
        Future<Integer> ft = Future.future();
        Future<Integer> result = Future.firstSuccess(Future.ofSupplier(()->1),ft);
               
        ft.complete(10);
        assertThat(result.get(), is(equalTo(1)));
    }
    @Test
    public void testBreakoutOne(){
        
        Future<ListX<Integer>> strings = Future.quorum(status -> status.getCompleted() >0, Future.ofSupplier(()->1), Future.future(), Future.future());
               

        assertThat(strings.get().size(), is(equalTo(1)));
    }

    @Test
    public void testZip(){
        assertThat(Future.ofResult(10).zip(Eval.now(20),(a, b)->a+b).get(),equalTo(30));
        assertThat(Future.ofResult(10).zipP(Eval.now(20),(a, b)->a+b).get(),equalTo(30));
        assertThat(Future.ofResult(10).zipS(Stream.of(20),(a, b)->a+b).get(),equalTo(30));
        assertThat(Future.ofResult(10).zip(Seq.of(20),(a, b)->a+b).get(),equalTo(30));
        assertThat(Future.ofResult(10).zip(Seq.of(20)).get(),equalTo(Tuple.tuple(10,20)));
        assertThat(Future.ofResult(10).zipS(Stream.of(20)).get(),equalTo(Tuple.tuple(10,20)));
        assertThat(Future.ofResult(10).zip(Eval.now(20)).get(),equalTo(Tuple.tuple(10,20)));
    }
   

     
    @Test
    public void apNonBlocking(){
        
      val f =  Future.ofSupplier(()->{ sleep(1000l); return "hello";},ex)
                      .combine(Future.ofSupplier(()->" world",ex),String::concat);
      
      
      System.out.println("hello");
      long start=System.currentTimeMillis();
      System.out.println(f.get());
      assertThat(System.currentTimeMillis()-start,greaterThan(400l));
    }
    
    @Test
    public void futureWFromIterableTest() {

        ReactiveSeq<Integer> stream = ReactiveSeq.of(1, 2, 3);

        Future<Integer> maybe = Future.fromPublisher(stream);
        assertThat(maybe.get(), equalTo(1));

    }

    @Test
    public void futureWAsyncFromIterableTest() {

        ReactiveSeq<Integer> stream = ReactiveSeq.of(1, 2, 3);

        Future<Integer> maybe = Future.fromIterable(stream, ex);
        assertThat(maybe.get(), equalTo(1));

    }

    @Test
    public void scheduleDelay(){
        
        long start = System.currentTimeMillis();
        String res = Future.schedule(100, Executors.newScheduledThreadPool(1), ()->"hello")
                            .get();
        
        assertThat(100l,lessThan(System.currentTimeMillis()-start));
        assertThat(res,equalTo("hello"));
        
    }
    @Test
    public void scheduleCron(){
        
        long start = System.currentTimeMillis();
        Future.schedule("* * * * * ?", Executors.newScheduledThreadPool(1), ()->"hello")
                            .get();
        
        String res = Future.schedule("* * * * * ?", Executors.newScheduledThreadPool(1), ()->"hello")
                .get();
        assertThat(1000l,lessThan(System.currentTimeMillis()-start));
        assertThat(res,equalTo("hello"));
        
    }
    

    @Test
    public void nest(){
       assertThat(just.nest().map(m->m.get()).get(),equalTo(just.get()));
       assertThat(none.nest().map(m->m.get()).isPresent(),equalTo(false));
    }
    @Test
    public void coFlatMap(){
        assertThat(just.coflatMap(m-> m.isPresent()? m.get() : 50).get(),equalTo(just.get()));
        assertThat(none.coflatMap(m-> m.isPresent()? m.get() : 50).get(),equalTo(50));
    }
    
    @Test
    public void combine(){
        Monoid<Integer> add = Monoid.of(0,Semigroups.intSum);
        
        assertThat(just.combineEager(add,Maybe.just(10)).toMaybe(),equalTo(Maybe.just(20)));
        Monoid<Integer> firstNonNull = Monoid.of(null , Semigroups.firstNonNull());
        assertThat(just.combineEager(firstNonNull,none).get(),equalTo(just.get()));
         
    }
    @Test
    public void testToMaybe() {
        assertThat(just.toMaybe(),equalTo(Maybe.of(10)));
        assertThat(none.toMaybe(),equalTo(Maybe.none()));
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
        Future<ListX<Integer>> maybes = Future.sequence(ListX.of(just, Future.ofResult(1)));
        assertThat(maybes.get(),equalTo(ListX.of(10,1)));
    }
    @Test
    public void testSequenceCF() {
        CompletableFuture<ListX<Integer>> maybes =CompletableFutures.sequence(ListX.of(just.getFuture(),none.getFuture(), Future.ofResult(1).getFuture()));
        assertThat(maybes.isCompletedExceptionally(),equalTo(true));
 
    }
    @Test
    public void testAccumulateSuccessSemigroup() {
        Future<Integer> maybes = Future.accumulateSuccess(Monoids.intCount,ListX.of(just,none, Future.ofResult(1)));
        
        assertThat(maybes.get(),equalTo(2));
    }
    @Test
    public void testAccumulateSuccess() {
        Future<PSetX<Integer>> maybes = Future.accumulateSuccess(ListX.of(just,none, Future.ofResult(1)),Reducers.toPSetX());
        
        assertThat(maybes.get(),equalTo(PSetX.of(10,1)));
    }
    @Test @Ignore
    public void testAccumulateJNonBlocking() {
        Future<PSetX<Integer>> maybes = Future.accumulateSuccess(ListX.of(just,none, Future.ofSupplier(()->{while(true){System.out.println("hello");}},Executors.newFixedThreadPool(1)), Future.ofResult(1)),Reducers.toPSetX());
        System.out.println("not blocked");
       
    }
    @Test
    public void testAccumulateNoValue() {
        Future<String> maybes = Future.accumulate(ListX.of(), i->""+i,Monoids.stringConcat);
        assertThat(maybes.get(),equalTo(""));
    }
    @Test
    public void testAccumulateOneValue() {
        Future<String> maybes = Future.accumulate(ListX.of(just), i->""+i,Monoids.stringConcat);
        assertThat(maybes.get(),equalTo("10"));
    }
    @Test
    public void testAccumulateJustCollectionXOfMaybeOfTFunctionOfQsuperTRSemigroupOfR() {
        Future<String> maybes = Future.accumulate(ListX.of(just, Future.ofResult(1)), i->""+i,Monoids.stringConcat);
        assertThat(maybes.get(),equalTo("101"));
    }
    @Test
    public void testAccumulateJust() {
        Future<Integer> maybes = Future.accumulate(Monoids.intSum,ListX.of(just, Future.ofResult(1)));
        assertThat(maybes.get(),equalTo(11));
    }
    @Test
    public void testAccumulateError() {
        Future<Integer> maybes = Future.accumulate(Monoids.intSum,ListX.of(none, Future.ofResult(1)));
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
        assertThat(just.map(i->i+5).get(),equalTo(15));
        assertTrue(none.map(i->i+5).isFailed());
    }

    @Test
    public void testFlatMap() {
        assertThat(just.flatMap(i-> Future.ofResult(i+5) ).get(),equalTo(Future.ofResult(15).get() ));
        assertTrue(none.flatMap(i-> Future.ofResult(i+5)).isFailed());
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
        Future<Stream<Integer>> async = Future.ofSupplier(()->just.visit(f->Stream.of((int)f),()->Stream.of()));
        
        assertThat(async.get().collect(Collectors.toList()),equalTo(ListX.of(10)));
    }

    
    @Test
    public void testIterate() {
        assertThat(just.iterate(i->i+1).limit(10).sumInt(i->i),equalTo(145));
    }

    @Test
    public void testGenerate() {
        assertThat(just.generate().limit(10).sumInt(i->i),equalTo(100));
    }

    @Test
    public void testMapReduceReducerOfE() {
        assertThat(just.mapReduce(Reducers.toCountInt()),equalTo(1));
    }


    @Test
    public void testToXor() {
        assertThat(just.toXor(),equalTo(Xor.primary(10)));
        
    }
    @Test
    public void testToXorNone(){
        Xor<Throwable,Integer> xor = none.toXor();
        assertTrue(xor.isSecondary());
        assertThat(xor,equalTo(Xor.secondary(exception)));
        
    }


    @Test
    public void testToXorSecondary() {
        assertThat(just.toXor().swap(),equalTo(Xor.secondary(10)));
    }

    @Test
    public void testToXorSecondaryNone(){
        Xor<Integer, Throwable> xorNone = none.toXor().swap();
        assertThat(xorNone,equalTo(Xor.primary(exception)));
        
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
        Ior<Integer, Throwable> ior = none.toIor().swap();
        assertTrue(ior.isPrimary());
        assertThat(ior,equalTo(Ior.primary(exception)));
        
    }


    @Test
    public void testToIorSecondary() {
        assertThat(just.toIor().swap(),equalTo(Ior.secondary(10)));
    }
    

    @Test
    public void testToIorSecondaryNone(){
        Ior<Integer,Throwable> ior = none.toIor().swap();
        assertTrue(ior.isPrimary());
        assertThat(ior,equalTo(Ior.primary(exception)));
        
    }
    @Test
    public void testToEvalNow() {
        assertThat(just.toEvalNow(),equalTo(Eval.now(10)));
    }
    @Test(expected=NoSuchElementException.class)
    public void testToEvalNowNone() {
        none.toEvalNow();
        fail("exception expected");
        
    }

    @Test
    public void testToEvalLater() {
        assertThat(just.toEvalLater(),equalTo(Eval.later(()->10)));
    }
    @Test(expected=NoSuchElementException.class)
    public void testToEvalLaterNone() {
        none.toEvalLater().get();
        fail("exception expected");
        
    }

    @Test
    public void testToEvalAlways() {
        assertThat(just.toEvalAlways(),equalTo(Eval.always(()->10)));
    }
    @Test(expected=NoSuchElementException.class)
    public void testToEvalAlwaysNone() {
        none.toEvalAlways().get();
        fail("exception expected");
        
    }


    @Test
    public void testMkString() {
        assertThat(just.mkString(),containsString("Future["));
        assertThat(none.mkString(),containsString("Future["));
    }
    LazyReact react = new LazyReact();

    @Test
    public void testGet() {
        assertThat(just.get(),equalTo(10));
    }
    @Test(expected=NoSuchElementException.class)
    public void testGetNone() {
        none.get();
        
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
    public void testMapReduceFunctionOfQsuperTQextendsRMonoidOfR() {
        assertThat(just.mapReduce(s->s.toString(), Monoid.of("",Semigroups.stringJoin(","))),equalTo(",10"));
    }

    @Test
    public void testReduceMonoidOfT() {
        assertThat(just.reduce(Monoid.of(1,Semigroups.intMult)),equalTo(10));
    }

    @Test
    public void testReduceBinaryOperatorOfT() {
        assertThat(just.reduce((a,b)->a+b),equalTo(Optional.of(10)));
    }

    @Test
    public void testReduceTBinaryOperatorOfT() {
        assertThat(just.reduce(10,(a,b)->a+b),equalTo(20));
    }

    @Test
    public void testReduceUBiFunctionOfUQsuperTUBinaryOperatorOfU() {
        assertThat(just.reduce(11,(a,b)->a+b,(a,b)->a*b),equalTo(21));
    }

    @Test
    public void testReduceStreamOfQextendsMonoidOfT() {
        ListX<Integer> countAndTotal = just.reduce(Stream.of(Reducers.toCountInt(),Reducers.toTotalInt()));
        assertThat(countAndTotal,equalTo(ListX.of(1,10)));
    }

    @Test
    public void testReduceIterableOfReducerOfT() {
        ListX<Integer> countAndTotal = just.reduce(Arrays.asList(Reducers.toCountInt(),Reducers.toTotalInt()));
        assertThat(countAndTotal,equalTo(ListX.of(1,10)));
    }

    

    @Test
    public void testFoldRightMonoidOfT() {
        assertThat(just.foldRight(Monoid.of(1,Semigroups.intMult)),equalTo(10));
    }

    @Test
    public void testFoldRightTBinaryOperatorOfT() {
        assertThat(just.foldRight(10,(a,b)->a+b),equalTo(20));
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
        assertThat(none.toStream().collect(Collectors.toList()).size(),equalTo(0));
        assertThat(just.toStream().collect(Collectors.toList()).size(),equalTo(1));
        
    }

    @Test
    public void testOrElse() {
        assertThat(none.orElse(20),equalTo(20));
        assertThat(just.orElse(20),equalTo(10));
    }

    @Test(expected=RuntimeException.class)
    public void testOrElseThrow() {
        none.orElseThrow(()->new RuntimeException());
    }
    @Test
    public void testOrElseThrowSome() {
        
        assertThat(just.orElseThrow(()->new RuntimeException()),equalTo(10));
    }


    
    @Test
    public void testToFutureW() {
        Future<Integer> cf = just.toFuture();
        assertThat(cf.get(),equalTo(10));
    }

    @Test
    public void testToCompletableFuture() {
        CompletableFuture<Integer> cf = just.toCompletableFuture();
        assertThat(cf.join(),equalTo(10));
    }

    @Test
    public void testToCompletableFutureAsync() {
        CompletableFuture<Integer> cf = just.toCompletableFutureAsync();
        assertThat(cf.join(),equalTo(10));
    }
    Executor exec = Executors.newFixedThreadPool(1);

    @Test
    public void testToCompletableFutureAsyncExecutor() {
        CompletableFuture<Integer> cf = just.toCompletableFutureAsync(exec);
        assertThat(cf.join(),equalTo(10));
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
        Future<Number> num = just.cast(Number.class);
    }

    @Test
    public void testMapFunctionOfQsuperTQextendsR1() {
        assertThat(just.map(i->i+5).get(),equalTo(15));
    }
    
    @Test
    public void testPeek() {
        Mutable<Integer> capture = Mutable.of(null);
        just = just.peek(c->capture.set(c));
        
        assertThat(capture.get(),equalTo(10));
    }

    private Trampoline<Integer> sum(int times,int sum){
        return times ==0 ?  Trampoline.done(sum) : Trampoline.more(()->sum(times-1,sum+times));
    }
    @Test
    public void testTrampoline() {
        assertThat(just.trampoline(n ->sum(10,n)).get(),equalTo(Xor.primary(65).get()));
    }

    
    @Test
    public void mapBoth(){
        assertThat(Future.ofResult(1).map(i->i*2, e->-1).get(),equalTo(2));
    }
    @Test
    public void testUnitT1() {
        assertThat(none.unit(10).get(),equalTo(just.get()));
    }

    @Test
    public void testRecover() {
        assertThat(Future.ofError(new RuntimeException()).recover(__ -> true).get(), equalTo(true));
    }
    
    @Test
    public void testFlatMapIterable() {
        Future<Integer> f = just.flatMapI(i -> Arrays.asList(i, 20, 30));
        assertThat(f.get(), equalTo(10));

        f = just.flatMapI(i -> AnyM.fromStream(ReactiveSeq.of(20, i, 30)));
        assertThat(f.get(), equalTo(20));
    }
    
    @Test
    public void testFlatMapPublisher() {
        Future<Integer> f = just.flatMapP(i -> Flux.just(100, i));
        assertThat(f.get(), equalTo(100));
    }

}
