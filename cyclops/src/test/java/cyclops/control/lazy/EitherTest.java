package cyclops.control.lazy;

import com.oath.cyclops.types.persistent.PersistentSet;
import cyclops.data.HashSet;
import cyclops.data.Seq;
import cyclops.companion.Monoids;
import cyclops.companion.Reducers;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.control.Future;
import com.oath.cyclops.util.box.Mutable;

import cyclops.control.*;
import cyclops.control.LazyEither;
import cyclops.control.LazyEither.CompletableEither;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;
import cyclops.function.Monoid;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
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
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

public class EitherTest {

    int called = 0;
    @Before
    public void setup(){
        called =0;
    }
    @Test
    public void later(){


        LazyEither<Object, Integer> e = LazyEither.later(() -> {
            called++;
            return Either.right(10);
        });
        assertThat(called,equalTo(0));

        e.isRight();
        assertThat(called,equalTo(1));
        e.isRight();
        assertThat(called,equalTo(1));
    }
    @Test
    public void always(){


        LazyEither<Object, Integer> e = LazyEither.always(() -> {
            called++;
            return Either.right(10);
        });
        assertThat(called,equalTo(0));

        e.isRight();
        assertThat(called,equalTo(1));
        e.isRight();
        assertThat(called,equalTo(2));
    }
    @Test
    public void nullPublisher() {
        assertThat(LazyEither.fromPublisher(Seq.of(null, 190)), not(equalTo(LazyEither.right(null))));
    }
    @Test
    public void fromNull(){
        System.out.println(LazyEither.fromIterable(Seq.of().plus(null),10));
        assertThat(LazyEither.right(null), equalTo(LazyEither.right(null)));
        assertThat(Maybe.nothing(), not(equalTo(Maybe.just(null))));
        assertThat(LazyEither.fromIterable(Seq.of(),10),equalTo(LazyEither.right(10)));

        System.out.println(Maybe.fromPublisher(Seq.of(null,190)));
        assertThat(LazyEither.fromPublisher(Seq.of(null,190)),not(equalTo(LazyEither.right(null))));
        assertThat(LazyEither.fromFuture(Future.ofResult(null)),equalTo(LazyEither.right(null)));

    }
    @Test
    public void filterAlt(){
      assertThat(LazyEither.right(10).filter(i->i>100,r->"hello"),equalTo(LazyEither.left("hello")));
      assertThat(LazyEither.right(101).filter(i->i>100,r->"hello"),equalTo(LazyEither.right(101)));
      assertThat(LazyEither.<Integer,Integer>left(101).filter(i->i>100,r->-1),equalTo(LazyEither.left(101)));
    }
    @Test
    public void completableTest(){
        CompletableEither<Integer,Integer> completable = LazyEither.either();
        LazyEither<Throwable,Integer> mapped = completable.map(i->i*2)
                                           .flatMap(i-> LazyEither.right(i+1));

        completable.complete(5);
        System.out.println(mapped.getClass());
        mapped.printOut();
        assertThat(mapped.get(),equalTo(Maybe.just(11)));


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

        Spouts.from(LazyEither.fromFuture(future)
                .map(i->i*2))
                .peek(System.out::println)
                .map(i->i*100)
                .forEachAsync(e->result.complete(e));

        assertFalse(result.isDone());
        System.out.println("Blocking?");
        assertThat("Result is " + result.get(),result.get(),equalTo(Try.success(2000)));

    }


    @Test
    public void testTraverseLeft1() {
        Seq<LazyEither<Integer,String>> list = Seq.of(just,none, LazyEither.<String,Integer>right(1)).map(LazyEither::swap);
      LazyEither<Integer, ReactiveSeq<String>> xors = LazyEither.traverseRight(list, s -> "hello:" + s);
        assertThat(xors.map(s->s.toList()),equalTo(LazyEither.right(Arrays.asList("hello:none"))));
    }
    @Test
    public void testSequenceLeft1() {
        Seq<LazyEither<Integer,String>> list = Seq.of(just,none, LazyEither.<String,Integer>right(1)).map(LazyEither::swap);
      LazyEither<Integer, ReactiveSeq<String>> xors = LazyEither.sequenceRight(list);
        assertThat(xors.map(s->s.toList()),equalTo(LazyEither.right(Arrays.asList("none"))));
    }
    @Test
    public void testAccumulate() {
      LazyEither<String, Integer> iors = LazyEither.accumulate(Monoids.intSum, Arrays.asList(none, just, LazyEither.right(10)));
        assertThat(iors,equalTo(LazyEither.right(20)));
    }

    boolean lazy = true;
    @Test
    public void lazyTest() {
        LazyEither.right(10)
             .flatMap(i -> { lazy=false; return  LazyEither.right(15);})
             .map(i -> { lazy=false; return  LazyEither.right(15);})
             .map(i -> Maybe.of(20));


        assertTrue(lazy);

    }
    @Test
    public void mapFlatMapTest(){
        assertThat(LazyEither.right(10)
               .map(i->i*2)
               .flatMap(i-> LazyEither.right(i*4))
               .get(),equalTo(Option.some(80)));
    }
    @Test
    public void odd() {
        System.out.println(even(LazyEither.right(200000)).get());
    }

    public LazyEither<String,String> odd(LazyEither<String,Integer> n) {

        return n.flatMap(x -> even(LazyEither.right(x - 1)));
    }

    public LazyEither<String,String> even(LazyEither<String,Integer> n) {
        return n.flatMap(x -> {
            return x <= 0 ? LazyEither.right("done") : odd(LazyEither.right(x - 1));
        });
    }
    LazyEither<String,Integer> just;
    LazyEither<String,Integer> none;
    @Before
    public void setUp() throws Exception {
        just = LazyEither.right(10);
        none = LazyEither.left("none");
    }

