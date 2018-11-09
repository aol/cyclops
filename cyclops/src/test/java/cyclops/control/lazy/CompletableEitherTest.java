package cyclops.control.lazy;

import cyclops.companion.Monoids;
import cyclops.companion.Reducers;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.control.Future;
import com.oath.cyclops.util.box.Mutable;

import cyclops.control.*;
import cyclops.control.LazyEither.CompletableEither;
import cyclops.function.Monoid;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class CompletableEitherTest {

    public static <RT> CompletableEither<RT,RT> right(RT value){
        CompletableEither<RT,RT> completable = LazyEither.either();
        completable.complete(value);
        return completable;
    }
    @Test
    public void completableTest(){
        CompletableEither<Integer,Integer> completable = LazyEither.either();
        LazyEither<Throwable,Integer> mapped = completable.map(i->i*2)
                                           .flatMap(i-> LazyEither.right(i+1));

        completable.complete(5);
        System.out.println(mapped.getClass());
        mapped.printOut();
        assertThat(mapped.get(),equalTo(Option.some(11)));


    }
    @Test
    public void completableNoneTest(){
        CompletableEither<Integer,Integer> completable = LazyEither.either();
        LazyEither<Throwable,Integer> mapped = completable.map(i->i*2)
                                                      .flatMap(i-> LazyEither.right(i+1));

        completable.completeExceptionally(new NoSuchElementException());

        mapped.printOut();
        assertThat(mapped.isPresent(),equalTo(false));
        assertThat(mapped.leftOrElse(null),instanceOf(NoSuchElementException.class));

    }
    @Test
    public void completableErrorTest(){
        CompletableEither<Integer,Integer> completable = LazyEither.either();
        LazyEither<Throwable,Integer> mapped = completable.map(i->i*2)
                .flatMap(i-> LazyEither.right(i+1));

        completable.completeExceptionally(new IllegalStateException());

        mapped.printOut();
        assertThat(mapped.isPresent(),equalTo(false));
        assertThat(mapped.leftOrElse(null),instanceOf(IllegalStateException.class));

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
               .get(),equalTo(Option.some(80)));
    }
    @Test
    public void odd() {
        System.out.println(even(CompletableEitherTest.right(200000)).get());
    }

    public LazyEither<Throwable,String> odd(LazyEither<Throwable,Integer> n) {

        return n.flatMap(x -> even(CompletableEitherTest.right(x - 1)));
    }

    public LazyEither<Throwable,String> even(LazyEither<Throwable,Integer> n) {
        return n.flatMap(x -> {
            return x <= 0 ? CompletableEitherTest.right("done") : odd(CompletableEitherTest.right(x - 1));
        });
    }
    LazyEither<Throwable,Integer> just;
    LazyEither<Throwable,Integer> none;
    @Before
    public void setUp() throws Exception {
        just = CompletableEitherTest.right(10);
        none = LazyEither.left(new NoSuchElementException("none"));
    }





    @Test
    public void testAccumulateSecondarySemigroupIntSum() {
        Ior<?,Integer> iors = Ior.accumulateLeft(Monoids.intSum,Arrays.asList(Ior.both(2, "boo!"),Ior.left(1)));
        assertThat(iors,equalTo(Ior.right(3)));
    }




    @Test
    public void visit(){

        assertThat(just.fold(secondary->"no", primary->"yes"),equalTo("yes"));
        assertThat(none.fold(secondary->"no", primary->"yes"),equalTo("no"));
    }
    @Test
    public void visitEither(){
        assertThat(just.bimap(secondary->"no", primary->"yes"),equalTo(LazyEither.right("yes")));
        assertThat(none.bimap(secondary->"no", primary->"yes"),equalTo(LazyEither.left("no")));
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









    @Test @Ignore  //pending https://github.com/aol/cyclops-react/issues/390
    public void hashCodeTest(){

        System.out.println(new Integer(10).hashCode());
        System.out.println("Xor " + Either.right(10).hashCode());
        assertThat(Either.right(10).hashCode(),equalTo(CompletableEitherTest.right(10).hashCode()));
    }





    @Test
    public void testUnitT() {
        assertThat(just.unit(20),equalTo(LazyEither.right(20)));
    }



    @Test
    public void testisPrimary() {
        assertTrue(just.isRight());
        assertFalse(none.isRight());
    }


    @Test
    public void testMapFunctionOfQsuperTQextendsR() {
        assertThat(just.map(i->i+5),equalTo(LazyEither.right(15)));

    }

    @Test
    public void testFlatMap() {
        assertThat(just.flatMap(i->CompletableEitherTest.right(i+5)),equalTo(LazyEither.right(15)));

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
        assertThat(just.toIor(),equalTo(Ior.right(10)));

    }
    @Test
    public void testToIorNone(){
        Ior<Throwable,Integer> ior = none.toIor();
        assertTrue(ior.isLeft());


    }


    @Test
    public void testToIorSecondary() {
        assertThat(just.toIor().swap(),equalTo(Ior.left(10)));
    }





      @Test
    public void testMkString() {
        assertThat(just.mkString(),equalTo("CompletableEither[10]"));
        assertThat(none.mkString(),equalTo("Either.left[java.util.NoSuchElementException: none]"));
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
        assertThat(just.map(i->i+5),equalTo(LazyEither.right(15)));
    }

    @Test
    public void testPeek() {
        Mutable<Integer> capture = Mutable.of(null);
        just = just.peek(c->capture.set(c));
        just.orElse(null);

        assertThat(capture.get(),equalTo(10));
    }

    private Trampoline<Integer> sum(int times,int sum){
        return times ==0 ?  Trampoline.done(sum) : Trampoline.more(()->sum(times-1,sum+times));
    }

    @Test
    public void equalsTest(){
        assertTrue(just.equals(CompletableEitherTest.right(10)
                                     .map(i->i)));
        assertThat(just,equalTo(LazyEither.left(10)
                                      .flatMapLeft(i->CompletableEitherTest.right(i))));
        assertThat(LazyEither.left(10)
                        .flatMap(i-> LazyEither.right(i)),equalTo(LazyEither.left(10)));
    }



}
