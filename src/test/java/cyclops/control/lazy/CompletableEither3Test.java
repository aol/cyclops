package cyclops.control.lazy;


import cyclops.companion.Reducers;
import cyclops.companion.Semigroups;
import cyclops.companion.Streams;
import cyclops.async.Future;
import com.aol.cyclops2.util.box.Mutable;
import cyclops.collections.mutable.ListX;
import cyclops.control.*;
import cyclops.control.LazyEither3.CompletableEither3;
import cyclops.function.Monoid;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class CompletableEither3Test {
    public static <LT2,RT> CompletableEither3<RT,LT2,RT> right(RT value){
        CompletableEither3<RT,LT2,RT> completable = LazyEither3.either3();
        completable.complete(value);
        return completable;
    }
    @Test
    public void completableTest(){
        CompletableEither3<Integer,Integer,Integer> completable = LazyEither3.either3();
        LazyEither3<Throwable,Integer,Integer> mapped = completable.map(i->i*2)
                                                               .flatMap(i-> LazyEither3.right(i+1));

        completable.complete(5);
        System.out.println(mapped.getClass());
        mapped.printOut();
        assertThat(mapped.orElse(null),equalTo(11));


    }
    @Test
    public void completableNoneTest(){
        CompletableEither3<Integer,Integer,Integer> completable = LazyEither3.either3();
        LazyEither3<Throwable,Integer,Integer> mapped = completable.map(i->i*2)
                                                              .flatMap(i-> LazyEither3.right(i+1));

        completable.complete(null);

        mapped.printOut();
        assertThat(mapped.isPresent(),equalTo(false));
        assertThat(mapped.swap1().orElse(null),instanceOf(NoSuchElementException.class));

    }
    @Test
    public void completableErrorTest(){
        CompletableEither3<Integer,Integer,Integer> completable = LazyEither3.either3();
        LazyEither3<Throwable,Integer,Integer> mapped = completable.map(i->i*2)
                                                                .flatMap(i-> LazyEither3.right(i+1));

        completable.completeExceptionally(new IllegalStateException());

        mapped.printOut();
        assertThat(mapped.isPresent(),equalTo(false));
        assertThat(mapped.swap1().orElse(null),instanceOf(IllegalStateException.class));

    }
    boolean lazy = true;
    @Test
    public void lazyTest() {
        CompletableEither3Test.right(10)
             .flatMap(i -> { lazy=false; return  right(15);})
             .map(i -> { lazy=false; return  right(15);})
             .map(i -> Maybe.of(20));
             
        
        assertTrue(lazy);
            
    }
    
    @Test
    public void mapFlatMapTest(){
        assertThat(right(10)
               .map(i->i*2)
               .flatMap(i->right(i*4))
               .orElse(null),equalTo(80));
    }
    static class Base{ }
    static class One extends Base{ }
    static class Two extends Base{}

    @Test
    public void odd() {
        System.out.println(even(CompletableEither3Test.right(200000)).get());
    }

    public LazyEither3<Throwable,String,String> odd(LazyEither3<Throwable,String,Integer> n) {

        return n.flatMap(x -> even(CompletableEither3Test.right(x - 1)));
    }

    public LazyEither3<Throwable,String,String> even(LazyEither3<Throwable,String,Integer> n) {
        return n.flatMap(x -> {
            return x <= 0 ? CompletableEither3Test.right("done") : odd(CompletableEither3Test.right(x - 1));
        });
    }
    LazyEither3<Throwable,String,Integer> just;
    LazyEither3<String,String,Integer> left2;
    LazyEither3<String,String,Integer> none;
    @Before
    public void setUp() throws Exception {
        just = right(10);
        none = LazyEither3.left1("none");
        left2 = LazyEither3.left2("left2");
    }
    @Test
    public void isLeftRight(){
        assertTrue(just.isRight());
        assertTrue(none.isLeft1());
        assertTrue(left2.isLeft2());
    }

   


    @Test
    public void nest(){
       assertThat(just.nest().map(m->m.toOptional().get()),equalTo(LazyEither3.right(10)));

    }
    @Test
    public void coFlatMap(){
        assertThat(just.coflatMap(m-> m.isPresent()? m.toOptional().get() : 50),equalTo(LazyEither3.right(10)));
        assertThat(none.coflatMap(m-> m.isPresent()? m.toOptional().get() : 50),equalTo(LazyEither3.right(50)));
    }

    @Test
    public void visit(){
        
        assertThat(just.visit(secondary->"no", left2->"left2", primary->"yes"),equalTo("yes"));
        assertThat(none.visit(secondary->"no", left2->"left2", primary->"yes"),equalTo("no"));
        assertThat(left2.visit(secondary->"no", left2->"left2", primary->"yes"),equalTo("left2"));
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
        assertThat(Ior.primary(1),equalTo(Ior.primary(1)));
    }

    

    

    

   

    @Test
    public void testUnitT() {
        assertThat(just.unit(20),equalTo(LazyEither3.right(20)));
    }

    

    @Test
    public void testisPrimary() {
        assertTrue(just.isRight());
        assertFalse(none.isRight());
    }

    
    @Test
    public void testMapFunctionOfQsuperTQextendsR() {
        assertThat(just.map(i->i+5),equalTo(LazyEither3.right(15)));

    }

    @Test
    public void testFlatMap() {
        assertThat(just.flatMap(i-> LazyEither3.right(i+5)),equalTo(LazyEither3.right(15)));
        assertThat(none.flatMap(i-> LazyEither3.right(i+5)),equalTo(LazyEither3.left1("none")));
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
        
        assertThat(async.orElse(Stream.empty()).collect(Collectors.toList()),equalTo(ListX.of(10)));
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
        Ior<String,Integer> ior = none.toIor();
        assertTrue(ior.isSecondary());
        assertThat(ior,equalTo(Ior.secondary("none")));
        
    }


    @Test
    public void testToIorSecondary() {
        assertThat(just.toIor().swap(),equalTo(Ior.secondary(10)));
    }
    

    @Test
    public void testToIorSecondaryNone(){
        Ior<Integer,String> ior = none.toIor().swap();
        assertTrue(ior.isPrimary());
        assertThat(ior,equalTo(Ior.primary("none")));
        
    }



    @Test
    public void testMkString() {
        assertThat(just.mkString(),equalTo("CompletableEither3[10]"));
        assertThat(none.mkString(),equalTo("Either3.left1[none]"));
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
        assertThat(just.map(i->i+5),equalTo(LazyEither3.right(15)));
    }
    
    @Test
    public void testPeek() {
        Mutable<Integer> capture = Mutable.of(null);
        just = just.peek(c->capture.set(c));
        just.get();
        
        assertThat(capture.get(),equalTo(10));
    }

    private Trampoline<Integer> sum(int times, int sum){
        return times ==0 ?  Trampoline.done(sum) : Trampoline.more(()->sum(times-1,sum+times));
    }
    @Test
    public void testTrampoline() {
        assertThat(just.trampoline(n ->sum(10,n)),equalTo(LazyEither3.right(65)));
    }

    

    @Test
    public void testUnitT1() {
        assertThat(none.unit(10),equalTo(LazyEither3.right(10)));
    }

  
}
