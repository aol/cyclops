package com.aol.cyclops2.functions.collections.extensions;


import com.aol.cyclops2.data.collections.extensions.CollectionX;
import com.aol.cyclops2.data.collections.extensions.FluentCollectionX;

import com.aol.cyclops2.util.SimpleTimer;
import cyclops.async.LazyReact;
import cyclops.collections.mutable.ListX;
import cyclops.collections.immutable.VectorX;
import cyclops.companion.*;
import cyclops.control.Maybe;
import cyclops.control.Trampoline;
import cyclops.function.Monoid;
import cyclops.monads.AnyM;
import cyclops.stream.ReactiveSeq;
import cyclops.stream.Streamable;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.reactivestreams.Subscription;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static org.hamcrest.Matchers.*;
import static org.jooq.lambda.tuple.Tuple.tuple;
import static org.junit.Assert.*;

public abstract class AbstractCollectionXTest {
	public abstract <T> FluentCollectionX<T> empty();
	public abstract <T> FluentCollectionX<T> of(T... values);
	public abstract  FluentCollectionX<Integer> range(int start, int end);
	public abstract  FluentCollectionX<Long> rangeLong(long start, long end);
	public abstract <T> FluentCollectionX<T> iterate(int times, T seed, UnaryOperator<T> fn);
	public abstract <T> FluentCollectionX<T> generate(int times,Supplier<T> fn);
	public abstract <U,T> FluentCollectionX<T> unfold(U seed,Function<? super U,Optional<Tuple2<T,U>>> unfolder);
	public static final LazyReact r = new LazyReact(10,10);
	
	int captured=-1;

