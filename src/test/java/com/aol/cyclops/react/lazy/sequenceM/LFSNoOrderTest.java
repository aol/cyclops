package com.aol.cyclops.react.lazy.sequenceM;

import static com.aol.cyclops.types.futurestream.LazyFutureStream.of;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.types.stream.reactive.FlatMapConfig;




//see BaseSequentialSeqTest for in order tests
public  class LFSNoOrderTest {
	
	
	LazyFutureStream<Integer> empty;
	LazyFutureStream<Integer> nonEmpty;
	public static Executor ex =  Executors.newFixedThreadPool(10);
    public static final LazyReact r = new LazyReact(10,10);
	@Before
	public void setup(){
		empty = of();
		nonEmpty = of(1);
	}
	@Test
    public void stream(){
        assertThat(of(1,2,3).stream().toListX(),hasItems(1,2,3));
    }

	@Test
    public void mergePublisher() throws InterruptedException{
      
        assertThat(of(1,2,3)
                        .mergePublisher(Arrays.asList(Maybe.of(4),Maybe.of(5)))
                        .toListX(),hasItems(1,2,3,4,5));
        
    }
	
    @Test
    public void mergePublisherSize() throws InterruptedException{
      
        assertThat(of(1,2,3)
                        .mergePublisher(Arrays.asList(Maybe.of(4),Maybe.of(5)))
                        .toListX().size(),equalTo(5));
        
    }
    @Test
    public void mergePublisherWithAsync() throws InterruptedException{
        assertThat(of(1,2,3)
                        .mergePublisher(Arrays.asList(Maybe.of(4),Maybe.of(5)),FlatMapConfig.unbounded(Executors.newFixedThreadPool(1)))
                        .toListX(),hasItems(1,2,3,4,5));
        
    }
    @Test
    public void mergePublisherWithSizeAsync() throws InterruptedException{
        assertThat(of(1,2,3)
                        .mergePublisher(Arrays.asList(Maybe.of(4),Maybe.of(5)),FlatMapConfig.unbounded(Executors.newFixedThreadPool(1)))
                        .toListX().size(),equalTo(5));
        
    }
    
    @Test
    public void mergePublisherAsync() throws InterruptedException{
       
       
       
      assertThat(of(3,2,1)
               .mergePublisher(ReactiveSeq.generate(()->r.generate(()->1).peek(a->sleep2(a*100)).limit(5).async()).limit(2).toList())
               .toListX().size(),equalTo(13));
    }
    @Test
    public void flatMapPublisher() throws InterruptedException{
        
        assertThat(of(1,2,3)
                        .flatMapPublisher(i->Maybe.of(i))
                        .toListX(),equalTo(Arrays.asList(1,2,3)));
        
        
    }
    