    @Test
    public void testSequenceSecondary() {
      Either<Integer, ReactiveSeq<String>> xors = Either.sequenceLeft(Arrays.asList(just, none, LazyEither.right(1)));
        assertThat(xors.map(s->s.toList()),equalTo(LazyEither.right(Arrays.asList("none"))));
    }

    @Test
    public void testAccumulateSecondary2() {
        Either<?,PersistentSet<String>> xors = Either.accumulateLeft(Arrays.asList(just,none, LazyEither.right(1)),Reducers.<String>toPersistentSet());
        assertThat(xors,equalTo(LazyEither.right(HashSet.of("none"))));
    }

    @Test
    public void testAccumulateSecondarySemigroup() {
        Either<?,String> xors = Either.accumulateLeft(Arrays.asList(just,none, LazyEither.left("1")), i->""+i,Monoids.stringConcat);
        assertThat(xors,equalTo(LazyEither.right("none1")));
    }
    @Test
    public void testAccumulateSecondarySemigroupIntSum() {
        Ior<?,Integer> iors = Ior.accumulateLeft(Monoids.intSum,Arrays.asList(Ior.both(2, "boo!"),Ior.left(1)));
        assertThat(iors,equalTo(Ior.right(3)));
    }

    @Test
    public void nest(){
       assertThat(just.nest().map(m->m.toOptional().get()),equalTo(just));
       assertThat(none.nest().map(m->m.get()),equalTo(none));
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







    @Test
    public void testSequence() {
      Either<String, ReactiveSeq<Integer>> maybes = Either.sequenceRight(Arrays.asList(just, none, LazyEither.right(1)));
        assertThat(maybes.map(s->s.toList()),equalTo(LazyEither.right(Arrays.asList(10,1))));
    }

    @Test @Ignore  //pending https://github.com/aol/cyclops-react/issues/390
    public void hashCodeTest(){

        System.out.println(new Integer(10).hashCode());
        System.out.println("Xor " + Either.right(10).hashCode());
        assertThat(Either.right(10).hashCode(),equalTo(LazyEither.right(10).hashCode()));
    }
    @Test
    public void testAccumulateJustCollectionXOfMaybeOfTReducerOfR() {
        Either<?,PersistentSet<Integer>> maybes = Either.accumulateRight(Arrays.asList(just,none, LazyEither.right(1)),Reducers.toPersistentSet());
        assertThat(maybes,equalTo(LazyEither.right(HashSet.of(10,1))));
    }

    @Test
    public void testAccumulateJustCollectionXOfMaybeOfTFunctionOfQsuperTRSemigroupOfR() {
        Either<?,String> maybes = Either.accumulateRight(Arrays.asList(just,none, LazyEither.right(1)), i->""+i,Monoids.stringConcat);
        assertThat(maybes,equalTo(LazyEither.right("101")));
    }
    @Test
    public void testAccumulateJust() {
        Either<?,Integer> maybes = Either.accumulateRight(Monoids.intSum,Arrays.asList(just,none, LazyEither.right(1)));
        assertThat(maybes,equalTo(LazyEither.right(11)));
    }
    @Test
    public void testAccumulateSecondary() {
        Either<?,String> maybes = Either.accumulateLeft(Monoids.stringConcat,Arrays.asList(just,none, LazyEither.left("hello")));
        assertThat(maybes,equalTo(LazyEither.right("nonehello")));
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
        assertThat(none.map(i->i+5),equalTo(LazyEither.left("none")));
    }

    @Test
    public void testFlatMap() {
        assertThat(just.flatMap(i-> LazyEither.right(i+5)),equalTo(LazyEither.right(15)));
        assertThat(none.flatMap(i-> LazyEither.right(i+5)),equalTo(LazyEither.left("none")));
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
        assertThat(just.asSupplier(-1000).iterate(i->i+1).limit(10).sumInt(i->i),equalTo(145));
    }

    @Test
    public void testGenerate() {
        assertThat(just.asSupplier(-10000).generate().limit(10).sumInt(i->i),equalTo(100));
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
        Ior<String,Integer> ior = none.toIor();
        assertTrue(ior.isLeft());
        assertThat(ior,equalTo(Ior.left("none")));

    }


    @Test
    public void testToIorSecondary() {
        assertThat(just.toIor().swap(),equalTo(Ior.left(10)));
    }


    @Test
    public void testToIorSecondaryNone(){
        Ior<Integer,String> ior = none.toIor().swap();
        assertTrue(ior.isRight());
        assertThat(ior,equalTo(Ior.right("none")));

    }

      @Test
    public void testMkString() {
        assertThat(just.mkString(),equalTo("Either.right[10]"));
        assertThat(none.mkString(),equalTo("Either.left[none]"));
    }

    @Test
    public void testGet() {
        assertThat(just.get(),equalTo(Option.some(10)));
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

    private Trampoline<Integer> sum(int times, int sum){
        return times ==0 ?  Trampoline.done(sum) : Trampoline.more(()->sum(times-1,sum+times));
    }


    @Test
    public void equalsTest(){
        assertTrue(just.equals(LazyEither.right(10)
                                     .map(i->i)));
        assertThat(just,equalTo(LazyEither.left(10)
                                      .flatMapLeft(i-> LazyEither.right(i))));
        assertThat(LazyEither.left(10)
                        .flatMap(i-> LazyEither.right(i)),equalTo(LazyEither.left(10)));
    }

    @Test
    public void testUnitT1() {
        assertThat(none.unit(10),equalTo(just));
    }

}