	static Executor ex = Executors.newFixedThreadPool(1);
    boolean set = false;
    @Test
    public void isLazy(){
        of(1,2,3).filterNot(i->{
            set = true;
            return i==1;
        });
        assertFalse(set);
    }
    @Test
    public void foldFuture(){
        assertThat(of(1,2,3).foldFuture(ex, l->l.reduce(Monoids.intSum)).get(),equalTo(6));
    }
    @Test
    public void foldLazy(){
        assertThat(of(1,2,3).foldLazy(l->l.reduce(Monoids.intSum)).get(),equalTo(6));
    }
    @Test
    public void foldTry(){
        assertThat(of(1,2,3).foldTry(l->l.reduce(Monoids.intSum), Throwable.class).get(),equalTo(6));
    }
    @Test
    public void plusLoop(){
        assertThat(of(0,1,2).plusLoop(10,i->i+100).size(),equalTo(13));
    }
    @Test
    public void plusLoopOpt(){
        int[] i = {10};
        assertThat(of(0,1,2).plusLoop(()->i[0]!=20? Optional.of(i[0]++) : Optional.empty()).size(),equalTo(13));
    }
    @Test
    public void subscribeEmpty(){
        List result = new ArrayList<>();
        Subscription s= of().forEachSubscribe(i->result.add(i));
        s.request(1l);
        assertThat(result.size(),equalTo(0));
        s.request(1l);
        assertThat(result.size(),equalTo(0));
        s.request(1l);
        assertThat(result.size(),equalTo(0));

    }
    @Test
    public void subscribe(){
        List<Integer> result = new ArrayList<>();
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i));
        s.request(1l);
        assertThat(result.size(),equalTo(1));
        s.request(1l);
        assertThat(result.size(),equalTo(2));
        s.request(1l);
        assertThat(result.size(),equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }
    @Test
    public void subscribe3(){
        List<Integer> result = new ArrayList<>();
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i));
        s.request(3l);
        assertThat(result.size(),equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }
    @Test
    public void subscribeErrorEmpty(){
        List result = new ArrayList<>();
        Subscription s= of().forEachSubscribe(i->result.add(i), e->e.printStackTrace());
        s.request(1l);
        assertThat(result.size(),equalTo(0));
        s.request(1l);
        assertThat(result.size(),equalTo(0));
        s.request(1l);
        assertThat(result.size(),equalTo(0));

    }
    @Test
    public void subscribeError(){
        List<Integer> result = new ArrayList<>();
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i), e->e.printStackTrace());
        s.request(1l);
        assertThat(result.size(),equalTo(1));
        s.request(1l);
        assertThat(result.size(),equalTo(2));
        s.request(1l);
        assertThat(result.size(),equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }
    @Test
    public void subscribe3Error(){
        List<Integer> result = new ArrayList<>();
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i), e->e.printStackTrace());
        s.request(3l);
        assertThat(result.size(),equalTo(3));
        assertThat(result,hasItems(1,2,3));
    }
    @Test
    public void subscribeErrorEmptyOnComplete(){
        List result = new ArrayList<>();
        AtomicBoolean onComplete = new AtomicBoolean(false);
        Subscription s= of().forEachSubscribe(i->result.add(i), e->e.printStackTrace(),()->onComplete.set(true));
        s.request(1l);
        assertThat(onComplete.get(),equalTo(true));
        assertThat(result.size(),equalTo(0));
        s.request(1l);
        assertThat(result.size(),equalTo(0));
        s.request(1l);
        assertThat(result.size(),equalTo(0));

    }
    @Test
    public void subscribeErrorOnComplete(){
        List<Integer> result = new ArrayList<>();
        AtomicBoolean onComplete = new AtomicBoolean(false);
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i), e->e.printStackTrace(),()->onComplete.set(true));

        assertThat(onComplete.get(),equalTo(false));
        s.request(1l);
        assertThat(result.size(),equalTo(1));
        assertThat(onComplete.get(),equalTo(false));
        s.request(1l);
        assertThat(result.size(),equalTo(2));
        assertThat(onComplete.get(),equalTo(false));
        s.request(1l);
        assertThat(result.size(),equalTo(3));
        assertThat(result,hasItems(1,2,3));
        s.request(1l);
        assertThat(onComplete.get(),equalTo(true));
    }
    @Test
    public void subscribe3ErrorOnComplete(){
        List<Integer> result = new ArrayList<>();
        AtomicBoolean onComplete = new AtomicBoolean(false);
        Subscription s= of(1,2,3).forEachSubscribe(i->result.add(i), e->e.printStackTrace(),()->onComplete.set(true));
        assertThat(onComplete.get(),equalTo(false));
        s.request(4l);
        assertThat(onComplete.get(),equalTo(true));

        assertThat(result.size(),equalTo(3));
        assertThat(result,hasItems(1,2,3));
        s.request(1l);
        assertThat(onComplete.get(),equalTo(true));
    }
    @Test
    public void iterate(){
        Iterator<Integer> it = of(1,2,3).iterator();
        List<Integer> list2 = new ArrayList<>();
        while(it.hasNext())
            list2.add(it.next());
        assertThat(list2.size(),equalTo(3));
    }
    @Test
    public void iterateStream(){
        Iterator<Integer> it = of(1,2,3).stream().iterator();
        List<Integer> list2 = new ArrayList<>();
        while(it.hasNext())
            list2.add(it.next());
        assertThat(list2.size(),equalTo(3));
    }
	@Test
	public void testRange(){
	    assertThat(range(0,2).size(),equalTo(2));
	}
	@Test
    public void testRangeLong(){
        assertThat(rangeLong(0,2).size(),equalTo(2));
    }
	@Test
    public void testIterate(){
        assertThat(iterate(5,1,i->i+1).size(),equalTo(5));
    }
	
	@Test
    public void testGenerate(){
	    count = 0;
        assertThat(generate(5,()->"hello"+(count++)).size(),equalTo(5));
    }
	@Test
    public void testUnfold(){
	    Function<Integer,Optional<Tuple2<Integer,Integer>>> fn= i-> i<=6 ? Optional.of(Tuple.tuple(i,i+1)) : Optional.empty();
	  
        assertThat(unfold(1,fn ).size(),equalTo(6));
    }
	

	@Test
	public void plusOne(){
	    assertThat(of().plus(1),hasItem(1));
	}
	@Test
    public void plusTwo(){
        assertThat(of().plus(1).plus(2),hasItems(1,2));
    }
	@Test
    public void plusOneOrder(){
        assertThat(of().plusInOrder(1),hasItem(1));
    }
	@Test
    public void plusAllOne(){
        assertThat(of().plusAll(of(1)),hasItem(1));
    }
    @Test
    public void plusAllTwo(){
        assertThat(of().plusAll(of(1)).plus(2),hasItems(1,2));
    }
    
	@Test
    public void minusOne(){
        assertThat(of().minus(1).size(),equalTo(0));
    }
	@Test
    public void minusOneNotEmpty(){
        assertThat(of(1).minus(1).size(),equalTo(0));
    }
	@Test
    public void minusOneTwoValues(){
        assertThat(of(1,2).minus(1),hasItem(2));
        assertThat(of(1,2).minus(1),not(hasItem(1)));
    }
	@Test
    public void minusAllOne(){
        assertThat(of().minusAll(of(1)).size(),equalTo(0));
    }
    @Test
    public void minusAllOneNotEmpty(){
        assertThat(of(1).minusAll(of(1)).size(),equalTo(0));
    }
    @Test
    public void minusAllOneTwoValues(){
        assertThat(of(1,2).minusAll(of(1)),hasItem(2));
        assertThat(of(1,2).minusAll(of(1)),not(hasItem(1)));
    }
	
	@Test
    public void notNull(){
        assertThat(of(1,2,3,4,5).notNull(),hasItems(1,2,3,4,5));
    }
	@Test
	public void retainAll(){
	    assertThat(of(1,2,3,4,5).retainAllI((Iterable<Integer>)of(1,2,3)),hasItems(1,2,3));
	}
	

	@Test
    public void retainAllStream(){
        assertThat(of(1,2,3,4,5).retainAllS(Stream.of(1,2,3)),hasItems(1,2,3));
    }
	@Test
    public void retainAllValues(){
        assertThat(of(1,2,3,4,5).retainAll(1,2,3),hasItems(1,2,3));
    }
	@Test
    public void removeAll(){
        assertThat(of(1,2,3,4,5).removeAllI((Iterable<Integer>)of(1,2,3)),hasItems(4,5));
    }

    @Test
    public void removeAllStream(){
        assertThat(of(1,2,3,4,5).removeAllS(Stream.of(1,2,3)),hasItems(4,5));
    }
    @Test
    public void removeAllValues(){
        assertThat(of(1,2,3,4,5).removeAll(1,2,3),hasItems(4,5));
    }
	@Test
    public void testAnyMatch(){
        assertThat(of(1,2,3,4,5).anyMatch(it-> it.equals(3)),is(true));
    }
    @Test
    public void testAllMatch(){
        assertThat(of(1,2,3,4,5).allMatch(it-> it>0 && it <6),is(true));
    }
    @Test
    public void testNoneMatch(){
        assertThat(of(1,2,3,4,5).noneMatch(it-> it==5000),is(true));
    }
    
    
    @Test
    public void testAnyMatchFalse(){
        assertThat(of(1,2,3,4,5).anyMatch(it-> it.equals(8)),is(false));
    }
    @Test
    public void testAllMatchFalse(){
        assertThat(of(1,2,3,4,5).allMatch(it-> it<0 && it >6),is(false));
    }
   
    @Test
    public void testMapReduce(){
        assertThat(of(1,2,3,4,5).map(it -> it*100).reduce( (acc,next) -> acc+next).get(),is(1500));
    }
    @Test
    public void testMapReduceSeed(){
        assertThat(of(1,2,3,4,5).map(it -> it*100).reduce( 50,(acc,next) -> acc+next),is(1550));
    }
    
    
    @Test
    public void testMapReduceCombiner(){
        assertThat(of(1,2,3,4,5).map(it -> it*100).reduce( 0,
                (acc, next) -> acc+next,
                Integer::sum),is(1500));
    }
    @Test
    public void testFindFirst(){
        assertThat(Arrays.asList(1,2,3),hasItem(of(1,2,3,4,5).filter(it -> it <3).findFirst().get()));
    }
    @Test
    public void testFindAny(){
        assertThat(Arrays.asList(1,2,3),hasItem(of(1,2,3,4,5).filter(it -> it <3).findAny().get()));
    }
    @Test
    public void testDistinct(){
        assertThat(of(1,1,1,2,1).distinct().collect(java.util.stream.Collectors.toList()).size(),is(2));
        assertThat(of(1,1,1,2,1).distinct().collect(java.util.stream.Collectors.toList()),hasItem(1));
        assertThat(of(1,1,1,2,1).distinct().collect(java.util.stream.Collectors.toList()),hasItem(2));
    }
    
   
    @Test
    public void testMax2(){
        assertThat(of(1,2,3,4,5).max((t1,t2) -> t1-t2).get(),is(5));
    }
    @Test
    public void testMin2(){
        assertThat(of(1,2,3,4,5).min((t1,t2) -> t1-t2).get(),is(1));
    }
    
   

    
   
    @Test
    public void sorted() {
        assertThat(of(1,5,3,4,2).sorted().collect(java.util.stream.Collectors.toList()),is(Arrays.asList(1,2,3,4,5)));
    }
    @Test
    public void sortedComparator() {
        assertThat(of(1,5,3,4,2).sorted((t1,t2) -> t2-t1).collect(java.util.stream.Collectors.toList()).size(),is(5));
    }
    @Test
    public void forEach() {
        List<Integer> list = new ArrayList<>();
        of(1,5,3,4,2).forEach(it-> list.add(it));
        assertThat(list,hasItem(1));
        assertThat(list,hasItem(2));
        assertThat(list,hasItem(3));
        assertThat(list,hasItem(4));
        assertThat(list,hasItem(5));
        
    }
    
    
    @Test
    public void testToArray() {
        assertThat( Arrays.asList(1,2,3,4,5),hasItem(of(1,5,3,4,2).toArray()[0]));
    }
   

    @Test
    public void testCount(){
        assertThat(of(1,5,3,4,2).count(),is(5L));
    }

    
    @Test
    public void collect(){
        assertThat(of(1,2,3,4,5).collect(java.util.stream.Collectors.toList()).size(),is(5));
        assertThat(of(1,1,1,2).collect(java.util.stream.Collectors.toSet()).size(),is(2));
    }
    @Test
    public void testFilter(){
        assertThat(of(1,1,1,2).filter(it -> it==1).collect(java.util.stream.Collectors.toList()),hasItem(1));
    }
    @Test
    public void testFilterNot(){
        assertThat(of(1,1,1,2).filterNot(it -> it==1).collect(java.util.stream.Collectors.toList()),hasItem(2));
    }
    @Test
    public void testMap2(){
        assertThat(of(1).map(it->it+100).collect(java.util.stream.Collectors.toList()).get(0),is(101));
    }
    Object val;
    @Test
    public void testPeek2(){
        val = null;
        List l = of(1).map(it->it+100)
                        .peek(it -> val=it)
                        .collect(java.util.stream.Collectors.toList());
        System.out.println(l);
        assertThat(val,is(101));
    }
	
	@SuppressWarnings("serial")
    public class X extends Exception {
    }

	@Test
	public void flatMapEmpty(){
	    assertThat(empty().flatMap(i->of(1,2,3)).size(),equalTo(0));
	}
	@Test
    public void flatMap(){
        assertThat(of(1).flatMap(i->of(1,2,3)),hasItems(1,2,3));
    }
	@Test
	public void slice(){
	    assertThat(of(1,2,3).slice(0,3),hasItems(1,2,3));
	    assertThat(empty().slice(0,2).size(),equalTo(0));
	}
	@Test
    public void testLimit(){
        assertThat(of(1,2,3,4,5).limit(2).collect(java.util.stream.Collectors.toList()).size(),is(2));
    }
	@Test
    public void testTake(){
        assertThat(of(1,2,3,4,5).take(2).collect(java.util.stream.Collectors.toList()).size(),is(2));
    }

    @Test
    public void testDrop() {
        assertThat(of(1, 2, 3, 4, 5).drop(2)
                                    .collect(java.util.stream.Collectors.toList())
                                    .size(),
                   is(3));
    }
    @Test
    public void testSkip(){
        assertThat(of(1,2,3,4,5).skip(2).collect(java.util.stream.Collectors.toList()).size(),is(3));
    }
    @Test
    public void testMax(){
        assertThat(of(1,2,3,4,5).max((t1,t2) -> t1-t2).get(),is(5));
    }
    @Test
    public void testMin(){
        assertThat(of(1,2,3,4,5).min((t1,t2) -> t1-t2).get(),is(1));
    }
	
	@Test
    public void testOnEmpty() throws X {
        assertEquals(asList(1), of().onEmpty(1).toListX());
        assertEquals(asList(1), of().onEmptyGet(() -> 1).toListX());

        assertEquals(asList(2), of(2).onEmpty(1).toListX());
        assertEquals(asList(2), of(2).onEmptyGet(() -> 1).toListX());
        assertEquals(asList(2), of(2).onEmptyThrow(() -> new X()).toListX());

        
    }
    @Test
    public void visit(){
        
        String res= of(1,2,3).visit((x,xs)-> xs.join(x>2? "hello" : "world"),
                                                              ()->"boo!");
                    
        assertThat(res,equalTo("2world3"));
    }
   
    @Test
    public void when2(){
        
        Integer res =   of(1,2,3).visit((x,xs)->x,()->10);
        System.out.println(res);
    }
    @Test
    public void whenNilOrNot(){
        String res1=    of(1,2,3).visit((x,xs)-> x>2? "hello" : "world",()->"EMPTY");
    }
 
	
	@Test
	public void testCollectable(){
		assertThat(of(1,2,3).collectors().anyMatch(i->i==2),equalTo(true));
	}
	@Test
	public void dropRight(){
		assertThat(of(1,2,3).dropRight(1).toList(),hasItems(1,2));
	}
	@Test
	public void dropRightEmpty(){
		assertThat(of().dropRight(1).toList(),equalTo(Arrays.asList()));
	}
	
	@Test
	public void dropUntil(){
		assertThat(of(1,2,3,4,5).dropUntil(p->p==2).toList().size(),lessThan(5));
	}
	@Test
	public void dropUntilEmpty(){
		assertThat(of().dropUntil(p->true).toList(),equalTo(Arrays.asList()));
	}
	@Test
	public void dropWhile(){
		assertThat(of(1,2,3,4,5).dropWhile(p->p<6).toList().size(),lessThan(1));
	}
	@Test
	public void dropWhileEmpty(){
		assertThat(of().dropWhile(p->true).toList(),equalTo(Arrays.asList()));
	}
	@Test
    public void skipUntil(){
        assertThat(of(1,2,3,4,5).skipUntil(p->p==2).toListX().size(),lessThan(5));
    }
    @Test
    public void skipUntilEmpty(){
        assertThat(of().skipUntil(p->true).toListX(),equalTo(Arrays.asList()));
    }
    @Test
    public void skipWhile(){
        assertThat(of(1,2,3,4,5).skipWhile(p->p<6).toListX().size(),lessThan(1));
    }
    @Test
    public void skipWhileEmpty(){
        assertThat(of().skipWhile(p->true).toListX(),equalTo(Arrays.asList()));
    }
	@Test
	public void filter(){
		assertThat(of(1,2,3,4,5).filter(i->i<3).toList(),hasItems(1,2));
	}
	@Test
	public void findAny(){
		assertThat(of(1,2,3,4,5).findAny().get(),lessThan(6));
	}
	@Test
	public void findFirst(){
		assertThat(of(1,2,3,4,5).findFirst().get(),lessThan(6));
	}
	
	
	
	
	CollectionX<Integer> empty;
	CollectionX<Integer> nonEmpty;

	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);
	}

	
	protected Object value() {
		
		return "jello";
	}
	private int value2() {
		
		return 200;
	}
	
	
	@Test
	public void batchBySize(){
		System.out.println(of(1,2,3,4,5,6).grouped(3).collect(java.util.stream.Collectors.toList()));
		assertThat(of(1,2,3,4,5,6).grouped(3).collect(java.util.stream.Collectors.toList()).size(),is(2));
	}
	

	

	
	@Test
	public void takeWhileTest(){
		
		List<Integer> list = new ArrayList<>();
		while(list.size()==0){
			list = of(1,2,3,4,5,6).takeWhile(it -> it<4)
						.peek(it -> System.out.println(it)).collect(java.util.stream.Collectors.toList());
	
		}
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(list.get(0)));
		
		
		
		
	}
	@Test
    public void limitWhileTest(){
        
        List<Integer> list = new ArrayList<>();
        while(list.size()==0){
            list = of(1,2,3,4,5,6).limitWhile(it -> it<4)
                        .toListX();
    
        }
        assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(list.get(0)));
        
        
        
        
    }

    @Test
    public void testScanLeftStringConcat() {
        assertThat(of("a", "b", "c").scanLeft("", String::concat).toList().size(),
        		is(4));
    }
    @Test
    public void testScanLeftSum() {
    	assertThat(of("a", "ab", "abc").map(str->str.length()).scanLeft(0, (u, t) -> u + t).toList().size(), 
    			is(asList(0, 1, 3, 6).size()));
    }
    @Test
    public void testScanRightStringConcatMonoid() {
        assertThat(of("a", "b", "c").scanRight(Monoid.of("", String::concat)).toList().size(),
            is(asList("", "c", "bc", "abc").size()));
    }
    @Test
    public void testScanRightStringConcat() {
        assertThat(of("a", "b", "c").scanRight("", String::concat).toList().size(),
            is(asList("", "c", "bc", "abc").size()));
    }
    @Test
    public void testScanRightSum() {
    	assertThat(of("a", "ab", "abc").map(str->str.length()).scanRight(0, (t, u) -> u + t).toList().size(),
            is(asList(0, 3, 5, 6).size()));

        
    }

    

   
    

   
   
    
    @Test
    public void testIterable() {
        List<Integer> list = of(1, 2, 3).to().collection(LinkedList::new);

        for (Integer i :of(1, 2, 3)) {
            assertThat(list,hasItem(i));
        }
    }
	

	   
	   
	   

		
	    @Test
	    public void testGroupByEager() {
	        Map<Integer, ListX<Integer>> map1 =of(1, 2, 3, 4).groupBy(i -> i % 2);
	       
	        assertThat(map1.get(0),hasItem(2));
	        assertThat(map1.get(0),hasItem(4));
	        assertThat(map1.get(1),hasItem(1));
	        assertThat(map1.get(1),hasItem(3));
	        
	        assertEquals(2, map1.size());

	     
	    }
	    

	    @Test
	    public void testJoin() {
	        assertEquals("123".length(),of(1, 2, 3).join().length());
	        assertEquals("1, 2, 3".length(), of(1, 2, 3).join(", ").length());
	        assertEquals("^1|2|3$".length(), of(1, 2, 3).join("|", "^", "$").length());
	        
	      
	    }

	    
	   
	  

	   
	    @Test
	    public void testSkipWhile() {
	        Supplier<CollectionX<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertTrue(s.get().dropWhile(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));
	      
	        assertEquals(asList(), s.get().dropWhile(i -> true).toList());
	    }

	    @Test
	    public void testSkipUntil() {
	        Supplier<CollectionX<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().dropUntil(i -> false).toList());
	        assertTrue(s.get().dropUntil(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
		  }

	   

	    @Test
	    public void testLimitWhile() {
	        Supplier<CollectionX<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().takeWhile(i -> false).toList());
	        assertTrue( s.get().takeWhile(i -> i < 3).toList().size()!=5);       
	        assertTrue(s.get().takeWhile(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
	    }

	    @Test
	    public void testTakeUntil() {
	        

	        assertTrue(of(1, 2, 3, 4, 5).takeUntil(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));
	        assertFalse(of(1, 2, 3, 4, 5).takeUntil(i -> i % 3 == 0).toList().size()==5);
	        
	        assertEquals(asList(), of(1, 2, 3, 4, 5).takeUntil(i -> true).toList());
	    }

	    @Test
        public void testLimitUntil() {
            

            assertTrue(of(1, 2, 3, 4, 5).limitUntil(i -> false).toListX().containsAll(asList(1, 2, 3, 4, 5)));
            assertFalse(of(1, 2, 3, 4, 5).limitUntil(i -> i % 3 == 0).toListX().size()==5);
            
            assertEquals(asList(), of(1, 2, 3, 4, 5).limitUntil(i -> true).toListX());
        }

	    

	    @Test
	    public void testMinByMaxBy() {
	        Supplier<CollectionX<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).get());
	        assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).get());

	        assertEquals(6, (int) s.get().maxBy(t -> "" + t).get());
	        assertEquals(1, (int) s.get().minBy(t -> "" + t).get());
	    }

	   
	   

		@Test
		public void onePer(){
			SimpleTimer timer = new SimpleTimer();
			System.out.println(of(1,2,3,4,5,6).onePer(1000,TimeUnit.NANOSECONDS).collect(java.util.stream.Collectors.toList()));
			assertThat(of(1,2,3,4,5,6).onePer(1000,TimeUnit.NANOSECONDS).collect(java.util.stream.Collectors.toList()).size(),is(6));
			assertThat(timer.getElapsedNanoseconds(),greaterThan(600l));
		}
		@Test
		public void xPer(){
			SimpleTimer timer = new SimpleTimer();
			System.out.println(of(1,2,3,4,5,6).xPer(6,1000,TimeUnit.NANOSECONDS).collect(java.util.stream.Collectors.toList()));
			assertThat(of(1,2,3,4,5,6).xPer(6,100000000,TimeUnit.NANOSECONDS).collect(java.util.stream.Collectors.toList()).size(),is(6));
			assertThat(timer.getElapsedNanoseconds(),lessThan(60000000l));
		}
	   

		@Test
		public void zip(){
			List<Tuple2<Integer,Integer>> list =
					of(1,2,3,4,5,6).zip(of(100,200,300,400))
													.peek(it -> System.out.println(it))
													
													.collect(java.util.stream.Collectors.toList());
			System.out.println(list);
			
			List<Integer> right = list.stream().map(t -> t.v2).collect(java.util.stream.Collectors.toList());
			
			assertThat(right,hasItem(100));
			assertThat(right,hasItem(200));
			assertThat(right,hasItem(300));
			assertThat(right,hasItem(400));
			
			List<Integer> left = list.stream().map(t -> t.v1).collect(java.util.stream.Collectors.toList());
			assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));
			
			
		}

		
		@Test
		public void testScanLeftStringConcatMonoid() {
			assertThat(of("a", "b", "c").scanLeft(Reducers.toString("")).toList(), is(asList("", "a", "ab", "abc")));
		}

		@Test
		public void testScanLeftSumMonoid() {
			
			assertThat(of("a", "ab", "abc").map(str -> str.length()).
								peek(System.out::println).scanLeft(Reducers.toTotalInt()).toList(), is(asList(0, 1, 3, 6)));
		}

		

		@Test
		public void testScanRightSumMonoid() {
			assertThat(of("a", "ab", "abc").peek(System.out::println)
										.map(str -> str.length())
										.peek(System.out::println)
										.scanRight(Reducers.toTotalInt()).toList(), is(asList(0, 3, 5, 6)));

		}	

	@Test
	public void forEach2() {

		assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), (a , b) -> a + b).toList().size(),
				equalTo(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 3, 4, 5, 6, 7, 8,
						9, 10, 11, 12).size()));
	}

	@Test
	public void forEach2Filter() {

		assertThat(of(1, 2, 3).forEach2(a -> Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), (a , b) -> a > 2 && b < 8,
				(a ,b) -> a + b).toList().size(), equalTo(Arrays.asList(3, 4, 5, 6, 7, 8, 9, 10).size()));
	}
	    
  
	@Test
	public void onEmptySwitchEmpty(){
		assertThat(of().stream()
							.onEmptySwitch(()->Stream.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(1,2,3)));
				
	}
	@Test
	public void onEmptySwitch(){
		assertThat(of(4,5,6).stream()
							.onEmptySwitch(()->Stream.of(1,2,3))
							.toList(),
							equalTo(Arrays.asList(4,5,6)));
				
	}
	
	@Test
	public void elapsedIsPositive(){
		
		
		assertTrue(of(1,2,3,4,5).stream().elapsed().noneMatch(t->t.v2<0));
	}
	@Test
	public void timeStamp(){
		
		
		assertTrue(of(1,2,3,4,5)
							.stream()
							.timestamp()
							.allMatch(t-> t.v2 <= System.currentTimeMillis()));
		

	}
	@Test
	public void elementAt0(){
		assertThat(of(1).stream().elementAt(0).v1,equalTo(1));
	}
	@Test
	public void getMultple(){
		assertThat(of(1,2,3,4,5).stream().elementAt(2).v1,equalTo(3));
	}
	@Test
	public void getMultpleStream(){
		assertThat(of(1,2,3,4,5).stream().elementAt(2).v2.toList(),equalTo(Arrays.asList(1,2,3,4,5)));
	}
	@Test(expected=NoSuchElementException.class)
	public void getMultiple1(){
		of(1).stream().elementAt(1);
	}
	@Test(expected=NoSuchElementException.class)
	public void getEmpty(){
		of().stream().elementAt(0);
	}
	@Test
	public void get0(){
		assertTrue(of(1).get(0).isPresent());
	}
	@Test
	public void getAtMultple(){
		assertThat(of(1,2,3,4,5).get(2).get(),equalTo(3));
	}
	@Test
	public void getAt1(){
		assertFalse(of(1).get(1).isPresent());
	}
	@Test
	public void elementAtEmpty(){
		assertFalse(of().get(0).isPresent());
	}
	@Test
	public void singleTest(){
		assertThat(of(1).single(),equalTo(1));
	}
	@Test(expected=UnsupportedOperationException.class)
	public void singleEmpty(){
		of().single();
	}
	@Test(expected=UnsupportedOperationException.class)
	public void single2(){
		of(1,2).single();
	}
	@Test
	public void singleOptionalTest(){
		assertThat(of(1).singleOptional().get(),equalTo(1));
	}
	@Test
	public void singleOptionalEmpty(){
		assertFalse(of().singleOptional().isPresent());
	}
	@Test
	public void singleOptonal2(){
		assertFalse(of(1,2).singleOptional().isPresent());
	}
	
	@Test
	public void limitTimeEmpty(){
		List<Integer> result = ReactiveSeq.<Integer>of()
										.peek(i->sleep(i*100))
										.limit(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		assertThat(result,equalTo(Arrays.asList()));
	}
	
	@Test
	public void skipTimeEmpty(){
		List<Integer> result = ReactiveSeq.<Integer>of()
										.peek(i->sleep(i*100))
										.skip(1000,TimeUnit.MILLISECONDS)
										.toList();
		
		
		assertThat(result,equalTo(Arrays.asList()));
	}
	private int sleep(Integer i) {
		try {
			Thread.currentThread().sleep(i);
		} catch (InterruptedException e) {
			
		}
		return i;
	}
	@Test
	public void testSkipLast(){
		assertThat(of(1,2,3,4,5)
							.skipLast(2)
							.toListX(),equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void testSkipLastEmpty(){
		assertThat(of()
							.skipLast(2)
							.stream().collect(java.util.stream.Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void testLimitLast(){
		assertThat(of(1,2,3,4,5)
							.limitLast(2)
							.stream().collect(java.util.stream.Collectors.toList()),equalTo(Arrays.asList(4,5)));
	}
	@Test
    public void testTakeRight(){
        assertThat(of(1,2,3,4,5)
                            .takeRight(2)
                            .stream().collect(java.util.stream.Collectors.toList()),equalTo(Arrays.asList(4,5)));
    }
	@Test
	public void testLimitLastEmpty(){
		assertThat(of()
							.limitLast(2)
							.stream().collect(java.util.stream.Collectors.toList()),equalTo(Arrays.asList()));
	}
	@Test
	public void endsWith(){
		assertTrue(of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(5,6)));
	}
	@Test
	public void endsWithFalse(){
		assertFalse(of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(5,6,7)));
	}
	@Test
	public void endsWithToLong(){
		assertFalse(of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmpty(){
		assertTrue(of(1,2,3,4,5,6)
				.endsWithIterable(Arrays.asList()));
	}
	@Test
	public void endsWithWhenEmpty(){
		assertFalse(of()
				.endsWithIterable(Arrays.asList(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmpty(){
		assertTrue(ReactiveSeq.<Integer>of()
				.endsWithIterable(Arrays.asList()));
	}
	@Test
	public void endsWithStream(){
		assertTrue(of(1,2,3,4,5,6)
				.endsWith(Stream.of(5,6)));
	}
	@Test
	public void endsWithFalseStream(){
		assertFalse(of(1,2,3,4,5,6)
				.endsWith(Stream.of(5,6,7)));
	}
	@Test
	public void endsWithToLongStream(){
		assertFalse(of(1,2,3,4,5,6)
				.endsWith(Stream.of(0,1,2,3,4,5,6)));
	}
	@Test
	public void endsWithEmptyStream(){
		assertTrue(of(1,2,3,4,5,6)
				.endsWith(Stream.of()));
	}
	@Test
	public void endsWithWhenEmptyStream(){
		assertFalse(of()
				.endsWith(Stream.of(1,2,3,4,5,6)));
	}
	@Test
	public void endsWithBothEmptyStream(){
		assertTrue(ReactiveSeq.<Integer>of()
				.endsWith(Stream.of()));
	}
	
	@Test
	public void streamable(){
		Streamable<Integer> repeat = (of(1,2,3,4,5,6)
												.map(i->i*2)
												).to()
												.streamable();
		
		assertThat(repeat.reactiveSeq().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.reactiveSeq().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}
	
	@Test
	public void concurrentLazyStreamable(){
		Streamable<Integer> repeat = of(1,2,3,4,5,6)
												.map(i->i*2).to()
												.lazyStreamableSynchronized();
		
		assertThat(repeat.reactiveSeq().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
		assertThat(repeat.reactiveSeq().toList(),equalTo(Arrays.asList(2,4,6,8,10,12)));
	}
	/**
	@Test
	public void splitBy(){
		assertThat( of(1, 2, 3, 4, 5, 6).reactiveStream().splitBy(i->i<4).v1.toList(),equalTo(Arrays.asList(1,2,3)));
		assertThat( of(1, 2, 3, 4, 5, 6).splitBy(i->i<4).v2.toList(),equalTo(Arrays.asList(4,5,6)));
	}
	**/
	@Test
	public void testLazy(){
		Collection<Integer> col = of(1,2,3,4,5)
											.peek(System.out::println).to()
											.lazyCollection();
		System.out.println("first!");
		col.forEach(System.out::println);
		assertThat(col.size(),equalTo(5));
	}
	@Test
	public void testLazyCollection(){
		Collection<Integer> col = of(1,2,3,4,5)
											.peek(System.out::println).to()
											.lazyCollectionSynchronized();
		System.out.println("first!");
		col.forEach(System.out::println);
		assertThat(col.size(),equalTo(5));
	}
	int peek = 0;
	@Test
	public void testPeek() {
		peek = 0 ;
		   AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c->AnyM.fromStream(c.stream()))
				  				.stream()
				  				.map(i->i*2)
				  				.peek(i-> peek=i)
				  				.collect(java.util.stream.Collectors.toList());
		assertThat(peek,equalTo(6));
	}
	@Test
	public void testMap() {
		  List<Integer> list = AnyM.fromStream(Stream.of(asList(1,3)))
				  				.flatMap(c->AnyM.fromStream(c.stream()))
				  				.stream()
				  				.map(i->i*2)
				  				.peek(System.out::println)
				  				.collect(java.util.stream.Collectors.toList());
		assertThat(Arrays.asList(2,6),equalTo(list));
	}
	@Test
	public void headAndTailTest(){
		Stream<String> s = Stream.of("hello","world");
		Iterator<String> it = s.iterator();
		String head = it.next();
		Stream<String> tail = Streams.stream(it);
		tail.forEach(System.out::println);
	}
	

	@Test(expected=ClassCastException.class)
	public void testCastPast() {
		of(1, "a", 2, "b", 3).cast(Date.class).map(d -> d.getTime()).join();
	



	}
	
	@Test(expected=ClassCastException.class)
	public void cast(){
		of(1,2,3).cast(String.class).join();
	}
	@Test
	public void xMatch(){
		assertTrue(of(1,2,3,5,6,7).xMatch(3, i-> i>4 ));
	}
	
	
	
	@Test
	public void zip2of(){
		
		List<Tuple2<Integer,Integer>> list =of(1,2,3,4,5,6)
											.zip(of(100,200,300,400).stream())
											.toListX();
				
	
		List<Integer> right = list.stream().map(t -> t.v2).collect(java.util.stream.Collectors.toList());
		assertThat(right,hasItem(100));
		assertThat(right,hasItem(200));
		assertThat(right,hasItem(300));
		assertThat(right,hasItem(400));
		
		List<Integer> left = list.stream().map(t -> t.v1).collect(java.util.stream.Collectors.toList());
		assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));

	}
	@Test
	public void zipInOrder(){
		
		List<Tuple2<Integer,Integer>> list =  of(1,2,3,4,5,6)
													.zip( of(100,200,300,400).stream())
													.toListX();
		
		assertThat(asList(1,2,3,4,5,6),hasItem(list.get(0).v1));
		assertThat(asList(100,200,300,400),hasItem(list.get(0).v2));
		
		
		
	}

	@Test
	public void zipEmpty() throws Exception {
		
		
		final CollectionX<Integer> zipped = this.<Integer>empty().zip(ReactiveSeq.<Integer>of(), (a, b) -> a + b);
		assertTrue(zipped.collect(java.util.stream.Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldReturnEmptySeqWhenZipEmptyWithNonEmpty() throws Exception {
		
		
		
		final CollectionX<Integer> zipped = this.<Integer>empty().zip(of(1,2), (a, b) -> a + b);
		assertTrue(zipped.collect(java.util.stream.Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldReturnEmptySeqWhenZipNonEmptyWithEmpty() throws Exception {
		
		
		final CollectionX<Integer> zipped = of(1,2,3).zip(this.<Integer>empty(), (a, b) -> a + b);

		
		assertTrue(zipped.collect(java.util.stream.Collectors.toList()).isEmpty());
	}

	@Test
	public void shouldZipTwoFiniteSequencesOfSameSize() throws Exception {
		
		final CollectionX<String> first = of("A", "B", "C");
		final CollectionX<Integer> second = of(1, 2, 3);

		
		final CollectionX<String> zipped = first.zip(second, (a, b) -> a + b);

		
		assertThat(zipped.collect(java.util.stream.Collectors.toList()).size(),is(3));
	}

	

	@Test
	public void shouldTrimSecondFixedSeqIfLonger() throws Exception {
		final CollectionX<String> first = of("A", "B", "C");
		final CollectionX<Integer> second = of(1, 2, 3, 4);

		
		final CollectionX<String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(java.util.stream.Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLonger() throws Exception {
		final CollectionX<String> first = of("A", "B", "C","D");
		final CollectionX<Integer> second = of(1, 2, 3);
		final CollectionX<String> zipped = first.zip(second, (a, b) -> a + b);

		
		assertThat(zipped.collect(java.util.stream.Collectors.toList()).size(),equalTo(3));
	}

	@Test
	public void testZipDifferingLength() {
		List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d")).toList();

		assertEquals(2, list.size());
		assertTrue(asList(1, 2).contains(list.get(0).v1));
		assertTrue("" + list.get(1).v2, asList(1, 2).contains(list.get(1).v1));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(0).v2));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(1).v2));

	}

	
	@Test
	public void shouldTrimSecondFixedSeqIfLongerStream() throws Exception {
		final CollectionX<String> first = of("A", "B", "C");
		final CollectionX<Integer> second = of(1, 2, 3, 4);

		
		final CollectionX<String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(java.util.stream.Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLongerStream() throws Exception {
		final CollectionX<String> first = of("A", "B", "C","D");
		final CollectionX<Integer> second = of(1, 2, 3);
		
		final CollectionX<String> zipped = first.zip(second, (a, b) -> a + b);

		
		assertThat(zipped.collect(java.util.stream.Collectors.toList()).size(),equalTo(3));
	}

	@Test
	public void testZipDifferingLengthStream() {
		List<Tuple2<Integer, String>> list = of(1, 2).zip(of("a", "b", "c", "d")).toList();

		assertEquals(2, list.size());
		assertTrue(asList(1, 2).contains(list.get(0).v1));
		assertTrue("" + list.get(1).v2, asList(1, 2).contains(list.get(1).v1));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(0).v2));
		assertTrue(asList("a", "b", "c", "d").contains(list.get(1).v2));

	}

	@Test
	public void shouldTrimSecondFixedSeqIfLongerSequence() throws Exception {
		final CollectionX<String> first = of("A", "B", "C");
		final CollectionX<Integer> second = of(1, 2, 3, 4);

		
		final CollectionX<String> zipped = first.zip(second, (a, b) -> a + b);

		assertThat(zipped.collect(java.util.stream.Collectors.toList()).size(),is(3));
	}

	@Test
	public void shouldTrimFirstFixedSeqIfLongerSequence() throws Exception {
		final CollectionX<String> first = of("A", "B", "C","D");
		final CollectionX<Integer> second = of(1, 2, 3);
		final CollectionX<String> zipped = first.zip(second, (a, b) -> a + b);

		
		assertThat(zipped.collect(java.util.stream.Collectors.toList()).size(),equalTo(3));
	}

	
	@Test
	public void testZipWithIndex() {
		assertEquals(asList(), of().zipWithIndex().toListX());

        of("a").zipWithIndex().map(t -> t.v2).printOut();
		assertThat(of("a").zipWithIndex().map(t -> t.v2).findFirst().get(), is(0l));
		assertEquals(asList(new Tuple2("a", 0L)), of("a").zipWithIndex().toListX());

	}
	
	
	
	
	@Test
	public void emptyConvert(){

		assertFalse(empty().to().optional().isPresent());
		assertFalse(empty().toListX().size()>0);
		assertFalse(empty().to().dequeX().size()>0);
		assertFalse(empty().to().linkedListX().size()>0);
		assertFalse(empty().to().queueX().size()>0);
		assertFalse(empty().to().vectorX().size()>0);
		assertFalse(empty().to().persistentQueueX().size()>0);
		assertFalse(empty().toSetX().size()>0);
		assertFalse(empty().to().sortedSetX().size()>0);
		assertFalse(empty().to().orderedSetX().size()>0);
		assertFalse(empty().to().bagX().size()>0);
		assertFalse(empty().to().persistentMapX(t->t, t->t).size()>0);
		assertFalse(empty().to().mapX(t->t,t->t).size()>0);

		assertFalse(empty().toSet().size()>0);
		assertFalse(empty().toList().size()>0);
		assertFalse(empty().to().streamable().size()>0);
		
		
	}
	@Test
	public void presentConvert(){

		assertTrue(of(1).to().optional().isPresent());
		assertTrue(of(1).toListX().size()>0);
		assertTrue(of(1).to().dequeX().size()>0);
		assertTrue(of(1).to().linkedListX().size()>0);
		assertTrue(of(1).to().queueX().size()>0);
		assertTrue(of(1).to().vectorX().size()>0);
		assertTrue(of(1).to().persistentQueueX().size()>0);
		assertTrue(of(1).toSetX().size()>0);
		assertTrue(of(1).to().sortedSetX().size()>0);
		assertTrue(of(1).to().orderedSetX().size()>0);
		assertTrue(of(1).to().bagX().size()>0);
		assertTrue(of(1).to().persistentMapX(t->t, t->t).size()>0);
		assertTrue(of(1).to().mapX(t->t,t->t).size()>0);

		assertTrue(of(1).toSet().size()>0);
		assertTrue(of(1).toList().size()>0);
		assertTrue(of(1).to().streamable().size()>0);
		
		
	}

	 
	    
	    @Test
	    public void batchBySizeCollection(){
	        
	        
	        assertThat(of(1,2,3,4,5,6).grouped(3,()->ListX.empty()).get(0).get().size(),is(3));
	        
	       // assertThat(of(1,1,1,1,1,1).grouped(3,()->new ListXImpl<>()).get(1).get().size(),is(1));
	    }
	    @Test
	    public void batchBySizeInternalSize(){
	        assertThat(of(1,2,3,4,5,6).grouped(3).collect(java.util.stream.Collectors.toList()).get(0).size(),is(3));
	    }
	    @Test
	    public void fixedDelay(){
	        SimpleTimer timer = new SimpleTimer();
	        
	        assertThat(of(1,2,3,4,5,6).fixedDelay(10000,TimeUnit.NANOSECONDS).collect(java.util.stream.Collectors.toList()).size(),is(6));
	        assertThat(timer.getElapsedNanoseconds(),greaterThan(60000l));
	    }
	    
	    
	   
	   
	    
	    @Test
	    public void testSorted() {
	        CollectionX<Tuple2<Integer, String>> t1 = of(tuple(2, "two"), tuple(1, "one"));
	        List<Tuple2<Integer, String>> s1 = t1.sorted().toList();
	        assertEquals(tuple(1, "one"), s1.get(0));
	        assertEquals(tuple(2, "two"), s1.get(1));

	        CollectionX<Tuple2<Integer, String>> t2 = of(tuple(2, "two"), tuple(1, "one"));
	        List<Tuple2<Integer, String>> s2 = t2.sorted(comparing(t -> t.v1())).toList();
	        assertEquals(tuple(1, "one"), s2.get(0));
	        assertEquals(tuple(2, "two"), s2.get(1));

	        CollectionX<Tuple2<Integer, String>> t3 = of(tuple(2, "two"), tuple(1, "one"));
	        List<Tuple2<Integer, String>> s3 = t3.sorted(t -> t.v1()).toList();
	        assertEquals(tuple(1, "one"), s3.get(0));
	        assertEquals(tuple(2, "two"), s3.get(1));
	    }

	    @Test
	    public void zip2(){
	        List<Tuple2<Integer,Integer>> list =
	                of(1,2,3,4,5,6).zipS(Stream.of(100,200,300,400))
	                                                .peek(it -> System.out.println(it))
	                                                
	                                                .collect(java.util.stream.Collectors.toList());
	        
	        List<Integer> right = list.stream().map(t -> t.v2).collect(java.util.stream.Collectors.toList());
	        assertThat(right,hasItem(100));
	        assertThat(right,hasItem(200));
	        assertThat(right,hasItem(300));
	        assertThat(right,hasItem(400));
	        
	        List<Integer> left = list.stream().map(t -> t.v1).collect(java.util.stream.Collectors.toList());
	        assertThat(Arrays.asList(1,2,3,4,5,6),hasItem(left.get(0)));
	        
	        
	    }
	    
	    

	    @Test
	    public void testReverse() {
	        assertThat( of(1, 2, 3).reverse().toList().size(), is(asList(3, 2, 1).size()));
	    }

	    @Test
	    public void testShuffle() {

	        Supplier<CollectionX<Integer>> s = () ->of(1, 2, 3);

	        assertEquals(3, ((CollectionX<Integer>)s.get().shuffle()).toListX().size());
	        assertThat(((CollectionX<Integer>)s.get().shuffle()).toListX(), hasItems(1, 2, 3));

	        
	    }
	    @Test
	    public void testShuffleRandom() {
	        Random r = new Random();
	        Supplier<CollectionX<Integer>> s = () ->of(1, 2, 3);

	        assertEquals(3, ((CollectionX<Integer>)s.get()).shuffle(r).toListX().size());
	        assertThat(((CollectionX<Integer>)s.get()).shuffle(r).toListX(), hasItems(1, 2, 3));

	        
	    }


	    
	    
	   
	    
	    

	     
	        @Test
	        public void testCastNumber() {
	            
	            of(1,  2,  3)
	                    .cast(Number.class)
	                        .peek(it ->System.out.println(it)).toList();
	            
	          
	        }
	       

	       

	        
	       

	        
	       

	    
	  

	        @Test
	        public void testSplitAtHead() {
	            assertEquals(Optional.empty(), of().headAndTail().headOptional());
	            assertEquals(asList(), of().headAndTail().tail().toList());

	            assertEquals(Optional.of(1), of(1).headAndTail().headOptional());
	            assertEquals(asList(), of(1).headAndTail().tail().toList());

	            assertEquals(Maybe.of(1), of(1, 2).headAndTail().headMaybe());
	            assertEquals(asList(2), of(1, 2).headAndTail().tail().toList());

	            assertEquals(Arrays.asList(1), of(1, 2, 3).headAndTail().headStream().toList());
	            assertEquals((Integer)2, of(1, 2, 3).headAndTail().tail().headAndTail().head());
	            assertEquals(Optional.of(3), of(1, 2, 3).headAndTail().tail().headAndTail().tail().headAndTail().headOptional());
	            assertEquals(asList(2, 3), of(1, 2, 3).headAndTail().tail().toList());
	            assertEquals(asList(3), of(1, 2, 3).headAndTail().tail().headAndTail().tail().toList());
	            assertEquals(asList(), of(1, 2, 3).headAndTail().tail().headAndTail().tail().headAndTail().tail().toList());
	        }

	        @Test
	        public void testMinByMaxBy2() {
	            Supplier<CollectionX<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	            assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).get());
	            assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).get());

	            assertEquals(6, (int) s.get().maxBy(t -> "" + t).get());
	            assertEquals(1, (int) s.get().minBy(t -> "" + t).get());
	        }

	       
	       

	        @Test
	        public void testFoldLeft() {
	            for(int i=0;i<100;i++){
	                Supplier<CollectionX<String>> s = () -> of("a", "b", "c");
	    
	                assertTrue(s.get().reduce("", String::concat).contains("a"));
	                assertTrue(s.get().reduce("", String::concat).contains("b"));
	                assertTrue(s.get().reduce("", String::concat).contains("c"));
	               
	                assertEquals(3, (int) s.get().reduce(0, (u, t) -> u + t.length()));
	    
	                
	                assertEquals(3, (int) s.get().foldRight(0, (t, u) -> u + t.length()));
	            }
	        }
	        
	        @Test
	        public void testFoldRight(){
	                Supplier<CollectionX<String>> s = () -> of("a", "b", "c");

	                assertTrue(s.get().foldRight("", String::concat).contains("a"));
	                assertTrue(s.get().foldRight("", String::concat).contains("b"));
	                assertTrue(s.get().foldRight("", String::concat).contains("c"));
	                assertEquals(3, (int) s.get().foldRight(0, (t, u) -> u + t.length()));
	        }
	        
	        @Test
	        public void testFoldLeftStringBuilder() {
	            Supplier<CollectionX<String>> s = () -> of("a", "b", "c");

	            
	            assertTrue(s.get().reduce(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("a"));
	            assertTrue(s.get().reduce(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("b"));
	            assertTrue(s.get().reduce(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("c"));
	            assertTrue(s.get().reduce(new StringBuilder(), (u, t) -> u.append("-").append(t)).toString().contains("-"));
	            
	            
	            assertEquals(3, (int) s.get().reduce(0, (u, t) -> u + t.length()));

	           
	        }

	        @Test
	        public void testFoldRighttringBuilder() {
	            Supplier<CollectionX<String>> s = () -> of("a", "b", "c");

	            
	            assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("a"));
	            assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("b"));
	            assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("c"));
	            assertTrue(s.get().foldRight(new StringBuilder(), (t, u) -> u.append("-").append(t)).toString().contains("-"));
	            
	               
	        }
	        
	        @Test
	        public void batchUntil(){
	            assertThat(of(1,2,3,4,5,6)
	                    .groupedUntil(i->false)
	                    .toListX().size(),equalTo(1));
	           
	        }
	        @Test
	        public void batchWhile(){
	            assertThat(of(1,2,3,4,5,6)
	                    .groupedWhile(i->true)
	                    .toListX()
	                    .size(),equalTo(1));
	           
	        }
	        @Test
            public void batchUntilSupplier(){
                assertThat(of(1,2,3,4,5,6)
                        .groupedUntil(i->false,()->ListX.empty())
                        .toListX().size(),equalTo(1));
               
            }
            @Test
            public void batchWhileSupplier(){
                assertThat(of(1,2,3,4,5,6)
                        .groupedWhile(i->true,()->ListX.empty())
                        .toListX()
                        .size(),equalTo(1));
               
            }
	      
	        @Test
	        public void slidingNoOrder() {
	            ListX<VectorX<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(2).toListX();

	            System.out.println(list);
	            assertThat(list.get(0).size(), equalTo(2));
	            assertThat(list.get(1).size(), equalTo(2));
	        }

	        @Test
	        public void slidingIncrementNoOrder() {
	            List<List<Integer>> list = of(1, 2, 3, 4, 5, 6).sliding(3, 2).collect(java.util.stream.Collectors.toList());

	            System.out.println(list);
	           
                assertThat(list.get(1).size(), greaterThan(1));
	        }

	        @Test
	        public void combineNoOrder(){
	            assertThat(of(1,2,3)
	                       .combine((a, b)->a.equals(b), Semigroups.intSum)
	                       .toListX(),equalTo(ListX.of(1,2,3))); 
	                       
	        }
	        @Test
	        public void groupedFunctionNoOrder(){
	            assertThat(of(1,2,3).grouped(f-> f<3? "a" : "b").count(),equalTo((2L)));
	            assertThat(of(1,2,3).grouped(f-> f<3? "a" : "b").filter(t->t.v1.equals("a"))
	                            .map(t->t.v2).map(ReactiveSeq::fromStream).map(ReactiveSeq::toListX).single(),
	                                equalTo((ListX.of(1,2))));
	        }
	        @Test
	        public void groupedFunctionCollectorNoOrder(){
	            assertThat(of(1,2,3).grouped(f-> f<3? "a" : "b", CyclopsCollectors.toListX()).count(),equalTo((2L)));
	            assertThat(of(1,2,3).grouped(f-> f<3? "a" : "b", CyclopsCollectors.toListX()).filter(t->t.v1.equals("a"))
	                    .map(t->t.v2).single(),
	                        equalTo((Arrays.asList(1,2))));
	        }
	        @Test
	        public void zip3NoOrder(){
	            List<Tuple3<Integer,Integer,Character>> list =
	                    of(1,2,3,4).zip3(of(100,200,300,400).stream(),of('a','b','c','d').stream())
	                                                    .toListX();
	            
	            System.out.println(list);
	            List<Integer> right = list.stream().map(t -> t.v2).collect(java.util.stream.Collectors.toList());
	            assertThat(right,hasItem(100));
	            assertThat(right,hasItem(200));
	            assertThat(right,hasItem(300));
	            assertThat(right,hasItem(400));
	            
	            List<Integer> left = list.stream().map(t -> t.v1).collect(java.util.stream.Collectors.toList());
	            assertThat(Arrays.asList(1,2,3,4),hasItem(left.get(0)));
	            
	            List<Character> three = list.stream().map(t -> t.v3).collect(java.util.stream.Collectors.toList());
	            assertThat(Arrays.asList('a','b','c','d'),hasItem(three.get(0)));
	            
	            
	        }
	        @Test
	        public void zip4NoOrder(){
	            List<Tuple4<Integer,Integer,Character,String>> list =
	                    of(1,2,3,4).zip4(of(100,200,300,400).stream(),of('a','b','c','d').stream(),of("hello","world","boo!","2").stream())
	                                                    .toListX();
	            System.out.println(list);
	            List<Integer> right = list.stream().map(t -> t.v2).collect(java.util.stream.Collectors.toList());
	            assertThat(right,hasItem(100));
	            assertThat(right,hasItem(200));
	            assertThat(right,hasItem(300));
	            assertThat(right,hasItem(400));
	            
	            List<Integer> left = list.stream().map(t -> t.v1).collect(java.util.stream.Collectors.toList());
	            assertThat(Arrays.asList(1,2,3,4),hasItem(left.get(0)));
	            
	            List<Character> three = list.stream().map(t -> t.v3).collect(java.util.stream.Collectors.toList());
	            assertThat(Arrays.asList('a','b','c','d'),hasItem(three.get(0)));
	        
	            List<String> four = list.stream().map(t -> t.v4).collect(java.util.stream.Collectors.toList());
	            assertThat(Arrays.asList("hello","world","boo!","2"),hasItem(four.get(0)));
	            
	            
	        }
	        
	        @Test
	        public void testIntersperseNoOrder() {
	            
	            assertThat(((CollectionX<Integer>)of(1,2,3).intersperse(0)).toListX(),hasItem(0));
	        



	        }
	     

	        
	        @Test @Ignore
	        public void testOfTypeNoOrder() {

	            
	            assertThat((((CollectionX<Number>)of(1, 0.2, 2, 0.3, 3).ofType(Number.class))).toListX(),containsInAnyOrder(1, 2, 3));

	            assertThat((((CollectionX<Number>)of(1,  0.2, 2, 0.3, 3).ofType(Number.class))).toListX(),not(containsInAnyOrder("a", "b",null)));

	            assertThat(((CollectionX<Serializable>)of(1,  0.2, 2, 0.3, 3)

	                    .ofType(Serializable.class)).toListX(),containsInAnyOrder(1, 0.2, 2,0.3, 3));

	        }

	        @Test
	        public void allCombinations3NoOrder() {
	            System.out.println(of(1, 2, 3).combinations().map(s->s.toListX()).toListX());
	            assertThat(of(1, 2, 3).combinations().map(s->s.toListX()).toListX().size(),equalTo(8));
	        }

	        @Test
	        public void emptyAllCombinationsNoOrder() {
	            assertThat(of().combinations().map(s -> s.toListX()).toListX(), equalTo(Arrays.asList(Arrays.asList())));
	        }
	        
	        @Test
	        public void emptyPermutationsNoOrder() {
	            assertThat(of().permutations().map(s->s.toList()).toList(),equalTo(Arrays.asList()));
	        }

	        @Test
	        public void permuations3NoOrder() {
	            System.out.println(of(1, 2, 3).permutations().map(s->s.toListX()).toListX());
	            assertThat(of(1, 2, 3).permutations().map(s->s.toListX()).toListX().get(0).size(),
	                    equalTo(3));
	        }

	        @Test
	        public void emptyCombinationsNoOrder() {
	            assertThat(of().combinations(2).map(s -> s.toListX()).toListX(), equalTo(Arrays.asList()));
	        }
	           
	         @Test
	        public void combinations2NoOrder() {
	             
	                assertThat(of(1, 2, 3).combinations(2).map(s->s.toListX()).toListX().get(0).size(),
	                        equalTo(2));
	            }
	    protected Object sleep(int i) {
	        try {
	            Thread.currentThread().sleep(i);
	        } catch (InterruptedException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
	        return i;
	    }
	    @Test
	    public void trampoline2Test(){
	        of(10,20,30,40)
	                 .trampoline(i-> fibonacci(i))
	                 .forEach(System.out::println);
	    }
	    @Test
	    public void trampolineTest(){
	        of(10_000,200_000,3_000_000,40_000_000)
	                 .trampoline(i-> fibonacci(i))
	                 .forEach(System.out::println);
	    }
	    Trampoline<Long> fibonacci(int i){
	        return fibonacci(i,1,0);
	    }
	    Trampoline<Long> fibonacci(int n, long a, long b) {
	        return n == 0 ? Trampoline.done(b) : Trampoline.more( ()->fibonacci(n-1, a+b, a));
	    }
	    @Test
	    public void cycleMonoidNoOrder(){
	        assertThat(of(1,2,3)
	                    .cycle(Reducers.toCountInt(),3)
	                    .toListX(),
	                    equalTo(ListX.of(3,3,3)));
	    }
	    @Test
	    public void testCycleNoOrder() {
	        assertEquals(6,of(1, 2).cycle(3).toListX().size());
	        assertEquals(6, of(1, 2, 3).cycle(2).toListX().size());
	    }
	    @Test
	    public void testCycleTimesNoOrder() {
	        assertEquals(6,of(1, 2).cycle(3).toListX().size());
	       
	    }
	    int count =0;
	    @Test
	    public void testCycleWhile() {
	        count =0;
	        assertEquals(6,of(1, 2, 3).cycleWhile(next->count++<6).toListX().size());
	       
	    }
	    @Test
	    public void testCycleUntil() {
	        count =0;
	        System.out.println("List " + of(1, 2, 3).peek(System.out::println).cycleUntil(next->count++==6).toListX());
	        count =0;
	        assertEquals(6,of(1, 2, 3).cycleUntil(next->count++==6).toListX().size());
	       
	    }
	 
}