    @Test
    public void flatMapPublisherWithAsync() throws InterruptedException{
        for(int x=0;x<10_000;x++){
        assertThat(of(1,2,3)
                        .flatMapPublisher(i->Maybe.of(i),FlatMapConfig.unbounded(ex))
                        .toListX(),hasItems(1,2,3));
        }
        
    }
    private void sleep2(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Test
    public void flatMapPublisherAsync() throws InterruptedException{
       LazyReact r = new LazyReact(10,10);
       
      
      assertThat(of(3,2,1)
               .flatMapPublisher(i-> r.generate(()->i).peek(a->sleep2(a*100)).limit(5).async())
               .toListX().size(),equalTo(15));
       
    }
	
	protected Object value() {
		
		return "jello";
	}
	private int value2() {
		
		return 200;
	}
	@Test
    public void groupedFunction(){
        assertThat(of(1,2,3).grouped(f-> f<3? "a" : "b").count(),equalTo((2L)));
        assertThat(of(1,2,3).grouped(f-> f<3? "a" : "b").filter(t->t.v1.equals("a"))
                        .map(t->t.v2).map(s->s.toList()).single(),
                            equalTo((Arrays.asList(1,2))));
    }
    @Test
    public void groupedFunctionCollector(){
        assertThat(of(1,2,3).grouped(f-> f<3? "a" : "b",Collectors.toList()).count(),equalTo((2L)));
        assertThat(of(1,2,3).grouped(f-> f<3? "a" : "b",Collectors.toList()).filter(t->t.v1.equals("a"))
                .map(t->t.v2).single(),
                    equalTo((Arrays.asList(1,2))));
    }
	@Test
	public void toStream(){
		List<Integer> list = LazyFutureStream.of(1,2,3).<Integer>toStream().collect(Collectors.toList());
		assertThat(list,equalTo(Arrays.asList(1,2,3)));
	}
	@Test
	public void batchBySize(){
		System.out.println(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()));
		assertThat(of(1,2,3,4,5,6).grouped(3).collect(Collectors.toList()).size(),is(2));
	}
	

	

	
	@Test
	public void limitWhileTest(){
		
		List<Integer> list = new ArrayList<>();
		while(list.size()==0){
			list = of(1,2,3,4,5,6).limitWhile(it -> it<4)
						.peek(it -> System.out.println(it)).collect(Collectors.toList());
	
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
    public void testReverse() {

        assertThat( of(1, 2, 3).reverse().toList(), equalTo(asList(3, 2, 1)));
    }
    @Test
    public void testReverseList() {
    	
        assertThat( LazyFutureStream.lazyFutureStreamFromIterable(Arrays.asList(10,400,2,-1))
        				.reverse().toList(), equalTo(asList(-1, 2, 400,10)));
    }
    @Test
    public void testReverseListLimit() {
    	
        assertThat( LazyFutureStream.lazyFutureStreamFromIterable(Arrays.asList(10,400,2,-1)).reverse().limit(2)
        				.toList(), equalTo(asList(-1, 2)));
    }
    @Test
    public void testReverseRange() {
    	
        assertThat( LazyFutureStream.lazyFutureStream(IntStream.range(0,10).boxed())
        				.reverse().toList(), equalTo(asList(9,8,7,6,5,4,3,2,1,0)));
    }

   
    @Test
    public void testCycle() {
    	
    	   assertEquals(asList(1, 1, 1, 1, 1,1),of(1).cycle().limit(6).toList());
      
    }
    
    @Test
    public void testIterable() {
        List<Integer> list = of(1, 2, 3).toCollection(LinkedList::new);

        for (Integer i :of(1, 2, 3)) {
            assertThat(list,hasItem(i));
        }
    }
	
	@Test
	public void testDuplicate(){
		 Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).duplicateSequence();
		 assertTrue(copies.v1.anyMatch(i->i==2));
		 assertTrue(copies.v2.anyMatch(i->i==2));
	}
	@Test
	public void testTriplicate(){
		 Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).triplicate();
		 assertTrue(copies.v1.anyMatch(i->i==2));
		 assertTrue(copies.v2.anyMatch(i->i==2));
		 assertTrue(copies.v3.anyMatch(i->i==2));
	}
	
