package cyclops.control.lazy;

import cyclops.collections.immutable.LinkedListX;
import cyclops.companion.Monoids;
import cyclops.companion.Reducers;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.async.Future;
import cyclops.collections.box.Mutable;
import cyclops.collections.mutable.ListX;
import cyclops.control.*;
import cyclops.control.lazy.Either.CompletableEither;
import cyclops.function.Monoid;
import cyclops.stream.ReactiveSeq;
import cyclops.collections.tuple.Tuple;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class CompletableEitherTest {

    public static <RT> CompletableEither<RT,RT> right(RT value){
        CompletableEither<RT,RT> completable = Either.either();
        completable.complete(value);
        return completable;
    }
    @Test
    public void completableTest(){
        CompletableEither<Integer,Integer> completable = Either.either();
        Either<Throwable,Integer> mapped = completable.map(i->i*2)
                                           .flatMap(i->Either.right(i+1));

        completable.complete(5);
        System.out.println(mapped.getClass());
        mapped.printOut();
        assertThat(mapped.get(),equalTo(11));


    }
    @Test
    public void completableNoneTest(){
        CompletableEither<Integer,Integer> completable = Either.either();
        Either<Throwable,Integer> mapped = completable.map(i->i*2)
                                                      .flatMap(i->Either.right(i+1));

        completable.complete(null);

        mapped.printOut();
        assertThat(mapped.isPresent(),equalTo(false));
        assertThat(mapped.secondaryGet(),instanceOf(NoSuchElementException.class));

    }
    @Test
    public void completableErrorTest(){
        CompletableEither<Integer,Integer> completable = Either.either();
        Either<Throwable,Integer> mapped = completable.map(i->i*2)
                .flatMap(i->Either.right(i+1));

        completable.completeExceptionally(new IllegalStateException());

        mapped.printOut();
        assertThat(mapped.isPresent(),equalTo(false));
        assertThat(mapped.secondaryGet(),instanceOf(IllegalStateException.class));

    }





    
    boolean lazy = true;
    @Test
    public void lazyTest() {
        CompletableEitherTest.right(10)
             .flatMap(i -> { lazy=false; return  CompletableEitherTest.right(15);})
             .map(i -> { lazy=false; return  CompletableEitherTest.right(15);})
             .map(i -> Maybe.of(20));
             
        
        assertTrue(lazy);
            
    }
    @Test
    public void mapFlatMapTest(){
        assertThat(CompletableEitherTest.right(10)
               .map(i->i*2)
               .flatMap(i->CompletableEitherTest.right(i*4))
               .get(),equalTo(80));
    }
    @Test
    public void odd() {
        System.out.println(even(CompletableEitherTest.right(200000)).get());
    }

    public Either<Throwable,String> odd(Either<Throwable,Integer> n) {

        return n.flatMap(x -> even(CompletableEitherTest.right(x - 1)));
    }

    public Either<Throwable,String> even(Either<Throwable,Integer> n) {
        return n.flatMap(x -> {
            return x <= 0 ? CompletableEitherTest.right("done") : odd(CompletableEitherTest.right(x - 1));
        });
    }
    Either<Throwable,Integer> just;
    Either<Throwable,Integer> none;
    @Before
    public void setUp() throws Exception {
        just = CompletableEitherTest.right(10);
        none = Either.left(new NoSuchElementException("none"));
    }





    @Test
    public void testAccumulateSecondarySemigroupIntSum() {
        Ior<?,Integer> iors = Ior.accumulateSecondary(Monoids.intSum,ListX.of(Ior.both(2, "boo!"),Ior.secondary(1)));
        assertThat(iors,equalTo(Ior.primary(3)));
    }




    @Test
    public void visit(){
        
        assertThat(just.visit(secondary->"no", primary->"yes"),equalTo("yes"));
        assertThat(none.visit(secondary->"no", primary->"yes"),equalTo("no"));
    }
    @Test
    public void visitEither(){
        assertThat(just.mapBoth(secondary->"no", primary->"yes"),equalTo(Either.right("yes")));
        assertThat(none.mapBoth(secondary->"no", primary->"yes"),equalTo(Either.left("no")));
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

    

    

    



    @Test @Ignore  //pending https://github.com/aol/cyclops-react/issues/390
    public void hashCodeTest(){
       
        System.out.println(new Integer(10).hashCode());
        System.out.println("Xor " + Xor.primary(10).hashCode());
        assertThat(Xor.primary(10).hashCode(),equalTo(CompletableEitherTest.right(10).hashCode()));
    }





    @Test
    public void testUnitT() {
        assertThat(just.unit(20),equalTo(Either.right(20)));
    }

    

    @Test
    public void testisPrimary() {
        assertTrue(just.isPrimary());
        assertFalse(none.isPrimary());
    }

    
    @Test
    public void testMapFunctionOfQsuperTQextendsR() {
        assertThat(just.map(i->i+5),equalTo(Either.right(15)));

    }

    @Test
    public void testFlatMap() {
        assertThat(just.flatMap(i->CompletableEitherTest.right(i+5)),equalTo(Either.right(15)));

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
        
        assertThat(async.get().collect(Collectors.toList()),equalTo(ListX.of(10)));
    }
    
    @Test
    public void testIterate() {
        assertThat(just.iterate(i->i+1,-1000).limit(10).sumInt(i->i),equalTo(145));
    }

    @Test
    public void testGenerate() {
        assertThat(just.generate(-10000).limit(10).sumInt(i->i),equalTo(100));
    }


    @Test
    public void testFoldMonoidOfT() {
        assertThat(just.fold(Reducers.toTotalInt()),equalTo(10));
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
        Ior<Throwable,Integer> ior = none.toIor();
        assertTrue(ior.isSecondary());

        
    }


    @Test
    public void testToIorSecondary() {
        assertThat(just.toIor().swap(),equalTo(Ior.secondary(10)));
    }
    




      @Test
    public void testMkString() {
        assertThat(just.mkString(),equalTo("CompletableEither[10]"));
        assertThat(none.mkString(),equalTo("Either.left[java.util.NoSuchElementException: none]"));
    }

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
        just.get();
        
        assertThat(capture.get(),equalTo(10));
    }

    private Trampoline<Integer> sum(int times,int sum){
        return times ==0 ?  Trampoline.done(sum) : Trampoline.more(()->sum(times-1,sum+times));
    }
    @Test
    public void testTrampoline() {
        assertThat(just.trampoline(n ->sum(10,n)),equalTo(Either.right(65)));
    }

    @Test
    public void equalsTest(){
        assertTrue(just.equals(CompletableEitherTest.right(10)
                                     .map(i->i)));
        assertThat(just,equalTo(Either.left(10)
                                      .secondaryFlatMap(i->CompletableEitherTest.right(i))));
        assertThat(Either.left(10)
                        .flatMap(i->Either.right(i)),equalTo(Either.left(10)));
    }



}