	@Test
	public void testQuadriplicate(){
		 Tuple4<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).quadruplicate();
		 assertTrue(copies.v1.anyMatch(i->i==2));
		 assertTrue(copies.v2.anyMatch(i->i==2));
		 assertTrue(copies.v3.anyMatch(i->i==2));
		 assertTrue(copies.v4.anyMatch(i->i==2));
	}

	@Test
	public void testDuplicateFilter(){
		 Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).duplicateSequence();
		 assertTrue(copies.v1.filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies.v2.filter(i->i%2==0).toList().size()==3);
	} 
	@Test
	public void testTriplicateFilter(){
		Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).triplicate();
		 assertTrue(copies.v1.filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies.v2.filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies.v3.filter(i->i%2==0).toList().size()==3);
	} 
	@Test
	public void testQuadriplicateFilter(){
		 Tuple4<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).quadruplicate();
		 assertTrue(copies.v1.filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies.v2.filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies.v3.filter(i->i%2==0).toList().size()==3);
		 assertTrue(copies.v4.filter(i->i%2==0).toList().size()==3);
	}
	@Test
	public void testDuplicateLimit(){
		 Tuple2<ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).duplicateSequence();
		 assertTrue(copies.v1.limit(3).toList().size()==3);
		 assertTrue(copies.v2.limit(3).toList().size()==3);
	} 
	@Test
	public void testTriplicateLimit(){
		Tuple3<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).triplicate();
		 assertTrue(copies.v1.limit(3).toList().size()==3);
		 assertTrue(copies.v2.limit(3).toList().size()==3);
		 assertTrue(copies.v3.limit(3).toList().size()==3);
	} 
	@Test
	public void testQuadriplicateLimit(){
		 Tuple4<ReactiveSeq<Integer>, ReactiveSeq<Integer>, ReactiveSeq<Integer>,ReactiveSeq<Integer>> copies =of(1,2,3,4,5,6).quadruplicate();
		 assertTrue(copies.v1.limit(3).toList().size()==3);
		 assertTrue(copies.v2.limit(3).toList().size()==3);
		 assertTrue(copies.v3.limit(3).toList().size()==3);
		 assertTrue(copies.v4.limit(3).toList().size()==3);
	}
	    @Test
	    public void testCastException() {
	    	of(1, "a", 2, "b", 3, null)
	    			.peek(it ->System.out.println(it))
	    			.cast(Integer.class)
	    			.peek(i->System.out.println(i.getClass()))
	    				.peek(it ->System.out.println(it))
	    				.toList()
	    				.stream().map(i->i.getClass())
	    				.allMatch(c->Integer.class.equals(c));
	    		
	    }
	   
	   

		
	    @Test
	    public void testGroupByEager() {
	        Map<Integer, List<Integer>> map1 =of(1, 2, 3, 4).groupBy(i -> i % 2);
	       
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
	        Supplier<LazyFutureStream<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertTrue(s.get().skipWhile(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));
	      
	        assertEquals(asList(), s.get().skipWhile(i -> true).toList());
	    }

	    @Test
	    public void testSkipUntil() {
	        Supplier<LazyFutureStream<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().skipUntil(i -> false).toList());
	        assertTrue(s.get().skipUntil(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
		  }

	    @Test
	    public void testSkipUntilWithNulls() {
	        Supplier<LazyFutureStream<Integer>> s = () -> of(1, 2, null, 3, 4, 5);
	       
	        assertTrue(s.get().skipUntil(i -> true).toList().containsAll(asList(1, 2, null, 3, 4, 5)));
	    }

	    @Test
	    public void testLimitWhile() {
	        Supplier<LazyFutureStream<Integer>> s = () -> of(1, 2, 3, 4, 5);

	        assertEquals(asList(), s.get().limitWhile(i -> false).toList());
	        assertTrue( s.get().limitWhile(i -> i < 3).toList().size()!=5);       
	        assertTrue(s.get().limitWhile(i -> true).toList().containsAll(asList(1, 2, 3, 4, 5)));
	    }

	    @Test
	    public void testLimitUntil() {
	        

	        assertTrue(of(1, 2, 3, 4, 5).limitUntil(i -> false).toList().containsAll(asList(1, 2, 3, 4, 5)));
	        assertFalse(of(1, 2, 3, 4, 5).limitUntil(i -> i % 3 == 0).toList().size()==5);
	        
	        assertEquals(asList(), of(1, 2, 3, 4, 5).limitUntil(i -> true).toList());
	    }

	    @Test
	    public void testLimitUntilWithNulls() {
	       
	    	System.out.println(of(1, 2, null, 3, 4, 5).limitUntil(i -> false).toList());
	        assertTrue(of(1, 2, null, 3, 4, 5).limitUntil(i -> false).toList().containsAll(asList(1, 2, null, 3, 4, 5)));
	    }

	    

	    @Test
	    public void testMinByMaxBy() {
	        Supplier<LazyFutureStream<Integer>> s = () -> of(1, 2, 3, 4, 5, 6);

	        assertEquals(1, (int) s.get().maxBy(t -> Math.abs(t - 5)).get());
	        assertEquals(5, (int) s.get().minBy(t -> Math.abs(t - 5)).get());

	        assertEquals(6, (int) s.get().maxBy(t -> "" + t).get());
	        assertEquals(1, (int) s.get().minBy(t -> "" + t).get());
	    }

	   
	   

	    @Test
	    public void testFoldLeft() {
	    	for(int i=0;i<100;i++){
		        Supplier<LazyFutureStream<String>> s = () -> of("a", "b", "c");
	
		        assertTrue(s.get().reduce("", String::concat).contains("a"));
		        assertTrue(s.get().reduce("", String::concat).contains("b"));
		        assertTrue(s.get().reduce("", String::concat).contains("c"));
		       
		        assertEquals(3, (int) s.get().map(str->str.length()).foldLeft(0, (u, t) -> u + t));
	
		        
		        assertEquals(3, (int) s.get().map(str->str.length()).foldRight(0, (t, u) -> u + t));
	    	}
	    }
	    
	    @Test
	    public void testFoldRight(){
	    	 	Supplier<LazyFutureStream<String>> s = () -> of("a", "b", "c");

		        assertTrue(s.get().foldRight("", String::concat).contains("a"));
		        assertTrue(s.get().foldRight("", String::concat).contains("b"));
		        assertTrue(s.get().foldRight("", String::concat).contains("c"));
		        assertEquals(3, (int) s.get().map(str->str.length())
		        					.foldRight(0, (t, u) -> u + t));
	    }
	    
	   
	    //tests converted from lazy-seq suite
	    @Test
		public void flattenEmpty() throws Exception {
				assertTrue(LazyFutureStream.<Integer>of().flatten().toList().isEmpty());
		}

		@Test
		public void flatten() throws Exception {
			assertThat(LazyFutureStream.of(Arrays.asList(1,2)).flatten().toList().size(),equalTo(asList(1,  2).size()));		
		}

		

		@Test
		public void flattenEmptyStream() throws Exception {
			
			assertThat(LazyFutureStream.<Integer>of(1,2,3,4,5,5,6,8,9,10).flatten().limit(10).collect(Collectors.toList()).size(),
											equalTo(asList(2, 3, 4, 5, 6, 7, 0, 0, 0, 0).size()));
		}
		
		
	
}